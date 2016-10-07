package com.example.audioplayer;


import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public final class TrackBrowseFragment extends BrowseFragment {
    public static TrackBrowseFragment newInstance(@Nullable String albumId) {
        Bundle arguments = new Bundle();
        arguments.putString(
                BrowseFragment.ARGUMENT_SORT_COLUMN,
                MediaStore.Audio.Media.TITLE_KEY
        );
        arguments.putParcelable(
                BrowseFragment.ARGUMENT_CONTENT_URI,
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
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

        String selection = MediaStore.Audio.Media.IS_MUSIC + "=?";
        String[] selectionArgs = {"1"};
        if (albumId != null) {
            selection += " AND " + MediaStore.Audio.Media.ALBUM_ID + "=?";
            selectionArgs = new String[] {selectionArgs[0], albumId};
        }

        arguments.putString(BrowseFragment.ARGUMENT_SELECTION, selection);
        arguments.putStringArray(BrowseFragment.ARGUMENT_SELECTION_ARGS, selectionArgs);

        TrackBrowseFragment fragment = new TrackBrowseFragment();
        fragment.setArguments(arguments);
        fragment.setRetainInstance(true);

        return fragment;
    }

    static final class TrackBrowseListAdapter extends BaseAdapter {
        private Cursor mMediaCursor;
        private LayoutInflater mInflater;
        private ContentResolver mContentResolver;
        private Resources mResources;

        private static class ViewHolder {
            TextView title;
            TextView artist;
            ImageView albumArt;
        }

        TrackBrowseListAdapter(Context context) {
            mContentResolver = context.getContentResolver();
            mInflater = LayoutInflater.from(context);
            mResources = context.getResources();
        }

        private void setDrawableToImageView(ViewHolder holder) {
            holder.albumArt.setImageDrawable(
                    ResourcesCompat.getDrawable(
                            mResources,
                            R.drawable.ic_music_note_black_24dp,
                            null
                    )
            );
        }

        @Override
        public int getCount() {
            if (mMediaCursor != null) {
                return mMediaCursor.getCount();
            }

            return 0;
        }

        @Override
        public Object getItem(int position) {
            if (mMediaCursor != null) {
                mMediaCursor.moveToPosition(position);
                return mMediaCursor;
            }

            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mMediaCursor != null) {
                if (mMediaCursor.moveToPosition(position)) {
                    return mMediaCursor.getLong(
                            mMediaCursor.getColumnIndex(MediaStore.Audio.Media._ID)
                    );
                }

                return 0;
            }

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.browse_list_item, parent, false);

                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.browse_list_top_text);
                holder.artist = (TextView) convertView.findViewById(R.id.browse_list_bottom_text);
                holder.albumArt = (ImageView) convertView.findViewById(R.id.browse_list_image);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            mMediaCursor.moveToPosition(position);

            String titleText = mMediaCursor.getString(
                    mMediaCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            );
            holder.title.setText(titleText);

            String artistText = mMediaCursor.getString(
                    mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            );
            holder.artist.setText(artistText);

            int albumId = mMediaCursor.getInt(
                    mMediaCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
            );

            // TODO: use cursor loader or other means of threading
            Cursor albumCursor = mContentResolver.query(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    new String[] {MediaStore.Audio.Albums.ALBUM_ART},
                    "_id=" + albumId,
                    null,
                    null
            );

            holder.albumArt.setImageURI(null);
            holder.albumArt.setImageDrawable(null);
            if (albumCursor != null) {
                if (albumCursor.getCount() > 0) {
                    albumCursor.moveToFirst();

                    String albumArtStr = albumCursor.getString(
                            albumCursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)
                    );

                    if (albumArtStr != null) {
                        holder.albumArt.setImageURI(Uri.fromFile(new File(albumArtStr)));
                    } else {
                        setDrawableToImageView(holder);
                    }
                } else {
                    setDrawableToImageView(holder);
                }
                albumCursor.close();
            } else {
                setDrawableToImageView(holder);
            }

            return convertView;
        }

        void changeCursor(Cursor cursor) {
            if (mMediaCursor != null) {
                mMediaCursor.close();
            }

            if (cursor != null) {
                mMediaCursor = cursor;
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}
