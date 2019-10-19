package com.example.fs.sperec;

import android.app.Activity;
import sperec_common.SPEREC;


/**
 * Created by FS on 25/12/2017.
 */

public class SperecAndroidWrapper {

    public Activity mainActivity = null;
    public SPEREC oSperec = null;

    //public SPEREC_Specs sperecSpecs = null;
    //public String dataSource = "";
    //public static String TAG = "SperecAndroidWrapper";


    public SperecAndroidWrapper(SPEREC oSperec, Activity ma) { // Ottobre 2018
        this.oSperec = oSperec;
        this.mainActivity = ma;
    }

}
