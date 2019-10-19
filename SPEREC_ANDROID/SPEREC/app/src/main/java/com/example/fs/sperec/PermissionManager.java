package com.example.fs.sperec;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Created by FS on 27/12/2017.
 */

public class PermissionManager {

    public  static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    Context ctx = null;
    Activity A = null;


    public PermissionManager(Activity A) {
        this.A = A;
        this.ctx = A.getApplicationContext();
    }



    public boolean checkPermission() {
        boolean ret = false;
        if (ActivityCompat.checkSelfPermission(ctx, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ActivityCompat.checkSelfPermission(ctx, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale (A, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale (A, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale (A, Manifest.permission.RECORD_AUDIO)) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("Permission to access the microphone and the local storage are required for this app.")
                        .setTitle("Permission required");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int id) {
                        //Log.i(TAG, "Clicked");
                        makeRequestAll();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

            } else {
                makeRequestAll();
            }
        } else {
            ret = true;// write your logic code if permission already granted
        }
        return ret;
    }

    /**
     *
     */
    protected void makeRequestAll() {
        ActivityCompat.requestPermissions(A,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                PERMISSIONS_MULTIPLE_REQUEST);
    }

    //@Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalFile = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean microphonePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;

                    if(microphonePermission && readExternalFile && writeExternalFile)
                    {
                        checkPermission();
                        /*
                        try {
                            init_activity();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        */

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(A);
                        builder.setMessage("Permission to access the microphone and the local storage are required for this app.")
                                .setTitle("Permission required");
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int id) {
                                //Log.i(TAG, "Clicked");
                                makeRequestAll();
                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                }
                break;
        }
    }
}
