package sperec_common;

public class MyURL {

	public static boolean isURL(String s) {
		
		if (s==null)
			return false;
		
		return (s.startsWith("http:") || s.startsWith("https:"));
	}
	
	
	public static String fixURL(String s) {
		String s1 = "";
		
		if (s.startsWith("http:"))
			s1 = s.replace("http:\\", "http://");
		else if(s.startsWith("https:"))
			s1 = s.replace("https:\\", "https://");
		else
			s1 = s;
		
		s1 = s1.replace("\\",  "/");
		
		return s1;
	}
}
