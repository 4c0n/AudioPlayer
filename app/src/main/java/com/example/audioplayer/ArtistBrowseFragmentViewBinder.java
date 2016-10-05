package com.example.audioplayer;

import android.content.res.Resources;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

class ArtistBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
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
        }

        return false;
    }
}
