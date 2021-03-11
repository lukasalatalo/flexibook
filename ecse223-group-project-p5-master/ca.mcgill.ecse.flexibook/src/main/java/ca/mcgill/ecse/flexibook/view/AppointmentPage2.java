package ca.mcgill.ecse.flexibook.view;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.controller.TOAppointment;
import ca.mcgill.ecse.flexibook.controller.TOService;
import ca.mcgill.ecse.flexibook.model.Appointment;
import ca.mcgill.ecse.flexibook.model.Customer;
import ca.mcgill.ecse.flexibook.model.SystemTime;
import ca.mcgill.ecse.flexibook.model.User;

import javax.swing.JSeparator;
import java.awt.Color;
import java.awt.Component;
import javax.swing.border.BevelBorder;
import java.awt.Rectangle;

public class AppointmentPage2 extends JFrame{

	private static final long serialVersionUID = 634543L;

	private JPanel frame;
	private JTextField timeTextField;
	private JLabel greetings;
	private JButton settings;
	private JButton bookAppointmentPageButton;
	private JButton updateAppointmentPageButton;
	private JButton deleteAppointmentPageButton;
	private JLayeredPane layeredPane;
	private JPanel bookAppointmentPanel;
	private JLabel bookNewAppointmentLabel;
	private JLabel selectServiceLabel;
	private JLabel additionalServiceLabel;
	private JLabel selectDateLabel;
	private JLabel selectTimeLabel;
	private JComboBox serviceSelector;
	private JComboBox additionalServiceSelector;
	private JDatePickerImpl dateSelector;
	private JLabel errorMsgBook;
	private JButton bookAppointmentButton;
	private JButton resetSelectionsButton;
	private JButton viewAvailableTimesButton;
	private JPanel updateAppointmentPanel;
	private JLabel updateAppointmentLabel;
	private JLabel selectAppointmentUpdateLabel;
	private JComboBox updateAppointmentSelector;
	private JLabel errorMsgUpdate;
	private JButton updateDateTimeButton;
	private JButton updateServiceButton;
	private JButton updateAdditionalServiceButton;
	private JPanel deleteAppointmentPanel;
	private JLabel deleteAppointmentLabel;
	private JLabel selectAppointmentDeleteLabel;
	private JComboBox deleteAppointmentSelector;
	private JLabel errorMsgDelete;
	private JButton deleteAppointmentButton;
	private JLabel currentAppointmentLabel;
	private DefaultTableModel currentAppointmentDtm;
	private String currentAppointmentColumnNames[] = {"Service", "Additional services","Date","Start Time", "End Time"};
	private JScrollPane tableScrollPane;
	private static final int HEIGHT_CURRENT_APPOINTMENT_TABLE = 110;
	private JScrollPane currentAppointmentScrollPane;

	private HashMap<Integer, String> services;
	private JComboBox<String> updatedServiceBox;
	private HashMap<Integer, TOAppointment> appointments;
	private User user;
	private JTable appointmentTable;
	private DateTimeFormatter timeFormat;


	// Initialize the appointment page
	/**
	 * @author antoninguerre
	 * 
	 */
	public AppointmentPage2() {
		initialize();
		refreshData();
	}

	
	
