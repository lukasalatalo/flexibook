package ca.mcgill.ecse.flexibook.controller;

import java.util.ArrayList;
import java.util.List;

import ca.mcgill.ecse.flexibook.model.Appointment.AppointmentState;
import ca.mcgill.ecse.flexibook.model.BookableService;
import ca.mcgill.ecse.flexibook.model.ComboItem;
import ca.mcgill.ecse.flexibook.model.Customer;
import ca.mcgill.ecse.flexibook.model.FlexiBook;
import ca.mcgill.ecse.flexibook.model.TimeSlot;

public class TOAppointment {
	//------------------------
	  // MEMBER VARIABLES
	  //------------------------

	  //TOAppointment Attributes
	  private Customer customer;
	  private BookableService bookableService;
	  private List<ComboItem> chosenItems;
	  private TimeSlot timeSlot;
	  private AppointmentState state;

	  //------------------------
	  // CONSTRUCTOR
	  //------------------------

	  public TOAppointment(Customer aCustomer, BookableService aBookableService, TimeSlot aTimeSlot, List<ComboItem> someChosenItems, AppointmentState aState){
	    customer = aCustomer;
	    bookableService = aBookableService;
	    timeSlot = aTimeSlot;
	    chosenItems = someChosenItems;
	    state = aState;
	  }

	  //------------------------
	  // INTERFACE
	  //------------------------

	  public boolean setCustomer(Customer aCustomer)
	  {
	    boolean wasSet = false;
	    customer = aCustomer;
	    wasSet = true;
	    return wasSet;
	  }

	  public boolean setBookableService(BookableService aBookableService)
	  {
	    boolean wasSet = false;
	    bookableService = aBookableService;
	    wasSet = true;
	    return wasSet;
	  }
	  
	  public boolean setTimeSlot(TimeSlot aTimeSlot)
	  {
	    boolean wasSet = false;
	    timeSlot = aTimeSlot;
	    wasSet = true;
	    return wasSet;
	  }
	  

	  public Customer getCustomer()
	  {
	    return customer;
	  }

	  public BookableService getBookableService()
	  {
	    return bookableService;
	  }
	  
	  public TimeSlot getTimeSlot()
	  {
	    return timeSlot;
	  }
	  
	  public String getStartDate() 
	  {
		  return timeSlot.getStartDate().toString();
	  }
	  
	  public String getStartTime() 
	  {
		  return timeSlot.getStartTime().toString();
	  }
	  
	  public List<ComboItem> getChosenItems()
	  {
	    return chosenItems;
	  }
	  
	  public AppointmentState getAppointmentState() {
			return state;
		}

		public void setAppointmentState(AppointmentState a) {
			state = a;
		}

	  
	  public void delete()
	  {}


	  public String toString()
	  {
	    return super.toString() + "["+
	            "customer" + ":" + getCustomer()+ "," +
	            "bookable service" + ":" + getBookableService()+ "," + 
	            "time slot" + ":" + getTimeSlot()+ "," +
	            "chosen items" + ":" + getChosenItems()+ "]";
	  }
}
