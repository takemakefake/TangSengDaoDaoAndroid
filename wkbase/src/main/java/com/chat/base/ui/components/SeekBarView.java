/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.chat.base.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.StateSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.chat.app.R;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;


public class SeekBarView extends FrameLayout {

    private final SeekBarAccessibilityDelegate seekBarAccessibilityDelegate;

    private final Paint innerPaint1;
    private final Paint outerPaint1;
    private final int thumbSize;
    private final int selectorWidth;
    private int thumbX;
    private int thumbDX;
    private float progressToSet = -100;
    private boolean pressed;
    public SeekBarViewDelegate delegate;
    private boolean reportChanges;
    private float bufferedProgress;
    private Drawable hoverDrawable;
    private long lastUpdateTime;
    private float currentRadius;
    private final int[] pressedState = new int[]{android.R.attr.state_enabled, android.R.attr.state_pressed};
    private float transitionProgress = 1f;
    private int transitionThumbX;

    private boolean twoSided;

    public interface SeekBarViewDelegate {
        void onSeekBarDrag(boolean stop, float progress);

        void onSeekBarPressed(boolean pressed);

        default CharSequence getContentDescription() {
            return null;
        }

        default int getStepsCount() {
            return 0;
        }
    }


    public SeekBarView(Context context) {
        this(context, false);
    }

    public SeekBarView(Context context, boolean inPercents) {
        super(context);
        setWillNotDraw(false);
        innerPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);

        outerPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        outerPaint1.setColor(Theme.colorAccount);

        selectorWidth = AndroidUtilities.dp(32);
        thumbSize = AndroidUtilities.dp(24);
        currentRadius = AndroidUtilities.dp(6);

