package com.example.audioplayer;

import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

public class MediaController extends FrameLayout implements SeekBar.OnSeekBarChangeListener {

    private TextView timeElapsed;
    private SeekBar seekBar;
    private TextView timeLength;
    private ImageButton repeat;
    private ImageButton play;
    private ImageButton pause;
    private ImageButton shuffle;
    private RepeatState repeatState = RepeatState.REPEAT_OFF;
    private int seekToMilliseconds;
    private boolean shuffleState = false;
    private MediaControllerCompat.TransportControls transportControls;
    private boolean isPlaying = false;
    private int duration;
    private int currentPosition;

    private OnClickListener onPlayClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            transportControls.play();
        }
    };

    private OnClickListener onPauseClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            transportControls.pause();
        }
    };

    private OnClickListener onRepeatClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (repeatState) {
                case REPEAT_OFF:
                    repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                    repeat.setAlpha(1.0F);

                    transportControls.sendCustomAction(AudioPlayerService.ACTION_REPEAT_ONE, null);

                    repeatState = RepeatState.REPEAT_ONE;
                    break;

                case REPEAT_ONE:
                    repeat.setImageResource(R.drawable.ic_repeat_black_24dp);

                    transportControls.sendCustomAction(AudioPlayerService.ACTION_REPEAT_ALL, null);

                    repeatState = RepeatState.REPEAT_ALL;
                    break;

                case REPEAT_ALL:
                    repeat.setAlpha(0.5F);

                    transportControls.sendCustomAction(AudioPlayerService.ACTION_REPEAT_OFF, null);

                    repeatState = RepeatState.REPEAT_OFF;
            }
        }
    };

    private OnClickListener onNextClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            transportControls.skipToNext();
        }
    };

    private OnClickListener onPreviousClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            transportControls.skipToPrevious();
        }
    };

    private OnClickListener onShuffleClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setShuffle(!shuffleState);

            Bundle args = new Bundle();
            args.putBoolean(AudioPlayerService.ACTION_SHUFFLE_STATE_ARG, shuffleState);

            transportControls.sendCustomAction(AudioPlayerService.ACTION_SHUFFLE, args);
        }
    };

    private MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.d("4c0n", "onPlaybackStateChanged");

            processPlaybackState(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            Log.d("4c0n", "onMetadataChanged");

            processMetadata(metadata);
        }
    };

    public MediaController(Context context) {
        super(context);
        initView();
    }

    public MediaController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MediaController(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void processPlaybackState(PlaybackStateCompat state) {
        int playbackState = state.getState();
        isPlaying = playbackState == PlaybackStateCompat.STATE_PLAYING;
        updatePausePlayButton();

        currentPosition = (int) state.getPosition();
        setProgress();
    }

    private void processMetadata(MediaMetadataCompat metadata) {
        duration = (int) metadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION);
        setDurationText();
    }

    private void initView() {
        View layout = inflate(getContext(), R.layout.media_controller, null);
        addView(layout);

        timeElapsed = (TextView) layout.findViewById(R.id.media_controller_time_elapsed);

        seekBar = (SeekBar) layout.findViewById(R.id.media_controller_seek_bar);
        seekBar.setOnSeekBarChangeListener(this);
        seekBar.setMax(1000);

        timeLength = (TextView) layout.findViewById(R.id.media_controller_time_length);

        repeat = (ImageButton) layout.findViewById(R.id.media_controller_repeat);
        repeat.setOnClickListener(onRepeatClicked);

        ImageButton previous = (ImageButton) layout.findViewById(R.id.media_controller_previous);
        previous.setOnClickListener(onPreviousClicked);

        play = (ImageButton) layout.findViewById(R.id.media_controller_play);
        play.setOnClickListener(onPlayClicked);

        pause = (ImageButton) layout.findViewById(R.id.media_controller_pause);
        pause.setOnClickListener(onPauseClicked);

        ImageButton next = (ImageButton) layout.findViewById(R.id.media_controller_next);
        next.setOnClickListener(onNextClicked);

        shuffle = (ImageButton) layout.findViewById(R.id.media_controller_shuffle);
        shuffle.setOnClickListener(onShuffleClicked);
    }

    private int setProgress() {
        // format milliseconds string
        String timeElapsed = new TimeStringFormatter(currentPosition).format();
        this.timeElapsed.setText(timeElapsed);

        if (duration > 0) {
            long progress = 1000L * currentPosition / duration;
            Log.d("4c0n", "progress: " + progress);
            seekBar.setProgress((int) progress);
        }

        return currentPosition;
    }

    private void setDurationText() {
        String timeLength = new TimeStringFormatter(duration).format();
        this.timeLength.setText(timeLength);
    }

    private void updatePausePlayButton() {
        if (isPlaying) {
            if (pause.getVisibility() == GONE) {
                play.setVisibility(GONE);
                pause.setVisibility(VISIBLE);
            }
        } else {
            if (play.getVisibility() == GONE) {
                pause.setVisibility(GONE);
                play.setVisibility(VISIBLE);
            }
        }
    }

    public void registerWithMediaSession(MediaSessionCompat.Token token) throws
            RemoteException {

        MediaControllerCompat mediaController = new MediaControllerCompat(
                getContext(),
                token
        );
        mediaController.registerCallback(mediaControllerCallback);
        transportControls = mediaController.getTransportControls();

        // Sync state if available
        MediaMetadataCompat metadata = mediaController.getMetadata();
        if (metadata != null) {
            processMetadata(metadata);
        }

        PlaybackStateCompat state = mediaController.getPlaybackState();
        if (state != null) {
            processPlaybackState(state);
        }
    }

    public void setRepeatState(RepeatState repeatState) {
        switch (repeatState) {
            case REPEAT_OFF:
                repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                repeat.setAlpha(0.5F);
                this.repeatState = repeatState;
                break;

            case REPEAT_ONE:
                repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                repeat.setAlpha(1.0F);
                this.repeatState = repeatState;
                break;

            case REPEAT_ALL:
                repeat.setImageResource(R.drawable.ic_repeat_black_24dp);
                repeat.setAlpha(1.0F);
                this.repeatState = repeatState;
                break;

            default:
                throw new IllegalArgumentException(
                        "State: " + repeatState + " is not a valid state"
                );
        }
    }

    public void setShuffle(boolean on) {
        if (on) {
            shuffle.setAlpha(1.0F);
        } else {
            shuffle.setAlpha(0.5F);
        }

        shuffleState = on;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // convert progress to time string
            if (duration > 0) {
                seekToMilliseconds = duration / 1000 * progress;
                timeElapsed.setText(new TimeStringFormatter(seekToMilliseconds).format());
            }
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        transportControls.seekTo(seekToMilliseconds);
    }
}
