package com.example.fs.sperec.myTarsosInterface;

import com.example.fs.sperec.specs.fea.MyFeatureProducer;
import com.example.fs.sperec.specs.fea.MyFeatureProducerInput;
import com.example.fs.sperec.specs.fea.MyFeatureProducerOutput;
import com.example.fs.sperec.specs.fea.MyFeatureSpecs;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 * Serve a incapsulare la mia clase MyFeatureProducer (non esattamente l'MFCC di Tarsos) nel framework TarsosDSP
 */
public class MyFeature_TarsosAudioProcessor implements AudioProcessor {

    private MyFeatureProducer myFeatureProducer;
    private MyFeatureProducerInput currentInput;
    private MyFeatureProducerOutput currentOutput;


     public MyFeature_TarsosAudioProcessor(MyFeatureSpecs feaSpecs) {
         myFeatureProducer = new MyFeatureProducer(feaSpecs); // Incapsulamento
         currentInput = new MyFeatureProducerInput();
     }

        @Override
        public boolean process(AudioEvent audioEvent) {
        /* TarsosDSP prevede che il "processor" faccia il suo lavoro sui dati in AudioEvent, sostuendo
        * il dati risultanti a quelli iniziali
        * */
            currentInput.audioEvent = audioEvent; // Gira AudioEvent sotto forma di input per myFeatureProducer
            myFeatureProducer.compute(currentInput);
            currentOutput = myFeatureProducer.getResult();
            return true;
        }

        @Override
        public void processingFinished() {

        }

    }

