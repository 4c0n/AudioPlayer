package com.example.audioplayer;


import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;

public final class TrackDetailsBrowseFragment extends ListFragment implements
        View.OnClickListener {

    private OnTrackSelectedListener onTrackSelectedListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        onTrackSelectedListener = (OnTrackSelectedListener) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        ImageButton sortButton = (ImageButton) view.findViewById(R.id.sort_menu_button);
        sortButton.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SimpleCursorAdapter adapter = (SimpleCursorAdapter) getListAdapter();
        if (adapter != null) {
            Cursor cursor = adapter.getCursor();
            setSelection(cursor.getPosition());
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        onTrackSelectedListener.onTrackSelected(position);
    }

    @Override
    public void onClick(View v) {

    }

    public interface OnTrackSelectedListener {
        void onTrackSelected(int position);
    }
}
