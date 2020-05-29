package info.scelus.soundvisualizer;

import android.Manifest;
import android.content.Intent;
import android.media.audiofx.Visualizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements Visualizer.OnDataCaptureListener {

    private static final int CAPTURE_SIZE = 512; // must be a power of two
    private static final String TAG = MainActivity.class.getSimpleName();
    private Visualizer visualiser;

    private static final int REQUEST_CODE = 0;
    static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO,
                                                     Manifest.permission.MODIFY_AUDIO_SETTINGS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PermissionsChecker checker = new PermissionsChecker(this);

        if (checker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        } else {
            startVisualiser();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    private void startVisualiser() {
        visualiser = new Visualizer(0);
        visualiser.setDataCaptureListener(this, Visualizer.getMaxCaptureRate(), false, true);
        visualiser.setCaptureSize(CAPTURE_SIZE);
        visualiser.setEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            finish();
        }
    }

    @Override
    public void onWaveFormDataCapture(Visualizer visualizer, byte[] waveform, int samplingRate) {
        StringBuilder wave = new StringBuilder();
        for (byte b : waveform) {
            wave.append(" ");
            wave.append(b);
        }

        Log.i(TAG, "onWaveFormDataCapture: " + wave.toString());
    }

    @Override
    public void onFftDataCapture(Visualizer visualizer, byte[] fft, int samplingRate) {
        RxBus.getInstance().send(new CaptureEvent(fft, samplingRate));
    }

    @Override
    protected void onPause() {
        if (visualiser != null) {
            visualiser.setEnabled(false);
            visualiser.release();
            visualiser.setDataCaptureListener(null, 0, false, false);
        }
        super.onPause();
    }
}
