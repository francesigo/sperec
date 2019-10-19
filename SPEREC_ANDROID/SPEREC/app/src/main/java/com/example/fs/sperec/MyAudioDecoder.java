package com.example.fs.sperec;

import android.content.Context;
import android.util.TypedValue;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import sperec_common.MyAudioInfo;
import sperec_common.MyURL;


public class MyAudioDecoder {

    //--------------------------------------------------------------------------
    public static MyAudioInfo decode(Context ctx, String resName)  {
        // Find the resource
        int resId = ctx.getResources().getIdentifier(resName, "raw", ctx.getPackageName());

        TypedValue value = new TypedValue();
        ctx.getResources().getValue(resId, value, true);
        String resFullName = value.string.toString(); //.substring(13, value.string.toString().length());
        String ext1 = resFullName.substring(resFullName.lastIndexOf('.')+1, resFullName.length());

        InputStream inputStream = ctx.getResources().openRawResource(resId);

        MyAudioInfo aInfo = MyAudioInfo.getFrom(inputStream, ext1);
        return aInfo;
    }

    /*
    //--------------------------------------------------------------------------
    public static MyAudioInfo decode(Context ctx, int resId)  {
        TypedValue value = new TypedValue();
        ctx.getResources().getValue(resId, value, true);
        String resname = value.string.toString(); //.substring(13, value.string.toString().length());
        int i = resname.lastIndexOf('.');
        String ext1 = resname.substring(i+1, resname.length());

        InputStream inputStream = ctx.getResources().openRawResource(resId);

        MyAudioInfo aInfo = decode(inputStream, ext1);
        return aInfo;
    }
    */

    //--------------------------------------------------------------------------
    /*private static MyAudioInfo decode(InputStream inputStream, String ext1)  {
        MyAudioInfo aInfo = null;
        switch(ext1) {

            case "wav": // 	System.out.println("IT IS A WAV");
                aInfo = decodeWav(inputStream);
                break;

            default:
                System.out.println("ERROR: Unsupported audio: " + ext1);
        }

        return aInfo;
    }*/
    //--------------------------------------------------------------------------

    /*public static MyAudioInfo decode(String filePathString) { //throws IOException, WavFileException  {
        MyAudioInfo aInfo = null;
        File f = new File(filePathString);
        if(f.exists() && !f.isDirectory()) {
            String ext1 = filePathString.substring(filePathString.lastIndexOf(".")+1); // System.out.println("ext1: " + ext1);
            switch(ext1) {

                case "wav": // 	System.out.println("IT IS A WAV");
                    aInfo = MyAudioInfo.getWavFrom(filePathString);
                    break;

                default:
                    System.out.println("ERROR: Unsupported audio file: " + ext1);
            }

        } else {
            System.out.println("ERROR: File not found: " + filePathString);
        }
        return aInfo;
    }
    */


    //--------------------------------------------------------------------------
    /*private static MyAudioInfo decodeWav(InputStream inputStream)  {
        MyAudioInfo aInfo = null;
        try {
            MyWavFile readWavFile = MyWavFile.openWavFile(inputStream);
            aInfo = decodeWavHlp(readWavFile);
        } catch (IOException e) {
        } catch (WavFileException e) {

        }
        return aInfo;
    }*/
    //--------------------------------------------------------------------------
    /*private static MyAudioInfo decodeWavHlp(MyWavFile readWavFile) throws IOException, WavFileException {

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
    }*/

    //--------------------------------------------------------------------------
    /*private static MyAudioInfo decodeWav(String filePathString) {
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
            aInfo = decodeWavHlp(readWavFile);

        }
        catch (Exception e)
        {

        }
        return aInfo;
    }*/
}
