package sperec_common;

/**
 * a list of sessions: each session is a list of chunks of fea
 * @author FS
 *
 */
public class LabeledArrayListOfSessions<T> extends LabeledArrayList<LabeledArrayList<T>> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public LabeledArrayListOfSessions(String label) {
		this.label = label;
	}
}
