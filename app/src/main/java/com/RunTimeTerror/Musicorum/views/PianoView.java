package com.RunTimeTerror.Musicorum.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.InputDeviceCompat;
import androidx.core.view.ViewCompat;

import com.RunTimeTerror.Musicorum.activities.PianoActivity;
import com.RunTimeTerror.Musicorum.audio.AudioTrackSoundPlayer;
import com.RunTimeTerror.Musicorum.model.Key;
import com.RunTimeTerror.Musicorum.model.Track;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class PianoView extends View {

    public Handler handler = new Handler() {
        public void handleMessage(Message message) {
            PianoView.this.invalidate();
        }
    };
    boolean isTrackSelected = false;
    boolean isTouchedDisabled;
    private AppCompatActivity activity;
    private Paint black = new Paint();
    private Paint blue;
    @SuppressLint({"UseSparseArrays"})
    private HashMap<Integer, Key> dieses = new HashMap<>();
    private int height;
    private int keyWidth;
    @SuppressLint({"UseSparseArrays"})
    private HashMap<Integer, Key> keys = new HashMap<>();
    private AudioTrackSoundPlayer soundPlayer;
    private Track track;
    private Paint white;
    private Paint yellow;

    public PianoView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.black.setColor(ViewCompat.MEASURED_STATE_MASK);
        this.white = new Paint();
        this.white.setColor(-1);
        this.white.setStyle(Style.FILL);
        this.yellow = new Paint();
        this.yellow.setColor(InputDeviceCompat.SOURCE_ANY);
        this.yellow.setStyle(Style.FILL);
        this.blue = new Paint();
        this.blue.setColor(-16776961);
        this.blue.setStyle(Style.FILL);
        this.soundPlayer = new AudioTrackSoundPlayer(context);
    }

    public void setActivity(AppCompatActivity activity, boolean isTouchedDisabled) {
        this.activity = activity;
        this.isTouchedDisabled = isTouchedDisabled;
    }


    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        this.keyWidth = i / 14;
        this.height = i2;
        int i5 = 15;
        int i6 = 0;
        while (i6 < 14) {
            int i7 = this.keyWidth * i6;
            int i8 = this.keyWidth + i7;
            if (i6 == 13) {
                i8 = i;
            }
            RectF rectF = new RectF((float) i7, 0.0f, (float) i8, (float) i2);
            int i9 = i6 + 1;
            this.keys.put(Integer.valueOf(i9), new Key(rectF, i9));
            if (!(i6 == 0 || i6 == 3 || i6 == 7 || i6 == 10)) {
                this.dieses.put(Integer.valueOf(i5), new Key(new RectF((((float) (i6 - 1)) * ((float) this.keyWidth)) + (((float) this.keyWidth) * 0.5f) + (((float) this.keyWidth) * 0.25f) - 7.5f, 0.0f, (((float) i6) * ((float) this.keyWidth)) + (((float) this.keyWidth) * 0.25f) + 7.5f, ((float) this.height) * 0.67f), i5));
                i5++;
            }
            i6 = i9;
        }
    }


    public void onDraw(Canvas canvas) {
        for (Key key : this.keys.values()) {
            RectF rectF = key.rect;
            Paint paint = key.down ? this.yellow : key.selected ? this.blue : this.white;
            canvas.drawRect(rectF, paint);
        }
        for (int i = 1; i < 14; i++) {
            canvas.drawLine((float) (this.keyWidth * i), 0.0f, (float) (this.keyWidth * i), (float) this.height, this.black);
        }
        for (Key key2 : this.dieses.values()) {
            RectF rectF2 = key2.rect;
            Paint paint2 = key2.down ? this.yellow : key2.selected ? this.blue : this.black;
            canvas.drawRect(rectF2, paint2);
        }
    }

    public void animate(int ind) {
        Key key = null;
        if (dieses.containsKey(ind))
            key = dieses.get(ind);
        else if (keys.containsKey(ind))
            key = keys.get(ind);

        if (key == null)
            return;
        if (!key.down) {
            this.soundPlayer.stopNote(key.sound);
            releaseKey(key);
        }
        key.down = true;
        invalidate();
        this.soundPlayer.playNote(key.sound);
        releaseKey(key);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (isTouchedDisabled)
            return true;
        int action = motionEvent.getAction();
        System.out.println("akhil" + action);
        boolean z = (action & 5) == 5 || action == 0 || action == 2;
        for (int i = 0; i < motionEvent.getPointerCount(); i++) {
            Key keyForCoords = keyForCoords(motionEvent.getX(i), motionEvent.getY(i));
            if (keyForCoords != null) {
                keyForCoords.down = z;
            }
        }
        playKeys();
        return true;
    }

    void playKeys() {
        ArrayList arrayList = new ArrayList(this.keys.values());
        arrayList.addAll(this.dieses.values());
        Iterator it = arrayList.iterator();
        while (it.hasNext()) {
            Key key = (Key) it.next();
            if (!key.down) {
                this.soundPlayer.stopNote(key.sound);
                releaseKey(key);
            } else if (!this.soundPlayer.isNotePlaying(key.sound)) {
                this.soundPlayer.playNote(key.sound);
                if (this.activity instanceof PianoActivity)
                    ((PianoActivity) this.activity).playNote(key.sound);
                int currentNoteFromTrack = currentNoteFromTrack();
                if (key.sound == currentNoteFromTrack) {
                    unSelectKey(currentNoteFromTrack);
                    nextNoteFromTrack();
                    int currentNoteFromTrack2 = currentNoteFromTrack();
                    if (currentNoteFromTrack2 != -1) {
                        selectKey(currentNoteFromTrack2);
                    } else if (isTrackSelected) {
                        isTrackSelected = false;
                        if (this.activity instanceof PianoActivity)
                            ((PianoActivity) this.activity).showAccuracy();
                    }
                }
                invalidate();
            } else {
                releaseKey(key);
            }
        }
    }


    private Key keyForCoords(float f, float f2) {
        for (Key key : this.dieses.values()) {
            if (key.rect.contains(f, f2)) {
                return key;
            }
        }
        for (Key key2 : this.keys.values()) {
            if (key2.rect.contains(f, f2)) {
                return key2;
            }
        }
        return null;
    }

    private void selectKey(int i) {
        Key key = (Key) this.keys.get(Integer.valueOf(i));
        if (key == null) {
            key = (Key) this.dieses.get(Integer.valueOf(i));
        }
        if (key != null) {
            key.selected = true;
        }
    }

    private void unSelectKey(int i) {
        Key key = (Key) this.keys.get(Integer.valueOf(i));
        if (key == null) {
            key = (Key) this.dieses.get(Integer.valueOf(i));
        }
        if (key != null) {
            key.selected = false;
        }
    }

    public void razTrack() {
        this.track = null;
        razKeys();
        invalidate();
    }

    public void setTrack(Track track2) {
        this.track = track2;
        isTrackSelected = true;
        initTrack();
    }

    public void initTrack() {
        if (this.track != null) {
            this.track.reset();
            unSelectKeys();
            selectKey(this.track.current());
            invalidate();
        }
    }

    public void unSelectKeys() {
        for (Key key : this.dieses.values()) {
            key.selected = false;
        }
        for (Key key2 : this.keys.values()) {
            key2.selected = false;
        }
    }

    public void razKeys() {
        for (Key key : this.dieses.values()) {
            key.selected = false;
            key.down = false;
        }
        for (Key key2 : this.keys.values()) {
            key2.selected = false;
            key2.down = false;
        }
    }

    public int currentNoteFromTrack() {
        if (this.track != null) {
            return this.track.current();
        }
        return -1;
    }

    public void nextNoteFromTrack() {
        if (this.track != null) {
            this.track.next();
        }
    }

    private void releaseKey(final Key key) {
        this.handler.postDelayed(new Runnable() {
            public void run() {
                key.down = false;
                PianoView.this.handler.sendEmptyMessage(0);
            }
        }, 100);
    }
}
