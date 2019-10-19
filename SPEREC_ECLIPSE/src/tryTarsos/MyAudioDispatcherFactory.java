package tryTarsos;


import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import sperec_common.MyRingBuffer;
import sperec_common.tryTarsos_common.*;

public class MyAudioDispatcherFactory extends AudioDispatcherFactory {
	
	public static AudioDispatcher fromRingBuffer(MyRingBuffer ringBuffer, TarsosDSPAudioFormat format, final int audioBufferSize, final int bufferOverlap) {
        TarsosDSPAudioInputStream audioStream = new RingBufferAudioInputStream(ringBuffer, format);
        return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
    }
	
	//Sigona ottobre 2018, al fine di introdurre offset e len TODO
	/*public static AudioDispatcher fromFloatArrayWithOffset(final float[] floatArray, final int offset, int len, final int sampleRate, final int audioBufferSize, final int bufferOverlap) {
		final AudioFormat audioFormat = new AudioFormat(sampleRate, 16, 1, true, false);		
		final TarsosDSPAudioFloatConverter converter = TarsosDSPAudioFloatConverter.getConverter(JVMAudioInputStream.toTarsosDSPFormat(audioFormat));
		final byte[] byteArray = new byte[floatArray.length * audioFormat.getFrameSize()]; 
		converter.toByteArray(floatArray, offset, len, byteArray, 0);
		return AudioDispatcherFactory.fromByteArray(byteArray, audioFormat, audioBufferSize, bufferOverlap);
	}*/
	
	
	/*
    private String outDispatchedFileFullPath;
    private String resName;
    AudioDispatcher dispatcher;

    public AudioDispatcher fromAndroidResourceName(Context ctx, String resName, final int audioBufferSize, final int bufferOverlap) {
        AudioDispatcher audioDispatcher = null;
        MyAudioInfo aInfo = MyAudioDecoder.decode(ctx, resName);
        boolean err = false;
        String errMsg = null;

        if (null == aInfo) {
            err = true;
            errMsg = "Can not decode audio resource " + resName;
        }
        // Save the decoded file to a temporary wav file
        String outFileFullPath = aInfo.saveAudioSamplesAsRaw("temp", "audio_decoded.raw");
        try {
            InputStream stream = new FileInputStream(outFileFullPath);
            audioDispatcher = fromAndroidInputStream(stream, (int) aInfo.getSampleRate(), audioBufferSize, bufferOverlap);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            err = true;
            errMsg = "Can not open audio resource " + resName;
        }

        if (err) {
            Log.e("ERROR", errMsg);
        } else {
            this.resName = resName;
        }
        this.dispatcher = audioDispatcher;
        return audioDispatcher;
    }
    */

    //----------------------------------------------------------------------------------------------
    /*public static AudioDispatcher fromAndroidInputStream(InputStream stream, int targetSampleRate, final int audioBufferSize, final int bufferOverlap) {
        TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(targetSampleRate, 16, 1, true, false);
        TarsosDSPAudioInputStream audioStream = new UniversalAudioInputStream(stream, format);
        return new AudioDispatcher(audioStream, audioBufferSize, bufferOverlap);
    }


    // ********************************** DEBUG stuff **********************************************
    /----------------------------------------------------------------------------------------------
    public void setDebug() {
        // Try to write the dispatched audio
        boolean err = false;
        String errMsg = null;
        outDispatchedFileFullPath = getFullPath("temp", "dispatcher_out.wav");

        WriterProcessor wp = null;
        try {
            File f = new File(outDispatchedFileFullPath);
            if (f.exists()) {
                f.delete();
            }
            RandomAccessFile output = new RandomAccessFile(outDispatchedFileFullPath, "rw");
            wp = new WriterProcessor(dispatcher.getFormat(), output);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            err = true;
            errMsg = "Can not dispatch audio resource " + resName;
        }

        if (!err) {
            dispatcher.addAudioProcessor(wp);

            dispatcher.addAudioProcessor(new AudioProcessor() {

                @Override
                public void processingFinished() {
                    //Log.i("DEBUG", "processingFinished");

                    File file = new File(outDispatchedFileFullPath);
                    if (!file.exists()) {
                        //Log.e("DEBUG", "File not found " + outDispatchedFileFullPath);
                    } else {
                        //Log.i("DEBUG", "File found " + outDispatchedFileFullPath);
                        MyAudioInfo aInfoDebug = MyAudioDecoder.decode(outDispatchedFileFullPath);
                        new PostAudioFinalChunkTask().execute(aInfoDebug);
                    }
                }

                @Override
                public boolean process(AudioEvent audioEvent) {
                    return true;
                }
            });
        }
    }

    //----------------------------------------------------------------------------------------------
    private String getFullPath(String myFolder, String myFile) {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, myFolder);
        boolean mkOk = true;
        if (!file.exists()) {
            mkOk = file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + myFile);
    }

    //----------------------------------------------------------------------------------------------
    public class PostAudioFinalChunkTask extends AsyncTask<MyAudioInfo, Void, String> {
        private String response;

        @Override
        protected String doInBackground(MyAudioInfo... params) {
            MyAudioInfo aInfo = params[0];
            String response = SperecRemote.sendSaveAsWavRequest(aInfo, "postVad.wav");
            return response;
        }
    }*/
}
