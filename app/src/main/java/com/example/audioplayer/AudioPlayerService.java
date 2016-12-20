package com.example.audioplayer;

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
import android.os.Handler;
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
    private Cursor cursor;
    private int currentTrackCursorPosition;
    private RepeatState repeatState = RepeatState.REPEAT_OFF;
    private boolean shuffle = false;
    private Random random = new Random();
    private MediaSessionCompat mediaSession;
    private boolean paused = false;
    private String artist;
    private String title;
    private Bitmap albumArt;
    private int duration;

    private Handler handler = new Handler();

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
            seekTo((int) pos);
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

            // Update media session
            updateMediaSessionPlaybackState(
                    PlaybackStateCompat.STATE_STOPPED,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN
            );
        }
    };

    private Runnable positionBroadcaster = new Runnable() {
        @Override
        public void run() {
            Log.d("4c0n", "run");
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                updateMediaSessionPlaybackState(
                        PlaybackStateCompat.STATE_PLAYING,
                        currentPosition
                );
                handler.postDelayed(positionBroadcaster, 1000 - (currentPosition % 1000));
            }
        }
    };

    private void showNotification(String artist, String title, Bitmap albumArt, boolean isPlaying) {
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

        if (isPlaying) {
            builder.addAction(
                    R.drawable.ic_pause_white_24dp,
                    getString(R.string.pause),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_PAUSE
                    )
            );
        } else {
            builder.addAction(
                    R.drawable.ic_play_arrow_white_24dp,
                    getString(R.string.play),
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                            this,
                            PlaybackStateCompat.ACTION_PLAY
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

        if (repeatState == RepeatState.REPEAT_ONE) {
            mediaPlayer.setLooping(true);
        }

        artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        albumArt = getAlbumArt();
        duration = mediaPlayer.getDuration();

        updateNotificationAndMediaSession(PlaybackStateCompat.STATE_PLAYING);

        handler.post(positionBroadcaster);

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

    private void updateMediaSessionPlaybackState(int state, long position) {
        float playbackSpeed = 0;
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackSpeed = 1;
        } else if (state != PlaybackStateCompat.STATE_PAUSED
                && state != PlaybackStateCompat.STATE_STOPPED) {

            throw new IllegalArgumentException(
                    "Expected PlaybackStateCompat.STATE_PLAYING or PlaybackStateCompat.STATE_PAUSED"
            );
        }

        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(
                        state,
                        position,
                        playbackSpeed
                )
                .setActions(
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                                | PlaybackStateCompat.ACTION_PLAY
                                | PlaybackStateCompat.ACTION_PAUSE
                                | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                                | PlaybackStateCompat.ACTION_STOP
                ).build()
        );
    }

    private void updateMediaSessionMetadata(
            String artist,
            String title,
            Bitmap albumArt,
            int duration
    ) {
        mediaSession.setMetadata(
                new MediaMetadataCompat.Builder()
                        .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                        .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                        //.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, "Album")
                        .putLong(
                                MediaMetadataCompat.METADATA_KEY_DURATION,
                                duration
                        )
                        .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                        .build()
        );
    }

    private void updateNotificationAndMediaSession(int state) {
        boolean isPlaying = state == PlaybackStateCompat.STATE_PLAYING;
        // Update notification
        showNotification(artist, title, albumArt, isPlaying);

        // Update media session
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mediaPlayer != null) {
            position = mediaPlayer.getCurrentPosition();
        }
        updateMediaSessionPlaybackState(state, position);
        updateMediaSessionMetadata(artist, title, albumArt, duration);
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
            updateNotificationAndMediaSession(PlaybackStateCompat.STATE_STOPPED);
        }
    }

    private void play() {
        if (mediaPlayer != null) {
            if (paused) {
                mediaPlayer.start();
                paused = false;

                updateNotificationAndMediaSession(PlaybackStateCompat.STATE_PLAYING);
                handler.post(positionBroadcaster);
            } else {
                startPlaying();
            }
        } else {
            cursor.moveToPosition(currentTrackCursorPosition);
            initMediaPlayer(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
        }
    }

    private void pause() {
        mediaPlayer.pause();
        paused = true;

        updateNotificationAndMediaSession(PlaybackStateCompat.STATE_PAUSED);
        handler.removeCallbacks(positionBroadcaster);
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

    private void seekTo(int milliseconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(milliseconds);
        }
    }

    private void next() {
        freeMediaPlayer();

        handler.removeCallbacks(positionBroadcaster);

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

    private void previous() {
        freeMediaPlayer();

        handler.removeCallbacks(positionBroadcaster);

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
}
