package com.raj.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener {

    public static final String TAG = "VoiceChatbot";
    SpeechRecognizer speech;
    RecyclerView recyclerView;
    ConversationAdapter adapter;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);
        adapter = new ConversationAdapter(this);
        recyclerView = findViewById(R.id.conversation_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        speech.startListening(getSpeechIntent());
    }

    private Intent getSpeechIntent() {
        if (intent == null) {
            intent = new Intent();
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ENGLISH);
        }
        return intent;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        speech.stopListening();
    }


    @Override
    public void onReadyForSpeech(Bundle params) {
        Log.i(TAG, "onReadyForSpeech: ");
    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(TAG, "onBeginningOfSpeech: ");

    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(TAG, "onRmsChanged: " + rmsdB);
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(TAG, "onBufferReceived: ");
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(TAG, "onEndOfSpeech: ");
    }

    @Override
    public void onError(int error) {
        Log.i(TAG, "onError: " + error);
    }

    @Override
    public void onResults(Bundle results) {
        handleResult(results);
        Log.i(TAG, "onResults: " + results.keySet());
    }

    private void handleResult(Bundle results) {
        float[] confidence_scores = results.getFloatArray("confidence_scores");
        int i;
        for (i = 0; i < confidence_scores.length; i++) {
            if (confidence_scores[i] > 0.7) {
                break;
            }
        }
        if (i < confidence_scores.length) {
            ArrayList<String> results_recognition = results.getStringArrayList("results_recognition");
            String responseText = results_recognition.get(i);
            adapter.addMessage(responseText, ConversationAdapter.VIEW_TYPE_USER);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    adapter.addMessage("Please speak again!", ConversationAdapter.VIEW_TYPE_BOT);
                    speech.startListening(getSpeechIntent());
                }
            },500);

        }
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        Log.i(TAG, "onPartialResults: " + partialResults.keySet());
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
        Log.i(TAG, eventType + " onEvent: " + params.keySet());
    }
}

