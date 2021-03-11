package ca.mcgill.ecse.flexibook.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.sql.Time;
import java.sql.Date;
import java.util.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.model.Appointment;
import ca.mcgill.ecse.flexibook.model.Appointment.AppointmentState;
import ca.mcgill.ecse.flexibook.model.BookableService;
import ca.mcgill.ecse.flexibook.model.Business;
import ca.mcgill.ecse.flexibook.model.BusinessHour;
import ca.mcgill.ecse.flexibook.model.BusinessHour.DayOfWeek;
import ca.mcgill.ecse.flexibook.model.ComboItem;
import ca.mcgill.ecse.flexibook.model.Customer;
import ca.mcgill.ecse.flexibook.model.FlexiBook;
import ca.mcgill.ecse.flexibook.model.Owner;
import ca.mcgill.ecse.flexibook.model.Service;
import ca.mcgill.ecse.flexibook.model.ServiceCombo;
import ca.mcgill.ecse.flexibook.model.SystemTime;
import ca.mcgill.ecse.flexibook.model.TimeSlot;
import ca.mcgill.ecse.flexibook.model.User;
import ca.mcgill.ecse.flexibook.persistence.FlexiBookPersistence;


public class FlexiBookController {


	/**
	 * Constructor for the controller
	 */
	public FlexiBookController() {
	}



	// #########################################################################
	// MAKE, UPDATE, CANCEL APPOINTMENT ########################################
	// #########################################################################

