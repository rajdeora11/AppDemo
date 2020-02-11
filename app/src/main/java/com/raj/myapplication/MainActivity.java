package com.raj.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements RecognitionListener , ActivityCompat.OnRequestPermissionsResultCallback {

    SpeechRecognizer speech;
    ImageButton B_mic,B_video,B_camera;
    private static final int IMAGE_CAPTURE = 102;
    private static final int MEDIA_RECORDER_REQUEST = 0;


    private static int VIDEO_REQUEST;
    private Uri videoUrl = null;



    //video recorder
    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder;
    private File mOutputFile;

    private boolean isRecording = false;
    private static final String TAG = "Recorder";




    //chat boat user interface
    EditText userInput;
    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List<ResponseMessage> responseMessageList;
    TextToSpeech t1;
    RelativeLayout relativeLayout;
    String s="hi what is your name";

    @SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launcher);



        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status!=TextToSpeech.ERROR)
                    t1.setLanguage(Locale.US);

            }
        });

        tts(s);

        if (!hasCamera()) {
            Toast.makeText(getApplicationContext(),"this device not supportable",Toast.LENGTH_LONG).show();
        }

        B_mic=findViewById(R.id.mic);
        B_camera=findViewById(R.id.camera);
        B_video=findViewById(R.id.video);
        mPreview=findViewById(R.id.surface_view);



       // relativeLayout = findViewById(R.id.conversation);
        userInput = findViewById(R.id.userInput);
        recyclerView = findViewById(R.id.conversation);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.setAdapter(messageAdapter);
        

        if (arePermissionGranted()){
            B_video.setEnabled(true);
            B_camera.setEnabled(true);
        }else{
            B_video.setEnabled(false);
            B_camera.setEnabled(false);
            Toast.makeText(getApplicationContext(),"Permissions are needed",Toast.LENGTH_LONG).show();
            requestPermissions();
        }



        B_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(videoIntent.resolveActivity(getPackageManager())!=null)
                {
                    startActivityForResult(videoIntent,VIDEO_REQUEST);
                }




            }
        });



       /* B_video.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (arePermissionGranted()){
                        mPreview.setVisibility(View.VISIBLE);
                        new MediaPrepareTask().execute(null, null, null);
                    } else {
                        requestPermissions();
                    }
                  //  Toast.makeText(getApplicationContext(), "button press", Toast.LENGTH_LONG).show();
                    // lastDown = System.currentTimeMillis();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //Toast.makeText(getApplicationContext(), "button release", Toast.LENGTH_LONG).show();

                    // BEGIN_INCLUDE(stop_release_media_recorder)

                    // stop recording and release camera
                    try {
                        mMediaRecorder.stop();  // stop the recording
                    } catch (RuntimeException e) {
                        // RuntimeException is thrown when stop() is called immediately after start().
                        // In this case the output file is not properly constructed ans should be deleted.
                        Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                        //noinspection ResultOfMethodCallIgnored
                        //mOutputFile.delete();
                    }
                    releaseMediaRecorder(); // release the MediaRecorder object
                    mCamera.lock();         // take camera access back from MediaRecorder

                    // inform the user that recording has stopped
                    // setCaptureButtonText("Capture");
                    isRecording = false;
                    releaseCamera();

                    mPreview.setVisibility(View.INVISIBLE);
                  // relativeLayout.setVisibility(View.VISIBLE);
                  // END_INCLUDE(stop_release_media_recorder)

                }

                return true;
            }
        });
*/
        ResponseMessage responseMessage2 = new ResponseMessage(botReply(""), false);
        responseMessageList.add(responseMessage2);
        tts(responseMessage2.getText().toString());
        messageAdapter.notifyDataSetChanged();
        if (!isLastVisible())
            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

        userInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_SEND) {

                    ResponseMessage responseMessage = new ResponseMessage(userInput.getText().toString(), true);
                    responseMessageList.add(responseMessage);
                    ResponseMessage responseMessage2 = new ResponseMessage(botReply(userInput.getText().toString()), false);
                    responseMessageList.add(responseMessage2);
                    tts(responseMessage2.getText().toString());
                    //tts(userInput.getText().toString());

                    messageAdapter.notifyDataSetChanged();
                    if (!isLastVisible())
                        recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                    userInput.setText("");

                }
                return false;
            }

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==VIDEO_REQUEST&&resultCode==RESULT_OK)
            videoUrl=data.getData();

    }


    public String botReply(String s)
    {
        if(s.equals(""))
            return "Hello.Let's get started. What's your full name?";
        else if(s.equals("Rajpal")||s.equals("Raj")||s.equals("raj")||s.equals("rajpal"))
            return "Nice to meet you," +" "+s+"!\n"+
                    " I need your email id. You will receive mails from recruiters on this email\n" +
                    " What's your email address?";
        else if(s.equals("rajinfoedge@gmail.com")||s.equals("xyz@gmail.com"))
            return "Recruiters need a number where they can reach you with jobs.\n" +
                    "What's your phone number?";
        else if(s.equals("1234567890"))
            return "Got it! Recruiters will be able to reach you at\n" + s+"How many years of experience do you have?";
        else if(s.equals("1")||s.equals("2")||s.equals("3")||s.equals("0"))
            return "Got it. Where are you located?";
        else if(s.equals("noida")||s.equals("jodhpur")||s.equals("bhopal"))
            return "Are you currently working?" ;
        else if(s.equals("yes")||s.equals("Yes")||s.equals("no")||s.equals("No"))
            return "What is your current/previous job title?";
        else if(s.equals("software developer"))
            return "Which company are/were you working with?";
        else if(s.equals("xyz"))
            return "What key skills do you have? eg. production,networking";
        else if(s.equals("production")||s.equals("networking"))
            return "What is your annual salary at your current company? (in lakhs)";
        else if(s.equals("10")||s.equals("11")||s.equals("12")||s.equals("13")||s.equals("14"))
            return "Great! You are almost done\n" +
                    "Let's complete your education details.\n" +
                    " What is your highest education level?";
        else if(s.equals("Doctorate or PhD")||s.equals("Post Graduate")||s.equals("Graduate or Diploma")||s.equals("12th")||s.equals("10th"))
            return "In which year did you pass out of college? Eg. 2019\n" +
                    "Type year of completion if you are currently studying. Eg. 2020\n";
        else if(s.equals("2019")||s.equals("2020"))
            return "Which industry would you like to work in?";
        else if(s.equals("IT")||s.equals("it")||s.equals("Software")||s.equals("software"))
            return "What is your desired location for jobs?";
        else if(s.equals("noida")||s.equals("jodhpur")||s.equals("bhopal")||equals("Noida")||s.equals("Jodhpur")||s.equals("Bhopal"))
            return "Great! Your profile has been created.\n" +
                    "We will mail you if we find a suitable job for you\n" +
                    "Thank you";
        else
            return "It is guided chat bot please fill required datails properly";

    }

    public void tts(String ed1) {
        String toSpeak = ed1;
        //Toast.makeText(getApplicationContext(),toSpeak,Toast.LENGTH_SHORT).show();
        t1.speak(toSpeak,TextToSpeech.QUEUE_FLUSH,null);


    }

    boolean isLastVisible() {
        LinearLayoutManager layoutManager = ((LinearLayoutManager) recyclerView.getLayoutManager());
        int pos = layoutManager.findLastCompletelyVisibleItemPosition();
        int numItems = recyclerView.getAdapter().getItemCount();
        return (pos >= numItems);
    }

    private void startCapture(){

        if (isRecording) {
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
                mOutputFile.delete();
            }
            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
           // setCaptureButtonText("Capture");
            isRecording = false;
            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

        } else {

            // BEGIN_INCLUDE(prepare_start_media_recorder)

            //new MediaPrepareTask().execute(null, null, null);

            // END_INCLUDE(prepare_start_media_recorder)

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }


    private boolean hasCamera() {
        if (getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_ANY)) {
            return true;
        } else {
            Toast.makeText(getApplicationContext(), "This application is not supportable for this device", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public  void CapturePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, IMAGE_CAPTURE);
    }

    private boolean arePermissionGranted() {

        for (String permission : requiredPermissions){
            if (!(ActivityCompat.checkSelfPermission(this, permission) ==
                    PackageManager.PERMISSION_GRANTED)){
                return false;
            }
        }
        return true;
    }


    private void requestPermissions(){
        ActivityCompat.requestPermissions(
                this,
                requiredPermissions,
                MEDIA_RECORDER_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (MEDIA_RECORDER_REQUEST != requestCode) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        boolean areAllPermissionsGranted = true;
        for (int result : grantResults){
            if (result != PackageManager.PERMISSION_GRANTED){
                areAllPermissionsGranted = false;
                break;
            }
        }

        if (areAllPermissionsGranted){
         //   CapturePicture(B_camera);
        } else {
            // User denied one or more of the permissions, without these we cannot record
            // Show a toast to inform the user.
            Toast.makeText(getApplicationContext(), "need Camera permission", Toast.LENGTH_SHORT).show();
        }
    }

    private final String[] requiredPermissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
    };


    public void Display(View view) {
        if(arePermissionGranted()){
            speech = SpeechRecognizer.createSpeechRecognizer(this);
            speech.setRecognitionListener(this);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
            speech.startListening(intent);     
        }else{
            requestPermissions();
        }
       
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
        B_mic.setBackgroundResource(R.drawable.fifth);
    }

    @Override
    public void onBeginningOfSpeech() {
        B_mic.setBackgroundResource(R.drawable.box_two);

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        B_mic.setBackgroundResource(R.drawable.firs);
    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onResults(Bundle bundle) {
        ArrayList<String> arrayList = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String stp = arrayList.toString();
        String spoken = null;
        if (stp.contains(",")) {
            spoken = stp.substring(1, stp.indexOf(","));
            if (spoken.contains("[") && spoken.length() >= 2) {
                spoken = spoken.substring(1, spoken.length() - 1);
            }
        } else {
            spoken = stp;
            if (spoken.contains("[") && spoken.length() >= 2) {
                spoken = spoken.substring(1, spoken.length() - 1);
            }
        }


        if (spoken.length() >= 2) {
            //Toast.makeText(getApplicationContext(),spoken,Toast.LENGTH_LONG).show();

            //userInput.setText(spoken.toString());

            ResponseMessage responseMessage = new ResponseMessage(spoken, true);
            responseMessageList.add(responseMessage);
            ResponseMessage responseMessage2 = new ResponseMessage(botReply(spoken), false);
            responseMessageList.add(responseMessage2);
            messageAdapter.notifyDataSetChanged();
            if (!isLastVisible())
                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

            tts(responseMessage2.getText().toString());
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    speech = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
                    speech.setRecognitionListener(MainActivity.this);
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
                    intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.this.getPackageName());
                    speech.startListening(intent);
                }
            },6000);

           /* speech = SpeechRecognizer.createSpeechRecognizer(MainActivity.this);
            speech.setRecognitionListener(MainActivity.this);
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 0);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, MainActivity.this.getPackageName());
            speech.startListening(intent);
*/

        } else {
          Toast.makeText(getApplicationContext(),"Please say something",Toast.LENGTH_LONG).show();
        }


    }

    @Override
    public void onPartialResults(Bundle bundle) {
        Toast.makeText(getApplicationContext(),"Please say something",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }


    private boolean prepareVideoRecorder(){

        // BEGIN_INCLUDE (configure_preview)
        mCamera = CameraHelper.getDefaultCameraInstance();

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();

        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        mMediaRecorder.setProfile(profile);

        // Step 4: Set output file
        mOutputFile = CameraHelper.getOutputMediaFile(getApplicationContext(),CameraHelper.MEDIA_TYPE_VIDEO);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording
                mMediaRecorder.start();

                isRecording = true;
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
                MainActivity.this.finish();
            }
            // inform the user that recording has started
          //  setCaptureButtonText("Stop");
         Toast.makeText(getApplicationContext(),"recording has started",Toast.LENGTH_LONG).show();
        }
    }


}




