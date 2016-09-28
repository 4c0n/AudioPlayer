package com.example.audioplayer;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private static final String SPINNER_POSITION_KEY = "spinnerPosition";
    private static final String LIST_SORTED_ASCENDING = "sortedAscending";

    private int mSpinnerPosition = 0;
    private boolean mSortedAscending = true;

    private TrackBrowseFragment initTrackBrowseFragment() {
        Log.d("4c0n", "initTrackBrowseFragment");
        TrackBrowseFragment fragment = new TrackBrowseFragment();
        fragment.setRetainInstance(true);

        MediaStoreAudioAdapter mediaStoreAudioAdapter = new MediaStoreAudioAdapter(this,
                getContentResolver());

        fragment.setListAdapter(mediaStoreAudioAdapter);

        return fragment;
    }

    private BrowseFragment initArtistBrowseFragment() {
        Log.d("4c0n", "initArtistBrowseFragment");
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.Artists.ARTIST_KEY
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Artists._ID,
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                }
        );

        BrowseFragment fragment = new BrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.artist_browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS
                },
                new int[] {
                        R.id.artist_name,
                        R.id.artist_info,
                        R.id.artist_info
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(new ArtistBrowseFragmentViewBinder(getResources()));

        fragment.setListAdapter(adapter);

        return fragment;
    }

    private AlbumBrowseFragment initAlbumBrowseFragment() {
        AlbumBrowseFragment fragment = new AlbumBrowseFragment();
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.album_browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.AlbumColumns.ALBUM_ART,
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
                        MediaStore.Audio.AlbumColumns.ARTIST
                },
                new int[] {
                        R.id.album_album_art,
                        R.id.album_title,
                        R.id.album_info,
                        R.id.album_info
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(new AlbumBrowseFragmentViewBinder(getResources()));

        fragment.setListAdapter(adapter);

        return fragment;
    }

    private PlaylistBrowseFragment initPlaylistBrowseFragment() {
        PlaylistBrowseFragment fragment = new PlaylistBrowseFragment();
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.playlist_browse_list_item,
                null,
                new String[] {MediaStore.Audio.PlaylistsColumns.NAME},
                new int[] {R.id.playlist_name},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        fragment.setListAdapter(adapter);

        return fragment;
    }

    private GenreBrowseFragment initGenreBrowseFragment() {
        GenreBrowseFragment fragment = new GenreBrowseFragment();
        fragment.setRetainInstance(true);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.genre_browse_list_item,
                null,
                new String[] {MediaStore.Audio.GenresColumns.NAME},
                new int[] {R.id.genre_name},
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        fragment.setListAdapter(adapter);

        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("4c0n", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.media_toolbar);
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mSpinnerPosition = savedInstanceState.getInt(SPINNER_POSITION_KEY);
            mSortedAscending = savedInstanceState.getBoolean(LIST_SORTED_ASCENDING);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("4c0n", "create");

        getMenuInflater().inflate(R.menu.browse_menu, menu);

        MenuItem item = menu.findItem(R.id.menu_browse_type);
        Spinner browseTypeSpinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.browse_types, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        browseTypeSpinner.setAdapter(spinnerAdapter);
        browseTypeSpinner.setOnItemSelectedListener(this);
        browseTypeSpinner.setSelection(mSpinnerPosition);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SPINNER_POSITION_KEY, mSpinnerPosition);
        outState.putBoolean(LIST_SORTED_ASCENDING, mSortedAscending);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.sort_menu_button:
                FragmentManager fragmentManager = getSupportFragmentManager();
                Sortable fragment = (Sortable) fragmentManager.findFragmentById(
                        R.id.media_fragment_container
                );
                mSortedAscending = !mSortedAscending;
                fragment.sort(mSortedAscending);
                return true;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        TextView textView = (TextView) view;
        String text = textView.getText().toString();

        FragmentManager fragmentManager = getSupportFragmentManager();
        ListFragment fragment = (ListFragment) fragmentManager.findFragmentById(
                R.id.media_fragment_container
        );

        if (fragment != null) {
            mSortedAscending = true;
        }

        ListFragment newFragment;

        if (text.equals(getString(R.string.browse_type_tracks))) {
            Log.d("4c0n", "Tracks");
            newFragment = initTrackBrowseFragment();
        } else if (text.equals(getString(R.string.browse_type_artists))) {
            Log.d("4c0n", "Artists");
            newFragment = initArtistBrowseFragment();
        } else if (text.equals(getString(R.string.browse_type_albums))) {
            Log.d("4c0n", "Albums");
            newFragment = initAlbumBrowseFragment();
        } else if (text.equals(getString(R.string.browse_type_playlists))) {
            Log.d("4c0n", "Playlists");
            newFragment = initPlaylistBrowseFragment();
        } else if (text.equals(getString(R.string.browse_type_genres))) {
            Log.d("4c0n", "Genres");
            newFragment = initGenreBrowseFragment();
        } else {
            throw new IllegalStateException("Unsupported item selected.");
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.media_fragment_container, newFragment);
        transaction.commit();

        mSpinnerPosition = position;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // The way the spinner is set up it is impossible to select nothing
    }
}