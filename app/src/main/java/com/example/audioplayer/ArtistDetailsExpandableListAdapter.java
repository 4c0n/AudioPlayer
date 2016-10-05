package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

/**
 * TODO: Register as data observer so the list is updated when songs are added to the MediaStore
 */
class ArtistDetailsExpandableListAdapter extends BaseExpandableListAdapter {
    private static final String[] PARENT_ITEMS = {"Albums", "Tracks"};
    private static final int ALBUM_VIEW_TYPE = 0;
    private static final int TRACK_VIEW_TYPE = 1;

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

        // TODO: consolidate list item layouts as they are basically the same
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