        if (Build.VERSION.SDK_INT >= 21) {
            hoverDrawable = Theme.createSelectorDrawable(ColorUtils.setAlphaComponent(0xff54AAEB, 40), 1, AndroidUtilities.dp(16));
            hoverDrawable.setCallback(this);
            hoverDrawable.setVisible(true, false);
        }

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);
        setAccessibilityDelegate(seekBarAccessibilityDelegate = new FloatSeekBarAccessibilityDelegate(inPercents) {
            @Override
            public float getProgress() {
                return SeekBarView.this.getProgress();
            }

            @Override
            public void setProgress(float progress) {
                pressed = true;
                SeekBarView.this.setProgress(progress);
                if (delegate != null) {
                    delegate.onSeekBarDrag(true, progress);
                }
                pressed = false;
            }

            @Override
            protected float getDelta() {
                final int stepsCount = delegate.getStepsCount();
                if (stepsCount > 0) {
                    return 1f / stepsCount;
                } else {
                    return super.getDelta();
                }
            }

            @Override
            public CharSequence getContentDescription(View host) {
                return delegate != null ? delegate.getContentDescription() : null;
            }
        });
    }

    public void setColors(int inner, int outer) {
        innerPaint1.setColor(inner);
        outerPaint1.setColor(outer);
        if (hoverDrawable != null) {
            Theme.setSelectorDrawableColor(hoverDrawable, ColorUtils.setAlphaComponent(outer, 40), true);
        }
    }

    public void setTwoSided(boolean value) {
        twoSided = value;
    }

    public boolean isTwoSided() {
        return twoSided;
    }

    public void setInnerColor(int color) {
        innerPaint1.setColor(color);
    }

    public void setOuterColor(int color) {
        outerPaint1.setColor(color);
        if (hoverDrawable != null) {
            Theme.setSelectorDrawableColor(hoverDrawable, ColorUtils.setAlphaComponent(color, 40), true);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return onTouch(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return onTouch(event);
    }

    public void setReportChanges(boolean value) {
        reportChanges = value;
    }

    public void setDelegate(SeekBarViewDelegate seekBarViewDelegate) {
        delegate = seekBarViewDelegate;
    }

    boolean captured;
    float sx, sy;

    boolean onTouch(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            sx = ev.getX();
            sy = ev.getY();
            return true;
        } else if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            captured = false;
            if (ev.getAction() == MotionEvent.ACTION_UP) {
                final ViewConfiguration vc = ViewConfiguration.get(getContext());
                if (Math.abs(ev.getY() - sy) < vc.getScaledTouchSlop()) {
                    int additionWidth = (getMeasuredHeight() - thumbSize) / 2;
                    if (!(thumbX - additionWidth <= ev.getX() && ev.getX() <= thumbX + thumbSize + additionWidth)) {
                        thumbX = (int) ev.getX() - thumbSize / 2;
                        if (thumbX < 0) {
                            thumbX = 0;
                        } else if (thumbX > getMeasuredWidth() - selectorWidth) {
                            thumbX = getMeasuredWidth() - selectorWidth;
                        }
                    }
                    thumbDX = (int) (ev.getX() - thumbX);
                    pressed = true;
                }
            }
            if (pressed) {
                if (ev.getAction() == MotionEvent.ACTION_UP) {
                    if (twoSided) {
                        float w = (getMeasuredWidth() - selectorWidth) / 2;
                        if (thumbX >= w) {
                            delegate.onSeekBarDrag(false, (thumbX - w) / w);
                        } else {
                            delegate.onSeekBarDrag(false, -Math.max(0.01f, 1.0f - (w - thumbX) / w));
                        }
                    } else {
                        delegate.onSeekBarDrag(true, (float) thumbX / (float) (getMeasuredWidth() - selectorWidth));
                    }
                }
                if (Build.VERSION.SDK_INT >= 21 && hoverDrawable != null) {
                    hoverDrawable.setState(StateSet.NOTHING);
                }
                delegate.onSeekBarPressed(false);
                pressed = false;
                invalidate();
                return true;
            }
        } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {
            if (!captured) {
                final ViewConfiguration vc = ViewConfiguration.get(getContext());
                if (Math.abs(ev.getY() - sy) > vc.getScaledTouchSlop()) {
                    return false;
                }
                if (Math.abs(ev.getX() - sx) > vc.getScaledTouchSlop()) {
                    captured = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                    int additionWidth = (getMeasuredHeight() - thumbSize) / 2;
                    if (ev.getY() >= 0 && ev.getY() <= getMeasuredHeight()) {
                        if (!(thumbX - additionWidth <= ev.getX() && ev.getX() <= thumbX + thumbSize + additionWidth)) {
                            thumbX = (int) ev.getX() - thumbSize / 2;
                            if (thumbX < 0) {
                                thumbX = 0;
                            } else if (thumbX > getMeasuredWidth() - selectorWidth) {
                                thumbX = getMeasuredWidth() - selectorWidth;
                            }
                        }
                        thumbDX = (int) (ev.getX() - thumbX);
                        pressed = true;
                        delegate.onSeekBarPressed(true);
                        if (Build.VERSION.SDK_INT >= 21 && hoverDrawable != null) {
                            hoverDrawable.setState(pressedState);
                            hoverDrawable.setHotspot(ev.getX(), ev.getY());
                        }
                        invalidate();
                        return true;
                    }
                }
            } else {
                if (pressed) {
                    thumbX = (int) (ev.getX() - thumbDX);
                    if (thumbX < 0) {
                        thumbX = 0;
                    } else if (thumbX > getMeasuredWidth() - selectorWidth) {
                        thumbX = getMeasuredWidth() - selectorWidth;
                    }
                    if (reportChanges) {
                        if (twoSided) {
                            float w = (getMeasuredWidth() - selectorWidth) / 2;
                            if (thumbX >= w) {
                                delegate.onSeekBarDrag(false, (thumbX - w) / w);
                            } else {
                                delegate.onSeekBarDrag(false, -Math.max(0.01f, 1.0f - (w - thumbX) / w));
                            }
                        } else {
                            delegate.onSeekBarDrag(false, (float) thumbX / (float) (getMeasuredWidth() - selectorWidth));
                        }
                    }
                    if (Build.VERSION.SDK_INT >= 21 && hoverDrawable != null) {
                        hoverDrawable.setHotspot(ev.getX(), ev.getY());
                    }
                    invalidate();
                    return true;
                }
            }
        }
        return false;
    }

    public float getProgress() {
        if (getMeasuredWidth() == 0) {
            return progressToSet;
        }
        return thumbX / (float) (getMeasuredWidth() - selectorWidth);
    }

    public void setProgress(float progress) {
        setProgress(progress, false);
    }

    public void setProgress(float progress, boolean animated) {
        if (getMeasuredWidth() == 0) {
            progressToSet = progress;
            return;
        }
        progressToSet = -100;
        int newThumbX;
        if (twoSided) {
            int w = getMeasuredWidth() - selectorWidth;
            float cx = w / 2;
            if (progress < 0) {
                newThumbX = (int) Math.ceil(cx + w / 2 * -(1.0f + progress));
            } else {
                newThumbX = (int) Math.ceil(cx + w / 2 * progress);
            }
        } else {
            newThumbX = (int) Math.ceil((getMeasuredWidth() - selectorWidth) * progress);
        }
        if (thumbX != newThumbX) {
            if (animated) {
                transitionThumbX = thumbX;
                transitionProgress = 0f;
            }
            thumbX = newThumbX;
            if (thumbX < 0) {
                thumbX = 0;
            } else if (thumbX > getMeasuredWidth() - selectorWidth) {
                thumbX = getMeasuredWidth() - selectorWidth;
            }
            invalidate();
        }
    }

    public void setBufferedProgress(float progress) {
        bufferedProgress = progress;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (progressToSet != -100 && getMeasuredWidth() > 0) {
            setProgress(progressToSet);
            progressToSet = -100;
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || who == hoverDrawable;
    }

    public boolean isDragging() {
        return pressed;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int y = (getMeasuredHeight() - thumbSize) / 2;
        innerPaint1.setColor(ContextCompat.getColor(getContext(), R.color.color999));
        canvas.drawRect(selectorWidth / 2, getMeasuredHeight() / 2 - AndroidUtilities.dp(1), getMeasuredWidth() - selectorWidth / 2, getMeasuredHeight() / 2 + AndroidUtilities.dp(1), innerPaint1);
        if (bufferedProgress > 0) {
            innerPaint1.setColor(ContextCompat.getColor(getContext(), R.color.black));
            canvas.drawRect(selectorWidth / 2, getMeasuredHeight() / 2 - AndroidUtilities.dp(1), selectorWidth / 2 + bufferedProgress * (getMeasuredWidth() - selectorWidth), getMeasuredHeight() / 2 + AndroidUtilities.dp(1), innerPaint1);
        }
        if (twoSided) {
            canvas.drawRect(getMeasuredWidth() / 2 - AndroidUtilities.dp(1), getMeasuredHeight() / 2 - AndroidUtilities.dp(6), getMeasuredWidth() / 2 + AndroidUtilities.dp(1), getMeasuredHeight() / 2 + AndroidUtilities.dp(6), outerPaint1);
            if (thumbX > (getMeasuredWidth() - selectorWidth) / 2) {
                canvas.drawRect(getMeasuredWidth() / 2, getMeasuredHeight() / 2 - AndroidUtilities.dp(1), selectorWidth / 2 + thumbX, getMeasuredHeight() / 2 + AndroidUtilities.dp(1), outerPaint1);
            } else {
                canvas.drawRect(thumbX + selectorWidth / 2, getMeasuredHeight() / 2 - AndroidUtilities.dp(1), getMeasuredWidth() / 2, getMeasuredHeight() / 2 + AndroidUtilities.dp(1), outerPaint1);
            }
        } else {
            canvas.drawRect(selectorWidth / 2, getMeasuredHeight() / 2 - AndroidUtilities.dp(1), selectorWidth / 2 + thumbX, getMeasuredHeight() / 2 + AndroidUtilities.dp(1), outerPaint1);
        }
        if (hoverDrawable != null) {
            int dx = thumbX + selectorWidth / 2 - AndroidUtilities.dp(16);
            int dy = y + thumbSize / 2 - AndroidUtilities.dp(16);
            hoverDrawable.setBounds(dx, dy, dx + AndroidUtilities.dp(32), dy + AndroidUtilities.dp(32));
            hoverDrawable.draw(canvas);
        }
        boolean needInvalidate = false;
        int newRad = AndroidUtilities.dp(pressed ? 8 : 6);
        long newUpdateTime = SystemClock.elapsedRealtime();
        long dt = newUpdateTime - lastUpdateTime;
        if (dt > 18) {
            dt = 16;
        }
        if (currentRadius != newRad) {
            if (currentRadius < newRad) {
                currentRadius += AndroidUtilities.dp(1) * (dt / 60.0f);
                if (currentRadius > newRad) {
                    currentRadius = newRad;
                }
            } else {
                currentRadius -= AndroidUtilities.dp(1) * (dt / 60.0f);
                if (currentRadius < newRad) {
                    currentRadius = newRad;
                }
            }
            needInvalidate = true;
        }
        if (transitionProgress < 1f) {
            transitionProgress += dt / 225f;
            if (transitionProgress < 1f) {
                needInvalidate = true;
            } else {
                transitionProgress = 1f;
            }
        }
        if (transitionProgress < 1f) {
            final float oldCircleProgress = 1f - Easings.easeInQuad.getInterpolation(Math.min(1f, transitionProgress * 3f));
            final float newCircleProgress = Easings.easeOutQuad.getInterpolation(transitionProgress);
            if (oldCircleProgress > 0f) {
                canvas.drawCircle(transitionThumbX + selectorWidth / 2, y + thumbSize / 2, currentRadius * oldCircleProgress, outerPaint1);
            }
            canvas.drawCircle(thumbX + selectorWidth / 2, y + thumbSize / 2, currentRadius * newCircleProgress, outerPaint1);
        } else {
            canvas.drawCircle(thumbX + selectorWidth / 2, y + thumbSize / 2, currentRadius, outerPaint1);
        }
        if (needInvalidate) {
            postInvalidateOnAnimation();
        }
    }

    public SeekBarAccessibilityDelegate getSeekBarAccessibilityDelegate() {
        return seekBarAccessibilityDelegate;
    }

}
