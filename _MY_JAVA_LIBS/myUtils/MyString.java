package myUtils;

public class MyString {

	public static String [] select(String [] src, int [] id) {
		
		String [] out = new String[id.length];
		for (int i = 0; i<id.length; i++)
			out[i] = src[id[i]];
		
		return out;
	}
	
	public static String [] cat(String [][] sa) {
		int n = 0;
		for (int i=0; i<sa.length; i++)
			if (sa[i]!=null)
				n += sa[i].length;
		
		String [] out = new String[n];
		int k=0;
		for (int i=0; i<sa.length; i++)
			if (sa[i] != null)
				for (int j=0; j<sa[i].length; j++)
					out[k++] = sa[i][j];
		
		return out;
	}
	
	static public boolean [] strcmp(String [] array, String what) {
		boolean [] b = new boolean[array.length];
		for (int i=0; i<b.length; i++)
			b[i] = array[i].equals(what);
	
	return b;
	}
}
