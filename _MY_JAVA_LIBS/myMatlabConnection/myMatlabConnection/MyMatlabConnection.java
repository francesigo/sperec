package myMatlabConnection;

import com.mathworks.engine.EngineException;
import com.mathworks.engine.MatlabExecutionException;
import com.mathworks.engine.UnsupportedTypeException;
import com.mathworks.engine.MatlabSyntaxException;
import com.mathworks.engine.MatlabEngine;
import com.mathworks.matlab.types.*;

public class MyMatlabConnection {

	/*
	 * Attributes
	 */
	public MatlabEngine eng = null; // ma deve essere davvero static? potrei anche utilizzare piu' istanze di wrapper con differenti eng? Al momento credo di no, quindi lascio static

	/*
	 * If true, the connection creation will block until the engine is shared
	 */
	public boolean blockingInitMatlab;
	/*
	 *  Methods
	 */

	/**
	 * Constructors
	 */
	public MyMatlabConnection(boolean blockingInitMatlab) throws Exception {
		this.blockingInitMatlab = blockingInitMatlab;
		System.out.println("Begin Matlab Wrapper initialization...");
		eng = newSperecMatlabConnection(blockingInitMatlab);

		if (eng != null) {
			System.out.println("Matlab Wrapper initialization complete.");

		} else {
			System.out.println("Matlab Wrapper Initialization failed.");
		}
	}
	public MyMatlabConnection() throws Exception {
		this(true);
	}
	
	//-------------------------------------------------------------------------------
	public Struct call(String fname, Struct reqBody) throws Exception {	
		// Send the request to Matlab server. Put the response in Struct res
		Struct res = null;

		if (eng!=null) {
			try {
				res = eng.feval(fname, reqBody);
			} catch (MatlabExecutionException e){
				System.out.println("MatlabExecutionException");
			} catch (UnsupportedTypeException e){
				System.out.println("UnsupportedTypeException");
			} catch (MatlabSyntaxException e){
				System.out.println("MatlabSyntaxException");
			} catch (EngineException e){
				System.out.println("EngineException");
			}catch (Exception e){
				System.out.println("Excepion: " + e.getClass().getName());
			}
		}
		return res;
	}

	/**
	 * A wrapper to check the connectin to Matlab.
	 * @return the MatlabEngine instance, or null
	 * @throws Exception
	 */
	private MatlabEngine newSperecMatlabConnection(boolean blockingInitMatlab) throws Exception {
		
		String [] engines;
		MatlabEngine eng = null;

		System.out.println("Searching for Matlab shared engines...");
		try {
			
			while (eng==null)
			{
				engines = MatlabEngine.findMatlab();
				if ((engines==null) || (engines.length==0))
				{
					if (blockingInitMatlab) {
						Thread.sleep(2000); // Wait 2 sec
						System.out.print(".");
					}
					else
						return null;
				}
				else
				{
					try {
						eng = MatlabEngine.connectMatlab(engines[0]);
					} catch (EngineException e) {
						System.out.println("EngineException!!");
					} catch (InterruptedException e) {
						System.out.println("InterruptedException!!");
					} catch(ArrayIndexOutOfBoundsException e) {
						System.out.println("ArrayIndexOutOfBoundsException!!");
					}
				}
			}		

		} catch (EngineException e) {
			e.printStackTrace();
		}
		
		return eng;
	}

	

}

