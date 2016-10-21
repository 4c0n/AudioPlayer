package com.example.audioplayer;


public class TimeStringFormatter {
    private int milliseconds;

    public TimeStringFormatter(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public String format() {
        int totalSeconds = milliseconds / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0)
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format("%02d:%02d", minutes, seconds);
    }
}
