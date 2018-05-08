package com.junhao.baby.utils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by keayuan on 17/7/16.
 */
public class AudioManager {

    public static final int ERROR = -1;
    public static final int ERROR_FILE_NOT_EXISTS = -2;
    public static final int ERROR_STORAGE = -3;

    private MediaPlayer mediaPlayer;
    private IAudioPlayListener mIAudioPlayListener;
    private MediaPlayer.OnCompletionListener mListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            release();
            if (mIAudioPlayListener != null) {
                mIAudioPlayListener.onPlayCompletion();
            }
        }
    };


    private AudioManager() {
        this.mediaPlayer = new MediaPlayer();
    }

    private static AudioManager mInstance;

    public static AudioManager getInstance() {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 播放语音
     *
     * @param path
     * @param listener 播放完成方法回调
     */
    public void play(String path, IAudioPlayListener listener) {
        if (path == null) {
            path = "";
        }
        File file = new File(path);
        if (file.exists()) {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    if (mIAudioPlayListener != null) {
                        mIAudioPlayListener.onStop();
                    }
                }
            } else {
                mediaPlayer = new MediaPlayer();
            }
            try {
                this.mediaPlayer.reset();
                mediaPlayer.setOnCompletionListener(mListener);
                this.mediaPlayer.setDataSource(path);
                this.mediaPlayer.prepare();
                this.mediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
                if (listener != null) {
                    if (e instanceof FileNotFoundException) {
                        listener.onError(ERROR_STORAGE);
                    } else {
                        listener.onError(ERROR);
                    }
                    release();
                }
            }
        } else {
            if (listener != null) {
                listener.onError(ERROR_FILE_NOT_EXISTS);
                release();
            }
        }
        mIAudioPlayListener = listener;
    }

    /**
     * 播放语音
     *
     * @param file
     * @param listener 播放完成方法回调
     */
    public void play(AssetFileDescriptor file, IAudioPlayListener listener) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        } else {
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.reset();
            mediaPlayer.setOnCompletionListener(mListener);
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(),
                    file.getStartOffset());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (listener != null) {
                if (e instanceof FileNotFoundException) {
                    listener.onError(ERROR_STORAGE);
                } else {
                    listener.onError(ERROR);
                }
                release();
            }
        }
        if (listener != null) {
            listener.onError(ERROR_FILE_NOT_EXISTS);
            release();
        }
        mIAudioPlayListener = listener;
    }

    /**
     * 获取语音的播放时长
     *
     * @return
     */
    public static int getMediaPlayTime(String path) {
        int duration = -1;
        if (path == null) {
            path = "";
        }
        if (new File(path).exists()) {
            MediaPlayer mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                duration = mediaPlayer.getDuration();
                mediaPlayer.release();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return duration;
    }

    public void release() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                if (mIAudioPlayListener != null) {
                    mIAudioPlayListener.onStop();
                }
            }
            mediaPlayer.release();
        }
        mediaPlayer = null;
    }

    public void stop() {
        release();
    }

    public interface IAudioPlayListener {
        void onPlayCompletion();

        void onStop();

        void onError(int code);
    }
}
