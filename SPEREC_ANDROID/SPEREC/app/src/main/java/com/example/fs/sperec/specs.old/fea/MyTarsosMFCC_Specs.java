package com.example.fs.sperec.specs.fea;

/**
 * Created by FS on 18/10/2017.
 */

public class MyTarsosMFCC_Specs implements MyFeatureSpecs {

    public int samplesPerFrame = 0;
    public float sampleRate = 0;
    public int amountOfCepstrumCoef = 0;
    public int amountOfMelFilters = 0;
    public float lowerFilterFreq = 0;
    public float upperFilterFreq = 0;

    // Interface implementation
    public int    getCount()    { return amountOfCepstrumCoef; }
    public String getBaseType() { return "MFCC_Tarsos"; }
    public String getFullType() { return ""+getCount()+getBaseType(); }

    public void setDefaults() {
        samplesPerFrame = 1024;
        sampleRate = 0;
        amountOfCepstrumCoef = 40;
        amountOfMelFilters = 50;
        lowerFilterFreq = 300;
        upperFilterFreq = 3000;
    }

    public MyTarsosMFCC_Specs() {
        setDefaults();
    }

}
