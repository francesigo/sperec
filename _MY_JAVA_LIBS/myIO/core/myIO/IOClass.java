package myIO;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class in intended as an improvement of de.fau.cs.jstk.io.IOUtil.
 * The readDouble (double []) function of de.fau.cs.jstk.io.IOUtil is not able to detect if the underlying InputStream,
 * such as a FileInputStream, has less data left than the required. In this case, no Exception is thrown and false is returned.
 * I think that it's a dangerous behavior because it hides underlying inconsistencies in I/O operations.
 * @author francesco.sigona (FS)
 *
 */
public class IOClass {

	
	/**
	 * Build a path from separate folder/file names.
	 * Basically, it oncatenate input strings by putting a File.separator between them
	 * @param s one ore more Strings to be used to build the final path
	 * @return the final path
	 */
	public static String fullfile(String ... s) {
		String o = s[0];
		for (int i=1; i<s.length; i++)
			o += File.separator + s[i];
		return o;
	}
	
	/**
	 * Read a single int from the InputStream using given ByteOrder
	 * pointer respectively.
	 * @param is the input stream
	 * @param bo the bite order
	 * @return the integer value
	 * @throws IOException
	 */
	public static int readInt(InputStream is, ByteOrder bo) 
		throws IOException {
		byte [] bbuf = new byte [Integer.SIZE / 8];
		int read = is.read(bbuf);

		if (read < bbuf.length)
			throw new IOException ("could not read required bytes");

		ByteBuffer bb = ByteBuffer.wrap(bbuf);
		bb.order(bo);

		return bb.getInt();
	}
	
	/**
	 * Write an int to the OutputStream and advance the stream
	 * pointer respectively.
	 * @param os the output stream
	 * @param val the integer value to be written
	 * @param bo the byte order
	 * @throws IOException
	 */
	public static void writeInt(OutputStream os, int val, ByteOrder bo) 
		throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE/8);
		bb.order(bo);
		bb.putInt(val);
		os.write(bb.array());
	}
	
	
	/**
	 * Read a n array of int from the InputStream using given ByteOrder
	 * pointer respectively.
	 * @param is the input stream
	 * @param buf the array of integer to be overwritten by the read values
	 * @param bo the byte order
	 * @return what InputStream.read returns, i.e. -1 in case of EOF, the number of int actually read (can be less than the required) otherwise. The caller application MUST check!
	 * @throws IOException
	 */
	public static int readIntArray(InputStream is, int [] buf,  ByteOrder bo) throws IOException {
		byte [] bbuf = new byte [buf.length * Integer.SIZE/8];
		int read = is.read(bbuf);

		/* That was the code in IOUtil
		 * if (read < bbuf.length)
		 *	return false;
     	 */
		if (read<0)
			return read; //EOF

		ByteBuffer bb = ByteBuffer.wrap(bbuf);
		bb.order(bo);
		
		for (int i = 0; i < buf.length; ++i)
			buf[i] = bb.getInt();
		
		int int_read = (int)Math.floor((double)read/(double)(Integer.SIZE/8));
		return int_read; //return true;
	}
	
	/**
	 * Write the given int array to the OutputStream using given ByteOrder
	 * @param os the output stream
	 * @param buf the array of integers to be written
	 * @param bo the byte order
	 * @throws IOException
	 */
	public static void writeIntArray(OutputStream os, int [] buf, ByteOrder bo) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(buf.length * Integer.SIZE/8);
		bb.order(bo);
		for (int d : buf) 
			bb.putInt(d);
		os.write(bb.array());
	}
	
	/**
	 * Write the given sohrt array to the OutputStream using given ByteOrder
	 * @param os the output stream
	 * @param buf the array of integers to be written
	 * @param bo the byte order
	 * @throws IOException
	 */
	public static void writeShortArray(OutputStream os, Short [] buf, ByteOrder bo) throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(buf.length * Integer.SIZE/8);
		bb.order(bo);
		for (int d : buf) 
			bb.putInt(d);
		os.write(bb.array());
	}
	
	/**
	 * Reads double array from a binary InputStream
	 * 
	 * @param is binary InputStream
	 * @param buf (output) buffer
	 * @param bo the byte order
	 * @return what InputStream.read returns, i.e. -1 in case of EOF, the number of doubles actually read (can be less than the required) otherwise. The caller application MUST check!
	 * @throws IOException
	 */
	public static int readDoubleArray(InputStream is, double [] buf, ByteOrder bo) throws IOException {
		byte [] bbuf = new byte[buf.length * Double.SIZE / 8];
		int read = is.read(bbuf);

		/* That was the code in IOUtil
		 * if (read < bbuf.length)
		 *	return false;
		 */
		if (read<0)
			return read; //EOF

		ByteBuffer bb = ByteBuffer.wrap(bbuf);
		bb.order(bo);

		for (int i = 0; i < buf.length; ++i)
			buf[i] = bb.getDouble();
		
		int double_read = (int)Math.floor((double)read/(double)(Double.SIZE/8));

		return double_read; //return true;
	}
	
	/**
	 * Read a single Double from the InputStream
	 * @param is the input stream
	 * @param bo the byte order
	 * @return the read Double
	 * @throws IOException
	 */
	public static double readDouble(InputStream is, ByteOrder bo) throws IOException {
		byte [] bbuf = new byte[Double.SIZE / 8];
		int read = is.read(bbuf);

		if (read < bbuf.length)
			throw new IOException("could not read required bytes");

		ByteBuffer bb = ByteBuffer.wrap(bbuf);
		bb.order(bo);

		return bb.getDouble();
	}
	/**
	 * Write the given double array to the OutputStream using the specified 
	 * ByteOrder
	 * @param os the output stream
	 * @param buf the array of double to be written
	 * @param bo the byte order
	 * @throws IOException
	 */
	public static void writeDoubleArray(OutputStream os, double [] buf, ByteOrder bo) 
		throws IOException {
		ByteBuffer bb = ByteBuffer.allocate(buf.length * Double.SIZE/8);
		bb.order(bo);
		for (double d : buf) 
			bb.putDouble(d);
		os.write(bb.array());
	}
}
