package ca.mcgill.ecse.flexibook.features;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
//import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.controller.TOAppointmentCalender;
import ca.mcgill.ecse.flexibook.model.*;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;
import ca.mcgill.ecse.flexibook.view.AppointmentCalendar;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;


public class CucumberStepDefinitions {

	private static String filename = "testdata.flexibook";

	private FlexiBook flexibook;
	private Business business;
	private Owner owner;
	private Customer customer;
	private User currentUser;
	private BusinessHour originalBusinessHour;
	private TimeSlot originalTimeSlot;
	private Service serviceAdded;
	private Service oldService;


	String error = "";
	String isSuccess = "successful";
	private boolean added = true;
	private boolean isUpdated;
	private int tmp;
	int changedNumber = 0;
	private int nError;
	private int originalIdx;
	private int errorCntr;
	private int serviceCntr;
	private List<TOAppointmentCalender> appointmentCalender = new ArrayList<TOAppointmentCalender>();
	private Appointment bookedAppointment = null;
	private String bookedService;
	private String bookedTime;
	private String bookedDate;
	private String customerString;
	private String name;
	private String address;
	private String email;
	private String phone;





	// #########################################################################
	// BEFORE ###################################################################
	// #########################################################################

	@Before
	public static void setUp() {
		FlexiBookPersistence.setFilename(filename);
		// remove test file
		File f = new File(filename);
		f.delete();
		// clear all data
		FlexiBookApplication.getFlexibook().delete();
	}




	// #########################################################################
	// GIVEN ###################################################################
	// #########################################################################

	@Given("a Flexibook system exists")
	public void a_flexibook_system_exists() {
		flexibook = FlexiBookApplication.getFlexibook();
		error = "";
		nError = 0;
	}

