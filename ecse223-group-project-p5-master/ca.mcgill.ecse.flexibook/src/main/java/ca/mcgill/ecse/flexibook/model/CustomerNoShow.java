package ca.mcgill.ecse.flexibook.model;

public class CustomerNoShow {

	private static int numberOfNoShows = 0;
	
	public static int getNoShow() {
		return numberOfNoShows;
	}
	
	public static void setNoShow(int i) {
		numberOfNoShows = numberOfNoShows + i;
	}
	
}
