package sperec_common;

//import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import myIO.MyWavFile;

/**
 * Created by FS on 11/10/2017.
 */

public class MyAudioInfo extends MyAudioSpecs {

    // Run time
    private double data_d [];
    private byte   data_b[];
    private long   numFrames;

    // Constructor
    public MyAudioInfo () {
        data_d = null;
        data_b = null;
        numFrames = 0;
    }

    // Constructor
    public MyAudioInfo (MyAudioInfo aInfo) {
        data_d = aInfo.data_d;
        data_b = aInfo.getDataBuffer();
        numFrames = aInfo.getNumFrames();
    }


    public byte [] getDataBuffer() {return data_b;}
    public long getNumFrames() {return numFrames;}

    public void setDataBuffer(byte [] buf) {data_b = buf;}
    public void setNumFrames(long numFrames) {this.numFrames = numFrames;}


    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }


    //----------------------------------------------------------
    public void setNewDataBuffer_b(int buflen) {
        data_b = new byte[buflen];
    }

    //----------------------------------------------------------
    public void display() {
        // For debug purpose
        super.display();
        System.out.println("data_b: " + data_b.length);
        System.out.println("number of frames: " + numFrames);
    }

    //----------------------------------------------------------
    public float [] getDataFloat() {
        float [] flo = null;

        if (null!=data_d) {
            flo = new float[data_d.length];
            for (int i = 0; i<flo.length-1; i++) {
                flo[i] = (float)data_d[i];
            }

        } else if (null!=data_b) {
            final ByteBuffer byteBuffer = ByteBuffer.wrap(data_b, 0, data_b.length).order(ByteOrder.LITTLE_ENDIAN);
            final ShortBuffer shorts = byteBuffer.asShortBuffer();
            flo = new float[shorts.capacity()];
            short shortMaxValue = Short.MAX_VALUE;
            for (int i = 0; i<flo.length-1; i++) {
                short raw = shorts.get(i);
                float amplitude = (float) raw / (float) shortMaxValue;
                flo[i] = amplitude;
            }
        }

        return flo;
    }

    //----------------------------------------------------------------------------------------------
    private String getFullPath(String myFolder, String myFile) {
        //String filepath = Environment.getExternalStorageDirectory().getPath();
        //File file = new File(filepath, myFolder);
        File file = new File(myFolder);
        boolean mkOk = true;
        if (!file.exists()) {
            mkOk = file.mkdirs();
        }
        return (file.getAbsolutePath() + "/" + myFile);
    }

    //----------------------------------------------------------------------------------------------
    public String saveAudioSamplesAsWaveFile(String folder, String filename) {
        String outFileFullPath = getFullPath(folder, filename);
        saveAudioSamplesAsWaveFile(outFileFullPath);
        return outFileFullPath;
    }

    //----------------------------------------------------------------------------------------------
    public String saveAudioSamplesAsRaw(String folder, String filename) {
        String outFileFullPath = getFullPath(folder, filename);
        try {
            FileOutputStream out = new FileOutputStream(outFileFullPath);
            out.write(this.data_b, 0, this.data_b.length);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFileFullPath;
    }
    //----------------------------------------------------------------------------------------------
    public void saveAudioSamplesAsWaveFile(String outFileFullPath) {
        int channels = 1;

        int byteRate = getByteRate();

        try {
            FileOutputStream out = new FileOutputStream(outFileFullPath);
            long totalAudioLen   = this.data_b.length;
            long totalDataLen    = totalAudioLen + 36;

            WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    getSampleRate(), channels, byteRate, getBytesPerSample());

            out.write(this.data_b);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    private void WriteWaveFileHeader(
            FileOutputStream out, long totalAudioLen,
            long totalDataLen, long longSampleRate, int channels,
            long byteRate, int bytesPerSample) throws IOException {

        byte[] header = new byte[44];

        header[0] = 'R'; // RIFF/WAVE header
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        header[12] = 'f'; // 'fmt ' chunk
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        header[16] = 16; // 4 bytes: size of 'fmt ' chunk
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        header[20] = 1; // format = 1
        header[21] = 0;
        header[22] = (byte) channels;
        header[23] = 0;
        header[24] = (byte) (longSampleRate & 0xff);
        header[25] = (byte) ((longSampleRate >> 8) & 0xff);
        header[26] = (byte) ((longSampleRate >> 16) & 0xff);
        header[27] = (byte) ((longSampleRate >> 24) & 0xff);
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        header[32] = (byte) (channels * 16 / 8); // block align //Sigona fixed
        header[33] = 0;
        header[34] = (byte)bytesPerSample; // bits per sample
        header[35] = 0;
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

        out.write(header, 0, 44);
    }


    /*public static MyAudioInfo getWavFrom(String filePathString) {
        MyAudioInfo aInfo = new MyAudioInfo();

        MyWavFile readWavFile = null;

        try {
            if (MyURL.isURL(filePathString)) {
                URL u = new URL(filePathString);
                InputStream is = u.openStream();
                readWavFile = MyWavFile.openWavFile(is);
            } else {
                readWavFile = MyWavFile.openWavFile(new File(filePathString));
            }
            aInfo = getWavFrom(readWavFile);

        }
        catch (Exception e)
        {

        }
        return aInfo;
    }*/

    public static MyAudioInfo getWavFrom(MyWavFile readWavFile) throws IOException, Exception {

        MyAudioInfo aInfo = new MyAudioInfo();
        aInfo.setEncodingStr("wav");
        aInfo.setOrigin("file");
        aInfo.setSampleRate((int)readWavFile.getSampleRate());
        aInfo.setChannelCount(readWavFile.getNumChannels());
        aInfo.setNumFrames(readWavFile.getNumFrames());
        aInfo.setBytesPerSample(readWavFile.getBytesPerSample());

        aInfo.setNewDataBuffer_b(readWavFile.getByteSize());
        long bytesRead = readWavFile.readBytes(aInfo.getDataBuffer(), aInfo.getDataBuffer().length);
        readWavFile.close();
        return aInfo;
    }

    private static MyAudioInfo getWavFrom(InputStream inputStream)  {
        MyAudioInfo aInfo = null;
        try {
            MyWavFile readWavFile = MyWavFile.openWavFile(inputStream);
            aInfo = getWavFrom(readWavFile);
        } catch (IOException e) {
        } catch (Exception e) {

        }
        return aInfo;
    }

    public static MyAudioInfo getFrom(InputStream inputStream, String ext1)  {
        MyAudioInfo aInfo = null;
        switch(ext1) {

            case "wav": // 	System.out.println("IT IS A WAV");
                aInfo = getWavFrom(inputStream);
                break;

            default:
                System.out.println("ERROR: Unsupported audio: " + ext1);
        }

        return aInfo;
    }

    public static MyAudioInfo getFrom(String filePathString) throws FileNotFoundException {
        String ext1 = filePathString.substring(filePathString.lastIndexOf('.') + 1, filePathString.length());
        FileInputStream iStream = new FileInputStream(new File(filePathString));
        return getFrom(iStream, ext1);
    }




}