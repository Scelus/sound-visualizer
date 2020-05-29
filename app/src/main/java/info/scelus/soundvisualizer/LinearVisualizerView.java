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
import android.util.Log;
import android.view.View;

import org.reactivestreams.Subscription;

import java.util.ArrayList;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by scelus on 02.05.17
 */

public class LinearVisualizerView extends View {
    public final static String TAG = "Visualizer View";
    int radius = 250; // pixels
    private Disposable subscription;

    private Paint mPaint;
    private Paint mPaintPath;

    private Path mPath;

    private byte[] waveform;
    private int samplingRate;

    private int width;
    private int height;
    private int lastCircularIndex = 0;
    private final int BUFFER_SIZE = 160;

    public LinearVisualizerView(Context context) {
        super(context);
        init();
    }

    public LinearVisualizerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LinearVisualizerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

    private void drawLinearVisualization(Canvas canvas) {
        int steps = 2;
        int value;
        mPath.reset();
        for (int i = 0; i < waveform.length; i += steps) {
            value = 0;

            for (int j = 0; j < steps; j++)
                value += waveform[i + j];
            value /= steps;

            if (i == 0)
                mPath.moveTo(i * 4, 206 - value);
            else
                mPath.lineTo(i * 4, 206 - value);

            canvas.drawLine(i * 4, 256, i * 4, 206 - value, mPaint);
        }

        canvas.drawPath(mPath, mPaintPath);
    }



    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mPaint.setColor(Color.WHITE);
        canvas.drawPaint(mPaint);

        if (waveform != null && waveform.length > 0) {
            mPaint.setColor(Color.LTGRAY);

            // circular visualization
            mPaint.setColor(Color.DKGRAY);
            drawLinearVisualization(canvas);
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int desiredWidth = getResources().getDimensionPixelSize(R.dimen.linear_visualizer_width);
        int desiredHeight = getResources().getDimensionPixelSize(R.dimen.linear_visualizer_height);

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
