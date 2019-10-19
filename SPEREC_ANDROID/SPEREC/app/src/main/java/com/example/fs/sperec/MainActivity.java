package com.example.fs.sperec;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import sperec_common.SPEREC;
import sperec_common.SPEREC_Factory;
import sperec_common.SimpleSpeakerIdentity;
import sperec_common.SpeakerModel_IVECTOR;
import sperec_common.Specs;
import sperec_common.Users;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

public class MainActivity extends AppCompatActivity { //NavDrawerActivity {

    SPEREC oSperec = null;

    // GUI
    Button btnProfiling = null;
    Button btnEnroll = null;
    Button btnTest = null;
    TextView logBar = null;
    MyLog MLog = null;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the main_menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.item1:
                MLog.i("Selected item 1");
                break;
            case R.id.item2:
                MLog.i("Selected item 2");
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // initiates all the nav drawer stuff from the BaseNavigationDrawerActivity
        if(savedInstanceState == null){
            //MLog.i("onCreate() savedInstanceState == null");
            helper();// normal fragment stuff here e.g. getSupportFragmentManager commit, add, replace etc
        } else {
            //MLog.i("onCreate() savedInstanceState != null");
            // fragment stuff here e.g. find fragment by tag, id whatever
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID)
    {
        super.setContentView(layoutResID);
        //onCreateDrawer() ;
    }

    protected void helper () {

        setContentView(R.layout.activity_main);
        //setContentView(R.layout.my_drawer_layout);
        // Reset the GUI
        setGUI();

        // Check the permissions for this app
        new PermissionManager(this).checkPermission();

        // Initialize the Engine and the Enrolled speakers database:
        if (null != (oSperec = initSperecEngine(this, "popref", "raw")) ) {
            // Inizializza il database degli enrolled speakers
            Users enrolledSpeakerDatabase = initEnrolledSpeakersDatabase(this.getApplicationContext());

            // Fornisce il database degli enrolled speakers all'oggetto oSperec
            oSperec.setEnrolledSpeakersDatabase(enrolledSpeakerDatabase);
        }

        // Se l'inizializzazione non ha avuto successo, disattiva l'interfaccia utente
        if  ((oSperec==null) || (oSperec.getEnrolledSpeakersDatabase()==null)) {
            // Disattiva i bottoni
            setEnabledGUI(false);

        } else {
           // Attiva l'interfaccia utente (GUI)
           setEnabledGUI(true);
           MLog.i("SPEREC DEMO IS NOW READY");

           // Fai qualche test diagnostico
           //SelfTest selfTest = new SelfTest(this.getApplicationContext(), oSperec);
           //selfTest.test_enroll(); // PASSED
           // selfTest.testSperecScoring(); // OK funziona ottobre 2018
           //selfTest.test_authentication(); // OK, funziona ottobre 2018
        }
    }

    /**
     * Reset the GUI
     */
    void setGUI() {

        // Set the logbar
        logBar = findViewById(R.id.statusBar);
        logBar.setText("");

        // Set my logger
        MLog = new MyLog("SPEREC_MAIN_ACTIVITY", null);
        MLog.setLogDestinationTextView(logBar);

        // Set the buttons and the actions
        setEnrollButton();
        setTestButton();
        setProfilingButton();

        // Welcome message
        MLog.i("WELCOME TO SPEREC DEMO");
    }

    void setProfilingButton() {
        btnProfiling = (Button) findViewById(R.id.buttonProfiling);
        btnProfiling.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i=new Intent(MainActivity.this , ProfilingActivity.class);
                        // Integra in un unico oggetto
                        SperecAndroidWrapper SAW = new SperecAndroidWrapper(oSperec, MainActivity.this);
                        // Lo passa all'activity successiva
                        EventBus.getDefault().postSticky(SAW);
                        startActivity(i);
                    }
                });
            }
        });
    }

    /**
     * Setup the button ENROLL
     */
    void setEnrollButton() {
        btnEnroll = (Button) findViewById(R.id.btnEnroll);
        btnEnroll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i=new Intent(MainActivity.this , EnrollActivity.class);
                        // Integra in un unico oggetto
                        SperecAndroidWrapper SAW = new SperecAndroidWrapper(oSperec, MainActivity.this);
                        // Lo passa all'activity successiva
                        EventBus.getDefault().postSticky(SAW);
                        startActivity(i);
                    }
                });
            }
        });
    }

    /**
     * Setup the button TEST/LOGIN
     */
    void setTestButton () {
        btnTest = (Button) findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Intent i = new Intent(MainActivity.this, TestActivity.class);
                        // Integra in un unico oggetto
                        SperecAndroidWrapper SAW = new SperecAndroidWrapper(oSperec, MainActivity.this);
                        // Lo passa all'activity successiva
                        EventBus.getDefault().postSticky(SAW);
                        startActivity(i);
                    }
                });
            }
        });
    }

    /**
     *
     * @param state
     */
    void setEnabledGUI(boolean state) {
        btnEnroll.setEnabled(state);
        btnTest.setEnabled(state);
    }

    /**
     *
     * @param ma:                   a reference to the MainActivity
     * @param defType:              the type of resource (raw)
     * @return
     */
    //private static SPEREC initSperecEngine(MainActivity ma, String popref_cfg_resource_name, String popref_resource_name, String defType) {
    private SPEREC initSperecEngine(MainActivity ma, String popref_cfg_resource_name, String defType) {

        SPEREC oSperec = null;

        // Instanzia e inizializza il loader
        SPEREC_Loader_Android sperecLoader = new SPEREC_Loader_Android();
        sperecLoader.init(ma, defType);

        // Instanzia la factory e la passa al loader
        SPEREC_Factory factory = new SPEREC_Factory();
        sperecLoader.setSperecFactory(factory);

        // Crea e configura l'engine tramite il loader
        try {
            oSperec = sperecLoader.load(popref_cfg_resource_name);
        } catch (Exception e) {
            MLog.e(e.getMessage(), e);
            oSperec = null;
        }
        return oSperec;
    }

    /**
     * Inizializza il database degli enrolled speakers
     * @param ctx
     * @return
     */
    private Users initEnrolledSpeakersDatabase(Context ctx) {

        boolean ok = true;
        String baseFolder = "";
        Users enrolledSpeakerDatabase = null;

        // Controlla se già esiste la cartella per gli enrolled speakers....
        File f = new File(ctx.getFilesDir(), "enrolledSpeakers");

        // Se non esiste, cerca di crearla
        if (!f.exists())
            if (! (ok=f.mkdirs() ) )
                MLog.e("INTERNAL ERROR: Cannot create a directory for enrolled speakers", null);


        if (ok) {
            baseFolder = f.getAbsolutePath();

            // Crea o carica il database degli enrolled speakers
            try {
                enrolledSpeakerDatabase = new Users(new SimpleSpeakerIdentity("dummy"),
                        new SpeakerModel_IVECTOR(null),
                        baseFolder);
            } catch (Exception e) {
                MLog.e("INTERNAL ERROR: Cannot instantiate a database for enrolled speakers", e);
            }
        }

        return enrolledSpeakerDatabase;
    }
}


/*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        */


        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/