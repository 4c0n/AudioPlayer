package com.example.audioplayer;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;


public class AudioPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayer {

    public static final String INTENT_ACTION_START_PLAYING = "startPlaying";
    public static final String INTENT_EXTRA_AUDIO_ID = "audioId";

    private static final int NOTIFICATION_ID = 32789;

    private MediaPlayer mediaPlayer;

    private final IBinder binder = new AudioPlayerBinder();

    private void showNotification() {
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle("AudioPlayer")
                .setContentText("player service")
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("4c0n", "onCreate AudioPlayerService");

        showNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("4c0n", "onBind AudioPlayerService");
        Uri uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                intent.getLongExtra(INTENT_EXTRA_AUDIO_ID, -1)
        );
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException ioe) {
            Log.e("AudioPlayerService", ioe.getMessage());
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.prepareAsync();

        return binder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        if (mediaPlayer != null) {
            mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void play() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void repeatOne() {
        mediaPlayer.setLooping(true);
    }

    @Override
    public void repeatOff() {
        mediaPlayer.setLooping(false);
    }

    @Override
    public void seekTo(int milliseconds) {
        mediaPlayer.seekTo(milliseconds);
    }

    public class AudioPlayerBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }
}
