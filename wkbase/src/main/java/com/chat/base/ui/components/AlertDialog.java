/*
 * This is the source code of Telegram for Android v. 5.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2018.
 */

package com.chat.base.ui.components;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.chat.app.R;
import com.chat.base.ui.Theme;
import com.chat.base.utils.AndroidUtilities;
import com.chat.base.utils.LayoutHelper;

import org.telegram.ui.Components.RLottieDrawable;
import org.telegram.ui.Components.RLottieImageView;

import java.util.ArrayList;
import java.util.Map;

public class AlertDialog extends Dialog implements Drawable.Callback {

    public static final int ALERT_TYPE_MESSAGE = 0;
    public static final int ALERT_TYPE_LOADING = 2;
    public static final int ALERT_TYPE_SPINNER = 3;

    private View customView;
    private int customViewHeight = LayoutHelper.WRAP_CONTENT;
    private TextView titleTextView;
    private TextView secondTitleTextView;
    private TextView subtitleTextView;
    private TextView messageTextView;
    private FrameLayout progressViewContainer;
    private FrameLayout titleContainer;
    private TextView progressViewTextView;
    private ScrollView contentScrollView;
    private LinearLayout scrollContainer;
    private ViewTreeObserver.OnScrollChangedListener onScrollChangedListener;
    private BitmapDrawable[] shadow = new BitmapDrawable[2];
    private boolean[] shadowVisibility = new boolean[2];
    private AnimatorSet[] shadowAnimation = new AnimatorSet[2];
    private int customViewOffset = 12;

    private OnCancelListener onCancelListener;

    private AlertDialog cancelDialog;

    private int lastScreenWidth;

    private OnClickListener onClickListener;
    private OnDismissListener onDismissListener;

    private CharSequence[] items;
    private int[] itemIcons;
    private CharSequence title;
    private CharSequence secondTitle;
    private CharSequence subtitle;
    private CharSequence message;
    private int topResId;
    private View topView;
    private boolean topAnimationIsNew;
    private int topAnimationId;
    private int topAnimationSize;
    private Map<String, Integer> topAnimationLayerColors;
    private int topHeight = 132;
    private Drawable topDrawable;
    private int topBackgroundColor;
    private int progressViewStyle;
    private int currentProgress;

    private boolean messageTextViewClickable = true;

    private boolean canCacnel = true;

    private boolean dismissDialogByButtons = true;
    private boolean drawBackground;
    private boolean notDrawBackgroundOnTopView;
    private RLottieImageView topImageView;
    private CharSequence positiveButtonText;
    private OnClickListener positiveButtonListener;
    private CharSequence negativeButtonText;
    private OnClickListener negativeButtonListener;
    private CharSequence neutralButtonText;
    private OnClickListener neutralButtonListener;
    protected ViewGroup buttonsLayout;
    private LineProgressView lineProgressView;
    private TextView lineProgressViewPercent;
    private OnClickListener onBackButtonListener;
    private int[] containerViewLocation = new int[2];

    private boolean checkFocusable = true;

    private Drawable shadowDrawable;
    private Rect backgroundPaddings;

    private float blurOpacity;
    private Bitmap blurBitmap;
    private Matrix blurMatrix;
    private BitmapShader blurShader;
    private Paint blurPaint;
    private Paint dimBlurPaint;

    private boolean focusable;

    private boolean verticalButtons;

    private Runnable dismissRunnable = this::dismiss;
    private Runnable showRunnable = () -> {
        if (isShowing()) {
            return;
        }
        try {
            show();
        } catch (Exception ignore) {

        }
    };

    private ArrayList<AlertDialogCell> itemViews = new ArrayList<>();
    private float aspectRatio;
    private boolean dimEnabled = true;
    private float dimAlpha = 0.5f;
    private boolean dimCustom = false;
    private boolean topAnimationAutoRepeat = true;
    private boolean blurredBackground;
    private boolean blurredNativeBackground;
    private int backgroundColor;
    float blurAlpha = 0.8f;
    private boolean blurBehind;
    private int additioanalHorizontalPadding;

    public void setBlurParams(float blurAlpha, boolean blurBehind, boolean blurBackground) {
        this.blurAlpha = blurAlpha;
        this.blurBehind = blurBehind;
        this.blurredBackground = blurBackground;
    }

    private boolean supportsNativeBlur() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public void redPositive() {
        TextView button = (TextView) getButton(DialogInterface.BUTTON_POSITIVE);
        if (button != null) {
            button.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
        }
    }

    public static class AlertDialogCell extends FrameLayout {

        private TextView textView;
        private ImageView imageView;

