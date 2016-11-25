package com.example.audioplayer;

import android.app.Notification;
import android.app.Service;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.MediaSessionCompat.Callback;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.Random;


public class AudioPlayerService extends Service implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaController.MediaPlayer,
        AudioManager.OnAudioFocusChangeListener {

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
    private boolean paused = false;

    private final IBinder binder = new AudioPlayerBinder();

    private Callback mediaSessionCallback = new Callback() {
        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.d("4c0n", "onMediaButtonEvent");
            Log.d("4c0n", mediaButtonEvent.toString());
            return super.onMediaButtonEvent(mediaButtonEvent);
        }

        @Override
        public void onPlay() {
            Log.d("4c0n", "onPlay");
            play();
        }

        @Override
        public void onPause() {
            Log.d("4c0n", "onPause");
            if (paused) {
                play();
            } else {
                pause();
            }
        }

        @Override
        public void onSkipToNext() {
            Log.d("4c0n", "onSkipToNext");
            next();
        }

        @Override
        public void onSkipToPrevious() {
            Log.d("4c0n", "onSkipToPrevious");
            previous();
        }

        @Override
        public void onSeekTo(long pos) {
            Log.d("4c0n", "onSeekTo");
        }

        @Override
        public void onStop() {
            Log.d("4c0n", "onStop");

            // Stop playing
            if (mediaPlayer != null) {
                freeMediaPlayer();
            }

            // Remove notification
            stopForeground(true);

            // TODO: Update media session (or drop audio focus?)
        }
    };

    private void showNotification(String artist, String title, Bitmap albumArt) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .addAction(
                        R.drawable.ic_skip_previous_white_24dp,
                        getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                this,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                );

        if (paused) {
            builder.addAction(
                    R.drawable.ic_play_arrow_white_24dp,
                    getString(R.string.play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_PLAY
                    )
            );
        } else {
            builder.addAction(
                    R.drawable.ic_pause_white_24dp,
                    getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_PAUSE
                    )
            );
        }

        builder.addAction(
                R.drawable.ic_skip_next_white_24dp,
                getString(R.string.next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        this,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                )
        )
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mediaSession.getSessionToken())
                        .setCancelButtonIntent(
                                MediaButtonReceiver.buildMediaButtonPendingIntent(
                                        this,
                                        PlaybackStateCompat.ACTION_STOP
                                )
                        )
                        .setShowCancelButton(true)
                )
                .setShowWhen(false)
                .setWhen(0)
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_music_note_white_24dp)
                .setLargeIcon(albumArt)
                .setOngoing(true);

        startForeground(NOTIFICATION_ID, builder.build());
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

        if (cursor != null) {
            cursor.moveToPosition(position);
        } else {
            Log.e("4c0n", "Failed to init cursor!");
        }
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

        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        Bitmap albumArt = getAlbumArt();

        showNotification(artist, title, albumArt);

        updateMediaSession(artist, title, albumArt);

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

    private void initMediaSession() {
        mediaSession = new MediaSessionCompat(this, "AudioPlayerService");
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                        | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
        );

        requestAudioFocus();

        mediaSession.setCallback(mediaSessionCallback);
        mediaSession.setActive(true);
    }

    private void requestAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int result = audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
        );
        if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            Log.e("4c0n", "Failed to get audio focus!");
        }
    }

    private Bitmap getAlbumArt() {
        // TODO: do async query?
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

        if (albumArtPath != null) {
            return BitmapFactory.decodeFile(albumArtPath);
        }

        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_music_note_black_96px);
    }

    private void updateMediaSession(String artist, String title, Bitmap albumArt) {
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(
                        paused
                                ? PlaybackStateCompat.STATE_PAUSED
                                : PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                        paused ? 0 : 1
                )
                .setActions(
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        | PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                        | PlaybackStateCompat.ACTION_STOP
                ).build());

        mediaSession.setMetadata(
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Album")
                        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, getDuration())
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                        .build()
        );
    }

    private void updateNotificationAndMediaSession() {
        int pos = cursor.getPosition();

        cursor.moveToPosition(currentTrackCursorPosition);
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        Bitmap albumArt = getAlbumArt();

        // Update notification
        showNotification(artist, title, albumArt);

        // Update media session
        updateMediaSession(artist, title, albumArt);

        cursor.moveToPosition(pos);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initMediaSession();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("4c0n", "onStartCommand");
        if (intent.getAction().equals(INTENT_ACTION_START_PLAYING)) {
            initCursor(
                    (QueryParams) intent.getParcelableExtra(INTENT_EXTRA_QUERY_PARAMS),
                    intent.getIntExtra(INTENT_EXTRA_CURSOR_POSITION, -1)
            );
            initMediaPlayer(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
            );
        } else {
            KeyEvent ke = MediaButtonReceiver.handleIntent(mediaSession, intent);
            Log.d("4c0n", ke.toString());
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
        super.onDestroy();

        mediaSession.release();

        freeMediaPlayer();

        if (nextMediaPlayer != null) {
            nextMediaPlayer.release();
            nextMediaPlayer = null;
        }
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
            if (paused) {
                mediaPlayer.start();
                paused = false;

                updateNotificationAndMediaSession();

                onPlayerStartedListener.onPlayerStarted();
            } else {
                startPlaying();
            }
        } else {
            cursor.moveToPosition(currentTrackCursorPosition);
            initMediaPlayer(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
        }
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
        paused = true;

        updateNotificationAndMediaSession();
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void onAudioFocusChange(int focusChange) {
        Log.d("4c0n", "onAudioFocusChange");
        // TODO: handle change in audio focus
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

    public MediaSessionCompat.Token getMediaSessionToken() {
        return mediaSession.getSessionToken();
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
