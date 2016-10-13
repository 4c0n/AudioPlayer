package com.example.audioplayer;

import android.database.Cursor;
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
import android.widget.TextView;

import java.io.File;

public class TrackDetailsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_TRACK_ID = "trackId";
    public static final String INTENT_EXTRA_TRACK_TITLE = "trackTitle";
    public static final String INTENT_EXTRA_TRACK_ARTIST = "trackArtist";
    public static final String INTENT_EXTRA_TRACK_ALBUM_ID = "trackAlbumId";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("4c0n", "onCreate");
        setContentView(R.layout.activity_track_details);

        Toolbar toolbar = (Toolbar) findViewById(R.id.track_details_activity_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            return;
        }

        TrackDetailsFragment fragment = TrackDetailsFragment.newInstance(
                getIntent().getLongExtra(INTENT_EXTRA_TRACK_ALBUM_ID, -1)
        );

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.track_details_fragment_container, fragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        TextView topText = (TextView) findViewById(R.id.menu_top_text);
        topText.setText(getIntent().getStringExtra(INTENT_EXTRA_TRACK_TITLE));

        TextView bottomText = (TextView) findViewById(R.id.menu_bottom_text);
        bottomText.setText(getIntent().getStringExtra(INTENT_EXTRA_TRACK_ARTIST));

        return true;
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

                    SquareImageView image =
                            (SquareImageView) getActivity().findViewById(R.id.track_details_image);

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
