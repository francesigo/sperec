package app;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.io.FilenameUtils;

import guiutils.ChooseFolder;
import guiutils.ComboBox;
import guiutils.InputNumericDouble;
import guiutils.InputNumericInteger;
import myIO.MyWavFile;

public class SplitSessions {

	public static void main(String[] args) throws Exception {

		Environment env = new Environment();
		String MAIN_OUTPUT_FOLDER_PATH = env.getDataDir();
		
		String inputDir = ChooseFolder.get(MAIN_OUTPUT_FOLDER_PATH, "SELEZIONA LA CARTELLA CON I FILE AUDIO");
		if (inputDir.equals(""))
			return;
		
		String _DURATA = "Dividi in base a durata";
		String _NUMERO = "Dividi in base a numero";
		String [] opts = new String[] {_NUMERO, _DURATA};
		
		String inbase = ComboBox.show("Come devo dividere i file?", opts);
	
		Integer numSessions = null;
		Double duration = null;
		if (inbase.equals(_NUMERO)) {
			numSessions = InputNumericInteger.get(2, "IN QUANTI SPEZZONI DEVO DIVIDERE I FILE AUDIO?");
			if (numSessions==null)
				return;
		}
		else
		{
			duration = InputNumericDouble.get(10.0, "QUANTI SECONDI DEVE DURARE OGNI SPEZZONE?");
			if (duration==null)
				return;
		}

		String outDir = ChooseFolder.get(MAIN_OUTPUT_FOLDER_PATH, "SELEZIONA LA CARTELLA PER OUTPUT");
		if (outDir.equals(""))
			return;

		// Ora posso lavorare

		// La tabella
		String tableFilePath = outDir + File.separator + "file.lst";
		
		
		File [] directoryListing = new File(inputDir).listFiles();
		if (directoryListing != null)
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(tableFilePath));
			writer.write("#Filename" + "\t" + "Speaker" + "\n");

			int spkCount = 0;

			int numFiles = directoryListing.length;

			long tStart = System.currentTimeMillis();

			// Per ogni file nella cartella
			int thread_idx = 0; for (int i=0; i<numFiles; i++) {
				File child = directoryListing[i];
				String filename = child.getName();
				String baseFilename = FilenameUtils.getBaseName(filename);
				// Controlla che abbia la giusta estensione
				String  fileExt = FilenameUtils.getExtension(child.getName());
				if (fileExt.equals("wav"))
				{
					String inputAudioFullPath = child.getAbsolutePath();
					String runMsg = "Thread " + thread_idx + " - Processing file: " + inputAudioFullPath;
					System.out.println(runMsg);

					// Carica in memoria l'intero file
					MyWavFile oWavfile = MyWavFile.openWavFile(new File(inputAudioFullPath));

					oWavfile.readAllFloat();	
					oWavfile.close();

					float [] audioSamples = oWavfile.getFloatSamples();
					int numSamples = audioSamples.length;

					int  splitDimension = (inbase.equals(_NUMERO)) ?
											numSamples/numSessions : (int)(duration*(double)oWavfile.getSampleRate());
					
					int offset = 0;
					int splitcount = 0;

					
					while (offset<numSamples)
					{
						String outFilename = baseFilename + "__split_" + splitcount + ".wav";
						String outpuFilePath = outDir + File.separator + outFilename;
						int remainingSamples = numSamples - offset;
						int remainingSamplesAfter = remainingSamples - splitDimension;

						// If after this session there is enough samples for another one, then
						// the current split size is the regular one, otherwise make the current split size larger
						int numFramesToWrite = (remainingSamplesAfter>=splitDimension) ? splitDimension : remainingSamples;

						System.out.println("Saving split: " + outpuFilePath);

						// Write the audio chunk to disk
						MyWavFile oWriteWavFile = MyWavFile.newWavFile(new File(outpuFilePath), 1, numFramesToWrite, oWavfile.getValidBits(), oWavfile.getSampleRate());
						oWriteWavFile.writeFrames(audioSamples, offset, numFramesToWrite);
						oWriteWavFile.close();

						// Update the table
						writer.write(outFilename + "\t" + spkCount + "\n");
				
						offset += numFramesToWrite;
						splitcount++;
					}
					
					spkCount++;

				}

			}
			showElapsedTime(tStart);
			writer.close();

		}
		

		
	}
	
	/**
	 * 
	 * @param tStart
	 */
	static void showElapsedTime(long tStart) {
		long tEnd = System.currentTimeMillis();
		long tDelta = tEnd - tStart;
		double elapsedSeconds = Math.round(tDelta / 1000.0);
		System.out.println("Elapsed seconds: " + elapsedSeconds );
	}
}
