package com.taberonvslavrik.boostgallery.view;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.taberonvslavrik.boostgallery.util.ImageLoader;

/**
 * @author Ivan Lavryshyn
 */

public class SwipeImageView extends FrameLayout {

    private static final int ANIMATION_DURATION = 128;

    private ColorDrawable mBackgroundDrawable = new ColorDrawable(Color.BLACK);

    private GestureDetector mGestureDetector;
    private ImageView mCurrentImageView;
    private String[] mImagePathArray;
    private int mCurrentImageIndex;

    public SwipeImageView(Context context) {
        super(context);
    }

    public SwipeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        this.setBackground(mBackgroundDrawable);
        mGestureDetector = new GestureDetector(getContext(), new SwipeGestureDetectorListener());
        if (mImagePathArray != null && mCurrentImageView == null) {
            final ImageView newImageView = createImageView();
            mCurrentImageView = newImageView;
            addView(newImageView);
            ImageLoader.loadImage(mImagePathArray[mCurrentImageIndex], newImageView);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        mImagePathArray = null;
        mCurrentImageView = null;
        mGestureDetector = null;
        removeAllViews();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.state = mCurrentImageIndex;
        savedState.array = mImagePathArray;
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentImageIndex = savedState.state;
        mImagePathArray = savedState.array;
    }

    public void setImages(String[] imagePathArray) {
        mImagePathArray = imagePathArray;
    }

    public void showImage(final int imageIndex,
                          final int thumbnailTop,
                          final int thumbnailLeft,
                          final int thumbnailRight,
                          final int thumbnailBottom) {
        final ImageView imageView = createImageView();
        mCurrentImageView = imageView;
        mCurrentImageIndex = imageIndex;
        this.addView(imageView);
        ImageLoader.loadImage(mImagePathArray[imageIndex], imageView);

        ViewTreeObserver observer = imageView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {

            @Override
            public boolean onPreDraw() {
                imageView.getViewTreeObserver().removeOnPreDrawListener(this);

                int[] screenLocation = new int[2];
                imageView.getLocationOnScreen(screenLocation);
                int mLeftDelta = thumbnailLeft - screenLocation[0];
                int mTopDelta = thumbnailTop - screenLocation[1];

                float mWidthScale = (float) thumbnailRight / imageView.getWidth();
                float mHeightScale = (float) thumbnailBottom / imageView.getHeight();

                openAnimation(mLeftDelta, mTopDelta, mWidthScale, mHeightScale);
                return true;
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private ImageView createImageView() {
        final ImageView imageView = new ImageView(getContext());
        final FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        imageView.setLayoutParams(layoutParams);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });
        return imageView;
    }

    private void openAnimation(final int mLeftDelta, final int mTopDelta,
                               final float mWidthScale, final float mHeightScale) {
        mCurrentImageView.setPivotX(0);
        mCurrentImageView.setPivotY(0);
        mCurrentImageView.setScaleX(mWidthScale);
        mCurrentImageView.setScaleY(mHeightScale);
        mCurrentImageView.setTranslationX(mLeftDelta);
        mCurrentImageView.setTranslationY(mTopDelta);

        mCurrentImageView.animate()
                .setDuration(ANIMATION_DURATION)
                .scaleX(1)
                .scaleY(1)
                .translationX(0)
                .translationY(0)
                .withStartAction(new Runnable() {
                    @Override
                    public void run() {
                        ObjectAnimator.ofInt(mBackgroundDrawable, "alpha", 0, 255)
                                .setDuration(ANIMATION_DURATION)
                                .start();
                    }
                })
                .setInterpolator(new DecelerateInterpolator());
    }

    private class SwipeGestureDetectorListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            // Swipe left (next)
            if (e1.getX() > e2.getX()) {
                if (mCurrentImageIndex < mImagePathArray.length) {
                    final ImageView newImageView = createImageView();
                    newImageView.setTranslationX(mCurrentImageView.getWidth());
                    SwipeImageView.this.addView(newImageView);
                    ImageLoader.loadImage(mImagePathArray[++mCurrentImageIndex], newImageView);
                    newImageView.animate()
                            .setDuration(ANIMATION_DURATION)
                            .setInterpolator(new AccelerateInterpolator())
                            .translationX(0)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    mCurrentImageView.animate()
                                            .setDuration(ANIMATION_DURATION)
                                            .setInterpolator(new LinearInterpolator())
                                            .translationX(-mCurrentImageView.getWidth())
                                            .start();
                                }
                            })
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    SwipeImageView.this.removeView(mCurrentImageView);
                                    mCurrentImageView = newImageView;
                                }
                            })
                            .start();
                }
            } else if (e1.getX() < e2.getX()) {// Swipe right (previous)
                if (mCurrentImageIndex > 0) {
                    final ImageView newImageView = createImageView();
                    newImageView.setTranslationX(-mCurrentImageView.getWidth());
                    SwipeImageView.this.addView(newImageView);
                    ImageLoader.loadImage(mImagePathArray[--mCurrentImageIndex], newImageView);
                    newImageView.animate()
                            .setDuration(ANIMATION_DURATION)
                            .setInterpolator(new AccelerateInterpolator())
                            .translationX(0)
                            .withStartAction(new Runnable() {
                                @Override
                                public void run() {
                                    mCurrentImageView.animate()
                                            .setDuration(ANIMATION_DURATION)
                                            .setInterpolator(new LinearInterpolator())
                                            .translationX(mCurrentImageView.getWidth())
                                            .start();
                                }
                            })
                            .withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    SwipeImageView.this.removeView(mCurrentImageView);
                                    mCurrentImageView = newImageView;
                                }
                            })
                            .start();
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    static class SavedState extends BaseSavedState {
        int state;
        String[] array;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);

            state = in.readInt();
            array = new String[]{};
            in.readStringArray(array);
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(state);
            out.writeStringArray(array);
        }

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
