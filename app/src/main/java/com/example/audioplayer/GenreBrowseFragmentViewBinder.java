package com.example.audioplayer;

import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;

class GenreBrowseFragmentViewBinder implements SimpleCursorAdapter.ViewBinder {
    private Resources mResources;

    GenreBrowseFragmentViewBinder(Resources resources) {
        mResources = resources;
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (view.getId() == R.id.browse_list_image) {
            ImageView imageView = (ImageView) view;
            imageView.setImageDrawable(
                    ResourcesCompat.getDrawable(
                            mResources,
                            R.drawable.ic_queue_music_black_24dp,
                            null
                    )
            );

            return true;
        }

        return false;    }
}
