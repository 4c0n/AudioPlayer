package com.example.audioplayer;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ArtistBrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ARTIST_BROWSE = 0;

    private String[] mColumns = {
            MediaStore.Audio.Artists._ID,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
            MediaStore.Audio.Artists.NUMBER_OF_TRACKS
    };
    private boolean mSortedAscending = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(ARTIST_BROWSE, null, this);

        return inflater.inflate(R.layout.fragment_artist_browse, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ARTIST_BROWSE:
                String sortOrder = MediaStore.Audio.Artists.ARTIST_KEY;
                if (mSortedAscending) {
                    sortOrder += " ASC";
                } else {
                    sortOrder += " DESC";
                }

                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
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
        String[] columnNames = data.getColumnNames();
        for (String columnName : columnNames) {
            Log.d("4c0n", columnName);
        }
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

        getLoaderManager().restartLoader(ARTIST_BROWSE, null, this);
    }
}
