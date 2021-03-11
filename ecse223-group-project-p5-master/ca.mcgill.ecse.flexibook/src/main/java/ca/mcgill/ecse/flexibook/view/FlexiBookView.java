package ca.mcgill.ecse.flexibook.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.sql.Date;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.WindowConstants;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.controller.TOAppointment;
import ca.mcgill.ecse.flexibook.model.Appointment.AppointmentState;
import ca.mcgill.ecse.flexibook.model.SystemTime;

public class FlexiBookView extends JFrame{
	//UI elements
	private JLabel errorMessage;

	//Start Appointment, End Appointment, Register No-show
	private JLabel chooseAppointmentLabel;
	private JLabel currentAppointmentLabel;
	private JButton startAppointmentButton;
	private JButton noShowAppointmentButton;
	private JButton finishAppointmentButton;
	private JComboBox<String> futureAppointmentToggleList;
	private JComboBox<String> currentAppointmentToggleList;
	private JButton back;

	// data elements
	private String error = null;
	private HashMap<Integer, TOAppointment> futureAppointments;
	private HashMap<Integer, TOAppointment> currentAppointments;


	// Create the appointment manager page
	/**
	 * @author jamesdarby
	 */
	public FlexiBookView() {
		initComponents();
		refreshData();
	}


	// Initialize the page's components
	/**
	 * @author jamesdarby
	 */
	private void initComponents(){

		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
		DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now);
		String time = timeFormat.format(now);
		SystemTime.setSystemDate(Date.valueOf(date));
		SystemTime.setSystemTime(FlexiBookController.convertStringToTime(time));
		
		setBounds(350,250,400,100);

		// elements for error message
		errorMessage = new JLabel();
		errorMessage.setForeground(Color.RED);

		//elements for Appointments
		chooseAppointmentLabel = new JLabel(); 
		chooseAppointmentLabel.setText("Choose Apointment");
		currentAppointmentLabel = new JLabel();
		currentAppointmentLabel.setText("Current Apointments");
		startAppointmentButton = new JButton();
		startAppointmentButton.setText("Start");
		noShowAppointmentButton = new JButton();
		noShowAppointmentButton.setText("No Show");
		finishAppointmentButton = new JButton();
		finishAppointmentButton.setText("Finish");
		futureAppointmentToggleList = new JComboBox<String>(new String[0]);
		currentAppointmentToggleList = new JComboBox<String>(new String[0]);
		back = new JButton("Back");

		// global settings
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("FlexiBook Application");

