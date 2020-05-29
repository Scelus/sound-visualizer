package info.scelus.soundvisualizer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by scelus on 02.05.17
 */

public class CircularVisualizerView extends View {
    public final static String TAG = "Visualizer View";
    int radius = 250; // pixels
    private Disposable subscription;

    private Paint mPaint;
    private Paint mPaintPath;


    private ArrayList<PointF> points;
    private Path mPath;

    private byte[] waveform;
    private int samplingRate;

    private int width;
    private int height;
    private int lastCircularIndex = 0;

    public CircularVisualizerView(Context context) {
        super(context);
        init();
    }

    public CircularVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(2.5f);

        mPaintPath = new Paint();
        mPaintPath.setStyle(Paint.Style.STROKE);
        mPaintPath.setStrokeWidth(2.5f);
        mPaintPath.setAntiAlias(true);
        mPaintPath.setPathEffect(new CornerPathEffect(2));

        mPath = new Path();
        points = new ArrayList<>();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode())
            return;

        subscription = RxBus.getInstance().getBus().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object captureEvent) throws Exception {
                if (captureEvent instanceof CaptureEvent) {
                    waveform = ((CaptureEvent) captureEvent).getWaveform();
                    samplingRate = ((CaptureEvent) captureEvent).getSamplingRate();

                    mPath.reset();
                    int steps = 4;
                    for (int i = 0; i < waveform.length; i += steps) {

                        float angle = 360f / (i + 1);
                        float x2 = (float) ((radius + waveform[i]) * Math.cos(angle));
                        float y2 = (float) ((radius + waveform[i]) * Math.sin(angle));

                        if (i == 0)
                            mPath.moveTo(x2, y2);
                        else
                            mPath.lineTo(x2, y2);
                    }
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (isInEditMode())
            return;

        subscription.dispose();
    }

    private void drawCircularVisualization(Canvas c) {
        points.clear();
        int BUFFER_SIZE = 160;
        float step = 360f / BUFFER_SIZE;
        for (int i = 16; i < BUFFER_SIZE + 16; i++) {
            float angleIndex =  lastCircularIndex + i;
            float angle = (float) Math.toRadians(step * angleIndex);

            float x1 = c.getWidth() / 2 + (float) (radius * Math.cos(angle));
            float y1 = c.getHeight() / 2 + (float) (radius * Math.sin(angle));

            float x2 = c.getWidth() / 2 + (float) ((radius + waveform[i]) * Math.cos(angle) * 1.25);
            float y2 = c.getHeight() / 2 + (float) ((radius + waveform[i]) * Math.sin(angle) * 1.25);

            points.add(new PointF(x2, y2));

            c.drawLine(x1, y1, x2, y2, mPaintPath);
        }

        lastCircularIndex = lastCircularIndex >= BUFFER_SIZE ? 0 : lastCircularIndex + 1;

        // calculate the bezier path
        mPath.reset();
        mPath.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 0; i < points.size() - 1; i++) {
            mPath.quadTo(points.get(i).x, points.get(i).y, points.get(i + 1).x, points.get(i + 1).y);
        }
        mPath.close();

        c.drawPath(mPath, mPaintPath);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(Color.WHITE);
        canvas.drawPaint(mPaint);

        if (waveform != null && waveform.length > 0) {
            mPaint.setColor(Color.LTGRAY);
            canvas.drawCircle(width / 2, height / 2, radius, mPaintPath);

            mPaint.setColor(Color.DKGRAY);
            drawCircularVisualization(canvas);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int desiredWidth = 400;
        int desiredHeight = 400;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        // Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.max(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        // Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.max(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        // MUST CALL THIS!
        setMeasuredDimension(width, height);
    }
}
