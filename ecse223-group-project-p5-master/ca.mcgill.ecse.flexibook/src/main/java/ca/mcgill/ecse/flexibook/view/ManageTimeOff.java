package ca.mcgill.ecse.flexibook.view;

import java.awt.Color;
import java.sql.Date;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.SpinnerModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.model.SystemTime;
import ca.mcgill.ecse.flexibook.model.TimeSlot;

public class ManageTimeOff extends JFrame {

	private static final long serialVersionUID = 256746130331127057L;

	private static final String PAGE_TITLE = "Manage Time Off";

	private String error = "";
	private String info = "";

	private static String[] timeOffTypes = new String[] { "Holiday", "Vacation" };

	private List<String> times = new ArrayList<String>();
	@SuppressWarnings("unused")
	private Date systemDate;

	private JDatePanelImpl addStartDatePanel;
	private JDatePanelImpl addEndDatePanel;
	private JDatePanelImpl updateStartDatePanel;
	private JDatePanelImpl updateEndDatePanel;

	private JDatePickerImpl addStartDatePicker;
	private JDatePickerImpl addEndDatePicker;
	private JDatePickerImpl updateStartDatePicker;
	private JDatePickerImpl updateEndDatePicker;

	private SpinnerModel addStartTimeModel;
	private SpinnerModel addEndTimeModel;
	private SpinnerModel updateStartTimeModel;
	private SpinnerModel updateEndTimeModel;
	private int addSelectedTimeOffType;
	private int updateSelectedTimeOffType;
	private int updateSelectedTimeOff;
	private int deleteSelectedTimeOff;

	// User Interface
	private JLabel errorMessage;
	private JLabel infoMessage;
	private JLabel pageTitle;
	private JButton backButton;
	private JSeparator titleSeparator;

	// Main input fields
	private JLabel addLabel;
	private JLabel addStartDateLabel;
	private JLabel addEndDateLabel;
	private JLabel addStartTimeLabel;
	private JLabel addEndTimeLabel;
	private JSpinner addStartTimeSpinner;
	private JSpinner addEndTimeSpinner;
	private JComboBox<String> addTimeOffTypeComboBox;
	private JButton addButton;
	private JSeparator addSeparator;

	private JLabel updateLabel;
	private JComboBox<String> updateSelectedTimeOffComboBox;
	private JLabel updateStartDateLabel;
	private JLabel updateEndDateLabel;
	private JLabel updateStartTimeLabel;
	private JLabel updateEndTimeLabel;
	private JSpinner updateStartTimeSpinner;
	private JSpinner updateEndTimeSpinner;
	private JComboBox<String> updateTimeOffTypeComboBox;
	private JButton updateButton;
	private JSeparator updateSeparator;

	private JLabel deleteLabel;
	private JComboBox<String> deleteSelectedTimeOffComboBox;
	private JButton deleteButton;

	// -------------------------------------------------------------------------
	/**
	 * ManageBusinessInfo entry-point
	 */
	public ManageTimeOff() {
		for (int i = 0; i < 25; i++) {
			String time = "";
			if (i < 10) {
				time += "0";
			}
			time += i;
			times.add(time + ":00");
			times.add(time + ":30");
		}

		addStartTimeModel = new SpinnerListModel(times);
		addEndTimeModel = new SpinnerListModel(times);

		updateStartTimeModel = new SpinnerListModel(times);
		updateEndTimeModel = new SpinnerListModel(times);

		systemDate = SystemTime.getSystemDate();
		SqlDateModel addStartModel = new SqlDateModel();
		SqlDateModel addEndModel = new SqlDateModel();
		SqlDateModel updateStartModel = new SqlDateModel();
		SqlDateModel updateEndModel = new SqlDateModel();
		Properties p = new Properties();
		p.put("text.today", "Today");
		p.put("text.month", "Month");
		p.put("text.year", "Year");

		addStartDatePanel = new JDatePanelImpl(addStartModel, p);
		addEndDatePanel = new JDatePanelImpl(addEndModel, p);
		updateStartDatePanel = new JDatePanelImpl(updateStartModel, p);
		updateEndDatePanel = new JDatePanelImpl(updateEndModel, p);

		initComponents();
		refreshData();
	}

