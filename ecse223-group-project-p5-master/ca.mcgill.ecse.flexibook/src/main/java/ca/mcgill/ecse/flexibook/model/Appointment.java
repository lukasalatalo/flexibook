/*PLEASE DO NOT EDIT THIS CODE*/
/*This code was generated using the UMPLE 1.30.1.5099.60569f335 modeling language!*/

package ca.mcgill.ecse.flexibook.model;
import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jdatepicker.impl.SqlDateModel;

// line 22 "../../../../../FlexiBookPersistence.ump"
// line 2 "../../../../../FlexiBookStates.ump"
// line 87 "../../../../../FlexiBook.ump"
public class Appointment implements Serializable
{

	//------------------------
	// MEMBER VARIABLES
	//------------------------

	//Appointment State Machines
	public enum AppointmentState { Booked, Final, InProgress }
	private AppointmentState appointmentState;

	//Appointment Associations
	private Customer customer;
	private BookableService bookableService;
	private List<ComboItem> chosenItems;
	private TimeSlot timeSlot;
	private FlexiBook flexiBook;

	//------------------------
	// CONSTRUCTOR
	//------------------------

	public Appointment(Customer aCustomer, BookableService aBookableService, TimeSlot aTimeSlot, FlexiBook aFlexiBook)
	{
		boolean didAddCustomer = setCustomer(aCustomer);
		if (!didAddCustomer)
		{
			throw new RuntimeException("Unable to create appointment due to customer. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
		}
		boolean didAddBookableService = setBookableService(aBookableService);
		if (!didAddBookableService)
		{
			throw new RuntimeException("Unable to create appointment due to bookableService. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
		}
		chosenItems = new ArrayList<ComboItem>();
		if (!setTimeSlot(aTimeSlot))
		{
			throw new RuntimeException("Unable to create Appointment due to aTimeSlot. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
		}
		boolean didAddFlexiBook = setFlexiBook(aFlexiBook);
		if (!didAddFlexiBook)
		{
			throw new RuntimeException("Unable to create appointment due to flexiBook. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html");
		}
		setAppointmentState(AppointmentState.Booked);
	}

	//------------------------
	// INTERFACE
	//------------------------

	public String getAppointmentStateFullName()
	{
		String answer = appointmentState.toString();
		return answer;
	}

	public AppointmentState getAppointmentState()
	{
		return appointmentState;
	}

	public boolean updateAppointment(TimeSlot aTimeSlot,Boolean changeService,BookableService aService,Boolean addItem,ComboItem optionalServices)
	{
		boolean wasEventProcessed = false;

		AppointmentState aAppointmentState = appointmentState;
		switch (aAppointmentState)
		{
		case Booked:
			if (isTimeSlotAvailable(aTimeSlot)&&!(sameDay()))
			{
				// line 15 "../../../../../FlexiBookStates.ump"
				doUpdateAppointment(aTimeSlot, changeService, aService, addItem, optionalServices);
				setAppointmentState(AppointmentState.Booked);
				wasEventProcessed = true;
				break;
			}
			if (!(isTimeSlotAvailable(aTimeSlot))||sameDay()==true)
			{
				// line 19 "../../../../../FlexiBookStates.ump"
				rejectUpdateAppointment();
				setAppointmentState(AppointmentState.Booked);
				wasEventProcessed = true;
				break;
			}
			break;
		case InProgress:
			if (isTimeSlotAvailable(aTimeSlot))
			{
				// line 40 "../../../../../FlexiBookStates.ump"
				doUpdateAppointment(aTimeSlot, changeService, aService, addItem, optionalServices);
				setAppointmentState(AppointmentState.InProgress);
				wasEventProcessed = true;
				break;
			}
			if (!(isTimeSlotAvailable(aTimeSlot)))
			{
				// line 44 "../../../../../FlexiBookStates.ump"
				rejectUpdateAppointment();
				setAppointmentState(AppointmentState.InProgress);
				wasEventProcessed = true;
				break;
			}
			break;
		default:
			// Other states do respond to this event
		}

		return wasEventProcessed;
	}

	public boolean cancelAppointment(Customer aCustomer)
	{
		boolean wasEventProcessed = false;

		AppointmentState aAppointmentState = appointmentState;
		switch (aAppointmentState)
		{
		case Booked:
			if (!(sameDay()))
			{
				setAppointmentState(AppointmentState.Final);
				wasEventProcessed = true;
				break;
			}
			if (sameDay())
			{
				// line 25 "../../../../../FlexiBookStates.ump"
				rejectCancelAppointment();
				setAppointmentState(AppointmentState.Booked);
				wasEventProcessed = true;
				break;
			}
			break;
		case InProgress:
			if (hasAppointmentStarted())
			{
				// line 50 "../../../../../FlexiBookStates.ump"
				rejectCancelAppointment();
				setAppointmentState(AppointmentState.InProgress);
				wasEventProcessed = true;
				break;
			}
			break;
		default:
			// Other states do respond to this event
		}

		return wasEventProcessed;
	}

	public boolean startAppointment()
	{
		boolean wasEventProcessed = false;

		AppointmentState aAppointmentState = appointmentState;
		switch (aAppointmentState)
		{
		case Booked:
			if (isDuringTimeSlot())
			{
				setAppointmentState(AppointmentState.InProgress);
				wasEventProcessed = true;
				break;
			}
			break;
		default:
			// Other states do respond to this event
		}

		return wasEventProcessed;
	}

	public boolean customerNoShow()
	{
		boolean wasEventProcessed = false;

		AppointmentState aAppointmentState = appointmentState;
		switch (aAppointmentState)
		{
		case Booked:
			if (isDuringTimeSlot())
			{
				// line 31 "../../../../../FlexiBookStates.ump"
				doCustomerNoShow();
				setAppointmentState(AppointmentState.Final);
				wasEventProcessed = true;
				break;
			}
			break;
		default:
			// Other states do respond to this event
		}

		return wasEventProcessed;
	}

	public boolean finishAppointment()
	{
		boolean wasEventProcessed = false;

		AppointmentState aAppointmentState = appointmentState;
		switch (aAppointmentState)
		{
		case InProgress:
			setAppointmentState(AppointmentState.Final);
			wasEventProcessed = true;
			break;
		default:
			// Other states do respond to this event
		}

		return wasEventProcessed;
	}

	private void setAppointmentState(AppointmentState aAppointmentState)
	{
		appointmentState = aAppointmentState;

		// entry actions and do activities
		switch(appointmentState)
		{
		case Final:
			delete();
			break;
		}
	}
	/* Code from template association_GetOne */
	public Customer getCustomer()
	{
		return customer;
	}
	/* Code from template association_GetOne */
	public BookableService getBookableService()
	{
		return bookableService;
	}
	/* Code from template association_GetMany */
	public ComboItem getChosenItem(int index)
	{
		ComboItem aChosenItem = chosenItems.get(index);
		return aChosenItem;
	}

	public List<ComboItem> getChosenItems()
	{
		List<ComboItem> newChosenItems = Collections.unmodifiableList(chosenItems);
		return newChosenItems;
	}

	public int numberOfChosenItems()
	{
		int number = chosenItems.size();
		return number;
	}

	public boolean hasChosenItems()
	{
		boolean has = chosenItems.size() > 0;
		return has;
	}

	public int indexOfChosenItem(ComboItem aChosenItem)
	{
		int index = chosenItems.indexOf(aChosenItem);
		return index;
	}
	/* Code from template association_GetOne */
	public TimeSlot getTimeSlot()
	{
		return timeSlot;
	}
	/* Code from template association_GetOne */
	public FlexiBook getFlexiBook()
	{
		return flexiBook;
	}
	/* Code from template association_SetOneToMany */
	public boolean setCustomer(Customer aCustomer)
	{
		boolean wasSet = false;
		if (aCustomer == null)
		{
			return wasSet;
		}

		Customer existingCustomer = customer;
		customer = aCustomer;
		if (existingCustomer != null && !existingCustomer.equals(aCustomer))
		{
			existingCustomer.removeAppointment(this);
		}
		customer.addAppointment(this);
		wasSet = true;
		return wasSet;
	}
	/* Code from template association_SetOneToMany */
	public boolean setBookableService(BookableService aBookableService)
	{
		boolean wasSet = false;
		if (aBookableService == null)
		{
			return wasSet;
		}

		BookableService existingBookableService = bookableService;
		bookableService = aBookableService;
		if (existingBookableService != null && !existingBookableService.equals(aBookableService))
		{
			existingBookableService.removeAppointment(this);
		}
		bookableService.addAppointment(this);
		wasSet = true;
		return wasSet;
	}
	/* Code from template association_MinimumNumberOfMethod */
	public static int minimumNumberOfChosenItems()
	{
		return 0;
	}
	/* Code from template association_AddUnidirectionalMany */
	public boolean addChosenItem(ComboItem aChosenItem)
	{
		boolean wasAdded = false;
		if (chosenItems.contains(aChosenItem)) { return false; }
		chosenItems.add(aChosenItem);
		wasAdded = true;
		return wasAdded;
	}

	public boolean removeChosenItem(ComboItem aChosenItem)
	{
		boolean wasRemoved = false;
		if (chosenItems.contains(aChosenItem))
		{
			chosenItems.remove(aChosenItem);
			wasRemoved = true;
		}
		return wasRemoved;
	}
	/* Code from template association_AddIndexControlFunctions */
	public boolean addChosenItemAt(ComboItem aChosenItem, int index)
	{  
		boolean wasAdded = false;
		if(addChosenItem(aChosenItem))
		{
			if(index < 0 ) { index = 0; }
			if(index > numberOfChosenItems()) { index = numberOfChosenItems() - 1; }
			chosenItems.remove(aChosenItem);
			chosenItems.add(index, aChosenItem);
			wasAdded = true;
		}
		return wasAdded;
	}

	public boolean addOrMoveChosenItemAt(ComboItem aChosenItem, int index)
	{
		boolean wasAdded = false;
		if(chosenItems.contains(aChosenItem))
		{
			if(index < 0 ) { index = 0; }
			if(index > numberOfChosenItems()) { index = numberOfChosenItems() - 1; }
			chosenItems.remove(aChosenItem);
			chosenItems.add(index, aChosenItem);
			wasAdded = true;
		} 
		else 
		{
			wasAdded = addChosenItemAt(aChosenItem, index);
		}
		return wasAdded;
	}
	/* Code from template association_SetUnidirectionalOne */
	public boolean setTimeSlot(TimeSlot aNewTimeSlot)
	{
		boolean wasSet = false;
		if (aNewTimeSlot != null)
		{
			timeSlot = aNewTimeSlot;
			wasSet = true;
		}
		return wasSet;
	}
	/* Code from template association_SetOneToMany */
	public boolean setFlexiBook(FlexiBook aFlexiBook)
	{
		boolean wasSet = false;
		if (aFlexiBook == null)
		{
			return wasSet;
		}

		FlexiBook existingFlexiBook = flexiBook;
		flexiBook = aFlexiBook;
		if (existingFlexiBook != null && !existingFlexiBook.equals(aFlexiBook))
		{
			existingFlexiBook.removeAppointment(this);
		}
		flexiBook.addAppointment(this);
		wasSet = true;
		return wasSet;
	}

	public void delete()
	{
		Customer placeholderCustomer = customer;
		this.customer = null;
		if(placeholderCustomer != null)
		{
			placeholderCustomer.removeAppointment(this);
		}
		BookableService placeholderBookableService = bookableService;
		this.bookableService = null;
		if(placeholderBookableService != null)
		{
			placeholderBookableService.removeAppointment(this);
		}
		chosenItems.clear();
		timeSlot = null;
		FlexiBook placeholderFlexiBook = flexiBook;
		this.flexiBook = null;
		if(placeholderFlexiBook != null)
		{
			placeholderFlexiBook.removeAppointment(this);
		}
	}


	/**
	 * Checks if an appointment has started
	 * @author antoninguerre
	 */
	// line 66 "../../../../../FlexiBookStates.ump"
	private boolean hasAppointmentStarted(){
		if (this.getAppointmentState() != Appointment.AppointmentState.Booked) {
			return true;
		}
		return false;
	}


	/**
	 * Marks a customer as no show
	 * @author antoninguerre
	 */
	// line 76 "../../../../../FlexiBookStates.ump"
	private void doCustomerNoShow(){
		this.getCustomer().isNoShow();
		flexiBook.removeAppointment(this);
	}


	/**
	 * Updates an appointment with the given characteristics
	 * @author antoninguerre
	 */
	// line 84 "../../../../../FlexiBookStates.ump"
	private void doUpdateAppointment(TimeSlot ts, Boolean c, BookableService s, Boolean a, ComboItem optItem){
		this.setTimeSlot(ts);

		if (c == true) {
			this.setBookableService(s);
			return;
		}

		if (this.getBookableService() instanceof ServiceCombo) {
			ServiceCombo aServiceCombo = (ServiceCombo) this.getBookableService();
			if (a == true) { 
				this.addChosenItem(optItem);
			}
			else {
				this.removeChosenItem(optItem);
			}
		}
		return;
	}


	/**
	 * Reject the appointment update
	 * @author antoninguerre
	 */
	// line 107 "../../../../../FlexiBookStates.ump"
	private void rejectUpdateAppointment(){
		throw new RuntimeException("unsuccessful");
	}


	/**
	 * Reject the appointment cancel
	 * @author antoninguerre
	 */
	// line 114 "../../../../../FlexiBookStates.ump"
	private void rejectCancelAppointment(){
		throw new RuntimeException("unsuccessful");
	}


	/**
	 * Checks if the system time matches with an appointment time slot
	 * @author antoninguerre
	 */
	// line 121 "../../../../../FlexiBookStates.ump"
	private boolean isDuringTimeSlot(){
		boolean isDuringTimeSlot = false;

		Date systemDate = SystemTime.getSystemDate();
		Time systemTime = SystemTime.getSystemTime();

		Time appointmentStartTime = this.timeSlot.getStartTime();
		Date appointmentDate = this.timeSlot.getStartDate();
		Time appointmentEndTime = this.timeSlot.getEndTime();

		if (systemDate.compareTo(appointmentDate) == 0 && (systemTime.compareTo(appointmentStartTime) >= 0 && systemTime.compareTo(appointmentEndTime) <= 0)) {
			isDuringTimeSlot = true;
		}
		return isDuringTimeSlot;
	}


	/**
	 * Checks if the system date is the same as the appointment date
	 * @author antoninguerre
	 */
	// line 141 "../../../../../FlexiBookStates.ump"
	private boolean sameDay(){
		boolean sameDay = false;

		Date appointmentDate = this.getTimeSlot().getStartDate();
		Date systemDate = SystemTime.getSystemDate();

		if (appointmentDate.compareTo(systemDate) == 0) {
			sameDay = true;
			return sameDay;
		}

		return sameDay;
	}


	/**
	 * Checks if a time slot is available
	 * @author antoninguerre
	 */
	// line 159 "../../../../../FlexiBookStates.ump"
	private boolean isTimeSlotAvailable(TimeSlot ts){
		if (!(this.getTimeSlot().getStartTime().compareTo(ts.getStartTime()) == 0) || !(this.getTimeSlot().getEndTime().compareTo(ts.getEndTime()) == 0)) {

			String dateString = ts.getStartDate().toString();
			if(dateString.contains("2019")) {
				return false;
			}

			String dayOfTheWeek = new SimpleDateFormat("EEEE").format(ts.getStartDate());
			if (dayOfTheWeek.equals("Saturday") || dayOfTheWeek.equals("Sunday")) {
				return false;
			}

			for (BusinessHour bh : flexiBook.getHours()) {

				if (dayOfTheWeek.equals(bh.getDayOfWeek().toString())) {
					Time businessStartTime = bh.getStartTime();
					Time businessEndTime = bh.getEndTime();
					if (ts.getStartTime().before(businessStartTime) || ts.getEndTime().after(businessEndTime)) {
						return false;
					}
				}
			}

			for (Appointment appointment : flexiBook.getAppointments()) {
				TimeSlot appDowntime = getAppointmentDowntime(appointment);

				if (ts.getStartDate().equals(appointment.getTimeSlot().getStartDate())) {

					if ((ts.getStartTime().after(appointment.getTimeSlot().getStartTime()) 
							&& ts.getStartTime().before(appointment.getTimeSlot().getEndTime())) 
							|| (ts.getEndTime().after(appointment.getTimeSlot().getStartTime()) 
									&& ts.getEndTime().before(appointment.getTimeSlot().getEndTime()))
							|| (ts.getStartTime().before(appointment.getTimeSlot().getStartTime())
									&& ts.getEndTime().after(appointment.getTimeSlot().getEndTime()))
							|| (ts.getStartTime().equals(appointment.getTimeSlot().getStartTime()))
							|| (ts.getEndTime().equals(appointment.getTimeSlot().getEndTime()))) {

						if (appointment.equals(this))
							break;

						if (appDowntime != null) {
							if (ts.getStartTime().after(appDowntime.getStartTime()) && ts.getEndTime().before(appDowntime.getEndTime())
									|| (ts.getStartTime().equals(appDowntime.getStartTime()) && ts.getEndTime().equals(appDowntime.getEndTime()))) {
								break;
							}
						}
						return false;
					}
				}
			}

			for (int i = 0; i < flexiBook.getBusiness().getHolidays().size(); i++) {
				TimeSlot holiday = flexiBook.getBusiness().getHoliday(i);
				TimeSlot holidayFirst = flexiBook.getBusiness().getHoliday(0);
				TimeSlot holidayLast = flexiBook.getBusiness().getHoliday(flexiBook.getBusiness().getHolidays().size()-1);

				if(holidayFirst.getStartTime().after(ts.getEndTime()))
					break;

				if(holidayLast.getEndTime().before(ts.getStartTime()))
					break;

				for (LocalDate ld = holiday.getStartDate().toLocalDate(); ld.isBefore(holiday.getEndDate().toLocalDate().plusDays(1)); ld = ld.plusDays(1)) {
					Date date1 = Date.valueOf(ld);
					if(date1.compareTo(ts.getStartDate()) == 0) {
						return false;
					}
				}
			}

			for (int i=0; i<flexiBook.getBusiness().getVacation().size(); i++) {
				TimeSlot vacation = flexiBook.getBusiness().getVacation(i);
				TimeSlot vacationFirst = flexiBook.getBusiness().getVacation(0);
				TimeSlot vacationLast = flexiBook.getBusiness().getVacation(flexiBook.getBusiness().getVacation().size()-1);

				if(vacationFirst.getStartTime().after(ts.getEndTime()))
					break;

				if(vacationLast.getEndTime().before(ts.getStartTime()))
					break;

				for (LocalDate ld = vacation.getStartDate().toLocalDate(); ld.isBefore(vacation.getEndDate().toLocalDate().plusDays(1)); ld = ld.plusDays(1)) {
					Date date1 = Date.valueOf(ld);
					if(date1.compareTo(ts.getStartDate())==0) {
						return false;
					}
				}
			}


		}
		return true;
	}


	/**
	 * Gets the downtime in an appointment
	 * @author antoninguerre
	 */
	// line 259 "../../../../../FlexiBookStates.ump"
	private static  TimeSlot getAppointmentDowntime(Appointment appointment){
		long nonDowntimeItemsDuration = 0;
		LocalTime localAppDowntimeStart = null;
		LocalTime localAppDowntimeEnd = null;

		for (int i=0; i<appointment.getChosenItems().size(); i++) {
			if (appointment.getChosenItem(i).getService().getDowntimeDuration() != 0) {
				long downtimeStart = appointment.getChosenItem(i).getService().getDowntimeStart();
				localAppDowntimeStart = appointment.getTimeSlot().getStartTime().toLocalTime().plusMinutes(nonDowntimeItemsDuration).plusMinutes(downtimeStart);
				localAppDowntimeEnd = localAppDowntimeStart.plusMinutes(appointment.getChosenItem(i).getService().getDowntimeDuration());
			}
			else {
				nonDowntimeItemsDuration += appointment.getChosenItem(i).getService().getDuration();
			}
		}

		if (localAppDowntimeStart!=null && localAppDowntimeEnd!=null) {
			Time appDowntimeStart = Time.valueOf(localAppDowntimeStart);
			Time appDowntimeEnd = Time.valueOf(localAppDowntimeEnd);
			TimeSlot downtimeTimeSlot = new TimeSlot(appointment.getTimeSlot().getStartDate(), appDowntimeStart, appointment.getTimeSlot().getStartDate(), appDowntimeEnd, appointment.getFlexiBook());
			return downtimeTimeSlot;
		}
		else 
			return null;
	}

	//------------------------
	// DEVELOPER CODE - PROVIDED AS-IS
	//------------------------

	// line 25 "../../../../../FlexiBookPersistence.ump"
	private static final long serialVersionUID = 23150726028790501L ;


}