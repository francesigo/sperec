package sperec_jvm.audioinputsream;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class MyAudioInputStreamFromFile implements IMyAudioInputStream {
	
	private AudioInputStream stream;
	private MyAudioSpecs myAudioSpecs;
	
	public MyAudioInputStreamFromFile(File audioFile) throws UnsupportedAudioFileException, IOException {
		this.stream = AudioSystem.getAudioInputStream(audioFile);
		this.myAudioSpecs = MyAudioInputStreamFromFile.toMyAudioSpecs(this.stream.getFormat());
	}
	
	public MyAudioInputStreamFromFile(String sAudioFileFullPath) throws UnsupportedAudioFileException, IOException {
		this(new File(sAudioFileFullPath));
	}
	
	@Override
	public long skip(long bytesToSkip) throws IOException {
		return stream.skip(bytesToSkip);
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return stream.read(b, off, len);
	}

	@Override
	public void close() throws IOException {
		stream.close();
	}

	@Override
	public MyAudioSpecs getAudioSpecs() {
		return myAudioSpecs;
	}
	
	public static MyAudioSpecs toMyAudioSpecs(AudioFormat format) {
		MyAudioSpecs myAudioSpecs = new MyAudioSpecs();
		myAudioSpecs.setChannelCount(format.getChannels());
		myAudioSpecs.setSampleRate((int) format.getSampleRate());
		myAudioSpecs.setBytesPerSample(format.getSampleSizeInBits()/8);
		
		return myAudioSpecs;
	}
}
