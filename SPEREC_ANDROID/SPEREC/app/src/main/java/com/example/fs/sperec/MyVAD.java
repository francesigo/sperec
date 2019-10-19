package com.example.fs.sperec;

import com.example.fs.sperec.externClasses.VoiceActivityDetector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sperec_common.MyAudioInfo;


public class MyVAD {

    private String MyVAD_ID = "VoiceActivityDetector"; //Default
    private long currentFrameNumber = 0;
    private ByteArrayOutputStream voicedSamples_b = null;
    private boolean voiceDetected;
    private boolean isVoicedFrame;
    private boolean ready = false;
    private VoiceActivityDetector vad = null;


    public MyVAD(String vadType) {

        if (null != vadType) {
            this.MyVAD_ID = vadType;
        }

        switch (this.MyVAD_ID) {
            case "VoiceActivityDetector":
                break;

            default:
                System.out.println("ERROR: Unsupported VAD type: " + this.MyVAD_ID);
                this.MyVAD_ID = null;
        }
    }

    //----------------------------------------------------------------------------------------------
    private void init_VoiceActivityDetector(int sampleRate) {

        vad = new VoiceActivityDetector(sampleRate);

        vad.setSpeechListener(new VoiceActivityDetector.SpeechEventsListener() {
            @Override
            public void onSpeechBegin() {
                // TODO speechStarted = true;
                isVoicedFrame = true;
                System.out.println("speechStarted at frame n. " + currentFrameNumber);
            }

            @Override
            public void onSpeechCancel() {
                isVoicedFrame = false;
            }

            @Override
            public void onSpeechEnd() {
                voiceDetected = true;
                isVoicedFrame = false;
                System.out.println(" --- speech End    at frame n. " + currentFrameNumber);
            }
         });
    }


    //----------------------------------------------------------------------------------------------
    public byte[] processFile(MyAudioInfo aInfo) {
        return processFile(aInfo.getDataBuffer(), (int)aInfo.getSampleRate());
    }

    //----------------------------------------------------------------------------------------------
    /* Entry point: esegue VAD sul buffer data_b
     */
    public byte[] processFile(byte [] data_b, int sampleRate) {
        byte [] out_data_b = null;

        /* Esegue l'iniziailizzazione del VoiceActivityDetector all'inizio, una volta per tutte, dopo
         * aver conosciuto il sampleRate.
         * Se la stessa istanza di MyVAD viene usata per chiamare questa "process", l'inizializzazione
         * non viene appunto svolta. In questo modo posso usare la stessa istanza di MyVAD per
         * elaborare più di un beffer (p.e. overlapped) con altrettante chiamate alla process.
         */
        if (!ready) {
            init_VoiceActivityDetector(sampleRate);
            ready = true;
        }

        int frameSize_b  = VoiceActivityDetector.FRAME_SIZE_IN_BYTES;

        byte[] currentFrameBuffer_b = new byte[frameSize_b]; // Alloca memoria per un singolo frame

        long currentOffsetBytes   = 0; // Puntatore alla memoria dei dati del frame da copiare
        while(currentOffsetBytes<data_b.length) {
            long remainingBytes = data_b.length - currentOffsetBytes;
            int bytesToRead = (int) Math.min(frameSize_b, remainingBytes);
            System.arraycopy(data_b, (int)currentOffsetBytes, currentFrameBuffer_b, 0, bytesToRead); // Copio il frame
            vad.processBuffer(currentFrameBuffer_b, bytesToRead); // Lo consegno al vero VAD
            if (isVoicedFrame) {
                outputVoicedFrame(currentFrameBuffer_b, bytesToRead);
            }
            currentOffsetBytes+=bytesToRead;
        }


        if (null!=voicedSamples_b) {
            out_data_b = voicedSamples_b.toByteArray();
            voicedSamples_b_close();
        }

        System.out.println(" END");

        return out_data_b;

    }


    //----------------------------------------------------------------------------------------------
    public byte[] processSingleFrame(MyAudioInfo aInfo) {
        return processFile(aInfo.getDataBuffer(), (int)aInfo.getSampleRate());
    }

    //----------------------------------------------------------------------------------------------
    /* Entry point: esegue VAD sul buffer data_b
     */
    public byte[] processSingleFrame(byte [] data_b, int sampleRate) {
        byte [] out_data_b;

        /* Esegue l'iniziailizzazione del VoiceActivityDetector all'inizio, una volta per tutte, dopo
         * aver conosciuto il sampleRate.
         * Se la stessa istanza di MyVAD viene usata per chiamare questa "process", l'inizializzazione
         * non viene appunto svolta. In questo modo posso usare la stessa istanza di MyVAD per
         * elaborare più di un beffer (p.e. overlapped) con altrettante chiamate alla process.
         */
        if (!ready) {
            init_VoiceActivityDetector(sampleRate);
            ready = true;
        }

        vad.processBuffer(data_b, data_b.length); // Lo consegno al vero VAD, che tramite il listener può aggiornare la variabile isVoicedFrame

        if (isVoicedFrame) {
            out_data_b = data_b;
        } else {
            out_data_b = null;
        }

        System.out.println(" END");

        return out_data_b;

    }

    //-------------------------------------------------------------------------------
    private void voicedSamples_b_close() {
        if (null!=voicedSamples_b) {
            try {
                voicedSamples_b.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
            voicedSamples_b = null;
        }
    }
    //------------------------------------------------------------------------------
    private void outputVoicedFrame(byte [] buf, int len) {
        if (null==voicedSamples_b) {
            voicedSamples_b = new ByteArrayOutputStream(len); //len can be less than buf.length
        }
        voicedSamples_b.write(buf, 0, len);
    }
}
