package com.RunTimeTerror.Musicorum.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.FileProvider;

import com.RunTimeTerror.Musicorum.BuildConfig;
import com.RunTimeTerror.Musicorum.R;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.MaterialDialog.ButtonCallback;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class ListFilesActivity extends AppCompatActivity {
    public static final String ATTACH_TYPE = "audio/wav";
    private static final String FILENAMES = "filenames";
    private static final String TEXT_FILE_EXTENSION = "txt";
    private static final String SOUND_FILE_EXTENSION = "wav";
    private static final String ATTACH_TYPE2 = "Musicorum/notes";
    public Dialog controlDialog = null;
    public Dialog controlTextDialog = null;
    public List<String> files = new ArrayList<>();
    public MediaPlayer mediaPlayer = null;
    public String sound = null;
    public ProgressBar soundProgress;
    @SuppressLint("HandlerLeak")
    public Handler progressHandler = new Handler() {
        public void handleMessage(final Message message) {
            ListFilesActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    if (ListFilesActivity.this.soundProgress != null) {
                        ListFilesActivity.this.soundProgress.setProgress(message.what);
                    }
                }
            });
        }
    };
    private final OnClickListener controlsListener = new OnClickListener() {
        public void onClick(View view) {
            if (ListFilesActivity.this.sound != null && ListFilesActivity.this.mediaPlayer != null) {
                int id = view.getId();
                if (id == R.id.play) {
                    ListFilesActivity.this.mediaPlayer.start();
                    ListFilesActivity.this.startProgress();
                } else if (id == R.id.share) {
                    ListFilesActivity.this.mediaPlayer.stop();
                    ListFilesActivity.this.stopProgress();
                    if (ListFilesActivity.this.controlDialog != null) {
                        ListFilesActivity.this.controlDialog.cancel();
                    }
                    ListFilesActivity.this.shareSound(ListFilesActivity.this.sound);
                } else if (id == R.id.stop) {
                    ListFilesActivity.this.mediaPlayer.stop();
                    ListFilesActivity.this.stopProgress();
                    if (ListFilesActivity.this.controlDialog != null) {
                        ListFilesActivity.this.controlDialog.cancel();
                    }
                }
            }
        }
    };
    private String text;
    private final OnClickListener controlsTextListener = new OnClickListener() {
        public void onClick(View view) {
            int id = view.getId();

            if (id == R.id.import_record_suggestion) {
                Intent output = new Intent();
                setResult(RESULT_OK, output);
                output.putExtra(PianoActivity.PIANO_DIR, ListFilesActivity.this.text);
                finish();
            } else if (id == R.id.share_record) {
                if (ListFilesActivity.this.controlTextDialog != null) {
                    ListFilesActivity.this.controlTextDialog.cancel();
                }
                ListFilesActivity.this.shareText(ListFilesActivity.this.text);
            }
        }
    };
    private final OnItemClickListener itemClickListener = new OnItemClickListener() {

        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            String str = (String) ListFilesActivity.this.files.get(i);

            if (str == null || "".equals(str.trim()))
                return;
            String[] arr = str.split("\\.", -2);
            if (arr[arr.length - 1].equals(SOUND_FILE_EXTENSION)) {
                ListFilesActivity.this.showPlayControls(str);
            } else if (arr[arr.length - 1].equals(TEXT_FILE_EXTENSION)) {
                ListFilesActivity.this.showTextControls(str);
            }

        }
    };
    private TextView dirTv;
    private ListView listView;
    private final OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long j) {
            String str = (String) ListFilesActivity.this.files.get(i);
            if (str != null && !"".equals(str.trim())) {
                ListFilesActivity.this.dialogDeleteSound(str);
            }
            return false;
        }
    };
    private ProgressBar progressBar;

    @SuppressLint("HandlerLeak")
    public Handler handler = new Handler() {
        public void handleMessage(Message message) {
            if (message.what == 1) {
                ListFilesActivity.this.files = new ArrayList<>();
            } else {
                ArrayList arrayList = (ArrayList) message.getData().getSerializable(ListFilesActivity.FILENAMES);
                if (arrayList != null && !arrayList.isEmpty()) {
                    ListFilesActivity.this.files = arrayList;
                }
            }
            ListFilesActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    ListFilesActivity.this.hideProgressBar();
                    ListFilesActivity.this.getListView().setAdapter(new ArrayAdapter<>(ListFilesActivity.this.getApplicationContext(), R.layout.list_item, 16908308, ListFilesActivity.this.files));
                }
            });
        }
    };


    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView((int) R.layout.list_files);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
        this.dirTv = (TextView) findViewById(R.id.dirFiles);
        this.listView = (ListView) findViewById(R.id.list);
        getListView().setDivider(new GradientDrawable(Orientation.RIGHT_LEFT, new int[]{0, -13388315, 0}));
        getListView().setDividerHeight(2);
        getListView().setOnItemClickListener(this.itemClickListener);
        getListView().setOnItemLongClickListener(this.itemLongClickListener);
    }


    public ListView getListView() {
        return this.listView;
    }


    public void onResume() {
        super.onResume();
        this.dirTv.setText(new File(Environment.getExternalStorageDirectory(), PianoActivity.PIANO_DIR).getAbsolutePath());
        listFiles();
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 16908332) {
            return super.onOptionsItemSelected(menuItem);
        }
        Intent intent = new Intent(this, PianoActivity.class);
        if (NavUtils.shouldUpRecreateTask(this, intent)) {
            TaskStackBuilder.from(this).addNextIntent(intent).startActivities();
            finish();
        } else {
            NavUtils.navigateUpTo(this, intent);
        }
        return true;
    }

    private void showProgressBar() {
        this.progressBar.setVisibility(0);
    }


    @SuppressLint("WrongConstant")
    public void hideProgressBar() {
        this.progressBar.setVisibility(4);
    }

    private void listFiles() {
        showProgressBar();
        new Thread(new Runnable() {
            public void run() {
                File file = new File(Environment.getExternalStorageDirectory(), PianoActivity.PIANO_DIR);
                file.mkdirs();
                File[] listFiles = file.listFiles();
                if (listFiles == null || listFiles.length <= 0) {
                    ListFilesActivity.this.handler.sendEmptyMessage(1);
                    return;
                }
                ArrayList arrayList = new ArrayList(listFiles.length);
                for (File name : listFiles) {
                    arrayList.add(name.getName());
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putSerializable(ListFilesActivity.FILENAMES, arrayList);
                message.setData(bundle);
                ListFilesActivity.this.handler.dispatchMessage(message);
            }
        }).start();
    }


    public void showPlayControls(String str) {
        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setOnCompletionListener(new OnCompletionListener() {
            public void onCompletion(MediaPlayer mediaPlayer) {
                ListFilesActivity.this.sound = null;
                ListFilesActivity.this.mediaPlayer = null;
                if (ListFilesActivity.this.controlDialog != null) {
                    ListFilesActivity.this.controlDialog.cancel();
                }
                ListFilesActivity.this.controlDialog = null;
            }
        });
        try {
            this.sound = new File(new File(Environment.getExternalStorageDirectory(), PianoActivity.PIANO_DIR), str).getAbsolutePath();
            this.mediaPlayer.setDataSource(this.sound);
            this.mediaPlayer.prepare();
        } catch (Exception unused) {
            this.mediaPlayer = null;
        }
        this.controlDialog = createControlSoundDialog(str);
        this.controlDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialogInterface) {
                if (ListFilesActivity.this.mediaPlayer != null) {
                    try {
                        ListFilesActivity.this.mediaPlayer.stop();
                    } catch (Exception unused) {
                    }
                }
            }
        });
        this.controlDialog.show();
    }

    public void showTextControls(String str) {
        this.text = new File(new File(Environment.getExternalStorageDirectory(), PianoActivity.PIANO_DIR), str).getAbsolutePath();
        this.controlTextDialog = createControlTextDialog(str);
        this.controlTextDialog.show();
    }


    public void startProgress() {
        new Thread(new Runnable() {
            public void run() {
                try {
                    if (ListFilesActivity.this.mediaPlayer != null) {
                        while (ListFilesActivity.this.mediaPlayer.getCurrentPosition() < ListFilesActivity.this.mediaPlayer.getDuration()) {
                            ListFilesActivity.this.progressHandler.sendEmptyMessage((ListFilesActivity.this.mediaPlayer.getCurrentPosition() * 100) / ListFilesActivity.this.mediaPlayer.getDuration());
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException unused) {
                            }
                        }
                        ListFilesActivity.this.progressHandler.sendEmptyMessage(100);
                    }
                } catch (Exception unused2) {
                }
            }
        }).start();
    }


    public void stopProgress() {
        if (this.soundProgress != null) {
            this.soundProgress.setProgress(100);
        }
    }


    public void removeSound(String str) {
        if (str != null && !"".equals(str.trim())) {
            try {
                if (new File(new File(Environment.getExternalStorageDirectory(), PianoActivity.PIANO_DIR), str).delete()) {
                    this.files.remove(str);
                    getListView().setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.list_item, 0x1020014, this.files));
                    Toast.makeText(getApplicationContext(), getString(R.string.deleted).replace("#name#", str), 0).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), getString(R.string.error_deleting).replace("#name#", str), 0).show();
            } catch (Exception unused) {
            }
        }
    }


    public void dialogDeleteSound(final String str) {
        new MaterialDialog.Builder(this).title((int) R.string.remove_title).content((CharSequence) getString(R.string.are_you_sure_remove).replace("#name#", str)).positiveText(R.string.ok).negativeText(R.string.cancel).callback(new ButtonCallback() {
            public void onPositive(MaterialDialog materialDialog) {
                ListFilesActivity.this.removeSound(str);
                try {
                    materialDialog.dismiss();
                } catch (Exception unused) {
                }
            }

            public void onNegative(MaterialDialog materialDialog) {
                try {
                    materialDialog.dismiss();
                } catch (Exception unused) {
                }
            }
        }).show();
    }

    private Dialog createControlSoundDialog(String str) {
        MaterialDialog build = new MaterialDialog.Builder(this).autoDismiss(false).title((CharSequence) str).customView((int) R.layout.sound_control, true).build();

        View customView = build.getCustomView();
        ((ImageButton) customView.findViewById(R.id.play)).setOnClickListener(this.controlsListener);
        ((ImageButton) customView.findViewById(R.id.stop)).setOnClickListener(this.controlsListener);
        ((ImageButton) customView.findViewById(R.id.share)).setOnClickListener(this.controlsListener);
        this.soundProgress = (ProgressBar) customView.findViewById(R.id.soundProgress);
        if (this.soundProgress != null) {
            this.soundProgress.setMax(100);
            this.soundProgress.setProgress(0);
        }
        return build;
    }

    private Dialog createControlTextDialog(String str) {

        MaterialDialog build = new MaterialDialog.Builder(this).title((CharSequence) str).customView((int) R.layout.text_control, true).build();
        View customView = build.getCustomView();
        ((ImageButton) customView.findViewById(R.id.import_record_suggestion)).setOnClickListener(this.controlsTextListener);
        ((ImageButton) customView.findViewById(R.id.share_record)).setOnClickListener(this.controlsTextListener);

        return build;
    }


    public void shareSound(String str) {
        if (str != null && !"".equals(str.trim())) {
            File file = new File(str);
            if (file.exists()) {
                String name = file.getName();
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType(ATTACH_TYPE);
                intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.share_title).replace("#name#", name));
                intent.putExtra("android.intent.extra.TEXT", getString(R.string.shared_from));
                intent.putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", file));
                intent.addFlags(1);

                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                return;
            }
            Toast.makeText(getApplicationContext(), R.string.share_error, 0).show();
        }
    }

    public void shareText(String str) {
        if (str != null && !"".equals(str.trim())) {
            File file = new File(str);
            if (file.exists()) {
                String name = file.getName();
                Intent intent = new Intent("android.intent.action.SEND");
                intent.setType(ATTACH_TYPE2);
                intent.putExtra("android.intent.extra.SUBJECT", getString(R.string.share_title_text).replace("#name#", name));
                intent.putExtra("android.intent.extra.TEXT", getString(R.string.shared_from));
                intent.putExtra("android.intent.extra.STREAM", FileProvider.getUriForFile(Objects.requireNonNull(getApplicationContext()), BuildConfig.APPLICATION_ID + ".provider", file));
                intent.addFlags(1);

                startActivity(Intent.createChooser(intent, getString(R.string.share)));
                return;
            }
            Toast.makeText(getApplicationContext(), R.string.share_error, 0).show();
        }
    }
}