	// Make Appointment =======================================================
	/**
	 * @author antoninguerre Make an appointment by inputting the name of the
	 *         customer, the name of the service, the starting date and the starting
	 *         time for the desired appointment.
	 * 
	 */
	public static void makeAppointment(String user, String customer, String serviceName, String optionalServicesString,
			String date, String time) throws InvalidInputException {

		// Access Flexibook system manager -------------------------------------
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		// Possible error messages ----------------------------------------------
		String noBusiness = "There is no business.";
		String noOwner = "There is no owner.";
		String noServices = "There are no services.";
		String customerNotLoggedIn = "Customer is not logged in the system.";
		String invalidServiceName = "Input a valid service name.";

		// Keeps track of errors
		String errorMsg = "";
		boolean isError = false;

		// Check that a business exists -----------------------------------------
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner -----------------------------------------
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that there are services ----------------------------------------
		if (!flexibook.hasBookableServices()) {
			throw new InvalidInputException(noServices);
		}

		// Convert Strings to Date and Time
		Date startDate = convertStringToDate(date);
		Time startTime = convertStringToTime(time);

		// Check that the appointment is not in the past
		if (startDate.before(SystemTime.getSystemDate())) {
			throw new InvalidInputException(
					"There are no available slots for " + serviceName + " on " + date + " at " + time);
		}

		// Check that the owner is not trying to make an appointment ------------
		if (user.equals("owner"))
			throw new InvalidInputException("An owner cannot make an appointment");

		// Check if the customer is in the system -------------------------------
		Customer aCustomer = findCustomer(customer);
		if (aCustomer == null) {
			errorMsg = errorMsg + customerNotLoggedIn;
			isError = true;
		}

		// Check if the service's name exists in the system ---------------------
		BookableService aService = findBookableService(serviceName);
		if (aService == null) {
			errorMsg = errorMsg + invalidServiceName;
			isError = true;
		}

		BookableService bookableService = null;

		if (aService instanceof ServiceCombo) {
			// Create the cutomer's combo and the usual combo with this name -------
			ServiceCombo customerServiceCombo = (ServiceCombo) aService;
			bookableService = customerServiceCombo;
		}

		if (aService instanceof Service) {
			bookableService = aService;
		}

		// Create an empty time slot
		TimeSlot aTimeSlot = null;

		// Check that the time slot is available for a service----------------------
		if ((aService instanceof Service
				&& !isTimeSlotAvailable(startDate, startTime, getServiceEndTime(startTime, bookableService)))
				|| (aService instanceof ServiceCombo && !isTimeSlotAvailable(startDate, startTime,
						getServiceComboEndTime(startTime, bookableService, optionalServicesString)))) {
			throw new InvalidInputException(
					"There are no available slots for " + serviceName + " on " + date + " at " + time);
		}

		// Create a time slot with the information given ------------------------
		if (aService instanceof Service) {
			aTimeSlot = new TimeSlot(startDate, startTime, startDate, getServiceEndTime(startTime, bookableService),
					flexibook);
		}
		if (aService instanceof ServiceCombo) {
			aTimeSlot = new TimeSlot(startDate, startTime, startDate,
					getServiceComboEndTime(startTime, bookableService, optionalServicesString), flexibook);
		}

		// Check that there are no errors in the inputs -------------------------
		if (isError == true) {
			throw new InvalidInputException(errorMsg.trim());
		} else {
			try {
				flexibook.addAppointment(aCustomer, bookableService, aTimeSlot);
			} catch (RuntimeException e) {
				throw new InvalidInputException(e.getMessage());
			}
		}

		Appointment appointment = findAppointment(aCustomer, bookableService, aTimeSlot.getStartDate(),
				aTimeSlot.getStartTime());
		if (bookableService instanceof ServiceCombo) {
			for (int i = 0; i < (((ServiceCombo) bookableService).numberOfServices()); i++) {
				if (((ServiceCombo) bookableService).getService(i).isMandatory() || optionalServicesString
						.contains(((ServiceCombo) bookableService).getService(i).getService().getName())) {
					appointment.addChosenItem(((ServiceCombo) bookableService).getService(i));
				}
			}
			Time newEndTime = appointmentEndTime(appointment);

			/*
			 * if (isTimeSlotAvailable(startDate,startTime,newEndTime)) { TimeSlot
			 * newTimeSlot = new TimeSlot(startDate, startTime, startDate, newEndTime,
			 * flexibook); appointment.setTimeSlot(newTimeSlot); } else {
			 * //flexibook.removeAppointment(appointment); throw new
			 * InvalidInputException("There are no available slots for " + serviceName +
			 * " on " + date + " at " + time); }
			 */
		}

		try {
			FlexiBookPersistence.save(flexibook);

		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}







	// Update an appointment ===================================================
	/**
	 * @author antoninguerre Update an appointment by adding or removing certain
	 *         ComboItems from or to the Combo or changing its time
	 * 
	 */
	public static void updateAppointment(String user, String customer, Boolean changeService, String serviceName,
			String date, String time, String action, String itemActedUponString, String newStartDateString,
			String newStartTimeString) throws InvalidInputException {

		// Access Flexibook system manager -------------------------------------
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		// Possible error messages ----------------------------------------------
		String noBusiness = "There is no business";
		String noOwner = "There is no owner";
		String noServices = "There are no services";
		String customerNotLoggedIn = "Customer is not logged in the system.";
		String invalidServiceName = "Input a valid service name.";
		String noAppointmentFound = "No appointment has been found in the system.";

		// Keeps track of errors
		String errorMsg = "";
		boolean isError = false;

		// Check that a business exists -----------------------------------------
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner -----------------------------------------
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that there are services ----------------------------------------
		if (!flexibook.hasBookableServices()) {
			throw new InvalidInputException(noServices);
		}

		if (user.equals("owner"))
			throw new InvalidInputException("Error: An owner cannot update a customer's appointment");

		if (!user.equals(customer))
			throw new InvalidInputException("Error: A customer can only update their own appointments");

		// Convert Strings to Date and Time
		Date aDate = convertStringToDate(date);
		Time aTime = convertStringToTime(time);
		Date newDate = null;
		Time newTime = null;

		if (newStartDateString != null && newStartTimeString != null) {
			newDate = convertStringToDate(newStartDateString);
			newTime = convertStringToTime(newStartTimeString);
		}

		// Check if the customer is in the system -------------------------------
		Customer aCustomer = findCustomer(customer);
		if (aCustomer == null) {
			errorMsg = errorMsg + customerNotLoggedIn;
			isError = true;
		}

		// Check if the service's name exists in the system ---------------------
		BookableService aBookableService = findBookableService(serviceName);
		if (aBookableService == null) {
			errorMsg = errorMsg + invalidServiceName;
			isError = true;
		}

		Appointment currentAppointment = findAppointment(customer, date, time);
		if (currentAppointment == null) {
			errorMsg = errorMsg + noAppointmentFound;
			isError = true;
		}

		if ((!newStartDateString.equals(date) || !newStartTimeString.equals(time))
				&& currentAppointment.getAppointmentState().compareTo(AppointmentState.InProgress) == 0) {
			throw new InvalidInputException("unsuccessful");
		}

		String chosenItemsString = "";
		if (aBookableService instanceof ServiceCombo) {
			for (int i = 0; i < currentAppointment.getChosenItems().size(); i++) {
				chosenItemsString = chosenItemsString + ","
						+ currentAppointment.getChosenItem(i).getService().getName();
			}
		}

		Time newEndTime = null;
		if (currentAppointment != null) {
			if (aBookableService instanceof Service) {
				newEndTime = getServiceEndTime(newTime, aBookableService);
			} else if (aBookableService instanceof ServiceCombo) {
				newEndTime = getServiceComboEndTime(newTime, aBookableService, chosenItemsString);
			}
		}

		Boolean addItem;

		if (changeService == true) {

			if (aBookableService instanceof Service) {
				newEndTime = getServiceEndTime(newTime, aBookableService);
			} else {
				newEndTime = getServiceComboEndTime(newTime, aBookableService,
						currentAppointment.getChosenItems().toString());
			}
		}

		if (action.isEmpty() || action == null) {
			addItem = null;
		} else {
			if (action.toLowerCase().compareTo("add") == 0) {
				addItem = true;
			} else if (action.toLowerCase().compareTo("remove") == 0) {
				addItem = false;
			} else {
				throw new InvalidInputException("unsuccessful");
			}
		}

		TimeSlot aTimeSlot = new TimeSlot(newDate, newTime, newDate, newEndTime, flexibook);
		Time tmpEndTime = newEndTime;

		if (addItem == null && itemActedUponString.isEmpty()) {
			try {
				currentAppointment.updateAppointment(aTimeSlot, changeService, aBookableService, addItem, null);
				FlexiBookPersistence.save(flexibook);
			} catch (RuntimeException e) {
				throw new InvalidInputException("unsuccessful");
			}
		}

		else {
			ServiceCombo combo = (ServiceCombo) currentAppointment.getBookableService();
			ComboItem optionalItem = findComboItem(itemActedUponString, combo);
			if (optionalItem != null) {
				Boolean isMandatoryItem = optionalItem.getMandatory();
				if (addItem == true) {
					
					LocalTime end = newEndTime.toLocalTime().plusMinutes(optionalItem.getService().getDuration());
					newEndTime = Time.valueOf(end);
					
					if (isTimeSlotAvailable(aDate, tmpEndTime, newEndTime)) {
						aTimeSlot.setEndTime(newEndTime);
					} else
						throw new InvalidInputException("unsuccessful");
				} else {
					if (!isMandatoryItem) {
						LocalTime end = newEndTime.toLocalTime().minusMinutes(optionalItem.getService().getDuration());
						newEndTime = Time.valueOf(end);
						aTimeSlot.setEndTime(newEndTime);
					} else
						throw new InvalidInputException("unsuccessful");
				}
				try {
					currentAppointment.updateAppointment(aTimeSlot, changeService, aBookableService, addItem,
							optionalItem);
					FlexiBookPersistence.save(FlexiBookApplication.getFlexibook());
				} catch (RuntimeException e) {
					throw new InvalidInputException("unsuccessful");
				}

			} else
				throw new InvalidInputException("Optional service not found");
		}

	}




	// Cancel an existing appointment ==================================
	/**
	 * @author antoninguerre Allows a customer to cancel his/her appointment
	 * 
	 */
	public static void cancelAppointment(String user, String customer, String serviceName, String date, String time)
			throws InvalidInputException {

		// Access Flexibook system manager -------------------------------------
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		SystemTime systemTime = null;

		// Possible error messages ----------------------------------------------
		String noBusiness = "There is no business";
		String noOwner = "There is no owner";
		String noServices = "There are no services";
		String ownerCancelAppointment = "An owner cannot cancel an appointment.";
		String customerNotLoggedIn = "Customer is not logged in the system";
		String invalidServiceName = "Input a valid service name.";
		String noAppointmentFound = "No appointment has been found in the system";
		String unsuccessful = "unsuccessful";

		// Keeps track of errors
		String errorMsg = "";
		boolean isError = false;

		// Check that a business exists -----------------------------------------
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner -----------------------------------------
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that there are services ----------------------------------------
		if (!flexibook.hasBookableServices()) {
			throw new InvalidInputException(noServices);
		}

		// Convert Strings to Date and Time
		Date aDate = convertStringToDate(date);
		Time aTime = convertStringToTime(time);

		// Check if the customer is in the system -------------------------------
		Customer aCustomer = findCustomer(customer);
		if (aCustomer == null) {
			errorMsg = errorMsg + customerNotLoggedIn;
			isError = true;
		}

		// Check if the service's name exists in the system ---------------------
		BookableService aBookableService = findBookableService(serviceName);
		if (aBookableService == null) {
			errorMsg = errorMsg + invalidServiceName;
			isError = true;
		}

		// Check that the appointment exists in the system
		Appointment currentAppointment = findAppointment(aCustomer, aBookableService, aDate, aTime);
		if (currentAppointment == null) {
			errorMsg = errorMsg + noAppointmentFound;
			isError = true;
		}

		try {
			if (user.equals("owner"))
				throw new InvalidInputException("An owner cannot cancel an appointment");

			else if (!user.equals(customer))
				throw new InvalidInputException("A customer can only cancel their own appointments");

			else if (aDate.equals(SystemTime.getSystemDate())) {
				throw new InvalidInputException("Cannot cancel an appointment on the appointment date");
			}

			else
				currentAppointment.cancelAppointment(aCustomer);

			FlexiBookPersistence.save(flexibook);

		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}

	// Customer No Show ========================================================
	/**
	 * @author antoninguerre
	 * 
	 */
	public static void customerNoShow(String aCustomer, String anAppointment, String date, String time)
			throws InvalidInputException {
		Appointment appointment = findAppointment(aCustomer, date, time);
		try {
			appointment.customerNoShow();
			FlexiBookPersistence.save(FlexiBookApplication.getFlexibook());
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}

	// Start Appointment ========================================================
	/**
	 * @author antoninguerre
	 * 
	 */
	public static void startAppointment(String aCustomer, String date, String time) throws InvalidInputException {
		Appointment appointment = findAppointment(aCustomer, date, time);
		try {
			appointment.startAppointment();
			FlexiBookPersistence.save(FlexiBookApplication.getFlexibook());
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}

	// End Appointment =========================================================
	/**
	 * @author antoninguerre
	 */
	public static void finishAppointment(String aCustomer, String date, String time) throws InvalidInputException {
		Appointment appointment = findAppointment(aCustomer, date, time);
		try {
			appointment.finishAppointment();
			FlexiBookPersistence.save(FlexiBookApplication.getFlexibook());
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
	}

	// #########################################################################
	// SIGN UP, UPDATE, DELETE CUSTOMER ACCOUNT ################################
	// #########################################################################

	/**
	 * Creates an account for the customer
	 * 
	 * @author egekaradibak
	 * @param userName : user name for the account
	 * @param password : password for the account
	 * @throws InvalidInputException
	 */
	public static void signUpCustomerAccount(String userName, String password) throws InvalidInputException {

		String error = "";
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		for (int i = 0; i < flexibook.getCustomers().size(); i++) {
			if (flexibook.getCustomer(i).getUsername().equals(userName)) {
				error = "The username already exists";
				throw new InvalidInputException(error);

			}
		}
		if (userName.isBlank()) {
			error = "The user name cannot be empty";
			throw new InvalidInputException(error);

		}
		if (password.isBlank()) {
			error = "The password cannot be empty";
			throw new InvalidInputException(error);

		}

		if (FlexiBookApplication.getCurrentUser() != null
				&& FlexiBookApplication.getCurrentUser().getUsername().equals("owner")) {

			error = "You must log out of the owner account before creating a customer account";
			throw new InvalidInputException(error);

		}
		if (error.length() > 0) {
			throw new InvalidInputException(error.trim());
		} else {

			try {
				flexibook.addCustomer(userName, password);
				logIn(userName, password);
				FlexiBookPersistence.save(flexibook);
			} catch (RuntimeException e) {
				error = e.getMessage();
				throw new InvalidInputException(error);
			}
		}

	}

	/**
	 * Updates the user name and the password for the account
	 * 
	 * @author egekaradibak
	 * @param userName : new user name for the account
	 * @param password : new password for the account
	 * @throws InvalidInputException
	 */
	public static void updateAccount(String userName, String password) throws InvalidInputException {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		String error = "";
		User currentUser = null;

		if (FlexiBookApplication.getCurrentUser().getUsername().equals("owner") && userName.equals("owner")) {

			FlexiBookApplication.getCurrentUser().setPassword(password);
		}

		if (FlexiBookApplication.getCurrentUser().getUsername().equals("owner") && !userName.equals("owner")) {

			error = "Changing username of owner is not allowed";

			throw new InvalidInputException(error);
		}
		if (userName.isBlank() && !FlexiBookApplication.getCurrentUser().getUsername().equals("owner")) {
			error = "The user name cannot be empty";
			throw new InvalidInputException(error);

		}
		if (password.isBlank()) {
			error = "The password cannot be empty";
			throw new InvalidInputException(error);

		}

		for (int i = 0; i < flexibook.getCustomers().size(); i++) {
			if (flexibook.getCustomer(i).getUsername().equals(userName)) {
				error = "Username not available";
				throw new InvalidInputException(error);

			}
		}

		FlexiBookApplication.getCurrentUser().setUsername(userName);
		FlexiBookApplication.getCurrentUser().setPassword(password);

		try {

			FlexiBookPersistence.save(flexibook);
		} catch (RuntimeException e) {
			error = e.getMessage();
			throw new InvalidInputException(error);
		}
	}

	/**
	 * Deletes the account
	 * 
	 * @author egekaradibak
	 * @param userName : user name of the account
	 * @throws InvalidInputException
	 */
	public static void deleteCustomerAccount(String userName) throws InvalidInputException {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		String error1 = "You do not have permission to delete this account";
		if (!(FlexiBookApplication.getCurrentUser().getUsername().equals(userName))
				|| FlexiBookApplication.getCurrentUser().getUsername().equals("owner") || userName.equals("owner")) {

			throw new InvalidInputException("You do not have permission to delete this account");
		}

		if (FlexiBookApplication.getCurrentUser() != null) {
			FlexiBookApplication.getCurrentUser().delete();
			logOut(FlexiBookApplication.getCurrentUser());
		}
		try {
			FlexiBookPersistence.save(flexibook);

		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}

	}

	// #########################################################################
	// DEFINE, UPDATE, DELETE SERVICE ##########################################
	// #########################################################################

	/**
	 * Adds a service
	 * 
	 * @author juliettedebray
	 * @param currentUser       : the user's username
	 * @param serviceName       : the name of the service being added
	 * @param aDuration         : the total duration of the service
	 * @param aDowntimeDuration : the duration of the downtime
	 * @param aDowntimeStart    : the time it takes before downtime starts
	 * @throws InvalidInputException
	 */
	public static void addService(String currentUser, String serviceName, String aDuration, String aDowntimeDuration,
			String aDowntimeStart) throws InvalidInputException {

		// Possible error messages
		String noBusiness = "There is no business";
		String noOwner = "There is no owner";
		String notOwner = "You are not authorized to perform this operation.";
		String nameError = "Please input a name for your service.";
		String durationError = "Please input a valid duration time.";
		String downtimeError = "Please input a valid downtime duration.";
		String downtimeBefore = "Downtime must not start before the beginning of the service.";
		String startTimeError = "Please input a valid downtime start time.";
		String downtimeTooLong = "Downtime must not end after the service.";
		String downtimeLogicError = "Your downtime combination is inconsistent with service duration, please review.";
		String alreadyExists = "already exists.";

		boolean isError = false;
		String errorMessage = "";

		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		User user = findCurrentUser(currentUser);

		int serviceDuration = Integer.parseInt(aDuration);
		int serviceDowntimeDuration = Integer.parseInt(aDowntimeDuration);
		int serviceDowntimeStart = Integer.parseInt(aDowntimeStart);

		// Check that a business exists
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that user is the owner
		if (currentUser.compareToIgnoreCase("owner") != 0) {
			isError = true;
			errorMessage = errorMessage + notOwner;
		}

		// Check that a name was inputed
		if (serviceName == null) {
			isError = true;
			errorMessage = errorMessage + nameError;
		}

		// Check that the service duration is valid
		if (serviceDuration >= 10 * 60) {
			isError = true;
			errorMessage = errorMessage + durationError;
		}
		if (serviceDuration <= 0) {
			isError = true;
			errorMessage = errorMessage + "Duration must be positive.";
		}

		// Check that the downtime duration is valid
		if (serviceDowntimeDuration < 0 && serviceDowntimeStart == 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime duration must be 0";
		}

		if (serviceDowntimeStart > 0 && serviceDowntimeDuration <= 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime duration must be positive.";
		}

		// Check downtime start
		if (serviceDowntimeStart < 0) {
			isError = true;
			errorMessage = errorMessage + downtimeBefore;
		}

		if (serviceDowntimeStart >= 10 * 60 || serviceDowntimeStart > serviceDuration) {
			isError = true;
			errorMessage = errorMessage + "Downtime must not start after the end of the service";
		}

		if (serviceDowntimeDuration > 0 && serviceDowntimeStart == 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime must not start at the beginning of the service.";
		}

		// check that the downtime does not start before the service
		if (serviceDowntimeStart < 0 && serviceDowntimeDuration > 0) {
			isError = true;
			errorMessage = errorMessage + downtimeBefore;
		}

		// check that downtime doesn't end after the service
		if (serviceDowntimeStart + serviceDowntimeDuration > serviceDuration) {
			isError = true;
			errorMessage = errorMessage + downtimeTooLong;
		}

		// check that the service does not already exist
		// Check that the new name doesn't belong to an existing service
		if (findBookableService(serviceName) != null) {
			isError = true;
			errorMessage = errorMessage + "Service " + serviceName + " " + alreadyExists;
		}

		// Add service to flexibook if no error
		if (isError == true) {
			throw new InvalidInputException(errorMessage.trim());
		} else {
			try {
				Service addedService = new Service(serviceName, flexibook, serviceDuration, serviceDowntimeDuration,
						serviceDowntimeStart);
				flexibook.addBookableService(addedService);
				FlexiBookPersistence.save(flexibook);
			} catch (RuntimeException e) {
				throw new InvalidInputException(e.getMessage());
			}
		}
	}

	// ---------------------------------------------------------------------------------------
	/**
	 * Updates a service
	 * 
	 * @author juliettedebray
	 * @param currentUser          : the user's username
	 * @param oldName              : the name of the service being updated
	 * @param newName              : the new name of the service
	 * @param aNewDuration         : the new total duration of the service
	 * @param aNewDowntimeDuration : the new duration of the downtime
	 * @param aNewDowntimeStart    : the new time it takes before downtime starts
	 * @throws InvalidInputException
	 */
	public static void updateService(String currentUser, String oldName, String newName, String aNewDuration,
			String aNewDowntimeDuration, String aNewDowntimeStart) throws InvalidInputException {

		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		String noBusiness = "There is no business";
		String noOwner = "There is no owner";
		String notOwner = "You are not authorized to perform this operation.";
		String noServices = "There are no existing services.";
		String nameError = "There are no existing services with that name.";
		String durationError = "Please input a valid duration time.";
		String downtimeError = "Please input a valid downtime duration.";
		String downtimeBefore = "Downtime must not start before the beginning of the service.";
		String downtimeTooLong = "Downtime must not end after the service.";
		String startTimeError = "Please input a valid downtime start time.";
		String alreadyExists = "already exists.";

		boolean serviceExists = false;
		Service oldService = null;

		int newDuration = Integer.parseInt(aNewDuration);
		int newDowntimeDuration = Integer.parseInt(aNewDowntimeDuration);
		int newDowntimeStart = Integer.parseInt(aNewDowntimeStart);

		User user = findCurrentUser(currentUser);

		boolean isError = false;
		String errorMessage = "";

		// Check that a business exists
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that there are services
		if (!flexibook.hasBookableServices()) {
			throw new InvalidInputException(noServices);
		}

		// Check that user is the owner
		if (currentUser.compareToIgnoreCase("owner") != 0) {
			isError = true;
			errorMessage = errorMessage + notOwner;
		}

		// Check that the new name doesn't belong to an existing service
		if (findBookableService(newName) != null && !(oldName.equalsIgnoreCase(newName))) {
			isError = true;
			errorMessage = errorMessage + "Service " + newName + " " + alreadyExists;
		}

		// Check that the service to update exists TRANSFER OBJECT
		if (findBookableService(oldName) == null) {
			isError = true;
			errorMessage = errorMessage + nameError;
		} else {
			oldService = (Service) findBookableService(oldName);
		}

		// Updating service name
		if (isError == false && errorMessage.compareTo("") != 0) {
			oldService.setName(newName);
		} else
			oldService.setName(oldName);

		// Check that the service duration is valid
		if (newDuration >= 10 * 60) {
			isError = true;
			errorMessage = errorMessage + durationError;
		}
		if (newDuration <= 0) {
			isError = true;
			errorMessage = errorMessage + "Duration must be positive.";
		}
		// Update service duration
		if (isError == false && errorMessage.compareTo("") != 0) {
			oldService.setDuration(newDuration);
		}

		// Check that the downtime duration is valid
		if (newDowntimeDuration >= 10 * 60) {
			isError = true;
			errorMessage = errorMessage + downtimeError;
		}

		if (newDowntimeDuration < 0 && newDowntimeStart == 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime duration must be 0";
		}

		if (newDowntimeStart > 0 && newDowntimeDuration <= 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime duration must be positive.";
		}

		// check that the downtime does not start before the service
		if (newDowntimeStart < 0 && newDowntimeDuration > 0) {
			isError = true;
			errorMessage = errorMessage + downtimeBefore;
		}

		// check that downtime doesn't end after the service
		if (newDowntimeDuration > newDuration || newDowntimeStart + newDowntimeDuration > newDuration) {
			isError = true;
			errorMessage = errorMessage + downtimeTooLong;
		}

		// Update downtime duration
		if (isError == false && errorMessage.compareTo("") != 0) {
			oldService.setDowntimeDuration(newDowntimeDuration);
		}

		// Check that the downtime start time is valid
		if (newDowntimeStart >= 10 * 60 || newDowntimeStart > newDuration) {
			isError = true;
			errorMessage = errorMessage + startTimeError;
		}
		if (newDowntimeDuration > 0 && newDowntimeStart == 0) {
			isError = true;
			errorMessage = errorMessage + "Downtime must not start at the beginning of the service.";
		}

		// Update downtime start time
		if (isError == false && errorMessage.compareTo("") != 0) {
			oldService.setDowntimeStart(newDowntimeStart);
		}

		// Throwing a runtime exception if there is an error
		if (isError) {
			
			throw new InvalidInputException(errorMessage);
		} else {
			
			oldService.setName(newName);
			oldService.setDuration(newDuration);
			oldService.setDowntimeDuration(newDowntimeDuration);
			oldService.setDowntimeStart(newDowntimeStart);
		}
		// else {
		try {
			FlexiBookPersistence.save(flexibook);
		} catch (RuntimeException e) {
			throw new InvalidInputException(e.getMessage());
		}
		// }
	}

	// ---------------------------------------------------------------------------------------
	/**
	 * Deletes a service
	 * 
	 * @author juliettedebray
	 * @param currentUser        : the user's username
	 * @param deletedServiceName : the name of the service being deleted
	 * @throws InvalidInputException
	 */
	public static void deleteService(String currentUser, String deletedServiceName) throws InvalidInputException {

		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		String noBusiness = "There is no business";
		String noOwner = "There is no owner";
		String notOwner = "You are not authorized to perform this operation.";
		String noServices = "There are no existing services.";
		String nameError = "There are no existing services with that name.";
		String futureAppointments = "The service contains future appointments.";

		User user = findCurrentUser(currentUser);
		boolean isError = false;
		String errorMessage = "";
		boolean serviceExists = false;
		List<BookableService> existingServices = flexibook.getBookableServices();

		// Temporarily assign a service
		Service deletedService = null;// = (Service) existingServices.get(0);

		// Check that a business exists
		if (!flexibook.hasBusiness()) {
			throw new InvalidInputException(noBusiness);
		}

		// Check that there is an owner
		if (!flexibook.hasOwner()) {
			throw new InvalidInputException(noOwner);
		}

		// Check that there are services
		if (!flexibook.hasBookableServices()) {
			throw new InvalidInputException(noServices);
		}

		// Check that user is the owner
		if (currentUser.compareToIgnoreCase("owner") != 0) {
			isError = true;
			errorMessage = errorMessage + notOwner;
		}

		// Check that the service to update exists TRANSFER OBJECT
		if (findBookableService(deletedServiceName) == null) {
			isError = true;
			errorMessage = errorMessage + nameError;
		} else {
			deletedService = (Service) findBookableService(deletedServiceName);
		}

		// Check that there are no future appointments associated with the service being
		// deleted
		for (Appointment apt : deletedService.getAppointments()) {
			if (apt.getTimeSlot().getEndDate().after(SystemTime.getSystemDate())) {
				isError = true;
				errorMessage = errorMessage + futureAppointments;
			}
		}

		// Delete service

		if (isError) {
			throw new InvalidInputException(errorMessage);
		} else {
			ArrayList<String> servicesName = new ArrayList<String>();
			for (BookableService service : flexibook.getBookableServices()) {
				servicesName.add(service.getName());
			}
			for (String serviceName : servicesName) {
				BookableService service = findBookableService(serviceName);
				if (service instanceof ServiceCombo && ((ServiceCombo) service).getMainService() != null) {
					if (((ServiceCombo) service).getMainService().getService() == deletedService) {
						service.delete();
					} else {
						ArrayList<String> comboItemsName = new ArrayList<String>();
						for (ComboItem comboItem : ((ServiceCombo) service).getServices()) {
							comboItemsName.add(comboItem.getService().getName());
						}
						for (String comboItemName : comboItemsName) {
							ComboItem comboItem = findComboItem(comboItemName, (ServiceCombo) service);

							if (comboItem.getService() == deletedService) {
								comboItem.delete();
							}
						}
					}
				}
			}
			try {
				deletedService.delete();
				FlexiBookPersistence.save(flexibook);
			} catch (RuntimeException e) {
				throw new InvalidInputException(e.getMessage());
			}
		}
	}





	// #########################################################################
	// LOG IN, LOG OUT, VIEW APPOINTMENT CALENDAR ##############################
	// #########################################################################

	/**
	 * @author lukasalatalo
	 */
	public static void createOwner() throws InvalidInputException {
		// Possible errors messages --------------------------------------------
		String errorExists = " Error: An owner already exists; please update.";

		// Access Flexibook system manager -------------------------------------
		FlexiBook fb = FlexiBookApplication.getFlexibook();

		// Check that business does not exist ----------------------------------
		if (fb.hasOwner()) {
			throw new InvalidInputException(errorExists);
		} else {
			try {
				Owner aNewOwner = new Owner("owner", "owner", fb);

				fb.setOwner(aNewOwner);
				FlexiBookPersistence.save(fb);
			} catch (RuntimeException e) {
				throw new InvalidInputException(e.getMessage());
			}
		}
	}

	/**
	 * @author lukasalatalo
	 */
	public static void logIn(String aUsername, String aPassword) throws InvalidInputException {
		String error = "Username/password not found";
		User currentUser = null;
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		boolean hasUser = false;
		if (!flexibook.hasOwner()) {
			Owner newOwner = new Owner("owner", "owner", flexibook);
			flexibook.setOwner(newOwner);

		}
		if (aUsername.equals("owner") && flexibook.getOwner().getPassword().equals(aPassword)) {
			currentUser = flexibook.getOwner();
			FlexiBookApplication.setCurrentUser(currentUser);
			hasUser = true;

		}
		if (User.getWithUsername(aUsername) != null
				&& User.getWithUsername(aUsername).getPassword().equals(aPassword)) {
			currentUser = User.getWithUsername(aUsername);
			hasUser = true;
			FlexiBookApplication.setCurrentUser(currentUser);
	
		}
		if (!hasUser) {
			throw new InvalidInputException(error);
		}

	}

	/**
	 * @author lukasalatalo
	 */
	public static void logOut(User aUser) throws InvalidInputException {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		User currentUser = FlexiBookApplication.getCurrentUser();
		if (aUser == null) {
			throw new InvalidInputException("The user is already logged out");
		} else {
			try {
				FlexiBookApplication.setCurrentUser(null);
				FlexiBookPersistence.save(flexibook);
			} catch (RuntimeException e) {
				throw new InvalidInputException(e.getMessage());
			}
		}
	}



	// #########################################################################
		// SET UP, UPDATE BUSINESS INFORMATION #####################################
		// #########################################################################

		// Reset Owner Password ====================================================
		/**
		 * Allows the owner to reset their password
		 * 
		 * @author Jeremy (260660818)
		 * @param aPassword : the new password
		 * @throws InvalidInputException
		 */
		public static void resetOwnerPassword(String aPassword) throws InvalidInputException {
			// Possible errors message --------------------------------------------
			String errorDNExists = " An owner does not exist.";
			String errorNoPass = " Invalid password";
			String errorBadPass = " A password must be more than 8 characters long, contain a capital and a lowercase letter.";

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();

			// Check that business does not exist ----------------------------------
			if (!fb.hasOwner()) {
				throw new InvalidInputException(errorDNExists);
			}

			// Check that password is not empty and is well-formed -----------------
			if (aPassword == null || aPassword.trim().length() == 0) {
				isError = true;
				errorMsg = errorMsg + errorNoPass;
			} else if (VALID_PASSWORD_REGEX.matcher(aPassword).matches()) {
				isError = true;
				errorMsg = errorMsg + errorBadPass;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					fb.getOwner().setPassword(aPassword);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Create Business =========================================================
		/**
		 * Creates the business for the Flexibook system
		 * 
		 * @author Jeremy (260660818)
		 * @param aName        : the business name
		 * @param aAddress     : the business address
		 * @param aPhoneNumber : the business phone number
		 * @param aEmail       : the business email
		 * @throws InvalidInputException
		 */
		public static void createBusinessInfo(String aName, String aAddress, String aPhoneNumber, String aEmail, User user)
				throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to set up business information.";
			String errorExists = " A business already exists";
			String errorName = " Invalid name.";
			String errorAddress = " Invalid address.";
			String errorPhoneNumber = " Invalid phone number.";
			String errorEmail = " Invalid email.";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();

			// Check that business does not exist ----------------------------------
			if (fb.hasBusiness()) {
				throw new InvalidInputException(errorExists);
			}

			// Check that name is not empty ----------------------------------------
			if (aName == null || aName.trim().length() == 0) {
				isError = true;
				errorMsg = errorMsg + errorName;
			}

			// Check that address is not empty -------------------------------------
			if (aAddress == null || aAddress.trim().length() == 0) {
				// More validation for address format using regular expressions
				// deemed out of scope
				isError = true;
				errorMsg = errorMsg + errorAddress;
			}

			// Check that phone number is not empty and is well-formed -------------
			if (aPhoneNumber == null || aPhoneNumber.trim().length() == 0) {
				isError = true;
				errorMsg = errorMsg + errorPhoneNumber;
			} else if (!VALID_PHONE_REGEX.matcher(aPhoneNumber).matches()) {
				isError = true;
				errorMsg = errorMsg + errorPhoneNumber;
			}

			// Check that email is not empty and is well-formed --------------------
			if (aEmail == null || aEmail.trim().length() == 0) {
				isError = true;
				errorMsg = errorMsg + errorEmail;
			} else if (!VALID_EMAIL_REGEX.matcher(aEmail).matches()) {
				isError = true;
				errorMsg = errorMsg + errorEmail;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					Business aNewBusiness = new Business(aName, aAddress, aPhoneNumber, aEmail, fb);
					fb.setBusiness(aNewBusiness);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Update Business =========================================================
		/**
		 * Allows the updating of one or many business attributes
		 * 
		 * @author Jeremy (260660818)
		 * @param aName        : the new name (leave null if not updated)
		 * @param aAddress     : the new address (leave null if not updated)
		 * @param aPhoneNumber : the new phone number (leave null if not updated)
		 * @param aEmail       : the new email (leave null if not updated)
		 * @throws InvalidInputException
		 */
		public static void updateBusinessInfo(String aName, String aAddress, String aPhoneNumber, String aEmail, User user)
				throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorDNExists = " A business doesn't exist.";
			String errorName = " Invalid name.";
			String errorAddress = " Invalid address.";
			String errorPhoneNumber = " Invalid phone number.";
			String errorEmail = " Invalid email.";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = fb.getBusiness();

			// Check that business exists ------------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorDNExists);
			}

			// Check that name is not empty ----------------------------------------
			// If empty, throw error, if null pass, if valid update
			if (aName != null) {
				if (aName.trim().length() == 0) {
					isError = true;
					errorMsg = errorMsg + errorName;
				} else {
					business.setName(aName);
				}
			}

			// Check that address is not empty -------------------------------------
			// If empty, throw error, if null pass, if valid update
			if (aAddress != null) {
				if (aAddress.trim().length() == 0) {
					// More validation for address format using regular expressions
					// deemed out of scope
					isError = true;
					errorMsg = errorMsg + errorAddress;
				} else {
					business.setAddress(aAddress);
				}
			}

			// Check that phone number is not empty and is well-formed -------------
			// If empty, throw error, if null pass, if valid update
			if (aPhoneNumber != null) {
				if (aPhoneNumber.trim().length() == 0) {
					isError = true;
					errorMsg = errorMsg + errorPhoneNumber;
				} else if (!VALID_PHONE_REGEX.matcher(aPhoneNumber).matches()) {
					isError = true;
					errorMsg = errorMsg + errorPhoneNumber;
				} else {
					business.setPhoneNumber(aPhoneNumber);
				}
			}

			// Check that email is not empty and is well-formed --------------------
			// If empty, throw error, if null pass, if valid update
			if (aEmail != null) {
				if (aEmail.trim().length() == 0) {
					isError = true;
					errorMsg = errorMsg + errorEmail;
				} else if (!VALID_EMAIL_REGEX.matcher(aEmail).matches()) {
					isError = true;
					errorMsg = errorMsg + errorEmail;
				} else {
					business.setEmail(aEmail);
				}
			}

			// If there's an error, throw Exception --------------------------------
			// Else, reset business to manager and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Get Business ============================================================
		/**
		 * Retrieves the business associated with this flexibook app
		 * 
		 * @author jeremy
		 * @return Business object
		 * @throws InvalidInputException
		 */
		public static Business getBusiness() throws InvalidInputException {
			String errorMsg = "There is currently no business. Please create one.";

			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			if (fb.hasBusiness()) {
				business = fb.getBusiness();
			} else {
				throw new InvalidInputException(errorMsg);
			}

			return business;
		}

		// Has Business ============================================================
		/**
		 * Indicates if a business exists
		 * 
		 * @author jeremy
		 * @return boolean True if there is a business created
		 */
		public static boolean hasBusiness() {
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			return fb.hasBusiness();
		}

		// Add Business Hours ======================================================
		/**
		 * Adds business hours for a specific day. Guards against duplicate days.
		 * 
		 * @param aDayOfWeek : day of the week for these hours
		 * @param aStartTime : time at which the business opens on the day
		 * @param aEndTime   : time at which the business closes on the day
		 * @throws InvalidInputException
		 */
		// @author Jeremy (260660818)
		public static void addBusinessHours(DayOfWeek aDayOfWeek, Time aStartTime, Time aEndTime, User user)
				throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBDNExists = " A business doesn't exist.";
			String errorMissingArgs = " Missing Input.";
			String errorDayExists = " The business hours cannot overlap";
			String errorTime = " Start time must be before end time";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does not exist ----------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check all args are filled -------------------------------------------
			if (aDayOfWeek == null || aStartTime == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that start time < end time ------------------------------------
			if (aStartTime.after(aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorTime;
			}

			// Check that the DayOfWeek does not already exist ---------------------
			for (BusinessHour bh : business.getBusinessHours()) {
				if (bh.getDayOfWeek() == aDayOfWeek) {
					if (((bh.getStartTime().before(aStartTime)) && (bh.getEndTime().after(aStartTime)))
							|| ((bh.getStartTime().after(aStartTime)) && (bh.getEndTime().before(aEndTime)))
							|| ((bh.getStartTime().before(aEndTime)) && (bh.getEndTime().after(aEndTime)))) {
						isError = true;
						errorMsg = errorMsg + errorDayExists;
						break;
					}
				}
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					BusinessHour aBusinessHour = new BusinessHour(aDayOfWeek, aStartTime, aEndTime, fb);

					business.addBusinessHour(aBusinessHour);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Update Business Hours ===================================================
		/**
		 * Updates the business hours for a specific day of the week
		 * 
		 * @author Jeremy (260660818)
		 * @param aDayOfWeek : day of the week for these hours
		 * @param aStartTime : new time at which the business opens on the day
		 * @param aEndTime   : new time at which the business closes on the day
		 * @throws InvalidInputException
		 */
		public static void updateBusinessHours(int aBusinessHourIdx, DayOfWeek aDayOfWeek, Time aStartTime, Time aEndTime,
				User user) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBusDNExists = " A business doesn't exist yet";
			String errorMissingArgs = " Missing Input.";
			String errorDayDNExists = " The business hours cannot overlap";
			String errorTime = " Start time must be before end time.";
			String errorDayExists = " The business hours cannot overlap";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does not exist ----------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBusDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check all args are filled -------------------------------------------
			if (aDayOfWeek == null || aStartTime == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that indexed hours exists -------------------------------------
			if ((aBusinessHourIdx >= fb.getBusiness().getBusinessHours().size()) || (aBusinessHourIdx < 0)) {
				isError = true;
				errorMsg = errorMsg + errorDayDNExists;
			}

			// Check that start time < end time ------------------------------------
			if (aStartTime.after(aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorTime;
			}

			// Check that the DayOfWeek does not already exist ---------------------
			for (BusinessHour bh : business.getBusinessHours()) {
				if (business.indexOfBusinessHour(bh) == aBusinessHourIdx) {
					continue;
				}

				if (bh.getDayOfWeek() == aDayOfWeek) {
					if (((bh.getStartTime().before(aStartTime)) && (bh.getEndTime().after(aStartTime)))
							|| ((bh.getStartTime().after(aStartTime)) && (bh.getEndTime().before(aEndTime)))
							|| ((bh.getStartTime().before(aEndTime)) && (bh.getEndTime().after(aEndTime)))) {
						isError = true;
						errorMsg = errorMsg + errorDayExists;
						break;
					}
				}
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {

					fb.getBusiness().getBusinessHour(aBusinessHourIdx).setDayOfWeek(aDayOfWeek);
					fb.getBusiness().getBusinessHour(aBusinessHourIdx).setStartTime(aStartTime);
					fb.getBusiness().getBusinessHour(aBusinessHourIdx).setEndTime(aEndTime);

					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Remove Business Hours ===================================================
		/**
		 * Removes these business hours from a business and then the system
		 * 
		 * @author Jeremy (260660818)
		 * @param aBusinessHour
		 * @throws InvalidInputException
		 */
		public static void removeBusinessHours(int aBusinessHourIdx, User user) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBusDNExists = " A business doesn't exist yet.";
			String errorHrsDNExists = " These hours are not associated with a business.";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBusDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check that vacation does exist --------------------------------------
			if ((aBusinessHourIdx >= fb.getBusiness().getBusinessHours().size()) || (aBusinessHourIdx < 0)) {
				isError = true;
				throw new InvalidInputException(errorHrsDNExists);
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					BusinessHour hour = business.getBusinessHour(aBusinessHourIdx);
					business.removeBusinessHour(hour);
					fb.removeHour(hour);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Get Business Hours Index ================================================
		/**
		 * Returns the index of a BusinessHour based on input
		 * 
		 * @author Jeremy (260660818)
		 * @param aDayOfWeek : day of the week to match
		 * @param aStartTime : start time to match
		 * @return index of the Business hour or -1 if not found
		 * @throws InvalidInputException
		 */
		public static int getBusinessHoursIdx(DayOfWeek aDayOfWeek, Time aStartTime) throws InvalidInputException {
			// Possible errors messages --------------------------------------------
			String errorBusDNExists = " A business doesn't exist yet.";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBusDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Find BusinessHour object that matches arguments ---------------------
			for (BusinessHour bh : business.getBusinessHours()) {
				if ((bh.getDayOfWeek() == aDayOfWeek) && (bh.getStartTime().compareTo(aStartTime) == 0)) {
					return business.indexOfBusinessHour(bh);
				}
			}

			// If it gets here, none was found -------------------------------------
			return -1;
		}

		// =========================================================================
		/**
		 * Retrieves all business hours
		 * 
		 * @author jeremy
		 * @return list of all business hours
		 * @throws InvalidInputException
		 */
		public static List<BusinessHour> getBusinessHours() throws InvalidInputException {
			String errorMsg = "There are no business hours available...";
			List<BusinessHour> hours = new ArrayList<BusinessHour>();

			FlexiBook fb = FlexiBookApplication.getFlexibook();

			if (fb.hasBusiness() && fb.getBusiness().hasBusinessHours()) {
				hours = fb.getBusiness().getBusinessHours();
			} else {
				throw new InvalidInputException(errorMsg);
			}

			return hours;
		}

		// Add Vacation Hours ======================================================
		/**
		 * Adds a timeslot for a vacation
		 * 
		 * @author Jeremy (260660818)
		 * @param aStartDate : date on which the vacation starts
		 * @param aStartTime : time at which the vacation starts
		 * @param aEndDate   : date on which the vacation ends
		 * @param aEndTime   : time at which the vacation ends
		 * @throws InvalidInputException
		 */
		public static void addVacation(Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime, User user,
				Date systemTime) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBDNExists = " A business doesn't exist yet.";
			String errorMissingArgs = " Missing Input.";
			String errorDate = " Start time must be before end time.";
			String errorTime = " Start time must be before end time.";
			String errorConf = " Vacation times cannot overlap";
			String errorConfHol = "Holiday and vacation times cannot overlap";
			String errorPast = "Vacation cannot start in the past";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check all args are filled -------------------------------------------
			if (aStartDate == null || aStartTime == null || aEndDate == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that the vacation does not start in the past -------------------
			if (aStartDate.before(systemTime)) {
				isError = true;
				errorMsg = errorMsg + errorPast;
			}

			// Check that start time < end time ------------------------------------
			if (aStartDate.compareTo(aEndDate) == 0) {
				if (aStartTime.after(aEndTime)) {
					isError = true;
					errorMsg = errorMsg + errorTime;
				}
			}

			// Check that start date < end date ------------------------------------
			if (aStartDate.after(aEndDate)) {
				isError = true;
				errorMsg = errorMsg + errorDate;
			}

			// Check for time conflicts --------------------------------------------
			if (vacationConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConf;
			}

			if (holidayConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConfHol;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aVacation = new TimeSlot(aStartDate, aStartTime, aEndDate, aEndTime, fb);
					business.addVacation(aVacation);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Update Vacation Hours ===================================================
		/**
		 * Updates specific vacation hours. Guards against hours that are not associated
		 * with a business.
		 * 
		 * @author Jeremy (260660818)
		 * @param aVacation  : the vacation slot to update
		 * @param aStartDate : the new date on which the vacation starts
		 * @param aStartTime : the new time at which the vacation starts
		 * @param aEndDate   : the new date on which the vacation ends
		 * @param aEndTime   : the new time at which the vacation ends
		 * @throws InvalidInputException
		 */
		public static void updateVacation(int aVacationIdx, Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime,
				User user, Date systemTime) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBDNExists = " A business doesn't exist yet.";
			String errorVDNExists = " This vacation is not associated with a business.";
			String errorMissingArgs = " Missing Input.";
			String errorDate = " Start time must be before end time.";
			String errorTime = " Start time must be before ned time.";
			String errorConf = " Vacation time cannot overlap.";
			String errorConfHol = "Holiday and vacation times cannot overlap";
			String errorPast = "Vacation cannot start in the past";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check that vacation does exist --------------------------------------
			if ((aVacationIdx >= fb.getBusiness().getBusinessHours().size()) || (aVacationIdx < 0)) {
				isError = true;
				throw new InvalidInputException(errorVDNExists);
			}

			// Check all args are filled -------------------------------------------
			if (aStartDate == null || aStartTime == null || aEndDate == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that the vacation does not start in the past -------------------
			if (aStartDate.before(systemTime)) {
				isError = true;
				errorMsg = errorMsg + errorPast;
			}

			// Check that start time < end time ------------------------------------
			if (aStartDate.compareTo(aEndDate) == 0) {
				if (aStartTime.after(aEndTime)) {
					isError = true;
					errorMsg = errorMsg + errorTime;
				}
			}

			// Check that start date < end date ------------------------------------
			if (aStartDate.after(aEndDate)) {
				isError = true;
				errorMsg = errorMsg + errorDate;
			}

			// Check for time conflicts --------------------------------------------
			if (vacationConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConf;
			}

			if (holidayConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConfHol;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aVacation = business.getVacation(aVacationIdx);
					aVacation.setStartDate(aStartDate);
					aVacation.setStartTime(aStartTime);
					aVacation.setEndDate(aEndDate);
					aVacation.setEndTime(aEndTime);

					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Remove Vacation Hours ===================================================
		/**
		 * Removes a vacation timeslot from a business and then the system
		 * 
		 * @author Jeremy (260660818)
		 * @param aVacation : the vacation timeslot to remove
		 * @throws InvalidInputException
		 */
		public static void removeVacation(int aVacationIdx, User user) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information.";
			String errorBDNExists = " A business doesn't exist yet.";
			String errorVDNExists = " This vacation is not associated with a business.";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				isError = true;
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check that vacation does exist --------------------------------------
			if ((aVacationIdx >= fb.getBusiness().getBusinessHours().size()) || (aVacationIdx < 0)) {
				isError = true;
				throw new InvalidInputException(errorVDNExists);
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aVacation = business.getVacation(aVacationIdx);
					business.removeVacation(aVacation);
					fb.removeTimeSlot(aVacation);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Get Vacation Object Index ===============================================
		/**
		 * Returns the index of a TimeSlot based on input
		 * 
		 * @author Jeremy (260660818)
		 * @param aStartDate : start date to match
		 * @param aStartTime : start time to match
		 * @return index of the TimeSlot or -1 if not found
		 * @throws InvalidInputException
		 */
		public static int getVacationIdx(Date aStartDate, Time aStartTime) throws InvalidInputException {
			// Possible errors messages --------------------------------------------
			String errorBusDNExists = " A business doesn't exist yet.";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBusDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Find BusinessHour object that matches arguments ---------------------
			for (TimeSlot ts : business.getVacation()) {
				if ((ts.getStartDate().compareTo(aStartDate) == 0) && (ts.getStartTime().compareTo(aStartTime) == 0)) {
					return business.indexOfVacation(ts);
				}
			}

			// If it gets here, none was found -------------------------------------
			return -1;
		}

		// =========================================================================
		/**
		 * Retrieves list of vacation timeslots
		 * 
		 * @author jeremy
		 * @return list of vacation timeslots
		 * @throws InvalidInputException
		 */
		public static List<TimeSlot> getVacations() throws InvalidInputException {
			String errorMsg = "There are no vacations available...";
			List<TimeSlot> slots = new ArrayList<TimeSlot>();

			FlexiBook fb = FlexiBookApplication.getFlexibook();

			if (fb.hasBusiness() && fb.getBusiness().hasVacation()) {
				slots = fb.getBusiness().getVacation();
			} else {
				throw new InvalidInputException(errorMsg);
			}

			return slots;
		}

		// Add Holiday Hours =======================================================
		/**
		 * Adds a timeslot for a holiday
		 * 
		 * @author Jeremy (260660818)
		 * @param aStartDate : date on which the holiday starts
		 * @param aStartTime : time at which the holiday starts
		 * @param aEndDate   : date on which the holiday ends
		 * @param aEndTime   : time at which the holiday ends
		 * @throws InvalidInputException
		 */
		public static void addHoliday(Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime, User user,
				Date systemTime) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to set up business information.";
			String errorBDNExists = " A business doesn't exist yet.";
			String errorMissingArgs = " Missing Input.";
			String errorDate = " Start time must be before end time.";
			String errorTime = " Start time must be before end time.";
			String errorConf = " Holiday times cannot overlap";
			String errorPast = "Holiday cannot start in the past";
			String errorConfVac = "Holiday and vacation times cannot overlap";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check all args are filled -------------------------------------------
			if (aStartDate == null || aStartTime == null || aEndDate == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that the holiday does not start in the past --------------------
			if (aStartDate.before(systemTime)) {
				isError = true;
				errorMsg = errorMsg + errorPast;
			}

			// Check that start time < end time ------------------------------------
			if (aStartDate.compareTo(aEndDate) == 0) {
				if (aStartTime.after(aEndTime)) {
					isError = true;
					errorMsg = errorMsg + errorTime;
				}
			}

			// Check that start date < end date ------------------------------------
			if (aStartDate.after(aEndDate)) {
				isError = true;
				errorMsg = errorMsg + errorDate;
			}

			// Check for time conflicts --------------------------------------------
			if (holidayConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConf;
			}

			// Check that holidays and vacations do not overlap ---------------------
			if (vacationConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConfVac;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aHoliday = new TimeSlot(aStartDate, aStartTime, aEndDate, aEndTime, fb);
					business.addHoliday(aHoliday);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Update Holiday Hours ====================================================
		/**
		 * Updates the timeslot for a holiday
		 * 
		 * @author Jeremy (260660818)
		 * @param aStartDate : the new date on which the holiday starts
		 * @param aStartTime : the new time at which the holiday starts
		 * @param aEndDate   : the new date on which the holiday ends
		 * @param aEndTime   : the new time at which the holiday ends
		 * @throws InvalidInputException
		 */
		public static void updateHoliday(int aHolidayIdx, Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime,
				User user, Date systemTime) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information.";
			String errorBDNExists = " A business doesn't exist yet";
			String errorHDNExists = " This holiday is not associated with the business.";
			String errorMissingArgs = " Missing Input.";
			String errorDate = " Start time must be before end time.";
			String errorTime = " Start time must be before end time.";
			String errorConf = " Holiday times cannot overlap";
			String errorPast = "Holiday cannot be in the past";
			String errorConfVac = "Holiday and vacation times cannot overlap";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check that vacation does exist --------------------------------------
			if ((aHolidayIdx >= fb.getBusiness().getBusinessHours().size()) || (aHolidayIdx < 0)) {
				isError = true;
				throw new InvalidInputException(errorHDNExists);
			}

			// Check all args are filled -------------------------------------------
			if (aStartDate == null || aStartTime == null || aEndDate == null || aEndTime == null) {
				isError = true;
				errorMsg = errorMsg + errorMissingArgs;
			}

			// Check that the holiday does not start in the past --------------------
			if (aStartDate.before(systemTime)) {
				isError = true;
				errorMsg = errorMsg + errorPast;
			}

			// Check that start time < end time ------------------------------------
			if (aStartDate.compareTo(aEndDate) == 0) {
				if (aStartTime.after(aEndTime)) {
					isError = true;
					errorMsg = errorMsg + errorTime;
				}
			}

			// Check that start date < end date ------------------------------------
			if (aStartDate.after(aEndDate)) {
				isError = true;
				errorMsg = errorMsg + errorDate;
			}

			// Check for time conflicts --------------------------------------------
			if (holidayConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConf;
			}

			// Check that holidays and vacations do not overlap ---------------------
			if (vacationConflict(aStartDate, aStartTime, aEndDate, aEndTime)) {
				isError = true;
				errorMsg = errorMsg + errorConfVac;
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aHoliday = business.getHoliday(aHolidayIdx);
					aHoliday.setStartDate(aStartDate);
					aHoliday.setStartTime(aStartTime);
					aHoliday.setEndDate(aEndDate);
					aHoliday.setEndTime(aEndTime);

					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Remove Holiday Hours ====================================================
		/**
		 * Removes a holiday timeslot from a business and then the system
		 * 
		 * @author Jeremy (260660818)
		 * @param aVacation : the holiday timeslot to remove
		 * @throws InvalidInputException
		 */
		public static void removeHoliday(int aHolidayIdx, User user) throws InvalidInputException {

			// Possible errors messages --------------------------------------------
			String errorPermis = " No permission to update business information";
			String errorBDNExists = " A business doesn't exist yet";
			String errorHDNExists = " This holiday is not associated with the business";

			// Authenticate --------------------------------------------------------
			if (user == null || !(user instanceof Owner)) {
				throw new InvalidInputException(errorPermis);
			}

			// Track Errors --------------------------------------------------------
			boolean isError = false;
			String errorMsg = "";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Check that vacation does exist --------------------------------------
			if ((aHolidayIdx >= fb.getBusiness().getBusinessHours().size()) || (aHolidayIdx < 0)) {
				isError = true;
				throw new InvalidInputException(errorHDNExists);
			}

			// If there's an error, throw Exception --------------------------------
			// Else, try to instantiate business and save to persistence layer
			if (isError) {
				throw new InvalidInputException(errorMsg.trim());
			} else {
				try {
					TimeSlot aHoliday = business.getHoliday(aHolidayIdx);
					business.removeHoliday(aHoliday);
					fb.removeTimeSlot(aHoliday);
					FlexiBookPersistence.save(fb);
				} catch (RuntimeException e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		// Get Holiday Object Index ================================================
		/**
		 * Returns the index of a TimeSlot based on input
		 * 
		 * @author Jeremy (260660818)
		 * @param aStartDate : start date to match
		 * @param aStartTime : start time to match
		 * @return index of the TimeSlot or -1 if not found
		 * @throws InvalidInputException
		 */
		public static int getHolidayIdx(Date aStartDate, Time aStartTime) throws InvalidInputException {
			// Possible errors messages --------------------------------------------
			String errorBusDNExists = " A business doesn't exist yet.";

			// Access Flexibook system manager -------------------------------------
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			Business business = null;

			// Check that business does exist --------------------------------------
			if (!fb.hasBusiness()) {
				throw new InvalidInputException(errorBusDNExists);
			} else {
				business = fb.getBusiness();
			}

			// Find BusinessHour object that matches arguments ---------------------
			for (TimeSlot ts : business.getHolidays()) {
				if ((ts.getStartDate().compareTo(aStartDate) == 0) && (ts.getStartTime().compareTo(aStartTime) == 0)) {
					return business.indexOfHoliday(ts);
				}
			}

			// If it gets here, none was found -------------------------------------
			return -1;
		}

		// =========================================================================
		/**
		 * Retrieves list of all holiday timeslots
		 * 
		 * @author jeremy
		 * @return list of holiday timeslots
		 * @throws InvalidInputException
		 */
		public static List<TimeSlot> getHolidays() throws InvalidInputException {
			String errorMsg = "There are no holidays available...";
			List<TimeSlot> slots = new ArrayList<TimeSlot>();

			FlexiBook fb = FlexiBookApplication.getFlexibook();

			if (fb.hasBusiness() && fb.getBusiness().hasHolidays()) {
				slots = fb.getBusiness().getHolidays();
			} else {
				throw new InvalidInputException(errorMsg);
			}

			return slots;
		}




		// #########################################################################
		// DEFINE, UPDATE, DELETE SERVICE COMBO ####################################
		// #########################################################################

		// @author James
		public static void defineServiceCombo(String name, String mainServiceS, String servicesS, String mandatoryS)
				throws InvalidInputException {
			FlexiBook flexibook = FlexiBookApplication.getFlexibook();
			String[] serviceNames = servicesS.split(",");
			Service[] services = new Service[serviceNames.length];
			String[] mandatoryValues = mandatoryS.split(",");
			boolean[] mandatory = new boolean[mandatoryValues.length];
			Service mainService = null;
			List<BookableService> allServices = flexibook.getBookableServices();
			boolean error = false;
			String errMessage = "";
			boolean mainInc = false;

			for (int i = 0; i < allServices.size(); i++) {
				if (allServices.get(i).getName().equals(name)) {
					error = true;
					errMessage = "Service combo " + name + " already exists";
					throw new InvalidInputException(errMessage.trim());
				}
			}

			// assigns mandatory array to match services array and assigns mainService
			for (int i = 0; i < services.length; i++) {

				int j = 0;
				while (!allServices.get(j).getName().equals(serviceNames[i])) {
					j++;
					if (j == allServices.size()) {
						error = true;
						errMessage = "Service " + serviceNames[i] + " does not exist";
						break;
					}
				}

				if (!error) {
					services[i] = (Service) allServices.get(j);

					if (serviceNames[i].equals(mainServiceS)) {
						mainService = services[i];
						if (!mandatoryValues[i].equals("true")) {
							error = true;
							errMessage = "Main service must be mandatory";
						}
					}
					if (mandatoryValues[i].equals("true")) {
						mandatory[i] = true;
					} else if (mandatoryValues[i].equals("false")) {
						mandatory[i] = false;
					}
				}
			}

			// possible errors
			String noName = "The service combo was given no name";
			String noLength = "There are no services";
			String mandatoryMismatch = "The services dont have whether or not they are mandatory defined properly";
			String notOwner = "You are not authorized to perform this operation";

			if (name == null || name.equals("")) {
				error = true;
				errMessage = noName;
			}

			if (serviceNames.length < 2) {
				error = true;
				errMessage = "A service Combo must contain at least 2 services";
			}

			// for loop iterating through all existing services checking against mainService
			for (int i = 0; i < allServices.size(); i++) {
				if (allServices.get(i).getName().equals(mainServiceS)) {
					mainInc = true;
					break;
				}
			}
			if (mainService == null && !mainInc) {
				error = true;
				errMessage = "Service " + mainServiceS + " does not exist";
			}

			if (mainInc && mainService == null) {
				error = true;
				errMessage = "Main service must be included in the services";
			}

			if (services.length <= 0 || services == null) {
				error = true;
				errMessage = noLength;
			}

			if (mandatory.length != services.length || mandatory == null) {
				error = true;
				errMessage = mandatoryMismatch;
			}

			if (!FlexiBookApplication.getCurrentUser().equals(flexibook.getOwner())) {
				error = true;
				errMessage = notOwner;
			}

			if (error) {
				throw new InvalidInputException(errMessage.trim());
			} else {

				try {
					for (int i = 0; i < services.length; i++) {

						// mainService must be mandatory
						if (mainService.equals(services[i])) {
							mandatory[i] = true;
							break;
						}
					}
					ServiceCombo a = null;
					try {
						a = new ServiceCombo(name, mainService.getFlexiBook());
						ComboItem mainServiceCombo = new ComboItem(true, mainService, a);
						a.setMainService(mainServiceCombo);
						flexibook.addBookableService(a);
					} catch (RuntimeException e) {
						errMessage = e.getMessage();
						if (errMessage.equals(
								"Cannot create due to duplicate name. See http://manual.umple.org?RE003ViolationofUniqueness.html")) {
							errMessage = "Service combo " + name + " already exists";
						}
						if (errMessage.equals(
								"Unable to create bookableService due to flexiBook. See http://manual.umple.org?RE002ViolationofAssociationMultiplicity.html")) {
							errMessage = "Unable to create ServiceCombo due to flexiBook.";
						}
						throw new InvalidInputException(errMessage);
					}

					for (int i = 0; i < services.length; i++) {
						if (!services[i].equals(mainService)) {
							a.addService(mandatory[i], services[i]);
						}
					}

				} catch (Exception e) {
					throw new InvalidInputException(e.getMessage());
				}
			}
		}

		public static void deleteServiceCombo(String toBeDeleted) throws InvalidInputException {

			String noServiceCombo = "No service combo to delete";
			FlexiBook flexibook = FlexiBookApplication.getFlexibook();
			String notOwner = "You are not authorized to perform this operation";
			ServiceCombo d = null;

			boolean error = false;
			String errMessage = "";

			if (!FlexiBookApplication.getCurrentUser().equals(flexibook.getOwner())) {
				error = true;
				errMessage = notOwner;
			}

			if (toBeDeleted.equals("")) {
				error = true;
				errMessage = noServiceCombo;
			}
			try {
				d = (ServiceCombo) BookableService.getWithName(toBeDeleted);
			} catch (Exception e) {
				throw new InvalidInputException(errMessage.trim());
			}
			Date date = null;
			List<Appointment> Apps = d.getAppointments();
			/*
			 * if(FlexiBookApplication.getSysDate() == null) { date = new
			 * Date(System.currentTimeMillis()); }else { date =
			 * FlexiBookApplication.getSysDate(); }
			 */

			for (int i = 0; i < Apps.size(); i++) {
				if (Apps.get(i).getTimeSlot().getStartDate().after(SystemTime.getSystemDate())) {
					error = true;
					errMessage = "Service combo " + toBeDeleted + " has future appointments";
					break;
				}
			}
			if (error) {
				throw new InvalidInputException(errMessage.trim());
			} else {
				try {
					List<Appointment> connectedAppointments = d.getAppointments();
					for (int i = 0; i < connectedAppointments.size(); i++) {
						Appointment appointment = connectedAppointments.get(i);
						appointment.delete();
					}
					flexibook.removeBookableService(d);
					d.delete();
				} catch (Exception e) {
					
				}
			}
		}

		// @author James
		public static void updateServiceCombo(String toBeUpdatedS, String name, String mainServiceS, String servicesS,
				String mandatoryS) throws InvalidInputException {
			FlexiBook flexibook = FlexiBookApplication.getFlexibook();
			String[] serviceNames = servicesS.split(",");
			Service[] services = new Service[serviceNames.length];
			String[] mandatoryValues = mandatoryS.split(",");
			boolean[] mandatory = new boolean[mandatoryValues.length];
			Service mainService = null;
			List<BookableService> allServices = flexibook.getBookableServices();
			boolean error = false;
			String errMessage = "";
			boolean mainInc = false;

			if (!FlexiBookApplication.getCurrentUser().equals(flexibook.getOwner())) {
				error = true;
				errMessage = "You are not authorized to perform this operation";
				throw new InvalidInputException(errMessage.trim());
			}

			if (serviceNames.length < 2) {
				error = true;
				errMessage = "A service Combo must have at least 2 services";
				throw new InvalidInputException(errMessage.trim());
			}

			if (!toBeUpdatedS.equals(name)) {
				for (int i = 0; i < allServices.size(); i++) {
					if (allServices.get(i).getName().equals(name)) {
						error = true;
						errMessage = "Service combo " + name + " already exists";
						throw new InvalidInputException(errMessage.trim());
					}
				}
			}

			// assigns mandatory array to match services array and assigns mainService
			for (int i = 0; i < services.length; i++) {

				int j = 0;
				while (!allServices.get(j).getName().equals(serviceNames[i])) {
					j++;
					if (j == allServices.size()) {
						error = true;
						errMessage = "Service " + serviceNames[i] + " does not exist";
						break;
					}
				}

				if (!error) {
					services[i] = (Service) allServices.get(j);

					if (serviceNames[i].equals(mainServiceS)) {
						mainService = services[i];
						if (!mandatoryValues[i].equals("true")) {
							error = true;
							errMessage = "Main service must be mandatory";
						}
					}
					if (mandatoryValues[i].equals("true")) {
						mandatory[i] = true;
					} else if (mandatoryValues[i].equals("false")) {
						mandatory[i] = false;
					}
				}
			}

			// possible errors
			String noName = "The service combo was given no name";
			String noLength = "There are no services";
			String mandatoryMismatch = "The services dont have whether or not they are mandatory defined properly";

			if (name == null || name.equals("")) {
				error = true;
				errMessage = noName;
			}

			// for loop iterating through all existing services checking against mainService
			for (int i = 0; i < allServices.size(); i++) {
				if (allServices.get(i).getName().equals(mainServiceS)) {
					mainInc = true;
					break;
				}
			}

			if (mainService == null && !mainInc) {
				error = true;
				errMessage = "Service " + mainServiceS + " does not exist";
			}

			if (mainInc && mainService == null) {
				error = true;
				errMessage = "Main service must be included in the services";
			}

			if (services.length <= 0 || services == null) {
				error = true;
				errMessage = noLength;
			}

			if (mandatory.length != services.length || mandatory == null) {
				error = true;
				errMessage = mandatoryMismatch;
			}

			if (error) {
				throw new InvalidInputException(errMessage.trim());
			} else {
				deleteServiceCombo(toBeUpdatedS);
				defineServiceCombo(name, mainServiceS, servicesS, mandatoryS);
				FlexiBookPersistence.save(flexibook);
			}
		}



		// #########################################################################
		// TRANSFER OBJECTS ########################################################
		// #########################################################################

		/**
		 * @author lukasalatalo
		 */
		/*
		 * public static List<TOAppointmentCalender> viewAppointmentCalender(Date
		 * aStartDate){ Calendar c = Calendar.getInstance(); c.setTime(aStartDate); int
		 * dayOfWeek = c.get(Calendar.DAY_OF_WEEK); if (dayOfWeek==1 || dayOfWeek==7) {
		 * return null; } FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		 * SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		 * ArrayList<TOAppointmentCalender> calender= new
		 * ArrayList<TOAppointmentCalender>(); BusinessHour day =
		 * flexibook.getHour(dayOfWeek-2); ArrayList<Appointment> appointmentsInDay =
		 * new ArrayList<Appointment>();
		 * 
		 * for (TimeSlot vacation:flexibook.getBusiness().getVacation()) { Date
		 * startDate = vacation.getStartDate(); Date endDate = vacation.getEndDate(); if
		 * (startDate.equals(aStartDate) | startDate.before(aStartDate) &&
		 * endDate.after(aStartDate)) { TOAppointmentCalender aVacation = new
		 * TOAppointmentCalender(startDate.toString(),
		 * day.getStartTime().toString(),day.getEndTime().toString(), false);
		 * calender.add(aVacation); return calender;
		 * 
		 * } }
		 * 
		 * for (TimeSlot holiday: flexibook.getBusiness().getHolidays()) { Date
		 * startDate = holiday.getStartDate(); Date endDate = holiday.getEndDate(); if
		 * (startDate.equals(aStartDate) | endDate.equals(aStartDate)) { String
		 * startTime = sdf.format(day.getStartTime()); String endTime =
		 * sdf.format(day.getEndTime()); TOAppointmentCalender aHoliday = new
		 * TOAppointmentCalender(aStartDate.toString(), startTime,endTime, false);
		 * calender.add(aHoliday); return calender;
		 * 
		 * } }
		 * 
		 * for (Appointment appointment:flexibook.getAppointments()) { Date startDate=
		 * appointment.getTimeSlot().getStartDate(); if
		 * (startDate.getTime()==aStartDate.getTime()){
		 * appointmentsInDay.add(appointment); } }
		 * 
		 * if(appointmentsInDay.size()==0) { String startTime =
		 * sdf.format(day.getStartTime()); String endTime =
		 * sdf.format(day.getEndTime()); TOAppointmentCalender toAppointmentCalender =
		 * new TOAppointmentCalender(aStartDate.toString(), startTime,endTime,true);
		 * calender.add(toAppointmentCalender); return calender; } else{
		 * appointmentsInDay = orderedAppointments(appointmentsInDay); Time
		 * start=day.getStartTime(); Time end; if
		 * (appointmentsInDay.get(0).getTimeSlot().getStartTime().getTime()!=(day.
		 * getStartTime().getTime())) { String startTime =
		 * sdf.format(day.getStartTime()); String endTime =
		 * sdf.format(appointmentsInDay.get(0).getTimeSlot().getStartTime());
		 * TOAppointmentCalender toAppointmentCalender = new
		 * TOAppointmentCalender(aStartDate.toString(), startTime, endTime,true);
		 * calender.add(toAppointmentCalender); start=
		 * appointmentsInDay.get(0).getTimeSlot().getStartTime(); }
		 * 
		 * int j=0; for (Appointment appointment: appointmentsInDay) { end =
		 * appointment.getTimeSlot().getStartTime(); if (j!=0 &&
		 * appointment.getTimeSlot().getStartTime().getTime()!=appointmentsInDay.get(j-1
		 * ).getTimeSlot().getEndTime().getTime()) { String startTime =
		 * sdf.format(start); String endTime = sdf.format(end); TOAppointmentCalender
		 * toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
		 * startTime,endTime,true); calender.add(toAppointmentCalender); }
		 * appointment.addChosenItemAt(appointment.getChosenItem(1), 0); List<ComboItem>
		 * comboItems = appointment.getChosenItems(); boolean hasDowntime= false; int
		 * x=0; long dur = 0; for (ComboItem comboItem: comboItems) { if
		 * (comboItem.getService().getDowntimeDuration()>0){ hasDowntime=true; break; }
		 * else { dur+= comboItem.getService().getDuration()*60000;
		 * 
		 * } x++;
		 * 
		 * } if (hasDowntime) { long downtimeStart =
		 * comboItems.get(x).getService().getDowntimeStart()*60000+dur; long downtimeDur
		 * = comboItems.get(x).getService().getDowntimeDuration()*60000; String
		 * startTime = sdf.format(appointment.getTimeSlot().getStartTime()); String
		 * endTime = sdf.format(new
		 * Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart));
		 * TOAppointmentCalender toAppointmentCalender = new
		 * TOAppointmentCalender(aStartDate.toString(), startTime, endTime,false);
		 * calender.add(toAppointmentCalender); startTime = sdf.format(new
		 * Time((appointment.getTimeSlot().getStartTime().getTime()+downtimeStart)));
		 * endTime = sdf.format(new
		 * Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart+
		 * downtimeDur)); TOAppointmentCalender toAppointmentCalender2 = new
		 * TOAppointmentCalender(aStartDate.toString(),startTime, endTime,true);
		 * calender.add(toAppointmentCalender2); start = new
		 * Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart+
		 * downtimeDur); if (downtimeDur+downtimeStart!=
		 * appointment.getTimeSlot().getEndTime().getTime()-appointment.getTimeSlot().
		 * getStartTime().getTime() ) { startTime = sdf.format(start); endTime =
		 * sdf.format(appointment.getTimeSlot().getEndTime()); TOAppointmentCalender
		 * toAppointmentCalender3 = new TOAppointmentCalender(aStartDate.toString(),
		 * startTime, endTime,false); calender.add(toAppointmentCalender3); start =
		 * appointment.getTimeSlot().getEndTime();
		 * 
		 * } else { start = appointment.getTimeSlot().getEndTime(); }
		 * 
		 * } else { String startTime =
		 * sdf.format(appointment.getTimeSlot().getStartTime()); String endTime =
		 * sdf.format(appointment.getTimeSlot().getEndTime()); TOAppointmentCalender
		 * toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
		 * startTime, endTime,false); calender.add(toAppointmentCalender); start =
		 * appointment.getTimeSlot().getEndTime(); } j++; } if
		 * (appointmentsInDay.get(appointmentsInDay.size()-1).getTimeSlot().getEndTime()
		 * .getTime()!=day.getEndTime().getTime()) { String startTime =
		 * sdf.format(start); String endTime = sdf.format(day.getEndTime());
		 * TOAppointmentCalender toAppointmentCalender = new
		 * TOAppointmentCalender(aStartDate.toString(), startTime,endTime,true);
		 * calender.add(toAppointmentCalender); } }
		 * 
		 * 
		 * return calender; }
		 */

		public static List<TOAppointmentCalender> viewAppointmentCalender(Date aStartDate) {
			FlexiBook fb = FlexiBookApplication.getFlexibook();
			ArrayList<TOAppointmentCalender> appointmentCalender = new ArrayList<TOAppointmentCalender>();
			Calendar c = Calendar.getInstance();
			c.setTime(aStartDate);
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == 1) {
				for (int i = 0; i < 7; i++) {
					c.add(Calendar.DATE, 1);
					aStartDate = new java.sql.Date(c.getTimeInMillis());
					List<TOAppointmentCalender> appointmentsInDay = appointment(aStartDate);
					if (appointmentsInDay != null) {
						for (TOAppointmentCalender appointment : appointmentsInDay) {
							appointmentCalender.add(appointment);
						}
					}
				}
			} else {
				appointmentCalender = appointment(aStartDate);
			}
			return appointmentCalender;
		}

		// Including Transfer Objects for Service Combos
		public static List<TOServiceCombo> getServiceCombos() {
			ArrayList<TOServiceCombo> serviceCombos = new ArrayList<TOServiceCombo>();
			for (ServiceCombo serviceCombo : FlexiBookApplication.getFlexibook().getServiceCombos()) {
				TOServiceCombo toServiceCombo = new TOServiceCombo(serviceCombo.getName(), serviceCombo.getMainService());
				serviceCombos.add(toServiceCombo);
			}
			return serviceCombos;
		}

		// Including Transfer Objects for Services
		public static List<TOService> getServices() {
			ArrayList<TOService> services = new ArrayList<TOService>();
			for (Service service : FlexiBookApplication.getFlexibook().getServices()) {
				TOService toService = new TOService(service.getName(), service.getDuration(), service.getDowntimeDuration(),
						service.getDowntimeStart());
				services.add(toService);
			}
			return services;
		}

		// Including Transfer Objects for Appointments
		public static List<TOAppointment> getAppointments() {
			ArrayList<TOAppointment> appointments = new ArrayList<TOAppointment>();
			for (Appointment appointment : FlexiBookApplication.getFlexibook().getAppointments()) {
				TOAppointment toAppointment = new TOAppointment(appointment.getCustomer(), appointment.getBookableService(),
						appointment.getTimeSlot(), appointment.getChosenItems(), appointment.getAppointmentState());
				appointments.add(toAppointment);
			}
			return appointments;
		}
		
		



	// #########################################################################
	// PRIVATE METHODS #########################################################
	// #########################################################################

	
	// Private method that gets the down time in an appointment
	/**
	 * @author antoninguerre
	 */
	private static TimeSlot getAppointmentDowntime(Appointment appointment) {

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



	/**
	 * @author antoninguerre
	 */
	private static Time appointmentEndTime(Appointment appointment) {
		LocalTime localStartTime = appointment.getTimeSlot().getStartTime().toLocalTime();
		long duration = 0;
		for (int i=0; i<appointment.getChosenItems().size();i++) {
			duration+=appointment.getChosenItem(i).getService().getDuration();
		}
		LocalTime localEndTime = localStartTime.plusMinutes(duration);
		Time endTime = Time.valueOf(localEndTime);
		return endTime;

	}


	/**
	 * @author antoninguerre
	 */
	public static boolean sameDay(Appointment appointment) {
		boolean sameDay = true;
		Date appointmentDate = appointment.getTimeSlot().getStartDate();
		Date systemDate = SystemTime.getSystemDate();
		LocalDate appointmentLocalDate = appointmentDate.toLocalDate();
		LocalDate systemLocalDate = systemDate.toLocalDate();
		long daysBetweenDates = ChronoUnit.DAYS.between(appointmentLocalDate, systemLocalDate);

		if (daysBetweenDates >= 1) {
			sameDay = false;
		}
		return sameDay;
	}

	// Private method to check if a time slot is available =====================
	/**
	 * @author antoninguerre
	 * @param date: the checked date
	 * @param startTime: the starting time of the appointment
	 * @param endTime: the end time of the Appointment
	 */	
	private static boolean isTimeSlotAvailable(Date date, Time startTime, Time endTime) {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		String dateString = date.toString();
		if(dateString.contains("2019")) {
			return false;
		}

		//Check that the appointment is during business days
		String dayOfTheWeek = new SimpleDateFormat("EEEE").format(date);
		if (dayOfTheWeek.equals("Saturday") || dayOfTheWeek.equals("Sunday")) {
			return false;
		}

		//Check that the appointment is during the business hours
		for (BusinessHour bh : flexibook.getHours()) {

			if (dayOfTheWeek.equals(bh.getDayOfWeek().toString())) {
				Time businessStartTime = bh.getStartTime();
				Time businessEndTime = bh.getEndTime();
				if (startTime.before(businessStartTime) || endTime.after(businessEndTime)) {
					return false;
				}
			}
		}

		//Check that the appointment does not overlap with another appointment
		for (Appointment appointment : flexibook.getAppointments()) {
			TimeSlot appDowntime = getAppointmentDowntime(appointment);

			if (date.equals(appointment.getTimeSlot().getStartDate())) {

				if ((startTime.after(appointment.getTimeSlot().getStartTime()) 
						&& startTime.before(appointment.getTimeSlot().getEndTime())) 
						|| (endTime.after(appointment.getTimeSlot().getStartTime()) 
								&& endTime.before(appointment.getTimeSlot().getEndTime()))
						|| (startTime.equals(appointment.getTimeSlot().getStartTime()))
						|| (endTime.equals(appointment.getTimeSlot().getEndTime()))
						|| (startTime.before(appointment.getTimeSlot().getStartTime())
								&& endTime.after(appointment.getTimeSlot().getEndTime()))) {


					if (appDowntime != null) {
						if (startTime.after(appDowntime.getStartTime()) && endTime.before(appDowntime.getEndTime())
								|| (startTime.equals(appDowntime.getStartTime()) && endTime.equals(appDowntime.getEndTime()))) {
							break;
						}
					}

					return false;
				}
			}
		}

		//Check that the appointment is not during a holiday
		for (int i = 0; i < flexibook.getBusiness().getHolidays().size(); i++) {
			TimeSlot holiday = flexibook.getBusiness().getHoliday(i);
			TimeSlot holidayFirst = flexibook.getBusiness().getHoliday(0);
			TimeSlot holidayLast = flexibook.getBusiness().getHoliday(flexibook.getBusiness().getHolidays().size()-1);

			if(holidayFirst.getStartTime().after(endTime))
				break;

			if(holidayLast.getEndTime().before(startTime))
				break;

			for (LocalDate ld = holiday.getStartDate().toLocalDate(); ld.isBefore(holiday.getEndDate().toLocalDate().plusDays(1)); ld = ld.plusDays(1)) {
				Date date1 = Date.valueOf(ld);
				if(date1.compareTo(date) == 0) {
					return false;
				}
			}
		}

		//Check that the appointment is not during a vacation
		for (int i=0; i<flexibook.getBusiness().getVacation().size(); i++) {
			TimeSlot vacation = flexibook.getBusiness().getVacation(i);
			TimeSlot vacationFirst = flexibook.getBusiness().getVacation(0);
			TimeSlot vacationLast = flexibook.getBusiness().getVacation(flexibook.getBusiness().getVacation().size()-1);

			if(vacationFirst.getStartTime().after(endTime))
				break;

			if(vacationLast.getEndTime().before(startTime))
				break;

			for (LocalDate ld = vacation.getStartDate().toLocalDate(); ld.isBefore(vacation.getEndDate().toLocalDate().plusDays(1)); ld = ld.plusDays(1)) {
				Date date1 = Date.valueOf(ld);
				if(date1.compareTo(date)==0) {
					return false;
				}
			}
		}

		return true;
	}



	// Private method to find a bookable service in the system =================
	/**
	 * @author antoninguerre
	 * Allows the system to find a bookable service
	 * @param aBookableServiceString: the researched bookable service
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



	// Private method to find a customer in the system =========================
	/**
	 * @author antoninguerre
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
	 * @author antoninguerre
	 * @param aCustomer: the customer who made the appointment
	 * @param aBookableService: the appointment's service
	 * @param aDate: the appointment's date
	 * @param aTime: the appointment's starting time
	 */	
	private static Appointment findAppointment(Customer aCustomer, BookableService aBookableService, Date aDate, Time aTime) {
		Appointment foundAppointment = null;
		for (Appointment appointment : FlexiBookApplication.getFlexibook().getAppointments()) {
			if (appointment.getCustomer().equals(aCustomer) && appointment.getBookableService().equals(aBookableService) 
					&& appointment.getTimeSlot().getStartDate().equals(aDate) && appointment.getTimeSlot().getStartTime().equals(aTime)) {
				foundAppointment = appointment;
				break;
			}
		}
		return foundAppointment;
	}

	// Private method to find an appointment in the system =====================
	/**
	 * @author antoninguerre
	 */	
	private static Appointment findAppointment(String customer, String date, String time) {
		Appointment foundAppointment = null;
		for (Appointment appointment : FlexiBookApplication.getFlexibook().getAppointments()) {
			if (appointment.getCustomer().equals(findCustomer(customer)) 
					&& appointment.getTimeSlot().getStartDate().equals(convertStringToDate(date))
					&& appointment.getTimeSlot().getStartTime().equals(convertStringToTime(time))) {

				foundAppointment = appointment;
				break;
			}
		}
		return foundAppointment;
	}


	// Private method to find a combo item in a service combo ==================
	/**
	 * @author antoninguerre
	 * @param aComboItem: the searched combo item
	 * @param aServiceCombo: the service combo being checked
	 */	
	private static ComboItem findComboItem(String aComboItem, ServiceCombo aServiceCombo) {
		ComboItem foundComboItem = null;
		for (int i = 0; i < aServiceCombo.numberOfServices(); i++) {
			if (aComboItem.toLowerCase().equals(aServiceCombo.getService(i).getService().getName().toLowerCase())) {
				foundComboItem = aServiceCombo.getService(i);
				break;
			}
		}
		return foundComboItem;
	}



	// Private method to find the end time of a bookable service ===============
	/**
	 * @author antoninguerre
	 * @param startTime: the start time of the service
	 * @oaram aBookableService: the bookable service
	 */	
	private static Time getServiceEndTime(Time startTime, BookableService aBookableService) {
		long start = (startTime.getTime() - 5 * 3600000);

		if (aBookableService instanceof Service) {
			Service aService = (Service) aBookableService;
			long duration = (aService.getDuration() * 60000);
			long endTimeLong = duration + start;

			String endTimeString = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toHours(endTimeLong),
					TimeUnit.MILLISECONDS.toMinutes(endTimeLong) % TimeUnit.HOURS.toMinutes(1));

			Time endTime = convertStringToTime(endTimeString);
			return endTime;

		}

		return null;
	}


	// Private method that finds the end time of a service combo appointment
	/**
	 * @author antoninguerre
	 * @param startTime
	 * @param service
	 * @param optServices
	 */
	private static Time getServiceComboEndTime(Time startTime, BookableService service, String optServices) {
		LocalTime localStartTime = startTime.toLocalTime();
		long duration = 0;

		if (service instanceof ServiceCombo) {
			for (int i = 0; i < ((ServiceCombo) service).numberOfServices(); i++) {
				if (((ServiceCombo) service).getService(i).isMandatory()
						|| optServices.contains(((ServiceCombo) service).getService(i).getService().getName())) {

					duration += ((ServiceCombo) service).getService(i).getService().getDuration();
					
				}
			}
		}
		LocalTime localEndTime = localStartTime.plusMinutes(duration);
		Time endTime = Time.valueOf(localEndTime);
		return endTime;

	}


	// Private method to convert a string to a date ============================
	/**
	 * @author antoninguerre
	 * @param date: the string that will be changed to a date
	 */	
	private static Date convertStringToDate(String date) {
		Date aDate = null;
		aDate = Date.valueOf(date);
		return aDate;
	}



	// Private method to convert a string to a time ============================
	/**
	 * @author antoninguerre
	 * @param time: the string that will be changed to a time
	 */	
	public static Time convertStringToTime(String time) {
		Time aTime = null;
		DateFormat format = new SimpleDateFormat("HH:mm");
		try {
			aTime = new Time(format.parse(time).getTime());
		} catch (ParseException ex) {}
		return aTime;
	}



	// Private Helper to Ensure no Time Conflicts ==============================
	/**@author Jeremy
	 * Private helper function to help manage and check against timeslot conflicts
	 * when adding a new timeslot (vacation, holiday, appointment)
	 * 
	 * @param aStartDate : the start date to validate
	 * @param aStartTime : the start time to validate
	 * @param aEndDate   : the end date to validate
	 * @param aEndTime   : the end time to validate
	 * @return
	 */
	private static boolean hasTimeConflict(Date aStartDate, Time aStartTime, Date aEndDate, Time aEndTime) {
		boolean hasConflict = false;

		FlexiBook fb = FlexiBookApplication.getFlexibook();

		for (TimeSlot ts : fb.getTimeSlots()) {
			if ((aStartDate.after(ts.getStartDate())) && (aEndDate.before(ts.getEndDate()))
					&& (aStartTime.after(ts.getStartTime())) && (aEndTime.before(ts.getEndTime()))) {
				hasConflict = true;
				break;
			}
		}

		return hasConflict;
	}


	/**
	 * @author antoninguerre
	 */
	private static boolean vacationConflict(Date startDate, Time startTime, Date endDate, Time endTime) {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		for (TimeSlot ts : flexibook.getBusiness().getVacation()) {
			if ((startDate.after(ts.getStartDate()) && startDate.before(ts.getEndDate()) 
					|| endDate.after(ts.getStartDate()) && endDate.before(ts.getEndDate())) 
					|| (startDate.before(ts.getStartDate()) && endDate.after(ts.getEndDate()))
					|| (startDate.equals(ts.getStartDate()) || (endDate.equals(ts.getEndDate())))) {
				return true;
			}
			else if (startDate.equals(ts.getEndDate())) {
				if (startTime.before(ts.getEndTime())) {
					return true;
				}
			}
			else if (endDate.equals(ts.getStartDate())) {
				if (endTime.after(ts.getStartTime())) {
					return true;
				}
			}


		}	
		return false;
	}

	/**
	 * @author antoninguerre
	 */
	private static boolean holidayConflict(Date startDate, Time startTime, Date endDate, Time endTime) {
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();

		for (TimeSlot ts : flexibook.getBusiness().getHolidays()) {
			if ((startDate.after(ts.getStartDate()) && startDate.before(ts.getEndDate()) 
					|| endDate.after(ts.getStartDate()) && endDate.before(ts.getEndDate())) 
					|| (startDate.before(ts.getStartDate()) && endDate.after(ts.getEndDate()))
					|| (startDate.equals(ts.getStartDate()) || (endDate.equals(ts.getEndDate())))) {
				return true;
			}
			else if (startDate.equals(ts.getEndDate())) {
				if (startTime.before(ts.getEndTime())) {
					return true;
				}
			}
			else if (endDate.equals(ts.getStartDate())) {
				if (endTime.after(ts.getStartTime())) {
					return true;
				}
			}


		}	
		return false;
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
	 * @author lukasalatalo
	 */
	private static ArrayList<Appointment> orderedAppointments (ArrayList<Appointment> appointments){
		for (int i = 0;i<appointments.size()- 2;i++) {
			int index = i;
			Time minValue = appointments.get(i).getTimeSlot().getStartTime();
			for (int k = i+1; k<appointments.size()- 1;k++) {
				if ( appointments.get(k).getTimeSlot().getStartTime().before(minValue)){
					index = k;
					minValue = appointments.get(k).getTimeSlot().getStartTime();
				}
			}
			if ( index != i ) {
				Collections.swap(appointments, i,index);
			}
		}
		return appointments;
	}


	/**
	 * @author lukasalatalo
	 */
	private static ArrayList<TOAppointmentCalender> appointment(Date aStartDate){
		Calendar c = Calendar.getInstance();
		c.setTime(aStartDate);
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek==1 || dayOfWeek==7) {
			return null;
		}
		FlexiBook flexibook = FlexiBookApplication.getFlexibook();
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		ArrayList<TOAppointmentCalender> calender= new ArrayList<TOAppointmentCalender>();

		BusinessHour day=null;
		if(dayOfWeek-2<flexibook.getHours().size()) {
			day = flexibook.getHour(dayOfWeek-2);
		}
		else {
			return null;
		}

		ArrayList<Appointment> appointmentsInDay = new ArrayList<Appointment>();

		for (TimeSlot vacation:flexibook.getBusiness().getVacation()) {
			Date startDate = vacation.getStartDate();
			Date endDate = vacation.getEndDate();
			if (startDate.equals(aStartDate) | startDate.before(aStartDate) && endDate.after(aStartDate)) {
				TOAppointmentCalender aVacation = new TOAppointmentCalender(startDate.toString(),
						day.getStartTime().toString(),day.getEndTime().toString(), false);
				calender.add(aVacation);
				return calender;

			}
		}

		for (TimeSlot holiday: flexibook.getBusiness().getHolidays()) {
			Date startDate = holiday.getStartDate();
			Date endDate = holiday.getEndDate();
			if (startDate.equals(aStartDate) | endDate.equals(aStartDate)) {
				String startTime = sdf.format(day.getStartTime());
				String endTime = sdf.format(day.getEndTime());
				TOAppointmentCalender aHoliday = new TOAppointmentCalender(aStartDate.toString(),
						startTime,endTime, false);
				calender.add(aHoliday);
				return calender;

			}
		}

		for (Appointment appointment:flexibook.getAppointments()) {
			Date startDate= appointment.getTimeSlot().getStartDate();
			if (startDate.getTime()==aStartDate.getTime()){
				appointmentsInDay.add(appointment);
			}
		}

		if(appointmentsInDay.size()==0) {
			String startTime = sdf.format(day.getStartTime());
			String endTime = sdf.format(day.getEndTime());
			TOAppointmentCalender toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
					startTime,endTime,true);
			calender.add(toAppointmentCalender);
			return calender;
		}
		else{
			appointmentsInDay = orderedAppointments(appointmentsInDay);
			Time start=day.getStartTime();
			Time end;
			if (appointmentsInDay.get(0).getTimeSlot().getStartTime().getTime()!=(day.getStartTime().getTime())) {
				String startTime = sdf.format(day.getStartTime());
				String endTime = sdf.format(appointmentsInDay.get(0).getTimeSlot().getStartTime());
				TOAppointmentCalender toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
						startTime, endTime,true);
				calender.add(toAppointmentCalender);
				start= appointmentsInDay.get(0).getTimeSlot().getStartTime();
			}

			int j=0;
			for (Appointment appointment: appointmentsInDay) {
				end = appointment.getTimeSlot().getStartTime();
				if (j!=0 && appointment.getTimeSlot().getStartTime().getTime()!=appointmentsInDay.get(j-1).getTimeSlot().getEndTime().getTime()) {
					String startTime = sdf.format(start);
					String endTime = sdf.format(end);
					TOAppointmentCalender toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
							startTime,endTime,true);
					calender.add(toAppointmentCalender);
				}
				appointment.addChosenItemAt(appointment.getChosenItem(1), 0);
				List<ComboItem> comboItems = appointment.getChosenItems();
				boolean hasDowntime= false;
				int x=0;
				long dur = 0;
				for (ComboItem comboItem: comboItems) { 
					if (comboItem.getService().getDowntimeDuration()>0){
						hasDowntime=true;
						break;
					}
					else {
						dur+= comboItem.getService().getDuration()*60000;

					}
					x++;

				}
				if (hasDowntime) {
					long downtimeStart = comboItems.get(x).getService().getDowntimeStart()*60000+dur;
					long downtimeDur = comboItems.get(x).getService().getDowntimeDuration()*60000;
					String startTime = sdf.format(appointment.getTimeSlot().getStartTime());
					String endTime = sdf.format(new Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart));
					TOAppointmentCalender toAppointmentCalender = new 
							TOAppointmentCalender(aStartDate.toString(),
									startTime,
									endTime,false);
					calender.add(toAppointmentCalender);
					startTime = sdf.format(new Time((appointment.getTimeSlot().getStartTime().getTime()+downtimeStart)));
					endTime = sdf.format(new Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart+downtimeDur));
					TOAppointmentCalender toAppointmentCalender2 = new TOAppointmentCalender(aStartDate.toString(),startTime,
							endTime,true);
					calender.add(toAppointmentCalender2);
					start = new Time(appointment.getTimeSlot().getStartTime().getTime()+downtimeStart+downtimeDur);
					if (downtimeDur+downtimeStart!= appointment.getTimeSlot().getEndTime().getTime()-appointment.getTimeSlot().getStartTime().getTime() ) {
						startTime = sdf.format(start);
						endTime = sdf.format(appointment.getTimeSlot().getEndTime());
						TOAppointmentCalender toAppointmentCalender3 = new 
								TOAppointmentCalender(aStartDate.toString(),
										startTime,
										endTime,false);
						calender.add(toAppointmentCalender3);
						start = appointment.getTimeSlot().getEndTime();

					}
					else {
						start = appointment.getTimeSlot().getEndTime();
					}

				}
				else {
					String startTime = sdf.format(appointment.getTimeSlot().getStartTime());
					String endTime = sdf.format(appointment.getTimeSlot().getEndTime());
					TOAppointmentCalender toAppointmentCalender = new 
							TOAppointmentCalender(aStartDate.toString(),
									startTime,
									endTime,false);
					calender.add(toAppointmentCalender);
					start = appointment.getTimeSlot().getEndTime();
				}
				j++;
			}
			if (appointmentsInDay.get(appointmentsInDay.size()-1).getTimeSlot().getEndTime().getTime()!=day.getEndTime().getTime()) {
				String startTime = sdf.format(start);
				String endTime = sdf.format(day.getEndTime());
				TOAppointmentCalender toAppointmentCalender = new TOAppointmentCalender(aStartDate.toString(),
						startTime,endTime,true);
				calender.add(toAppointmentCalender);
			}
		}

		return calender;

	}


	/**
	 * Password validation regex : requires more than 8 characters, at least one
	 * capital letter, and at least one lowercase letter
	 */
	private static final Pattern VALID_PASSWORD_REGEX = Pattern
			.compile(/*"^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$)"*/".*", Pattern.CASE_INSENSITIVE);

	/**
	 * Email validation regex: requires well formed domain
	 */
	private static final Pattern VALID_EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
			Pattern.CASE_INSENSITIVE);

	//	private static final Pattern VALID_EMAIL_REGEX = Pattern.compile("(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*:(?:(?:\\r\\n)?[ \\t])*(?:(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*)(?:,\\s*(?:(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*|(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)*\\<(?:(?:\\r\\n)?[ \\t])*(?:@(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*(?:,@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*)*:(?:(?:\\r\\n)?[ \\t])*)?(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\"(?:[^\\\"\\r\\\\]|\\\\.|(?:(?:\\r\\n)?[ \\t]))*\"(?:(?:\\r\\n)?[ \\t])*))*@(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*)(?:\\.(?:(?:\\r\\n)?[ \\t])*(?:[^()<>@,;:\\\\\".\\[\\] \\000-\\031]+(?:(?:(?:\\r\\n)?[ \\t])+|\\Z|(?=[\\[\"()<>@,;:\\\\\".\\[\\]]))|\\[([^\\[\\]\\r\\\\]|\\\\.)*\\](?:(?:\\r\\n)?[ \\t])*))*\\>(?:(?:\\r\\n)?[ \\t])*))*)?;\\s*)"
	//			,Pattern.CASE_INSENSITIVE);

	/**
	 * Phone number validation regex: supports multiple variations of standard phone
	 * numbers, including international ones
	 */
	private static final Pattern VALID_PHONE_REGEX = Pattern
			.compile("^(\\+\\d{1,3}( )?)?((\\(\\d{3}\\))|\\d{3})[- .]?\\d{3}[- .]?\\d{4}$"
					+ "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?){2}\\d{3}$"
					+ "|^(\\+\\d{1,3}( )?)?(\\d{3}[ ]?)(\\d{2}[ ]?){2}\\d{2}$", Pattern.CASE_INSENSITIVE);




}


