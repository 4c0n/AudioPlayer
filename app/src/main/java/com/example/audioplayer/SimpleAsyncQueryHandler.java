package com.example.audioplayer;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.database.Cursor;

import java.util.ArrayList;


class SimpleAsyncQueryHandler extends AsyncQueryHandler {
    private ArrayList<OnDeleteCompleteListener> mOnDeleteCompleteListeners;
    private ArrayList<OnQueryCompleteListener> mOnQueryCompleteListeners;

    SimpleAsyncQueryHandler(ContentResolver cr) {
        super(cr);
        mOnDeleteCompleteListeners = new ArrayList<>(1);
        mOnQueryCompleteListeners = new ArrayList<>(1);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);

        for (OnDeleteCompleteListener onDeleteCompleteListener : mOnDeleteCompleteListeners) {
            onDeleteCompleteListener.onDeleteComplete(token, cookie, result);
        }
    }

    @Override
    protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
        super.onQueryComplete(token, cookie, cursor);

        for (OnQueryCompleteListener onQueryCompleteListener : mOnQueryCompleteListeners) {
            onQueryCompleteListener.onQueryComplete(token, cookie, cursor);
        }
    }

    void registerOnDeleteCompleteListener(OnDeleteCompleteListener listener) {
        if (!mOnDeleteCompleteListeners.contains(listener)) {
            mOnDeleteCompleteListeners.add(listener);
        }
    }

    void unregisterOnDeleteCompleteListener(OnDeleteCompleteListener listener) {
        if (mOnDeleteCompleteListeners.contains(listener)) {
            mOnDeleteCompleteListeners.remove(listener);
        } else {
            throw new RuntimeException("Trying to unregister a listener that is not registered...");
        }
    }

    void registerOnQueryCompleteListener(OnQueryCompleteListener listener) {
        if (!mOnQueryCompleteListeners.contains(listener)) {
            mOnQueryCompleteListeners.add(listener);
        }
    }

    void unregisterOnQueryCompleteListener(OnQueryCompleteListener listener) {
        if (mOnQueryCompleteListeners.contains(listener)) {
            mOnQueryCompleteListeners.remove(listener);
        } else {
            throw new RuntimeException("Trying to unregister a listener that is not registered...");
        }
    }

    interface OnDeleteCompleteListener {
        void onDeleteComplete(int token, Object cookie, int result);
    }

    interface OnQueryCompleteListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
    }
}
