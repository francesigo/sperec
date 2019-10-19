package sperec_common;

import java.io.File;

import myIO.MyWavFile;
import myMath.MyMatrix;

// Universal reader
public class IUReader {

	public static Object uReadFromFile(String in, Class<?> cl) throws Exception {
		
		if(cl.equals(MyMatrix.class)) {
			return MyMatrix.readFromFile(in);
	    }
		else if (cl.equals(MyWavFile.class)) {
			return MyWavFile.openWavFile(new File(in));
		}
		else if (cl.equals(String.class)) {
			return in; //just return the string
		}
		/*else if(cl.equals(MyAudioInfo.class)) {
	        return MyAudioInfo.getFrom(in);
	    }*/
		return null;
	}
}
