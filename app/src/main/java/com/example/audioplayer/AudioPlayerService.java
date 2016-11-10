package com.example.audioplayer;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
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

// TODO: implement shuffle
// TODO: implement next and previous
public class AudioPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaController.MediaPlayer {

    public static final String INTENT_ACTION_START_PLAYING = "startPlaying";

    public static final String INTENT_EXTRA_QUERY_PARAMS = "queryParams";
    public static final String INTENT_EXTRA_CURSOR_POSITION = "cursorPosition";

    private static final int NOTIFICATION_ID = 32789;

    private MediaPlayer mediaPlayer;
    private MediaPlayer nextMediaPlayer;
    private OnPlayerStartedListener onPlayerStartedListener;
    private OnPlayerStoppedListener onPlayerStoppedListener;
    private Cursor cursor;
    private OnTrackChangedListener onTrackChangedListener;
    private int currentTrackCursorPosition;

    private RepeatState repeatState = RepeatState.REPEAT_OFF;

    private final IBinder binder = new AudioPlayerBinder();

    private void showNotification(String artist, String title) {
        // TODO: add PendingIntent
        /* TODO: add locks screen notification:
            https://developer.android.com/guide/topics/ui/notifiers/notifications.html#controllingMedia
         */

        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .build();

        notification.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIFICATION_ID, notification);
    }

    private void initCursor(QueryParams queryParams, int position) {
        if (cursor != null) {
            cursor.close();
        }

        cursor = getContentResolver().query(
                queryParams.getContentUri(),
                queryParams.getProjection(),
                queryParams.getSelection(),
                queryParams.getSelectionArgs(),
                queryParams.getSortOrder()
        );

        cursor.moveToPosition(position);
    }

    private void freeMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void initMediaPlayer(long trackId) {
        freeMediaPlayer();
        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }

        mediaPlayer = getMediaPlayer(trackId);
    }

    private MediaPlayer getMediaPlayer(long trackId) {
        Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, trackId);

        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
        } catch (IOException ioe) {
            Log.e("AudioPlayerService", ioe.getMessage());
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.prepareAsync();

        return mediaPlayer;
    }

    private void startPlaying() {
        currentTrackCursorPosition = cursor.getPosition();
        mediaPlayer.start();
        onTrackChangedListener.onTrackChanged(currentTrackCursorPosition);
        if (repeatState == RepeatState.REPEAT_ONE) {
            mediaPlayer.setLooping(true);
        }
        onPlayerStartedListener.onPlayerStarted();

        showNotification(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        );

        boolean lastTrack = cursor.getCount() == currentTrackCursorPosition + 1;
        if (lastTrack && repeatState == RepeatState.REPEAT_ALL) {
            cursor.moveToPosition(-1);
            lastTrack = false;
        }

        if (nextMediaPlayer == null && !lastTrack) {
            if (cursor.moveToNext()) {
                nextMediaPlayer = getMediaPlayer(
                        cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                );
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(INTENT_ACTION_START_PLAYING)) {
            initCursor(
                    (QueryParams) intent.getParcelableExtra(INTENT_EXTRA_QUERY_PARAMS),
                    intent.getIntExtra(INTENT_EXTRA_CURSOR_POSITION, -1)
            );
            initMediaPlayer(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            );
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (mp == mediaPlayer) {
            startPlaying();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        freeMediaPlayer();

        if (nextMediaPlayer != null) {
            mediaPlayer = nextMediaPlayer;
            nextMediaPlayer = null;
            startPlaying();
        } else {
            onPlayerStoppedListener.onPlayerStopped();
        }
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }

    @Override
    public void play() {
        if (mediaPlayer != null) {
            startPlaying();
        } else {
            cursor.moveToPosition(currentTrackCursorPosition);
            initMediaPlayer(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public void repeatOne() {
        mediaPlayer.setLooping(true);
        repeatState = RepeatState.REPEAT_ONE;
    }

    @Override
    public void repeatOff() {
        mediaPlayer.setLooping(false);
        repeatState = RepeatState.REPEAT_OFF;
    }

    @Override
    public void repeatAll() {
        mediaPlayer.setLooping(false);
        repeatState = RepeatState.REPEAT_ALL;

        if (cursor.getCount() == currentTrackCursorPosition + 1) {
            cursor.moveToFirst();
            nextMediaPlayer = getMediaPlayer(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            );
        }
    }

    @Override
    public void seekTo(int milliseconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(milliseconds);
        }
    }

    public void next() {
        freeMediaPlayer();

        if (nextMediaPlayer != null) {
            mediaPlayer = nextMediaPlayer;
            nextMediaPlayer = null;
            startPlaying();
        } else {
            // When we are at the last track rewind cursor and play the first track
            if (currentTrackCursorPosition == cursor.getCount() - 1) {
                cursor.moveToFirst();
                initMediaPlayer(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
            }
        }
    }

    public void setOnPlayerStartedListener(OnPlayerStartedListener listener) {
        onPlayerStartedListener = listener;
    }

    public void setOnPlayerStoppedListener(OnPlayerStoppedListener listener) {
        onPlayerStoppedListener = listener;
    }

    public void setOnTrackChangedListener(OnTrackChangedListener listener) {
        onTrackChangedListener = listener;
    }

    public RepeatState getRepeatState() {
        return repeatState;
    }

    public class AudioPlayerBinder extends Binder {
        AudioPlayerService getService() {
            return AudioPlayerService.this;
        }
    }

    interface OnTrackChangedListener {
        void onTrackChanged(int position);
    }
}
