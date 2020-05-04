package com.RunTimeTerror.Musicorum.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.RunTimeTerror.Musicorum.R;
import com.RunTimeTerror.Musicorum.audio.AudioRecorder;
import com.RunTimeTerror.Musicorum.audio.AudioTrackSoundPlayer;
import com.RunTimeTerror.Musicorum.model.Track;
import com.RunTimeTerror.Musicorum.utils.Util;
import com.RunTimeTerror.Musicorum.views.PianoView;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.Builder;
import com.afollestad.materialdialogs.MaterialDialog.ListCallback;
import com.afollestad.materialdialogs.MaterialDialog.SingleButtonCallback;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.obsez.android.lib.filechooser.ChooserDialog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static com.RunTimeTerror.Musicorum.model.Track.trackNames;

public class PianoActivity extends AppCompatActivity {
    public static final String PIANO_DIR = "Musicorum_runTimeTerror";
    public static final String WAVE = ".wav";
    private final int REQUEST_CODE = 1;
    public ArrayList<Integer> notes = new ArrayList<>();
    public ArrayList<String> permissionsRejected = new ArrayList<>();
    public PianoView Musicorum;
    public HashMap<String, Integer> keyMap;
    public ArrayList<Track> tracks;
    AudioRecorder recorder;
    private ImageButton btnRecord;
    private ImageButton audioRecord;
    private ImageButton saveAudioRecord;
    private boolean isBtnRecording = false;
    private boolean isAudioRecording = false;
    private int flag = 0;   // 1 for audio 2 for button 3 for file access 4 for file save 5 for another activity
    private boolean audioRecordingPaused = false;
    private int requiredKeys;
    private int pressedKeys;
    private String contentToSave;


    private OnLongClickListener longTouchListener = new OnLongClickListener() {
        public boolean onLongClick(View view) {
            int id = view.getId();
            int i = R.string.tracks_long_msg;
            switch (id) {
                case R.id.list:
                    i = R.string.list_long_msg;
                    break;
                case R.id.overflow:
                    i = R.string.more_long_msg;
                    break;
                case R.id.stopAudioRecord:
                    i = R.string.stopAudioRecording;
                    break;
                case R.id.AudioRecord:
                    i = R.string.AudioRecord;
                    break;
                case R.id.raz:
                    i = R.string.raz_long_msg;
                    break;
                case R.id.record:
                    i = R.string.record_long_msg;
                    break;
                case R.id.volume_down:
                    i = R.string.volume_down_long_msg;
                    break;
                case R.id.volume_up:
                    i = R.string.volume_up_long_msg;
                    break;
                case R.id.convert:
                    i = R.string.convert;
                    break;
                case R.id.import_record:
                    i = R.string.import_record;
                    break;
                case R.id.animate:
                    i = R.string.animate;
                    break;
            }
            Toast.makeText(PianoActivity.this, i, Toast.LENGTH_LONG).show();
            return true;
        }
    };

