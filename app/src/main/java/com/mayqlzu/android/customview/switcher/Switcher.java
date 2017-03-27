package com.mayqlzu.android.customview.switcher;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Switch;

/**
 * Created by mayq on 17-3-27.
 * an iOS style switcher, it demos gesture and animation.
 * <p/>
 * todo:
 * 1) add a persistent boolean status(on/off).
 * 2) click to switch.
 * 3) save state when orientation changes.
 */
public class Switcher extends View {
    private static final int TRACK_WIDTH = 400; // px
    private static final int TRACK_HEIGHT = 100; // px

    private static int COLOR_CIRCLE = Color.GREEN;
    private static int COLOR_TRACK = Color.WHITE;

    private Paint paint;
    private int firstPointerId = -1;
    private float xDown = -1;

    private boolean pressed = false;
    private int circleCenterX = TRACK_HEIGHT / 2;
    private int circleCenterXDown = circleCenterX;

    public Switcher(Context context, AttributeSet attrs) {
        super(context, attrs);

        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        setMeasuredDimension(TRACK_WIDTH, TRACK_HEIGHT);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int BIG_NUMER = TRACK_HEIGHT * 10;
        RectF rectF = new RectF();
        rectF.set(TRACK_HEIGHT / 2, 0, TRACK_WIDTH - TRACK_HEIGHT / 2, TRACK_HEIGHT);
        paint.setColor(COLOR_TRACK);

        // draw a circle on the left end
        canvas.drawCircle(TRACK_HEIGHT / 2, TRACK_HEIGHT / 2, TRACK_HEIGHT / 2, paint);
        // draw a rectangle
        canvas.drawRect(rectF, paint);
        // draw a circle on the right end
        canvas.drawCircle(TRACK_WIDTH - TRACK_HEIGHT / 2, TRACK_HEIGHT / 2, TRACK_HEIGHT / 2, paint);

        // draw a green circle
        paint.setColor(COLOR_CIRCLE);
        canvas.drawCircle(circleCenterX, TRACK_HEIGHT / 2, TRACK_HEIGHT / 2, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int firstPointerIndex = -1;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // only react to first finger
                firstPointerId = event.getPointerId(0);
                firstPointerIndex = event.findPointerIndex(firstPointerId);
                if (firstPointerIndex >= 0) {
                    xDown = event.getX(firstPointerIndex);
                    circleCenterXDown = circleCenterX;
                    if (xDown > circleCenterX - TRACK_HEIGHT / 2
                            && xDown < circleCenterX + TRACK_HEIGHT / 2) {
                        // circle pressed
                        pressed = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressed) {
                    firstPointerIndex = event.findPointerIndex(firstPointerId);
                    if (firstPointerIndex >= 0) {
                        float xNow = event.getX(firstPointerIndex);
                        float diff = xNow - xDown;
                        circleCenterX = (int) (circleCenterXDown + diff);
                        circleCenterX = Math.max(circleCenterX, TRACK_HEIGHT / 2);
                        circleCenterX = Math.min(circleCenterX, TRACK_WIDTH - TRACK_HEIGHT / 2);

                        invalidate();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // reset
                xDown = -1;
                circleCenterXDown = -1;
                pressed = false;

                // animation
                animate_();
                break;
            default:
                return false;
        }

        return true;
    }

    /**
     * animate circle to left end or right end.
     */
    private void animate_() {
        int targetX = 0;
        if (circleCenterX < TRACK_WIDTH / 2) {
            targetX = TRACK_HEIGHT / 2;
        } else {
            targetX = TRACK_WIDTH - TRACK_HEIGHT / 2;
        }
        int diff = Math.abs(circleCenterX - targetX);
        // the closer you are to the target, the faster it moves
        long duration = 2000 * ((long) diff / TRACK_WIDTH);

        ValueAnimator animator = ValueAnimator.ofInt(circleCenterX, targetX);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int val = (int) animation.getAnimatedValue();
                circleCenterX = val;
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                // todo update on/off status

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }
}
