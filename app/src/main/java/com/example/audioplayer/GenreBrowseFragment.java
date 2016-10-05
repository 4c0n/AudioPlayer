package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.widget.SimpleCursorAdapter;

public class GenreBrowseFragment extends BrowseFragment {
    public static GenreBrowseFragment getInstance(Context context, boolean sortedAscending) {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.GenresColumns.NAME
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI
        );
        arguments.putStringArray(
                BrowseFragment.ARGUMENT_COLUMNS,
                new String[] {
                        MediaStore.Audio.Genres._ID,
                        MediaStore.Audio.GenresColumns.NAME
                }
        );
        arguments.putString(
                BrowseFragment.ARGUMENT_EMPTY_TEXT,
                context.getString(R.string.no_genres)
        );

        GenreBrowseFragment fragment = new GenreBrowseFragment();
        fragment.setArguments(arguments);
        fragment.setSortedAscending(sortedAscending);

        SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                context,
                R.layout.browse_list_item,
                null,
                new String[] {
                        MediaStore.Audio.GenresColumns.NAME,
                        MediaStore.Audio.Genres._ID
                },
                new int[] {
                        R.id.browse_list_top_text,
                        R.id.browse_list_image
                },
                SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        );

        adapter.setViewBinder(new GenreBrowseFragmentViewBinder(context.getResources()));

        fragment.setListAdapter(adapter);

        return fragment;
    }
}