    private ArrayList<String> permissions = new ArrayList<>();
    public ArrayList<String> permissionsToRequest = permissionsToRequest(this.permissions);

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.abc_activity_main);
        initTracks();
        initKeyMap();
        this.Musicorum = findViewById(R.id.Musicorum);
        this.Musicorum.setActivity(this, false);
        ImageButton btnTracks = findViewById(R.id.btn);


        btnTracks.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                handleMusics(false);
            }
        });

        ImageButton btnRaz = findViewById(R.id.raz);
        btnRaz.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.Musicorum.razTrack();
            }
        });
        ImageButton btnAnimate = findViewById(R.id.animate);
        btnAnimate.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.handleAnimation();
            }
        });
        ImageButton btnOverflow = findViewById(R.id.overflow);
        btnOverflow.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.manageBtnOverflow();
            }
        });
        this.btnRecord = findViewById(R.id.record);
        this.btnRecord.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.manageButtonRecord();
            }
        });
        ImageButton btnConvert = findViewById(R.id.convert);
        btnConvert.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                flag = 3;
                PianoActivity.this.checkPermissions();
            }
        });
        //--------------audio recorder-------------------------------

        this.audioRecord = this.findViewById(R.id.AudioRecord);
        if (this.isAudioRecording) {
            if (this.audioRecordingPaused)
                this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio_pause);
            else
                this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio);

        }
        this.audioRecord.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.manageAudioRecord();
            }
        });


        this.saveAudioRecord = this.findViewById(R.id.stopAudioRecord);
        if (this.isAudioRecording) {
            this.saveAudioRecord.setImageResource(R.drawable.recording_audio_stop_hot);
        }
        this.saveAudioRecord.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.handleSaveAudioRecord();
            }
        });


        this.audioRecord.setOnLongClickListener(this.longTouchListener);
        this.saveAudioRecord.setOnLongClickListener(this.longTouchListener);
        btnTracks.setOnLongClickListener(this.longTouchListener);
        btnRaz.setOnLongClickListener(this.longTouchListener);
        this.btnRecord.setOnLongClickListener(this.longTouchListener);
        btnOverflow.setOnLongClickListener(this.longTouchListener);
        btnConvert.setOnLongClickListener(this.longTouchListener);
        btnAnimate.setOnLongClickListener(this.longTouchListener);

    }

    void startNewActivity() {
        PianoActivity.this.startActivityForResult(new Intent(PianoActivity.this, ListFilesActivity.class), REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            String returnValue = data.getStringExtra(PIANO_DIR);
            if (returnValue != null) {
                importRecordHelper(new File(returnValue));
            }
        }
    }

    private void initKeyMap() {
        if (keyMap != null)
            return;
        keyMap = new HashMap<>();
        keyMap.put("G4", 5);
        keyMap.put("D#6", 21);
        keyMap.put("B5", 14);
        keyMap.put("F5", 11);
        keyMap.put("D#4", 16);
        keyMap.put("F#4", 17);
        keyMap.put("G#4", 18);
        keyMap.put("F#5", 22);
        keyMap.put("C4", 1);
        keyMap.put("G#5", 23);
        keyMap.put("B4", 7);
        keyMap.put("A5", 13);
        keyMap.put("F4", 4);
        keyMap.put("E5", 10);
        keyMap.put("D5", 9);
        keyMap.put("D4", 2);
        keyMap.put("C5", 8);
        keyMap.put("A#5", 24);
        keyMap.put("C#5", 20);
        keyMap.put("G5", 12);
        keyMap.put("A#4", 19);
        keyMap.put("E4", 3);
        keyMap.put("C#4", 15);
        keyMap.put("A4", 6);
    }


    private void handleAnimation() {
        if (this.isBtnRecording) {
            new Builder(PianoActivity.this).title(getString(R.string.AudioRecorderError)).content(R.string.AudioRecorderErrorMsg).positiveText(R.string.ok).onPositive(new SingleButtonCallback() {
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    materialDialog.dismiss();
                }
            }).show();
            return;
        }
        if (this.isAudioRecording) {
            new Builder(PianoActivity.this).title(getString(R.string.ButtonRecorderError)).content(R.string.ButtonRecorderErrorMsg).positiveText(R.string.ok).onPositive(new SingleButtonCallback() {
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    materialDialog.dismiss();
                }
            }).show();
            return;
        }
        handleMusics(true);

    }

    void handleMusics(final boolean opt) {
        new Builder(PianoActivity.this).title(R.string.track_title).items(trackNames).itemsCallback(new ListCallback() {
                                                                                                        public void onSelection(MaterialDialog materialDialog, View view, int i, CharSequence charSequence) {
                                                                                                            materialDialog.dismiss();

                                                                                                            Toast.makeText(PianoActivity.this, trackNames.get(i), Toast.LENGTH_SHORT).show();
                                                                                                            if (opt) {
                                                                                                                String obj = PianoActivity.this.tracks.get(i).getName() + "/" + PianoActivity.this.tracks.get(i).getAsString(" ");
                                                                                                                Intent intent = new Intent(PianoActivity.this, AnimationActivity.class);
                                                                                                                intent.putExtra(PIANO_DIR, obj);
                                                                                                                startActivity(intent);
                                                                                                            } else {
                                                                                                                requiredKeys = PianoActivity.this.tracks.get(i).getLength();
                                                                                                                pressedKeys = 0;
                                                                                                                PianoActivity.this.Musicorum.setTrack(PianoActivity.this.tracks.get(i));
                                                                                                            }
                                                                                                        }

                                                                                                    }
        ).itemsLongCallback((dialog, itemView, position, text) -> {
            new Builder(PianoActivity.this).title(getString(R.string.deleteConfirmation)).content(getString(R.string.are_you_sure_remove).replace("#name#", trackNames.get(position))).negativeText(R.string.cancel).positiveText(R.string.yes).onPositive(new SingleButtonCallback() {
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    materialDialog.dismiss();
                    trackNames.remove(position);
                    tracks.remove(position);
                    dialog.getItems().remove(position);
                    dialog.notifyItemsChanged();
                }
            }).show();
            return false;

        }).negativeText(R.string.cancel).onNegative(new SingleButtonCallback() {
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
            }
        }).autoDismiss(false).show();
    }


    private void notesExtracter() {
        new ChooserDialog(PianoActivity.this)
                .withFilterRegex(false, false, ".*\\.(wav)")
                .withStartFile(Environment.getExternalStorageDirectory() + "/" + PIANO_DIR)
                .withResources(R.string.title_choose_file, R.string.ok, R.string.cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(PianoActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        new AsyncTaskRunner().execute(path);
                    }
                })
                .build()
                .show();
    }

    private void notesExtracterHelper(final PyObject notes) {
        ArrayList<Integer> keys = new ArrayList<>();
        for (PyObject x : notes.asList()) {
            String s = x.repr().substring(1, x.repr().length() - 1);
            System.out.print(s + " ");
            if (keyMap.containsKey(s)) {
                keys.add(keyMap.get(s));
                System.out.println(keyMap.get(s) + " ");
            } else {
                System.out.println("Not present\n");
            }
        }
        addTrack(keys);
    }

    private boolean checkIfTrackExists(String obj) {
        for (Track t : tracks) {
            if (t.getName().equals(obj)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void onResume() {
        super.onResume();
        AudioTrackSoundPlayer.configureVolume(this);
        if (this.isAudioRecording) {
            Toast.makeText(PianoActivity.this, "Audio Recoder is Paused", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        if (this.isBtnRecording) {
            endButtonRecord();
        }
        if (this.isAudioRecording) {
            pauseResumeAudioRecord(false);
        }
    }


    public void onDestroy() {
        super.onDestroy();
    }

    public void initTracks() {
        if (trackNames == null) {
            trackNames = new ArrayList<>();
            trackNames.add(getString(R.string.merry_christmas));
            trackNames.add(getString(R.string.happy_birthday));
            trackNames.add(getString(R.string.thank_you_tune));
            trackNames.add(getString(R.string.tum_hi_ho));
            trackNames.add(getString(R.string.jingle_bells));
            trackNames.add(getString(R.string.papa_noel));
            trackNames.add(getString(R.string.clair_lune));
            trackNames.add(getString(R.string.frere_jacques));
            trackNames.add(getString(R.string.meunier_dors));
            trackNames.add(getString(R.string.fais_dodo));
            trackNames.add(getString(R.string.hymne_joie));
            trackNames.add(getString(R.string.la_marseillaise));
            trackNames.add(getString(R.string.claire_fontaine));
            trackNames.add(getString(R.string.bon_tabac));
            trackNames.add(getString(R.string.when_saints));
            trackNames.add(getString(R.string.love_tender));
        }
        if (this.tracks == null) {
            tracks = new ArrayList<>();
            this.tracks.add(new Track(getString(R.string.merry_christmas), Arrays.asList(Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(8), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(4))));
            this.tracks.add(new Track(getString(R.string.happy_birthday), Arrays.asList(Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(12), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(12), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8))));
            this.tracks.add(new Track(getString(R.string.thank_you_tune), Arrays.asList(7, 7, 6, 8, 7, 7, 6, 8, 7, 7, 6, 8, 7, 6, 5, 17, 17, 17, 5, 6, 5, 17)));
            this.tracks.add(new Track(getString(R.string.tum_hi_ho), Arrays.asList(4, 19, 18, 17, 4, 16, 4, 19, 18, 17, 4, 16, 4, 19, 18, 17, 4, 16, 4, 19, 18, 17, 4, 16, 4, 15, 16, 4, 16, 4, 16, 4, 16, 4, 4, 17, 4, 4, 20, 20, 8, 8, 19, 19, 18, 18, 4, 17, 4, 17, 18, 17, 15, 16, 4, 4, 17, 4, 15, 16, 4, 18, 17, 4, 4, 20, 20, 8, 8, 19, 19, 18, 18, 4, 17, 4, 17, 18, 17)));
            this.tracks.add(new Track(getString(R.string.jingle_bells), Arrays.asList(Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(2), Integer.valueOf(1))));
            this.tracks.add(new Track(getString(R.string.papa_noel), Arrays.asList(Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(11), Integer.valueOf(12))));
            this.tracks.add(new Track(getString(R.string.clair_lune), Arrays.asList(Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(8))));
            this.tracks.add(new Track(getString(R.string.frere_jacques), Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(1), Integer.valueOf(4))));
            this.tracks.add(new Track(getString(R.string.meunier_dors), Arrays.asList(Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(5))));
            this.tracks.add(new Track(getString(R.string.fais_dodo), Arrays.asList(Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1))));
            this.tracks.add(new Track(getString(R.string.hymne_joie), Arrays.asList(Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4))));
            this.tracks.add(new Track(getString(R.string.la_marseillaise), Arrays.asList(Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(2), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(2), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(6), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(5))));
            this.tracks.add(new Track(getString(R.string.claire_fontaine), Arrays.asList(Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(8), Integer.valueOf(8), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(6), Integer.valueOf(5), Integer.valueOf(4))));
            this.tracks.add(new Track(getString(R.string.bon_tabac), Arrays.asList(Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(8), Integer.valueOf(12), Integer.valueOf(12), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(9), Integer.valueOf(12), Integer.valueOf(12), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(12), Integer.valueOf(8))));
            this.tracks.add(new Track(getString(R.string.when_saints), Arrays.asList(Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(1), Integer.valueOf(3), Integer.valueOf(5), Integer.valueOf(5), Integer.valueOf(4), Integer.valueOf(3), Integer.valueOf(4), Integer.valueOf(5), Integer.valueOf(3), Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(1), Integer.valueOf(1))));
            this.tracks.add(new Track(getString(R.string.love_tender), Arrays.asList(Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(5), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(9), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(10), Integer.valueOf(11), Integer.valueOf(10), Integer.valueOf(9), Integer.valueOf(6), Integer.valueOf(9), Integer.valueOf(8), Integer.valueOf(7), Integer.valueOf(6), Integer.valueOf(7), Integer.valueOf(8))));

        }
    }

    private void addTrack(ArrayList<Integer> keys) {
        if (keys.size() == 0) {
            Toast.makeText(PianoActivity.this, R.string.this_track_has_no_key, Toast.LENGTH_SHORT).show();
            return;
        }
        new Builder(PianoActivity.this).title(getString(R.string.EnterTheRecordName)).customView(R.layout.prompt, true).positiveText(R.string.enter).negativeText("cancel").onPositive(new SingleButtonCallback() {
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                String obj = ((EditText) materialDialog.getCustomView().findViewById(R.id.input)).getText().toString();

                if ("".equals(obj.trim())) {
                    Toast.makeText(PianoActivity.this.getApplicationContext(), R.string.enter_filename, Toast.LENGTH_SHORT).show();
                } else if (!PianoActivity.this.checkIfTrackExists(obj)) {
                    materialDialog.dismiss();
                    PianoActivity.this.tracks.add(new Track(obj, keys));
                    trackNames.add(obj);
                    addTrackExtensor(obj, keys);
                } else {
                    materialDialog.setTitle(R.string.trackNameCollison);
                }
            }
        }).onNegative(new SingleButtonCallback() {
                          public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                              materialDialog.dismiss();
                          }
                      }
        ).autoDismiss(false).show();
    }


    private void addTrackExtensor(String name, ArrayList<Integer> keys) {
        new Builder(PianoActivity.this).title(getString(R.string.title_save)).content(R.string.content_save).positiveText(R.string.ok).onPositive(new SingleButtonCallback() {
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
                flag = 2;
                StringBuilder temp = new StringBuilder();
                temp.append(name).append("/");

                for (int i : keys) {
                    temp.append(i).append("/");
                }
                contentToSave = temp.toString();
                checkPermissions();
            }
        }).show();
    }

    private void saveNotesToFile() {
        String arr[] = contentToSave.split("/", -2);
        for (String a : arr) {
            System.out.println(a);
        }
        String name = new AudioRecorder(this).fileNameResolver(arr[0], ".txt");
        writeToFile(contentToSave, name);
    }


    public void writeToFile(String data, String name) {
        String path = Environment.getExternalStorageDirectory() + File.separator + PIANO_DIR;
        File folder = new File(path);
        folder.mkdirs();

        File file = new File(folder, name);
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);

            myOutWriter.close();

            fOut.flush();
            fOut.close();
        } catch (IOException ignored) {
            Toast.makeText(PianoActivity.this, R.string.operationFailed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(PianoActivity.this, "File saved as " + name, Toast.LENGTH_SHORT).show();
    }

    private void updateVolume(int i) {
        StringBuilder sb = new StringBuilder();
        sb.append(i);
        sb.append("");
        Toast.makeText(this, getString(R.string.volume_title).replace("#volume#", sb.toString()), Toast.LENGTH_LONG).show();
    }


    public void volumeUp() {
        int volumeUp = AudioTrackSoundPlayer.volumeUp();
        Util.saveInPreferences(this, Util.CURRENT_VOLUME_KEY, volumeUp);
        updateVolume(volumeUp);
    }

    public void volumeDown() {
        int volumeDown = AudioTrackSoundPlayer.volumeDown();
        Util.saveInPreferences(this, Util.CURRENT_VOLUME_KEY, volumeDown);
        updateVolume(volumeDown);
    }

    public void manageBtnOverflow() {
        View customView = new Builder(this).title(R.string.toolsbar).customView(R.layout.toolsbar, true).positiveText(17039370).show().getCustomView();

        ImageButton imageButton = customView.findViewById(R.id.volume_up);
        imageButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.volumeUp();
            }
        });
        imageButton.setOnLongClickListener(this.longTouchListener);

        ImageButton imageButton2 = customView.findViewById(R.id.volume_down);
        imageButton2.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.volumeDown();
            }
        });
        imageButton2.setOnLongClickListener(this.longTouchListener);

        ImageButton imageButton3 = customView.findViewById(R.id.list);
        imageButton3.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                flag = 5;
                checkPermissions();
            }
        });
        imageButton3.setOnLongClickListener(this.longTouchListener);

        ImageButton imageButton4 = customView.findViewById(R.id.import_record);
        imageButton4.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                PianoActivity.this.manageRecordImport();
            }
        });
        imageButton4.setOnLongClickListener(this.longTouchListener);

    }


    private void manageRecordImport() {
        flag = 4;
        checkPermissions();
    }

    private void importRecord() {
        new ChooserDialog(PianoActivity.this)
                .withFilterRegex(false, false, ".*\\.(txt)")
                .withStartFile(Environment.getExternalStorageDirectory() + "/" + PIANO_DIR)
                .withResources(R.string.title_choose_file, R.string.ok, R.string.cancel)
                .withChosenListener(new ChooserDialog.Result() {
                    @Override
                    public void onChoosePath(String path, File pathFile) {
                        Toast.makeText(PianoActivity.this, "FILE: " + path, Toast.LENGTH_SHORT).show();
                        importRecordHelper(pathFile);
                    }
                })
                .build()
                .show();
    }


    private void importRecordHelper(File path) {
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
            System.out.println(text);
            String[] arr = text.toString().split("/", -2);
            if (arr.length < 3) {
                Toast.makeText(PianoActivity.this, R.string.import_record_low_size_error, Toast.LENGTH_SHORT).show();
                throw new Exception();
            }

            if (checkIfTrackExists(arr[0])) {
                Toast.makeText(PianoActivity.this, R.string.trackExist, Toast.LENGTH_SHORT).show();
                return;
            }
            ArrayList<Integer> keys = new ArrayList<>();
            for (int i = 1; i < arr.length - 1; ++i)
                keys.add(Integer.parseInt(arr[i]));

            PianoActivity.this.tracks.add(new Track(arr[0], keys));
            trackNames.add(arr[0]);
        } catch (Exception e) {
            e.printStackTrace();
            ;
            Toast.makeText(PianoActivity.this, R.string.operationFailed, Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(PianoActivity.this, "Track added", Toast.LENGTH_SHORT).show();

    }

    private void handleSaveAudioRecord() {
        if (!this.isAudioRecording) {
            Toast.makeText(this, R.string.SaveAudioRecordError, Toast.LENGTH_SHORT).show();
            return;
        }
        this.endAudioRecord();
    }

    private void manageAudioRecord() {
        /*if (this.isBtnRecording) {
            new Builder(PianoActivity.this).title(getString(R.string.AudioRecorderError)).content(R.string.AudioRecorderErrorMsg).positiveText(R.string.ok).onPositive(new SingleButtonCallback() {
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    materialDialog.dismiss();
                }
            }).show();
            return;
        }*/
        if (this.isAudioRecording) {
            this.pauseResumeAudioRecord(true);
        } else {
            flag = 1;
            checkPermissions();
        }
    }

    private void startAudioRecord() {
        recorder = new AudioRecorder(PianoActivity.this);
        recorder.startRecording();
        this.isAudioRecording = true;
        this.audioRecordingPaused = false;
        this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio);
        this.saveAudioRecord.setImageResource(R.drawable.recording_audio_stop_hot);
        Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
    }


    public void handleRecordingException(Exception e) {
        PianoActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                e.printStackTrace();
                PianoActivity.this.isAudioRecording = false;
                PianoActivity.this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio_stop);
                PianoActivity.this.saveAudioRecord.setImageResource(R.drawable.recording_audio_stop);
                Toast.makeText(PianoActivity.this, R.string.recordingError, Toast.LENGTH_SHORT).show();
                recorder = null;
            }
        });

    }


    private void endAudioRecord() {
        this.isAudioRecording = false;
        recorder.stopRecording(true);
        recorder = null;
        this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio_stop);
        this.saveAudioRecord.setImageResource(R.drawable.recording_audio_stop);
    }


    private void pauseResumeAudioRecord(boolean val) {
        if (this.audioRecordingPaused) {

            if (!val) return;
            this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio);
            this.recorder.resumeRecording();

        } else {
            this.recorder.pauseRecording();
            this.audioRecord.setImageResource(R.drawable.ic_action_recording_audio_pause);
            Toast.makeText(PianoActivity.this, "Recording Paused", Toast.LENGTH_SHORT).show();
        }
        this.audioRecordingPaused = !this.audioRecordingPaused;
    }

    public void manageButtonRecord() {
        /*if (this.isAudioRecording) {
            new Builder(PianoActivity.this).title(getString(R.string.ButtonRecorderError)).content(R.string.ButtonRecorderErrorMsg).positiveText(R.string.ok).onPositive(new SingleButtonCallback() {
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    materialDialog.dismiss();
                }
            }).show();
            return;
        }*/
        if (this.isBtnRecording) {
            endButtonRecord();
        } else {
            startButtonRecord();
        }
    }

    private void startButtonRecord() {
        this.isBtnRecording = true;
        this.btnRecord.setImageResource(R.drawable.ic_action_record_hot);
        Toast.makeText(this, R.string.recording, Toast.LENGTH_SHORT).show();
    }

    private void endButtonRecord() {
        this.isBtnRecording = false;
        this.btnRecord.setImageResource(R.drawable.ic_action_record);
        saveButtonRecording();
    }


    private void saveButtonRecording(/*String str*/) {
        addTrack((ArrayList<Integer>) notes.clone());
        notes.clear();
    }


    public void playNote(int i) {
        pressedKeys++;
        if (this.isBtnRecording) {
            this.notes.add(i);
        }
    }

    public void showAccuracy() {
        if (requiredKeys > 0 && pressedKeys > 0)
            Toast.makeText(PianoActivity.this, "Your's strokes were " + 100 * ((double) requiredKeys / pressedKeys) + "% accurate.", Toast.LENGTH_LONG).show();
        requiredKeys = pressedKeys = 0;
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> arrayList) {
        ArrayList<String> arrayList2 = new ArrayList<>();
        for (String str : arrayList) {
            if (hasPermission(str)) {
                arrayList2.add(str);
            }
        }
        return arrayList2;
    }

    private boolean hasPermission(String str) {
        boolean z = true;
        if (VERSION.SDK_INT < 23) {
            return false;
        }
        if (checkSelfPermission(str) != 0) {
            z = false;
        }
        return !z;
    }

    public void checkPermissions() {
        PianoActivity.this.permissions.clear();
        PianoActivity.this.permissions.add("android.permission.READ_EXTERNAL_STORAGE");
        PianoActivity.this.permissions.add("android.permission.WRITE_EXTERNAL_STORAGE");
        if (flag == 1) {
            PianoActivity.this.permissions.add("android.permission.RECORD_AUDIO");
        }
        PianoActivity.this.permissionsToRequest = permissionsToRequest(PianoActivity.this.permissions);

        if (VERSION.SDK_INT < 23 || PianoActivity.this.permissionsToRequest.size() == 0) {
            switch (flag) {
                case 1:
                    startAudioRecord();
                    break;
                case 2:
                    saveNotesToFile();
                    break;
                case 3:
                    notesExtracter();
                    break;
                case 4:
                    importRecord();
                    break;
                case 5:
                    startNewActivity();
                    break;
            }
            flag = 0;

        } else {
            new MaterialDialog.Builder(this).content(R.string.grant_permissions).positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                    PianoActivity.this.requestPermissions(PianoActivity.this.permissionsToRequest.toArray(new String[0]), 1011);
                }
            }).negativeText(R.string.cancel).show();
        }
    }


    public void onRequestPermissionsResult(int i, @NonNull String[] strArr, @NonNull int[] iArr) {
        if (i == 1011) {
            for (String str : PianoActivity.this.permissionsToRequest) {
                if (hasPermission(str)) {
                    PianoActivity.this.permissionsRejected.add(str);
                }
            }
            if (PianoActivity.this.permissionsRejected.size() <= 0) {
                switch (flag) {
                    case 1:
                        startAudioRecord();
                        break;
                    case 2:
                        saveNotesToFile();
                        break;
                    case 3:
                        notesExtracter();
                        break;
                    case 4:
                        importRecord();
                        break;
                    case 5:
                        startNewActivity();
                        break;
                }
                flag = 0;
            } else if (VERSION.SDK_INT >= 23 && shouldShowRequestPermissionRationale(this.permissionsRejected.get(0))) {
                new Builder(this).content(R.string.permissions_mandatory).positiveText(R.string.ok).onPositive(new MaterialDialog.SingleButtonCallback() {
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        PianoActivity.this.requestPermissions(PianoActivity.this.permissionsRejected.toArray(new String[0]), 1011);
                    }
                }).negativeText(R.string.cancel).onNegative(new SingleButtonCallback() {
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        PianoActivity.this.checkPermissions();
                    }
                }).show();
            }
        }
    }


    @SuppressLint("StaticFieldLeak")
    private class AsyncTaskRunner extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        PyObject notes;
        String name;
        boolean without_error = true;

        @Override
        protected String doInBackground(String... obj) {
            publishProgress("Processing..."); // Calls onProgressUpdate()
            try {
                if (!Python.isStarted()) {
                    Python.start(new AndroidPlatform(PianoActivity.this));
                }
                name = obj[0];
                Python python = Python.getInstance();
                PyObject pythonFile = python.getModule("single_file");
                notes = pythonFile.callAttr("all_final", name);
            } catch (Exception e) {
                without_error = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            if (without_error) {
                PianoActivity.this.notesExtracterHelper(notes);
            } else {
                Toast.makeText(PianoActivity.this, "Unable to process", Toast.LENGTH_LONG).show();
            }
        }


        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(PianoActivity.this);
            progressDialog.setTitle("Processing");
            progressDialog.setMessage("this will take a while");
            progressDialog.setCancelable(false);
            progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AsyncTaskRunner.this.cancel(true);
                }
            });
            progressDialog.show();
        }

    }
}