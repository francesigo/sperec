package myString;

public class MyString {

	static public boolean [] strcmp(String [] array, String what) {
		boolean [] b = new boolean[array.length];
		for (int i=0; i<b.length; i++)
			b[i] = array[i].equals(what);
	
	return b;
	}
}
