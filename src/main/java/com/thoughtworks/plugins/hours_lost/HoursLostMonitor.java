package com.thoughtworks.plugins.hours_lost;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.*;
import hudson.util.FormValidation;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.widgets.HistoryWidget;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter     ;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.servlet.ServletException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;


public class HoursLostMonitor extends Builder {

    private final String name;
    private int timeLost =0;
    //private String valueOfTimeLost= null;
    private boolean wasPreviousFail;
    private long dateSinceFailing;

    @DataBoundConstructor
    public HoursLostMonitor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        timeLost=0;
        dateSinceFailing=0;
        wasPreviousFail=false;
        //class build list
        String builds = (Hudson.getInstance().getRootPath()+ "/jobs/" + getDescriptor().getJobName() + "/builds/");
        TreeSet<File> buildList =SearchForBuilds(builds, listener);
        for( File file : buildList){
          incrementLostTime(file, listener);
        }
        ElapsedTime interval = new ElapsedTime(timeLost);
        listener.getLogger().println("time lost: " + interval.getElapsedTime());
        return true;
    }

    private TreeSet<File> SearchForBuilds(String builds, BuildListener listener) {
        LinkedList<File> buildXmls = new LinkedList<File>();
        File directory = new File (builds);
        File[] fList = directory.listFiles();
        Date d = new Date();
        String todaysDate = new SimpleDateFormat("yyyy-MM-dd").format(d);

        for (File file : fList) {

           if (file.getAbsolutePath().contains(todaysDate) ) {
               File[] found = file.listFiles();
               for (int i =0; i < found.length; i++){
                if (found[i].getName().contains("build.xml"))
                   buildXmls.add(found[i]);
               }
           } else if (file.isDirectory()) {
               buildXmls.addAll(SearchForBuilds(file.getPath(), listener));
           }
        }
        TreeSet<File> files;
        files = new TreeSet<File>(new Comparator<File>(){
            @Override
            public int compare(File a, File b){
                String aParentName = a.getParentFile().getName();
                String bParentName = b.getParentFile().getName();
                return aParentName.compareTo(bParentName);
            }
        });
        for(File xml : buildXmls){
            files.add(xml);
        }
        return files;
    }
   //class xml parser
   private void incrementLostTime(File buildXml, BuildListener listener){
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       DocumentBuilder db = null;
       try {
           BufferedReader br = new BufferedReader(new FileReader(buildXml));
           String line;
           StringBuilder sb = new StringBuilder();
           while((line=br.readLine())!= null){
               sb.append(line.trim());
           }
           String thing = sb.toString();
           thing.replaceAll("[^\\x20-\\x7e]", "");
           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder = factory.newDocumentBuilder();
           InputSource is = new InputSource(new StringReader(thing));
           Document document = builder.parse(is);

          NodeList nodeList = document.getElementsByTagName("result");
          if(nodeList.item(0).getTextContent().equalsIgnoreCase( "SUCCESS")){

               if(wasPreviousFail){

                long good=  Long.parseLong(document.getElementsByTagName("startTime").item(0).getTextContent());
                Date timeLostBadStart = new Date(dateSinceFailing);
                Date timeRecovery = new Date(good);
                    timeLost+= (timeRecovery.getTime() - timeLostBadStart.getTime());


                dateSinceFailing = 0;
               }
              wasPreviousFail=false;




           }
          else {
              wasPreviousFail=true;
                  if (dateSinceFailing == 0) {
                  dateSinceFailing= Long.parseLong(document.getElementsByTagName("startTime").item(0).getTextContent());
                  }

          }
       } catch (Exception e) {
           listener.getLogger().println("ERROR" + e.toString());  //To change body of catch statement use File | Settings | File Templates.
       }

   }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        private String jobName = "test123";

        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public String getDisplayName() {
            return "Hours lost due to red builds";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            jobName =  (String) formData.get("job name");
            save();
            return super.configure(req,formData);
        }

        public String getJobName(){
            return jobName;
        }
    }
}

