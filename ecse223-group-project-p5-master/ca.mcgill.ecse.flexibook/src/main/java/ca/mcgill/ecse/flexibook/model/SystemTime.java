package ca.mcgill.ecse.flexibook.model;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;

public class SystemTime {

	private static Date systemDate = null;
	private static Time systemTime = null;
	
	public static Date getSystemDate() {
		return systemDate;
	}
	
	public static Time getSystemTime() {
		return systemTime;
	}
	
	public static void setSystemDate(Date date) {
		systemDate = date;
	}
	
	public static void setSystemTime(Time time) {
		systemTime = time;
	}
	
	
	
	
	public static void setSystemDateAndTime(String dateAndTime) {
		String date = dateAndTime.substring(0, 10);
		String time = dateAndTime.substring(11, 16);
		//String[] dateArray = date.split("-");
		//String[] timeArray = date.split(":");
		//LocalDate localDate = LocalDate.of(Integer.parseInt(dateArray[0]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[2]));
		//LocalTime localTime = LocalTime.of(Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]), Integer.parseInt(timeArray[2]));
		systemDate = Date.valueOf(date);
		systemTime = Time.valueOf(time);
	}
	
		/*String date = dateAndTime.split("\\+")[0];
		String time = dateAndTime.split("\\+")[1];
		try {
			systemDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(date).getTime());
			systemTime = new Time(new SimpleDateFormat("HH:mm").parse(time).getTime());
		}catch(ParseException e) {}
	}*/
}
