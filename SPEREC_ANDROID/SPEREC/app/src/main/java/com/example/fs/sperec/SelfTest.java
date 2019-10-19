package com.example.fs.sperec;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.Arrays;

import be.tarsos.dsp.AudioDispatcher;
import sperec_common.AuthenticationResult;
import sperec_common.IEnrolledSpeakersDatabase;
import sperec_common.SPEREC;
import sperec_common.SimpleSpeakerIdentity;
import sperec_common.SpeakerIdentity;
import tryTarsos.MyAudioDispatcherFactory;

public class SelfTest {

    Context ctx = null;
    String TAG = "SelfTest";
    SPEREC oSperec = null;


    public SelfTest(Context ctx, SPEREC oSperec) {
        this.ctx = ctx;
        this.oSperec = oSperec;
    }


    // TEST
    public void test_authentication() {

        // Try to authentica a user which was not enrolled, from pre-recorded speech
        test_authentication_hlp("abook3", null); // Should fail

        // Try to authenticate an enrolled user, from pre-recorded speech
        test_authentication_hlp("abook2", "abook_002_test");
    }

    /**
     *
     * @param userName
     * @param resource
     */
    public void test_authentication_hlp(String userName, String resource) {
        String msg ="";

        IEnrolledSpeakersDatabase users = oSperec.getEnrolledSpeakersDatabase();
        SpeakerIdentity claimedId = users.getSpeakerIdentityByUserName(userName);

        if (claimedId==null) {
            msg = "Unknown user: " + userName;
            Log.i(TAG, msg);
        }
        else {
            try {
                AudioDispatcher mainAudioDispatcher = MyAudioDispatcherFactory.fromAndroidResourceName(ctx, resource, 2048, 0);
                AuthenticationResult res = oSperec.authenticateFromSpeech(mainAudioDispatcher, claimedId);
                msg = res.toString();
                Log.i(TAG, msg);
            } catch (Exception e) {
                msg = "INTERNAL ERROR: cannot perform authentication: " + e.getMessage();
                Log.i(TAG, msg);
            }
        }
    }


    /**
     * Simulate an enrollment, taking the speech from a file
     */
    public void test_enroll() {

        AudioDispatcher mainAudioDispatcher = null;
        String spkResName = "abook_002_enroll";
        String userName = "abook2";
        String usernames [] = null;

        int sampleRate = 11025;
        mainAudioDispatcher = MyAudioDispatcherFactory.fromAndroidResourceName(ctx, spkResName, 2048, 0);

        IEnrolledSpeakersDatabase users = oSperec.getEnrolledSpeakersDatabase();
        usernames = users.getSpeakerIdentitiesStrings();
        if (null==usernames)  {
            Log.i(TAG, "No enrolled speakers");
        } else {
            Log.i(TAG, "Enrolled speakers:" + Arrays.toString(usernames));
        }

        try {
            SimpleSpeakerIdentity speakerIdentity = new SimpleSpeakerIdentity(userName);
            oSperec.enrollFromSpeech(mainAudioDispatcher, speakerIdentity); // Output not required
            String msg = "SPEREC.enrollFromSpeech return";
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            Log.i(TAG, msg);
            usernames = users.getSpeakerIdentitiesStrings();
            if (null==usernames)  {
                Log.i(TAG, "No enrolled speakers");
            } else {
                Log.i(TAG, "Enrolled speakers:");
                Log.i(TAG, Arrays.toString(usernames));
            }
            if (users.hasIdentity(speakerIdentity)) {
                Log.i(TAG,"ENROLLMENT TEST SUCCESSFUL");
            } else {
                Log.i(TAG,"ENROLLMENT FAILED");
            }
        } catch (Exception e) {
            e.printStackTrace();
            String msg = "INTERNAL ERROR: Can not enroll the speaker";
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
            Log.e(TAG, msg);
        }
    }


    /**
     * Direct comparison between two pre-recorded speech
     */
    public void testSperecScoring() {

        AuthenticationResult res = new AuthenticationResult(true);
        String msg = "";

        String spkResName = "abook_002_enroll";
        String spkTestResName = "abook_002_test";

        boolean ok = (null!=oSperec);
        if (ok) {
            AudioDispatcher a1 = MyAudioDispatcherFactory.fromAndroidResourceName(ctx, spkResName, 2048, 0);
            AudioDispatcher a2 = MyAudioDispatcherFactory.fromAndroidResourceName(ctx, spkTestResName, 2048, 0);
            try {
                res = oSperec.compareSpeakers(a1, a2);
                System.out.println(res.msg);
                System.out.println("SCORE = " + res.score);
            } catch (Exception e) {
                e.printStackTrace();
                msg ="INTERNAL ERROR (test for debug): Can not compare speakers." + e.getMessage();
                Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
                Log.e(TAG, msg);
            }
        }

        System.out.println("DONE");
    }
}
