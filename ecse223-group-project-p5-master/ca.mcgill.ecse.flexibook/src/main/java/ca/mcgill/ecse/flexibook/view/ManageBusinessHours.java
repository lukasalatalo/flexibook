package ca.mcgill.ecse.flexibook.view;

import java.awt.Color;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

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

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.model.BusinessHour;
import ca.mcgill.ecse.flexibook.model.BusinessHour.DayOfWeek;

public class ManageBusinessHours extends JFrame {

	private static final long serialVersionUID = 8320367024117863736L;

	private static final String PAGE_TITLE = "Manage Business Hours";

	private String error = "";
	private String info = "";

	private List<String> times = new ArrayList<String>();
	private static String[] days = new String[] { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
			"Sunday" };
	private SpinnerModel addStartModel;
	private SpinnerModel addEndModel;
	private SpinnerModel updateStartModel;
	private SpinnerModel updateEndModel;
	private int addSelectedDayOfWeek;
	private int updateSelectedDayOfWeek;
	private int updateSelectedHours;
	private int deleteSelectedHours;

	// User Interface
	private JLabel errorMessage;
	private JLabel infoMessage;
	private JLabel pageTitle;
	private JButton backButton;
	private JSeparator titleSeparator;

	// Main input fields
	private JLabel addLabel;
	private JLabel addStartLabel;
	private JLabel addEndLabel;
	private JLabel addDayOfWeekLabel;
	private JComboBox<String> addDayOfWeekComboBox;
	private JSpinner addStartSpinner;
	private JSpinner addEndSpinner;
	private JButton addButton;
	private JSeparator addSeparator;

	private JLabel updateLabel;
	private JComboBox<String> updateSelectedHoursComboBox;
	private JLabel updateStartLabel;
	private JLabel updateEndLabel;
	private JLabel updateDayOfWeekLabel;
	private JComboBox<String> updateDayOfWeekComboBox;
	private JSpinner updateStartSpinner;
	private JSpinner updateEndSpinner;
	private JButton updateButton;
	private JSeparator updateSeparator;

	private JLabel deleteLabel;
	private JComboBox<String> deleteSelectedHoursComboBox;
	private JButton deleteButton;

