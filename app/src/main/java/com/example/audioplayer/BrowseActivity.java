package com.example.audioplayer;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private static final String SPINNER_POSITION_KEY = "spinnerPosition";
    private static final String LIST_SORTED_ASCENDING = "sortedAscending";

    private int mSpinnerPosition = 0;

    // TODO: let fragments retain their instance, rendering this property useless
    private boolean mSortedAscending = true;

    private BrowseFragment initTrackBrowseFragment() {
        TrackBrowseListAdapter trackBrowseListAdapter = new TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.getInstance(this, mSortedAscending);
        fragment.setListAdapter(trackBrowseListAdapter);

        return fragment;
    }

    private ArtistBrowseFragment initArtistBrowseFragment() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.ArtistColumns.ARTIST,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                        MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS,
                        MediaStore.Audio.Artists._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(
                new ArtistBrowseFragment.ArtistBrowseFragmentViewBinder(getResources())
        );

        ArtistBrowseFragment fragment = ArtistBrowseFragment.getInstance(
                this,
                mSortedAscending
        );
        fragment.setListAdapter(adapter);
        fragment.setOnItemClickListener(new ArtistBrowseListViewOnItemClickListener(this));

        return fragment;
    }

    private AlbumBrowseFragment initAlbumBrowseFragment() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.AlbumColumns.ALBUM_ART,
                        MediaStore.Audio.AlbumColumns.ALBUM,
                        MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
                        MediaStore.Audio.AlbumColumns.ARTIST
                },
                new int[] {
                        R.id.browse_list_image,
                        R.id.browse_list_top_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_bottom_text
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );
        adapter.setViewBinder(
                new AlbumBrowseFragment.AlbumBrowseFragmentViewBinder(getResources())
        );

        AlbumBrowseFragment fragment = AlbumBrowseFragment.getInstance(this, mSortedAscending);
        fragment.setListAdapter(adapter);

        return fragment;
    }

    private BrowseFragment initPlaylistBrowseFragment() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.PlaylistsColumns.NAME,
                        MediaStore.Audio.Playlists._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        adapter.setViewBinder(new PlaylistBrowseFragmentViewBinder(getResources()));

        PlaylistBrowseFragment fragment = PlaylistBrowseFragment.getInstance(this, mSortedAscending);
        fragment.setListAdapter(adapter);

        return fragment;
    }

    private BrowseFragment initGenreBrowseFragment() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.GenresColumns.NAME,
                        MediaStore.Audio.Genres._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        adapter.setViewBinder(new GenreBrowseFragmentViewBinder(getResources()));

        GenreBrowseFragment fragment = GenreBrowseFragment.getInstance(this, mSortedAscending);
        fragment.setListAdapter(adapter);

        return fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("4c0n", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media);
        Toolbar toolbar = (Toolbar) findViewById(R.id.browse_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mSpinnerPosition = savedInstanceState.getInt(SPINNER_POSITION_KEY);
            mSortedAscending = savedInstanceState.getBoolean(LIST_SORTED_ASCENDING);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("4c0n", "create");

        Spinner browseTypeSpinner = (Spinner) findViewById(R.id.browse_type_spinner);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.browse_types, android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        browseTypeSpinner.setAdapter(spinnerAdapter);
        browseTypeSpinner.setOnItemSelectedListener(this);
        browseTypeSpinner.setSelection(mSpinnerPosition);

        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(SPINNER_POSITION_KEY, mSpinnerPosition);

        FragmentManager fragmentManager = getSupportFragmentManager();
        BrowseFragment fragment = (BrowseFragment) fragmentManager.findFragmentById(
                R.id.media_fragment_container
        );

        outState.putBoolean(LIST_SORTED_ASCENDING, fragment.getSortedAscending());
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

        BrowseFragment newFragment;

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

    abstract public static class BrowseFragment extends ListFragment implements
            Sortable, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

        private static final int BROWSE_LOADER = 0;
        public static final String ARGUMENT_SORT_COLUMN = "sortColumn";
        public static final String ARGUMENT_CONTENT_URI = "contentURI";
        public static final String ARGUMENT_COLUMNS = "columns";
        public static final String ARGUMENT_SELECTION = "selection";
        public static final String ARGUMENT_EMPTY_TEXT = "empyText";

        private boolean mSortedAscending = true;

        private AdapterView.OnItemClickListener mOnItemClickListener;

        public BrowseFragment() {
            Log.d("4c0n", "BrowseFragment");
        }

        public void setSortedAscending(boolean ascending) {
            mSortedAscending = ascending;
        }

        public boolean getSortedAscending() {
            return mSortedAscending;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_browse, container, false);
            ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
            sortButton.setOnClickListener(this);

            Spinner browseTypeSpinner = (Spinner) getActivity().findViewById(R.id.browse_type_spinner);
            browseTypeSpinner.setVisibility(View.VISIBLE);

            TextView menuTextView = (TextView) getActivity().findViewById(R.id.menu_text);
            menuTextView.setVisibility(View.GONE);

            Log.d("4c0n", "onCreateView " + mSortedAscending);
            getLoaderManager().restartLoader(BROWSE_LOADER, null, this);

            return view;
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            if (mOnItemClickListener != null) {
                getListView().setOnItemClickListener(mOnItemClickListener);
            }

            super.onViewCreated(view, savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.d("4c0n", "onCreateLoader " + mSortedAscending);
            switch (id) {
                case BROWSE_LOADER:
                    Bundle arguments = getArguments();
                    String sortOrder = arguments.getString(ARGUMENT_SORT_COLUMN);
                    if (sortOrder != null) {
                        if (mSortedAscending) {
                            sortOrder += " ASC";
                        } else {
                            sortOrder += " DESC";
                        }
                    }

                    return new CursorLoader(
                            getActivity(),
                            (Uri) arguments.get(ARGUMENT_CONTENT_URI),
                            arguments.getStringArray(ARGUMENT_COLUMNS),
                            arguments.getString(ARGUMENT_SELECTION),
                            null,
                            sortOrder
                    );
                default:
                    return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.d("4c0n", "onLoadFinished");
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof SimpleCursorAdapter) {
                SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
                cursorAdapter.changeCursor(data);
            } else if (adapter instanceof TrackBrowseListAdapter) {
                TrackBrowseListAdapter trackBrowseListAdapter = (TrackBrowseListAdapter) adapter;
                trackBrowseListAdapter.changeCursor(data);
            }

            TextView emptyView = (TextView) getView().findViewById(R.id.no_data);
            View loadingView = getView().findViewById(R.id.loading_data);

            if (data.getCount() > 0) {
                emptyView.setVisibility(View.INVISIBLE);
                loadingView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(getArguments().getString(ARGUMENT_EMPTY_TEXT));
                loadingView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof SimpleCursorAdapter) {
                SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
                cursorAdapter.changeCursor(null);
            } else if (adapter instanceof TrackBrowseListAdapter) {
                TrackBrowseListAdapter trackBrowseListAdapter = (TrackBrowseListAdapter) adapter;
                trackBrowseListAdapter.changeCursor(null);
            }
        }

        @Override
        public void sort(boolean ascending) {
            mSortedAscending = ascending;
            getLoaderManager().restartLoader(BROWSE_LOADER, null, this);
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
            mOnItemClickListener = listener;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sort_menu_button) {
                sort(!mSortedAscending);
            }
        }
    }

    public static final class AlbumBrowseFragment extends BrowseFragment {
        public static AlbumBrowseFragment getInstance(Context context, boolean sortedAscending) {
            Bundle arguments = new Bundle();
            arguments.putString(
                    BrowseFragment.ARGUMENT_SORT_COLUMN,
                    MediaStore.Audio.Albums.ALBUM_KEY
            );
            arguments.putParcelable(
                    BrowseFragment.ARGUMENT_CONTENT_URI,
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
            );
            arguments.putStringArray(
                    BrowseFragment.ARGUMENT_COLUMNS,
                    new String[] {
                            MediaStore.Audio.Albums._ID,
                            MediaStore.Audio.AlbumColumns.ALBUM,
                            MediaStore.Audio.AlbumColumns.ALBUM_ART,
                            MediaStore.Audio.AlbumColumns.ARTIST,
                            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
                    }
            );
            arguments.putString(
                    BrowseFragment.ARGUMENT_EMPTY_TEXT,
                    context.getString(R.string.no_albums)
            );

            AlbumBrowseFragment fragment = new AlbumBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setSortedAscending(sortedAscending);

            return fragment;
        }

        static final class AlbumBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;

            AlbumBrowseFragmentViewBinder(Resources resources) {
                mResources = resources;
            }

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.browse_list_bottom_text) {
                    if (columnIndex == 3) {
                        // Album info
                        int numberOfTracks = cursor.getInt(
                                cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                        );
                        String artist = cursor.getString(
                                cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ARTIST)
                        );
                        TextView textView = (TextView) view;
                        textView.setText(
                                mResources.getString(
                                        R.string.album_info,
                                        mResources.getQuantityString(
                                                R.plurals.tracks,
                                                numberOfTracks,
                                                numberOfTracks
                                        ),
                                        artist
                                )
                        );

                        return true;
                    }

                    if (columnIndex == 4) {
                        return true;
                    }
                } else if (view.getId() == R.id.browse_list_image) {
                    String albumArtPath = cursor.getString(columnIndex);
                    if (albumArtPath == null) {
                        ImageView imageView = (ImageView) view;
                        imageView.setImageDrawable(
                                ResourcesCompat.getDrawable(
                                        mResources,
                                        R.drawable.ic_album_black_24dp,
                                        null
                                )
                        );

                        return true;
                    }
                }

                return false;
            }
        }
    }

    public static final class ArtistBrowseFragment extends BrowseFragment {
        public static ArtistBrowseFragment getInstance(BrowseActivity activity,
                                                       boolean sortedAscending) {
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
            arguments.putString(
                    BrowseFragment.ARGUMENT_EMPTY_TEXT,
                    activity.getString(R.string.no_artists)
            );

            ArtistBrowseFragment fragment = new ArtistBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setSortedAscending(sortedAscending);

            return fragment;
        }

        static final class ArtistBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;

            ArtistBrowseFragmentViewBinder(Resources resources) {
                mResources = resources;
            }

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                Log.d("setViewValue", view.toString());
                Log.d("columnIndex", "" + columnIndex);
                if (view.getId() == R.id.browse_list_bottom_text) {
                    TextView textView = (TextView) view;
                    if (columnIndex == 2) {
                        // Number of albums
                        int numberOfAlbums = cursor.getInt(
                                cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_ALBUMS)
                        );
                        int numberOfTracks = cursor.getInt(
                                cursor.getColumnIndex(MediaStore.Audio.Artists.NUMBER_OF_TRACKS)
                        );
                        textView.setText(
                                mResources.getString(
                                        R.string.artist_info,
                                        mResources.getQuantityString(
                                                R.plurals.artist_info_albums,
                                                numberOfAlbums,
                                                numberOfAlbums
                                        ),
                                        mResources.getQuantityString(
                                                R.plurals.tracks,
                                                numberOfTracks,
                                                numberOfTracks
                                        )
                                )
                        );

                        return true;
                    }

                    if (columnIndex == 3) {
                        // Number of tracks (Already set)
                        return true;
                    }
                } else if(view.getId() == R.id.browse_list_image) {
                    // TODO: this only needs to be done one time
                    ImageView imageView = (ImageView) view;
                    imageView.setImageDrawable(
                            ResourcesCompat.getDrawable(mResources, R.drawable.ic_person_black_24dp, null)
                    );
                    return true;
                }

                return false;
            }
        }
    }
}