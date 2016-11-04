package com.example.audioplayer;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

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

    private AudioPlayerService.OnTrackChangedListener onTrackChangedListener =
            new AudioPlayerService.OnTrackChangedListener() {
        @Override
        public void onTrackChanged(int pos) {

            Log.d("4c0n", "onTrackChanged: pos: " + pos);

            position = pos;

            cursor.moveToPosition(position);

            Log.d("4c0n", "onTrackChanged: cursor: " + cursor.getPosition());

            initTrackDetailsFragment(
                    cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
            );

            initMenuText(
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                    cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
            );

            // TODO: Refactor (we don't want a new fragment)
            initTrackBrowseFragment();
        }
    };

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerService = ((AudioPlayerService.AudioPlayerBinder) service).getService();

            mediaController.setMediaPlayer(playerService);
            playerService.setOnPlayerStartedListener(mediaController);
            playerService.setOnPlayerStoppedListener(mediaController);
            playerService.setOnTrackChangedListener(onTrackChangedListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playerService = null;
        }
    };

    private void initTrackDetailsFragment(long albumId) {
        TrackDetailsFragment trackDetailsFragment = TrackDetailsFragment.newInstance(
                albumId
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.track_details_fragment_container, trackDetailsFragment)
                .commitAllowingStateLoss();
    }

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

    private void initMenuText(String title, String artist) {
        TextView topText = (TextView) findViewById(R.id.menu_top_text);
        topText.setText(title);

        TextView bottomText = (TextView) findViewById(R.id.menu_bottom_text);
        bottomText.setText(artist);
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

        initTrackDetailsFragment(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
        );

        mediaController = (MediaController) findViewById(R.id.track_details_media_controller);

        initMediaPlayer();

        initMenuText(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        );

        // TODO: handle in onConfigurationChanged
        initTrackBrowseFragment();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("4c0n", "TrackDetailsActivity onCreate");

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
        super.onConfigurationChanged(newConfig);

        setContentView(R.layout.activity_track_details);

        cursor.moveToPosition(position);
        Log.d("4c0n", "TrackDetailsActivity onConfigurationChanged: pos: " + position + " cursor: " + cursor.getPosition());

        mediaController = (MediaController) findViewById(R.id.track_details_media_controller);

        initTrackDetailsFragment(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
        );

        initTrackBrowseFragment();

        initMenuText(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        );

        Log.d("4c0n", playerService.toString());
        mediaController.setMediaPlayer(playerService);
        playerService.setOnPlayerStartedListener(mediaController);
        playerService.setOnPlayerStoppedListener(mediaController);
        //playerService.setOnTrackChangedListener(onTrackChangedListener);
        // TODO: replace with something like updateStatus()
        mediaController.onPlayerStarted();
        Log.d("4c0n", mediaController.toString());
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

    public static final class TrackDetailsFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<Cursor> {

        private static final int IMAGE_LOADER = 1;
        private static final String ARGUMENT_ALBUM_ID = "albumId";

        public static TrackDetailsFragment newInstance(long albumId) {
            Bundle arguments = new Bundle();
            arguments.putLong(ARGUMENT_ALBUM_ID, albumId);

            TrackDetailsFragment fragment = new TrackDetailsFragment();
            fragment.setArguments(arguments);

            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                                 @Nullable Bundle savedInstanceState) {

            getLoaderManager().restartLoader(IMAGE_LOADER, null, this);

            return inflater.inflate(R.layout.fragment_track_details, container, false);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            switch (id) {
                case IMAGE_LOADER:
                    return new CursorLoader(
                            getActivity(),
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            new String[] {MediaStore.Audio.AlbumColumns.ALBUM_ART},
                            MediaStore.Audio.Albums._ID + "=?",
                            new String[] {Long.toString(getArguments().getLong(ARGUMENT_ALBUM_ID))},
                            null
                    );
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (loader.getId() == IMAGE_LOADER) {
                if (data.moveToNext()) {
                    String albumArt = data.getString(
                            data.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)
                    );

                    ImageView image =
                            (ImageView) getActivity().findViewById(R.id.track_details_image);

                    if (albumArt != null) {
                        image.setImageURI(Uri.fromFile(new File(albumArt)));
                    } else {
                        image.setImageResource(R.drawable.ic_music_note_black_192px);
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