        public AlertDialogCell(Context context) {
            super(context);

            setBackground(Theme.createSelectorDrawable(ContextCompat.getColor(getContext(), R.color.screen_bg), 2));
            setPadding(AndroidUtilities.dp(23), 0, AndroidUtilities.dp(23), 0);

            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER);
            imageView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(getContext(), R.color.dialogText), PorterDuff.Mode.MULTIPLY));
            addView(imageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 40, Gravity.CENTER_VERTICAL | (AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT)));

            textView = new TextView(context);
            textView.setLines(1);
            textView.setSingleLine(true);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(48), MeasureSpec.EXACTLY));
        }

        public void setTextColor(int color) {
            textView.setTextColor(color);
        }

        public void setGravity(int gravity) {
            textView.setGravity(gravity);
        }

        public void setTextAndIcon(CharSequence text, int icon) {
            textView.setText(text);
            if (icon != 0) {
                imageView.setImageResource(icon);
                imageView.setVisibility(VISIBLE);
                textView.setPadding(AndroidUtilities.isRTL ? 0 : AndroidUtilities.dp(56), 0, AndroidUtilities.isRTL ? AndroidUtilities.dp(56) : 0, 0);
            } else {
                imageView.setVisibility(INVISIBLE);
                textView.setPadding(0, 0, 0, 0);
            }
        }

    }


    public AlertDialog(Context context, int progressStyle) {
        super(context, R.style.TransparentDialog);

        blurredNativeBackground = supportsNativeBlur() && progressViewStyle == ALERT_TYPE_MESSAGE;
        backgroundColor = ContextCompat.getColor(context, R.color.screen_bg);
        final boolean isDark = Theme.isDark();
        blurredBackground = blurredNativeBackground || !supportsNativeBlur() && isDark;

        backgroundPaddings = new Rect();
        if (progressStyle != ALERT_TYPE_SPINNER || blurredBackground) {
            shadowDrawable = ContextCompat.getDrawable(context, R.mipmap.popup_fixed_alert3);
            blurOpacity = progressStyle == ALERT_TYPE_SPINNER ? 0.55f : (isDark ? 0.80f : 0.985f);
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY));
            shadowDrawable.getPadding(backgroundPaddings);
        }

        progressViewStyle = progressStyle;
    }

    @Override
    public void show() {
        super.show();
        if (progressViewContainer != null && progressViewStyle == ALERT_TYPE_SPINNER) {
            progressViewContainer.setScaleX(0);
            progressViewContainer.setScaleY(0);
            progressViewContainer.animate()
                    .scaleX(1f).scaleY(1f)
                    .setInterpolator(new OvershootInterpolator(1.3f))
                    .setDuration(190)
                    .start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout containerView = new LinearLayout(getContext()) {

            private boolean inLayout;

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (progressViewStyle == ALERT_TYPE_SPINNER) {
                    showCancelAlert();
                    return false;
                }
                return super.onTouchEvent(event);
            }

            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (progressViewStyle == ALERT_TYPE_SPINNER) {
                    showCancelAlert();
                    return false;
                }
                return super.onInterceptTouchEvent(ev);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                if (progressViewStyle == ALERT_TYPE_SPINNER) {
                    progressViewContainer.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(86), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(86), MeasureSpec.EXACTLY));
                    setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
                } else {
                    inLayout = true;
                    int width = MeasureSpec.getSize(widthMeasureSpec);
                    int height = MeasureSpec.getSize(heightMeasureSpec);
                    int maxContentHeight;
                    int availableHeight = maxContentHeight = height - getPaddingTop() - getPaddingBottom();
                    int availableWidth = width - getPaddingLeft() - getPaddingRight();

                    int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth - AndroidUtilities.dp(48), MeasureSpec.EXACTLY);
                    int childFullWidthMeasureSpec = MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY);
                    LayoutParams layoutParams;

                    if (buttonsLayout != null) {
                        int count = buttonsLayout.getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = buttonsLayout.getChildAt(a);
                            if (child instanceof TextView) {
                                TextView button = (TextView) child;
                                button.setMaxWidth(AndroidUtilities.dp((availableWidth - AndroidUtilities.dp(24)) / 2));
                            }
                        }
                        buttonsLayout.measure(childFullWidthMeasureSpec, heightMeasureSpec);
                        layoutParams = (LayoutParams) buttonsLayout.getLayoutParams();
                        availableHeight -= buttonsLayout.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                    }
                    if (secondTitleTextView != null) {
                        secondTitleTextView.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(childWidthMeasureSpec), MeasureSpec.AT_MOST), heightMeasureSpec);
                    }
                    if (titleTextView != null) {
                        if (secondTitleTextView != null) {
                            titleTextView.measure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(childWidthMeasureSpec) - secondTitleTextView.getMeasuredWidth() - AndroidUtilities.dp(8), MeasureSpec.EXACTLY), heightMeasureSpec);
                        } else {
                            titleTextView.measure(childWidthMeasureSpec, heightMeasureSpec);
                        }
                    }
                    if (titleContainer != null) {
                        titleContainer.measure(childWidthMeasureSpec, heightMeasureSpec);
                        layoutParams = (LayoutParams) titleContainer.getLayoutParams();
                        availableHeight -= titleContainer.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                    }
                    if (subtitleTextView != null) {
                        subtitleTextView.measure(childWidthMeasureSpec, heightMeasureSpec);
                        layoutParams = (LayoutParams) subtitleTextView.getLayoutParams();
                        availableHeight -= subtitleTextView.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                    }
                    if (topImageView != null) {
                        topImageView.measure(MeasureSpec.makeMeasureSpec(availableWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(topHeight), MeasureSpec.EXACTLY));
                        availableHeight -= topImageView.getMeasuredHeight();
                    }
                    if (topView != null) {
                        int w = width;
                        int h;
                        if (aspectRatio == 0) {
                            float scale = w / 936.0f;
                            h = (int) (354 * scale);
                        } else {
                            h = (int) (w * aspectRatio);
                        }
                        topView.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
                        topView.getLayoutParams().height = h;
                        availableHeight -= topView.getMeasuredHeight();
                    }
                    if (progressViewStyle == ALERT_TYPE_MESSAGE) {
                        layoutParams = (LayoutParams) contentScrollView.getLayoutParams();

                        if (customView != null) {
                            layoutParams.topMargin = titleTextView == null && messageTextView.getVisibility() == GONE && items == null ? AndroidUtilities.dp(16) : 0;
                            layoutParams.bottomMargin = buttonsLayout == null ? AndroidUtilities.dp(8) : 0;
                        } else if (items != null) {
                            layoutParams.topMargin = titleTextView == null && messageTextView.getVisibility() == GONE ? AndroidUtilities.dp(8) : 0;
                            layoutParams.bottomMargin = AndroidUtilities.dp(8);
                        } else if (messageTextView.getVisibility() == VISIBLE) {
                            layoutParams.topMargin = titleTextView == null ? AndroidUtilities.dp(19) : 0;
                            layoutParams.bottomMargin = AndroidUtilities.dp(20);
                        }

                        availableHeight -= layoutParams.bottomMargin + layoutParams.topMargin;
                        contentScrollView.measure(childFullWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST));
                        availableHeight -= contentScrollView.getMeasuredHeight();
                    } else {
                        if (progressViewContainer != null) {
                            progressViewContainer.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST));
                            layoutParams = (LayoutParams) progressViewContainer.getLayoutParams();
                            availableHeight -= progressViewContainer.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                        } else if (messageTextView != null) {
                            messageTextView.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST));
                            if (messageTextView.getVisibility() != GONE) {
                                layoutParams = (LayoutParams) messageTextView.getLayoutParams();
                                availableHeight -= messageTextView.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                            }
                        }
                        if (lineProgressView != null) {
                            lineProgressView.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(4), MeasureSpec.EXACTLY));
                            layoutParams = (LayoutParams) lineProgressView.getLayoutParams();
                            availableHeight -= lineProgressView.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;

                            lineProgressViewPercent.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(availableHeight, MeasureSpec.AT_MOST));
                            layoutParams = (LayoutParams) lineProgressViewPercent.getLayoutParams();
                            availableHeight -= lineProgressViewPercent.getMeasuredHeight() + layoutParams.bottomMargin + layoutParams.topMargin;
                        }
                    }

                    setMeasuredDimension(width, maxContentHeight - availableHeight + getPaddingTop() + getPaddingBottom() - (topAnimationIsNew ? AndroidUtilities.dp(8) : 0));
                    inLayout = false;

                    if (lastScreenWidth != AndroidUtilities.getScreenWidth()) {
                        AndroidUtilities.runOnUIThread(() -> {
                            lastScreenWidth = AndroidUtilities.getScreenWidth();
                            final int calculatedWidth = AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(56);
                            int maxWidth;
                            if (AndroidUtilities.isTablet()) {
                                if (AndroidUtilities.isSmallTablet()) {
                                    maxWidth = AndroidUtilities.dp(446);
                                } else {
                                    maxWidth = AndroidUtilities.dp(496);
                                }
                            } else {
                                maxWidth = AndroidUtilities.dp(356);
                            }

                            Window window = getWindow();
                            WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                            params.copyFrom(window.getAttributes());

                            params.width = Math.min(maxWidth, calculatedWidth) + backgroundPaddings.left + backgroundPaddings.right;
                            try {
                                window.setAttributes(params);
                            } catch (Throwable e) {
                            }
                        });
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                super.onLayout(changed, l, t, r, b);
                if (progressViewStyle == ALERT_TYPE_SPINNER) {
                    int x = (r - l - progressViewContainer.getMeasuredWidth()) / 2;
                    int y = (b - t - progressViewContainer.getMeasuredHeight()) / 2;
                    progressViewContainer.layout(x, y, x + progressViewContainer.getMeasuredWidth(), y + progressViewContainer.getMeasuredHeight());
                } else if (contentScrollView != null) {
                    if (onScrollChangedListener == null) {
                        onScrollChangedListener = () -> {
                            runShadowAnimation(0, titleTextView != null && contentScrollView.getScrollY() > scrollContainer.getTop());
                            runShadowAnimation(1, buttonsLayout != null && contentScrollView.getScrollY() + contentScrollView.getHeight() < scrollContainer.getBottom());
                            contentScrollView.invalidate();
                        };
                        contentScrollView.getViewTreeObserver().addOnScrollChangedListener(onScrollChangedListener);
                    }
                    onScrollChangedListener.onScrollChanged();
                }

                getLocationOnScreen(containerViewLocation);
                if (blurMatrix != null && blurShader != null) {
                    blurMatrix.reset();
                    blurMatrix.postScale(8f, 8f);
                    blurMatrix.postTranslate(-containerViewLocation[0], -containerViewLocation[1]);
                    blurShader.setLocalMatrix(blurMatrix);
                }
            }

            @Override
            public void requestLayout() {
                if (inLayout) {
                    return;
                }
                super.requestLayout();
            }

            @Override
            public boolean hasOverlappingRendering() {
                return false;
            }

            private AnimatedFloat blurPaintAlpha = new AnimatedFloat(0, this);
            private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

            @Override
            public void draw(Canvas canvas) {
                if (blurredBackground && !blurredNativeBackground) {
                    float r;
                    if (progressViewStyle == ALERT_TYPE_SPINNER && progressViewContainer != null) {
                        r = AndroidUtilities.dp(18);
                        float w = progressViewContainer.getWidth() * progressViewContainer.getScaleX();
                        float h = progressViewContainer.getHeight() * progressViewContainer.getScaleY();
                        AndroidUtilities.rectTmp.set(
                                (getWidth() - w) / 2f,
                                (getHeight() - h) / 2f,
                                (getWidth() + w) / 2f,
                                (getHeight() + h) / 2f
                        );
                    } else {
                        r = AndroidUtilities.dp(10);
                        AndroidUtilities.rectTmp.set(getPaddingLeft(), getPaddingTop(), getMeasuredWidth() - getPaddingRight(), getMeasuredHeight() - getPaddingBottom());
                    }

                    // draw blur of background
                    float blurAlpha = blurPaintAlpha.set(blurPaint != null ? 1f : 0f);
                    if (blurPaint != null) {
                        blurPaint.setAlpha((int) (0xFF * blurAlpha));
                        canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, blurPaint);
                    }

                    // draw dim above blur
                    if (dimBlurPaint == null) {
                        dimBlurPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        dimBlurPaint.setColor(ColorUtils.setAlphaComponent(0xff000000, (int) (0xFF * dimAlpha)));
                    }
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, dimBlurPaint);

                    // draw background
                    backgroundPaint.setColor(backgroundColor);
                    backgroundPaint.setAlpha((int) (backgroundPaint.getAlpha() * (blurAlpha * (blurOpacity - 1f) + 1f)));
                    canvas.drawRoundRect(AndroidUtilities.rectTmp, r, r, backgroundPaint);
                }
                super.draw(canvas);
            }

            @Override
            protected void dispatchDraw(Canvas canvas) {
                if (drawBackground && !blurredBackground) {
                    shadowDrawable.setBounds(0, 0, getMeasuredWidth(), getMeasuredHeight());
                    if (topView != null && notDrawBackgroundOnTopView) {
                        int clipTop = topView.getBottom();
                        canvas.save();
                        canvas.clipRect(0, clipTop, getMeasuredWidth(), getMeasuredHeight());
                        shadowDrawable.draw(canvas);
                        canvas.restore();
                    } else {
                        shadowDrawable.draw(canvas);
                    }
                }
                super.dispatchDraw(canvas);
            }
        };
        containerView.setOrientation(LinearLayout.VERTICAL);
        if ((blurredBackground || progressViewStyle == ALERT_TYPE_SPINNER) && progressViewStyle != ALERT_TYPE_LOADING) {
            containerView.setBackground(null);
            containerView.setPadding(0, 0, 0, 0);
            if (blurredBackground && !blurredNativeBackground) {
                containerView.setWillNotDraw(false);
            }
            drawBackground = false;
        } else {
            if (notDrawBackgroundOnTopView) {
                Rect rect = new Rect();
                shadowDrawable.getPadding(rect);
                containerView.setPadding(rect.left, rect.top, rect.right, rect.bottom);
                drawBackground = true;
            } else {
                containerView.setBackground(null);
                containerView.setPadding(0, 0, 0, 0);
                containerView.setBackground(shadowDrawable);
                drawBackground = false;
            }
        }
        containerView.setFitsSystemWindows(Build.VERSION.SDK_INT >= 21);
        setContentView(containerView);

        final boolean hasButtons = positiveButtonText != null || negativeButtonText != null || neutralButtonText != null;

        if (topResId != 0 || topAnimationId != 0 || topDrawable != null) {
            topImageView = new RLottieImageView(getContext());
            if (topDrawable != null) {
                topImageView.setImageDrawable(topDrawable);
            } else if (topResId != 0) {
                topImageView.setImageResource(topResId);
            } else {
                topImageView.setAutoRepeat(topAnimationAutoRepeat);
                topImageView.setAnimation(topAnimationId, topAnimationSize, topAnimationSize);
                if (topAnimationLayerColors != null) {
                    RLottieDrawable drawable = topImageView.getAnimatedDrawable();
                    for (Map.Entry<String, Integer> en : topAnimationLayerColors.entrySet()) {
                        drawable.setLayerColor(en.getKey(), en.getValue());
                    }
                }
                topImageView.playAnimation();
            }
            topImageView.setScaleType(ImageView.ScaleType.CENTER);
            if (topAnimationIsNew) {
                GradientDrawable d = new GradientDrawable();
                d.setColor(topBackgroundColor);
                d.setCornerRadius(AndroidUtilities.dp(128));
                topImageView.setBackground(new Drawable() {
                    int size = topAnimationSize + AndroidUtilities.dp(52);

                    @Override
                    public void draw(@NonNull Canvas canvas) {
                        d.setBounds((int) ((topImageView.getWidth() - size) / 2f), (int) ((topImageView.getHeight() - size) / 2f), (int) ((topImageView.getWidth() + size) / 2f), (int) ((topImageView.getHeight() + size) / 2f));
                        d.draw(canvas);
                    }

                    @Override
                    public void setAlpha(int alpha) {
                        d.setAlpha(alpha);
                    }

                    @Override
                    public void setColorFilter(@Nullable ColorFilter colorFilter) {
                        d.setColorFilter(colorFilter);
                    }

                    @Override
                    public int getOpacity() {
                        return d.getOpacity();
                    }
                });
                topHeight = 92;
            } else {
                topImageView.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(10), 0, topBackgroundColor));
            }
            if (topAnimationIsNew) {
                topImageView.setTranslationY(AndroidUtilities.dp(16));
            } else {
                topImageView.setTranslationY(0);
            }
            topImageView.setPadding(0, 0, 0, 0);
            containerView.addView(topImageView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, topHeight, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
        } else if (topView != null) {
            topView.setPadding(0, 0, 0, 0);
            containerView.addView(topView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, topHeight, Gravity.LEFT | Gravity.TOP, 0, 0, 0, 0));
        }

        if (title != null) {
            titleContainer = new FrameLayout(getContext());
            containerView.addView(titleContainer, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, topAnimationIsNew ? Gravity.CENTER_HORIZONTAL : 0, 24, 0, 24, 0));

            titleTextView = new TextView(getContext());
            titleTextView.setText(title);
            titleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorDark));
            titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            titleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            titleTextView.setGravity((topAnimationIsNew ? Gravity.CENTER_HORIZONTAL : AndroidUtilities.isRTL ? Gravity.END : Gravity.START) | Gravity.TOP);
            titleContainer.addView(titleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (topAnimationIsNew ? Gravity.CENTER_HORIZONTAL : AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 0, 19, 0, topAnimationIsNew ? 4 : (subtitle != null ? 2 : (items != null ? 14 : 10))));
        }

        if (secondTitle != null && title != null) {
            secondTitleTextView = new TextView(getContext());
            secondTitleTextView.setText(secondTitle);
            secondTitleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
            secondTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            secondTitleTextView.setGravity((AndroidUtilities.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP);
            titleContainer.addView(secondTitleTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (AndroidUtilities.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 0, 21, 0, 0));
        }

        if (subtitle != null) {
            subtitleTextView = new TextView(getContext());
            subtitleTextView.setText(subtitle);
            subtitleTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
            subtitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            subtitleTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            subtitleTextView.setGravity((AndroidUtilities.isRTL ? Gravity.END : Gravity.START) | Gravity.TOP);
            containerView.addView(subtitleTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 24, 0, 24, items != null ? 14 : 10));
        }

        if (progressViewStyle == ALERT_TYPE_MESSAGE) {
            shadow[0] = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.mipmap.header_shadow).mutate();
            shadow[1] = (BitmapDrawable) ContextCompat.getDrawable(getContext(), R.mipmap.header_shadow_reverse).mutate();
            shadow[0].setAlpha(0);
            shadow[1].setAlpha(0);
            shadow[0].setCallback(this);
            shadow[1].setCallback(this);

            contentScrollView = new ScrollView(getContext()) {
                @Override
                protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
                    boolean result = super.drawChild(canvas, child, drawingTime);
                    if (shadow[0].getPaint().getAlpha() != 0) {
                        shadow[0].setBounds(0, getScrollY(), getMeasuredWidth(), getScrollY() + AndroidUtilities.dp(3));
                        shadow[0].draw(canvas);
                    }
                    if (shadow[1].getPaint().getAlpha() != 0) {
                        shadow[1].setBounds(0, getScrollY() + getMeasuredHeight() - AndroidUtilities.dp(3), getMeasuredWidth(), getScrollY() + getMeasuredHeight());
                        shadow[1].draw(canvas);
                    }
                    return result;
                }
            };
            contentScrollView.setVerticalScrollBarEnabled(false);
            AndroidUtilities.setScrollViewEdgeEffectColor(contentScrollView, ContextCompat.getColor(getContext(), R.color.color999));
            containerView.addView(contentScrollView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 0, 0));

            scrollContainer = new LinearLayout(getContext());
            scrollContainer.setOrientation(LinearLayout.VERTICAL);
            contentScrollView.addView(scrollContainer, new ScrollView.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        }

        messageTextView = new TextView(getContext());
        messageTextView.setTextColor(ContextCompat.getColor(getContext(),R.color.dialogText));
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        messageTextView.setMovementMethod(new AndroidUtilities.LinkMovementMethodMy());
        messageTextView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        messageTextView.setLinkTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        if (!messageTextViewClickable) {
            messageTextView.setClickable(false);
            messageTextView.setEnabled(false);
        }
        messageTextView.setGravity((topAnimationIsNew ? Gravity.CENTER_HORIZONTAL : AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
        if (progressViewStyle == ALERT_TYPE_LOADING) {
            containerView.addView(messageTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 24, title == null ? 19 : 0, 24, 20));

            lineProgressView = new LineProgressView(getContext());
            lineProgressView.setProgress(currentProgress / 100.0f, false);
            lineProgressView.setProgressColor(Theme.colorAccount);
            lineProgressView.setBackColor(ContextCompat.getColor(getContext(), R.color.screen_bg));
            containerView.addView(lineProgressView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 4, Gravity.LEFT | Gravity.CENTER_VERTICAL, 24, 0, 24, 0));

            lineProgressViewPercent = new TextView(getContext());
            lineProgressViewPercent.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            lineProgressViewPercent.setGravity((AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP);
            lineProgressViewPercent.setTextColor(ContextCompat.getColor(getContext(), R.color.color999));
            lineProgressViewPercent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            containerView.addView(lineProgressViewPercent, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 23, 4, 23, 24));
            updateLineProgressTextView();
        } else if (progressViewStyle == ALERT_TYPE_SPINNER) {
            setCanceledOnTouchOutside(false);
            setCancelable(false);

            progressViewContainer = new FrameLayout(getContext());
            backgroundColor = ContextCompat.getColor(getContext(), R.color.screen_bg);
            if (!(blurredBackground && !blurredNativeBackground)) {
                progressViewContainer.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), backgroundColor));
            }
            containerView.addView(progressViewContainer, LayoutHelper.createLinear(86, 86, Gravity.CENTER));

            RadialProgressView progressView = new RadialProgressView(getContext());
            progressView.setSize(AndroidUtilities.dp(32));
            progressView.setProgressColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
            progressViewContainer.addView(progressView, LayoutHelper.createFrame(86, 86, Gravity.CENTER));
        } else {
            scrollContainer.addView(messageTextView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (topAnimationIsNew ? Gravity.CENTER_HORIZONTAL : AndroidUtilities.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, 24, 0, 24, customView != null || items != null ? customViewOffset : 0));
        }
        if (!TextUtils.isEmpty(message)) {
            messageTextView.setText(message);
            messageTextView.setVisibility(View.VISIBLE);
        } else {
            messageTextView.setVisibility(View.GONE);
        }

        if (items != null) {
            for (int a = 0; a < items.length; a++) {
                if (items[a] == null) {
                    continue;
                }
                AlertDialogCell cell = new AlertDialogCell(getContext());
                cell.setTextAndIcon(items[a], itemIcons != null ? itemIcons[a] : 0);
                cell.setTag(a);
                itemViews.add(cell);
                scrollContainer.addView(cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));
                cell.setOnClickListener(v -> {
                    if (onClickListener != null) {
                        onClickListener.onClick(AlertDialog.this, (Integer) v.getTag());
                    }
                    dismiss();
                });
            }
        }
        if (customView != null) {
            if (customView.getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) customView.getParent();
                viewGroup.removeView(customView);
            }
            scrollContainer.addView(customView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, customViewHeight));
        }
        if (hasButtons) {
            if (!verticalButtons) {
                int buttonsWidth = 0;
                TextPaint paint = new TextPaint();
                paint.setTextSize(AndroidUtilities.dp(14));
                if (positiveButtonText != null) {
                    buttonsWidth += paint.measureText(positiveButtonText, 0, positiveButtonText.length()) + AndroidUtilities.dp(10);
                }
                if (negativeButtonText != null) {
                    buttonsWidth += paint.measureText(negativeButtonText, 0, negativeButtonText.length()) + AndroidUtilities.dp(10);
                }
                if (neutralButtonText != null) {
                    buttonsWidth += paint.measureText(neutralButtonText, 0, neutralButtonText.length()) + AndroidUtilities.dp(10);
                }
                if (buttonsWidth > AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(110)) {
                    verticalButtons = true;
                }
            }
            if (verticalButtons) {
                LinearLayout linearLayout = new LinearLayout(getContext());
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                buttonsLayout = linearLayout;

            } else {
                buttonsLayout = new FrameLayout(getContext()) {
                    @Override
                    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                        int count = getChildCount();
                        View positiveButton = null;
                        int width = right - left;
                        for (int a = 0; a < count; a++) {
                            View child = getChildAt(a);
                            Integer tag = (Integer) child.getTag();
                            if (tag != null) {
                                if (tag == Dialog.BUTTON_POSITIVE) {
                                    positiveButton = child;
                                    if (AndroidUtilities.isRTL) {
                                        child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                                    } else {
                                        child.layout(width - getPaddingRight() - child.getMeasuredWidth(), getPaddingTop(), width - getPaddingRight(), getPaddingTop() + child.getMeasuredHeight());
                                    }
                                } else if (tag == Dialog.BUTTON_NEGATIVE) {
                                    if (AndroidUtilities.isRTL) {
                                        int x = getPaddingLeft();
                                        if (positiveButton != null) {
                                            x += positiveButton.getMeasuredWidth() + AndroidUtilities.dp(8);
                                        }
                                        child.layout(x, getPaddingTop(), x + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                                    } else {
                                        int x = width - getPaddingRight() - child.getMeasuredWidth();
                                        if (positiveButton != null) {
                                            x -= positiveButton.getMeasuredWidth() + AndroidUtilities.dp(8);
                                        }
                                        child.layout(x, getPaddingTop(), x + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                                    }
                                } else if (tag == Dialog.BUTTON_NEUTRAL) {
                                    if (AndroidUtilities.isRTL) {
                                        child.layout(width - getPaddingRight() - child.getMeasuredWidth(), getPaddingTop(), width - getPaddingRight(), getPaddingTop() + child.getMeasuredHeight());
                                    } else {
                                        child.layout(getPaddingLeft(), getPaddingTop(), getPaddingLeft() + child.getMeasuredWidth(), getPaddingTop() + child.getMeasuredHeight());
                                    }
                                }
                            } else {
                                int w = child.getMeasuredWidth();
                                int h = child.getMeasuredHeight();
                                int l;
                                int t;
                                if (positiveButton != null) {
                                    l = positiveButton.getLeft() + (positiveButton.getMeasuredWidth() - w) / 2;
                                    t = positiveButton.getTop() + (positiveButton.getMeasuredHeight() - h) / 2;
                                } else {
                                    l = t = 0;
                                }
                                child.layout(l, t, l + w, t + h);
                            }
                        }
                    }

                    @Override
                    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

                        int totalWidth = 0;
                        int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
                        int count = getChildCount();
                        for (int a = 0; a < count; a++) {
                            View child = getChildAt(a);
                            if (child instanceof TextView && child.getTag() != null) {
                                totalWidth += child.getMeasuredWidth();
                            }
                        }
                        if (totalWidth > availableWidth) {
                            View negative = findViewWithTag(BUTTON_NEGATIVE);
                            View neutral = findViewWithTag(BUTTON_NEUTRAL);
                            if (negative != null && neutral != null) {
                                if (negative.getMeasuredWidth() < neutral.getMeasuredWidth()) {
                                    neutral.measure(MeasureSpec.makeMeasureSpec(neutral.getMeasuredWidth() - (totalWidth - availableWidth), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(neutral.getMeasuredHeight(), MeasureSpec.EXACTLY));
                                } else {
                                    negative.measure(MeasureSpec.makeMeasureSpec(negative.getMeasuredWidth() - (totalWidth - availableWidth), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(negative.getMeasuredHeight(), MeasureSpec.EXACTLY));
                                }
                            }
                        }
                    }
                };
            }
            buttonsLayout.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            containerView.addView(buttonsLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 52));
            if (topAnimationIsNew) {
                buttonsLayout.setTranslationY(-AndroidUtilities.dp(8));
            }

            if (positiveButtonText != null) {
                TextView textView = new AppCompatTextView(getContext()) {
                    @Override
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), color));
                    }
                };
                textView.setMinWidth(AndroidUtilities.dp(64));
                textView.setTag(Dialog.BUTTON_POSITIVE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
                textView.setGravity(Gravity.CENTER);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
//                textView.setLines(1);
//                textView.setSingleLine(true); //TODO
                textView.setText(positiveButtonText.toString());
                textView.setBackground(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), ContextCompat.getColor(getContext(), R.color.dialogText)));
                textView.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
                if (verticalButtons) {
                    buttonsLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, AndroidUtilities.isRTL ? Gravity.START : Gravity.END));
                } else {
                    buttonsLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 36, Gravity.TOP | Gravity.END));
                }
                textView.setOnClickListener(v -> {
                    if (positiveButtonListener != null) {
                        positiveButtonListener.onClick(AlertDialog.this, Dialog.BUTTON_POSITIVE);
                    }
                    if (dismissDialogByButtons) {
                        dismiss();
                    }
                });
            }

            if (negativeButtonText != null) {
                TextView textView = new AppCompatTextView(getContext()) {
                    @Override
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        setBackground(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), color));
                    }
                };
                textView.setMinWidth(AndroidUtilities.dp(64));
                textView.setTag(Dialog.BUTTON_NEGATIVE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
                textView.setGravity(Gravity.CENTER);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setSingleLine(true);
                textView.setText(negativeButtonText.toString());
                textView.setBackground(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), ContextCompat.getColor(getContext(), R.color.dialogText)));
                textView.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
                if (verticalButtons) {
                    buttonsLayout.addView(textView, 0, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, AndroidUtilities.isRTL ? Gravity.LEFT : Gravity.RIGHT));
                } else {
                    buttonsLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 36, Gravity.TOP | Gravity.RIGHT));
                }
                textView.setOnClickListener(v -> {
                    if (negativeButtonListener != null) {
                        negativeButtonListener.onClick(AlertDialog.this, Dialog.BUTTON_NEGATIVE);
                    }
                    if (dismissDialogByButtons) {
                        cancel();
                    }
                });
            }

            if (neutralButtonText != null) {
                TextView textView = new AppCompatTextView(getContext()) {
                    @Override
                    public void setEnabled(boolean enabled) {
                        super.setEnabled(enabled);
                        setAlpha(enabled ? 1.0f : 0.5f);
                    }

                    @Override
                    public void setTextColor(int color) {
                        super.setTextColor(color);
                        setBackgroundDrawable(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), color));
                    }
                };
                textView.setMinWidth(AndroidUtilities.dp(64));
                textView.setTag(Dialog.BUTTON_NEUTRAL);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                textView.setTextColor(ContextCompat.getColor(getContext(), R.color.dialogText));
                textView.setGravity(Gravity.CENTER);
                textView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
                textView.setEllipsize(TextUtils.TruncateAt.END);
                textView.setSingleLine(true);
                textView.setText(neutralButtonText.toString());
                textView.setBackground(Theme.getRoundRectSelectorDrawable(AndroidUtilities.dp(6), ContextCompat.getColor(getContext(), R.color.dialogText)));
                textView.setPadding(AndroidUtilities.dp(12), 0, AndroidUtilities.dp(12), 0);
                if (verticalButtons) {
                    buttonsLayout.addView(textView, 1, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, 36, AndroidUtilities.isRTL ? Gravity.START : Gravity.END));
                } else {
                    buttonsLayout.addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 36, Gravity.TOP | Gravity.START));
                }
                textView.setOnClickListener(v -> {
                    if (neutralButtonListener != null) {
                        neutralButtonListener.onClick(AlertDialog.this, Dialog.BUTTON_NEGATIVE);
                    }
                    if (dismissDialogByButtons) {
                        dismiss();
                    }
                });
            }

            if (verticalButtons) {
                for (int i = 1; i < buttonsLayout.getChildCount(); i++) {
                    ((ViewGroup.MarginLayoutParams) buttonsLayout.getChildAt(i).getLayoutParams()).topMargin = AndroidUtilities.dp(6);
                }
            }
        }

        Window window = getWindow();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.copyFrom(window.getAttributes());
        if (progressViewStyle == ALERT_TYPE_SPINNER) {
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
        } else {
            if (dimEnabled && !dimCustom) {
                params.dimAmount = dimAlpha;
                params.flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            } else {
                params.dimAmount = 0f;
                params.flags ^= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            }

            lastScreenWidth = AndroidUtilities.getScreenWidth();
//            final int calculatedWidth = AndroidUtilities.displaySize.x - AndroidUtilities.dp(48) - additioanalHorizontalPadding * 2;
            final int calculatedWidth = AndroidUtilities.getScreenWidth() - AndroidUtilities.dp(48) - additioanalHorizontalPadding * 2;
            int maxWidth;
            if (AndroidUtilities.isTablet()) {
                if (AndroidUtilities.isSmallTablet()) {
                    maxWidth = AndroidUtilities.dp(446);
                } else {
                    maxWidth = AndroidUtilities.dp(496);
                }
            } else {
                maxWidth = AndroidUtilities.dp(356);
            }

            params.width = Math.min(maxWidth, calculatedWidth) + backgroundPaddings.left + backgroundPaddings.right;
        }
        if (customView == null || !checkFocusable || !canTextInput(customView)) {
            params.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        } else {
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE;
        }
        if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        }

        if (blurredBackground) {
            if (supportsNativeBlur()) {
                if (progressViewStyle == ALERT_TYPE_MESSAGE) {
                    blurredNativeBackground = true;
                    window.setBackgroundBlurRadius(50);
                    float rad = AndroidUtilities.dp(12);
                    ShapeDrawable shapeDrawable = new ShapeDrawable(new RoundRectShape(new float[]{rad, rad, rad, rad, rad, rad, rad, rad}, null, null));
                    shapeDrawable.getPaint().setColor(ColorUtils.setAlphaComponent(backgroundColor, (int) (blurAlpha * 255)));
                    window.setBackgroundDrawable(shapeDrawable);
                    if (blurBehind) {
                        params.flags |= WindowManager.LayoutParams.FLAG_BLUR_BEHIND;
                        params.setBlurBehindRadius(20);
                    }
                }
            } else {

            }
        }
        window.setAttributes(params);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (onBackButtonListener != null) {
            onBackButtonListener.onClick(AlertDialog.this, AlertDialog.BUTTON_NEGATIVE);
        }
    }

    public void setFocusable(boolean value) {
        if (focusable == value) {
            return;
        }
        focusable = value;
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        if (focusable) {
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
            params.flags &= ~WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        } else {
            params.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
            params.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
        window.setAttributes(params);
    }

    public void setBackgroundColor(int color) {
        backgroundColor = color;
        if (shadowDrawable != null) {
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(backgroundColor, PorterDuff.Mode.MULTIPLY));
        }
    }

    public void setTextColor(int color) {
        if (titleTextView != null) {
            titleTextView.setTextColor(color);
        }
        if (messageTextView != null) {
            messageTextView.setTextColor(color);
        }
    }

    private void showCancelAlert() {
        if (!canCacnel || cancelDialog != null) {
            return;
        }
        Builder builder = new Builder(getContext());
        builder.setTitle("标题");
        builder.setMessage("message1");
        builder.setPositiveButton("等待", null);
        builder.setNegativeButton("停止", (dialogInterface, i) -> {
            if (onCancelListener != null) {
                onCancelListener.onCancel(AlertDialog.this);
            }
            dismiss();
        });
        builder.setOnDismissListener(dialog -> cancelDialog = null);
        try {
            cancelDialog = builder.show();
        } catch (Exception ignore) {

        }
    }

    private void runShadowAnimation(final int num, final boolean show) {
        if (show && !shadowVisibility[num] || !show && shadowVisibility[num]) {
            shadowVisibility[num] = show;
            if (shadowAnimation[num] != null) {
                shadowAnimation[num].cancel();
            }
            shadowAnimation[num] = new AnimatorSet();
            if (shadow[num] != null) {
                shadowAnimation[num].playTogether(ObjectAnimator.ofInt(shadow[num], "alpha", show ? 255 : 0));
            }
            shadowAnimation[num].setDuration(150);
            shadowAnimation[num].addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (shadowAnimation[num] != null && shadowAnimation[num].equals(animation)) {
                        shadowAnimation[num] = null;
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (shadowAnimation[num] != null && shadowAnimation[num].equals(animation)) {
                        shadowAnimation[num] = null;
                    }
                }
            });
            try {
                shadowAnimation[num].start();
            } catch (Exception e) {
            }

        }
    }

    public void setDismissDialogByButtons(boolean value) {
        dismissDialogByButtons = value;
    }

    public void setProgress(int progress) {
        currentProgress = progress;
        updateLineProgressTextView();
    }

    private void updateLineProgressTextView() {
        lineProgressViewPercent.setText(String.format("%d%%", currentProgress));
    }

    public void setCanCancel(boolean value) {
        canCacnel = value;
    }

    private boolean canTextInput(View v) {
        if (v.onCheckIsTextEditor()) {
            return true;
        }
        if (!(v instanceof ViewGroup)) {
            return false;
        }
        ViewGroup vg = (ViewGroup) v;
        int i = vg.getChildCount();
        while (i > 0) {
            i--;
            v = vg.getChildAt(i);
            if (canTextInput(v)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void dismiss() {
        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }
        if (cancelDialog != null) {
            cancelDialog.dismiss();
        }
        try {
            super.dismiss();
        } catch (Throwable ignore) {

        }
        AndroidUtilities.cancelRunOnUIThread(showRunnable);

        if (blurShader != null && blurBitmap != null) {
            blurBitmap.recycle();
            blurShader = null;
            blurPaint = null;
            blurBitmap = null;
        }
    }

    @Override
    public void setCanceledOnTouchOutside(boolean cancel) {
        super.setCanceledOnTouchOutside(cancel);
    }

    public void setTopImage(int resId, int backgroundColor) {
        topResId = resId;
        topBackgroundColor = backgroundColor;
    }

    public void setTopAnimation(int resId, int backgroundColor) {
        setTopAnimation(resId, 94, backgroundColor);
    }

    public void setTopAnimation(int resId, int size, int backgroundColor) {
        topAnimationId = resId;
        topAnimationSize = size;
        topBackgroundColor = backgroundColor;
    }

    public void setTopHeight(int value) {
        topHeight = value;
    }

    public void setTopImage(Drawable drawable, int backgroundColor) {
        topDrawable = drawable;
        topBackgroundColor = backgroundColor;
    }

    public void setTitle(CharSequence text) {
        title = text;
        if (titleTextView != null) {
            titleTextView.setText(text);
        }
    }

    public void setSecondTitle(CharSequence text) {
        secondTitle = text;
    }

    public void setPositiveButton(CharSequence text, final OnClickListener listener) {
        positiveButtonText = text;
        positiveButtonListener = listener;
    }

    public void setNegativeButton(CharSequence text, final OnClickListener listener) {
        negativeButtonText = text;
        negativeButtonListener = listener;
    }

    public void setNeutralButton(CharSequence text, final OnClickListener listener) {
        neutralButtonText = text;
        neutralButtonListener = listener;
    }

    public void setItemColor(int item, int color, int icon) {
        if (item < 0 || item >= itemViews.size()) {
            return;
        }
        AlertDialogCell cell = itemViews.get(item);
        cell.textView.setTextColor(color);
        cell.imageView.setColorFilter(new PorterDuffColorFilter(icon, PorterDuff.Mode.MULTIPLY));
    }

    public int getItemsCount() {
        return itemViews.size();
    }

    public void setMessage(CharSequence text) {
        message = text;
        if (messageTextView != null) {
            if (!TextUtils.isEmpty(message)) {
                messageTextView.setText(message);
                messageTextView.setVisibility(View.VISIBLE);
            } else {
                messageTextView.setVisibility(View.GONE);
            }
        }
    }

    public void setMessageTextViewClickable(boolean value) {
        messageTextViewClickable = value;
    }

    public void setButton(int type, CharSequence text, final OnClickListener listener) {
        switch (type) {
            case BUTTON_NEUTRAL:
                neutralButtonText = text;
                neutralButtonListener = listener;
                break;
            case BUTTON_NEGATIVE:
                negativeButtonText = text;
                negativeButtonListener = listener;
                break;
            case BUTTON_POSITIVE:
                positiveButtonText = text;
                positiveButtonListener = listener;
                break;
        }
    }

    public View getButton(int type) {
        if (buttonsLayout != null) {
            return buttonsLayout.findViewWithTag(type);
        }
        return null;
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        contentScrollView.invalidate();
        scrollContainer.invalidate();
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        if (contentScrollView != null) {
            contentScrollView.postDelayed(what, when);
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        if (contentScrollView != null) {
            contentScrollView.removeCallbacks(what);
        }
    }

    @Override
    public void setOnCancelListener(OnCancelListener listener) {
        onCancelListener = listener;
        super.setOnCancelListener(listener);
    }

    public void setPositiveButtonListener(final OnClickListener listener) {
        positiveButtonListener = listener;
    }

    public void showDelayed(long delay) {
        AndroidUtilities.cancelRunOnUIThread(showRunnable);
        AndroidUtilities.runOnUIThread(showRunnable, delay);
    }


    public ViewGroup getButtonsLayout() {
        return buttonsLayout;
    }

    public static class Builder {

        private AlertDialog alertDialog;

        protected Builder(AlertDialog alert) {
            alertDialog = alert;
        }

        public Builder(Context context) {
            this(context, 0);
        }


        public Builder(Context context, int progressViewStyle) {
            alertDialog = new AlertDialog(context, progressViewStyle);
        }

        public Context getContext() {
            return alertDialog.getContext();
        }

        public Builder forceVerticalButtons() {
            alertDialog.verticalButtons = true;
            return this;
        }

        public Builder setItems(CharSequence[] items, final OnClickListener onClickListener) {
            alertDialog.items = items;
            alertDialog.onClickListener = onClickListener;
            return this;
        }

        public Builder setCheckFocusable(boolean value) {
            alertDialog.checkFocusable = value;
            return this;
        }

        public Builder setItems(CharSequence[] items, int[] icons, final OnClickListener onClickListener) {
            alertDialog.items = items;
            alertDialog.itemIcons = icons;
            alertDialog.onClickListener = onClickListener;
            return this;
        }

        public Builder setView(View view) {
            return setView(view, LayoutHelper.WRAP_CONTENT);
        }

        public Builder setView(View view, int height) {
            alertDialog.customView = view;
            alertDialog.customViewHeight = height;
            return this;
        }

        public Builder setTitle(CharSequence title) {
            alertDialog.title = title;
            return this;
        }

        public Builder setSubtitle(CharSequence subtitle) {
            alertDialog.subtitle = subtitle;
            return this;
        }

        public Builder setTopImage(int resId, int backgroundColor) {
            alertDialog.topResId = resId;
            alertDialog.topBackgroundColor = backgroundColor;
            return this;
        }

        public Builder setTopView(View view) {
            alertDialog.topView = view;
            return this;
        }

        public Builder setTopAnimation(int resId, int size, boolean autoRepeat, int backgroundColor) {
            return setTopAnimation(resId, size, autoRepeat, backgroundColor, null);
        }

        public Builder setTopAnimation(int resId, int size, boolean autoRepeat, int backgroundColor, Map<String, Integer> layerColors) {
            alertDialog.topAnimationId = resId;
            alertDialog.topAnimationSize = size;
            alertDialog.topAnimationAutoRepeat = autoRepeat;
            alertDialog.topBackgroundColor = backgroundColor;
            alertDialog.topAnimationLayerColors = layerColors;
            return this;
        }

        public Builder setTopAnimationIsNew(boolean isNew) {
            alertDialog.topAnimationIsNew = isNew;
            return this;
        }

        public Builder setTopAnimation(int resId, int backgroundColor) {
            return setTopAnimation(resId, 94, true, backgroundColor);
        }

        public Builder setTopImage(Drawable drawable, int backgroundColor) {
            alertDialog.topDrawable = drawable;
            alertDialog.topBackgroundColor = backgroundColor;
            return this;
        }

        public Builder setMessage(CharSequence message) {
            alertDialog.message = message;
            return this;
        }

        public Builder setPositiveButton(CharSequence text, final OnClickListener listener) {
            alertDialog.positiveButtonText = text;
            alertDialog.positiveButtonListener = listener;
            return this;
        }

        public Builder setNegativeButton(CharSequence text, final OnClickListener listener) {
            alertDialog.negativeButtonText = text;
            alertDialog.negativeButtonListener = listener;
            return this;
        }

        public Builder setNeutralButton(CharSequence text, final OnClickListener listener) {
            alertDialog.neutralButtonText = text;
            alertDialog.neutralButtonListener = listener;
            return this;
        }

        public Builder setOnBackButtonListener(final OnClickListener listener) {
            alertDialog.onBackButtonListener = listener;
            return this;
        }

        public Builder setOnCancelListener(OnCancelListener listener) {
            alertDialog.setOnCancelListener(listener);
            return this;
        }

        public Builder setCustomViewOffset(int offset) {
            alertDialog.customViewOffset = offset;
            return this;
        }

        public Builder setMessageTextViewClickable(boolean value) {
            alertDialog.messageTextViewClickable = value;
            return this;
        }

        public AlertDialog create() {
            return alertDialog;
        }

        public AlertDialog show() {
            alertDialog.show();
            return alertDialog;
        }

        public Runnable getDismissRunnable() {
            return alertDialog.dismissRunnable;
        }

        public Builder setOnDismissListener(OnDismissListener onDismissListener) {
            alertDialog.setOnDismissListener(onDismissListener);
            return this;
        }

        public void setTopViewAspectRatio(float aspectRatio) {
            alertDialog.aspectRatio = aspectRatio;
        }

        public Builder setDimEnabled(boolean dimEnabled) {
            alertDialog.dimEnabled = dimEnabled;
            return this;
        }

        public Builder setDimAlpha(float dimAlpha) {
            alertDialog.dimAlpha = dimAlpha;
            return this;
        }

        public void notDrawBackgroundOnTopView(boolean b) {
            alertDialog.notDrawBackgroundOnTopView = b;
            alertDialog.blurredBackground = false;
        }

        public void setButtonsVertical(boolean vertical) {
            alertDialog.verticalButtons = vertical;
        }

        public Builder setOnPreDismissListener(OnDismissListener onDismissListener) {
            alertDialog.onDismissListener = onDismissListener;
            return this;
        }

        public Builder setBlurredBackground(boolean b) {
            alertDialog.blurredBackground = b;
            return this;
        }

        public Builder setAdditionalHorizontalPadding(int padding) {
            alertDialog.additioanalHorizontalPadding = padding;
            return this;
        }
    }
}
