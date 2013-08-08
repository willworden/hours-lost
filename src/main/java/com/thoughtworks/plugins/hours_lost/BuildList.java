package com.thoughtworks.plugins.hours_lost;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Created with IntelliJ IDEA.
 * User: Thoughtworker
 * Date: 8/8/13
 * Time: 8:21 AM
 * To change this template use File | Settings | File Templates.
 */
public class BuildList {

    private TreeSet<File> buildsSortedByDate;

    private String buildPath;

    public BuildList(String buildPath){
        this.buildPath = buildPath;
        buildsSortedByDate = SearchForBuilds(buildPath);
    }

    public TreeSet<File> getSortedSet(){
        buildsSortedByDate = SearchForBuilds(buildPath);
        return buildsSortedByDate;
    }

    private TreeSet<File> SearchForBuilds(String builds) {
        //tested using mac
        LinkedList<java.io.File> buildXmls = new LinkedList<java.io.File>();
        java.io.File directory = new java.io.File(builds);
        java.io.File[] fList = directory.listFiles();
        Date d = new Date();
        String todaysDate = new SimpleDateFormat("yyyy-MM-dd").format(d);     //date format OS based?

        for (java.io.File file : fList) {

            if (file.getAbsolutePath().contains(todaysDate) ) {
                java.io.File[] found = file.listFiles();
                for (int i =0; i < found.length; i++){
                    if (found[i].getName().contains("build.xml"))
                        buildXmls.add(found[i]);
                }
            } else if (file.isDirectory()) {
                buildXmls.addAll(SearchForBuilds(file.getPath()));
            }
        }
        TreeSet<java.io.File> files;
        files = new TreeSet<java.io.File>(new Comparator<java.io.File>(){
            @Override
            public int compare(java.io.File a, java.io.File b){
                String aParentName = a.getParentFile().getName();
                String bParentName = b.getParentFile().getName();
                return aParentName.compareTo(bParentName);
            }
        });
        for(java.io.File xml : buildXmls){
            files.add(xml);
        }
        return files;
    }

}
