package ca.mcgill.ecse.flexibook.view;

import java.awt.Color;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.plaf.basic.BasicArrowButton;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
import ca.mcgill.ecse.flexibook.model.Business;

public class ManageBusinessInfo extends JFrame {

	private static final long serialVersionUID = -6947306506677458241L;

	private static final String PAGE_TITLE = "Manage Business Information";

	private String error = "";
	private String info = "";

	// User Interface
	private JLabel errorMessage;
	private JLabel infoMessage;
	private JLabel pageTitle;
	private JButton backButton;
	private JButton submitButton;

	// Main input fields
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel addressLabel;
	private JTextField addressTextField;
	private JLabel phoneNumberLabel;
	private JTextField phoneNumberTextField;
	private JLabel emailLabel;
	private JTextField emailTextField;

	// -------------------------------------------------------------------------
	/**
	 * ManageBusinessInfo entry-point
	 */
	public ManageBusinessInfo() {
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
		pageTitle = new JLabel("Manage Business Information");
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

		// Basic Queries
		nameLabel = new JLabel("Business Name:");
		nameLabel.setForeground(Color.DARK_GRAY);
		nameTextField = new JTextField();

		addressLabel = new JLabel("Primary Address:");
		addressLabel.setForeground(Color.DARK_GRAY);
		addressTextField = new JTextField();

		phoneNumberLabel = new JLabel("Phone Number:");
		phoneNumberLabel.setForeground(Color.DARK_GRAY);
		phoneNumberTextField = new JTextField();

		emailLabel = new JLabel("Primary Email:");
		emailLabel.setForeground(Color.DARK_GRAY);
		emailTextField = new JTextField();

		// Submit Button
		submitButton = new JButton();
		submitButton.setText("Submit");
		submitButton.setForeground(Color.DARK_GRAY);
		submitButton.setBackground(Color.LIGHT_GRAY);
		submitButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				submitButtonActionPerformed();
			}
		});

		// Creating the Layout
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER).addComponent(backButton)
				.addComponent(errorMessage).addComponent(infoMessage)
				.addGroup(layout.createSequentialGroup().addComponent(pageTitle))
				.addGroup(layout.createSequentialGroup().addComponent(nameLabel).addComponent(nameTextField, 200, 200,
						400))
				.addGroup(layout.createSequentialGroup().addComponent(addressLabel).addComponent(addressTextField, 200,
						200, 400))
				.addGroup(layout.createSequentialGroup().addComponent(phoneNumberLabel)
						.addComponent(phoneNumberTextField, 200, 200, 400))
				.addGroup(layout.createSequentialGroup().addComponent(emailLabel).addComponent(emailTextField, 200, 200,
						400))
				.addComponent(submitButton));

		layout.linkSize(SwingConstants.HORIZONTAL,
				new java.awt.Component[] { nameLabel, addressLabel, phoneNumberLabel, emailLabel });
		layout.linkSize(SwingConstants.HORIZONTAL,
				new java.awt.Component[] { nameTextField, addressTextField, phoneNumberTextField, emailTextField });

		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(backButton).addComponent(errorMessage)
				.addComponent(infoMessage).addGap(10).addComponent(pageTitle).addGap(10)
				.addGroup(layout.createParallelGroup().addComponent(nameLabel).addComponent(nameTextField))
				.addGroup(layout.createParallelGroup().addComponent(addressLabel).addComponent(addressTextField))
				.addGroup(
						layout.createParallelGroup().addComponent(phoneNumberLabel).addComponent(phoneNumberTextField))
				.addGroup(layout.createParallelGroup().addComponent(emailLabel).addComponent(emailTextField)).addGap(10)
				.addComponent(submitButton));

		pack();
		setLocationRelativeTo(null);
	}

	// -------------------------------------------------------------------------
	/**
	 * Handle data updates
	 */
	private void refreshData() {

		if (error.isBlank() || error.isEmpty()) {
			try {
				Business business = FlexiBookController.getBusiness();
				nameTextField.setText(business.getName());
				addressTextField.setText(business.getAddress());
				phoneNumberTextField.setText(business.getPhoneNumber());
				emailTextField.setText(business.getEmail());
				error = "";
			} catch (InvalidInputException e) {
				nameTextField.setText("");
				addressTextField.setText("");
				phoneNumberTextField.setText("");
				emailTextField.setText("");
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
	private void submitButtonActionPerformed() {

		error = "";
		info = "";

		if (FlexiBookController.hasBusiness()) {
			try {
				FlexiBookController.updateBusinessInfo(nameTextField.getText(), addressTextField.getText(),
						phoneNumberTextField.getText(), emailTextField.getText(),
						FlexiBookApplication.getCurrentUser());
				info = "Successfully updated your business";
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}
		} else {
			try {
				FlexiBookController.createBusinessInfo(nameTextField.getText(), addressTextField.getText(),
						phoneNumberTextField.getText(), emailTextField.getText(),
						FlexiBookApplication.getCurrentUser());
				info = "Successfully created your business";
			} catch (InvalidInputException e) {
				error = e.getMessage();
			}
		}

		refreshData();
	}
}
