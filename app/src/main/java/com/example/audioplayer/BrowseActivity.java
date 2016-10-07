package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.File;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private static final String SPINNER_POSITION_KEY = "spinnerPosition";

    private int mSpinnerPosition = 0;
    private boolean mInit = true;

    private TrackBrowseFragment initTrackBrowseFragment() {
        TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                new TrackBrowseFragment.TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.getInstance();
        fragment.setEmptyText(getString(R.string.no_tracks));
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

        ArtistBrowseFragment fragment = ArtistBrowseFragment.newInstance();
        fragment.setListAdapter(adapter);
        fragment.setEmptyText(getString(R.string.no_artists));

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

        AlbumBrowseFragment fragment = AlbumBrowseFragment.getInstance();
        fragment.setListAdapter(adapter);
        fragment.setEmptyText(getString(R.string.no_albums));

        return fragment;
    }

    private PlaylistBrowseFragment initPlaylistBrowseFragment() {
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

        adapter.setViewBinder(
                new PlaylistBrowseFragment.PlaylistBrowseFragmentViewBinder(getResources())
        );

        PlaylistBrowseFragment fragment = PlaylistBrowseFragment.getInstance();
        fragment.setEmptyText(getString(R.string.no_playlists));
        fragment.setListAdapter(adapter);

        return fragment;
    }

    private GenreBrowseFragment initGenreBrowseFragment() {
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

        adapter.setViewBinder(
                new GenreBrowseFragment.GenreBrowseFragmentViewBinder(getResources())
        );

        GenreBrowseFragment fragment = GenreBrowseFragment.getInstance();
        fragment.setEmptyText(getString(R.string.no_genres));
        fragment.setListAdapter(adapter);

        return fragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse);
        Toolbar toolbar = (Toolbar) findViewById(R.id.browse_activity_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            mSpinnerPosition = savedInstanceState.getInt(SPINNER_POSITION_KEY);
            mInit = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
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
        super.onSaveInstanceState(outState);

        outState.putInt(SPINNER_POSITION_KEY, mSpinnerPosition);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mInit) {
            TextView textView = (TextView) view;
            String text = textView.getText().toString();

            BrowseFragment newFragment;

            if (text.equals(getString(R.string.browse_type_tracks))) {
                newFragment = initTrackBrowseFragment();
            } else if (text.equals(getString(R.string.browse_type_artists))) {
                newFragment = initArtistBrowseFragment();
            } else if (text.equals(getString(R.string.browse_type_albums))) {
                newFragment = initAlbumBrowseFragment();
            } else if (text.equals(getString(R.string.browse_type_playlists))) {
                newFragment = initPlaylistBrowseFragment();
            } else if (text.equals(getString(R.string.browse_type_genres))) {
                newFragment = initGenreBrowseFragment();
            } else {
                throw new IllegalStateException("Unsupported item selected.");
            }

            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.browse_fragment_container, newFragment);
            transaction.commit();
        }

        mSpinnerPosition = position;
        mInit = true;
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

        private boolean mSortedAscending = true;

        private String mEmptyText;

        public BrowseFragment() {
        }

        public void setEmptyText(String emptyText) {
            mEmptyText = emptyText;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View view = inflater.inflate(R.layout.fragment_browse, container, false);
            ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
            sortButton.setOnClickListener(this);

            getLoaderManager().restartLoader(BROWSE_LOADER, null, this);

            return view;
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof SimpleCursorAdapter) {
                SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
                cursorAdapter.changeCursor(data);
            } else if (adapter instanceof TrackBrowseFragment.TrackBrowseListAdapter) {
                TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                        (TrackBrowseFragment.TrackBrowseListAdapter) adapter;
                trackBrowseListAdapter.changeCursor(data);
            }

            @SuppressWarnings("ConstantConditions")
            TextView emptyView = (TextView) getView().findViewById(R.id.no_data);
            View loadingView = getView().findViewById(R.id.loading_data);

            if (data.getCount() > 0) {
                emptyView.setVisibility(View.INVISIBLE);
                loadingView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.VISIBLE);
                emptyView.setText(mEmptyText);
                loadingView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            ListAdapter adapter = getListAdapter();
            if (adapter instanceof SimpleCursorAdapter) {
                SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
                cursorAdapter.changeCursor(null);
            } else if (adapter instanceof TrackBrowseFragment.TrackBrowseListAdapter) {
                TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                        (TrackBrowseFragment.TrackBrowseListAdapter) adapter;
                trackBrowseListAdapter.changeCursor(null);
            }
        }

        @Override
        public void sort(boolean ascending) {
            mSortedAscending = ascending;
            getLoaderManager().restartLoader(BROWSE_LOADER, null, this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sort_menu_button) {
                sort(!mSortedAscending);
            }
        }
    }

    public static final class AlbumBrowseFragment extends BrowseFragment {
        public static AlbumBrowseFragment getInstance() {
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

            AlbumBrowseFragment fragment = new AlbumBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setRetainInstance(true);

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
        public static ArtistBrowseFragment newInstance() {
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

            ArtistBrowseFragment fragment = new ArtistBrowseFragment();
            fragment.setRetainInstance(true);
            fragment.setArguments(arguments);

            return fragment;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            TextView textView = (TextView) v.findViewById(R.id.browse_list_top_text);

            Intent intent = new Intent();
            intent.setClass(getActivity(), ArtistDetailsActivity.class);
            intent.putExtra(ArtistDetailsActivity.INTENT_EXTRA_ARTIST_ID, "" + id);
            intent.putExtra(ArtistDetailsActivity.INTENT_EXTRA_ARTIST_NAME, textView.getText());
            startActivity(intent);
        }

        static final class ArtistBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;

            ArtistBrowseFragmentViewBinder(Resources resources) {
                mResources = resources;
            }

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
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

    public static final class GenreBrowseFragment extends BrowseFragment {
        public static GenreBrowseFragment getInstance() {
            Bundle arguments = new Bundle();
            arguments.putString(
                    BrowseFragment.ARGUMENT_SORT_COLUMN,
                    MediaStore.Audio.GenresColumns.NAME
            );
            arguments.putParcelable(
                    BrowseFragment.ARGUMENT_CONTENT_URI,
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
            );
            arguments.putStringArray(
                    BrowseFragment.ARGUMENT_COLUMNS,
                    new String[] {
                            MediaStore.Audio.Genres._ID,
                            MediaStore.Audio.GenresColumns.NAME
                    }
            );

            GenreBrowseFragment fragment = new GenreBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setRetainInstance(true);

            return fragment;
        }

        static final class GenreBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;

            GenreBrowseFragmentViewBinder(Resources resources) {
                mResources = resources;
            }

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.browse_list_image) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    mResources,
                                    R.drawable.ic_queue_music_black_24dp,
                                    null
                            )
                    );

                    return true;
                }

                return false;
            }
        }
    }

    public static final class PlaylistBrowseFragment extends BrowseFragment {
        public static PlaylistBrowseFragment getInstance() {
            Bundle arguments = new Bundle();
            arguments.putString(
                    BrowseFragment.ARGUMENT_SORT_COLUMN,
                    MediaStore.Audio.PlaylistsColumns.NAME
            );
            arguments.putParcelable(
                    BrowseFragment.ARGUMENT_CONTENT_URI,
                    MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI
            );
            arguments.putStringArray(
                    BrowseFragment.ARGUMENT_COLUMNS,
                    new String[] {
                            MediaStore.Audio.Playlists._ID,
                            MediaStore.Audio.PlaylistsColumns.NAME
                    }
            );

            PlaylistBrowseFragment fragment = new PlaylistBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setRetainInstance(true);

            return fragment;
        }

        static final class PlaylistBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;

            PlaylistBrowseFragmentViewBinder(Resources resources) {
                mResources = resources;
            }

            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.browse_list_image) {
                    ImageView imageView = (ImageView) view;
                    imageView.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    mResources,
                                    R.drawable.ic_playlist_play_black_24dp,
                                    null
                            )
                    );

                    return true;
                }

                return false;
            }
        }
    }

    public static final class TrackBrowseFragment extends BrowseFragment {
        public static TrackBrowseFragment getInstance() {
            Bundle arguments = new Bundle();
            arguments.putString(
                    BrowseFragment.ARGUMENT_SORT_COLUMN,
                    MediaStore.Audio.Media.TITLE_KEY
            );
            arguments.putParcelable(
                    BrowseFragment.ARGUMENT_CONTENT_URI,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            );
            arguments.putStringArray(
                    BrowseFragment.ARGUMENT_COLUMNS,
                    new String[] {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM_ID
                    }
            );
            arguments.putString(
                    BrowseFragment.ARGUMENT_SELECTION,
                    MediaStore.Audio.Media.IS_MUSIC + "=1"
            );

            TrackBrowseFragment fragment = new TrackBrowseFragment();
            fragment.setArguments(arguments);
            fragment.setRetainInstance(true);

            return fragment;
        }

        static final class TrackBrowseListAdapter extends BaseAdapter {
            private Cursor mMediaCursor;
            private LayoutInflater mInflater;
            private ContentResolver mContentResolver;
            private Resources mResources;

            private static class ViewHolder {
                TextView title;
                TextView artist;
                ImageView albumArt;
            }

            TrackBrowseListAdapter(Context context) {
                mContentResolver = context.getContentResolver();
                mInflater = LayoutInflater.from(context);
                mResources = context.getResources();
            }

            private void setDrawableToImageView(ViewHolder holder) {
                holder.albumArt.setImageDrawable(
                        ResourcesCompat.getDrawable(
                                mResources,
                                R.drawable.ic_music_note_black_24dp,
                                null
                        )
                );
            }

            @Override
            public int getCount() {
                if (mMediaCursor != null) {
                    return mMediaCursor.getCount();
                }

                return 0;
            }

            @Override
            public Object getItem(int position) {
                if (mMediaCursor != null) {
                    mMediaCursor.moveToPosition(position);
                    return mMediaCursor;
                }

                return null;
            }

            @Override
            public long getItemId(int position) {
                if (mMediaCursor != null) {
                    if (mMediaCursor.moveToPosition(position)) {
                        return mMediaCursor.getLong(
                                mMediaCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                        );
                    }

                    return 0;
                }

                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ViewHolder holder;

                if (convertView == null) {
                    convertView = mInflater.inflate(R.layout.browse_list_item, parent, false);

                    holder = new ViewHolder();
                    holder.title = (TextView) convertView.findViewById(R.id.browse_list_top_text);
                    holder.artist = (TextView) convertView.findViewById(R.id.browse_list_bottom_text);
                    holder.albumArt = (ImageView) convertView.findViewById(R.id.browse_list_image);

                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }

                mMediaCursor.moveToPosition(position);

                String titleText = mMediaCursor.getString(
                        mMediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                );
                holder.title.setText(titleText);

                String artistText = mMediaCursor.getString(
                        mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                );
                holder.artist.setText(artistText);

                int albumId = mMediaCursor.getInt(
                        mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                );

                // TODO: use cursor loader or other means of threading
                Cursor albumCursor = mContentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                        "_id=" + albumId,
                        null,
                        null
                );

                holder.albumArt.setImageURI(null);
                holder.albumArt.setImageDrawable(null);
                if (albumCursor != null) {
                    if (albumCursor.getCount() > 0) {
                        albumCursor.moveToFirst();

                        String albumArtStr = albumCursor.getString(
                                albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                        );

                        if (albumArtStr != null) {
                            holder.albumArt.setImageURI(Uri.fromFile(new File(albumArtStr)));
                        } else {
                            setDrawableToImageView(holder);
                        }
                    } else {
                        setDrawableToImageView(holder);
                    }
                    albumCursor.close();
                } else {
                    setDrawableToImageView(holder);
                }

                return convertView;
            }

            void changeCursor(Cursor cursor) {
                if (mMediaCursor != null) {
                    mMediaCursor.close();
                }

                if (cursor != null) {
                    mMediaCursor = cursor;
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }
}