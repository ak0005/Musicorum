package com.RunTimeTerror.Musicorum.audio;

import android.content.Context;
import android.content.res.AssetManager;
import android.media.AudioTrack;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.RunTimeTerror.Musicorum.activities.PianoActivity;
import com.RunTimeTerror.Musicorum.utils.Util;

import java.io.InputStream;

public class AudioTrackSoundPlayer {
    public static final int DEFAULT_CURRENT_VOLUME = 90;
    public static final int MAX_VOLUME = 100;
    public static final SparseArray<String> SOUND_MAP = new SparseArray<>();
    public static int CURRENT_VOLUME = 90;
    public static int INC_VOLUME = 10;
    public static SparseIntArray SOUND_LENGTHS = new SparseIntArray();

    static {
        SOUND_MAP.put(1, "note_do");
        SOUND_MAP.put(2, "note_re");
        SOUND_MAP.put(3, "note_mi");
        SOUND_MAP.put(4, "note_fa");
        SOUND_MAP.put(5, "note_sol");
        SOUND_MAP.put(6, "note_la");
        SOUND_MAP.put(7, "note_si");
        SOUND_MAP.put(8, "second_do");
        SOUND_MAP.put(9, "second_re");
        SOUND_MAP.put(10, "second_mi");
        SOUND_MAP.put(11, "second_fa");
        SOUND_MAP.put(12, "second_sol");
        SOUND_MAP.put(13, "second_la");
        SOUND_MAP.put(14, "second_si");
        SOUND_MAP.put(15, "do_dies");
        SOUND_MAP.put(16, "re_dies");
        SOUND_MAP.put(17, "fa_dies");
        SOUND_MAP.put(18, "sol_dies");
        SOUND_MAP.put(19, "la_dies");
        SOUND_MAP.put(20, "second_dies_do");
        SOUND_MAP.put(21, "second_dies_re");
        SOUND_MAP.put(22, "second_dies_fa");
        SOUND_MAP.put(23, "second_dies_sol");
        SOUND_MAP.put(24, "second_dies_la");
    }

    public Context context;
    private SparseArray<PlayThread> threadMap = null;

    public AudioTrackSoundPlayer(Context context) {
        this.context = context;
        this.threadMap = new SparseArray<>();
    }

    public static void setCurrentVolume(int i) {
        CURRENT_VOLUME = i;
        if (CURRENT_VOLUME > 100) {
            CURRENT_VOLUME = 100;
        }
        if (CURRENT_VOLUME < 0) {
            CURRENT_VOLUME = 0;
        }
    }

    public static int volumeUp() {
        if (CURRENT_VOLUME + INC_VOLUME <= 100) {
            CURRENT_VOLUME += INC_VOLUME;
        }
        return CURRENT_VOLUME;
    }

    public static int volumeDown() {
        if (CURRENT_VOLUME - INC_VOLUME >= 0) {
            CURRENT_VOLUME -= INC_VOLUME;
        }
        return CURRENT_VOLUME;
    }

    public static void configureVolume(Context context2) {
        setCurrentVolume(Util.getPreferenceValue(Util.CURRENT_VOLUME_KEY, DEFAULT_CURRENT_VOLUME, context2));
    }

    public void playNote(int i) {
        if (!isNotePlaying(i)) {
            //AudioTrackSoundPlayer.SOUND_LENGTHS.put(i, System.nanoTime());
            PlayThread playThread = new PlayThread(i);
            playThread.start();
            this.threadMap.put(i, playThread);
        }
    }

    public void stopNote(int i) {
        PlayThread playThread = this.threadMap.get(i);
        if (playThread != null) {
            //AudioTrackSoundPlayer.SOUND_LENGTHS.put(i, System.nanoTime());
            getClass();
            playThread.requestStop();
            this.threadMap.remove(i);
        }
    }

    public boolean isNotePlaying(int i) {
        return this.threadMap.get(i) != null;
    }

    private class PlayThread extends Thread {
        AudioTrack audioTrack = null;
        int note;
        boolean stop = false;

        public PlayThread(int i) {
            this.note = i;
        }


        public void run() {
            long j;
            try {
                String sb2 = AudioTrackSoundPlayer.SOUND_MAP.get(this.note) + PianoActivity.WAVE;
                AssetManager assets = AudioTrackSoundPlayer.this.context.getAssets();
                long length = assets.openFd(sb2).getLength();

                byte[] bArr = new byte[4096];

                this.audioTrack = new AudioTrack(0b11, 44100, 2, 2, 4096, 1);
                float log = (float) (1.0d - (Math.log((double) (100 - AudioTrackSoundPlayer.CURRENT_VOLUME)) / Math.log(100.0d)));

                this.audioTrack.setVolume(log);
                this.audioTrack.play();

                InputStream open = assets.open(sb2);
                long j2 = 0;
                open.read(bArr, 0, 44);
                while (true) {
                    j = length - ((long) 44);
                    if (j2 >= j) {
                        break;
                    }
                    j2 += (long) this.audioTrack.write(bArr, 0, open.read(bArr, 0, 4096));
                }

                AudioTrackSoundPlayer.SOUND_LENGTHS.put(this.note, (int) j);
                this.audioTrack.stop();
                this.audioTrack.release();
                try {
                    if (this.audioTrack == null) {
                        return;
                    }
                } catch (Exception unused) {
                    return;
                }
            } catch (Exception a) {
                if (this.audioTrack == null) {
                    return;
                }
            } catch (Throwable th) {
                try {
                    if (this.audioTrack != null) {
                        this.audioTrack.release();
                    }
                } catch (Exception ignored) {
                }
                throw th;
            }
            this.audioTrack.release();
        }

        synchronized void requestStop() {
            this.stop = true;
        }
    }
}