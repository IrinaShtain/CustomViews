package com.shtainyky.customview.views;


import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.shtainyky.customview.R;

/**
 * Created by Irina Shtain on 01.04.2017.
 */

public class CustomProgressView extends View {

    private final static int DEFAULT_DURATION_SHORT = 300;  //for moving
    private final static int DEFAULT_DURATION_LONG = 500;   //for blinking
    private final static int STATE_IS_ANIMATED = 101;
    private final static int STATE_IS_CANCELED = 102;

    private final static int DEFAULT_BACKGROUND_SMALL_SQUARE_COLOR = R.color.colorAccent;
    private final static int DEFAULT_BACKGROUND_BIG_SQUARE_COLOR = R.color.colorAccent;
    private final static int DEFAULT_LENGTH_SQUARE_SIDE = 20;
    private final static int DEFAULT_DISTANCE_BETWEEN_SQUARES = 200;

    private final static String WARN_WRONG_LENGTH_SIDE_OF_SQUARE = "Wrong length of square's side";
    private final static String WARN_WRONG_DISTANCE_BETWEEN_SQUARES = "Wrong distance between squares";

    private int mBackgroundSmallSquareColor;
    private int mBackgroundBigSquareColor;
    private int mLengthSquareSide;
    private int mDistanceBetweenSquares;

    private int mCenterX, mCenterY;
    private int mBlinkBigSquareAlpha = 90;
    private int mBlinkSmallSquareAlpha = 255;

    private Paint mBackgroundPaint;
    private Paint mInnerSquarePaint;
    private RectF mRectF;
    private Path mPath;

    private int mLeftTopX, mLeftTopY;
    private int mRightTopX, mRightTopY;
    private int mRightBottomX, mRightBottomY;
    private int mLeftBottomX, mLeftBottomY;

    private AnimatorSet mAnimatorSetFirst;
    private AnimatorSet mAnimatorSetSecond;
    private boolean mShouldStartEscalationSquareAnimation;
    private boolean mShouldStartConvolutionSquareAnimation;
    private int mAnimatedState = 100;

    public CustomProgressView(Context context) {
        super(context);
        init(context);
    }

