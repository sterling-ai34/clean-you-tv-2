package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

/**
 * now_playing: four small bars that bob while the song plays and rest low while it is paused. Redrawn about
 * twelve times a second, and only while playing and on screen, so a 1 GB TV barely notices it.
 */
public class EqualizerView extends View {
    private static final int BARS = 4;
    private static final long FRAME_MS = 80;
    /** Each bar's own speed and phase, so they never move in step. */
    private static final float[] SPEED = {0.0071f, 0.0093f, 0.0057f, 0.0082f};
    private static final float[] PHASE = {0f, 1.7f, 3.1f, 4.4f};

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bar = new RectF();
    private boolean playing;
    /** reduced_motion: the bars stand still at fixed heights. */
    private boolean still;
    private final Runnable tick = this::invalidate;

    public EqualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xFF1ED760);
    }

    void setStill(boolean still) {
        this.still = still;
    }

    void setPlaying(boolean playing) {
        if (this.playing == playing) return;
        this.playing = playing;
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(tick);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;
        float gap = w / (BARS * 3f - 1f);
        float width = gap * 2f;
        float radius = width / 2f;
        long now = SystemClock.uptimeMillis();
        paint.setAlpha(playing ? 255 : 140);
        for (int i = 0; i < BARS; i++) {
            float level;
            if (!playing) level = 0.22f;
            else if (still) level = 0.45f + 0.15f * (i % 2);
            else level = 0.3f + 0.7f * (0.5f + 0.5f * (float) Math.sin(now * SPEED[i] + PHASE[i]));
            float left = i * (width + gap);
            bar.set(left, h - h * level, left + width, h);
            canvas.drawRoundRect(bar, radius, radius, paint);
        }
        // One pending frame at a time, however often something else invalidates the view.
        removeCallbacks(tick);
        if (playing && !still && isShown()) postDelayed(tick, FRAME_MS);
    }
}
