package com.example.fs.sperec.specs.fea;


public class MyFeatureProducer implements MyFeatureProducerInterface {

    private boolean ready = false; // If the object has been initialized or not
    private MyFeatureSpecs feaSpecs = null; // mfccConfig = null;

    private String My_ID = ""; //  "MFCC_Tarsos"; //default

    // Actual implementations (wrappers)
    private MyFeatureProducerInterface wx_actual_producer = null;

    MyFeatureProducerOutput currentOutput;

    //public void compute();

    //----------------------------------------------------------------------------------------------
    public MyFeatureProducer(MyFeatureSpecs feaSpecs) {
        assert(null != feaSpecs);
        assert(null != feaSpecs.getBaseType());

        String feaType = feaSpecs.getBaseType();

        switch (feaType) {
            case "MFCC_Tarsos":
                wx_actual_producer = new Wx_Tarsos_MFCC((MyTarsosMFCC_Specs) feaSpecs);
                break;

            default: // QUa ci vorrebbe una exception
                System.out.println("ERROR: Unsupported type: " + feaType);
                feaType = null;
        }
        if (null!=feaType) {
            this.feaSpecs = feaSpecs;
            currentOutput = new MyFeatureProducerOutput();
        }
    }

    //----------------------------------------------------------------------------------------------
    public void compute(MyFeatureProducerInput inputData) {
        wx_actual_producer.compute(inputData);
    }

    //----------------------------------------------------------------------------------------------
    public MyFeatureProducerOutput getResult() {
        currentOutput = wx_actual_producer.getResult();
        return currentOutput;
    }

    //----------------------------------------------------------------------------------------------
    public MyFeatureProducerOutput getResultClone() {
        currentOutput = wx_actual_producer.getResultClone();
        return currentOutput;
    }
}
