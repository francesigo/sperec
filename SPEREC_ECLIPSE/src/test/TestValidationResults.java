package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import sperec_common.ValidationResultGauss;
import sperec_jvm.SPEREC_Loader_JVM;

public class TestValidationResults {

	public static void main(String[] args) throws Exception {
		
		ArrayList<ValidationResultGauss> valResults = new ArrayList<ValidationResultGauss>();
		
		String provafile = "C:/Users/FS/Desktop/SPEREC_tmp_files/provaValidationResults.val";
		
		File fid = new File(provafile);
		try(BufferedReader br = new BufferedReader(new FileReader(fid)))
		{
		    for(String line; (line = br.readLine()) != null; )
		    {
		        if (line.charAt(0)!='#')
		        {
		        	String [] fields = line.split("\\t");
		        	ValidationResultGauss v = new ValidationResultGauss();

		        	v.SNR = Double.parseDouble(fields[0]);
		        	v.testDur = Double.parseDouble(fields[1]);
		        	String errFilePath = fields[2];
		        	
		        	InputStream errFileIs = new SPEREC_Loader_JVM().getInputStream(errFilePath);
					
					v.loadErrorRatesFromMatrixInputStream(errFileIs);

		        	
		        	valResults.add(v);
		        	
		        }
		    }
		}
		
		
	}
}
