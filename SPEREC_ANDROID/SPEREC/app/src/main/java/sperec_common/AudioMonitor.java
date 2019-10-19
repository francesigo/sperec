package sperec_common;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

public class AudioMonitor implements AudioProcessor {

    private double rms;

    public double getRMS() {
        return rms;
    }

    /**
     * Does not touch the audio samples: just make some calculations
     * @param var1
     * @return
     */
    public boolean process(AudioEvent var1) {
        float [] audioFloatBuffer = (float[])var1.getFloatBuffer();

        // Compute the volume of the audio samples in the buffer
        double sum = 0;
        int readSize = audioFloatBuffer.length;
        for (int i = 0; i < readSize; i++) {
            sum += audioFloatBuffer [i] * audioFloatBuffer [i];
        }
        if (readSize > 0) {
            final double amplitude = sum / readSize;
            this.rms = Math.sqrt(amplitude);
        }
        return true;
    }

    public void processingFinished() {
    }
}