    public CustomProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CustomProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context) {
        mBackgroundSmallSquareColor = ContextCompat.getColor(context, DEFAULT_BACKGROUND_SMALL_SQUARE_COLOR);
        mBackgroundBigSquareColor = ContextCompat.getColor(context, DEFAULT_BACKGROUND_BIG_SQUARE_COLOR);
        mLengthSquareSide = DEFAULT_LENGTH_SQUARE_SIDE;
        mDistanceBetweenSquares = DEFAULT_DISTANCE_BETWEEN_SQUARES;
        initMainUtil();
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        TypedArray attributes = context.getTheme().obtainStyledAttributes(
                attrs, R.styleable.CustomProgressView, 0, 0);
        try {
            mBackgroundSmallSquareColor = attributes.getColor(R.styleable.CustomProgressView_backgroundSmallSquaresColor,
                    ContextCompat.getColor(context, DEFAULT_BACKGROUND_SMALL_SQUARE_COLOR));
            mBackgroundBigSquareColor = attributes.getColor(R.styleable.CustomProgressView_backgroundBigSquareColor,
                    ContextCompat.getColor(context, DEFAULT_BACKGROUND_BIG_SQUARE_COLOR));
            mLengthSquareSide = attributes.getColor(R.styleable.CustomProgressView_lengthSquareSide,
                    DEFAULT_LENGTH_SQUARE_SIDE);
            mDistanceBetweenSquares = attributes.getColor(R.styleable.CustomProgressView_distanceBetweenSquares,
                    DEFAULT_DISTANCE_BETWEEN_SQUARES);

        } finally {
            attributes.recycle();
        }
        initMainUtil();
    }

    private void initMainUtil() {
        setSaveEnabled(true);
        mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerSquarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mRectF = new RectF();
        mPath = new Path();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int size = mLengthSquareSide + mDistanceBetweenSquares;
        int width = resolveSizeAndState(size, widthMeasureSpec, 0);
        int height = resolveSizeAndState(size, heightMeasureSpec, 0);

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = getWidth() / 2 - mLengthSquareSide / 2;
        mCenterY = getHeight() / 2 - mLengthSquareSide / 2;
        initPoints();
        if (mAnimatedState == STATE_IS_ANIMATED) startAnimation();
        if (mAnimatedState == STATE_IS_CANCELED) cancelAnimationAndHide();

    }

    //initialization coordinates for small squares
    private void initPoints() {
        mLeftTopX = mCenterX - mDistanceBetweenSquares / 2;
        mLeftTopY = mCenterY - mDistanceBetweenSquares / 2;
        mRightTopX = mCenterX + mDistanceBetweenSquares / 2;
        mRightTopY = mCenterY - mDistanceBetweenSquares / 2;
        mRightBottomX = mCenterX + mDistanceBetweenSquares / 2;
        mRightBottomY = mCenterY + mDistanceBetweenSquares / 2;
        mLeftBottomX = mCenterX - mDistanceBetweenSquares / 2;
        mLeftBottomY = mCenterY + mDistanceBetweenSquares / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawSmallSquares(canvas);
        drawLines(canvas);
        drawBigSquares(canvas);
    }

    private void drawSmallSquares(Canvas canvas) {
        mBackgroundPaint.setColor(mBackgroundSmallSquareColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setAlpha(mBlinkSmallSquareAlpha);
        //left top square
        mRectF.set(mLeftTopX, mLeftTopY, mLeftTopX + mLengthSquareSide, mLeftTopY + mLengthSquareSide);
        canvas.drawRect(mRectF, mBackgroundPaint);
        //right top square
        mRectF.set(mRightTopX, mRightTopY, mRightTopX + mLengthSquareSide, mRightTopY + mLengthSquareSide);
        canvas.drawRect(mRectF, mBackgroundPaint);
        // right bottom square
        mRectF.set(mRightBottomX, mRightBottomY, mRightBottomX + mLengthSquareSide, mRightBottomY + mLengthSquareSide);
        canvas.drawRect(mRectF, mBackgroundPaint);
        // left bottom square
        mRectF.set(mLeftBottomX, mLeftBottomY, mLeftBottomX + mLengthSquareSide, mLeftBottomY + mLengthSquareSide);
        canvas.drawRect(mRectF, mBackgroundPaint);
    }

    private void drawLines(Canvas canvas) {
        mBackgroundPaint.setColor(mBackgroundSmallSquareColor);
        mBackgroundPaint.setStyle(Paint.Style.STROKE);
        mBackgroundPaint.setStrokeWidth(mLengthSquareSide / 10);
        mPath.reset();
        mPath.moveTo(mLeftBottomX + mLengthSquareSide / 2, mLeftBottomY + mLengthSquareSide / 2); //move to left bottom square
        mPath.lineTo(mRightBottomX + mLengthSquareSide / 2, mRightBottomY + mLengthSquareSide / 2); //line to right bottom square
        mPath.lineTo(mRightTopX + mLengthSquareSide / 2, mRightTopY + mLengthSquareSide / 2); //line to right top square
        mPath.lineTo(mLeftTopX + mLengthSquareSide / 2, mLeftTopY + mLengthSquareSide / 2); //line to left top square
        mPath.lineTo(mLeftBottomX + mLengthSquareSide / 2, mLeftBottomY + mLengthSquareSide / 2); //line to left bottom square
        mPath.lineTo(mRightTopX + mLengthSquareSide / 2, mRightTopY + mLengthSquareSide / 2); //line to right top square
        mPath.moveTo(mLeftTopX + mLengthSquareSide / 2, mLeftTopY + mLengthSquareSide / 2); //move to left top square
        mPath.lineTo(mRightBottomX + mLengthSquareSide / 2, mRightBottomY + mLengthSquareSide / 2); //line to right bottom square
        canvas.drawPath(mPath, mBackgroundPaint);
    }

    private void drawBigSquares(Canvas canvas) {
        mInnerSquarePaint.setColor(mBackgroundBigSquareColor);
        mInnerSquarePaint.setStyle(Paint.Style.FILL);
        mInnerSquarePaint.setAlpha(mBlinkBigSquareAlpha);
        mRectF.set(mCenterX - mDistanceBetweenSquares / 4, mCenterY - mDistanceBetweenSquares / 4,
                mCenterX + mDistanceBetweenSquares / 4 + mLengthSquareSide, mCenterY + mDistanceBetweenSquares / 4 + mLengthSquareSide);
        canvas.drawRect(mRectF, mInnerSquarePaint);
    }


    /* public methods for starting and canceling animation */
    public void startAnimation() {
        this.setVisibility(VISIBLE);
        if (!mShouldStartEscalationSquareAnimation && !mShouldStartConvolutionSquareAnimation) {
            startConvolutionSquareAnimation();
            mAnimatedState = STATE_IS_ANIMATED;
        }
        mShouldStartEscalationSquareAnimation = true;
        mShouldStartConvolutionSquareAnimation = true;

    }

    public void cancelAnimationAndHide() {
        Log.d("myLog", "cancelAnimationAndHide ");
        mShouldStartEscalationSquareAnimation = false;
        mShouldStartConvolutionSquareAnimation = false;
        if (mAnimatorSetFirst != null && mAnimatorSetFirst.isStarted()) {
            mAnimatorSetFirst.cancel();
            Log.d("myLog", " cancelAnimationAndHide mAnimatorSetFirst");
        }
        if (mAnimatorSetSecond != null && mAnimatorSetSecond.isStarted()) {
            mAnimatorSetSecond.cancel();
            Log.d("myLog", "cancelAnimationAndHide mAnimatorSetSecond");
        }
        mAnimatedState = STATE_IS_CANCELED;
        initPoints();
        this.setVisibility(GONE);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.setValue(mAnimatedState);
        return savedState;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mAnimatedState = savedState.getValue();
    }


    /* getters and setters*/

    public int getBackgroundSmallSquareColor() {
        return mBackgroundSmallSquareColor;
    }

    public void setBackgroundSmallSquareColor(int backgroundSmallSquareColor) {
        mBackgroundSmallSquareColor = backgroundSmallSquareColor;
        invalidate();
    }

    public int getLengthSquareSide() {
        return mLengthSquareSide;
    }

    public void setLengthSquareSide(int lengthSquareSide) {
        if (lengthSquareSide <= 0)
            throw new IllegalArgumentException(WARN_WRONG_LENGTH_SIDE_OF_SQUARE);
        mLengthSquareSide = lengthSquareSide;
        invalidate();
    }

    public int getDistanceBetweenSquares() {
        return mDistanceBetweenSquares;
    }

    public void setDistanceBetweenSquares(int distanceBetweenSquares) {
        if (distanceBetweenSquares <= 0)
            throw new IllegalArgumentException(WARN_WRONG_DISTANCE_BETWEEN_SQUARES);
        mDistanceBetweenSquares = distanceBetweenSquares;
        invalidate();
    }

    public int getBackgroundBigSquareColor() {
        return mBackgroundBigSquareColor;
    }

    public void setBackgroundBigSquareColor(int backgroundBigSquareColor) {
        mBackgroundBigSquareColor = backgroundBigSquareColor;
        invalidate();
    }

    /*  starts to make convolution of a square  */

    private void startConvolutionSquareAnimation() {
        ValueAnimator blinkBigSquareAnimator = getBlinkBigSquareAnimator();

        ValueAnimator moveRightBottomYAnimator = getMovedRightBottomYAnimator();
        ValueAnimator moveRightTopXAnimator = getMovedRightTopXAnimator();
        ValueAnimator moveLeftTopYAnimator = getMovedLeftTopYAnimator();
        ValueAnimator moveLeftBottomXAnimator = getMovedLeftBottomXAnimator();

        ValueAnimator moveBottomYAnimator = getMovedBottomYAnimator();
        ValueAnimator moveTopYAnimator = getMovedTopYAnimator();
        ValueAnimator moveRightXAnimator = getMovedRightXAnimator();
        ValueAnimator moveLeftXAnimator = getMovedLeftXAnimator();

        ValueAnimator blinkSmallSquareAnimator = getBlinkSmallSquareAnimator();

        mAnimatorSetFirst = new AnimatorSet();

        mAnimatorSetFirst.playSequentially(blinkBigSquareAnimator, moveRightBottomYAnimator,
                moveLeftBottomXAnimator, moveLeftTopYAnimator, moveRightTopXAnimator,
                moveBottomYAnimator, moveTopYAnimator, moveRightXAnimator,
                moveLeftXAnimator, blinkSmallSquareAnimator);


        mAnimatorSetFirst.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShouldStartEscalationSquareAnimation)
                    startEscalationSquareAnimation();
                Log.d("myLog", "onAnimationEnd mAnimatorSetSecond");
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d("myLog", "cancelAnimationAndHide mAnimatorSetFirst");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (!mAnimatorSetFirst.isStarted()) {
            mAnimatorSetFirst.start();
        }
    }

    private ValueAnimator getBlinkBigSquareAnimator() {
        ValueAnimator blinkAnimator = ValueAnimator.ofInt(90, 0);
        blinkAnimator.setDuration(DEFAULT_DURATION_LONG);
        blinkAnimator.setRepeatCount(2);
        blinkAnimator.setRepeatMode(ValueAnimator.RESTART);
        blinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBlinkBigSquareAlpha = (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return blinkAnimator;
    }

    private ValueAnimator getMovedRightBottomYAnimator() {
        ValueAnimator moveRightBottomYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveRightBottomYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveRightBottomYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveRightBottomYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightBottomY = mCenterY + mDistanceBetweenSquares / 2 - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveRightBottomYAnimator;
    }

    private ValueAnimator getMovedRightTopXAnimator() {
        ValueAnimator moveRightTopXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveRightTopXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveRightTopXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveRightTopXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightTopX = mCenterX + mDistanceBetweenSquares / 2 - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveRightTopXAnimator;
    }

    private ValueAnimator getMovedLeftTopYAnimator() {
        ValueAnimator moveLeftTopYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveLeftTopYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveLeftTopYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveLeftTopYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftTopY = mCenterY - mDistanceBetweenSquares / 2 + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveLeftTopYAnimator;
    }

    private ValueAnimator getMovedLeftBottomXAnimator() {
        ValueAnimator moveLeftBottomXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveLeftBottomXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveLeftBottomXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveLeftBottomXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftBottomX = mCenterX - mDistanceBetweenSquares / 2 + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveLeftBottomXAnimator;
    }

    private ValueAnimator getMovedBottomYAnimator() {
        ValueAnimator moveBottomYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveBottomYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveBottomYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveBottomYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftBottomY = mCenterY + mDistanceBetweenSquares / 2 - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveBottomYAnimator;
    }

    private ValueAnimator getMovedTopYAnimator() {
        ValueAnimator moveTopYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveTopYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveTopYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveTopYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightTopY = mCenterY - mDistanceBetweenSquares / 2 + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveTopYAnimator;
    }

    private ValueAnimator getMovedRightXAnimator() {
        ValueAnimator moveRightXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveRightXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveRightXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveRightXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightBottomX = mCenterX + mDistanceBetweenSquares / 2 - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveRightXAnimator;
    }

    private ValueAnimator getMovedLeftXAnimator() {
        ValueAnimator moveLeftXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveLeftXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveLeftXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveLeftXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftTopX = mCenterX - mDistanceBetweenSquares / 2 + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveLeftXAnimator;
    }

    private ValueAnimator getBlinkSmallSquareAnimator() {
        ValueAnimator blinkAnimator = ValueAnimator.ofInt(255, 0);
        blinkAnimator.setDuration(DEFAULT_DURATION_LONG);
        blinkAnimator.setRepeatCount(2);
        blinkAnimator.setRepeatMode(ValueAnimator.RESTART);
        blinkAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mBlinkSmallSquareAlpha = (int) animator.getAnimatedValue();
                invalidate();
            }
        });

        blinkAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mBlinkSmallSquareAlpha = 255;
                invalidate();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        return blinkAnimator;
    }

    /*starts to escalate square, it is a reverse of convolution a square */

    private void startEscalationSquareAnimation() {
        ValueAnimator reverseMovedRightXAnimator = getReverseMovedRightXAnimator();
        ValueAnimator reverseMovedBottomYAnimator = getReverseMovedBottomYAnimator();
        ValueAnimator reverseMovedLeftXAnimator = getReverseMovedLeftXAnimator();
        ValueAnimator reverseMovedTopYAnimator = getReverseMovedTopYAnimator();

        ValueAnimator reverseMovedRightBottomYAnimator = getReverseMovedRightBottomYAnimator();
        ValueAnimator reverseMovedLeftBottomXAnimator = getReverseMovedLeftBottomXAnimator();
        ValueAnimator reverseMovedRightTopXAnimator = getReverseMovedRightTopXAnimator();
        ValueAnimator reverseMovedLeftTopYAnimator = getReverseMovedLeftTopYAnimator();

        mAnimatorSetSecond = new AnimatorSet();

        mAnimatorSetSecond.playSequentially(reverseMovedRightXAnimator, reverseMovedBottomYAnimator,
                reverseMovedLeftXAnimator, reverseMovedTopYAnimator, reverseMovedRightBottomYAnimator,
                reverseMovedLeftBottomXAnimator, reverseMovedRightTopXAnimator, reverseMovedLeftTopYAnimator);

        mAnimatorSetSecond.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (mShouldStartConvolutionSquareAnimation)
                    startConvolutionSquareAnimation();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.d("myLog", "cancelAnimationAndHide mAnimatorSetSecond");
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        if (!mAnimatorSetSecond.isStarted()) {
            mAnimatorSetSecond.start();
        }

    }

    private ValueAnimator getReverseMovedRightXAnimator() {
        ValueAnimator moveReverseRightBottomYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseRightBottomYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseRightBottomYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseRightBottomYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightBottomX = mCenterX + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseRightBottomYAnimator;
    }

    private ValueAnimator getReverseMovedBottomYAnimator() {
        ValueAnimator moveReverseBottomYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseBottomYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseBottomYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseBottomYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftBottomY = mCenterY + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseBottomYAnimator;
    }

    private ValueAnimator getReverseMovedLeftXAnimator() {
        ValueAnimator moveReverseLeftXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseLeftXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseLeftXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseLeftXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftTopX = mCenterX - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseLeftXAnimator;
    }

    private ValueAnimator getReverseMovedTopYAnimator() {
        ValueAnimator moveTopYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveTopYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveTopYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveTopYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightTopY = mCenterY - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveTopYAnimator;
    }

    private ValueAnimator getReverseMovedRightBottomYAnimator() {
        ValueAnimator moveReverseRightBottomYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseRightBottomYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseRightBottomYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseRightBottomYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightBottomY = mCenterY + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseRightBottomYAnimator;
    }

    private ValueAnimator getReverseMovedLeftBottomXAnimator() {
        ValueAnimator moveReverseLeftBottomXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseLeftBottomXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseLeftBottomXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseLeftBottomXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftBottomX = mCenterX - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseLeftBottomXAnimator;
    }

    private ValueAnimator getReverseMovedRightTopXAnimator() {
        ValueAnimator moveReverseRightTopXAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseRightTopXAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseRightTopXAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseRightTopXAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mRightTopX = mCenterX + (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseRightTopXAnimator;
    }

    private ValueAnimator getReverseMovedLeftTopYAnimator() {
        ValueAnimator moveReverseLeftTopYAnimator = ValueAnimator.ofInt(0, mDistanceBetweenSquares / 2);
        moveReverseLeftTopYAnimator.setDuration(DEFAULT_DURATION_SHORT);
        moveReverseLeftTopYAnimator.setRepeatMode(ValueAnimator.RESTART);
        moveReverseLeftTopYAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                mLeftTopY = mCenterY - (int) animator.getAnimatedValue();
                invalidate();
            }
        });
        return moveReverseLeftTopYAnimator;
    }

    private static class SavedState extends BaseSavedState {
        private int value; //this will store the current value from ValueBar

        int getValue() {
            return value;
        }

        void setValue(int value) {
            this.value = value;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            value = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(value);
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
