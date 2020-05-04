package com.RunTimeTerror.Musicorum.model;

import android.graphics.RectF;

public class Key {
    public boolean down;
    public RectF rect;
    public boolean selected;
    public int sound;

    public Key(RectF rectF, int i) {
        this.rect = rectF;
        this.sound = i;
    }
}
