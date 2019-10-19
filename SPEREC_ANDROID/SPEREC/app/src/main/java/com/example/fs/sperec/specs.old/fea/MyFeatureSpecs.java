package com.example.fs.sperec.specs.fea;

/**
 * Created by FS on 18/10/2017.
 */


/* Properties:
    int count (even if differently named)
    String baseType;
    String fullType;
        // Computing algorithm (for now, just a String)

 */
public interface MyFeatureSpecs {
    public int getCount();
    public String getBaseType();
    public String getFullType();
}
