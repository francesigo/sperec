package com.example.fs.sperec;

import android.content.Context;

import java.io.File;
import java.io.InputStream;

import sperec_common.SPEREC_Loader;

public class SPEREC_Loader_Android extends SPEREC_Loader {

    MainActivity ma = null;
    Context ctx = null;
    String defType = "";
    String packageName = "";

    public void init(MainActivity ma, String defType) {
        this.ma = ma;
        this.ctx = ma.getApplicationContext();
        this.packageName = ma.getPackageName();
        this.defType = defType;
    }

    /**
     * THIS WAS ABSTRACT IN THE UPPER CLASS
     * @param resource
     * @return
     * @throws Exception
     */
    public InputStream getInputStream(String resource) throws Exception {

        String resourceName = filename2AndroidResourceName(resource);

        int  resId = ctx.getResources().getIdentifier(resourceName, defType, packageName);
        if (resId==0) {
            String msg = "INTERNAL ERROR. Cannot find the specified resource : " + resourceName;
            throw new Exception(msg);
        }

        InputStream is = ctx.getResources().openRawResource(resId);
        return is;
    }

    /**
     *
     * @param resource
     * @return
     * @throws Exception
     */
    public InputStream getInputStreamDeep(String resource, String parentFolder) throws Exception {
        return getInputStream(resource);
    }
}
