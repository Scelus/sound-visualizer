package info.scelus.soundvisualizer;

import java.util.Arrays;

/**
 * Created by scelus on 02.05.17
 */

public class CaptureEvent {

    private byte[] waveform;
    private int samplingRate;

    public CaptureEvent(byte[] waveform, int samplingRate) {
        this.waveform = waveform;
        this.samplingRate = samplingRate;
    }

    public byte[] getWaveform() {
        return waveform;
    }

    public void setWaveform(byte[] waveform) {
        this.waveform = waveform;
    }

    public int getSamplingRate() {
        return samplingRate;
    }

    public void setSamplingRate(int samplingRate) {
        this.samplingRate = samplingRate;
    }

    @Override
    public String toString() {
        return "CaptureEvent{" +
                "waveform=" + Arrays.toString(waveform) +
                ", samplingRate=" + samplingRate +
                '}';
    }
}
