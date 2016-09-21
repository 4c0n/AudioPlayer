package com.example.audioplayer;

import android.content.ContentResolver;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class MediaStoreAudioAdapter extends BaseAdapter {
    private static final String[]

    private ContentResolver mContentResolver;
    private String[] mColumns;

    public MediaStoreAdapter(ContentResolver contentResolver, String[] columns) {
        mContentResolver = contentResolver;
        mColumns = columns;

        for (String column : mColumns) {
            MediaStore.Audio.Media.
        }
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
