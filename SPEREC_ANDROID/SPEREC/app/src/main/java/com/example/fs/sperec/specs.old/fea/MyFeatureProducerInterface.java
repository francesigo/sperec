package com.example.fs.sperec.specs.fea;

/**
 * Created by FS on 19/10/2017.
 */

public interface MyFeatureProducerInterface {
    void compute(MyFeatureProducerInput i);
    MyFeatureProducerOutput getResult();
    MyFeatureProducerOutput getResultClone();
}

