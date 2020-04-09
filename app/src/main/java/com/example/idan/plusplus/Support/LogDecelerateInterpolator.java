package com.example.idan.plusplus.Support;

import android.animation.TimeInterpolator;
import androidx.annotation.RestrictTo;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

/**
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class LogDecelerateInterpolator implements TimeInterpolator {

    int mBase;
    int mDrift;
    final float mLogScale;

    public LogDecelerateInterpolator(int base, int drift) {
        mBase = base;
        mDrift = drift;

        mLogScale = 1f / computeLog(1, mBase, mDrift);
    }

    static float computeLog(float t, int base, int drift) {
        return (float) -Math.pow(base, -t) + 1 + (drift * t);
    }

    @Override
    public float getInterpolation(float t) {
        return computeLog(t, mBase, mDrift) * mLogScale;
    }
}

