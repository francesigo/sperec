package sperec_common;

import myIO.MyWavFile;

public class SessionsTable_Audio extends SessionsTable_v2<MyWavFile> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4056070419678406179L;

	public SessionsTable_Audio() {
		super(MyWavFile.class);
	}
	public SessionsTable_Audio(Class cl) {
		super(cl);
		// TODO Auto-generated constructor stub
	}

	@Override
	public LabeledArrayList<MyWavFile> chunkSession(StRecord session, int chunkSize) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
