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
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.TextView;

abstract public class BrowseFragment extends ListFragment implements
        Sortable, LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {

    private static final int BROWSE_LOADER = 0;
    public static final String ARGUMENT_SORT_COLUMN = "sortColumn";
    public static final String ARGUMENT_CONTENT_URI = "contentURI";
    public static final String ARGUMENT_COLUMNS = "columns";
    public static final String ARGUMENT_SELECTION = "selection";
    public static final String ARGUMENT_SELECTION_ARGS = "SelectionArgs";

    private boolean mSortedAscending = true;

    private String mEmptyText;

    public BrowseFragment() {
    }

    public void setEmptyText(String emptyText) {
        mEmptyText = emptyText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
        sortButton.setOnClickListener(this);

        getLoaderManager().restartLoader(BROWSE_LOADER, null, this);

        return view;
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
                        arguments.getStringArray(ARGUMENT_SELECTION_ARGS),
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
        } else if (adapter instanceof TrackBrowseFragment.TrackBrowseListAdapter) {
            TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                    (TrackBrowseFragment.TrackBrowseListAdapter) adapter;
            trackBrowseListAdapter.changeCursor(data);
        }

        @SuppressWarnings("ConstantConditions")
        TextView emptyView = (TextView) getView().findViewById(R.id.no_data);
        View loadingView = getView().findViewById(R.id.loading_data);

        if (data.getCount() > 0) {
            emptyView.setVisibility(View.INVISIBLE);
            loadingView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText(mEmptyText);
            loadingView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        ListAdapter adapter = getListAdapter();
        if (adapter instanceof SimpleCursorAdapter) {
            SimpleCursorAdapter cursorAdapter = (SimpleCursorAdapter) adapter;
            cursorAdapter.changeCursor(null);
        } else if (adapter instanceof TrackBrowseFragment.TrackBrowseListAdapter) {
            TrackBrowseFragment.TrackBrowseListAdapter trackBrowseListAdapter =
                    (TrackBrowseFragment.TrackBrowseListAdapter) adapter;
            trackBrowseListAdapter.changeCursor(null);
        }
    }

    @Override
    public void sort(boolean ascending) {
        mSortedAscending = ascending;
        getLoaderManager().restartLoader(BROWSE_LOADER, null, this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sort_menu_button) {
            sort(!mSortedAscending);
        }
    }
}
