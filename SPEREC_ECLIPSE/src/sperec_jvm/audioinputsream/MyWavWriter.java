package sperec_jvm.audioinputsream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class MyWavWriter {

	    RandomAccessFile output;
	    MyAudioSpecs myAudioSpecs;
	    private int audioLen=0;
	    private  static final int HEADER_LENGTH=44;//byte


	    public MyWavWriter(MyAudioSpecs myAudioSpecs, String sOutAudioFullPath) throws FileNotFoundException {
		    this(myAudioSpecs, new RandomAccessFile(new File(sOutAudioFullPath), "rw"));
	    }
	    
	    public MyWavWriter(MyAudioSpecs myAudioSpecs, File fAudioOut) throws FileNotFoundException {
		    this(myAudioSpecs, new RandomAccessFile(fAudioOut, "rw"));
	    }
	    
	    public MyWavWriter(MyAudioSpecs myAudioSpecs, RandomAccessFile output) {
	        this.output=output;
	        this.myAudioSpecs=myAudioSpecs;
	        try {
	            output.write(new byte[HEADER_LENGTH]);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    
	    public boolean write(byte[] b) {
	        try {
	            audioLen+=b.length;
	            output.write(b);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return true;
	    }
	    
	    public void close() {
	        //write header and data to the result output
	        MyWavHeader waveHeader=new MyWavHeader(MyWavHeader.FORMAT_PCM,
	                (short)myAudioSpecs.getChannelCount(),
	                (int)myAudioSpecs.getSampleRate(), (short)16,audioLen);//16 is for pcm, Read WaveHeader class for more details
	        ByteArrayOutputStream header=new ByteArrayOutputStream();
	        try {
	            waveHeader.write(header);
	            output.seek(0);
	            output.write(header.toByteArray());
	            output.close();
	        }catch (IOException e){
	            e.printStackTrace();
	        }
	    }
	    
	    /*public boolean process(AudioEvent audioEvent) {
	        try {
	            audioLen+=audioEvent.getByteBuffer().length;
	            //write audio to the output
	            output.write(audioEvent.getByteBuffer());
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	        return true;
	    }*/

	    /*
	    public void processingFinished() {
	        //write header and data to the result output
	        WaveHeader waveHeader=new WaveHeader(WaveHeader.FORMAT_PCM,
	                (short)audioFormat.getChannels(),
	                (int)audioFormat.getSampleRate(),(short)16,audioLen);//16 is for pcm, Read WaveHeader class for more details
	        ByteArrayOutputStream header=new ByteArrayOutputStream();
	        try {
	            waveHeader.write(header);
	            output.seek(0);
	            output.write(header.toByteArray());
	            output.close();
	        }catch (IOException e){
	            e.printStackTrace();
	        }
	    }*/	
}
