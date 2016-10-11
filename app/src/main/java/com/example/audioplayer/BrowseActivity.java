package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Comparator;
import java.util.HashMap;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener, SimpleAsyncQueryHandler.OnQueryCompleteListener {
    private static final String SPINNER_POSITION_KEY = "spinnerPosition";
    private static final int QUERY_MEDIA = 47832;

    private int mSpinnerPosition = 0;
    private boolean mInit = true;

    private TrackBrowseFragment initTrackBrowseFragment() {
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                this,
                R.layout.browse_list_item,
                null,
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
        adapter.setViewBinder(
                new TrackBrowseFragment.ViewBinder(getResources(), getContentResolver())
        );

        TrackBrowseFragment fragment = TrackBrowseFragment.newInstance();
        fragment.setEmptyText(getString(R.string.no_tracks));
        fragment.setListAdapter(adapter);

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
                        MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.Genres._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_bottom_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        adapter.setViewBinder(
                new GenreBrowseFragment.GenreBrowseFragmentViewBinder(
                        getResources(),
                        getContentResolver()
                )
        );

        GenreBrowseFragment fragment = GenreBrowseFragment.getInstance();
        fragment.setEmptyText(getString(R.string.no_genres));
        fragment.setListAdapter(adapter);

        return fragment;
    }

    private void initFolderBrowseFragment() {
        SimpleAsyncQueryHandler queryHandler = new SimpleAsyncQueryHandler(getContentResolver());
        queryHandler.registerOnQueryCompleteListener(this);
        queryHandler.startQuery(
                QUERY_MEDIA,
                null,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] {MediaStore.Audio.Media.DATA},
                MediaStore.Audio.Media.IS_MUSIC + "=1",
                null,
                MediaStore.Audio.Media.DATA
        );
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
            } else if (text.equals(getString(R.string.browse_type_folders))) {
                initFolderBrowseFragment();
                // Returning because the query results on which the adapter is based will be
                // processed asynchronously.
                // TODO: pass position as cookie and set init to true in callback
                mSpinnerPosition = position;
                mInit = true;
                return;
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

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (token == QUERY_MEDIA) {
            // Actually init FolderBrowseFragment
            HashMap<String, FolderBrowseFragment.MediaFolder> uniquePaths = new HashMap<>();

            while (cursor.moveToNext()) {
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String key = path.substring(0, path.lastIndexOf("/"));

                if (uniquePaths.containsKey(key)) {
                    uniquePaths.get(key).numberOfTracks++;
                } else {
                    FolderBrowseFragment.MediaFolder folder =
                            new FolderBrowseFragment.MediaFolder();

                    folder.path = key;
                    folder.numberOfTracks = 1;
                    folder.name = key.substring(key.lastIndexOf("/") + 1);
                    uniquePaths.put(key, folder);
                }
            }
            cursor.close();

            FolderBrowseFragment.MediaFolderArrayAdapter adapter =
                    new FolderBrowseFragment.MediaFolderArrayAdapter(
                            this,
                            R.layout.browse_list_item
                    );

            adapter.addAll(uniquePaths.values());

            FolderBrowseFragment fragment = new FolderBrowseFragment();
            fragment.setRetainInstance(true);
            fragment.setListAdapter(adapter);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.browse_fragment_container, fragment)
                    .commit();
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

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            TextView textView = (TextView) v.findViewById(R.id.browse_list_top_text);

            Intent intent = new Intent();
            intent.setClass(getActivity(), AlbumDetailsActivity.class);
            intent.putExtra(AlbumDetailsActivity.INTENT_EXTRA_ALBUM_ID, id);
            intent.putExtra(AlbumDetailsActivity.INTENT_EXTRA_ALBUM_NAME, textView.getText());
            startActivity(intent);
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
            intent.putExtra(ArtistDetailsActivity.INTENT_EXTRA_ARTIST_ID, id);
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

    public static final class GenreBrowseFragment extends BrowseFragment implements
            SimpleAsyncQueryHandler.OnDeleteCompleteListener {

        private static final int ASYNC_DELETE_TOKEN = 1000;

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

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            getActivity().getMenuInflater().inflate(R.menu.genre_context_menu, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.genre_delete:
                    TextView textView =
                            (TextView) menuInfo.targetView.findViewById(R.id.browse_list_top_text);

                    SimpleAsyncQueryHandler queryHandler = new SimpleAsyncQueryHandler(
                            getActivity().getContentResolver()
                    );
                    queryHandler.registerOnDeleteCompleteListener(this);
                    queryHandler.startDelete(
                            ASYNC_DELETE_TOKEN,
                            textView.getText(),
                            MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Genres._ID + "=?",
                            new String[] {Long.toString(menuInfo.id)}
                    );

                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            TextView textView = (TextView) v.findViewById(R.id.browse_list_top_text);

            Intent intent = new Intent();
            intent.setClass(getActivity(), GenreDetailsActivity.class);
            intent.putExtra(GenreDetailsActivity.INTENT_EXTRA_GENRE_ID, id);
            intent.putExtra(GenreDetailsActivity.INTENT_EXTRA_GENRE_NAME, textView.getText());
            startActivity(intent);
        }

        @Override
        public void onDeleteComplete(int token, Object cookie, int result) {
            Toast.makeText(
                    getActivity(),
                    "Genre \"" + cookie + "\" deleted.",
                    Toast.LENGTH_SHORT
            ).show();
        }

        static final class GenreBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
            private Resources mResources;
            private ContentResolver mContentResolver;

            GenreBrowseFragmentViewBinder(Resources resources, ContentResolver contentResolver) {
                mResources = resources;
                mContentResolver = contentResolver;
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
                } else if (view.getId() == R.id.browse_list_bottom_text) {
                    int numberOfTracks = mContentResolver.query(
                            MediaStore.Audio.Genres.Members.getContentUri(
                                    "external",
                                    cursor.getLong(
                                            cursor.getColumnIndex(MediaStore.Audio.Genres._ID)
                                    )
                            ),
                            null,
                            null,
                            null,
                            null
                    ).getCount();

                    TextView textView = (TextView) view;
                    textView.setText(
                            mResources.getQuantityString(
                                    R.plurals.tracks,
                                    numberOfTracks,
                                    numberOfTracks
                            )
                    );

                    return true;
                }

                return false;
            }
        }
    }

    public static final class PlaylistBrowseFragment extends BrowseFragment implements
            SimpleAsyncQueryHandler.OnDeleteCompleteListener {

        private static final int ASYNC_DELETE_TOKEN = 1020;

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

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            registerForContextMenu(getListView());
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v,
                                        ContextMenu.ContextMenuInfo menuInfo) {
            super.onCreateContextMenu(menu, v, menuInfo);

            MenuInflater inflater = getActivity().getMenuInflater();
            inflater.inflate(R.menu.playlist_context_menu, menu);
        }

        @Override
        public boolean onContextItemSelected(MenuItem item) {
            AdapterView.AdapterContextMenuInfo menuInfo =
                    (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

            switch (item.getItemId()) {
                case R.id.playlist_delete:
                    TextView textView =
                            (TextView) menuInfo.targetView.findViewById(R.id.browse_list_top_text);

                    SimpleAsyncQueryHandler asyncQueryHandler = new SimpleAsyncQueryHandler(
                            getActivity().getContentResolver()
                    );
                    asyncQueryHandler.registerOnDeleteCompleteListener(this);
                    asyncQueryHandler.startDelete(
                            ASYNC_DELETE_TOKEN,
                            textView.getText(),
                            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                            MediaStore.Audio.Playlists._ID + "=?",
                            new String[] {Long.toString(menuInfo.id)}
                    );

                    return true;
                default:
                    return super.onContextItemSelected(item);
            }
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);

            TextView textView = (TextView) v.findViewById(R.id.browse_list_top_text);

            Intent intent = new Intent();
            intent.setClass(getActivity(), PlaylistDetailsActivity.class);
            intent.putExtra(PlaylistDetailsActivity.INTENT_EXTRA_PLAYLIST_ID, id);
            intent.putExtra(PlaylistDetailsActivity.INTENT_EXTRA_PLAYLIST_NAME, textView.getText());
            startActivity(intent);
        }

        @Override
        public void onDeleteComplete(int token, Object cookie, int result) {
            switch (token) {
                case ASYNC_DELETE_TOKEN:
                    Toast.makeText(
                            getActivity(),
                            "Playlist \"" + cookie + "\" deleted.",
                            Toast.LENGTH_SHORT
                    ).show();
            }
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

    public static final class FolderBrowseFragment extends ListFragment implements
            Sortable, View.OnClickListener {

        private boolean mSortedAscending = true;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_browse, container, false);

            ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
            sortButton.setOnClickListener(this);

            return view;
        }

        @Override
        public void sort(boolean ascending) {
            mSortedAscending = ascending;
            MediaFolderArrayAdapter adapter = (MediaFolderArrayAdapter) getListAdapter();
            adapter.sort(new Comparator<MediaFolder>() {
                @Override
                public int compare(MediaFolder o1, MediaFolder o2) {
                    int score = o1.name.compareTo(o2.name);

                    if (mSortedAscending) {
                        return score;
                    } else {
                        return 0 - score;
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sort_menu_button) {
                sort(!mSortedAscending);
            }
        }

        static class MediaFolder {
            String path;
            int numberOfTracks;
            String name;
        }

        static class MediaFolderArrayAdapter extends ArrayAdapter<MediaFolder> {
            private int resource;

            MediaFolderArrayAdapter(@NonNull Context context, @LayoutRes int resource) {
                super(context, resource);
                this.resource = resource;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView,
                                @NonNull ViewGroup parent) {
                Context context = getContext();
                LayoutInflater inflater = LayoutInflater.from(context);

                if (convertView == null) {
                    convertView = inflater.inflate(resource, parent, false);
                    ImageView imageView =
                            (ImageView) convertView.findViewById(R.id.browse_list_image);

                    imageView.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    context.getResources(),
                                    R.drawable.ic_folder_open_black_24dp,
                                    null
                            )
                    );
                }

                MediaFolder folder = getItem(position);

                TextView topTextView =
                        (TextView) convertView.findViewById(R.id.browse_list_top_text);
                topTextView.setText(folder.name);

                TextView bottomTextView =
                        (TextView) convertView.findViewById(R.id.browse_list_bottom_text);
                bottomTextView.setText(
                        context.getResources().getQuantityString(
                                R.plurals.tracks,
                                folder.numberOfTracks,
                                folder.numberOfTracks
                        )
                );

                return convertView;
            }
        }
    }
}
