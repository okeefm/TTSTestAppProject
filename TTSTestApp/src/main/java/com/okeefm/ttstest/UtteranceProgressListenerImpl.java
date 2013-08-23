package com.okeefm.ttstest;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;

public class UtteranceProgressListenerImpl extends UtteranceProgressListener {
    private MediaPlayer tonefile;
    private MediaPlayer disp1;
    private String tempDestFile;
    private Context context;

    public UtteranceProgressListenerImpl(MediaPlayer mp1, MediaPlayer mp2, String filename, Context c) {
        tonefile = mp1;
        disp1 = mp2;
        tempDestFile = filename;
        context = c;
    }

    @Override
    public void onStart(String s) {
        Log.d("TTS", "Synth to file started: " + s);
    }

    @Override
    public void onDone(String s) {
        try {
            disp1.setDataSource(tempDestFile);
            disp1.prepare();
        } catch (IOException e) {
            Log.e("TTS", e.toString());
        }
        tonefile.start();
    }

    @Override
    public void onError(String s) {
        Log.e("TTS", "Synth to file error: " + s);
    }


}