package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.res.Resources;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

// TODO create activity for this fragment
public class ArtistDetailsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int ALBUM_LOADER = 0;
    private static final int TRACK_LOADER = 1;

    public static final String ARGUMENT_ARTIST_ID = "artistId";

    private ArtistDetailsExpandableListAdapter mAdapter;

    public ArtistDetailsFragment() {
        // Required empty public constructor
    }

    public static ArtistDetailsFragment newInstance(String artistId) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_ARTIST_ID, artistId);

        ArtistDetailsFragment fragment = new ArtistDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("4c0n", "ArtistDetailFragment onCreateView");
        getLoaderManager().initLoader(ALBUM_LOADER, null, this);
        getLoaderManager().initLoader(TRACK_LOADER, null, this);
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_artist_details, container, false);
        ExpandableListView listView = (ExpandableListView) view.findViewById(
                R.id.artist_detail_list
        );
        listView.setGroupIndicator(null);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("4c0n", "ArtistDetailFragment onViewCreated");
        ExpandableListView listView = (ExpandableListView) view.findViewById(
                R.id.artist_detail_list
        );
        listView.setAdapter(mAdapter);


        Log.d("4c0n", "here");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String artistId = getArguments().getString(ARGUMENT_ARTIST_ID);
        switch (id) {
            case ALBUM_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Artists.Albums.getContentUri(
                                "external",
                                Long.parseLong(artistId)
                        ),
                        new String[] {
                                MediaStore.Audio.Albums._ID,
                                MediaStore.Audio.Artists.Albums.ALBUM,
                                MediaStore.Audio.Artists.Albums.ALBUM_ART,
                                MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS
                        },
                        null,
                        null,
                        MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
                );
            case TRACK_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[] {
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM_ID
                        },
                        MediaStore.Audio.Media.IS_MUSIC + "=? AND " +
                                MediaStore.Audio.Media.ARTIST_ID + "=?",
                        new String[] {"1", artistId},
                        MediaStore.Audio.Media.TITLE_KEY + " ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ALBUM_LOADER:
                mAdapter.changeAlbumCursor(data);
                break;
            case TRACK_LOADER:
                mAdapter.changeTrackCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ALBUM_LOADER:
                mAdapter.changeAlbumCursor(null);
                break;
            case TRACK_LOADER:
                mAdapter.changeTrackCursor(null);
        }
    }

    public void setAdapter(ArtistDetailsExpandableListAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * TODO: Register as data observer so the list is updated when songs are added to the MediaStore
     */
    static final class ArtistDetailsExpandableListAdapter extends BaseExpandableListAdapter {
        private static final String[] PARENT_ITEMS = {"Albums", "Tracks"};

        private Cursor[] mCursors = {null, null};
        private LayoutInflater mInflater;
        private Resources mResources;
        private ContentResolver mContentResolver;

        ArtistDetailsExpandableListAdapter(
                LayoutInflater inflater,
                Resources resources,
                ContentResolver contentResolver
        ) {
            mInflater = inflater;
            mResources = resources;
            mContentResolver = contentResolver;
        }

        private void changeCursor(int index, Cursor cursor) {
            if (mCursors[index] != null) {
                mCursors[index].close();
            }

            if (cursor != null) {
                mCursors[index] = cursor;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }

        @Override
        public int getGroupCount() {
            return PARENT_ITEMS.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            Cursor cursor = mCursors[groupPosition];
            if (cursor != null && !cursor.isClosed()) {
                return cursor.getCount();
            }

            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return PARENT_ITEMS[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            Cursor cursor = mCursors[groupPosition];
            if (cursor != null) {
                cursor.moveToPosition(childPosition);
                return cursor;
            }

            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            Cursor cursor = mCursors[groupPosition];
            if (cursor != null) {
                if (cursor.moveToPosition(childPosition)) {
                    return cursor.getLong(
                            cursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    );
                }
            }

            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                                 ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.artist_details_group_view, parent, false);
            }

            TextView textView = (TextView) convertView;
            textView.setText(PARENT_ITEMS[groupPosition]);

            ((ExpandableListView) parent).expandGroup(groupPosition);

            return textView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                                 View convertView, ViewGroup parent) {
            Cursor cursor = mCursors[groupPosition];
            cursor.moveToPosition(childPosition);

            // TODO: cache child views
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.browse_list_item, parent, false);
            }
            ImageView image = (ImageView) convertView.findViewById(R.id.browse_list_image);
            TextView topText = (TextView) convertView.findViewById(R.id.browse_list_top_text);
            TextView bottomText = (TextView) convertView.findViewById(R.id.browse_list_bottom_text);

            if (groupPosition == 0) {
                // Albums
                String albumArtStr = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM_ART)
                );

                image.setImageURI(null);
                image.setImageDrawable(null);
                if (albumArtStr != null) {
                    image.setImageURI(Uri.fromFile(new File(albumArtStr)));
                } else {
                    image.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    mResources,
                                    R.drawable.ic_album_black_24dp,
                                    null
                            )
                    );
                }

                String title = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.ALBUM)
                );
                topText.setText(title);

                int numberOfTracks = cursor.getInt(
                        cursor.getColumnIndex(MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS)
                );
                bottomText.setText(
                        mResources.getQuantityString(R.plurals.tracks, numberOfTracks, numberOfTracks)
                );
            } else {
                // Tracks

                // TODO: this code is similar to that in TrackBrowseListAdapter, refactoring is in order
                int albumId = cursor.getInt(
                        cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                );

                // TODO: use cursor loader or other means of threading
                Cursor albumCursor = mContentResolver.query(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                        "_id=" + albumId,
                        null,
                        null
                );

                image.setImageURI(null);
                image.setImageDrawable(null);
                if (albumCursor != null) {
                    if (albumCursor.getCount() > 0) {
                        albumCursor.moveToFirst();

                        String albumArtStr = albumCursor.getString(
                                albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                        );

                        if (albumArtStr != null) {
                            image.setImageURI(Uri.fromFile(new File(albumArtStr)));
                        } else {
                            image.setImageDrawable(
                                    ResourcesCompat.getDrawable(
                                            mResources,
                                            R.drawable.ic_music_note_black_24dp,
                                            null
                                    )
                            );
                        }
                    }
                    albumCursor.close();
                }

                String title = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
                );
                topText.setText(title);

                String artist = cursor.getString(
                        cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
                );
                bottomText.setText(artist);
            }

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        void changeAlbumCursor(Cursor cursor) {
            changeCursor(0, cursor);
        }

        void changeTrackCursor(Cursor cursor) {
            changeCursor(1, cursor);
        }
    }
}
