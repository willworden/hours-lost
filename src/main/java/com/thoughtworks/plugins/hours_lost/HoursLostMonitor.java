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
import org.xml.sax.SAXException;

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
    private int timeLost;
    private boolean wasPreviousFail;
    private long dateSinceFailing;

    @DataBoundConstructor
    public HoursLostMonitor(String name) {
        this.name = name;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        timeLost=0;
        dateSinceFailing=0;
        wasPreviousFail=false;
        String builds = (Hudson.getInstance().getRootPath()+ "/jobs/test123/builds/");
        TreeSet<File> buildList = new BuildList(builds).getSortedSet();
        for( File file : buildList){
          incrementLostTime(file, listener);
        }
        ElapsedTime interval = new ElapsedTime(timeLost);
        listener.getLogger().println("time lost today: " + interval.getElapsedTime());
        return true;
    }

   private void incrementLostTime(File buildXml, BuildListener listener){
       DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
       DocumentBuilder db = null;
       try {
          Document document = XMLLoader.getDocument(buildXml);
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
            save();
            return super.configure(req,formData);
        }
    }
}

