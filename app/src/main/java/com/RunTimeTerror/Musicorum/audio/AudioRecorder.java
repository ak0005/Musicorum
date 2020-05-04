package com.RunTimeTerror.Musicorum.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.RunTimeTerror.Musicorum.R;
import com.RunTimeTerror.Musicorum.activities.PianoActivity;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AudioRecorder {
    private static final int RECORDER_BPP = 16;
    private static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    private static final String AUDIO_RECORDER_TEMP_FILE = "tempRecord.raw";
    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static String PIANO_DIR;
    private AudioRecord recorder = null;
    private int bufferSize = 0;
    private Thread recordingThread = null;
    private boolean isRecording = false;
    private boolean needToAppend = false;
    private AppCompatActivity activity;

    public AudioRecorder(AppCompatActivity activity) {
        this.activity = activity;
        PIANO_DIR = PianoActivity.PIANO_DIR;
        bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
    }

    private boolean checkIfExists(String str) {
        if (str == null || "".equals(str.trim())) {
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory(), PIANO_DIR);
        file.mkdirs();
        return new File(file, str).exists();
    }

    public String fileNameResolver(String str, String format) {

        if (str != null && !"".equals(str.trim())) {
            String trim = str.trim();
            if (!trim.endsWith(format)) {
                StringBuilder sb = new StringBuilder();
                sb.append(trim);
                sb.append(format);
                trim = sb.toString();
            }
            String trim2 = trim.split("\\.")[0];
            int i = 1;
            while (checkIfExists(trim)) {
                trim = trim2 + "(" + (i++) + ")" + format;
            }
            return trim;
        }
        return null;
    }

    private void chooseFilename() {
        new MaterialDialog.Builder(activity).title(R.string.choose_filename).customView(R.layout.prompt, true).positiveText(R.string.ok).negativeText("cancel").onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                assert materialDialog.getCustomView() != null;
                String obj = ((EditText) materialDialog.getCustomView().findViewById(R.id.input)).getText().toString();
                if ("".equals(obj.trim())) {
                    Toast.makeText(activity.getApplicationContext(), R.string.enter_filename, Toast.LENGTH_SHORT).show();
                    return;
                }
                materialDialog.dismiss();
                obj = fileNameResolver(obj, AUDIO_RECORDER_FILE_EXT_WAV);
                if (obj == null) {
                    deleteTempFile();
                    if (activity instanceof PianoActivity)
                        ((PianoActivity) activity).handleRecordingException(new Exception("File name resolve to Null"));
                }
                saveFile(obj);
            }
        }).onNegative(new MaterialDialog.SingleButtonCallback() {
            public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                materialDialog.dismiss();
                deleteTempFile();
            }
        }).autoDismiss(false).show();

    }

    private void saveFile(String obj) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, PIANO_DIR);

        if (!file.exists()) {
            file.mkdirs();
        }
        String fileName = file.getAbsolutePath() + "/" + obj;
        try {
            copyWaveFile(getTempFilename(), fileName);
        } catch (Exception e) {
            if (activity instanceof PianoActivity)
                ((PianoActivity) activity).handleRecordingException(new Exception("File name resolve to Null"));
        } finally {
            deleteTempFile();
        }
    }


    private String getTempFilename() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, PIANO_DIR);
        if (!file.exists()) {
            file.mkdirs();
        }

        File tempFile = new File(filepath, AUDIO_RECORDER_TEMP_FILE);

        if (tempFile.exists())
            tempFile.delete();

        return (file.getAbsolutePath() + "/" + AUDIO_RECORDER_TEMP_FILE);
    }

    public void startRecording() {
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);

        int i = recorder.getState();
        if (i == 1)
            recorder.startRecording();

        isRecording = true;
        recordingThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    writeAudioDataToFile();
                } catch (IOException e) {
                    if (activity instanceof PianoActivity)
                        ((PianoActivity) activity).handleRecordingException(new Exception("File name resolve to Null"));
                    //Toast.makeText(MusicorumActivity, R.string.recordingError, Toast.LENGTH_SHORT).show();
                }
            }
        }, "AudioRecorder Thread");

        recordingThread.start();
    }

    public void stopRecording(boolean needToSave) {
        if (null != recorder) {
            isRecording = false;

            int i = recorder.getState();
            if (i == 1)
                recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }
        if (needToSave)
            chooseFilename();
    }

    public void pauseRecording() {
        stopRecording(false);
    }

    public void resumeRecording() {
        needToAppend = true;
        startRecording();
    }

    private void writeAudioDataToFile() throws IOException {
        byte[] data = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;


        os = new FileOutputStream(filename, needToAppend);

        int read = 0;

        while (isRecording) {
            read = recorder.read(data, 0, bufferSize);

            if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                os.write(data);
            }
        }
        os.close();
    }


    private void deleteTempFile() {
        File file = new File(getTempFilename());

        file.delete();
    }

    private void copyWaveFile(String inFilename, String outFilename) throws IOException {
        FileInputStream in = null;
        FileOutputStream out = null;
        long totalAudioLen = 0;
        long totalDataLen = totalAudioLen + 36;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        in = new FileInputStream(inFilename);
        out = new FileOutputStream(outFilename);
        totalAudioLen = in.getChannel().size();
        totalDataLen = totalAudioLen + 36;

        //AppLog.logString("File size: " + totalDataLen);

        WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                longSampleRate, channels, byteRate);

        while (in.read(data) != -1) {
            out.write(data);
        }

        in.close();
        out.close();

        Toast.makeText(activity, "Record saved in " + outFilename, Toast.LENGTH_SHORT).show();
    }

    private void WriteWaveFileHeader(FileOutputStream out, long totalAudioLen,
                                     long totalDataLen, long longSampleRate, int channels,
                                     long byteRate) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (2 * 16 / 8); // block align
        header[33] = 0;
        header[34] = RECORDER_BPP; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }

}