	//Initialize the contents of the frame.
	/**
	 * @author antoninguerre
	 * 
	 */
	private void initialize() {

		user = FlexiBookApplication.getCurrentUser();

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(400, 200, 650, 391);
		frame = new JPanel();
		frame.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(frame);
		frame.setPreferredSize(new Dimension(800, 500));
		frame.setLayout(null);

		greetings = new JLabel("Hello "+user.getUsername()+" !");
		greetings.setBounds(6, 6, 139, 16);
		frame.add(greetings);

		settings = new JButton("...");
		settings.setBounds(527, 1, 117, 29);
		frame.add(settings);

		bookAppointmentPageButton = new JButton("Book Appointment");
		bookAppointmentPageButton.setBounds(28, 23, 149, 29);
		frame.add(bookAppointmentPageButton);

		updateAppointmentPageButton = new JButton("Update Appointment");
		updateAppointmentPageButton.setBounds(240, 23, 158, 29);
		frame.add(updateAppointmentPageButton);

		deleteAppointmentPageButton = new JButton("Delete Appointment");
		deleteAppointmentPageButton.setBounds(457, 23, 158, 29);
		frame.add(deleteAppointmentPageButton);

		layeredPane = new JLayeredPane();
		layeredPane.setBounds(6, 62, 638, 141);
		frame.add(layeredPane);
		layeredPane.setLayout(new CardLayout(0, 0));

		bookAppointmentPanel = new JPanel();
		layeredPane.add(bookAppointmentPanel, "name_874907700001419");
		bookAppointmentPanel.setLayout(null);

		bookNewAppointmentLabel = new JLabel("Book a new Appointment");
		bookNewAppointmentLabel.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		bookNewAppointmentLabel.setBounds(6, 6, 188, 16);
		bookAppointmentPanel.add(bookNewAppointmentLabel);

		selectServiceLabel = new JLabel("Select a Service");
		selectServiceLabel.setBounds(6, 34, 116, 16);
		bookAppointmentPanel.add(selectServiceLabel);

		additionalServiceLabel = new JLabel("Additional Services");
		additionalServiceLabel.setBounds(184, 34, 120, 16);
		bookAppointmentPanel.add(additionalServiceLabel);

		selectDateLabel = new JLabel("Select a Date");
		selectDateLabel.setBounds(394, 34, 87, 16);
		bookAppointmentPanel.add(selectDateLabel);

		selectTimeLabel = new JLabel("Select a Time");
		selectTimeLabel.setBounds(545, 34, 87, 16);
		bookAppointmentPanel.add(selectTimeLabel);

		serviceSelector = new JComboBox();
		serviceSelector.setBounds(6, 51, 126, 27);
		bookAppointmentPanel.add(serviceSelector);

		additionalServiceSelector = new JComboBox();
		additionalServiceSelector.setBounds(184, 51, 143, 27);
		bookAppointmentPanel.add(additionalServiceSelector);

		SqlDateModel model = new SqlDateModel();
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");  
		LocalDateTime now = LocalDateTime.now();
		String date = dtf.format(now);
		SystemTime.setSystemDate(Date.valueOf(date));
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model,p);
		dateSelector = new JDatePickerImpl(datePanel, new DateLabelFormatter());
		dateSelector.setBounds(370, 50, 150, 26);
		bookAppointmentPanel.add(dateSelector);

		timeTextField = new JTextField();
		timeTextField.setBounds(545, 50, 87, 26);
		bookAppointmentPanel.add(timeTextField);
		timeTextField.setColumns(10);

		errorMsgBook = new JLabel("");
		errorMsgBook.setBounds(6, 78, 626, 16);
		bookAppointmentPanel.add(errorMsgBook);

		bookAppointmentButton = new JButton("Book Appointment");
		bookAppointmentButton.setBounds(154, 106, 150, 29);
		bookAppointmentPanel.add(bookAppointmentButton);

		resetSelectionsButton = new JButton("Reset Selections");
		resetSelectionsButton.setBounds(333, 106, 143, 29);
		bookAppointmentPanel.add(resetSelectionsButton);

		viewAvailableTimesButton = new JButton("View Available Times");
		viewAvailableTimesButton.setBounds(456, 2, 176, 29);
		bookAppointmentPanel.add(viewAvailableTimesButton);

		updateAppointmentPanel = new JPanel();
		layeredPane.add(updateAppointmentPanel, "name_874918725568567");
		updateAppointmentPanel.setLayout(null);

