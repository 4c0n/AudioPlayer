package com.example.audioplayer;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.util.Random;


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
    private boolean shuffle = false;
    private Random random = new Random();
    private MediaSessionCompat mediaSession;

    private final IBinder binder = new AudioPlayerBinder();

    private void showNotification(String artist, String title) {
        // TODO: add PendingIntents

        // TODO: do async query
        Cursor albumCursor = getContentResolver().query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums._ID + "=?",
                new String[] {
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
                },
                null
        );

        String albumArtPath = null;
        if (albumCursor != null) {
            albumCursor.moveToFirst();
            albumArtPath = albumCursor.getString(
                    albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
            );
            albumCursor.close();
        }

        // TODO: Replace buttons with white smaller versions
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                        R.drawable.ic_skip_previous_black_24dp,
                        getString(R.string.previous),
                        null
                )
                .addAction(
                        R.drawable.ic_pause_black_24dp,
                        getString(R.string.pause),
                        null
                )
                .addAction(
                        R.drawable.ic_skip_next_black_24dp,
                        getString(R.string.next),
                        null
                )
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)
                        .setMediaSession(mediaSession.getSessionToken())
                        // TODO: set cancel button intent
                        .setShowCancelButton(true)
                )
                .setShowWhen(false)
                .setWhen(0)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .setLargeIcon(
                        albumArtPath != null
                        ? BitmapFactory.decodeFile(albumArtPath)
                        : BitmapFactory.decodeResource(
                                getResources(),
                                // TODO: Use bigger icon
                                R.drawable.ic_music_note_white_24dp
                        )
                )
                .setOngoing(true)
                .build();

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
        if (onTrackChangedListener != null) {
            onTrackChangedListener.onTrackChanged(currentTrackCursorPosition);
        }
        if (repeatState == RepeatState.REPEAT_ONE) {
            mediaPlayer.setLooping(true);
        }
        if (onPlayerStartedListener != null) {
            onPlayerStartedListener.onPlayerStarted();
        }

        showNotification(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
        );

        prepareNextMediaPlayer();
    }

    private void shuffleCursor() {
        cursor.moveToPosition(random.nextInt(cursor.getCount() - 1));
    }

    private void prepareNextMediaPlayer() {
        if (shuffle) {
            shuffleCursor();
            nextMediaPlayer = getMediaPlayer(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            );
        } else {
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
    }

    /*
    TODO: It might better to start the actual playing by having to call a method after binding.
            That way all listeners should be set and everything should be more reliable.
            There can be a race condition between onPrepared and onBind, if onBind wins the race all
            is well, otherwise the UI will be missing out on receiving some important events.
     */
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
            mediaSession = new MediaSessionCompat(getApplicationContext(), "mediaSession");
            mediaSession.setActive(true);
        } else {
            MediaButtonReceiver.handleIntent(mediaSession, intent);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        if (mediaSession != null) {
            mediaSession.release();
        }
        freeMediaPlayer();
        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }

        super.onDestroy();
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
        // TODO: Update notification replacing pause button with play button
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

    public void previous() {
        freeMediaPlayer();

        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;

            cursor.moveToPrevious();
            cursor.moveToPrevious();
        } else {
            cursor.moveToPrevious();
        }

        if (cursor.getPosition() < 0) {
            cursor.moveToLast();
        }

        initMediaPlayer(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
    }

    public void shuffle(boolean on) {
        shuffle = on;

        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }

        if (!shuffle) {
            cursor.moveToPosition(currentTrackCursorPosition);
        }

        prepareNextMediaPlayer();
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

    public boolean getShuffleState() {
        return shuffle;
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
