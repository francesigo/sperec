package com.example.fs.sperec;

import com.example.fs.sperec.externClasses.VoiceActivityDetector;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;

/**
 * Serve a incapsulare la mia clase MyVad nel framework TarsosDSP
 */

public class MyVAD_TarsosAudioProcessor implements AudioProcessor{

    private MyVAD myVad = null;

    private final TarsosDSPAudioFloatConverter converter;

    public MyVAD_TarsosAudioProcessor(String VAD_TYPE, TarsosDSPAudioFormat format) {
        myVad = new MyVAD(VAD_TYPE);
        converter = TarsosDSPAudioFloatConverter.getConverter(format);
    }


    @Override
    public boolean process(AudioEvent audioEvent) {
        /* TarsosDSP prevede che il "processor" faccia il suo lavoro sui dati in AudioEvent, sostuendo
        * il dati risultatni a quelli iniziali
        * */

        byte [] in_data_b;
        byte [] out_data_b;
        float [] out_data_f = new float[0];

        // I dati mi servono in byte per farli processare dal mio Vad, che ritorna sempre in byte
        in_data_b = audioEvent.getByteBuffer(); // Qui dentro c'è una coversione da float a byte... un po' inefficiente ...

        // Esegue la mia VAD
        out_data_b = myVad.processFile(in_data_b, (int)audioEvent.getSampleRate());
        // out_data_b = myVad.processSingleFrame (in_data_b, (int)audioEvent.getSampleRate()); //DOES NOT WORK WITH VoiceActivityDetector due to private

        if (null!=out_data_b) {
            // Devo quindi convertire da byte a float
            int frameSize = in_data_b.length / audioEvent.getFloatBuffer().length;
            out_data_f = new float[out_data_b.length / frameSize];
            converter.toFloatArray(out_data_b, out_data_f);
        }

        // Salva in AudioEvent i nuovi dati, che saranno poi passati al "Processor" seguente nella catena
        audioEvent.setFloatBuffer(out_data_f);
        return true;
    }

    @Override
    public void processingFinished() {

    }


}
