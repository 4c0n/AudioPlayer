package com.example.audioplayer;

import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.TextView;

class AlbumBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
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
        }
        return false;
    }
}
