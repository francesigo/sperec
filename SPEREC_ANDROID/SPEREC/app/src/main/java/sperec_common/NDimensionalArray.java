package sperec_common;

import java.lang.reflect.Array;

public class NDimensionalArray {
	
	  private ValidationResultGauss [] array; // internal representation of the N-dimensional array
	  private int[] dimensions; // dimensions of the array
	  private int[] multipliers; // used to calculate the index in the internal array

	  public ValidationResultGauss [] getArray() {
		  return array;
	  }
	  
	  public NDimensionalArray(int... dimensions) {
	    int arraySize = 1;

	    multipliers = new int[dimensions.length];
	    for (int idx = dimensions.length - 1; idx >= 0; idx--) {
	      multipliers[idx] = arraySize;
	      arraySize *= dimensions[idx];
	    }
	    array = (ValidationResultGauss[]) Array.newInstance(ValidationResultGauss.class, arraySize); // new Object[arraySize];
	    this.dimensions = dimensions;
	  }
	  
	  /**
	   * 
	   * @param indices
	   * @return
	   */
	  public ValidationResultGauss get(int... indices) {
	    assert indices.length == dimensions.length;
	    int internalIndex = 0;

	    for (int idx = 0; idx < indices.length; idx++) {
	      internalIndex += indices[idx] * multipliers[idx];
	    }
	    return array[internalIndex];
	  }
	  
	  /**
	   * 
	   * @param O
	   * @param indices
	   */
	  public void set(ValidationResultGauss O, int... indices) {
		  assert indices.length == dimensions.length;
		  
		  int internalIndex = 0;

		  for (int idx = 0; idx < indices.length; idx++) {
			  internalIndex += indices[idx] * multipliers[idx];
		  }
		  
		  array[internalIndex] = O;
		    
	  }
	  
	  public int [] getDimensions() {
		  return dimensions;
	  }
	  
}
