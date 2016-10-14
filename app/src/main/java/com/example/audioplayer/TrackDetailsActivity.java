package com.example.audioplayer;

import android.content.ContentUris;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class TrackDetailsActivity extends AppCompatActivity implements
        MediaPlayer.OnPreparedListener, MediaController.MediaPlayerControl {

    public static final String INTENT_EXTRA_TRACK_ID = "trackId";
    public static final String INTENT_EXTRA_TRACK_TITLE = "trackTitle";
    public static final String INTENT_EXTRA_TRACK_ARTIST = "trackArtist";
    public static final String INTENT_EXTRA_TRACK_ALBUM_ID = "trackAlbumId";

    // TODO: Move to Service
    private MediaPlayer mediaPlayer;

    // TODO: replace MediaController with custom widget
    private MediaController mediaController;

    private void initMediaPlayer() {
        mediaController = new MediaController(this);

        Uri uri = ContentUris.withAppendedId(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                getIntent().getLongExtra(INTENT_EXTRA_TRACK_ID, -1)
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("4c0n", "onCreate");
        setContentView(R.layout.activity_track_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.track_details_activity_toolbar);
        setSupportActionBar(toolbar);

        initMediaPlayer();

        if (savedInstanceState != null) {
            return;
        }

        TrackDetailsFragment fragment = TrackDetailsFragment.newInstance(
                getIntent().getLongExtra(INTENT_EXTRA_TRACK_ALBUM_ID, -1)
        );

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.track_details_fragment_container, fragment)
                .commit();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView topText = (TextView) findViewById(R.id.menu_top_text);
        topText.setText(getIntent().getStringExtra(INTENT_EXTRA_TRACK_TITLE));

        TextView bottomText = (TextView) findViewById(R.id.menu_bottom_text);
        bottomText.setText(getIntent().getStringExtra(INTENT_EXTRA_TRACK_ARTIST));

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mediaController.setMediaPlayer(this);

        View view = findViewById(R.id.track_details_activity_layout);

        Log.d("4c0n", view == null ? "null" : view.toString());

        mediaController.setAnchorView(view);
        mediaController.setEnabled(true);
        mediaController.show(0);
        start();
    }

    @Override
    public void start() {
        mediaPlayer.start();
    }

    @Override
    public void pause() {
        mediaPlayer.pause();
    }

    @Override
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }

        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }

        return 0;
    }

    @Override
    public void seekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.isPlaying();
        }

        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 100;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return mediaPlayer.getAudioSessionId();
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
            Log.d("4c0n", "ALBUM ID: " + getArguments().getLong(ARGUMENT_ALBUM_ID));
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
                        image.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        getResources(),
                                        R.drawable.ic_music_note_black_24dp,
                                        null
                                )
                        );
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
        }
    }
}
