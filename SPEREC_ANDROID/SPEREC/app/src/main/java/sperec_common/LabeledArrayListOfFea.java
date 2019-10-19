package sperec_common;

import java.util.ArrayList;

import myMath.MyMatrix;

public class LabeledArrayListOfFea extends LabeledArrayList<MyMatrix> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6237298215119234601L;
	
	public LabeledArrayListOfFea(ArrayList<MyMatrix> in, String label) {
		this.addAll(in);
		this.label = label;
	}
	public LabeledArrayListOfFea(ArrayList<MyMatrix> in) {
		this(in, "");
	}
}