		updateAppointmentLabel = new JLabel("Update Appointment");
		updateAppointmentLabel.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		updateAppointmentLabel.setBounds(6, 6, 159, 16);
		updateAppointmentPanel.add(updateAppointmentLabel);

		selectAppointmentUpdateLabel = new JLabel("Select an Appointment");
		selectAppointmentUpdateLabel.setBounds(6, 34, 142, 16);
		updateAppointmentPanel.add(selectAppointmentUpdateLabel);

		updateAppointmentSelector = new JComboBox();
		updateAppointmentSelector.setBounds(160, 30, 370, 27);
		updateAppointmentPanel.add(updateAppointmentSelector);

		errorMsgUpdate = new JLabel("");
		errorMsgUpdate.setBounds(6, 62, 626, 16);
		updateAppointmentPanel.add(errorMsgUpdate);

		updateDateTimeButton = new JButton("Update Date or Time");
		updateDateTimeButton.setBounds(98, 94, 159, 29);
		updateAppointmentPanel.add(updateDateTimeButton);

		updateServiceButton = new JButton("Update Service");
		updateServiceButton.setBounds(269, 94, 127, 29);
		updateAppointmentPanel.add(updateServiceButton);

		updateAdditionalServiceButton = new JButton("Update Additional Services");
		updateAdditionalServiceButton.setBounds(408, 94, 198, 29);
		updateAppointmentPanel.add(updateAdditionalServiceButton);

		deleteAppointmentPanel = new JPanel();
		layeredPane.add(deleteAppointmentPanel, "name_874928952009038");
		deleteAppointmentPanel.setLayout(null);

		deleteAppointmentLabel = new JLabel("Delete Appointment");
		deleteAppointmentLabel.setFont(new Font("Lucida Grande", Font.BOLD, 14));
		deleteAppointmentLabel.setBounds(6, 6, 145, 16);
		deleteAppointmentPanel.add(deleteAppointmentLabel);

		selectAppointmentDeleteLabel = new JLabel("Select an Appointment");
		selectAppointmentDeleteLabel.setBounds(6, 34, 145, 16);
		deleteAppointmentPanel.add(selectAppointmentDeleteLabel);

		deleteAppointmentSelector = new JComboBox();
		deleteAppointmentSelector.setBounds(163, 30, 369, 27);
		deleteAppointmentPanel.add(deleteAppointmentSelector);

		errorMsgDelete = new JLabel("");
		errorMsgDelete.setBounds(6, 62, 626, 16);
		deleteAppointmentPanel.add(errorMsgDelete);

		deleteAppointmentButton = new JButton("Delete Appointment");
		deleteAppointmentButton.setBounds(253, 93, 164, 29);
		deleteAppointmentPanel.add(deleteAppointmentButton);

		currentAppointmentLabel = new JLabel("Current Appointments");
		currentAppointmentLabel.setBounds(6, 215, 149, 16);
		frame.add(currentAppointmentLabel);

		appointmentTable = new JTable();	
		appointmentTable.setEnabled(false);
		currentAppointmentScrollPane = new JScrollPane(appointmentTable);
		Dimension d = appointmentTable.getPreferredSize();
		currentAppointmentScrollPane.setPreferredSize(new Dimension(d.width,HEIGHT_CURRENT_APPOINTMENT_TABLE));
		currentAppointmentScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		appointmentTable.setBounds(6, 243, 638, 120);
		frame.add(appointmentTable);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(0, 51, 650, 12);
		frame.add(separator);

		JSeparator separator_1 = new JSeparator();
		separator_1.setForeground(Color.BLACK);
		separator_1.setBounds(0, 204, 650, 12);
		frame.add(separator_1);

		updatedServiceBox = new JComboBox<String>();


		timeFormat = DateTimeFormatter.ofPattern("HH:mm");


		// global settings
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("FlexiBook Application");


