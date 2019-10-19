package com.example.fs.sperec.specs.fea;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.mfcc.MFCC;

/**
 * Wrapper for Tarsos MFCC
 */

public class Wx_Tarsos_MFCC implements MyFeatureProducerInterface {

    private int requiredInputSize; // Samples
    private float [] smallChunckInputBuffer;
    private int smallChunckInputBufferCount;
    private MFCC tarsos_MFCC;

    private float [] mfccs; // The result in the specific format

    private MyFeatureProducerOutput myFeatureProducerOutput; // The result in the generic (wrapper) format

    /* ---------------------------------------------------------------------------------------------
     * Initialize the "core" of Tarsos MFCC
     * ---------------------------------------------------------------------------------------------
     */
    public Wx_Tarsos_MFCC(MyTarsosMFCC_Specs feaSpecs) {
        tarsos_MFCC = new MFCC(feaSpecs.samplesPerFrame,
                feaSpecs.sampleRate,
                feaSpecs.amountOfCepstrumCoef,
                feaSpecs.amountOfMelFilters,
                feaSpecs.lowerFilterFreq,
                feaSpecs.upperFilterFreq);

        myFeatureProducerOutput = new MyFeatureProducerOutput();

        requiredInputSize = feaSpecs.samplesPerFrame;
        smallChunckInputBuffer = new float[requiredInputSize];
        smallChunckInputBufferCount = 0;
    }

    /*----------------------------------------------------------------------------------------------
    compute (--> MFCC.process)
     */
    public void compute(MyFeatureProducerInput i) {
        AudioEvent audioEvent = i.audioEvent; // Estrae l'input specifico

        float [] newData = audioEvent.getFloatBuffer();
        int newDataSize = audioEvent.getBufferSize();

        if (newDataSize >0) {

            int totSamples = smallChunckInputBufferCount + newDataSize;

            if (totSamples<requiredInputSize) {
                System.arraycopy(newData, 0, smallChunckInputBuffer, smallChunckInputBufferCount, newDataSize);
                smallChunckInputBufferCount += newDataSize;
                mfccs = new float[0];

            } else if (smallChunckInputBufferCount>0) {
                float [] newBuffer = new float[totSamples] ;
                System.arraycopy(smallChunckInputBuffer, 0, newBuffer, 0, smallChunckInputBufferCount);
                int samples_now = requiredInputSize-smallChunckInputBufferCount;
                System.arraycopy(newData, 0, newBuffer, smallChunckInputBufferCount, samples_now);

                int samples_later = newDataSize-(requiredInputSize-smallChunckInputBufferCount);
                smallChunckInputBufferCount = samples_later;

                audioEvent.setFloatBuffer(newBuffer);
                tarsos_MFCC.process(audioEvent);
                mfccs = tarsos_MFCC.getMFCC();
            } else {
                tarsos_MFCC.process(audioEvent);
                mfccs = tarsos_MFCC.getMFCC();
            }
          } else {
            mfccs = new float[0];
        }

    }

    /*----------------------------------------------------------------------------------------------
     */
    public MyFeatureProducerOutput getResult() { // Incapsula il risultato nel formato generico
        myFeatureProducerOutput.features = mfccs;
        return myFeatureProducerOutput;
    }

    /*----------------------------------------------------------------------------------------------
     */
    public MyFeatureProducerOutput getResultClone() { // Incapsula il risultato nel formato generico
        return getResult(); // Non clona, in quanto il risultato è già un clone di quello cha sta in MFCC, uindi credo che non serva clonarlo di nuovo
    }
}
