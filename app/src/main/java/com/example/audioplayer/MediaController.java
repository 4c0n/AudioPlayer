package com.example.audioplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class MediaController extends FrameLayout implements
        SeekBar.OnSeekBarChangeListener,
        OnPlayerStartedListener,
        OnPlayerStoppedListener {

    private TextView timeElapsed;
    private SeekBar seekBar;
    private TextView timeLength;
    private ImageButton repeat;
    private ImageButton previous;
    private ImageButton play;
    private ImageButton pause;
    private ImageButton next;
    private ImageButton shuffle;
    private MediaPlayer mediaPlayer;
    private RepeatState repeatState = RepeatState.REPEAT_OFF;
    private boolean draggingSeekBar = false;
    private int seekToMilliseconds;

    private Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            int currentPosition = setProgress();
            if (mediaPlayer.isPlaying() && !draggingSeekBar) {
                // use currentPosition to determine offset, because postDelayed only queues
                postDelayed(progressUpdater, 1000 - (currentPosition % 1000));
            }
        }
    };

    private OnClickListener onPlayClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.play();
        }
    };

    private OnClickListener onPauseClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.pause();
            updatePausePlayButton();
        }
    };

    private OnClickListener onRepeatClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (repeatState) {
                case REPEAT_OFF:
                    repeat.setImageResource(R.drawable.ic_repeat_one_black_24dp);
                    repeat.setAlpha(1.0F);

                    mediaPlayer.repeatOne();

                    repeatState = RepeatState.REPEAT_ONE;
                    break;

                case REPEAT_ONE:
                    repeat.setImageResource(R.drawable.ic_repeat_black_24dp);

                    mediaPlayer.repeatAll();

                    repeatState = RepeatState.REPEAT_ALL;
                    break;

                case REPEAT_ALL:
                    repeat.setAlpha(0.5F);

                    mediaPlayer.repeatOff();

                    repeatState = RepeatState.REPEAT_OFF;
            }
        }
    };

    private OnClickListener onNextClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.next();
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

        previous = (ImageButton) layout.findViewById(R.id.media_controller_previous);

        play = (ImageButton) layout.findViewById(R.id.media_controller_play);
        play.setOnClickListener(onPlayClicked);

        pause = (ImageButton) layout.findViewById(R.id.media_controller_pause);
        pause.setOnClickListener(onPauseClicked);

        next = (ImageButton) layout.findViewById(R.id.media_controller_next);
        next.setOnClickListener(onNextClicked);

        shuffle = (ImageButton) layout.findViewById(R.id.media_controller_shuffle);
    }

    private int setProgress() {
        // format milliseconds string
        int currentPosition = mediaPlayer.getCurrentPosition();
        String timeElapsed = new TimeStringFormatter(currentPosition).format();
        this.timeElapsed.setText(timeElapsed);

        // TODO: Duration does not need to be updated all the time
        int duration = mediaPlayer.getDuration();
        String timeLength = new TimeStringFormatter(duration).format();
        this.timeLength.setText(timeLength);

        if (duration > 0) {
            seekBar.setProgress(1000 * currentPosition / duration);
        }

        return currentPosition;
    }

    private void updatePausePlayButton() {
        if (mediaPlayer.isPlaying()) {
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

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
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

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            // convert progress to time string
            int duration = mediaPlayer.getDuration();
            if (duration > 0) {
                seekToMilliseconds = duration / 1000 * progress;
                timeElapsed.setText(new TimeStringFormatter(seekToMilliseconds).format());
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        draggingSeekBar = true;
        removeCallbacks(progressUpdater);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        draggingSeekBar = false;
        mediaPlayer.seekTo(seekToMilliseconds);
        post(progressUpdater);
    }

    @Override
    public void onPlayerStarted() {
        // start progress update cycle
        post(progressUpdater);
        updatePausePlayButton();
    }

    @Override
    public void onPlayerStopped() {
        removeCallbacks(progressUpdater);
        updatePausePlayButton();
        seekBar.setProgress(0);
        String timeElapsed = new TimeStringFormatter(0).format();
        this.timeElapsed.setText(timeElapsed);
    }

    interface MediaPlayer {
        int getCurrentPosition();
        int getDuration();
        boolean isPlaying();
        void play();
        void pause();
        void repeatOne();
        void repeatOff();
        void repeatAll();
        void seekTo(int milliseconds);
        void next();
    }
}
