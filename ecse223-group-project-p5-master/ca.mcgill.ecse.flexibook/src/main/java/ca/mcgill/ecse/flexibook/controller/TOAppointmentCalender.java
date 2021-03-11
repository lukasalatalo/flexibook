package ca.mcgill.ecse.flexibook.controller;

/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/



//line 2 "model.ump"
//line 15 "model.ump"
public class TOAppointmentCalender
{

//------------------------
// MEMBER VARIABLES
//------------------------

//TOAppointmentCalender Attributes
private String date;
private String startTime;
private String endTime;
private boolean isAvailable;

//------------------------
// CONSTRUCTOR
//------------------------

public TOAppointmentCalender(String aDate, String aStartTime, String aEndTime, boolean aIsAvailable)
{
 date = aDate;
 startTime = aStartTime;
 endTime = aEndTime;
 isAvailable = aIsAvailable;
}

//------------------------
// INTERFACE
//------------------------

public boolean setDate(String aDate)
{
 boolean wasSet = false;
 date = aDate;
 wasSet = true;
 return wasSet;
}

public boolean setStartTime(String aStartTime)
{
 boolean wasSet = false;
 startTime = aStartTime;
 wasSet = true;
 return wasSet;
}

public boolean setEndTime(String aEndTime)
{
 boolean wasSet = false;
 endTime = aEndTime;
 wasSet = true;
 return wasSet;
}

public boolean setIsAvailable(boolean aIsAvailable)
{
 boolean wasSet = false;
 isAvailable = aIsAvailable;
 wasSet = true;
 return wasSet;
}

public String getDate()
{
 return date;
}

public String getStartTime()
{
 return startTime;
}

public String getEndTime()
{
 return endTime;
}

public boolean getIsAvailable()
{
 return isAvailable;
}

public void delete()
{}


public String toString()
{
 return super.toString() + "["+
         "date" + ":" + getDate()+ "," +
         "startTime" + ":" + getStartTime()+ "," +
         "endTime" + ":" + getEndTime()+ "," +
         "isAvailable" + ":" + getIsAvailable()+ "]";
}
}