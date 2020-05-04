package com.RunTimeTerror.Musicorum.activities;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.RunTimeTerror.Musicorum.R;
import com.RunTimeTerror.Musicorum.views.PianoView;

public class AnimationActivity extends AppCompatActivity {

    boolean flag;
    private PianoView animationView;
    private AsyncRunner runner;
    private View.OnLongClickListener longTouchListener = new View.OnLongClickListener() {
        public boolean onLongClick(View view) {
            int id = view.getId();
            int i = R.string.tracks_long_msg;
            switch (id) {
                case R.id.stopAudioRecord:
                    i = R.string.stopAudioRecording;
                    break;
                case R.id.AudioRecord:
                    i = R.string.AudioRecord;
                    break;
            }
            Toast.makeText(AnimationActivity.this, i, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.abc_paino_animation);

        this.animationView = findViewById(R.id.MusicorumAnimation);
        this.animationView.setActivity(this, true);

        TextView t = findViewById(R.id.MusicorumAnimationText);
        bundle = getIntent().getExtras();
        assert bundle != null;
        String track = bundle.getString(PianoActivity.PIANO_DIR);
        assert track != null;


        StringBuilder stringBuilder = new StringBuilder();
        int i;

        for (i = 0; i < track.length(); ++i) {
            if (track.charAt(i) == '/')
                break;
            stringBuilder.append(track.charAt(i));
        }

        t.setText(stringBuilder.toString());
        track = track.substring(i + 1);

        this.runner = (AsyncRunner) new AsyncRunner().execute(track);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            Thread.sleep(700);                // in worst case
            runner.cancel(true);
        } catch (InterruptedException ignored) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        runner.cancel(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (flag)
            this.finish();
        else flag = true;
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncRunner extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... obj) {
            publishProgress("Processing..."); // Calls onProgressUpdate()
            String arr[] = obj[0].split(" ", -1);
            try {
                Thread.sleep(500);
                for (String x : arr) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AnimationActivity.this.animationView.animate(Integer.parseInt(x));
                        }
                    });
                    Thread.sleep(700);
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AnimationActivity.this, R.string.operationFailed, Toast.LENGTH_LONG).show();
                    }
                });
                return null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Toast.makeText(AnimationActivity.this, R.string.AnimationFinished, Toast.LENGTH_LONG).show();
            AnimationActivity.this.finish();
        }
    }
}