package com.example.mydrivingapp;

import static android.net.wifi.p2p.WifiP2pManager.ERROR;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import com.example.mydrivingapp.model.Case;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    static String[] eng = {"Are you all set?", "I'm sure you'll do better next time", "I wish you all the best"};
    static String[] kor = {"준비 다 됐어?", "다음에 더 잘할거라고 확신해.", "모든 일이 잘되시길 빌어요."};
    int idx = 0;
    private int question = 0;
    private long delay1 = 0;
    private long delay2 = 0;
    private long speechLen1 = 0;
    private long speechLen2 = 0;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    TextToSpeech tts;
    SpeechRecognizer speechRecognizer;

    TextView speaking;

    TextView firstSpeak;
    TextView firstKor;
    TextView progress;
    TextView follow;
    TextView postSpeech;
    ImageButton blindBtn;
    ImageButton leftBtn;
    ImageButton rightBtn;
    ImageButton replayBtn;
    ImageButton correct;

    LinearLayout something;

    private static final int REQUEST_CODE_SPEECH_INPUT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        firstSpeak = findViewById(R.id.firstSpeech);
        firstKor = findViewById(R.id.firstKor);
        progress = findViewById(R.id.progress);
        follow = findViewById(R.id.follow);
        postSpeech = findViewById(R.id.postSpeech);
        speaking = findViewById(R.id.speaking);
        firstSpeak.setText(eng[idx]);
        firstKor.setText(kor[idx]);
        progress.setText(idx+1 + "/3");
        follow.setText("");
        postSpeech.setText("");
        something = findViewById(R.id.something);
        blindBtn = findViewById(R.id.blind);
        leftBtn = findViewById(R.id.leftbutton);
        rightBtn = findViewById(R.id.rightbutton);
        replayBtn = findViewById(R.id.replaybtn);
        correct = findViewById(R.id.correct);
        blindBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                blindBtn.setSelected(!blindBtn.isSelected());
                if (blindBtn.isSelected()) {
                    firstSpeak.setVisibility(View.INVISIBLE);
                } else {
                    firstSpeak.setVisibility(View.VISIBLE);
                }
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idx--;
                progress.setText(idx-1 + "/3");
                firstSpeak.setText(eng[idx]);
                firstKor.setText(kor[idx]);
                postSpeech.setText("");
                correct.setBackgroundResource(R.drawable.round_box_g);
                speaking.setBackgroundResource(R.drawable.circleempty);
                speaking.setTextColor(Color.BLACK);
                speak(firstSpeak.getText().toString());
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                idx++;
                progress.setText(idx+1 + "/3");
                firstSpeak.setText(eng[idx]);
                firstKor.setText(kor[idx]);
                postSpeech.setText("");
                correct.setBackgroundResource(R.drawable.round_box_g);
                speaking.setBackgroundResource(R.drawable.circleempty);
                speaking.setTextColor(Color.BLACK);
                speak(firstSpeak.getText().toString());
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = tts.setLanguage(Locale.CANADA);
                    if(result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA){
                        Log.e("TTS", "This Language is not supported");
                    }
                    ttsInitialize();
                }else{
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });



        sttInitialize();

        replayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speak(firstSpeak.getText().toString());
            }
        });

        requestRecordAudioPermission();

