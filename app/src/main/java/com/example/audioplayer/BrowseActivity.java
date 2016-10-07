package com.example.audioplayer;

import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class BrowseActivity extends AppCompatActivity implements
        AdapterView.OnItemSelectedListener {
    private static final String SPINNER_POSITION_KEY = "spinnerPosition";

    private int mSpinnerPosition = 0;
    private boolean mInit = true;

    private TrackBrowseFragment initTrackBrowseFragment() {
        TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                new TrackBrowseFragment.TrackBrowseListAdapter(this);

        TrackBrowseFragment fragment = TrackBrowseFragment.newInstance();
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
            intent.putExtra(AlbumDetailsActivity.INTENT_EXTRA_ALBUM_ID, "" + id);
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
}
