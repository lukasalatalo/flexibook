package ca.mcgill.ecse.flexibook.controller;

public class TOService {
	//------------------------
	  // MEMBER VARIABLES
	  //------------------------

	  //TOService Attributes
	  private String name;
	  private int duration;
	  private int downtimeDuration;
	  private int downtimeStart;

	  //------------------------
	  // CONSTRUCTOR
	  //------------------------

	  public TOService(String aName, int aduration, int aDowntimeDuration, int aDowntimeStart){
	    name = aName;
	    duration = aduration;
	    downtimeDuration = aDowntimeDuration;
	    downtimeStart = aDowntimeStart;
	  }

	  //------------------------
	  // INTERFACE
	  //------------------------

	  public boolean setName(String aName)
	  {
	    boolean wasSet = false;
	    name = aName;
	    wasSet = true;
	    return wasSet;
	  }

	  public boolean setDuration(int aDuration)
	  {
	    boolean wasSet = false;
	    duration = aDuration;
	    wasSet = true;
	    return wasSet;
	  }
	  
	  public boolean setDowntimeDuration(int aDowntimeDuration)
	  {
	    boolean wasSet = false;
	    downtimeDuration = aDowntimeDuration;
	    wasSet = true;
	    return wasSet;
	  }
	  
	  public boolean setDowntimeStart(int aDowntimeStart)
	  {
	    boolean wasSet = false;
	    downtimeStart = aDowntimeStart;
	    wasSet = true;
	    return wasSet;
	  }

	  public String getName()
	  {
	    return name;
	  }

	  public int getDuration()
	  {
	    return duration;
	  }
	  
	  public int getDowntimeDuration()
	  {
	    return downtimeDuration;
	  }
	  
	  public int getDowntimeStart()
	  {
	    return downtimeStart;
	  }

	  
	  public void delete()
	  {}


	  public String toString()
	  {
	    return super.toString() + "["+
	            "name" + ":" + getName()+ "," +
	            "service duration" + ":" + getDuration()+ "," + 
	            "downtime duration" + ":" + getDowntimeDuration()+ "," +
	            "downtime start" + ":" + getDowntimeStart()+ "]";
	  }

}