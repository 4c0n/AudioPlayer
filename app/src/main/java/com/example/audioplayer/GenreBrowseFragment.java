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

public class GenreBrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int GENRE_BROWSE = 0;

    private String[] mColumns = {
            MediaStore.Audio.Genres._ID,
            MediaStore.Audio.GenresColumns.NAME
    };
    private boolean mSortedAscending = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(GENRE_BROWSE, null, this);

        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case GENRE_BROWSE:
                String sortOrder = MediaStore.Audio.GenresColumns.NAME;
                if (mSortedAscending) {
                    sortOrder += " ASC";
                } else {
                    sortOrder += " DESC";
                }

                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
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
    public void sort(boolean ascending) {
        mSortedAscending = ascending;
        getLoaderManager().restartLoader(GENRE_BROWSE, null, this);
    }
}
