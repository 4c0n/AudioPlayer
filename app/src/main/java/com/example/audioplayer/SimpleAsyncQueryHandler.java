package com.example.audioplayer;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

import java.util.ArrayList;


class SimpleAsyncQueryHandler extends AsyncQueryHandler {
    private ArrayList<OnDeleteCompleteListener> mOnDeleteCompleteListeners;

    SimpleAsyncQueryHandler(ContentResolver cr) {
        super(cr);
        mOnDeleteCompleteListeners = new ArrayList<>(1);
    }

    @Override
    protected void onDeleteComplete(int token, Object cookie, int result) {
        super.onDeleteComplete(token, cookie, result);

        for (OnDeleteCompleteListener onDeleteCompleteListener: mOnDeleteCompleteListeners) {
            onDeleteCompleteListener.onDeleteComplete(token, cookie, result);
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

    interface OnDeleteCompleteListener {
        void onDeleteComplete(int token, Object cookie, int result);
    }
}
