package sperec_common;

import java.util.ArrayList;

public class LabeledArrayList<T> extends ArrayList<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public String label = "";


	/*public <T> LabeledArrayList (ArrayList<T> in, String label) {
		this.addAll(in);
		this.label = label;
	}*/
	
	public LabeledArrayList() {
		// TODO Auto-generated constructor stub
	}
	
	public LabeledArrayList (String label) {
		this.label = label;
	}


	
	
	//Aliases
	/**
	 * a list of matrices
	 * @author FS
	 *
	 */
	/*public static class LabeledArrayListOfFea extends LabeledArrayList<MyMatrix> {

		public LabeledArrayListOfFea(ArrayList<MyMatrix> in, String label) {
			this.addAll(in);
			this.label = label;
		}
		public LabeledArrayListOfFea(ArrayList<MyMatrix> in) {
			this(in, "");
		}
	}*/

	/**
	 * a list of sessions: each session is a list of chunks of fea
	 * @author FS
	 *
	 */
	/*public static class LabeledArrayListOfSessions extends LabeledArrayList<LabeledArrayListOfObjects> {
		public LabeledArrayListOfSessions(String label) {
			this.label = label;
		}
	};*/

	/**
	 * a list of "speakers": each one is a list of sessions; each session is a list of chunks
	 * @author FS
	 *
	 */
	//public static class LabeledArrayListOfSpeakers extends LabeledArrayList<LabeledArrayListOfSessions> {};



}