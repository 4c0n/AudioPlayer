package com.example.audioplayer;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;

public class BrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int BROWSE_LOADER = 0;

    public static final String ARGUMENT_SORT_COLUMN = "sortColumn";
    public static final String ARGUMENT_CONTENT_URI = "contentURI";
    public static final String ARGUMENT_COLUMNS = "columns";
    public static final String ARGUMENT_SELECTION = "selection";
    public static final String ARGUMENT_EMPTY_TEXT = "empyText";

    private boolean mSortedAscending = true;

    private AdapterView.OnItemClickListener mOnItemClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(BROWSE_LOADER, null, this);

        return inflater.inflate(R.layout.fragment_browse, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        if (mOnItemClickListener != null) {
            getListView().setOnItemClickListener(mOnItemClickListener);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case BROWSE_LOADER:
                Bundle arguments = getArguments();
                String sortOrder = arguments.getString(ARGUMENT_SORT_COLUMN);
                if (sortOrder != null) {
                    if (mSortedAscending) {
                        sortOrder += " ASC";
                    } else {
                        sortOrder += " DESC";
                    }
                }

                return new CursorLoader(
                        getActivity(),
                        (Uri) arguments.get(ARGUMENT_CONTENT_URI),
                        arguments.getStringArray(ARGUMENT_COLUMNS),
                        arguments.getString(ARGUMENT_SELECTION),
                        null,
                        sortOrder
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof SimpleCursorAdapter) {
            SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
            cursorAdapter.changeCursor(data);
        } else if (adapter instanceof  MediaStoreAudioAdapter) {
            MediaStoreAudioAdapter mediaStoreAudioAdapter = (MediaStoreAudioAdapter) adapter;
            mediaStoreAudioAdapter.changeCursor(data);
        }

        TextView emptyView = (TextView) getView().findViewById(R.id.no_data);
        View loadingView = getView().findViewById(R.id.loading_data);

        if (data.getCount() > 0) {
            emptyView.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(getArguments().getString(ARGUMENT_EMPTY_TEXT));
            loadingView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof SimpleCursorAdapter) {
            SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
            cursorAdapter.changeCursor(null);
        } else if (adapter instanceof  MediaStoreAudioAdapter) {
            MediaStoreAudioAdapter mediaStoreAudioAdapter = (MediaStoreAudioAdapter) adapter;
            mediaStoreAudioAdapter.changeCursor(null);
        }
    }

    @Override
    public void sort(boolean ascending) {
        mSortedAscending = ascending;
        getLoaderManager().restartLoader(BROWSE_LOADER, null, this);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }
}