	// -------------------------------------------------------------------------
	/**
	 * ManageBusinessInfo entry-point
	 */
	public ManageBusinessHours() {
		for (int i = 0; i < 25; i++) {
			String time = "";
			if (i < 10) {
				time += "0";
			}
			time += i;
			times.add(time + ":00");
			times.add(time + ":30");
		}

		addStartModel = new SpinnerListModel(times);
		addEndModel = new SpinnerListModel(times);

		updateStartModel = new SpinnerListModel(times);
		updateEndModel = new SpinnerListModel(times);

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
		pageTitle = new JLabel("Manage Business Hours");
		pageTitle.setForeground(Color.DARK_GRAY);
		pageTitle.setFont(new java.awt.Font(null, java.awt.Font.PLAIN, 25));

		// Back Button
		backButton = new BasicArrowButton(BasicArrowButton.WEST);
		backButton.setForeground(Color.LIGHT_GRAY);
		backButton.setBackground(Color.DARK_GRAY);
		backButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				backButtonActionPerformed();
			}
		});

		titleSeparator = new JSeparator(SwingConstants.HORIZONTAL);
		titleSeparator.setForeground(Color.DARK_GRAY);

		// Basic Queries
		addLabel = new JLabel("Add New Business Hours");
		addLabel.setForeground(Color.DARK_GRAY);
		addLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		addDayOfWeekLabel = new JLabel("Day of Week:");
		addDayOfWeekLabel.setForeground(Color.DARK_GRAY);
		addDayOfWeekComboBox = new JComboBox<String>(days);
		addDayOfWeekComboBox.setForeground(Color.DARK_GRAY);
		addDayOfWeekComboBox.setBackground(Color.LIGHT_GRAY);
		addDayOfWeekComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				addSelectedDayOfWeek = comboBox.getSelectedIndex();
			}
		});

		addStartLabel = new JLabel("Start:", SwingConstants.RIGHT);
		addStartLabel.setForeground(Color.DARK_GRAY);
		addEndLabel = new JLabel("End:");
		addEndLabel.setForeground(Color.DARK_GRAY);
		addStartSpinner = new JSpinner(addStartModel);
		addEndSpinner = new JSpinner(addEndModel);

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
		updateLabel = new JLabel("Update Business Hours");
		updateLabel.setForeground(Color.DARK_GRAY);
		updateLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		updateSelectedHoursComboBox = new JComboBox<String>(days);
		updateSelectedHoursComboBox.setForeground(Color.DARK_GRAY);
		updateSelectedHoursComboBox.setBackground(Color.LIGHT_GRAY);
		updateSelectedHoursComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				updateSelectedHours = comboBox.getSelectedIndex();
			}
		});

		updateDayOfWeekLabel = new JLabel("Day of Week:");
		updateDayOfWeekLabel.setForeground(Color.DARK_GRAY);
		updateDayOfWeekComboBox = new JComboBox<String>(days);
		updateDayOfWeekComboBox.setForeground(Color.DARK_GRAY);
		updateDayOfWeekComboBox.setBackground(Color.LIGHT_GRAY);
		updateDayOfWeekComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				updateSelectedDayOfWeek = comboBox.getSelectedIndex();
			}
		});

		updateStartLabel = new JLabel("Start:", SwingConstants.RIGHT);
		updateStartLabel.setForeground(Color.DARK_GRAY);
		updateEndLabel = new JLabel("End:");
		updateEndLabel.setForeground(Color.DARK_GRAY);
		updateStartSpinner = new JSpinner(updateStartModel);
		updateEndSpinner = new JSpinner(updateEndModel);

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
		deleteLabel = new JLabel("Remove Business Hours");
		deleteLabel.setForeground(Color.DARK_GRAY);
		deleteLabel.setFont(new java.awt.Font(null, java.awt.Font.BOLD, 14));

		deleteSelectedHoursComboBox = new JComboBox<String>(days);
		deleteSelectedHoursComboBox.setForeground(Color.DARK_GRAY);
		deleteSelectedHoursComboBox.setBackground(Color.LIGHT_GRAY);
		deleteSelectedHoursComboBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				@SuppressWarnings("unchecked")
				JComboBox<String> comboBox = (JComboBox<String>) evt.getSource();
				deleteSelectedHours = comboBox.getSelectedIndex();
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
				.addGroup(layout.createSequentialGroup().addComponent(addDayOfWeekLabel)
						.addComponent(addDayOfWeekComboBox))
				.addGroup(layout.createSequentialGroup().addComponent(addStartLabel).addComponent(addStartSpinner)
						.addComponent(addEndLabel).addComponent(addEndSpinner).addComponent(addButton))
				.addComponent(addSeparator).addComponent(updateLabel).addComponent(updateSelectedHoursComboBox)
				.addGroup(layout.createSequentialGroup().addComponent(updateDayOfWeekLabel)
						.addComponent(updateDayOfWeekComboBox))
				.addGroup(layout.createSequentialGroup().addComponent(updateStartLabel).addComponent(updateStartSpinner)
						.addComponent(updateEndLabel).addComponent(updateEndSpinner).addComponent(updateButton))
				.addComponent(updateSeparator).addComponent(deleteLabel).addGroup(layout.createSequentialGroup()
						.addComponent(deleteSelectedHoursComboBox).addComponent(deleteButton)));

		layout.linkSize(SwingConstants.HORIZONTAL,
				new java.awt.Component[] { addDayOfWeekLabel, addStartLabel, updateDayOfWeekLabel, updateStartLabel });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addStartLabel, updateStartLabel });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addStartSpinner, updateStartSpinner });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addEndLabel, updateEndLabel });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addEndSpinner, updateEndSpinner });
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] { addButton, updateButton, deleteButton });
		layout.linkSize(SwingConstants.VERTICAL,
				new java.awt.Component[] { addStartLabel, addStartSpinner, addEndLabel, addEndSpinner, addButton,
						updateSelectedHoursComboBox, updateStartLabel, updateStartSpinner, updateEndLabel,
						updateEndSpinner, updateButton, deleteSelectedHoursComboBox, deleteButton });
		layout.linkSize(SwingConstants.VERTICAL, new java.awt.Component[] { addDayOfWeekLabel, addDayOfWeekComboBox,
				updateDayOfWeekLabel, updateDayOfWeekComboBox });

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(backButton).addComponent(errorMessage)
				.addComponent(infoMessage).addGap(10).addComponent(pageTitle).addComponent(titleSeparator).addGap(30)
				.addComponent(addLabel)
				.addGroup(
						layout.createParallelGroup().addComponent(addDayOfWeekLabel).addComponent(addDayOfWeekComboBox))
				.addGroup(layout.createParallelGroup().addComponent(addStartLabel).addComponent(addStartSpinner)
						.addComponent(addEndLabel).addComponent(addEndSpinner).addComponent(addButton))
				.addComponent(addSeparator).addGap(30).addComponent(updateLabel)
				.addComponent(updateSelectedHoursComboBox)
				.addGroup(layout.createParallelGroup().addComponent(updateDayOfWeekLabel)
						.addComponent(updateDayOfWeekComboBox))
				.addGroup(layout.createParallelGroup().addComponent(updateStartLabel).addComponent(updateStartSpinner)
						.addComponent(updateEndLabel).addComponent(updateEndSpinner).addComponent(updateButton))
				.addComponent(updateSeparator).addGap(30).addComponent(deleteLabel).addGroup(layout
						.createParallelGroup().addComponent(deleteSelectedHoursComboBox).addComponent(deleteButton)));

		pack();
		setLocationRelativeTo(null);
	}

	// -------------------------------------------------------------------------
	/**
	 * Handle data updates
	 */
	private void refreshData() {

		if (error.isBlank() || error.isEmpty()) {
			updateSelectedHoursComboBox.removeAllItems();
			deleteSelectedHoursComboBox.removeAllItems();

			try {
				List<BusinessHour> businessHours = FlexiBookController.getBusinessHours();
				for (BusinessHour b : businessHours) {
					String entry = "";
					entry += b.getDayOfWeek().toString();
					entry += ": ";
					entry += b.getStartTime().toString();
					entry += " - ";
					entry += b.getEndTime().toString();

					updateSelectedHoursComboBox.addItem(entry);
					deleteSelectedHoursComboBox.addItem(entry);
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
			DayOfWeek dayOfWeek = resolveDayOfWeek(addSelectedDayOfWeek);
			Time start = string2Time((String) addStartSpinner.getValue());
			Time end = string2Time((String) addEndSpinner.getValue());
			FlexiBookController.addBusinessHours(dayOfWeek, start, end, FlexiBookApplication.getCurrentUser());
			info = "Successfully added the business hours";
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
			DayOfWeek dayOfWeek = resolveDayOfWeek(updateSelectedDayOfWeek);
			Time start = string2Time((String) updateStartSpinner.getValue());
			Time end = string2Time((String) updateEndSpinner.getValue());
			FlexiBookController.updateBusinessHours(updateSelectedHours, dayOfWeek, start, end,
					FlexiBookApplication.getCurrentUser());
			info = "Successfully updated the business hours";
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

		try {
			FlexiBookController.removeBusinessHours(deleteSelectedHours, FlexiBookApplication.getCurrentUser());
			info = "Successfully deleted the business hours";
		} catch (InvalidInputException e) {
			error = e.getMessage();
		}

		refreshData();
	}

	private static DayOfWeek resolveDayOfWeek(int aDayOfWeekIdx) throws InvalidInputException {

		String dayOfWeek = days[aDayOfWeekIdx];

		switch (dayOfWeek) {
		case "Monday":
			return DayOfWeek.Monday;
		case "Tuesday":
			return DayOfWeek.Tuesday;
		case "Wednesday":
			return DayOfWeek.Wednesday;
		case "Thursday":
			return DayOfWeek.Thursday;
		case "Friday":
			return DayOfWeek.Friday;
		case "Saturday":
			return DayOfWeek.Saturday;
		case "Sunday":
			return DayOfWeek.Sunday;
		default:
			throw new InvalidInputException("Invalid day of week");
		}
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
}
