package com.thoughtworks.plugins.hours_lost;

/**
 * Created with IntelliJ IDEA.
 * User: Thoughtworker
 * Date: 8/7/13
 * Time: 9:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElapsedTime {
    private long baseMilliseconds;
    private long hours;
    private long minutes;
    private long seconds;
    private String timeAsString;

    public ElapsedTime(long milliseconds){
        baseMilliseconds = milliseconds;


        seconds = milliseconds / 1000;

        minutes = seconds / 60;

        seconds = seconds % 60;

        hours = minutes / 60;

        minutes = minutes % 60;


        timeAsString=   String.format("%02d:%02d:%02d",
                hours, minutes, seconds);
    }

    public String getElapsedTime(){
        return timeAsString;
    }


}
