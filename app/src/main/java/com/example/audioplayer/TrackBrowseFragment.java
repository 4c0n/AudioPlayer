package com.example.audioplayer;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TrackBrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MEDIA_LOADER = 0;

    private String[] mColumns = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID
    };
    private boolean mSortedAscending = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(MEDIA_LOADER, null, this);

        return inflater.inflate(R.layout.fragment_track_browse, container, false);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case MEDIA_LOADER:
                String sortOrder = MediaStore.Audio.Media.TITLE;
                if (mSortedAscending) {
                    sortOrder += " ASC";
                } else {
                    sortOrder += " DESC";
                }

                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        mColumns,
                        MediaStore.Audio.Media.IS_MUSIC + "=1",
                        null,
                        sortOrder
                );

            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        MediaStoreAudioAdapter adapter = (MediaStoreAudioAdapter) getListAdapter();
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        MediaStoreAudioAdapter adapter = (MediaStoreAudioAdapter) getListAdapter();
        adapter.changeCursor(null);
    }

    public void sort() {
        if (mSortedAscending) {
            mSortedAscending = false;
        } else {
            mSortedAscending = true;
        }

        getLoaderManager().restartLoader(MEDIA_LOADER, null, this);
    }
}
