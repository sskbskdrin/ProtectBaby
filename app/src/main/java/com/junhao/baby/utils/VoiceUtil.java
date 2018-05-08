package com.junhao.baby.utils;

import android.content.Context;
import android.content.res.AssetFileDescriptor;

import java.io.IOException;

/**
 * Created by ex-keayuan001 on 2018/3/22.
 *
 * @author ex-keayuan001
 */
public class VoiceUtil {

    private static boolean isPlay = false;
    private static int count = 0;

    public static void play(final Context context) {
        if (!isPlay) {
            try {
                AssetFileDescriptor fileDescriptor = context.getAssets().openFd
                        ("skin/alert.wav");
                AudioManager.getInstance().play(context.getFilesDir().getAbsolutePath() + "/alert" +
                        ".wav", new
                        AudioManager
                                .IAudioPlayListener() {
                            @Override
                            public void onPlayCompletion() {
                                isPlay = false;
                                if (count < 10) {
                                    play(context);
                                }
                            }

                            @Override
                            public void onStop() {
                                isPlay = false;
                            }

                            @Override
                            public void onError(int code) {
                                isPlay = false;
                            }
                        });
                count++;
                isPlay = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void stop() {
        AudioManager.getInstance().stop();
        isPlay = false;
    }

    public static boolean isPlay() {
        return isPlay;
    }
}
