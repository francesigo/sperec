package com.example.fs.sperec.specs;

import sperec_common.MyAudioSpecs;
import com.example.fs.sperec.specs.fea.MyFeatureSpecs;

/**
 * Created by FS on 18/10/2017.
 */

public class SPEREC_Specs {

    // Specifications for AUDIO
    public MyAudioSpecs myAudioSpecs;

    // Specifications for FEATURES
    public MyFeatureSpecs myFeaSpecs;


    /*
    POPOLAZIONE DI RIFERIMENTO
        - Numerosità popolazione
        - Durata audio dei parlanti nella popolazione
        GMM_UBM:
            - nmix        : number of Gaussian components (must be a power of 2)
            - final_iter  : number of EM iterations in the final split              (e.g. 10)
            - ds_factor   : feature sub-sampling factor (every ds_factor frame)     (e.g. 1)

    BAUM-WELCH (no parametri)

    I-VECTOR
        - tv_dim (400)     : dimensionality of the total variability subspace
        - niter (5)      : number of EM iterations for total subspace learning

    GPLDA
    - lda_dim (200)
    - nphi (200) : dimensionality of the Eigenvoice subspace
    - niter (10) : number of EM iterations for learning PLDA model

    INOLTRE:
        - durata audio parlante arruolato
        - durata audio parlante anonimo

*/

/*
    MyAudioSpecs:
        int    sampleRate;   // Like android.media.AudioFormat:
        int    channelCount; // Like android.media.AudioFormat:
        String codec;     // Like android.media.AudioFormat:
        int    bytesPerSample; // Deriva in realtà dall'encoding
        String origin;

    MyFeatureSpecs:
        String type;
        int count;
            (MFCC):
 */

}