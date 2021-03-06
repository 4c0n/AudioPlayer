package com.example.audioplayer;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

public final class TrackBrowseFragment extends BrowseFragment {
    private static String getDefaultSelection() {
        return MediaStore.Audio.Media.IS_MUSIC + "=?";
    }

    private static ArrayList<String> getDefaultSelectionArgs() {
        ArrayList<String> selectionArgs = new ArrayList<>();
        selectionArgs.add("1");

        return selectionArgs;
    }

    private static TrackBrowseFragment newInstance(
            String selection,
            ArrayList<String> selectionArgs,
            Uri contentUri,
            String sortColumn
    ) {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                sortColumn
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                contentUri
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Media._ID,
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.ALBUM_ID
                }
        );
        arguments.putString(BrowseFragment.ARGUMENT_SELECTION, selection);

        String[] selectionArguments = new String[selectionArgs.size()];
        selectionArgs.toArray(selectionArguments);
        arguments.putStringArray(BrowseFragment.ARGUMENT_SELECTION_ARGS, selectionArguments);

        TrackBrowseFragment fragment = new TrackBrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        return fragment;
    }

    public static TrackBrowseFragment newInstance() {
        return newInstance(
                getDefaultSelection(),
                getDefaultSelectionArgs(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.TITLE_KEY
        );
    }

    public static TrackBrowseFragment newAlbumBasedInstance(long albumId) {
        String selection = getDefaultSelection();
        ArrayList<String> defaultSelectionArgs = getDefaultSelectionArgs();

        selection += " AND " + MediaStore.Audio.Media.ALBUM_ID + "=?";
        defaultSelectionArgs.add("" + albumId);

        return newInstance(
                selection,
                defaultSelectionArgs,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.TITLE_KEY
        );
    }

    public static TrackBrowseFragment newGenreBasedInstance(long genreId) {
        return newInstance(
                getDefaultSelection(),
                getDefaultSelectionArgs(),
                MediaStore.Audio.Genres.Members.getContentUri("external", genreId),
                MediaStore.Audio.Media.TITLE_KEY
        );
    }

    public static TrackBrowseFragment newPlaylistBasedInstance(long playlistId) {
        return newInstance(
                getDefaultSelection(),
                getDefaultSelectionArgs(),
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                MediaStore.Audio.Playlists.Members.PLAY_ORDER
        );
    }

    public static TrackBrowseFragment newFolderBasedInstance(String path) {
        String selection = getDefaultSelection();
        // TODO: do this in a less hacky way
        selection += " AND " + MediaStore.Audio.Media.DATA + " LIKE '" + path + "/%' AND " +
                MediaStore.Audio.Media.DATA + " NOT IN (SELECT " + MediaStore.Audio.Media.DATA +
                " FROM audio WHERE substr(" + MediaStore.Audio.Media.DATA + "," +
                (path.length() + 2) + ") GLOB '*[/]*')";

        return newInstance(
                selection,
                getDefaultSelectionArgs(),
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                MediaStore.Audio.Media.TITLE_KEY
        );
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Bundle arguments = getArguments();
        QueryParams params = new QueryParams(
                (Uri) arguments.getParcelable(ARGUMENT_CONTENT_URI),
                arguments.getStringArray(ARGUMENT_COLUMNS),
                arguments.getString(ARGUMENT_SELECTION),
                arguments.getStringArray(ARGUMENT_SELECTION_ARGS),
                arguments.getString(ARGUMENT_SORT_COLUMN)
        );

        Intent intent = new Intent();
        intent.setClass(getActivity(), TrackDetailsActivity.class);
        intent.putExtra(TrackDetailsActivity.INTENT_EXTRA_QUERY_PARAMS, params);
        intent.putExtra(TrackDetailsActivity.INTENT_EXTRA_CURSOR_POSITION, position);

        startActivity(intent);
    }

    static final class ViewBinder implements SimpleCursorAdapter.ViewBinder {
        private Resources mResources;
        private ContentResolver mContentResolver;

        ViewBinder(Resources resources, ContentResolver contentResolver) {
            mResources = resources;
            mContentResolver = contentResolver;
        }

        @Override
        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
            if (view.getId() == R.id.browse_list_image) {
                AlbumArtImageViewInitializer initializer = new AlbumArtImageViewInitializer(
                        mResources,
                        (ImageView) view,
                        mContentResolver,
                        cursor.getLong(columnIndex)
                );
                initializer.initialize();

                return true;
            }

            return false;
        }
    }
}
