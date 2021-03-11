package ca.mcgill.ecse.flexibook.view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.table.DefaultTableModel;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.TOAppointment;
import ca.mcgill.ecse.flexibook.model.User;
import javax.swing.GroupLayout.Alignment;
import java.awt.Component;
import javax.swing.LayoutStyle.ComponentPlacement;

public class OwnerWelcomePage extends JFrame {

	private static final String IMG_PATH = "/flexibook.png";

	private JPanel frame;
	private User user;
	private JTable table;
	private JLabel welcomeLabel;
	private JLabel welcomeLabel_1;
	private JButton settingsButton;
	private JSeparator separator;
	private JLabel currentAppointmentsLabel;
	private JButton servicesButton;
	private JButton timeOffButton;
	private JButton infoButton;
	private JButton hoursButton;
	private DefaultTableModel currentAppointmentDtm;
	private String currentAppointmentColumnNames[] = { "Customer", "Service", "Date", "Start Time", "End Time" };
	private DateTimeFormatter timeFormat;
	private JButton viewAppointmentCalendar;
	private JButton appointmentManagerButton;

	/**
	 * Launch the application.
	 */
	/*
	 * public static void main(String[] args) { EventQueue.invokeLater(new
	 * Runnable() { public void run() { try { OwnerWelcomePage window = new
	 * OwnerWelcomePage(); window.frame.setVisible(true); } catch (Exception e) {
	 * e.printStackTrace(); } } }); }
	 * 
	 * /** Create the application.
	 */
	public OwnerWelcomePage() {
		timeFormat = DateTimeFormatter.ofPattern("HH:mm");

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		user = FlexiBookApplication.getCurrentUser();

		// global settings
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Admin Page");
		//getContentPane().setBackground(Color.LIGHT_GRAY);

		settingsButton = new BasicArrowButton(BasicArrowButton.SOUTH);
		settingsButton.setForeground(Color.LIGHT_GRAY);
		settingsButton.setBackground(Color.DARK_GRAY);
		settingsButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				settingsButtonActionPerfomed(evt);
			}
		});

		welcomeLabel = new JLabel("Welcome!", SwingConstants.CENTER);
		welcomeLabel.setForeground(Color.DARK_GRAY);
		welcomeLabel.setFont(new java.awt.Font(null, java.awt.Font.PLAIN, 25));

		try {
			BufferedImage img = ImageIO.read(new File(IMG_PATH));
			ImageIcon icon = new ImageIcon(img);
			welcomeLabel_1 = new JLabel(icon);
		} catch (IOException e) {
		}

		servicesButton = new JButton("Manage Services");
		servicesButton.setForeground(Color.DARK_GRAY);
		servicesButton.setBackground(Color.LIGHT_GRAY);
		servicesButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				serviceButtonActionPerfomed(evt);
			}
		});

		timeOffButton = new JButton("Manage Time Off");
		timeOffButton.setForeground(Color.DARK_GRAY);
		timeOffButton.setBackground(Color.LIGHT_GRAY);
		timeOffButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				timeOffButtonActionPerformed(evt);
			}
		});

		infoButton = new JButton("Manage Business Information");
		infoButton.setForeground(Color.DARK_GRAY);
		infoButton.setBackground(Color.LIGHT_GRAY);
		infoButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				infoButtonActionPerformed(evt);
			}
		});

		hoursButton = new JButton("Manage Business Hours");
		hoursButton.setForeground(Color.DARK_GRAY);
		hoursButton.setBackground(Color.LIGHT_GRAY);
		hoursButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				hoursButtonActionPerformed(evt);
			}
		});
		
		appointmentManagerButton = new JButton("Appointment Manager");
		appointmentManagerButton.setForeground(Color.DARK_GRAY);
		appointmentManagerButton.setBackground(Color.LIGHT_GRAY);
		appointmentManagerButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				appointmentManagerButtonActionPerformed(evt);
			}
		});
		
		currentAppointmentsLabel = new JLabel("Current Booked Appointments");

		table = new JTable();
		table.setEnabled(false);

		currentAppointmentDtm = new DefaultTableModel(0, 0);
		currentAppointmentDtm.setColumnIdentifiers(currentAppointmentColumnNames);
		table.setModel(currentAppointmentDtm);

		String[] obj1 = { "Customer", "Service", "Date", "Start Time", "End Time" };
		currentAppointmentDtm.addRow(obj1);

		for (TOAppointment appointment : FlexiBookController.getAppointments()) {

			String startTime = timeFormat.format(appointment.getTimeSlot().getStartTime().toLocalTime());
			String endTime = timeFormat.format(appointment.getTimeSlot().getEndTime().toLocalTime());
			Object[] obj = { appointment.getCustomer().getUsername(), appointment.getBookableService().getName(),
					appointment.getTimeSlot().getStartDate().toString(), startTime, endTime };
			currentAppointmentDtm.addRow(obj);
		}

		viewAppointmentCalendar = new JButton("View Appointment Calendar");
		viewAppointmentCalendar.setForeground(Color.DARK_GRAY);
		viewAppointmentCalendar.setBackground(Color.LIGHT_GRAY);
		viewAppointmentCalendar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				viewAppointmentCalendarButtonActionPerfomed(evt);
			}
		});
		
		

		// Creating the Layout
		GroupLayout layout = new GroupLayout(getContentPane());
		layout.setHorizontalGroup(
			layout.createParallelGroup(Alignment.CENTER)
				.addComponent(settingsButton, GroupLayout.PREFERRED_SIZE, 463, GroupLayout.PREFERRED_SIZE)
				.addGroup(layout.createSequentialGroup()
					.addComponent(infoButton)
					.addComponent(hoursButton))
				.addGroup(layout.createSequentialGroup()
					.addComponent(timeOffButton)
					.addComponent(servicesButton))
				.addComponent(currentAppointmentsLabel)
				.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(viewAppointmentCalendar)
				.addComponent(appointmentManagerButton)
		);
		layout.setVerticalGroup(
			layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
					.addComponent(settingsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(35)
					.addComponent(appointmentManagerButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(infoButton)
						.addComponent(hoursButton))
					.addGroup(layout.createParallelGroup(Alignment.LEADING)
						.addComponent(timeOffButton)
						.addComponent(servicesButton))
					.addGap(30)
					.addComponent(currentAppointmentsLabel)
					.addComponent(table, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(30)
					.addComponent(viewAppointmentCalendar)
					.addContainerGap(6, Short.MAX_VALUE))
		);
		layout.linkSize(SwingConstants.VERTICAL, new Component[] {servicesButton, timeOffButton, infoButton, hoursButton, viewAppointmentCalendar});
		layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {servicesButton, timeOffButton, infoButton, hoursButton, viewAppointmentCalendar});
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		pack();
		setLocationRelativeTo(null);
	}

	private void appointmentManagerButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new FlexiBookView().setVisible(true);
		
	}

	private void viewAppointmentCalendarButtonActionPerfomed(ActionEvent evt) {
		new AppointmentCalendar().setVisible(true);

	}

	private void serviceButtonActionPerfomed(ActionEvent evt) {
		this.setVisible(false);
		new ServicePage().setVisible(true);

	}

	private void infoButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new ManageBusinessInfo().setVisible(true);

	}

	private void hoursButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new ManageBusinessHours().setVisible(true);

	}

	private void timeOffButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new ManageTimeOff().setVisible(true);

	}

	private void settingsButtonActionPerfomed(ActionEvent evt) {
		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.yesButtonText", "Logout");
		UIManager.put("OptionPane.noButtonText", "Account Settings");
		int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, null, "Settings", dialogButton,
				JOptionPane.PLAIN_MESSAGE);

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
}