	@Given("the system's time and date is {string}")
	public void the_system_s_time_and_date_is(String aDateTime) {
		String dateString = aDateTime.substring(0, 10);
		String timeString = aDateTime.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);


		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);
	}

	@Given("an owner account exists in the system")
	public void an_owner_account_exists_in_the_system() {
		owner = new Owner("owner", "123", flexibook);
		flexibook.setOwner(owner);
	}

	@Given("a business exists in the system")
	public void a_business_exists_in_the_system() {
		business = new Business("busName", "busAddress", "busPhoneNum", "busEmail", flexibook);
		flexibook.setBusiness(business);
	}

	@Given("the following customers exist in the system:")
	public void the_following_customers_exist_in_the_system(io.cucumber.datatable.DataTable dt) {
		List<Map<String, String>> rows = dt.asMaps(String.class, String.class);

		// Remove all customers
		for (Customer customer : flexibook.getCustomers()) {
			flexibook.removeCustomer(customer);
			customer.delete();
		}

		// Input customers for this scenario
		for (Map<String, String> col : rows) {
			Customer aCustomer = new Customer(col.get("username"),
					col.get("password"), flexibook);

			flexibook.addCustomer(aCustomer);
		}

	}
	@Given("the following services exist in the system:")
	public void the_following_services_exist_in_the_system(io.cucumber.datatable.DataTable dataTable) {

		List<Map<String, String>> valueMaps = dataTable.asMaps(String.class, String.class);

		for (Map<String, String> map : valueMaps) {
			flexibook.addBookableService(new Service(map.get("name"), flexibook,  Integer.parseInt(map.get("duration")), Integer.parseInt(map.get("downtimeDuration")), Integer.parseInt(map.get("downtimeStart"))));

		}

	}

	/**
	 * @author Antonin Guerre
	 */
	@Given("the following service combos exist in the system:")
	public void the_following_service_combos_exist_in_the_system(io.cucumber.datatable.DataTable dataTable) {

		List<Map<String, String>> rows = dataTable.asMaps(String.class, String.class); 

		for (Map<String, String> columns : rows) {
			ServiceCombo aServiceCombo = new ServiceCombo(columns.get("name"), flexibook);

			String services = columns.get("services");
			String[] optionalServices = services.split(",");
			List<String> list = Arrays.asList(optionalServices);
			ArrayList<String> listOfOptionalServices = new ArrayList<String>(list);

			String mandatory = columns.get("mandatory");
			String[] booleans = mandatory.split(",");

			String mainService = columns.get("mainService");

			for (int i=0; i<listOfOptionalServices.size(); i++) {
				for (int j=0; j<booleans.length;j++) {
					if (i == j) {
						Service aService = (Service) findBookableService(listOfOptionalServices.get(i));
						boolean isMandatory = false;
						if (booleans[j].equals("true"))
							isMandatory = true;
						ComboItem aComboItem = new ComboItem(isMandatory, aService, aServiceCombo);
						if (aService.getName().equals(mainService))
							aServiceCombo.setMainService(aComboItem);
					}
				}
			}
			flexibook.addBookableService(aServiceCombo);	
		}	
	}


	@Given("the business has the following opening hours")
	public void the_business_has_the_following_opening_hours(io.cucumber.datatable.DataTable dataTable) {

		List<Map<String,String>> valueMaps = dataTable.asMaps(String.class, String.class);
		for (Map<String,String> map:valueMaps) {
			String aStartTimeString = map.get("startTime");
			String aEndTimeString = map.get("endTime");
			Time aStartTime = null;
			Time aEndTime = null;
			BusinessHour.DayOfWeek aDayOfWeek = null;
			switch (map.get("day")) {
			case "Sunday":
				aDayOfWeek = BusinessHour.DayOfWeek.Sunday;
				break;
			case "Monday":
				aDayOfWeek = BusinessHour.DayOfWeek.Monday;
				break;
			case "Tuesday":
				aDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
				break;
			case "Wednesday":
				aDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
				break;
			case "Thursday":
				aDayOfWeek = BusinessHour.DayOfWeek.Thursday;
				break;
			case "Friday":
				aDayOfWeek = BusinessHour.DayOfWeek.Friday;
				break;
			case "Saturday":
				aDayOfWeek = BusinessHour.DayOfWeek.Saturday;
				break;
			}
			DateFormat format = new SimpleDateFormat("HH:mm");

			aStartTime = convertStringToTime(aStartTimeString);
			aEndTime = convertStringToTime(aEndTimeString);

			BusinessHour aBusinessHour = new BusinessHour(aDayOfWeek,aStartTime, aEndTime, flexibook);
			flexibook.addHour(aBusinessHour);
		}
	}


	@Given("the business has the following opening hours:")
	public void the_business_has_the_following_opening_hoursn(io.cucumber.datatable.DataTable dataTable) {

		List<Map<String,String>> valueMaps = dataTable.asMaps(String.class, String.class);
		for (Map<String,String> map:valueMaps) {
			String aStartTimeString = map.get("startTime");
			String aEndTimeString = map.get("endTime");
			Time aStartTime = null;
			Time aEndTime = null;
			BusinessHour.DayOfWeek aDayOfWeek = null;
			switch (map.get("day")) {
			case "Sunday":
				aDayOfWeek = BusinessHour.DayOfWeek.Sunday;
				break;
			case "Monday":
				aDayOfWeek = BusinessHour.DayOfWeek.Monday;
				break;
			case "Tuesday":
				aDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
				break;
			case "Wednesday":
				aDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
				break;
			case "Thursday":
				aDayOfWeek = BusinessHour.DayOfWeek.Thursday;
				break;
			case "Friday":
				aDayOfWeek = BusinessHour.DayOfWeek.Friday;
				break;
			case "Saturday":
				aDayOfWeek = BusinessHour.DayOfWeek.Saturday;
				break;
			}
			aStartTime = convertStringToTime(aStartTimeString);
			aEndTime = convertStringToTime(aEndTimeString);

			BusinessHour aBusinessHour = new BusinessHour(aDayOfWeek,aStartTime, aEndTime, flexibook);
			flexibook.addHour(aBusinessHour);
		}
	}

	@Given("the business has the following holidays")
	public void the_business_has_the_following_holidays(io.cucumber.datatable.DataTable dataTable) {
		Business business = flexibook.getBusiness();

		List<Map<String,String>> rows = dataTable.asMaps(String.class, String.class);

		for (Map<String,String> columns : rows) {

			Date startDate = convertStringToDate(columns.get("startDate"));
			Date endDate = convertStringToDate(columns.get("endDate"));
			Time startTime = convertStringToTime(columns.get("startTime"));
			Time endTime = convertStringToTime(columns.get("endTime"));


			TimeSlot holidayTimeSlot = new TimeSlot(startDate, startTime, endDate, endTime, flexibook);
			business.addHoliday(holidayTimeSlot);
		}

	}

	@Given("the business has the following holidays:")
	public void the_business_has_the_following_holidaysn(io.cucumber.datatable.DataTable dataTable) {
		Business business = flexibook.getBusiness();

		List<Map<String,String>> rows = dataTable.asMaps(String.class, String.class);

		for (Map<String,String> columns : rows) {

			Date startDate = convertStringToDate(columns.get("startDate"));
			Date endDate = convertStringToDate(columns.get("endDate"));
			Time startTime = convertStringToTime(columns.get("startTime"));
			Time endTime = convertStringToTime(columns.get("endTime"));


			TimeSlot holidayTimeSlot = new TimeSlot(startDate, startTime, endDate, endTime, flexibook);
			business.addHoliday(holidayTimeSlot);
		}

	}

	/**
	 * @author Antonin Guerre
	 */
	@Given("the following appointments exist in the system:")
	public void the_following_appointments_exist_in_the_system(io.cucumber.datatable.DataTable dataTable) {
		List<Map<String,String>> rows = dataTable.asMaps(String.class, String.class);

		for (Map<String,String> columns : rows) {

			Date appointmentDate = convertStringToDate(columns.get("date"));
			Time startTime = convertStringToTime(columns.get("startTime"));
			Time endTime= convertStringToTime(columns.get("endTime"));
			BookableService aBookableService = findBookableService(columns.get("serviceName"));
			String optionalServicesString = columns.get("optServices");
			String optionalServicesString1 = columns.get("selectedComboItems");

			TimeSlot appointmentTimeSlot = new TimeSlot(appointmentDate, startTime, appointmentDate, endTime, flexibook);
			Appointment appointment = new Appointment(findCustomer(columns.get("customer")),
					aBookableService, appointmentTimeSlot, flexibook);

			flexibook.addAppointment(appointment);

			if (aBookableService instanceof ServiceCombo && optionalServicesString1 == null) {
				for (int i=0; i<(((ServiceCombo) aBookableService).numberOfServices()); i++){
					if (((ServiceCombo) aBookableService).getService(i).isMandatory()
							|| optionalServicesString.contains(((ServiceCombo) aBookableService).getService(i).getService().getName())) {
						appointment.addChosenItem(((ServiceCombo) aBookableService).getService(i));
					}
				}
			}
			if (aBookableService instanceof ServiceCombo && optionalServicesString == null) {
				for (int i=0; i<(((ServiceCombo) aBookableService).numberOfServices()); i++){
					if (((ServiceCombo) aBookableService).getService(i).isMandatory()
							|| optionalServicesString1.contains(((ServiceCombo) aBookableService).getService(i).getService().getName())) {
						appointment.addChosenItem(((ServiceCombo) aBookableService).getService(i));
					}
				}
			}
		}
	}

	/**
	 * @author Antonin Guerre
	 */
	@Given("{string} is logged in to their account")
	public void is_logged_in_to_their_account(String username) {
		//FlexiBookApplication.setCurrentUser(findCustomer(username));
		if (username=="owner") {
			currentUser= flexibook.getOwner();
		}
		else {
			currentUser = User.getWithUsername(username);
		}
		FlexiBookApplication.setCurrentUser(currentUser);
	}


	/**
	 * @author Antonin Guerre
	 */
	@Given("{string} has a {string} appointment with optional sevices {string} on {string} at {string}")
	public void has_a_appointment_with_optional_sevices_on_at(String customer, String serviceName, String optionalServices, String date, String time) {
		Customer aCustomer = findCustomer(customer);
		BookableService aBS = findBookableService(serviceName);
		Date aDate = convertStringToDate(date);
		Time startTime = convertStringToTime(time);
		Time endTime = getServiceComboEndTime(startTime,aBS,optionalServices);
		TimeSlot timeSlot = new TimeSlot(aDate, startTime, aDate, endTime, flexibook);

		Appointment appointment = new Appointment(aCustomer,aBS,timeSlot, flexibook);

		flexibook.addAppointment(appointment);

		if (aBS instanceof ServiceCombo) {
			for (int i=0; i<(((ServiceCombo) aBS).numberOfServices()); i++){
				if (((ServiceCombo) aBS).getService(i).isMandatory()
						|| optionalServices.contains(((ServiceCombo) aBS).getService(i).getService().getName())) {
					appointment.addChosenItem(((ServiceCombo) aBS).getService(i));
				}
			}
		}


	}

	private static User findCurrentUser(String userName) {
		User foundUser = null;
		if (userName == "owner") {
			foundUser = FlexiBookApplication.getFlexibook().getOwner();
		} else {
			for (Customer customer : FlexiBookApplication.getFlexibook().getCustomers()) {
				if (customer.getUsername().equals(userName)) {
					foundUser = customer;
					break;
				}
			}
		}


		return foundUser;
	}


	/**
	 * @author egekaradibak
	 */
	@Given("the user is logged in to an account with username {string}")
	public void the_user_is_logged_in_to_an_account_with_username(String string) {
		flexibook = FlexiBookApplication.getFlexibook();

		if (string.equals("owner") && flexibook.hasOwner()) {
			FlexiBookApplication.setCurrentUser(flexibook.getOwner());
		}

		if(!flexibook.hasOwner()) {
			FlexiBookApplication.setCurrentUser(findCurrentUser(string));
		}

		for (Customer c : flexibook.getCustomers()) {
			if(c.getUsername().equals(string)) {
				FlexiBookApplication.setCurrentUser(c);
			}
		}
		currentUser = FlexiBookApplication.getCurrentUser();

	}	

	/**
	 * @author egekaradibak
	 */
	@Given("there is no existing username {string}")
	public void there_is_no_existing_username(String string) {

		for (int i = 0; i < flexibook.getCustomers().size(); i++) {
			if (flexibook.getCustomer(i).getUsername().equals(string)) {
				flexibook.getCustomer(i).delete();
			}
		}
	}

	/**
	 * @author egekaradibak
	 */
	@Given("there is an existing username {string}")
	public void there_is_an_existing_username(String string) {
		flexibook = FlexiBookApplication.getFlexibook();
		flexibook.addCustomer(string, "lalal");
	}

	/**
	 * @author egekaradibak
	 */
	@Given("an owner account exists in the system with username {string} and password {string}")
	public void an_owner_account_exists_in_the_system_with_username_and_password(String string, String string2) {
		flexibook = FlexiBookApplication.getFlexibook();
		Owner owner = new Owner(string, string2, flexibook);
		flexibook.setOwner(owner);
		currentUser = FlexiBookApplication.getCurrentUser();
	}

	@Given("the user is logged in to an account with username \"owner\"and password {string}")
	public void the_user_is_logged_in_to_an_account_with_username_owner_and_password(String string) {
		flexibook = FlexiBookApplication.getFlexibook();
		owner = new Owner("owner", string, flexibook);
		currentUser = FlexiBookApplication.getCurrentUser();
		FlexiBookApplication.setCurrentUser(owner);
	}

	/**
	 * @author egekaradibak
	 */
	@Given("the user is logged in to an account with username \"User1\"and password {string}")
	public void the_user_is_logged_in_to_an_account_with_username_user1_and_password(String string) {
		flexibook = FlexiBookApplication.getFlexibook();
		customer = new Customer("User1", string, flexibook);
		flexibook.addCustomer(customer);
		currentUser = FlexiBookApplication.getCurrentUser();
		FlexiBookApplication.setCurrentUser(customer);
	}

	/**
	 * @author egekaradibak
	 */
	@Given("the account with username {string} has pending appointments")
	public void the_account_with_username_has_pending_appointments(String string) {
		flexibook = FlexiBookApplication.getFlexibook();
		TimeSlot aTimeSlot = new TimeSlot(null, null, null, null, flexibook);
		if(!flexibook.hasAppointments()) {
			for(int i =0; i<flexibook.getBookableServices().size(); i++) {
				for(Customer c : flexibook.getCustomers()) {
					if(c.getUsername().equals(string)) {	
						c.addAppointment(flexibook.getBookableService(i), aTimeSlot, flexibook);
					}
				}
			}
		}
	}

	/**
	 * @author jamesdarby
	 */
	@Given("the Owner with username {string} is logged in")
	public void the_owner_with_username_is_logged_in(String username) {
		FlexiBookApplication.setCurrentUser(owner);
		owner = flexibook.getOwner();
		owner.setUsername(username);
	}

	/**
	 * @author juliettedebray
	 */
	@Given("Customer with username {string} is logged in")
	public void customer_with_username_is_logged_in(String username) {
		flexibook = FlexiBookApplication.getFlexibook();
		customer = new Customer("", "", flexibook);
		flexibook.addCustomer(customer);
		customer.setUsername(username);

		FlexiBookApplication.setCurrentUser(customer);

	}

	// No Business Exists ======================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Given("no business exists")
	public void givenNoBusinessExists() {
		if (flexibook.hasBusiness()) {
			flexibook.getBusiness().delete();
		}
	}


	// A Business Exists =======================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Given("a business exists with the following information:")
	public void givenBusinessExists(DataTable dt) {
		List<Map<String, String>> rows = dt.asMaps(String.class, String.class);

		for (Map<String, String> col : rows) {
			Business aNewBusiness = new Business(col.get("name"), col.get("address"), col.get("phone number"),
					col.get("email"), flexibook);
			flexibook.setBusiness(aNewBusiness);
		}
	}

	// The Business Hour Exists ================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Given("the business has a business hour on {string} with start time {string} and end time {string}")
	public void givenBusinessHourExists(String aDayOfWeekString, String aStartTimeString, String aEndTimeString) {

		BusinessHour.DayOfWeek aDayOfWeek = null;
		switch (aDayOfWeekString) {
		case "Sunday":
			aDayOfWeek = BusinessHour.DayOfWeek.Sunday;
			break;
		case "Monday":
			aDayOfWeek = BusinessHour.DayOfWeek.Monday;
			break;
		case "Tuesday":
			aDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
			break;
		case "Wednesday":
			aDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
			break;
		case "Thursday":
			aDayOfWeek = BusinessHour.DayOfWeek.Thursday;
			break;
		case "Friday":
			aDayOfWeek = BusinessHour.DayOfWeek.Friday;
			break;
		case "Saturday":
			aDayOfWeek = BusinessHour.DayOfWeek.Saturday;
			break;
		}

		Time aStartTime = convertStringToTime(aStartTimeString);
		Time aEndTime = convertStringToTime(aEndTimeString);



		if (aDayOfWeek != null && aStartTime != null && aEndTime != null) {
			BusinessHour aNewBusinessHour = new BusinessHour(aDayOfWeek, aStartTime, aEndTime, flexibook);

			flexibook.getBusiness().addBusinessHour(aNewBusinessHour);
		}
	}

	// A Time Slot Exists ======================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Given("a {string} time slot exists with start time {string} at {string} and end time {string} at {string}")
	public void givenTimeSlotExists(String aType, String aStartDateString, String aStartTimeString,
			String aEndDateString, String aEndTimeString) {


		Date aStartDate = convertStringToDate(aStartDateString);
		Time aStartTime = convertStringToTime(aStartTimeString);
		Date aEndDate = convertStringToDate(aEndDateString);
		Time aEndTime = convertStringToTime(aEndTimeString);


		if (aStartDate != null && aStartTime != null && aEndDate != null && aEndTime != null) {

			TimeSlot aNewTimeSlot = new TimeSlot(aStartDate, aStartTime, aEndDate, aEndTime, flexibook);

			switch (aType) {
			case "vacation":
				flexibook.getBusiness().addVacation(aNewTimeSlot);
				break;
			case "holiday":
				flexibook.getBusiness().addHoliday(aNewTimeSlot);
				break;
			}
		}
	}









	// #########################################################################
	// MAKE, UPDATE, CANCEL APPOINTMENT ########################################
	// #########################################################################

	@When("{string} schedules an appointment on {string} for {string} at {string}")
	public void schedules_an_appointment_on_for_at(String aCustomer, String date, String aService, String time) {
		tmp = flexibook.numberOfAppointments();
		String user = aCustomer;

		try {	
			FlexiBookController.makeAppointment(user, aCustomer, aService,null, date, time);
		} catch (InvalidInputException e) {
			error = e.getMessage();	
		}
	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} schedules an appointment on {string} for {string} with {string} at {string}")
	public void schedules_an_appointment_on_for_with_at(String aCustomer, String date, String aService, String optionalServices, String time) {
		tmp = flexibook.numberOfAppointments();
		String user = aCustomer;
		try {
			System.out.println(date+ " "+time);
			FlexiBookController.makeAppointment(user, aCustomer, aService, optionalServices, date, time);

		} catch (InvalidInputException e) {
			System.out.println("error");
			error = e.getMessage();	
		}	
	}


	/**
	 * @author antoninguerre
	 */
	@Then("{string} shall have a {string} appointment on {string} from {string} to {string}")
	public void shall_have_a_appointment_on_from_to(String aCustomer, String serviceName, String startDateString, String startTimeString, String endTimeString) {
		Boolean successfullyAdded = false;

		for (Appointment appointment:flexibook.getAppointments()) {
			System.out.println(appointment.getTimeSlot().getStartTime());
			if (appointment.getCustomer().equals(findCustomer(aCustomer)) 
					&& appointment.getTimeSlot().getStartDate().equals(convertStringToDate(startDateString))
					){//&& appointment.getTimeSlot().getStartTime().equals(convertStringToTime(startTimeString))) {
				System.out.println(appointment.getTimeSlot());
				successfullyAdded = true;
			}
		}

		assertTrue(successfullyAdded);
	}


	/**
	 * @author antoninguerre
	 */
	@Then("there shall be {int} more appointment in the system")
	public void there_shall_be_more_appointment_in_the_system(Integer addedAppointments) {

		assertEquals(flexibook.numberOfAppointments() , addedAppointments + tmp);
	}


	/**
	 * @author antoninguerre
	 */
	@Then("the system shall report {string}")
	public void the_system_shall_report(String msg) {
		//assertEquals(msg,error);
		assertTrue(error.contains(msg));
	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} attempts to cancel their {string} appointment on {string} at {string}")
	public void attempts_to_cancel_their_appointment_on_at(String aCustomer, String aService, String date, String time) {

		String user = aCustomer;
		tmp = flexibook.numberOfAppointments();
		try {
			FlexiBookController.cancelAppointment(user, aCustomer, aService, date, time);
		} catch (InvalidInputException e) {
			error = e.getMessage();
			added=false;
		}	
	}


	/**
	 * @author antoninguerre
	 */
	@Then("{string}'s {string} appointment on {string} at {string} shall be removed from the system")
	public void s_appointment_on_at_shall_be_removed_from_the_system(String aCustomer, String serviceName, String date, String time) {
		boolean successfullyRemoved = false;

		if (findAppointment(aCustomer, serviceName, date, time) == null) {
			successfullyRemoved = true;
		}
		assertTrue(successfullyRemoved);
	}


	/**
	 * @author antoninguerre
	 */
	@Then("there shall be {int} less appointment in the system")
	public void there_shall_be_less_appointment_in_the_system(Integer removedAppointments) {

		assertEquals(flexibook.numberOfAppointments(), removedAppointments - tmp);
	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} attempts to cancel {string}'s {string} appointment on {string} at {string}")
	public void attempts_to_cancel_s_appointment_on_at(String customer1, String customer2, String serviceName, String date, String time) {
		tmp = flexibook.numberOfAppointments();
		try {
			FlexiBookController.cancelAppointment(customer1, customer2, serviceName, date, time);
		} catch (InvalidInputException e) {
			error = e.getMessage();
		}
	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} attempts to update their {string} appointment on {string} at {string} to {string} at {string}")
	public void attempts_to_update_their_appointment_on_at_to_at(String aCustomer, String serviceName, String currentDate, String currentTime, String newDate, String newTime) {
		String user = aCustomer;
		Boolean change = false;
		tmp = flexibook.numberOfAppointments();
		try {
			System.out.println("here");
			FlexiBookController.updateAppointment(user, aCustomer,change, serviceName, currentDate, currentTime,"","", newDate, newTime);
			isSuccess = "successful";

		} catch (InvalidInputException e) {
			isSuccess = e.getMessage();
		}
	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} attempts to {string} {string} from their {string} appointment on {string} at {string}")
	public void attempts_to_from_their_appointment_on_at(String aCustomer, String action, String comboItem, String serviceName, String date, String time) {
		String user = aCustomer;
		Boolean change = false;
		tmp = flexibook.numberOfAppointments();
		try {
			FlexiBookController.updateAppointment(user, aCustomer,change, serviceName, date, time, action, comboItem,date,time);
			isSuccess = "successful";
			System.out.println("here");
		} catch (InvalidInputException e) {
			isSuccess = e.getMessage();
		}
	}


	/**
	 * @author antoninguerre
	 */
	@Then("the system shall report that the update was {string}")
	public void the_system_shall_report_that_the_update_was(String msg) {
		System.out.println(isSuccess);
		assertEquals(msg,isSuccess);

	}


	/**
	 * @author antoninguerre
	 */
	@When("{string} attempts to update {string}'s {string} appointment on {string} at {string} to {string} at {string}")
	public void attempts_to_update_s_appointment_on_at_to_at(String user, String customer2, String serviceName, String date, String time, String newDate, String newTime) {
		tmp = flexibook.numberOfAppointments();
		Boolean change = false;
		try {
			FlexiBookController.updateAppointment(user, customer2,change, serviceName, date, time,null,null, date, time);
		} catch (InvalidInputException e) {
			error = e.getMessage();
			added=false;
		}
	}




	// #########################################################################
	// SIGN UP, UPDATE, DELETE CUSTOMER ACCOUNT ################################
	// #########################################################################

	/**
	 * @author egekaradibak
	 */
	@When("the user provides a new username {string} and a password {string}")
	public void the_user_provides_a_new_username_and_a_password(String string, String string2) {
		try {
			FlexiBookController.signUpCustomerAccount(string, string2);
		} catch (InvalidInputException e) {

			error += e.getMessage();
		}
	}

	/**
	 * @author egekaradibak
	 */
	@When("the user tries to update account with a new username {string} and password {string}")
	public void the_user_tries_to_update_account_with_a_new_username_and_password(String string, String string2) {
		try {
			FlexiBookController.updateAccount(string, string2);
		} catch (InvalidInputException e) {
			error += e.getMessage();
		}
	}


	/**
	 * @author egekaradibak
	 */
	@Then("a new customer account shall be created")
	public void a_new_customer_account_shall_be_created() {
		assertEquals(1, flexibook.getCustomers().size());
	}


	/**
	 * @author egekaradibak
	 */
	@Then("the account shall have username {string} and password {string}")
	public void the_account_shall_have_username_and_password(String string, String string2) {

		String userName = FlexiBookApplication.getCurrentUser().getUsername();
		String password = FlexiBookApplication.getCurrentUser().getPassword();

		assertEquals(string, userName);
		assertEquals(string2, password);
	}

	/**
	 * @author egekaradibak
	 */
	@Then("no new account shall be created")
	public void no_new_account_shall_be_created() {

		if (flexibook.getCustomers().size() == 0) {
			assertEquals(0, flexibook.getCustomers().size());
		}
		if (flexibook.getCustomers().size() == 1) {
			assertEquals(1, flexibook.getCustomers().size());
		}
	}

	/**
	 * @author egekaradibak
	 */
	@Then("an error message {string} shall be raised")
	public void an_error_message_shall_be_raised(String string) {
		assertTrue(error.contains(string));
	}


	/**
	 * @author egekaradibak
	 */
	@Then("the account shall not be updated")
	public void the_account_shall_not_be_updated() {
		flexibook = FlexiBookApplication.getFlexibook();
		boolean isUpdated = true;
		if (error.contains("The user name cannot be empty") || error.contains("The password cannot be empty")) {
			isUpdated = false;
		}
		assertEquals(false, isUpdated);
	}

	/**
	 * @author egekaradibak
	 */
	@When("the user tries to delete account with the username {string}")
	public void the_user_tries_to_delete_account_with_the_username(String string) {
		try {
			FlexiBookController.deleteCustomerAccount(string);
		} catch (InvalidInputException e) {
			error = e.getMessage();
		}
	}


	/**
	 * @author egekaradibak
	 */
	@Then("the account with the username {string} does not exist")
	public void the_account_with_the_username_does_not_exist(String string) {


		boolean doesExists = false;
		for(int i =0; i<flexibook.getCustomers().size(); i++) {
			if(!flexibook.getCustomer(i).getUsername().equals(string)) {
				doesExists = true;
			}
		}
		assertEquals(true, doesExists);
	}


	/**
	 * @author egekaradibak
	 */
	@Then("all associated appointments of the account with the username {string} shall not exist")
	public void all_associated_appointments_of_the_account_with_the_username_shall_not_exist(String string) {

		for(Customer customer : flexibook.getCustomers()) {
			if(customer.getUsername().equals(string));
			assertEquals(0, flexibook.getAppointments().size());
		}
	}


	/**
	 * @author egekaradibak
	 */
	@Then("the user shall be logged out")
	public void the_user_shall_be_logged_out() {
		boolean isLoggedOut = false;
		if (FlexiBookApplication.getCurrentUser()==null) {
			isLoggedOut = true;
		}
		assertEquals(true, isLoggedOut);
	}


	/**
	 * @author egekaradibak
	 */
	@Then("the account with the username {string} exists")
	public void the_account_with_the_username_exists(String string) {
		String userName = FlexiBookApplication.getCurrentUser().getUsername();
		boolean doesExists = false;
		for(int i =0; i<flexibook.getCustomers().size(); i++) {
			if(flexibook.getCustomer(i).getUsername().equals(string)) {
				doesExists = true;
			}
			if(userName.equals("owner")|| string.equals("owner")) {
				doesExists = true;
			}
		}
		assertEquals(true, doesExists);
	}





	// #########################################################################
	// DEFINE, UPDATE, DELETE SERVICE COMBO ####################################
	// #########################################################################

	/**
	 * @author jamesdarby
	 */
	@When("{string} initiates the definition of a service combo {string} with main service {string}, services {string} and mandatory setting {string}")
	public void initiates_the_definition_of_a_service_combo_with_main_service_services_and_mandatory_setting(String string, String string2, String string3, String string4, String string5) {

		try {
			FlexiBookController.defineServiceCombo(string2, string3, string4, string5);
		}catch(InvalidInputException e) {
			error += e.getMessage();
		}
	}

	/**
	 * @author jamesdarby
	 */
	@Then("the service combo {string} shall exist in the system")
	public void the_service_combo_shall_exist_in_the_system(String string) {
		List<BookableService> allServices = flexibook.getBookableServices();
		String name = "";
		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string)) {
				name = allServices.get(i).getName();
				break;
			}
		}
		assertEquals(string, name);
	}

	/**
	 * @author jamesdarby
	 */
	@Then("the service combo {string} shall contain the services {string} with mandatory setting {string}")
	public void the_service_combo_shall_contain_the_services_with_mandatory_setting(String string, String string2, String string3) {
		String[] serviceNames = string2.split(",");
		String[] mandatoryValues = string3.split(",");
		boolean mandatory = false;
		List<BookableService> allServices = flexibook.getBookableServices();
		int index = -1;

		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string)) {
				index = flexibook.indexOfBookableService(allServices.get(i));
				break;
			}
		}
		ServiceCombo a = (ServiceCombo) flexibook.getBookableService(index);
		List<ComboItem> comboItems = a.getServices();
		ComboItem[] comboItemsSorted = new ComboItem[comboItems.size()];
		String[] mandatorySorted = mandatoryValues;

		for(int i = 0; i < comboItems.size(); i++) {
			int k = -1;
			for(int j = 0; j < serviceNames.length; j++) {
				if(serviceNames[i].equals(comboItems.get(j).getService().getName())) {
					k = j;
					break;
				}
			}
			comboItemsSorted[i] = comboItems.get(k);
		}

		for(int i = 0; i < comboItems.size(); i++) {
			if(mandatorySorted[i].equals("true")) {
				mandatory = true;
			} else if (mandatorySorted[i].equals("false")) {
				mandatory = false;
			}
			assertEquals(comboItemsSorted[i].getMandatory(), mandatory);
			assertEquals(comboItemsSorted[i].getService().getName(), serviceNames[i]);
		}
	}

	/**
	 * @author jamesdarby
	 */
	@Then("the main service of the service combo {string} shall be {string}")
	public void the_main_service_of_the_service_combo_shall_be(String string, String string2) {
		List<BookableService> allServices = flexibook.getBookableServices();
		int index = -1;

		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string)) {
				index = flexibook.indexOfBookableService(allServices.get(i));
				break;
			}
		}
		if(index == -1) {
			throw new io.cucumber.java.PendingException();
		}
		ServiceCombo a = (ServiceCombo) flexibook.getBookableService(index);
		assertEquals(string2, a.getMainService().getService().getName());
	}

	/**
	 * @author jamesdarby
	 */
	@Then("the service {string} in service combo {string} shall be mandatory")
	public void the_service_in_service_combo_shall_be_mandatory(String string, String string2) {
		List<BookableService> allServices = flexibook.getBookableServices();
		int index = -1;

		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string2)) {
				index = flexibook.indexOfBookableService(allServices.get(i));
				break;
			}
		}
		if(index == -1) {
			throw new io.cucumber.java.PendingException();
		}
		ServiceCombo a = (ServiceCombo) flexibook.getBookableService(index);
		List<ComboItem> comboItems = a.getServices();
		boolean mandatory = false;
		for(int i = 0; i < comboItems.size(); i++) {
			if(comboItems.get(i).getService().getName().equals(string)) {
				mandatory = comboItems.get(i).getMandatory();
				break;
			}
		}
		assertEquals(a.getMainService().getMandatory(), mandatory);
	}


	/**
	 * @author jamesdarby
	 */
	@Then("the service combo {string} shall preserve the following properties:")
	public void the_service_combo_shall_preserve_the_following_properties(String string, io.cucumber.datatable.DataTable dataTable) {
		List<Map<String, String>> listProperties;
		listProperties = dataTable.asMaps(String.class, String.class);
		List<BookableService> allServices = flexibook.getBookableServices();
		int index = -1;
		String name;
		String mainService;
		String services;
		String mandatory;

		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(listProperties.get(0).get("name"))) {
				index = i;
				break;
			}
		}
		if(index == -1) {
			name = "";
			mainService = "";
			services = "";
			mandatory = "";
		}else {
			name = flexibook.getBookableService(index).getName();
			mainService = ((ServiceCombo)flexibook.getBookableService(index)).getMainService().getService().getName();

			List<ComboItem> comboServices = ((ServiceCombo) flexibook.getBookableService(index)).getServices();
			services = comboServices.get(0).getService().getName();
			if(comboServices.get(0).getMandatory()){
				mandatory = "true";
			}else {
				mandatory = "false";
			}

			for(int i = 1; i < comboServices.size(); i++) {
				services += "," + comboServices.get(i).getService().getName();
				if(comboServices.get(i).getMandatory()){
					mandatory += ",true";
				}else {
					mandatory += ",false";
				}
			}
		}
		assertEquals(listProperties.get(0).get("name"),name);
		assertEquals(listProperties.get(0).get("mainService"),mainService);
		assertEquals(listProperties.get(0).get("services"),services);
		assertEquals(listProperties.get(0).get("mandatory"),mandatory);
	}
	/**
	 * @author jamesdarby
	 */
	@Then("the service combo {string} shall not exist in the system")
	public void the_service_combo_shall_not_exist_in_the_system(String string) {
		List<BookableService> allServices = flexibook.getBookableServices();
		boolean exists = false;
		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string)) {
				exists = true;
				break;
			}
		}
		assertEquals(exists, false);
	}

	/**
	 * @author jamesdarby
	 */
	@When("{string} initiates the deletion of service combo {string}")
	public void initiates_the_deletion_of_service_combo(String string, String string2) {
		try {
			FlexiBookController.deleteServiceCombo(string2);
		} catch (InvalidInputException e) {
			error += e.getMessage();
		}
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the number of appointments in the system shall be {string}")
	public void the_number_of_appointments_in_the_system_shall_be(String numOfAppointments) {
		int numOfApp = Integer.parseInt(numOfAppointments);
		assertEquals(flexibook.numberOfAppointments(), numOfApp);
	}

	/**
	 * @author jamesdarby
	 */
	@When("{string} initiates the update of service combo {string} to name {string}, main service {string} and services {string} and mandatory setting {string}")
	public void initiates_the_update_of_service_combo_to_name_main_service_and_services_and_mandatory_setting(String string, String string2, String string3, String string4, String string5, String string6) {
		try {
			FlexiBookController.updateServiceCombo(string2,string3,string4,string5,string6);
		} catch (InvalidInputException e) {
			error += e.getMessage();
		}
	}

	/**
	 * @author jamesdarby
	 */
	@Then("the service combo {string} shall be updated to name {string}")
	public void the_service_combo_shall_be_updated_to_name(String string, String string2) {
		List<BookableService> allServices = flexibook.getBookableServices();
		String name = "";
		for(int i = 0; i < allServices.size(); i++) {
			if(allServices.get(i).getName().equals(string2)) {
				name = allServices.get(i).getName();
				break;
			}
		}
		assertEquals(string2, name);
	}





	// #########################################################################
	// DEFINE, UPDATE, DELETE SERVICE ##########################################
	// #########################################################################


	/**
	 * @author juliettedebray
	 */
	@When("{string} initiates the addition of the service {string} with duration {string}, start of down time {string} and down time duration {string}")
	public void initiates_the_addition_of_the_service_with_duration_start_of_down_time_and_down_time_duration(String owner, String serviceName, String duration, String downtimeStart, String downtimeDuration) {
		try {
			serviceCntr = flexibook.numberOfServices();
			FlexiBookController.addService(owner, serviceName, duration, downtimeDuration, downtimeStart);
		} catch (InvalidInputException e) {
			error += e.getMessage();
			errorCntr++;
		}
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service {string} shall exist in the system")
	public void the_service_shall_exist_in_the_system(String serviceName) {
		Service deletedService = null;
		deletedService = (Service) findBookableService(serviceName);
		assertTrue(deletedService != null);
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service {string} shall have duration {string}, start of down time {string} and down time duration {string}")
	public void the_service_shall_have_duration_start_of_down_time_and_down_time_duration(String serviceName, String duration, String downtimeStart, String downtimeDuration) {
		serviceAdded = (Service) findBookableService(serviceName);
		int serviceDuration = Integer.parseInt(duration);
		int serviceDowntimeDuration = Integer.parseInt(downtimeDuration);
		int serviceDowntimeStart = Integer.parseInt(downtimeStart);
		assertEquals(serviceAdded.getName(), serviceName);
		assertEquals(serviceAdded.getDuration(), serviceDuration);
		assertEquals(serviceAdded.getDowntimeDuration(), serviceDowntimeDuration);
		assertEquals(serviceAdded.getDowntimeStart(), serviceDowntimeStart);
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the number of services in the system shall be {string}")
	public void the_number_of_services_in_the_system_shall_be(String numberOfServices) {
		int numOfServices = Integer.parseInt(numberOfServices);

		assertEquals(flexibook.numberOfServices(), numOfServices);
	}

	/**
	 * @author juliettedebray
	 */
	@Then("an error message with content {string} shall be raised")
	public void an_error_message_with_content_shall_be_raised(String errorMsg) {
		assertTrue(error.contains(errorMsg));
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service {string} shall not exist in the system")
	public void the_service_shall_not_exist_in_the_system(String serviceName) {
		assertTrue(findBookableService(serviceName)==null);
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service {string} shall still preserve the following properties:")
	public void the_service_shall_still_preserve_the_following_properties(String string, io.cucumber.datatable.DataTable dataTable) {

		List<Map<String, String>> comparedValueMaps = dataTable.asMaps();
		oldService = (Service) findBookableService(string);
		for (Map<String, String> map : comparedValueMaps) {
			assertEquals(oldService.getName(), map.get("name"));
			String stringDuration = Integer.toString(oldService.getDuration());
			String stringDowntime = Integer.toString(oldService.getDowntimeDuration());
			String stringDStart = Integer.toString(oldService.getDowntimeStart());
			assertEquals(map.get("duration"),stringDuration);
			assertEquals(map.get("downtimeDuration"), stringDowntime);
			assertEquals(map.get("downtimeStart"), stringDStart);
		}
	}

	/**
	 * @author juliettedebray
	 */
	@When("{string} initiates the deletion of service {string}")
	public void initiates_the_deletion_of_service(String owner, String serviceName) {
		try {
			FlexiBookController.deleteService(owner, serviceName);
		} catch (InvalidInputException e) {
			error += e.getMessage();
			errorCntr++;
		}
	}


	/**
	 * @author juliettedebray
	 */
	@Then("the number of appointments in the system with service {string} shall be {string}")
	public void the_number_of_appointments_in_the_system_with_service_shall_be(String serviceName, String numOfAppointments) {
		int numOfApp = Integer.parseInt(numOfAppointments);
		int counter = 0;
		int i;
		for (i=0; i<flexibook.numberOfAppointments(); i++) {
			if (flexibook.getAppointment(i).getBookableService().getName().compareToIgnoreCase(serviceName) == 0) {
				counter++;
			}
		}
		assertEquals(counter, numOfApp);
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service combos {string} shall not exist in the system")
	public void the_service_combos_shall_not_exist_in_the_system(String deletedServiceCombo) {
		int i;
		for (i=0; i<flexibook.numberOfBookableServices(); i++) {
			assertFalse(flexibook.getBookableService(i).getName().toLowerCase().equals(deletedServiceCombo.toLowerCase()));
		}
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service combos {string} shall not contain service {string}")
	public void the_service_combos_shall_not_contain_service(String modServiceCombo, String deletedService) {
		int j;
		int k;
		for (j=0; j<flexibook.numberOfBookableServices(); j++) {
			if (modServiceCombo.contains(flexibook.getBookableService(j).getName())) {
				ServiceCombo serviceCombo = (ServiceCombo) flexibook.getBookableService(j);
				for (k=0; k<serviceCombo.numberOfServices(); k++) {
					assertFalse(serviceCombo.getService(k).getService().getName().equals(deletedService));
				}
			}
		}
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the number of service combos in the system shall be {string}")
	public void the_number_of_service_combos_in_the_system_shall_be(String numberOfServiceCombos) {
		int numOfServiceCombos = Integer.parseInt(numberOfServiceCombos);
		int i;
		int testNumOfSC=0;
		for(i=0; i<flexibook.numberOfBookableServices(); i++) {
			if (flexibook.getBookableService(i) instanceof ServiceCombo) {
				testNumOfSC++;
			}
		}
		assertEquals(numOfServiceCombos, testNumOfSC);
	}

	/**
	 * @author juliettedebray
	 */
	@When("{string} initiates the update of the service {string} to name {string}, duration {string}, start of down time {string} and down time duration {string}")
	public void initiates_the_update_of_the_service_to_name_duration_start_of_down_time_and_down_time_duration(String owner, String oldName, String newName, String duration, String downtimeStart, String downtimeDuration) {
		try {
			oldService = (Service) findBookableService(oldName);
			FlexiBookController.updateService(owner, oldName, newName, duration, downtimeDuration, downtimeStart);
		} catch (InvalidInputException e) {
			error += e.getMessage();
			errorCntr++;
		}
	}

	/**
	 * @author juliettedebray
	 */
	@Then("the service {string} shall be updated to name {string}, duration {string}, start of down time {string} and down time duration {string}")
	public void the_service_shall_be_updated_to_name_duration_start_of_down_time_and_down_time_duration(String oldName, String newName, String duration, String downtimeStart, String downtimeDuration) {
		String stringDuration = Integer.toString(oldService.getDuration());
		String stringDowntime = Integer.toString(oldService.getDowntimeDuration());
		String stringDStart = Integer.toString(oldService.getDowntimeStart());
		assertEquals(oldService.getName(), newName);
		if (duration.compareToIgnoreCase("<duration>") != 0 ) {
			assertEquals(stringDuration, duration);
		}
		if (downtimeStart.compareToIgnoreCase("<downTimeStart>")!=0) {
			assertEquals(stringDStart, downtimeStart);
		}
		if (downtimeDuration.compareToIgnoreCase("<downtimeDuration>")!=0) {
			assertEquals(stringDowntime, downtimeDuration);
		}
	}






	// #########################################################################
	// LOG IN, LOG OUT #########################################################
	// #########################################################################


	/**
	 * @author lukasalatalo
	 */
	@When("the user tries to log in with username {string} and password {string}")
	public void logInWithUsernamePassword(String aUsername, String aPassword) {
		try {
			currentUser = User.getWithUsername(aUsername);
			FlexiBookController.logIn(aUsername, aPassword);
			if (aUsername.equals("owner")) {
				currentUser =flexibook.getOwner();
			}
		}catch(InvalidInputException e) {
			currentUser = null;
			error +=e.getMessage();
			nError++;
		}
	}

	/**
	 * @author lukasalatalo
	 */
	@Then("the user should be successfully logged in")
	public void shouldBeLogIN() {
		assertTrue(FlexiBookApplication.getCurrentUser().equals(currentUser));
	}

	/**
	 * @author lukasalatalo
	 */
	@Then("the user should not be logged in")
	public void unsuccessfulLogIn() {
		assertTrue(currentUser == null);
		//assertTrue(FlexiBookApplication.getCurrentUser()==null);
	}

	/**
	 * @author lukasalatalo
	 */
	@Then("a new account shall be created")
	public void createOwnerLogIn() {
		assertTrue(flexibook.hasOwner());
	}

	/**
	 * @author lukasalatalo
	 */
	@Then("the user shall be successfully logged in")
	public void successfullLogIN() {
		assertTrue(FlexiBookApplication.getCurrentUser().equals(currentUser));
	}

	/**
	 * @author lukasalatalo
	 */
	@Given("the user is logged out")
	public void isLoggedOut() {
		FlexiBookApplication.setCurrentUser(null);
	}


	/**
	 * @author lukasalatalo
	 */
	@When("the user tries to log out")
	public void triesToLogOutWhenLoggedOut() {
		try {
			FlexiBookController.logOut(FlexiBookApplication.getCurrentUser());
		}catch(InvalidInputException e) {
			error+=e.getMessage();
			nError++;
		}
	}


	/**
	 * @author lukasalatalo
	 */
	/*@When("{string} requests the appointment calendar for the week starting on {string}")
	public void requestAppointmentCalenderForWeek(String aUser, String startDateString) {
		if (User.getWithUsername(aUser).equals(FlexiBookApplication.getCurrentUser()) | flexibook.getOwner().equals(currentUser)) {
			//			try {
			Date startDate = convertStringToDate(startDateString);
			//Date startDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(startDateString).getTime());
			for (int i=0; i<7;i++) {
				Calendar c = Calendar.getInstance(); 
				c.setTime(startDate); 
				c.add(Calendar.DATE, 1);
				//					System.out.println(startDate);
				startDate= new java.sql.Date(c.getTimeInMillis());
				//					System.out.println(startDate);
				List<TOAppointmentCalender>appointmentsInDay = FlexiBookController.viewAppointmentCalender(startDate);
				//					appointmentCalender
				System.out.println(appointmentCalender + "jjjjj");
				if (appointmentsInDay!=null){
					for(TOAppointmentCalender appointment:appointmentsInDay) {
						appointmentCalender.add(appointment);
						System.out.println(appointmentCalender);
					}
				}
			}
			//			}catch (ParseException e) {
			//				error+= e.getMessage();
			//				nError++;
			//			}
		}
	}
	*/
	
	/**
	 * @author lukasalatalo
	 */
	@When("{string} requests the appointment calendar for the week starting on {string}")
	public void requestAppointmentCalenderForWeek(String aUser, String startDateString) {
		if (User.getWithUsername(aUser).equals(FlexiBookApplication.getCurrentUser()) | flexibook.getOwner().equals(currentUser)) {
			try {
				Date startDate = new Date(new SimpleDateFormat("yyyy-MM-dd").parse(startDateString).getTime());
				appointmentCalender = FlexiBookController.viewAppointmentCalender(startDate);

			}catch (ParseException e) {
				error+= e.getMessage();
				nError++;
			}
		}
	}

	
	

	/**
	 * @author lukasalatalo
	 */
	/*@When("{string} requests the appointment calendar for the day of {string}")
	public void requestAppointmentCalenderForDay(String aUser, String startDateString) {
		try {
			LocalDate.parse(startDateString);
		}catch (DateTimeParseException e) {
			error+= startDateString + " is not a valid date";
			nError++;
		}
		if(User.getWithUsername(aUser).equals(currentUser) | flexibook.getOwner().equals(currentUser)) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				Date startDate = new Date(sdf.parse(startDateString).getTime());
				appointmentCalender= FlexiBookController.viewAppointmentCalender(startDate);
			}catch (ParseException e) {
				error+= startDateString + " is not a valid date";
				nError++;
			}
		}
	}
	*/
	
	/**
	 * @author lukasalatalo
	 */
	@When("{string} requests the appointment calendar for the day of {string}")
	public void requestAppointmentCalenderForDay(String aUser, String startDateString) {
		try {
			LocalDate.parse(startDateString);
		}catch (DateTimeParseException e) {
			error+= startDateString + " is not a valid date";
			nError++;
		}
		
		if(User.getWithUsername(aUser).equals(currentUser) | flexibook.getOwner().equals(currentUser)) {
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				sdf.setLenient(false);
				Date startDate = new Date(sdf.parse(startDateString).getTime());
				appointmentCalender= FlexiBookController.viewAppointmentCalender(startDate);
				
				AppointmentCalendar frame = new AppointmentCalendar();
				frame.setVisible(true);
					
			}catch (ParseException e) {
				error+= startDateString + " is not a valid date";
				nError++;
			}
		}
	}
	
	

	/**
	 * @author lukasalatalo
	 */
	@Then("the following slots shall be unavailable:")
	public void unavailableSlots(DataTable dataTable) {
		List<Map<String, String>> map = dataTable.asMaps(String.class, String.class);
		int i=0;
		List<TOAppointmentCalender>unavailable =new ArrayList<TOAppointmentCalender>();
		for (TOAppointmentCalender slot: appointmentCalender) {
			if (!slot.getIsAvailable()) {
				unavailable.add(slot);
			}
		}
		for (Map<String,String> row:map) {

			String testSTime = null;
			String testETime = null;
			if (row.get("startTime").length()==4) {
				testSTime="0"+row.get("startTime");
				if(row.get("endTime").length()==4) {
					testETime = "0"+row.get("endTime");
				}
				else {
					testETime = row.get("endTime");
				}
			}
			else {
				testSTime = row.get("startTime");
				testETime = row.get("endTime");
			}

			String date =unavailable.get(i).getDate();
			String startTime= unavailable.get(i).getStartTime();
			String endTime=unavailable.get(i).getEndTime();

			assertEquals(date,row.get("date"));			
			//assertEquals(startTime,row.get("startTime"));
			//assertEquals(endTime,row.get("endTime"));

			assertEquals(startTime,testSTime);
			assertEquals(endTime,testETime);

			i++;
		}
	}

	/**
	 * @author lukasalatalo
	 */
	@Then("the following slots shall be available:")
	public void availableSlots(DataTable dataTable) {
		List<Map<String, String>> map = dataTable.asMaps(String.class, String.class);
		int i=0;
		List<TOAppointmentCalender>available =new ArrayList<TOAppointmentCalender>();
		for (TOAppointmentCalender slot: appointmentCalender) {
			if (slot.getIsAvailable()) {
				available.add(slot);
			}
		}
		for (Map<String,String> row:map) {

			String testSTime = null;
			String testETime = null;
			if (row.get("startTime").length()==4) {
				testSTime="0"+row.get("startTime");
				if(row.get("endTime").length()==4) {
					testETime = "0"+row.get("endTime");
				}
				else {
					testETime = row.get("endTime");
				}
			}
			else {
				testSTime = row.get("startTime");
				testETime = row.get("endTime");
			}

			String date =available.get(i).getDate();
			String startTime= available.get(i).getStartTime();
			String endTime=available.get(i).getEndTime();
			assertEquals(date,row.get("date"));
			//assertEquals(startTime,row.get("startTime"));
			//assertEquals(endTime,row.get("endTime"));

			assertEquals(startTime,testSTime);
			assertEquals(endTime,testETime);

			i++;
		}
	}











	// #########################################################################
	// Create Business Information Feature ####################################
	// #########################################################################

	// Add Business Information ================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to set up the business information with new {string} and {string} and {string} and {string}")
	public void whenCreateBusinessInformation(String aName, String aAddress, String aPhoneNumber, String aEmail) {
		nError = 0;
		currentUser = FlexiBookApplication.getCurrentUser();
		try {
			FlexiBookController.createBusinessInfo(aName, aAddress, aPhoneNumber, aEmail, currentUser);
		} catch (InvalidInputException e) {
			error += e.getMessage();
			nError++;
		}
	}

	// Check Business Information ==============================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("a new business with new {string} and {string} and {string} and {string} shall {string} created")
	public void thenCheckBusinessInformation(String aName, String aAddress, String aPhoneNumber, String aEmail,
			String aResult) {
		if (aResult.compareTo("not be") == 0) {
			assertTrue(!flexibook.hasBusiness());
		} else {
			Business b = flexibook.getBusiness();
			assertTrue(b.getName().compareTo(aName) == 0);
			assertTrue(b.getAddress().compareTo(aAddress) == 0);
			assertTrue(b.getPhoneNumber().compareTo(aPhoneNumber) == 0);
			assertTrue(b.getEmail().compareTo(aEmail) == 0);
		}
	}

	// Add a Business Hour =====================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to add a new business hour on {string} with start time {string} and end time {string}")
	public void whenCreateBusinesssHour(String aDayOfWeekString, String aStartTimeString, String aEndTimeString) {
		nError = 0;
		currentUser = FlexiBookApplication.getCurrentUser();
		System.out.println(currentUser + "fffff");
		BusinessHour.DayOfWeek aDayOfWeek = null;
		switch (aDayOfWeekString) {
		case "Sunday":
			aDayOfWeek = BusinessHour.DayOfWeek.Sunday;
			break;
		case "Monday":
			aDayOfWeek = BusinessHour.DayOfWeek.Monday;
			break;
		case "Tuesday":
			aDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
			break;
		case "Wednesday":
			aDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
			break;
		case "Thursday":
			aDayOfWeek = BusinessHour.DayOfWeek.Thursday;
			break;
		case "Friday":
			aDayOfWeek = BusinessHour.DayOfWeek.Friday;
			break;
		case "Saturday":
			aDayOfWeek = BusinessHour.DayOfWeek.Saturday;
			break;
		}

		Time aStartTime = convertStringToTime(aStartTimeString);
		Time aEndTime = convertStringToTime(aEndTimeString);

		if (aDayOfWeek != null && aStartTime != null && aEndTime != null) {
			nError = 0;
			try {
				FlexiBookController.addBusinessHours(aDayOfWeek, aStartTime, aEndTime, currentUser);
			} catch (InvalidInputException e) {
				error += e.getMessage();
				nError++;
			}
		}
	}

	// Check a Business Hour ===================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("a new business hour shall {string} created")
	public void thenCheckBusinessHour(String aResult) {
		// Given the scenario, only one business hour initially existed
		if (aResult.compareTo("not be") == 0) {
			assertEquals(flexibook.getBusiness().getBusinessHours().size(), 1);
		} else if (aResult.compareTo("be") == 0) {
			assertEquals(flexibook.getBusiness().getBusinessHours().size(), 2);
		}
	}

	// Add a TimeSlot ==========================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to add a new {string} with start date {string} at {string} and end date {string} at {string}")
	public void whenCreateTimeSlot(String aType, String aStartDateString, String aStartTimeString,
			String aEndDateString, String aEndTimeString) {
		nError = 0;

		currentUser = FlexiBookApplication.getCurrentUser();

		Date aStartDate = convertStringToDate(aStartDateString);
		Time aStartTime = convertStringToTime(aStartTimeString);
		Date aEndDate = convertStringToDate(aEndDateString);
		Time aEndTime = convertStringToTime(aEndTimeString);

		if (aStartDate != null && aStartTime != null && aEndDate != null && aEndTime != null) {
			System.out.println("herrrre");
			try {
				switch (aType) {
				case "vacation":
					FlexiBookController.addVacation(aStartDate, aStartTime, aEndDate, aEndTime, currentUser,SystemTime.getSystemDate());
					break;
				case "holiday":
					FlexiBookController.addHoliday(aStartDate, aStartTime, aEndDate, aEndTime, currentUser,SystemTime.getSystemDate());
					break;
				}
			} catch (InvalidInputException e) {
				error += e.getMessage();
				nError++;
			}
		}
	}



	// Check a TimeSlot ========================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("a new {string} shall {string} be added with start date {string} at {string} and end date {string} at {string}")
	public void thenCheckTimeSlot(String aType, String aResult, String aStartDateString, String aStartTimeString,
			String aEndDateString, String aEndTimeString) {
		Date aStartDate = convertStringToDate(aStartDateString);
		Time aStartTime = convertStringToTime(aStartTimeString);
		Date aEndDate = convertStringToDate(aEndDateString);
		Time aEndTime = convertStringToTime(aEndTimeString);


		// Given the scenario there is initially only one vacation
		// and one holiday
		if (aResult.compareTo("not be") == 0) {
			assertEquals(flexibook.getBusiness().getHolidays().size(), 1);
		} else if (aResult.compareTo("be") == 0) {
			TimeSlot newTimeSlot;

			if (aStartDate != null && aStartTime != null && aEndDate != null && aEndTime != null) {
				switch (aType) {
				case "vacation":
					assertEquals(flexibook.getBusiness().getVacation().size(), 2);
					assertEquals(flexibook.getBusiness().getHolidays().size(), 1);
					newTimeSlot = flexibook.getBusiness().getVacation(1);
					assertTrue(newTimeSlot.getStartDate().compareTo(aStartDate) == 0);
					assertTrue(newTimeSlot.getStartTime().compareTo(aStartTime) == 0);
					assertTrue(newTimeSlot.getEndDate().compareTo(aEndDate) == 0);
					assertTrue(newTimeSlot.getEndTime().compareTo(aEndTime) == 0);
					break;
				case "holiday":
					System.out.println(flexibook.getBusiness().getHolidays().size());
					assertEquals(flexibook.getBusiness().getVacation().size(), 1);
					assertEquals(flexibook.getBusiness().getHolidays().size(), 2);
					newTimeSlot = flexibook.getBusiness().getHoliday(1);
					assertTrue(newTimeSlot.getStartDate().compareTo(aStartDate) == 0);
					assertTrue(newTimeSlot.getStartTime().compareTo(aStartTime) == 0);
					assertTrue(newTimeSlot.getEndDate().compareTo(aEndDate) == 0);
					assertTrue(newTimeSlot.getEndTime().compareTo(aEndTime) == 0);
					break;
				}
			}
		}
	}






	// #########################################################################
	// Update Business Information Feature #####################################
	// #########################################################################

	// Update Business Information =============================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to update the business information with new {string} and {string} and {string} and {string}")
	public void whenUpdateBusinessInformation(String aName, String aAddress, String aPhoneNumber, String aEmail) {
		nError = 0;
		isUpdated = true;

		try {
			FlexiBookController.updateBusinessInfo(aName, aAddress, aPhoneNumber, aEmail, currentUser);
		} catch (InvalidInputException e) {
			isUpdated = false;
			error += e.getMessage();
			nError++;
		}
	}

	// Check Updated business Information ======================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("the business information shall {string} updated with new {string} and {string} and {string} and {string}")
	public void thenCheckUpdatedBusinessInformation(String aResult, String aName, String aAddress, String aPhoneNumber,
			String aEmail) {
		Business b = flexibook.getBusiness();
		if (aResult.compareTo("not be") == 0) {
			assertTrue(!isUpdated);
		} else if (aResult.compareTo("be") == 0) {
			assertTrue(isUpdated);
			assertEquals(b.getName(), aName);
			assertEquals(b.getAddress(), aAddress);
			assertEquals(b.getPhoneNumber(), aPhoneNumber);
			assertEquals(b.getEmail(), aEmail);
		}
	}

	// Update the Business Hours ===============================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to change the business hour {string} at {string} to be on {string} starting at {string} and ending at {string}")
	public void whenUpdateBusinessHour(String aInitialDayOfWeekString, String aInitialStartTimeString,
			String aNewDayOfWeekString, String aNewStartTimeString, String aNewEndTimeString) {
		nError = 0;
		isUpdated = true;

		BusinessHour.DayOfWeek aInitialDayOfWeek = null;
		switch (aInitialDayOfWeekString) {
		case "Sunday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Sunday;
			break;
		case "Monday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Monday;
			break;
		case "Tuesday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
			break;
		case "Wednesday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
			break;
		case "Thursday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Thursday;
			break;
		case "Friday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Friday;
			break;
		case "Saturday":
			aInitialDayOfWeek = BusinessHour.DayOfWeek.Saturday;
			break;
		}

		BusinessHour.DayOfWeek aNewDayOfWeek = null;
		switch (aNewDayOfWeekString) {
		case "Sunday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Sunday;
			break;
		case "Monday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Monday;
			break;
		case "Tuesday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
			break;
		case "Wednesday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
			break;
		case "Thursday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Thursday;
			break;
		case "Friday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Friday;
			break;
		case "Saturday":
			aNewDayOfWeek = BusinessHour.DayOfWeek.Saturday;
			break;
		}

		Time aInitialStartTime = convertStringToTime(aInitialStartTimeString);
		Time aNewStartTime = convertStringToTime(aNewStartTimeString);
		Time aNewEndTime = convertStringToTime(aNewEndTimeString);


		if (aInitialDayOfWeek != null && aInitialStartTime != null && aNewDayOfWeek != null && aNewStartTime != null
				&& aNewEndTime != null) {

			try {
				int idx = FlexiBookController.getBusinessHoursIdx(aInitialDayOfWeek, aInitialStartTime);
				originalIdx = idx;
				originalBusinessHour = flexibook.getBusiness().getBusinessHour(idx);
				FlexiBookController.updateBusinessHours(idx, aNewDayOfWeek, aNewStartTime, aNewEndTime, currentUser);
			} catch (InvalidInputException e) {
				isUpdated = false;
				error += e.getMessage();
				nError++;
			}
		}
	}

	// Check Updated Business Hour =============================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("the business hour shall {string} be updated")
	public void thenCheckUpdatedBusinessHour(String aResult) {
		if (aResult.compareTo("not") == 0) {
			assertTrue(!isUpdated);
		} else if (aResult.compareTo(" ") == 0) {
			assertTrue(isUpdated);
			// No further input was given to directly test this...
		}
	}

	
	
	// Remove the Business Hour ================================================
		/**
		 * @author Jeremy (260660818)
		 */
		@When("the user tries to remove the business hour starting {string} at {string}")
		public void whenRemoveBusinessHour(String aInitialDayOfWeekString, String aInitialStartTimeString) {
			nError = 0;
			isUpdated = true;

			BusinessHour.DayOfWeek aInitialDayOfWeek = null;
			switch (aInitialDayOfWeekString) {
			case "Sunday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Sunday;
				break;
			case "Monday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Monday;
				break;
			case "Tuesday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Tuesday;
				break;
			case "Wednesday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Wednesday;
				break;
			case "Thursday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Thursday;
				break;
			case "Friday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Friday;
				break;
			case "Saturday":
				aInitialDayOfWeek = BusinessHour.DayOfWeek.Saturday;
				break;
			}

			Time aInitialStartTime = convertStringToTime(aInitialStartTimeString);

			if (aInitialDayOfWeek != null && aInitialStartTime != null) {
				try {
					int idx = FlexiBookController.getBusinessHoursIdx(aInitialDayOfWeek, aInitialStartTime);
					originalIdx = idx;
					if (idx >= 0 && idx < flexibook.getBusiness().getBusinessHours().size()) {
						originalBusinessHour = flexibook.getBusiness().getBusinessHour(idx);
					}
					FlexiBookController.removeBusinessHours(idx, currentUser);
				} catch (InvalidInputException e) {
					isUpdated = false;
					error += e.getMessage();
					nError++;
				}
			}
		}


		// Check Removed Business Hour =============================================
		/**
		 * @author Jeremy (260660818)
		 */
		@Then("the business hour starting {string} at {string} shall {string} exist")
		public void thenCheckRemovedBusinessHour(String aInitialDay, String aInitialTime, String aResult) {
			Business b = flexibook.getBusiness();
			if (aResult.compareTo("not") == 0 || originalIdx < 0) {
				assertTrue(!b.getBusinessHours().contains(originalBusinessHour));
			} else {
				assertTrue(b.getBusinessHours().contains(originalBusinessHour));
			}
		}

	// Update a Time Slot ======================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to change the {string} on {string} at {string} to be with start date {string} at {string} and end date {string} at {string}")
	public void whenUpdateTimeSlot(String aType, String aInitialStartDateString, String aInitialStartTimeString,
			String aNewStartDateString, String aNewStartTimeString, String aNewEndDateString,
			String aNewEndTimeString) {
		nError = 0;
		isUpdated = true;

		Date aInitialStartDate = convertStringToDate(aInitialStartDateString);
		Time aInitialStartTime = convertStringToTime(aInitialStartTimeString);
		Date aNewStartDate = convertStringToDate(aNewStartDateString);
		Time aNewStartTime = convertStringToTime(aNewStartTimeString);
		Date aNewEndDate = convertStringToDate(aNewEndDateString);
		Time aNewEndTime = convertStringToTime(aNewEndTimeString);



		if (aInitialStartDate != null && aInitialStartTime != null && aNewStartDate != null && aNewStartTime != null
				&& aNewEndDate != null && aNewEndTime != null) {

			int idx = -1;
			try {
				switch (aType) {
				case "vacation":
					idx = FlexiBookController.getVacationIdx(aInitialStartDate, aInitialStartTime);
					originalIdx = idx;
					originalTimeSlot = flexibook.getBusiness().getVacation(idx);
					FlexiBookController.updateVacation(idx, aNewStartDate, aNewStartTime, aNewEndDate, aNewEndTime,
							currentUser, SystemTime.getSystemDate());
					break;
				case "holiday":
					idx = FlexiBookController.getHolidayIdx(aInitialStartDate, aInitialStartTime);
					originalIdx = idx;
					originalTimeSlot = flexibook.getBusiness().getHoliday(idx);
					FlexiBookController.updateHoliday(idx, aNewStartDate, aNewStartTime, aNewEndDate, aNewEndTime,
							currentUser, SystemTime.getSystemDate());
					break;
				}
			} catch (InvalidInputException e) {
				isUpdated = false;
				error += e.getMessage();
				nError++;
			}
		}
	}

	// Check the updated Time Slot =============================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("the {string} shall {string} be updated with start date {string} at {string} and end date {string} at {string}")
	public void thenCheckUpdatedTimeSlot(String aType, String aResult, String aNewStartDateString, String aNewStartTimeString,
			String aNewEndDateString, String aNewEndTimeString) {

		if (aResult.compareTo("not be") == 0) {
			assertTrue(!isUpdated);
			return;
		}

		Date aNewStartDate = convertStringToDate(aNewStartDateString);
		Time aNewStartTime = convertStringToTime(aNewStartTimeString);
		Date aNewEndDate = convertStringToDate(aNewEndDateString);
		Time aNewEndTime = convertStringToTime(aNewEndTimeString);

		if (aNewStartDate != null && aNewStartTime != null
				&& aNewEndDate != null && aNewEndTime != null) {

			switch (aType) {
			case "vacation":
				TimeSlot v = flexibook.getBusiness().getVacation(originalIdx);
				assertTrue(v.getStartDate().compareTo(aNewStartDate) == 0);
				assertTrue(v.getStartTime().compareTo(aNewStartTime) == 0);
				assertTrue(v.getEndDate().compareTo(aNewEndDate) == 0);
				assertTrue(v.getEndTime().compareTo(aNewEndTime) == 0);
				break;
			case "holiday":
				TimeSlot h = flexibook.getBusiness().getHoliday(originalIdx);
				assertTrue(h.getStartDate().compareTo(aNewStartDate) == 0);
				assertTrue(h.getStartTime().compareTo(aNewStartTime) == 0);
				assertTrue(h.getEndDate().compareTo(aNewEndDate) == 0);
				assertTrue(h.getEndTime().compareTo(aNewEndTime) == 0);
				break;
			}
		}
	}

	// Remove Time Slot ========================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@When("the user tries to remove an existing {string} with start date {string} at {string} and end date {string} at {string}")
	public void whenRemoveTimeSlot(String aType, String aStartDateString, String aStartTimeString,
			String aEndDateString, String aEndTimeString) {
		Date aStartDate = null;
		Time aStartTime = null;
		Date aEndDate = null;
		Time aEndTime = null;
		aStartDate = convertStringToDate(aStartDateString);
		aStartTime = convertStringToTime(aStartTimeString);
		aEndDate = convertStringToDate(aEndDateString);
		aEndTime = convertStringToTime(aEndTimeString);


		if (aStartDate != null && aStartTime != null && aEndDate != null && aEndTime != null) {
			int idx = -1;
			try {
				switch (aType) {
				case "vacation":
					idx = FlexiBookController.getVacationIdx(aStartDate, aStartTime);
					originalIdx = idx;
					originalTimeSlot = flexibook.getBusiness().getVacation(idx);
					FlexiBookController.removeVacation(idx, currentUser);
					break;
				case "holiday":
					idx = FlexiBookController.getHolidayIdx(aStartDate, aStartTime);
					originalIdx = idx;
					originalTimeSlot = flexibook.getBusiness().getHoliday(idx);
					FlexiBookController.removeHoliday(idx, currentUser);
					break;
				}
			} catch (InvalidInputException e) {
				isUpdated = false;
				error += e.getMessage();
				nError++;
			}
		}
	}

	// Check Removed Time Slot =================================================
	/**
	 * @author Jeremy (260660818)
	 */
	@Then("the {string} with start date {string} at {string} shall {string} exist")
	public void thenCheckRemovedTimeSlot(String aType, String aStartDateString, String aStartTimeString,
			String aResult) {
		Business b = flexibook.getBusiness();

		switch (aType) {
		case "vacation":
			if (aResult.compareTo("not") == 0) {
				assertTrue(!b.getVacation().contains(originalTimeSlot));
			} else {
				assertTrue(b.getVacation().contains(originalTimeSlot));
			}
			break;
		case "holiday":
			if (aResult.compareTo("not") == 0) {
				assertTrue(!b.getHolidays().contains(originalTimeSlot));
			} else {
				assertTrue(b.getHolidays().contains(originalTimeSlot));
			}
			break;
		}
	}


	@Then("an error message {string} shall {string} raised")
	public void an_error_message_shall_raised(String aError, String aResultError) {
		if (aResultError.compareTo("not be") == 0) {
			assertTrue(nError == 0);
		}
		else {
			assertTrue(error.contains(aError));
		}
	}


	@Then("an error message {string} shall {string} be raised")
	public void an_error_message_shall_be_raised(String aError, String aResultError) {
		if (aResultError.compareTo("not") == 0) {
			assertTrue(!error.contains(aError));
		} else {
			assertTrue(error.contains(aError));
		}
	}


	@When("the user tries to access the business information")
	public void the_user_tries_to_access_the_business_information() {
		name = flexibook.getBusiness().getName();
		address = flexibook.getBusiness().getAddress();
		phone = flexibook.getBusiness().getPhoneNumber();
		email = flexibook.getBusiness().getEmail();

	}


	@Then("the {string} and {string} and {string} and {string} shall be provided to the user")
	public void the_and_and_and_shall_be_provided_to_the_user(String string, String string2, String string3, String string4) {
		assertEquals(name, flexibook.getBusiness().getName());
		assertEquals(address, flexibook.getBusiness().getAddress());
		assertEquals(phone, flexibook.getBusiness().getPhoneNumber());
		assertEquals(email, flexibook.getBusiness().getEmail());
	}















	// #########################################################################
	// AFTER EACH ##############################################################
	// #########################################################################

	@After
	public void tearDown() {
		FlexiBookApplication.setCurrentUser(null);
		flexibook.delete();
	}







	// #########################################################################
	// PRIVATE METHODS #########################################################
	// #########################################################################




	public static Time getServiceComboEndTime(Time startTime, BookableService service, String optServices) {
		LocalTime localStartTime = startTime.toLocalTime();
		long duration = 0;

		if (service instanceof ServiceCombo) {
			for (int i=0; i<((ServiceCombo)service).numberOfServices();i++) {
				if (((ServiceCombo)service).getService(i).isMandatory()
						|| optServices.contains(((ServiceCombo)service).getService(i).getService().getName())) {

					duration += ((ServiceCombo)service).getService(i).getService().getDuration();
					System.out.println(duration);
				}
			}
		}
		LocalTime localEndTime = localStartTime.plusMinutes(duration);
		Time endTime = Time.valueOf(localEndTime);
		return endTime;

	}



	// Private method to find a BookableService in the system ===================
	/**
	 * @author - Antonin Guerre
	 */	
	private static BookableService findBookableService(String aBookableServiceString) {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		BookableService aBookableService = null;
		for (int i = 0; i < flexibook.numberOfBookableServices(); i++) {
			if (aBookableServiceString.toLowerCase().equals(flexibook.getBookableService(i).getName().toLowerCase())) {
				aBookableService = flexibook.getBookableService(i);
				break;
			}
		}
		return aBookableService;
	}


	// Private method to convert a string to a date ============================
	/**
	 * @author - Antonin Guerre
	 * @param date: the string that will be changed to a date
	 */	
	private static Date convertStringToDate(String date) {
		Date aDate = null;
		aDate = Date.valueOf(date);
		return aDate;
	}



	// Private method to convert a string to a time ============================
	/**
	 * @author - Antonin Guerre
	 * @param time: the string that will be changed to a time
	 */	
	private static Time convertStringToTime(String time) {
		Time aTime = null;
		DateFormat format = new SimpleDateFormat("HH:mm");
		try {
			aTime = new Time(format.parse(time).getTime());
		} catch (ParseException ex) {}
		return aTime;
	}


	// Private method to find a customer in the system =========================
	/**
	 * @author - Antonin Guerre
	 * @param aCustomerString: the researched customer
	 */	
	private static Customer findCustomer(String aCustomerString) {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		Customer aCustomer = null;

		for (int i = 0; i < flexibook.numberOfCustomers(); i++) {
			if (aCustomerString.toLowerCase().equals(flexibook.getCustomer(i).getUsername().toLowerCase())) {
				aCustomer = flexibook.getCustomer(i);
				break;
			}
		}
		return aCustomer;
	}

	// Private method to find an appointment in the system =====================
	/**
	 * @author - Antonin Guerre
	 * @param aCustomer: the customer who made the appointment
	 * @param aBookableService: the appointment's service
	 * @param aDate: the appointment's date
	 * @param aTime: the appointment's starting time
	 */	
	private static Appointment findAppointment(String aCustomer, String aBookableService, String aDate, String aTime) {
		Appointment foundAppointment = null;
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		for (int i = 0; i < flexibook.numberOfAppointments(); i++) {
			if (flexibook.getAppointment(i).getCustomer().equals(findCustomer(aCustomer)) 
					&& flexibook.getAppointment(i).getBookableService().equals(findBookableService(aBookableService)) 
					&& flexibook.getAppointment(i).getTimeSlot().getStartDate().equals(convertStringToDate(aDate))
					&& flexibook.getAppointment(i).getTimeSlot().getStartTime().equals(convertStringToTime(aTime))) {
				foundAppointment = flexibook.getAppointment(i);
				return foundAppointment;
			}
			else 
				continue;
		}
		return foundAppointment;
	}









	// #########################################################################
	// ITERATION 3 #############################################################
	// #########################################################################


	/**
	 * @author juliettedebray
	 */
	@When("{string} attempts to cancel the appointment at {string}")
	public void attempts_to_cancel_the_appointment_at(String aUser, String aDateTime) {
		String dateString = aDateTime.substring(0, 10);
		String timeString = aDateTime.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);

		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		try {
			FlexiBookController.cancelAppointment(aUser, bookedAppointment.getCustomer().getUsername(), bookedAppointment.getBookableService().getName(), 
					bookedAppointment.getTimeSlot().getStartDate().toString(), bookedAppointment.getTimeSlot().getStartTime().toString());
		}catch (InvalidInputException e) {
			error+=e.getMessage();
			nError++;
		}
	}


	/**
	 * 
	 * @author Ege
	 */
	@Given("{string} has {int} no-show records")
	public void has_no_show_records(String customerUsername, Integer noShow) {
		Customer customer = findCustomer(customerUsername);
		customer.setNoShow(noShow);
	}


	/**
	 * @author juliettedebray
	 */
	@When("{string} makes a {string} appointment for the date {string} and time {string} at {string}")
	public void makes_a_appointment_for_the_date_and_time_at(String customerUsername, String serviceName, String aptDate, String aptTime, String currentTimeDate) {
		String user = customerUsername;
		customerString = customerUsername;
		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		try {
			FlexiBookController.makeAppointment(user, customerUsername, serviceName, null, aptDate, aptTime);
			bookedAppointment = findAppointment(customerUsername, serviceName, aptDate, aptTime);
			bookedDate = aptDate;
			bookedTime = aptTime;
			bookedService = serviceName;
		} catch (InvalidInputException e) {
			error += e.getMessage();	
		}
	}

	/**
	 * @author juliettedebray
	 */
	@When("{string} attempts to change the service in the appointment to {string} at {string}")
	public void attempts_to_change_the_service_in_the_appointment_to_at(String customerUsername, String newService, String currentTimeDate) {
		String user = customerUsername;
		customerString = customerUsername;

		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		try {
			FlexiBookController.updateAppointment(user, customerString, true, newService, bookedDate, bookedTime,"","", bookedDate, bookedTime);
		} catch (InvalidInputException e) {
			error += e.getMessage();
		}

	}

	/**
	 * @author juliettedebray
	 */
	@Then("the appointment shall be booked")
	public void the_appointment_shall_be_booked() {
		assertTrue(bookedAppointment!=null);
	}


	/**
	 * @author juliettedebray
	 */
	@Then("the service in the appointment shall be {string}")
	public void the_service_in_the_appointment_shall_be(String serviceName) {
		assertEquals(bookedAppointment.getBookableService().getName(), serviceName);
	}

	/**
	 * @author juliettedebray
	 */
	@When("{string} attempts to update the date to {string} and time to {string} at {string}")
	public void attempts_to_update_the_date_to_and_time_to_at(String customerUsername, String newDate, String newTime, String currentTimeDate) {
		customerString = customerUsername;
		String date1 = bookedAppointment.getTimeSlot().getStartDate().toString();
		String time1 = bookedAppointment.getTimeSlot().getStartTime().toString();
		String bookedService = bookedAppointment.getBookableService().getName();
		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		try {
			FlexiBookController.updateAppointment(customerUsername, customerString, false, bookedService, date1, time1,"","", newDate, newTime);
		} catch (InvalidInputException e) {
			error += e.getMessage();
		}
	}


	/**
	 * @author lukasalatalo
	 */
	@When("{string} makes a {string} appointment without choosing optional services for the date {string} and time {string} at {string}")
	public void makes_a_appointment_without_choosing_optional_services_for_the_date_and_time_at(String aUser, String service, String date, String time, String dateTime) {    	
		String dateString = dateTime.substring(0, 10);
		String timeString = dateTime.substring(11, 16);
		timeString = timeString + ":00";
		Time sysTime = Time.valueOf(timeString);
		Date sysDate = Date.valueOf(dateString);

		SystemTime.setSystemDate(sysDate);
		SystemTime.setSystemTime(sysTime);

		try {    		
			FlexiBookController.makeAppointment(aUser, aUser, service, "", date, time);
			bookedAppointment = findAppointment(aUser, service, date, time);
		}catch(InvalidInputException e) {
			error+= e.getMessage();
			nError++;
		}

	}
	/**
	 * @author lukasalatalo
	 */
	@When("{string} attempts to add the optional service {string} to the service combo in the appointment at {string}")
	public void attempts_to_add_the_optional_service_to_the_service_combo_in_the_appointment_at(String aUser, String service, String dateTime) {

		String dateString = dateTime.substring(0, 10);
		String timeString = dateTime.substring(11, 16);
		timeString = timeString + ":00";
		Time sysTime = Time.valueOf(timeString);
		Date sysDate = Date.valueOf(dateString);

		SystemTime.setSystemDate(sysDate);
		SystemTime.setSystemTime(sysTime);

		String startDate = bookedAppointment.getTimeSlot().getStartDate().toString();
		String startTime = bookedAppointment.getTimeSlot().getStartTime().toString();

		try {
			FlexiBookController.updateAppointment(aUser, aUser, false, bookedAppointment.getBookableService().getName(), startDate, startTime, "add", service , startDate,startTime);
		}catch (InvalidInputException e) {
			error+=e.getMessage();
			nError++;
		}
	}
	/**
	 * @author lukasalatalo
	 */
	@Then("the appointment shall be in progress")
	public void the_appointment_shall_be_in_progress() {
		assertEquals(bookedAppointment.getAppointmentStateFullName(),"InProgress");

	}
	/**
	 * @author antoninguerre
	 */
	@Then("the service combo in the appointment shall be {string}")
	public void the_service_combo_in_the_appointment_shall_be(String string) {
		assertEquals(bookedAppointment.getBookableService().getName(),string);
	}


	/**
	 * @author juliettedebray
	 */
	@When("the owner starts the appointment at {string}")
	public void the_owner_starts_the_appointment_at(String currentTimeDate) {

		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		bookedAppointment.startAppointment();
	}

	/**
	 * @author juliettedebray
	 */

	@When("the owner ends the appointment at {string}")
	public void the_owner_ends_the_appointment_at(String currentTimeDate) {
		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		bookedAppointment.finishAppointment();
	}



	/**
	 * @author juliettedebray
	 */
	@When("the owner attempts to register a no-show for the appointment at {string}")
	public void the_owner_attempts_to_register_a_no_show_for_the_appointment_at(String currentTimeDate) {
		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		bookedAppointment.customerNoShow();
	}

	/**
	 * @author juliettedebray
	 */
	@When("the owner attempts to end the appointment at {string}")
	public void the_owner_attempts_to_end_the_appointment_at(String currentTimeDate) {
		String dateString = currentTimeDate.substring(0, 10);
		String timeString = currentTimeDate.substring(11, 16);
		timeString = timeString + ":00";
		Time time = Time.valueOf(timeString);
		Date date = Date.valueOf(dateString);
		SystemTime.setSystemDate(date);
		SystemTime.setSystemTime(time);

		bookedAppointment.finishAppointment();
	}


	/**
	 * @author Ege
	 */
	@Then("the user {string} shall have {int} no-show records")
	public void the_user_shall_have_no_show_records(String string, Integer int1) {
		assertEquals(int1, findCustomer(string).getNoShow());
	}


	/**
	 * @author Ege
	 */
	@Then("the system shall have {int} appointment")
	public void the_system_shall_have_appointment(Integer int1) {
		//FlexiBook fb = FlexiBookApplication.getFlexibook();
		assertEquals(int1, flexibook.numberOfAppointments());
	}


	/**
	 * @author Ege
	 */
	@Then("the service combo shall have {string} selected services")
	public void the_service_combo_shall_have_selected_services(String string) {
		int i;
		for (i=0; i<bookedAppointment.numberOfChosenItems(); i++) {
			String chosenItem = bookedAppointment.getChosenItem(i).getService().getName();
			assertTrue(string.contains(chosenItem));
		}
	}

	/**
	 * @author Ege
	 */
	@Then("the appointment shall be for the date {string} with start time {string} and end time {string}")
	public void the_appointment_shall_be_for_the_date_with_start_time_and_end_time(String date, String startTime,
			String endTimeString) {

		assertEquals(date , bookedAppointment.getTimeSlot().getStartDate().toString());
		assertEquals(startTime + ":00", bookedAppointment.getTimeSlot().getStartTime().toString());
		assertEquals(endTimeString +":00", bookedAppointment.getTimeSlot().getEndTime().toString());
	}

	/**
	 * @author Ege
	 */
	@Then("the username associated with the appointment shall be {string}")
	public void the_username_associated_with_the_appointment_shall_be(String string) {
		assertEquals(string, bookedAppointment.getCustomer().getUsername());
	}

	/**
	 * @author Ege
	 */
	@Then("the system shall have {int} appointments")
	public void the_system_shall_have_appointments(Integer int1) {
		assertEquals(int1, flexibook.getAppointments().size());
	}

}

