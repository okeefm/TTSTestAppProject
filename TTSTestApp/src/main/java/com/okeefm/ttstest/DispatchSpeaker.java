package com.okeefm.ttstest;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by okeefm on 8/31/13.
 */
public class DispatchSpeaker {
    private String firstDispatchText;
    private String secondDispatchText;
    private MediaPlayer mp_beeps;
    private MediaPlayer mp_tone1;
    private TextToSpeech tts;
    private Context context;

    //initialize the 2 media players we'll be using
    private final MediaPlayer disp1 = new MediaPlayer();
    private final MediaPlayer disp2 = new MediaPlayer();

    //Use this boolean as the world's hackiest semaphore
    private final Boolean prepared[] = new Boolean[2];

    public DispatchSpeaker(String firstDispatch, String secondDispatch, TextToSpeech tts) {
        firstDispatchText = firstDispatch;
        secondDispatchText = secondDispatch;
        this.context = context;

        this.tts = tts;

        generateDispatchWavs();
    }

    public void generateDispatchWavs() {

        for (int i = 0; i < 2; i++) {
            prepared[i] = false;
        }

        //Figure out the external storage path on this device
        String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("TTS", "exStoragePath : " + exStoragePath);
        File appTmpPath = new File(exStoragePath + "/sounds/");
        //Create (or use, if already created) the path to the sounds directory in external storage
        boolean success = true;
        if (!appTmpPath.exists()) {
            success = appTmpPath.mkdirs();
        }
        Log.d("TTS", "directory "+appTmpPath+" is created : "+success);

        //Make up filenames for the 2 dispatches
        String dispatch1name = "dispatch1.wav";
        String dispatch2name = "dispatch2.wav";

        final String dispatchFile1 = appTmpPath.getAbsolutePath() + File.separator + dispatch1name;
        final String dispatchFile2 = appTmpPath.getAbsolutePath() + File.separator + dispatch2name;

        //Set up a completion listener for the TTS synthesis
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                Log.d("TTS", "String passed to onDone: " + s);

                try {
                    if (Integer.parseInt(s) == 1) {
                        disp1.setDataSource(dispatchFile1);
                        disp1.prepareAsync();
                    } else if (Integer.parseInt(s) == 3) {
                        disp2.setDataSource(dispatchFile2);
                        disp2.prepareAsync();
                    }
                } catch (IOException e) {
                    Log.e("TTS", e.toString());
                }

            }

            @Override
            public void onError(String s) {

            }
        });

        //Synthesize the 3 speech files
        HashMap<String, String> hash1 = new HashMap();
        hash1.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");
        tts.synthesizeToFile(firstDispatchText, hash1, dispatchFile1);

        HashMap<String, String> hash2 = new HashMap();
        hash2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "2");
        tts.synthesizeToFile(secondDispatchText, hash2, dispatchFile2);

        //Set OnCompletionListeners for all MediaPlayers, so things play in the right order
        mp_beeps.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp1.start();
            }
        });
        disp1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp_tone1.start();
            }
        });
        mp_tone1.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp2.start();
            }
        });
        disp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp1.release();
                disp2.release();
            }
        });

        //When a MediaPlayer is prepared, set its prepared variable to true, and check if the others are done too. If they are, start playing
        disp1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepared[0] = true;
            }
        });
        disp2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepared[1] = true;
            }
        });

    }

    public void play() {
        new Thread(new Runnable() {
            public void run() {
                while(!prepared[0] && prepared[1]) {}
                mp_beeps.start();
            }
        }).start();
    }


}
