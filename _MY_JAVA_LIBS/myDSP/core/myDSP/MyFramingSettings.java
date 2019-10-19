package myDSP;

public class MyFramingSettings {
	
	public double window_sec = 0.030;
	public double overlap_sec = 0.020;
	

	public MyFramingSettings () {
	}
	
	public MyFramingSettings (MyFramingSettings in) {
		this.window_sec = in.window_sec;
		this.overlap_sec = in.overlap_sec;
	}
	
	public void showInfo(String title) {
		System.out.println(title);
		System.out.println("  window_sec  = " + window_sec);
		System.out.println("  overlap_sec = " + overlap_sec);
	}

}
