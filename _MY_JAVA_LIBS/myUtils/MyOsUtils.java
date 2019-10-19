package myUtils;

import java.io.File;

public final class MyOsUtils
{
   private static String OS = null;
   
   public static String getOsName()
   {
      if(OS == null) { OS = System.getProperty("os.name"); }
      return OS;
   }
   public static boolean isWindows()
   {
      return getOsName().startsWith("Windows");
   }
 //public static boolean isUnix() // and so on
   
   
   /**
    * Remove "/" from the beginning of a String (that usually represents a path)
    * @param p the input string
    * @return the output string
    */
   public static String getDiskPath(String p) {
	   if (isWindows())
	   {
		   while(p.startsWith("/"))
			   p = p.substring(1, p.length());
		   
		   if (p.contains("/"))
			   p = p.replace("/", "\\");
	   }
	   return p;
   }
   
   /**
    * Add "/" at the beginning, after checking on "\\" or "/"
    * @param p the input String
    * @return the output String
    */
   public static String getRoutablePath(String p) {

	   if (isWindows())
	   {
		   if (p.contains("\\"))
			   p = p.replace("\\", "/");

		   if (!p.startsWith("/"))

			   p = "/".concat(p);
	   }

	   return p;
   }

   /**
    * Check if a folder is writable.
    * It tries to create a temporary file inside the provided folder.
    * @param dir the folder location
    * @return true is the folder is writeable, false otherwise
    */
   public static boolean isWriteable(String dir) {
	   	   
	   String tempSubFolder = "" + System.currentTimeMillis();
	   File f = new File(dir + File.separator + tempSubFolder);
	   if (f.mkdir()) 
	   {
		   f.delete();
		   return true;
	   }
	   return false;
   }
   
}