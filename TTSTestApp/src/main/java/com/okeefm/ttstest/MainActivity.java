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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity implements
        TextToSpeech.OnInitListener {
    /** Called when the activity is first created. */

    public static final String PREFS_NAME = "TTSPrefs";

    private TextToSpeech tts;
    private Button btnSpeak;
    private EditText txtText;
    private EditText locText;
    private EditText crossText;
    private EditText addlText;
    private EditText numText;
    private Spinner detSpinner;
    private String det = null;
    private CheckBox simCheck;
    private CheckBox futureCheck;


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
        simCheck = (CheckBox) findViewById(R.id.simCheck);
        futureCheck = (CheckBox) findViewById(R.id.futureTrigger);

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

        restoreValues(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString("det_string", det);
        savedInstanceState.putInt("det", detSpinner.getSelectedItemPosition());
        savedInstanceState.putCharSequence("injury", txtText.getText().toString());
        savedInstanceState.putCharSequence("location", locText.getText().toString());
        savedInstanceState.putCharSequence("crosses", crossText.getText().toString());
        savedInstanceState.putCharSequence("additional", addlText.getText().toString());
        savedInstanceState.putCharSequence("dispatcher", numText.getText().toString());
        savedInstanceState.putBoolean("simulation", simCheck.isChecked());

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        restoreValues(savedInstanceState);
    }

    public void restoreValues(Bundle savedInstanceState) {
        if (savedInstanceState != null && !savedInstanceState.isEmpty()) {
            det = savedInstanceState.getString("det_string");
            detSpinner.setSelection(savedInstanceState.getInt("det"));
            txtText.setText(savedInstanceState.getCharSequence("injury"));
            locText.setText(savedInstanceState.getCharSequence("location"));
            crossText.setText(savedInstanceState.getCharSequence("crosses"));
            addlText.setText(savedInstanceState.getCharSequence("additional"));
            numText.setText(savedInstanceState.getCharSequence("dispatcher"));
            simCheck.setChecked(savedInstanceState.getBoolean("simulation"));
        }
    }

    @Override
    public void onDestroy() {
        // Don't forget to shutdown!
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

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

        String newDet = det;
        if (simCheck.isChecked()) {
            newDet = "simulated " + det;
        }
        //Put together the 3 dispatch sentences
        final String firstDispatchText = "Stand by RPI Ambulance for a " + newDet + " determinant " + txt + ". " + locTxt;
        //final String secondDispatchText = "for the RPI Ambulance, a " + newDet + " determinant " + txt + " " + locTxt + ", " + addtxt;
        final String timeStamp = new SimpleDateFormat("HHmm").format(Calendar.getInstance().getTime());
        final String lastDispatchText = "Repeating for the RPI Ambulance, a " + newDet + " determinant " + txt + " " + locTxt + ", " + addtxt + ", crosses of " + crossTxt +", time is " + timeStamp + ", dispatcher " + numTxt;

        DispatchSpeaker ds = new DispatchSpeaker(firstDispatchText, lastDispatchText, tts, getApplicationContext());

        if (!futureCheck.isChecked()) {
            ds.play();
        }
    }
}