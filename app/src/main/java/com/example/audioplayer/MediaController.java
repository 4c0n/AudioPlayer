package com.example.audioplayer;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;


public class MediaController extends FrameLayout {
    private static final String REPEAT_OFF = "repeatNone";
    private static final String REPEAT_ONE = "repeatOne";
    private static final String REPEAT_ALL = "repeatAll";

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
    private String repeatState = REPEAT_OFF;

    private Runnable progressUpdater = new Runnable() {
        @Override
        public void run() {
            int currentPosition = setProgress();
            if (mediaPlayer.isPlaying()) {
                // use currentPosition to determine offset, because postDelayed only queues
                postDelayed(progressUpdater, 1000 - (currentPosition % 1000));
            }
        }
    };

    private OnClickListener onPlayClicked = new OnClickListener() {
        @Override
        public void onClick(View v) {
            mediaPlayer.play();
            post(progressUpdater);
            updatePausePlayButton();
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
                    repeat.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    getResources(),
                                    R.drawable.ic_repeat_one_black_24dp,
                                    null
                            )
                    );
                    repeat.setAlpha(1.0F);

                    mediaPlayer.repeatOne();

                    repeatState = REPEAT_ONE;
                    break;

                case REPEAT_ONE:
                    repeat.setImageDrawable(
                            ResourcesCompat.getDrawable(
                                    getResources(),
                                    R.drawable.ic_repeat_black_24dp,
                                    null
                            )
                    );
                    repeat.setAlpha(0.5F);

                    mediaPlayer.repeatOff();

                    repeatState = REPEAT_OFF;
            }
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

        shuffle = (ImageButton) layout.findViewById(R.id.media_controller_shuffle);
    }

    private int setProgress() {
        // format milliseconds string
        int currentPosition = mediaPlayer.getCurrentPosition();
        String timeElapsed = new TimeStringFormatter(currentPosition).format();
        this.timeElapsed.setText(timeElapsed);

        int duration = mediaPlayer.getDuration();
        String timeLength = new TimeStringFormatter(duration).format();
        this.timeLength.setText(timeLength);

        if (duration > 0) {
            seekBar.setProgress(1000 * currentPosition / duration);
        }

        return currentPosition;
    }

    private void updatePausePlayButton() {
        Log.d("4c0n", "updatePausePlayButton " + mediaPlayer.isPlaying());
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

        // start progress update cycle
        post(progressUpdater);
        updatePausePlayButton();
    }

    interface MediaPlayer {
        int getCurrentPosition();
        int getDuration();
        boolean isPlaying();
        void play();
        void pause();
        void repeatOne();
        void repeatOff();
    }
}
