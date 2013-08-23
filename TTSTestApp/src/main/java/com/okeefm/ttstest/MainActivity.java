package com.okeefm.ttstest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements
        TextToSpeech.OnInitListener {
    /** Called when the activity is first created. */

    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    private EditText locText;
    private EditText crossText;
    private EditText addlText;
    private EditText numText;
    private Spinner detSpinner;
    private String det = null;
    private MediaPlayer mp_beeps;
    private MediaPlayer mp_tone1;
    private MediaPlayer mp_tone2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tts = new TextToSpeech(this, this);

        detSpinner = (Spinner) findViewById(R.id.determinant_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.determinants_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
            detSpinner.setAdapter(adapter);
            detSpinner.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    // An item was selected. You can retrieve the selected item using
                    // parent.getItemAtPosition(pos)
                    det = (String) parent.getItemAtPosition(pos);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                    // Another interface callback
                }
            });

        btnSpeak = (Button) findViewById(R.id.btnSpeak);

        txtText = (EditText) findViewById(R.id.txtText);
        locText = (EditText) findViewById(R.id.locText);
        crossText = (EditText) findViewById(R.id.crossText);
        addlText = (EditText) findViewById(R.id.addlText);
        numText = (EditText) findViewById(R.id.numText);


        mp_beeps = MediaPlayer.create(getApplicationContext(), R.raw.tone_beeps_silence);
        mp_tone1 = MediaPlayer.create(getApplicationContext(), R.raw.tone_only);
        mp_tone2 = MediaPlayer.create(getApplicationContext(), R.raw.tone_only);

        // button on click event
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                try {
                    speakOut();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        mp_beeps.release();
        mp_tone1.release();
        mp_tone2.release();
        mp_beeps = null;
        mp_tone1 = null;
        mp_tone2 = null;

        super.onDestroy();
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.US);

            //tts.setPitch((float) 0.9); // set pitch level

           //tts.setSpeechRate((float) 0.9); // set speech speed rate

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Language is not supported");
            } else {
                btnSpeak.setEnabled(true);
                //speakOut();
            }

        } else {
            Log.e("TTS", "Initilization Failed");
        }

    }

    private void speakOut() throws IOException {

        //Get field values to generate speech
        String txt = txtText.getText().toString();
        String locTxt = locText.getText().toString();
        final String addtxt = addlText.getText().toString();
        final String crossTxt = crossText.getText().toString();
        final String numTxt = numText.getText().toString();
        //Put together the 3 dispatch sentences
        final String firstDispatchText = "Stand by RPI Ambulance for a " + det + " determinant " + txt + ". " + locTxt;
        final String secondDispatchText = "RPI Ambulance, a " + det + " determinant " + txt + " " + locTxt + ", " + addtxt;
        final String timeStamp = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
        final String lastDispatchText = "Repeating for the RPI Ambulance, a " + det + " determinant " + txt + " " + locTxt + ", " + addtxt + ", crosses of " + crossTxt +", time is " + timeStamp + ", dispatcher " + numTxt;

        //Figure out the external storage path on this device
        String exStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        Log.d("TTS", "exStoragePath : "+exStoragePath);
        File appTmpPath = new File(exStoragePath + "/sounds/");
        //Create (or use, if already created) the path to the sounds directory in external storage
        boolean success = true;
        if (!appTmpPath.exists()) {
            success = appTmpPath.mkdirs();
        }
        Log.d("TTS", "directory "+appTmpPath+" is created : "+success);
        //Make up filenames for the 3 dispatches
        String dispatch1name = "dispatch1.wav";
        String dispatch2name = "dispatch2.wav";
        String dispatch3name = "dispatch3.wav";
        final String dispatchFile1 = appTmpPath.getAbsolutePath() + File.separator + dispatch1name;
        final String dispatchFile2 = appTmpPath.getAbsolutePath() + File.separator + dispatch2name;
        final String dispatchFile3 = appTmpPath.getAbsolutePath() + File.separator + dispatch3name;

        //initialize the 3 media players we'll be using
        final MediaPlayer disp1 = new MediaPlayer();
        final MediaPlayer disp2 = new MediaPlayer();
        final MediaPlayer disp3 = new MediaPlayer();

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
                } else if (Integer.parseInt(s) == 2) {
                    disp2.setDataSource(dispatchFile2);
                    disp2.prepareAsync();
                }else if (Integer.parseInt(s) == 3) {
                    disp3.setDataSource(dispatchFile3);
                    disp3.prepareAsync();
                }
                } catch (IOException e) {
                    Log.e("TTS", e.toString());
                }

            }

            @Override
            public void onError(String s) {

            }
        });

        final Toast toast = Toast.makeText(getApplicationContext(), "Synthesizing speech, please wait", Toast.LENGTH_LONG);
        toast.show();

        //Synthesize the 3 speech files
        HashMap<String, String> hash1 = new HashMap();
        hash1.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "1");
        tts.synthesizeToFile(firstDispatchText, hash1, dispatchFile1);

        HashMap<String, String> hash2 = new HashMap();
        hash2.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "2");
        tts.synthesizeToFile(secondDispatchText, hash2, dispatchFile2);

        HashMap<String, String> hash3 = new HashMap();
        hash3.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "3");
        tts.synthesizeToFile(lastDispatchText, hash3, dispatchFile3);

        //Set OnCompletionListeners for all MediaPlayers, so things play in the right order
        mp_beeps.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp1.start();
                mp_beeps.reset();
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
                mp_tone1.reset();
            }
        });
        disp2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mp_tone2.start();
            }
        });
        mp_tone2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp3.start();
                mp_tone2.reset();
            }
        });
        disp3.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                disp1.release();
                disp2.release();
                disp3.release();
            }
        });

        //Use this boolean as the world's hackiest semaphore
        final Boolean prepared[] = new Boolean[3];
        for (int i = 0; i < 3; i++) {
            prepared[i] = false;
        }
        //When a MediaPlayer is prepared, set its prepared variable to true, and check if the others are done too. If they are, start playing
        disp1.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepared[0] = true;
                if (prepared[1] && prepared[2]) {
                    mp_beeps.start();
                    toast.cancel();
                }
            }
        });
        disp2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepared[1] = true;
                if (prepared[0] && prepared[2]) {
                    mp_beeps.start();
                    toast.cancel();
                }
            }
        });
        disp3.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                prepared[2] = true;
                if (prepared[0] && prepared[1]) {
                    mp_beeps.start();
                    toast.cancel();
                }
            }
        });


    }
}