		//listeners
		startAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				startAppointmentButtonActionPerformed(evt);
			}
		});

		noShowAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				noShowButtonActionPerformed(evt);
			}
		});

		finishAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				finishAppointmentButtonActionPerformed(evt);
			}
		});

		back.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backButtonActionPerformed(evt);
			}
		});

		// horizontal line element
		JSeparator horizontalLine = new JSeparator();

		//layout
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createParallelGroup()
				.addComponent(errorMessage)
				.addComponent(horizontalLine)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup()
								.addComponent(chooseAppointmentLabel)
								.addComponent(currentAppointmentLabel))
						.addGroup(layout.createParallelGroup()
								.addComponent(futureAppointmentToggleList)
								.addComponent(currentAppointmentToggleList))
						.addGroup(layout.createParallelGroup()
								.addComponent(startAppointmentButton)
								.addComponent(finishAppointmentButton))
						.addGroup(layout.createParallelGroup()
								.addComponent(noShowAppointmentButton)
								.addComponent(back))
						)
				);

		layout.setVerticalGroup(
				layout.createSequentialGroup()
				.addComponent(errorMessage)
				.addGroup(layout.createParallelGroup()
						.addComponent(chooseAppointmentLabel)
						.addComponent(futureAppointmentToggleList)
						.addComponent(startAppointmentButton)
						.addComponent(noShowAppointmentButton))
				.addGroup(layout.createParallelGroup()
						.addComponent(horizontalLine))
				.addGroup(layout.createParallelGroup()
						.addComponent(currentAppointmentLabel)
						.addComponent(currentAppointmentToggleList)
						.addComponent(finishAppointmentButton)
						.addComponent(back))
				);
		pack();					
	}




	// Refresh the page's data
	/**
	 * @author jamesdarby
	 */
	private void refreshData() {

		// error
		//errorMessage.setText(error);

//		if (error == null || error.length() == 0) {

			// populate page with data

			//toggle future appointments
			futureAppointments = new HashMap<Integer, TOAppointment>();
			futureAppointmentToggleList.removeAllItems();
			Integer index = 0;
			for (TOAppointment appointment : FlexiBookController.getAppointments()) {
				//if(appointment is today and is not started) view should not have to handle this
				if(appointment.getAppointmentState().compareTo(AppointmentState.Booked)==0) {
					futureAppointments.put(index, appointment);
					futureAppointmentToggleList.addItem(appointment.getCustomer().getUsername() + " " + appointment.getTimeSlot().getStartDate() + " " + 
							appointment.getTimeSlot().getStartTime() + " "+ appointment.getBookableService().getName());
					index++;
				}
			};
			futureAppointmentToggleList.setSelectedIndex(-1);

			//toggle current appointments
			currentAppointments = new HashMap<Integer, TOAppointment>();
			currentAppointmentToggleList.removeAllItems();
			index = 0;
			for (TOAppointment appointment : FlexiBookController.getAppointments()) {
				//if(appointment is currently active )
				if(appointment.getAppointmentState().compareTo(AppointmentState.InProgress)==0) {
					currentAppointments.put(index, appointment);
					currentAppointmentToggleList.addItem(appointment.getCustomer().getUsername() + " " + appointment.getTimeSlot().getStartDate() + " " + 
							appointment.getTimeSlot().getStartTime() + " "+ appointment.getBookableService().getName());
					index++;
				}
			};
			currentAppointmentToggleList.setSelectedIndex(-1);
		//}
		pack();
	}


	// Button that starts an appointment by calling the controller
	/**
	 * @author jamesdarby
	 */
	private void startAppointmentButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		errorMessage.setText("");

		int selectedAppointment = futureAppointmentToggleList.getSelectedIndex();
		if (selectedAppointment < 0) {
			errorMessage.setText("An Appointment needs to be selected to start it!");
			error = "An Appointment needs to be selected to start it!";
			return;
		}
		
		if (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().after(SystemTime.getSystemDate()) 
				|| (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().equals(SystemTime.getSystemDate()) 
						&& futureAppointments.get(selectedAppointment).getTimeSlot().getStartTime().after(SystemTime.getSystemTime()))){
			errorMessage.setText("Cannot start the appointment yet");
			return;
		}
		
		if (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().equals(SystemTime.getSystemDate()) 
				&& futureAppointments.get(selectedAppointment).getTimeSlot().getEndTime().before(SystemTime.getSystemTime())){
			errorMessage.setText("Cannot start the appointment anymore");
			return;
		}

		// call the controller
		try {
			FlexiBookController.startAppointment(futureAppointments.get(selectedAppointment).getCustomer().getUsername(), futureAppointments.get(selectedAppointment).getStartDate(), 
					futureAppointments.get(selectedAppointment).getStartTime());
			errorMessage.setForeground(Color.GREEN);
			errorMessage.setText("Appointment started");
		} catch (InvalidInputException e) {
			errorMessage.setText(e.getMessage());
		}

		// update visuals
		refreshData();
	}


	// Button that marks the customer as a No Show
	/**
	 * @author jamesdarby
	 */
	private void noShowButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		error = "";

		int selectedAppointment = futureAppointmentToggleList.getSelectedIndex();
		if (selectedAppointment < 0) {
			error = "An appointment needs to be selected to register a customer no show";
		}

		if (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().after(SystemTime.getSystemDate()) 
				|| (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().equals(SystemTime.getSystemDate()) 
						&& futureAppointments.get(selectedAppointment).getTimeSlot().getStartTime().after(SystemTime.getSystemTime()))){
			errorMessage.setText("Cannot register no show yet");
			return;
		}
		
		if (futureAppointments.get(selectedAppointment).getTimeSlot().getStartDate().equals(SystemTime.getSystemDate()) 
				&& futureAppointments.get(selectedAppointment).getTimeSlot().getEndTime().before(SystemTime.getSystemTime())){
			errorMessage.setText("Cannot register no show anymore");
			return;
		}

		
		
		// call the controller
		if (error.length() == 0) {
			try {
				FlexiBookController.customerNoShow(futureAppointments.get(selectedAppointment).getCustomer().getUsername(),"Appointment", futureAppointments.get(selectedAppointment).getStartDate(),
						futureAppointments.get(selectedAppointment).getStartTime());
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}
		}

		// update visuals
		refreshData();
	}


	// Button that finshes an appointment by calling the controller
	/**
	 * @author jamesdarby
	 */
	private void finishAppointmentButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		error = "";

		int selectedAppointment = currentAppointmentToggleList.getSelectedIndex();
		if (selectedAppointment < 0) {
			error = "An appointment needs to be selected to finish it";
		}

		// call the controller
		if (error.length() == 0) {
			try {
				FlexiBookController.finishAppointment(currentAppointments.get(selectedAppointment).getCustomer().getUsername(), currentAppointments.get(selectedAppointment).getStartDate(), 
						currentAppointments.get(selectedAppointment).getStartTime());
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}
		}
		// update visuals
		refreshData();
	}


	// Button that goes back to the Owner Welcome Page
	/**
	 * @author antoninguerre
	 */
	private void backButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new OwnerWelcomePage().setVisible(true);

	}

}

