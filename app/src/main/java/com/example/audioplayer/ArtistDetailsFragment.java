package com.example.audioplayer;


import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

public class ArtistDetailsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final int ALBUM_LOADER = 0;
    private static final int TRACK_LOADER = 1;

    public static final String ARGUMENT_ARTIST_ID = "artistId";

    private ArtistDetailsExpandableListAdapter mAdapter;

    public ArtistDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d("4c0n", "ArtistDetailFragment onCreateView");
        getLoaderManager().initLoader(ALBUM_LOADER, null, this);
        getLoaderManager().initLoader(TRACK_LOADER, null, this);
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_artist_details, container, false);
        ExpandableListView listView = (ExpandableListView) view.findViewById(
                R.id.artist_detail_list
        );
        listView.setGroupIndicator(null);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d("4c0n", "ArtistDetailFragment onViewCreated");
        ExpandableListView listView = (ExpandableListView) view.findViewById(
                R.id.artist_detail_list
        );
        listView.setAdapter(mAdapter);


        Log.d("4c0n", "here");
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String artistId = getArguments().getString(ARGUMENT_ARTIST_ID);
        switch (id) {
            case ALBUM_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Artists.Albums.getContentUri(
                                "external",
                                Long.parseLong(artistId)
                        ),
                        new String[] {
                                MediaStore.Audio.Albums._ID,
                                MediaStore.Audio.Artists.Albums.ALBUM,
                                MediaStore.Audio.Artists.Albums.ALBUM_ART,
                                MediaStore.Audio.Artists.Albums.NUMBER_OF_SONGS
                        },
                        null,
                        null,
                        MediaStore.Audio.Albums.DEFAULT_SORT_ORDER
                );
            case TRACK_LOADER:
                return new CursorLoader(
                        getActivity(),
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        new String[] {
                                MediaStore.Audio.Media._ID,
                                MediaStore.Audio.Media.TITLE,
                                MediaStore.Audio.Media.ARTIST,
                                MediaStore.Audio.Media.ALBUM_ID
                        },
                        MediaStore.Audio.Media.IS_MUSIC + "=? AND " +
                                MediaStore.Audio.Media.ARTIST_ID + "=?",
                        new String[] {"1", artistId},
                        MediaStore.Audio.Media.TITLE_KEY + " ASC"
                );
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case ALBUM_LOADER:
                mAdapter.changeAlbumCursor(data);
                break;
            case TRACK_LOADER:
                mAdapter.changeTrackCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()) {
            case ALBUM_LOADER:
                mAdapter.changeAlbumCursor(null);
                break;
            case TRACK_LOADER:
                mAdapter.changeTrackCursor(null);
        }
    }

    public void setAdapter(ArtistDetailsExpandableListAdapter adapter) {
        mAdapter = adapter;
    }
}
