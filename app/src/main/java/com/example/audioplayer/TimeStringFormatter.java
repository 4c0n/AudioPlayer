package com.example.audioplayer;


import java.util.Locale;

class TimeStringFormatter {
    private int milliseconds;

    TimeStringFormatter(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    String format() {
        int totalSeconds = milliseconds / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        if (hours > 0)
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        else
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
}