//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
//        audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
//        audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
//        audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
//        audioManager.setStreamMute(AudioManager.STREAM_RING, true);
//        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    private void requestRecordAudioPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String requiredPermission = Manifest.permission.RECORD_AUDIO;

            // If the user previously denied this permission then show a message explaining why
            // this permission is needed
            if (checkCallingOrSelfPermission(requiredPermission) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(new String[]{requiredPermission}, 101);
            }
        }
    }

    private void sttInitialize() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {
                Log.e("STT", "ready");
            }

            @Override
            public void onBeginningOfSpeech() {
                delay2 = System.currentTimeMillis();
                speechLen1 = System.currentTimeMillis();
                Log.e("발화까지 걸리는 시간:", String.valueOf((delay2-delay1)/1000) + "초");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                Log.e("STT", "speach end");
            }

            @Override
            public void onError(int i) {
                startListen();
            }

            @Override
            public void onResults(Bundle bundle) {
                speechLen2 = System.currentTimeMillis();
                Log.e("발화 시간:", String.valueOf((speechLen2-speechLen1)/1000) + "초");

                ArrayList<String> str = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (str.get(0) != null) {
                    postSpeech.setText(str.get(0));

                    answerCheck();
                }

            }

            @Override
            public void onPartialResults(Bundle bundle) {
//                ArrayList<String> matches = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
//                String text = "";
//                for (String result : matches)
//                    text += result;
//
//                postSpeech.setText(text);
            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
    }

    private void answerCheck() {
        String[] first = firstSpeak.getText().toString().replace("?", "").toLowerCase().split(" ");
        String[] post = postSpeech.getText().toString().toLowerCase().split(" ");
        int cnt = 0;

        if (first.length != post.length) {
            cnt = (int) Math.abs(first.length - post.length);
            question++;
            correct.setBackgroundResource(R.drawable.ic_baseline_priority_high_24);
        } else {
            for (int i = 0; i < first.length; i++) {
                if (!first[i].equals(post[i])) cnt++;
            }
            if (cnt == 0) correct.setBackgroundResource(R.drawable.ic_baseline_check_24);
            else {
                correct.setBackgroundResource(R.drawable.ic_baseline_priority_high_24);
                question++;
            }
        }

        follow.setText("");

        Log.e("틀린 단어의 개수:", String.valueOf(cnt));
        Log.e("현재까지 틀린 문항의 수:", String.valueOf(question));

        writeNewCase(idx, cnt, question, (int) (delay2-delay1)/1000, (int) (speechLen2-speechLen1)/1000);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                rightBtn.callOnClick();

                ttsInitialize();
            }
        }, 1500);
    }


    private void ttsInitialize() {
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {

            }

            @Override
            public void onDone(String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        follow.setText("이제 따라해보세요.");
                        speaking.setBackgroundResource(R.drawable.circleshape);
                        speaking.setTextColor(Color.WHITE);
                    }
                });

                something.post(new Runnable() {
                    @Override
                    public void run() {
                        delay1 = System.currentTimeMillis();
                        startListen();
                    }
                });
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
//
//                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
//                        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
//
//
//                        speechRecognizer.startListening(intent);
//
//                        Handler handler = new Handler();
//                        handler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                speechRecognizer.stopListening();
//                            }
//                        }, 3000);
//                    }
//                }).start();
//                follow.setText("이제 따라해보세요.");
//                startListening();
            }

            @Override
            public void onError(String s) {

            }
        });
    }

    private void writeNewCase(int idx, int wordCnt, int questionCnt, int delayToSpeak, int delayDuringSpeak) {
        Case ca = new Case(wordCnt, questionCnt, delayToSpeak, delayDuringSpeak);

        mDatabase.child("case").child(String.valueOf(idx)).setValue(ca).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Log.e("main", "저장성공");
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("main", "저장실패");
            }
        });
    }

    private void startListen() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra("android.speech.extra.DICTATION_MODE", true);


        speechRecognizer.startListening(intent);
    }

    private void speak(String text) {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_NOT_SUPPORTED || result == TextToSpeech.LANG_MISSING_DATA) {
                        Log.e("TTS", "This Language is not supported");
                    } else {
                        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
                    }
                } else {
                    Log.e("TTS", "Initialization Failed!");
                }
            }
        });
    }

    private void saveIdx() {
        SharedPreferences pref = getSharedPreferences("index", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("idx", idx);
        editor.commit();
    }

    private void loadIdx() {
        SharedPreferences pref = getSharedPreferences("index", Activity.MODE_PRIVATE);
        int current = pref.getInt("idx", 0);
        currentScreen(current);
    }

    private void currentScreen(int current) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progress.setText(current+1 + "/3");
                firstSpeak.setText(eng[current]);
                firstKor.setText(kor[current]);
                postSpeech.setText("");
                correct.setBackgroundResource(R.drawable.round_box_g);
                speaking.setBackgroundResource(R.drawable.circleempty);
                speaking.setTextColor(Color.BLACK);
                speak(firstSpeak.getText().toString());

                ttsInitialize();
            }
        });
    }

    @Override
    protected void onStart() {
        speak(firstSpeak.getText().toString());
        super.onStart();
    }

    @Override
    protected void onPause() {
        saveIdx();
        super.onPause();
    }

    @Override
    protected void onResume() {
        loadIdx();
        super.onResume();
    }



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
//            if (resultCode == RESULT_OK && data != null) {
//                ArrayList<String> result = data.getStringArrayListExtra(
//                        RecognizerIntent.EXTRA_RESULTS);
//                postSpeech.setText(
//                        Objects.requireNonNull(result).get(0));
//            }
//        }
//    }

    @Override
    protected void onDestroy() {

        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }

        speechRecognizer.stopListening();
        speechRecognizer.destroy();

        super.onDestroy();
    }


}

