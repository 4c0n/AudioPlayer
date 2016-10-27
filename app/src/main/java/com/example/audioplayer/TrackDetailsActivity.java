package com.example.audioplayer;

import android.content.ContentUris;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class TrackDetailsActivity extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener,
        MediaController.MediaPlayer,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_EXTRA_QUERY_PARAMS = "queryParams";
    public static final String INTENT_EXTRA_CURSOR_POSITION = "cursorPosition";

    private static final int LOADER_ID = 0;

    private Cursor cursor;
    // TODO: Move to Service
    private MediaPlayer mediaPlayer;
    private MediaController mediaController;

    private void initTrackDetailsFragment(long albumId) {
        TrackDetailsFragment trackDetailsFragment = TrackDetailsFragment.newInstance(
                albumId
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.track_details_fragment_container, trackDetailsFragment)
                .commitAllowingStateLoss();
    }

    private void initMediaPlayer(long trackId) {
        mediaController = (MediaController) findViewById(R.id.track_details_media_controller);

        Uri uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                trackId
        );

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepareAsync();
        } catch (IOException ioe) {
            Toast.makeText(this, "Unable to play!", Toast.LENGTH_LONG).show();
            Log.e("TrackDetailsActivity", ioe.getMessage());
        }
    }

    private void initMenuText(String title, String artist) {
        TextView topText = (TextView) findViewById(R.id.menu_top_text);
        topText.setText(title);

        TextView bottomText = (TextView) findViewById(R.id.menu_bottom_text);
        bottomText.setText(artist);
    }

    private void initTrackBrowseFragment() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {

            Log.d("4c0n", "LANDSCAPE");

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

            TrackBrowseFragment fragment = new TrackBrowseFragment();
            fragment.setListAdapter(cursorAdapter);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.track_details_browse_container, fragment)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_track_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.track_details_activity_toolbar);
        setSupportActionBar(toolbar);

        /*if (savedInstanceState != null) {
            return;
        }*/

        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    protected void onPause() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onStop();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        mediaController.setMediaPlayer(this);
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
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }

        return false;
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                QueryParams params = getIntent().getParcelableExtra(INTENT_EXTRA_QUERY_PARAMS);
                return new CursorLoader(
                        this,
                        params.getContentUri(),
                        params.getProjection(),
                        params.getSelection(),
                        params.getSelectionArgs(),
                        params.getSortOrder()
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        cursor = data;
        cursor.moveToPosition(getIntent().getIntExtra(INTENT_EXTRA_CURSOR_POSITION, -1));

        initTrackDetailsFragment(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID))
        );

        initMediaPlayer(
                cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
        );

        initMenuText(
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)),
                cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
        );

        initTrackBrowseFragment();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // TODO: Handle onLoaderReset event
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

            Log.d("4c0n", "TrackDetailsFragment onCreateView");

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

    public static final class TrackBrowseFragment extends ListFragment implements
            View.OnClickListener {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_browse, container, false);
            ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
            sortButton.setOnClickListener(this);

            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
            if (adapter != null) {
                Cursor cursor = adapter.getCursor();
                setSelection(cursor.getPosition());
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
