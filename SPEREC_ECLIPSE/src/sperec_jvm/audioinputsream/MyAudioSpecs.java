package sperec_jvm.audioinputsream;


/**
 * Created by FS on 18/10/2017.
 *
 * DO NOT USE AUDIOFORMAT BECAUSE I CANNOT SET ITS PROPERTIES
 */

public class MyAudioSpecs {

    // **************************** PROPERTIES START

    private int    sampleRate;   // Like android.media.AudioFormat:
    private int    channelCount; // Like android.media.AudioFormat:
    private String codec;     // Like android.media.AudioFormat:
    private int    bytesPerSample; // Deriva in realtà dall'encoding
    private String origin;

    // **************************** PROPERTIES END


    public int getSampleRate()   {return sampleRate;}
    public int getChannelCount() {return channelCount;}
    public String getEncodingStr()     {return codec;}
    public String getOrigin() {return origin;}
    public int getBytesPerSample() {return bytesPerSample;}


    public void setSampleRate(int sampleRate)   {this.sampleRate = sampleRate;}
    public void setChannelCount(int channelCount) {this.channelCount = channelCount;}
    public void setEncodingStr(String codec)  {this.codec = codec;}
    public void setOrigin(String origin) {this.origin = origin;}
    public void setBytesPerSample(int bytesPerSample) {this.bytesPerSample = bytesPerSample;}



    //private void setOrigin(String origin) {this.origin = origin;}
    public int getByteRate() {return getBytesPerSample() * getSampleRate() * getChannelCount();};


    // Constructor
    public MyAudioSpecs() {
        sampleRate = 0;
        channelCount = 0;
        codec = "";
        bytesPerSample = 0;
        origin = "";
    }

    // Constructor
    public MyAudioSpecs(MyAudioSpecs s) {
        sampleRate = s.sampleRate;
        channelCount = s.channelCount;
        codec = s.codec;

        bytesPerSample = s.bytesPerSample;
        origin = s.origin;
    }

    //----------------------------------------------------------------------------------------------
    public void display() {
        // For debug purpose
        System.out.println("sample rate: " + getSampleRate());
        System.out.println("number of channels: " + getChannelCount());
        System.out.println("codec: " + getEncodingStr());

        System.out.println("origin: " + origin);
        System.out.println("bytesPerSample: " + bytesPerSample);
    }
}

