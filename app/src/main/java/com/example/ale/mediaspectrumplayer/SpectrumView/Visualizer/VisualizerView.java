package com.example.ale.mediaspectrumplayer.SpectrumView.Visualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.audiofx.Equalizer;
import android.media.audiofx.Visualizer;
import android.util.AttributeSet;
import android.view.View;

import java.util.HashSet;
import java.util.Set;

import com.example.ale.mediaspectrumplayer.SpectrumView.Visualizer.ShapeDrawer;


public class VisualizerView extends View {

    private static final String TAG = VisualizerView.class.getName();

    private byte[] mBytes;
    private byte[] mFFTBytes;
    private Rect mRect = new Rect();
    private Visualizer mVisualizer;
    private Equalizer mEqualizer;

    private Set<ShapeDrawer> mShapeDrawers;

    private Paint mFlashPaint = new Paint();
    private Paint mFadePaint = new Paint();

    boolean mFlash = false;

    Bitmap mCanvasBitmap;
    Canvas mCanvas;

    public VisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mBytes = null;
        mFFTBytes = null;

        mFlashPaint.setColor(Color.argb(122, 255, 255, 255)); // Change Alpha to adjust fading speed
        mFadePaint.setColor(Color.argb(218, 255, 255, 255)); // Change Alpha to adjust fading speed
        mFadePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));

        mShapeDrawers = new HashSet<ShapeDrawer>();
    }



    public void link(MediaPlayer player) {

        if (player == null) {

            throw new NullPointerException("Cannot link to null MediaPlayer");
        }

        mEqualizer = new Equalizer(0, player.getAudioSessionId());
        mEqualizer.setEnabled(true); // need to enable equalizer

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(player.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

        // Pass through Visualizer data to VisualizerView
        Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
            @Override
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                updateVisualizer(bytes);
            }

            @Override
            public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                         int samplingRate) {
                updateVisualizerFFT(bytes);
            }
        };

        mVisualizer.setDataCaptureListener(captureListener,
                Visualizer.getMaxCaptureRate() / 2, true, true);

        // Enabled Visualizer and disable when we're done with the stream
        mVisualizer.setEnabled(true);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mVisualizer.setEnabled(false);

            }
        });
    }

    public void addRenderer(ShapeDrawer shapeDrawer) {
        if (shapeDrawer != null) {
            mShapeDrawers.add(shapeDrawer);
        }
    }

    // Clears all Drawers ( Circle, etc ..)
    public void clearDrawers() {
        mShapeDrawers.clear();
    }

    /**
     * Call to release the resources used by VisualizerView.
     */
    public void release() {
        mVisualizer.release();
    }


    public void updateVisualizer(byte[] bytes) {
        mBytes = bytes;
        invalidate();
    }


    public void updateVisualizerFFT(byte[] bytes) {
        mFFTBytes = bytes;
        invalidate();
    }


    /**
     * Call this to make the visualizer flash. Useful for flashing at the start
     * of a song/loop etc...
     */
    public void flash() {
        mFlash = true;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create canvas once we're ready to draw
        mRect.set(0, 0, getWidth(), getHeight());

        if (mCanvasBitmap == null) {
            mCanvasBitmap = Bitmap.createBitmap(canvas.getWidth(), canvas.getHeight(), Bitmap.Config.ARGB_8888);
        }
        if (mCanvas == null) {
            mCanvas = new Canvas(mCanvasBitmap);
        }

        if (mBytes != null) {
            // Render all audio renderers
            AudioData audioData = new AudioData(mBytes);
            for (ShapeDrawer r : mShapeDrawers) {
                r.render(mCanvas, audioData, mRect);
            }
        }

        if (mFFTBytes != null) {
            // Render all FFT renderers
            FFTData fftData = new FFTData(mFFTBytes);
            for (ShapeDrawer r : mShapeDrawers) {
                r.render(mCanvas, fftData, mRect);
            }
        }

        // Fade out old content
        mCanvas.drawPaint(mFadePaint);

        if (mFlash) {
            mFlash = false;
            mCanvas.drawPaint(mFlashPaint);
        }

        canvas.drawBitmap(mCanvasBitmap, new Matrix(), null);
    }

}
