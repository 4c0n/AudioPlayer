package com.example.audioplayer;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AlbumBrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ALBUM_BROWSE = 0;

    private String[] mColumns = {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.AlbumColumns.ALBUM,
            MediaStore.Audio.AlbumColumns.ALBUM_ART,
            MediaStore.Audio.AlbumColumns.ARTIST,
            MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS
    };
    private boolean mSortedAscending = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(ALBUM_BROWSE, null, this);

        return inflater.inflate(R.layout.fragment_album_browse, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ALBUM_BROWSE:
                String sortOrder = MediaStore.Audio.Albums.ALBUM_KEY;
                if (mSortedAscending) {
                    sortOrder += " ASC";
                } else {
                    sortOrder += " DESC";
                }

                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        mColumns,
                        null,
                        null,
                        sortOrder
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) getListAdapter();
        cursorAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) getListAdapter();
        cursorAdapter.changeCursor(null);
    }

    @Override
    public void sort() {
        mSortedAscending = !mSortedAscending;

        getLoaderManager().restartLoader(ALBUM_BROWSE, null, this);
    }
}
