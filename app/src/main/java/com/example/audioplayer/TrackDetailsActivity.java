package com.example.audioplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;


public class TrackDetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        TrackDetailsBrowseFragment.OnTrackSelectedListener {

    public static final String INTENT_EXTRA_QUERY_PARAMS = "queryParams";
    public static final String INTENT_EXTRA_CURSOR_POSITION = "cursorPosition";

    private static final int LOADER_ID = 0;
    private static final String SAVE_POSITION = "savePosition";

    private Cursor cursor;
    private int position = -1;
    private QueryParams queryParams;
    private MediaController mediaController;
    private AudioPlayerService playerService;
    private String title;
    private String artist;
    private Bitmap albumArt;
    private MediaSessionCompat.Token mediaSessionToken;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((AudioPlayerService.AudioPlayerBinder) service).getService();
            try {
                mediaSessionToken = playerService.getMediaSessionToken();
                MediaControllerCompat mediaControllerCompat = new MediaControllerCompat(
                        getParent(),
                        mediaSessionToken
                );
                mediaControllerCompat.registerCallback(mediaControllerCallback);
                mediaControllerCompat.getTransportControls();
                mediaController.registerWithMediaSession(mediaSessionToken);
                mediaController.setRepeatState(playerService.getRepeatState());
                mediaController.setShuffle(playerService.getShuffleState());
            } catch (RemoteException re) {
                Log.e("4c0n", re.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
        }
    };

    private MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("4c0n", "TrackDetailsActivity: onPlaybackStateChanged");
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("4c0n", "TrackDetailsActivity: onMetadataChanged");

            title = metadata.getString(MediaMetadataCompat.METADATA_KEY_TITLE);
            artist = metadata.getString(MediaMetadataCompat.METADATA_KEY_ARTIST);
            albumArt = metadata.getBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART);

            // TODO: check if changed
            setMenuText(title, artist);
            setAlbumArtImage(albumArt);
        }
    };

    // TODO: Fix playing playlists
    private void initMediaPlayer() {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), AudioPlayerService.class);
        intent.setAction(AudioPlayerService.INTENT_ACTION_START_PLAYING);
        intent.putExtra(AudioPlayerService.INTENT_EXTRA_QUERY_PARAMS, queryParams);
        intent.putExtra(AudioPlayerService.INTENT_EXTRA_CURSOR_POSITION, position);

        startService(intent);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }

    private void setMenuText(String title, String artist) {
        TextView topText = (TextView) findViewById(R.id.menu_top_text);
        topText.setText(title);

        TextView bottomText = (TextView) findViewById(R.id.menu_bottom_text);
        bottomText.setText(artist);
    }

    private void setAlbumArtImage(Bitmap image) {
        ImageView trackDetailsImage = (ImageView) findViewById(R.id.track_details_image);
        trackDetailsImage.setImageBitmap(image);
    }

    private void initTrackBrowseFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            // TODO: Place view binder in its own file
            com.example.audioplayer.TrackBrowseFragment.ViewBinder viewBinder =
                    new com.example.audioplayer.TrackBrowseFragment.ViewBinder(
                            getResources(),
                            getContentResolver()
                    );

            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                    this,
                    R.layout.browse_list_item,
                    cursor,
                    new String[] {
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM_ID
                    },
                    new int[] {
                            R.id.browse_list_top_text,
                            R.id.browse_list_bottom_text,
                            R.id.browse_list_image
                    },
                    SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
            );
            cursorAdapter.setViewBinder(viewBinder);

            TrackDetailsBrowseFragment fragment = new TrackDetailsBrowseFragment();
            fragment.setListAdapter(cursorAdapter);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_details_browse_container, fragment)
                    .commitAllowingStateLoss();
        }
    }

    private void init() {
        cursor.moveToPosition(position);

        mediaController = (MediaController) findViewById(R.id.track_details_media_controller);

        initMediaPlayer();

        initTrackBrowseFragment();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.track_details_activity_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(SAVE_POSITION);
        }

        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d("4c0n", "onConfigurationChanged");
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_track_details);

        cursor.moveToPosition(position);

        mediaController = (MediaController) findViewById(R.id.track_details_media_controller);

        initTrackBrowseFragment();

        setMenuText(title, artist);
        setAlbumArtImage(albumArt);

        try {
            mediaController.registerWithMediaSession(mediaSessionToken);
        } catch (RemoteException re) {
            Log.e("4c0n", re.getMessage());
            re.printStackTrace();
        }

        mediaController.setRepeatState(playerService.getRepeatState());
        mediaController.setShuffle(playerService.getShuffleState());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(SAVE_POSITION, position);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                queryParams = getIntent().getParcelableExtra(INTENT_EXTRA_QUERY_PARAMS);
                return new CursorLoader(
                        this,
                        queryParams.getContentUri(),
                        queryParams.getProjection(),
                        queryParams.getSelection(),
                        queryParams.getSelectionArgs(),
                        queryParams.getSortOrder()
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        if (position == -1) {
            position = getIntent().getIntExtra(INTENT_EXTRA_CURSOR_POSITION, -1);
        }
        init();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO: Handle onLoaderReset event
    }

    @Override
    public void onTrackSelected(int position) {
        this.position = position;
        // TODO: Refactor
        init();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