	// -------------------------------------------------------------------------
	/**
	 * Initialize all JSwing components
	 */
	private void initComponents() {
		// Page Defaults
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle(PAGE_TITLE);
		//getContentPane().setBackground(Color.LIGHT_GRAY);

		// Error Message
		errorMessage = new JLabel();
		errorMessage.setForeground(Color.RED);

		// Info Message
		infoMessage = new JLabel();
		infoMessage.setForeground(Color.DARK_GRAY);

		// Title
		pageTitle = new JLabel("Manage Time Off");
		pageTitle.setForeground(Color.DARK_GRAY);
		pageTitle.setFont(new java.awt.Font(null, java.awt.Font.PLAIN, 25));

		titleSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		titleSeparator.setForeground(Color.DARK_GRAY);

		// Back Button
		backButton = new BasicArrowButton(BasicArrowButton.WEST);
		backButton.setForeground(Color.LIGHT_GRAY);
		backButton.setBackground(Color.DARK_GRAY);
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backButtonActionPerformed();
			}
		});

		// Basic Queries
		addLabel = new JLabel("Add New Time Off");
		addLabel.setForeground(Color.DARK_GRAY);
		addLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		addStartDateLabel = new JLabel("Start Date:");
		addStartDateLabel.setForeground(Color.DARK_GRAY);
		addEndDateLabel = new JLabel("End Date:");
		addEndDateLabel.setForeground(Color.DARK_GRAY);
		addStartDatePicker = new JDatePickerImpl(addStartDatePanel, new DateLabelFormatter());
		addEndDatePicker = new JDatePickerImpl(addEndDatePanel, new DateLabelFormatter());
		addStartTimeLabel = new JLabel("Start Time:");
		addStartTimeLabel.setForeground(Color.DARK_GRAY);
		addEndTimeLabel = new JLabel("End Time:");
		addEndTimeLabel.setForeground(Color.DARK_GRAY);
		addStartTimeSpinner = new JSpinner(addStartTimeModel);
		addEndTimeSpinner = new JSpinner(addEndTimeModel);

		addTimeOffTypeComboBox = new JComboBox<String>(timeOffTypes);
		addTimeOffTypeComboBox.setForeground(Color.DARK_GRAY);
		addTimeOffTypeComboBox.setBackground(Color.LIGHT_GRAY);
		addTimeOffTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				addSelectedTimeOffType = comboBox.getSelectedIndex();
			}
		});

		// Submit Button
		addButton = new JButton();
		addButton.setText("Add");
		addButton.setForeground(Color.DARK_GRAY);
		addButton.setBackground(Color.LIGHT_GRAY);
		addButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				addButtonActionPerformed();
			}
		});

		addSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		addSeparator.setForeground(Color.DARK_GRAY);

		// Update Queries
		updateLabel = new JLabel("Update Time Off");
		updateLabel.setForeground(Color.DARK_GRAY);
		updateLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		updateSelectedTimeOffComboBox = new JComboBox<String>();
		updateSelectedTimeOffComboBox.setForeground(Color.DARK_GRAY);
		updateSelectedTimeOffComboBox.setBackground(Color.LIGHT_GRAY);
		updateSelectedTimeOffComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				updateSelectedTimeOff = comboBox.getSelectedIndex();
			}
		});

		updateStartDateLabel = new JLabel("Start Date:");
		updateStartDateLabel.setForeground(Color.DARK_GRAY);
		updateEndDateLabel = new JLabel("End Date:");
		updateEndDateLabel.setForeground(Color.DARK_GRAY);
		updateStartDatePicker = new JDatePickerImpl(updateStartDatePanel, new DateLabelFormatter());
		updateEndDatePicker = new JDatePickerImpl(updateEndDatePanel, new DateLabelFormatter());
		updateStartTimeLabel = new JLabel("Start Time:");
		updateStartTimeLabel.setForeground(Color.DARK_GRAY);
		updateEndTimeLabel = new JLabel("End Time:");
		updateEndTimeLabel.setForeground(Color.DARK_GRAY);
		updateStartTimeSpinner = new JSpinner(updateStartTimeModel);
		updateEndTimeSpinner = new JSpinner(updateEndTimeModel);

		updateTimeOffTypeComboBox = new JComboBox<String>(timeOffTypes);
		updateTimeOffTypeComboBox.setForeground(Color.DARK_GRAY);
		updateTimeOffTypeComboBox.setBackground(Color.LIGHT_GRAY);
		updateTimeOffTypeComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				updateSelectedTimeOffType = comboBox.getSelectedIndex();
			}
		});

		// Submit Button
		updateButton = new JButton();
		updateButton.setText("Update");
		updateButton.setForeground(Color.DARK_GRAY);
		updateButton.setBackground(Color.LIGHT_GRAY);
		updateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				updateButtonActionPerformed();
			}
		});

		updateSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		updateSeparator.setForeground(Color.DARK_GRAY);

		// Delete Queries
		deleteLabel = new JLabel("Remove Time Off");
		deleteLabel.setForeground(Color.DARK_GRAY);
		deleteLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		deleteSelectedTimeOffComboBox = new JComboBox<String>();
		deleteSelectedTimeOffComboBox.setForeground(Color.DARK_GRAY);
		deleteSelectedTimeOffComboBox.setBackground(Color.LIGHT_GRAY);
		deleteSelectedTimeOffComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				deleteSelectedTimeOff = comboBox.getSelectedIndex();
			}
		});

		// Submit Button
		deleteButton = new JButton();
		deleteButton.setText("Delete");
		deleteButton.setForeground(Color.DARK_GRAY);
		deleteButton.setBackground(Color.LIGHT_GRAY);
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed();
			}
		});

		// Creating the Layout
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(backButton)
				.addComponent(errorMessage).addComponent(infoMessage)
				.addGroup(layout.createSequentialGroup().addComponent(pageTitle)).addComponent(titleSeparator)
				.addComponent(addLabel)
				.addGroup(layout.createSequentialGroup().addComponent(addStartDateLabel)
						.addComponent(addStartDatePicker).addComponent(addEndDateLabel).addComponent(addEndDatePicker))
				.addGroup(
						layout.createSequentialGroup().addComponent(addStartTimeLabel).addComponent(addStartTimeSpinner)
								.addComponent(addEndTimeLabel).addComponent(addEndTimeSpinner))
				.addGroup(layout.createSequentialGroup().addComponent(addTimeOffTypeComboBox).addComponent(addButton))
				.addComponent(addSeparator).addComponent(updateLabel).addComponent(updateSelectedTimeOffComboBox)
				.addGroup(layout.createSequentialGroup().addComponent(updateStartDateLabel)
						.addComponent(updateStartDatePicker).addComponent(updateEndDateLabel)
						.addComponent(updateEndDatePicker))
				.addGroup(layout.createSequentialGroup().addComponent(updateStartTimeLabel)
						.addComponent(updateStartTimeSpinner).addComponent(updateEndTimeLabel)
						.addComponent(updateEndTimeSpinner))
				.addGroup(layout.createSequentialGroup().addComponent(updateTimeOffTypeComboBox)
						.addComponent(updateButton))
				.addComponent(updateSeparator).addComponent(deleteLabel).addGroup(layout.createSequentialGroup()
						.addComponent(deleteSelectedTimeOffComboBox).addComponent(deleteButton)));

		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addStartDateLabel, addStartTimeLabel,
				updateStartDateLabel, updateStartTimeLabel });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addStartDatePicker, addStartTimeSpinner,
				updateStartDatePicker, updateStartTimeSpinner });
		layout.linkSize(SwingConstants.HORIZONTAL,
				new java.awt.Component[] { addEndDateLabel, addEndTimeLabel, updateEndDateLabel, updateEndTimeLabel });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addEndDatePicker, addEndTimeSpinner,
				updateEndDatePicker, updateEndTimeSpinner });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addButton, updateButton, deleteButton });
		layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] { addStartDateLabel, addStartTimeLabel,
				addStartDatePicker, addStartTimeSpinner, addEndDateLabel, addEndTimeLabel, addEndDatePicker,
				addEndTimeSpinner, addButton, updateSelectedTimeOffComboBox, updateStartDateLabel, updateStartTimeLabel,
				updateStartDatePicker, updateStartTimeSpinner, updateEndDateLabel, updateEndTimeLabel,
				updateEndDatePicker, updateEndTimeSpinner, updateButton, deleteSelectedTimeOffComboBox, deleteButton });

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(backButton).addComponent(errorMessage)
				.addComponent(infoMessage).addGap(10).addGroup(layout.createParallelGroup().addComponent(pageTitle))
				.addComponent(titleSeparator).addGap(30).addComponent(addLabel)
				.addGroup(layout.createParallelGroup().addComponent(addStartDateLabel).addComponent(addStartDatePicker)
						.addComponent(addEndDateLabel).addComponent(addEndDatePicker))
				.addGroup(layout.createParallelGroup().addComponent(addStartTimeLabel).addComponent(addStartTimeSpinner)
						.addComponent(addEndTimeLabel).addComponent(addEndTimeSpinner))
				.addGap(10)
				.addGroup(layout.createParallelGroup().addComponent(addTimeOffTypeComboBox).addComponent(addButton))
				.addComponent(addSeparator).addGap(30).addComponent(updateLabel)
				.addComponent(updateSelectedTimeOffComboBox)
				.addGroup(layout.createParallelGroup().addComponent(updateStartDateLabel)
						.addComponent(updateStartDatePicker).addComponent(updateEndDateLabel)
						.addComponent(updateEndDatePicker))
				.addGroup(layout.createParallelGroup().addComponent(updateStartTimeLabel)
						.addComponent(updateStartTimeSpinner).addComponent(updateEndTimeLabel)
						.addComponent(updateEndTimeSpinner))
				.addGap(10)
				.addGroup(
						layout.createParallelGroup().addComponent(updateTimeOffTypeComboBox).addComponent(updateButton))
				.addComponent(updateSeparator).addGap(30).addComponent(deleteLabel).addGroup(layout
						.createParallelGroup().addComponent(deleteSelectedTimeOffComboBox).addComponent(deleteButton)));

		pack();
		setLocationRelativeTo(null);
	}

	// -------------------------------------------------------------------------
		/**
		 * Handle data updates
		 */
		private void refreshData() {

			if (error.isBlank() || error.isEmpty()) {
				updateSelectedTimeOffComboBox.removeAllItems();
				deleteSelectedTimeOffComboBox.removeAllItems();

				try {
					List<TimeSlot> slots = FlexiBookController.getHolidays();
					for (TimeSlot ts : slots) {
						String entry = "Holiday: ";
						entry += ts.getStartDate().toString();
						entry += " ";
						entry += ts.getStartTime().toString();
						entry += " - ";
						entry += ts.getEndDate().toString();
						entry += " ";
						entry += ts.getEndTime().toString();

						updateSelectedTimeOffComboBox.addItem(entry);
						deleteSelectedTimeOffComboBox.addItem(entry);
					}

					error = "";
				} catch (InvalidInputException e) {
					error += e.getMessage();
				}

				try {
					List<TimeSlot> slots = FlexiBookController.getVacations();
					for (TimeSlot ts : slots) {
						String entry = "Vacation: ";
						entry += ts.getStartDate().toString();
						entry += " ";
						entry += ts.getStartTime().toString();
						entry += " - ";
						entry += ts.getEndDate().toString();
						entry += " ";
						entry += ts.getEndTime().toString();

						updateSelectedTimeOffComboBox.addItem(entry);
						deleteSelectedTimeOffComboBox.addItem(entry);
					}

					error = "";
				} catch (InvalidInputException e) {
					error += e.getMessage();
				}
			}

			errorMessage.setText(error);
			infoMessage.setText(info);

			pack();
		}

		// -------------------------------------------------------------------------
		/**
		 * Back Button pressed handler
		 */
		private void backButtonActionPerformed() {
			new OwnerWelcomePage().setVisible(true);
			this.dispose();
		}

		// -------------------------------------------------------------------------
		/**
		 * Submit Button pressed handler
		 */
		private void addButtonActionPerformed() {

			error = "";
			info = "";

			try {
				Date startDate = string2Date(addStartDatePicker.getModel().getValue().toString());
				Time startTime = string2Time((String) addStartTimeSpinner.getValue());
				Date endDate = string2Date(addEndDatePicker.getModel().getValue().toString());
				Time endTime = string2Time((String) addEndTimeSpinner.getValue());
				Date systemTime = new Date(System.currentTimeMillis());

				if (addSelectedTimeOffType == 0) {
					FlexiBookController.addHoliday(startDate, startTime, endDate, endTime,
							FlexiBookApplication.getCurrentUser(), systemTime);
				} else {
					FlexiBookController.addVacation(startDate, startTime, endDate, endTime,
							FlexiBookApplication.getCurrentUser(), systemTime);
				}

				info = "Successfully added the time off";
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}

			refreshData();
		}

		// -------------------------------------------------------------------------
		/**
		 * Submit Button pressed handler
		 */
		private void updateButtonActionPerformed() {

			error = "";
			info = "";

			try {
				Date startDate = string2Date(updateStartDatePicker.getModel().getValue().toString());
				Time startTime = string2Time((String) addStartTimeSpinner.getValue());
				Date endDate = string2Date(updateEndDatePicker.getModel().getValue().toString());
				Time endTime = string2Time((String) addEndTimeSpinner.getValue());
				Date systemTime = new Date(System.currentTimeMillis());

				if (updateSelectedTimeOffType == 0) {
					FlexiBookController.updateHoliday(updateSelectedTimeOff, startDate, startTime, endDate, endTime,
							FlexiBookApplication.getCurrentUser(), systemTime);
				} else {
					FlexiBookController.updateVacation(updateSelectedTimeOff, startDate, startTime, endDate, endTime,
							FlexiBookApplication.getCurrentUser(), systemTime);
				}

				info = "Successfully updated the time off";
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}

			refreshData();
		}

		// -------------------------------------------------------------------------
		/**
		 * Submit Button pressed handler
		 */
		private void deleteButtonActionPerformed() {

			error = "";
			info = "";

			String[] selection = deleteSelectedTimeOffComboBox.getItemAt(deleteSelectedTimeOff).split(" ");

			try {
				if (deleteSelectedTimeOffComboBox.getItemAt(deleteSelectedTimeOff).contains("Holiday")) {
					int idx = FlexiBookController.getHolidayIdx(string2Date(selection[1]), string2Time(selection[2]));
					FlexiBookController.removeHoliday(idx, FlexiBookApplication.getCurrentUser());
				} else {
					int idx = FlexiBookController.getVacationIdx(string2Date(selection[1]), string2Time(selection[2]));
					FlexiBookController.removeVacation(idx, FlexiBookApplication.getCurrentUser());
				}

				info = "Successfully deleted the time off";
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}

			refreshData();
		}

		private static Time string2Time(String time) throws InvalidInputException {
			Time aTime = null;
			DateFormat format = new SimpleDateFormat("HH:mm");

			try {
				aTime = new Time(format.parse(time).getTime());
			} catch (ParseException e) {
				throw new InvalidInputException("Invalid time format");
			}

			return aTime;
		}

		private static Date string2Date(String date) {
			Date aDate = null;
			aDate = Date.valueOf(date);
			return aDate;
		}
	}