		// Listeners for the layered frame
		bookAppointmentPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(bookAppointmentPanel);
				layeredPane.repaint();
				layeredPane.revalidate();
				errorMsgBook.setText("");
				refreshData();
			}
		});
		updateAppointmentPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(updateAppointmentPanel);
				layeredPane.repaint();
				layeredPane.revalidate();
				errorMsgUpdate.setText("");
				refreshData();
			}
		});
		deleteAppointmentPageButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(deleteAppointmentPanel);
				layeredPane.repaint();
				layeredPane.revalidate();
				errorMsgDelete.setText("");
				refreshData();
			}
		});

		// Listeners for account settings button
		settings.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				settingsButtonActionPerfomed(evt);
			}
		});

		//Listeners for book Appointment
		bookAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				bookAppointmentButtonActionPerformed(evt);
			}
		});

		// listeners for view available time slots button
		viewAvailableTimesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				viewAvailableTimeSlotsButtonActionPerformed(evt);
			}
		});

		// listeners for reset button
		resetSelectionsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetButtonActionPerformed(evt);
			}
		});

		// listeners for update date or time button
		updateDateTimeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				updateDateTimeButtonActionPerformed(evt);
			}
		});

		// listeners for update service button
		updateServiceButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				updateServiceButtonActionPerformed(evt);
			}
		});

		// listeners for update service button
		deleteAppointmentButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteAppointmentButtonActionPerformed(evt);
			}
		});

	}




	// Refresh the page's data
	/**
	 * @author antoninguerre
	 */
	private void refreshData() {

		// refresh Book Appointment
		services = new HashMap<Integer, String>();
		serviceSelector.removeAllItems();
		Integer index = 0;
		for (TOService service : FlexiBookController.getServices()) {
			services.put(index, service.getName());
			serviceSelector.addItem(service.getName());
			index++;
		};
		serviceSelector.setSelectedIndex(-1);

		additionalServiceSelector.removeAllItems();
		for (int i=0;i<1;i++)
			additionalServiceSelector.addItem("Bonus");
		additionalServiceSelector.setSelectedIndex(-1);

		dateSelector.getModel().setSelected(false);
		timeTextField.setText("");



		// refresh Update Appointment
		services = new HashMap<Integer, String>();
		updatedServiceBox.removeAllItems();
		Integer indexUpdate = 0;
		for (TOService service : FlexiBookController.getServices()) {
			services.put(indexUpdate, service.getName());
			updatedServiceBox.addItem(service.getName());
			indexUpdate++;
		};
		updatedServiceBox.setSelectedIndex(-1);

		appointments = new HashMap<Integer, TOAppointment>();
		updateAppointmentSelector.removeAllItems();
		Integer indexAppointmentSelectorUpdate = 0;
		for (TOAppointment appointment : FlexiBookController.getAppointments()) {
			if (appointment.getCustomer().getUsername().equals(user.getUsername())) {

				String startTime = timeFormat.format(appointment.getTimeSlot().getStartTime().toLocalTime());
				appointments.put(indexAppointmentSelectorUpdate, appointment);
				updateAppointmentSelector.addItem("- "+ appointment.getBookableService().getName()
						+ " appointment on "+appointment.getTimeSlot().getStartDate().toString()
						+ " at "+ startTime);
				indexAppointmentSelectorUpdate++;
			}
		}
		updateAppointmentSelector.setSelectedIndex(-1);


		// refresh Delete Appointment
		appointments = new HashMap<Integer, TOAppointment>();
		deleteAppointmentSelector.removeAllItems();
		Integer indexAppointmentSelectorDelete = 0;
		for (TOAppointment appointment : FlexiBookController.getAppointments()) {
			if (appointment.getCustomer().getUsername().equals(user.getUsername())) {

				String startTime = timeFormat.format(appointment.getTimeSlot().getStartTime().toLocalTime());

				appointments.put(indexAppointmentSelectorDelete, appointment);
				deleteAppointmentSelector.addItem("- "+ appointment.getBookableService().getName()
						+ " appointment on "+appointment.getTimeSlot().getStartDate().toString()
						+ " at "+ startTime);
				indexAppointmentSelectorDelete++;
			}
		}
		deleteAppointmentSelector.setSelectedIndex(-1);

		
		// refresh the current appointment table
		refreshCurrentAppointmentTable();
	}


	// Refresh the current appointment table at the bottom of the page
	/**
	 * @author antoninguerre
	 */
	private void refreshCurrentAppointmentTable() {
		currentAppointmentDtm = new DefaultTableModel(0,0);
		currentAppointmentDtm.setColumnIdentifiers(currentAppointmentColumnNames);
		appointmentTable.setModel(currentAppointmentDtm);

		String[] obj1 = {"Service", "Additional Services", "Date", "Start Time", "End Time"};
		currentAppointmentDtm.addRow(obj1);

		for (TOAppointment appointment : FlexiBookController.getAppointments()) {
			if (appointment.getCustomer().getUsername().equals(user.getUsername())) {

				String startTime = timeFormat.format(appointment.getTimeSlot().getStartTime().toLocalTime());
				String endTime = timeFormat.format(appointment.getTimeSlot().getEndTime().toLocalTime());
				Object[] obj = {appointment.getBookableService().getName(), "----",appointment.getTimeSlot().getStartDate().toString(),startTime,endTime};
				currentAppointmentDtm.addRow(obj);
			}
		}
	}


	// Refresh the selected JDatePickerImpl date
	/**
	 * @author antoninguerre
	 */
	private void refreshDate() {
		dateSelector.getModel().setSelected(false);
	}




	// Check if all the fields required to book an appointment are valid and
	// try to book an appointment with these characteristics
	/**
	 * @author antoninguerre
	 */
	private void bookAppointmentButtonActionPerformed(ActionEvent evt) {
		errorMsgBook.setText("");

		if (serviceSelector.getModel().getSelectedItem() == null) {
			errorMsgBook.setForeground(Color.RED);
			errorMsgBook.setText("Please select a service");
			return;
		}
		if (dateSelector.getModel().getValue() == null) {
			errorMsgBook.setForeground(Color.RED);
			errorMsgBook.setText("Please select a date for the appointment");
			return;
		}
		if (!(timeTextField.getText().matches("([01]?[0-9]|2[0-3]):[0-5][0-9]"))) {
			errorMsgBook.setForeground(Color.RED);
			errorMsgBook.setText("Please enter a valid time format: HH:mm");
			timeTextField.setText("");
			return;
		}
		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.okButtonText", "Confirm Booking");
		int dialogButton = JOptionPane.OK_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null,
				"Confirm booking for "+ serviceSelector.getSelectedItem().toString() +" on "+ dateSelector.getModel().getValue().toString() +" at "+timeTextField.getText()+" ?",
				"Book Appointment",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.OK_OPTION) {
			try {
				FlexiBookController.makeAppointment(user.getUsername(), user.getUsername(), serviceSelector.getSelectedItem().toString(), null, dateSelector.getModel().getValue().toString(), timeTextField.getText());
				errorMsgBook.setForeground(Color.GREEN);
				errorMsgBook.setText("Your appointment was successfully booked");
			} catch (InvalidInputException e) {
				errorMsgBook.setForeground(Color.RED);
				errorMsgBook.setText(e.getMessage());
			}

		}
		if (dialogResult == JOptionPane.CANCEL_OPTION) {
			errorMsgBook.setForeground(Color.RED);
			errorMsgBook.setText("No appointment was booked");
			return;
		}
		refreshData();
	}


	// Opens a "settings" popup when the settings button is pressed
	/**
	 * @author antoninguerre
	 */
	private void settingsButtonActionPerfomed(ActionEvent evt) {
		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.yesButtonText", "Logout");
		UIManager.put("OptionPane.noButtonText", "Account Settings");
		int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, null,"Settings",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.YES_OPTION) {	
			FlexiBookApplication.setCurrentUser(null);
			this.setVisible(false);
			new Login().setVisible(true);
		}
		if (dialogResult == JOptionPane.CANCEL_OPTION) {
			return;
		}
		if (dialogResult == JOptionPane.NO_OPTION) {
			this.setVisible(false);
			new UpdateAccountPage().setVisible(true);
		}
	}


	// Makes the appointment calendar visible when the "view appointment calendar" button is pressed
	/**
	 * @author antoninguerre
	 */
	private void viewAvailableTimeSlotsButtonActionPerformed(ActionEvent evt) {
		errorMsgBook.setText("");
		try {
			new AppointmentCalendar().setVisible(true);
		} catch (RuntimeException e) {
			errorMsgBook.setText(e.getMessage());
		}
	}


	// Resets all the selected options and entered texts on the page
	/**
	 * @author antoninguerre
	 */
	private void resetButtonActionPerformed(ActionEvent evt) {
		errorMsgBook.setText("");

		if (serviceSelector != null)
			serviceSelector.setSelectedIndex(-1);

		if (additionalServiceSelector != null)
			additionalServiceSelector.setSelectedIndex(-1);

		if (dateSelector.getModel().getValue() != null)
			dateSelector.getModel().setSelected(false);

		if (timeTextField.getText() != null)
			timeTextField.setText("");
	}


	// Update the date and/or the time of a booked appointment by checking if 
	// all the inputs are valid and calling the controller
	/**
	 * @author antoninguerre
	 */
	private void updateDateTimeButtonActionPerformed(ActionEvent evt) {
		errorMsgUpdate.setText("");

		if (updateAppointmentSelector.getSelectedItem() == null) {
			errorMsgUpdate.setForeground(Color.RED);
			errorMsgUpdate.setText("Please select an appointment to update");
			return;
		}

		TOAppointment selectedUpdateAppointment = appointments.get(updateAppointmentSelector.getSelectedIndex());
		String newDate = selectedUpdateAppointment.getTimeSlot().getStartDate().toString();
		String newTime = selectedUpdateAppointment.getTimeSlot().getStartTime().toString();

		SqlDateModel model = new SqlDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");
		JDatePanelImpl datePanel = new JDatePanelImpl(model,p);
		bookAppointmentPanel.add(dateSelector);
		JTextField updatedTime = new JTextField();
		JDatePickerImpl updatedDate = new JDatePickerImpl(datePanel,new DateLabelFormatter());
		Object[] updatedDateTime = {
				"New date: ", updatedDate,
				"New Time:", updatedTime
		};

		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.okButtonText", "Confirm Changes");
		int dialogButton = JOptionPane.OK_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, updatedDateTime,"Update Date and Time",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.OK_OPTION) {
			if (updatedDate.getModel().getValue() != null) {
				newDate = updatedDate.getModel().getValue().toString();
			}
			if (updatedDate.getModel().getValue() != null && updatedTime.getText().length() == 0) {
				errorMsgUpdate.setForeground(Color.RED);
				errorMsgUpdate.setText("A time must be selected");
				return;
			}
			if (updatedTime != null) {
				newTime = updatedTime.getText();
			}
			try {
				FlexiBookController.updateAppointment(user.getUsername(), selectedUpdateAppointment.getCustomer().getUsername(), false, selectedUpdateAppointment.getBookableService().getName(),
						selectedUpdateAppointment.getTimeSlot().getStartDate().toString(), selectedUpdateAppointment.getTimeSlot().getStartTime().toString(), 
						"", "", newDate, newTime);
			} catch (InvalidInputException e) {
				errorMsgUpdate.setForeground(Color.RED);
				errorMsgUpdate.setText(e.getMessage());
			}
		}

		if (dialogResult == JOptionPane.CANCEL_OPTION) {
			errorMsgUpdate.setForeground(Color.RED);
			errorMsgUpdate.setText("Your appointment was not updated");
		}
		refreshData();
	}


	// Update the service of a booked appointment by checking if 
	// all the inputs are valid and calling the controller
	/**
	 * @author antoninguerre
	 */
	private void updateServiceButtonActionPerformed(ActionEvent evt) {
		errorMsgUpdate.setText("");

		if (updateAppointmentSelector.getSelectedItem() == null) {
			errorMsgUpdate.setForeground(Color.RED);
			errorMsgUpdate.setText("Please select an appointment to update");
			return;
		}
		TOAppointment selectedUpdateAppointment = appointments.get(updateAppointmentSelector.getSelectedIndex());
		String newService = selectedUpdateAppointment.getBookableService().getName();
		Object[] updatedServiceMessage = {
				"New Service: ", updatedServiceBox
		};

		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.okButtonText", "Confirm Changes");
		int dialogButton = JOptionPane.OK_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, updatedServiceMessage,"Update Service",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.OK_OPTION) {
			if (updatedServiceBox.getSelectedItem() != null && !updatedServiceBox.getSelectedItem().toString().equals(selectedUpdateAppointment.getBookableService().getName())) {
				newService = updatedServiceBox.getSelectedItem().toString();

				try {
					FlexiBookController.updateAppointment(user.getUsername(), selectedUpdateAppointment.getCustomer().getUsername(), true, newService,
							selectedUpdateAppointment.getTimeSlot().getStartDate().toString(), selectedUpdateAppointment.getTimeSlot().getStartTime().toString(), 
							"", "", selectedUpdateAppointment.getTimeSlot().getStartDate().toString(), selectedUpdateAppointment.getTimeSlot().getStartTime().toString());
				} catch (InvalidInputException e) {
					errorMsgUpdate.setForeground(Color.RED);
					errorMsgUpdate.setText(e.getMessage());
				}
			}
			else {
				errorMsgUpdate.setForeground(Color.RED);
				errorMsgUpdate.setText("No new service was selected");
			}
		}
		if (dialogResult == JOptionPane.CANCEL_OPTION) {
			errorMsgUpdate.setForeground(Color.RED);
			errorMsgUpdate.setText("Your appointment was not updated");
		}
		refreshData();
	}


	// Delete a booked appointment by checking if an appointment 
	// has been selected and calling the controller
	/**
	 * @author antoninguerre
	 */
	private void deleteAppointmentButtonActionPerformed(ActionEvent evt) {
		errorMsgDelete.setText("");

		if (deleteAppointmentSelector.getSelectedItem() == null) {
			errorMsgDelete.setForeground(Color.RED);
			errorMsgDelete.setText("Please select an appointment to delete");
			return;
		}

		TOAppointment selectedDeleteAppointment = appointments.get(deleteAppointmentSelector.getSelectedIndex());

		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.okButtonText", "Delete Appointment");
		int dialogButton = JOptionPane.OK_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this appointment?","Delete Appointment",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.OK_OPTION) {

			try {
				FlexiBookController.cancelAppointment(user.getUsername(), selectedDeleteAppointment.getCustomer().getUsername(), selectedDeleteAppointment.getBookableService().getName(), selectedDeleteAppointment.getTimeSlot().getStartDate().toString(), selectedDeleteAppointment.getTimeSlot().getStartTime().toString());
				errorMsgDelete.setForeground(Color.GREEN);
				errorMsgDelete.setText("This appointment was successfully deleted");
			} catch (InvalidInputException e) {
				errorMsgDelete.setForeground(Color.RED);
				errorMsgDelete.setText(e.getMessage());
			}
		}
		if (dialogResult == JOptionPane.CANCEL_OPTION) {
			errorMsgDelete.setForeground(Color.RED);
			errorMsgDelete.setText("Your appointment was not deleted");
		}

		refreshData();
	}

}





