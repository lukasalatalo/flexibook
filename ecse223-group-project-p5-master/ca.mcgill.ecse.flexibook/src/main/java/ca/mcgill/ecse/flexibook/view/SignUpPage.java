package ca.mcgill.ecse.flexibook.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import java.awt.Window.Type;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;

public class SignUpPage extends JFrame{
	private JTextField usernameTextField;
	private JPasswordField passwordTextField;
	private JButton viewPassword;
	private JLabel shownPassword;
	private String error = null;
	private JLabel errorMsg;

	
	// Create the sign up page
	/**
	 * @author egekaradibak
	 */
	public SignUpPage() {
		initComponents();
	}


	// Initialize the sign up page components
	/**
	 * @author egekaradibak
	 */
	private void initComponents() {
		
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 200, 300, 300);

		JLabel lblNewLabel = new JLabel("Sign Up Page");
		lblNewLabel.setBounds(20, 16, 107, 16);
		getContentPane().add(lblNewLabel);

		JLabel lblNewLabel_1 = new JLabel("Username");
		lblNewLabel_1.setBounds(60, 74, 107, 16);
		getContentPane().add(lblNewLabel_1);

		usernameTextField = new JTextField();
		usernameTextField.setBounds(60, 94, 130, 26);
		getContentPane().add(usernameTextField);
		usernameTextField.setColumns(10);

		JLabel lblNewLabel_2 = new JLabel("Password");
		lblNewLabel_2.setBounds(60, 143, 61, 16);
		getContentPane().add(lblNewLabel_2);

		passwordTextField = new JPasswordField();
		passwordTextField.setEchoChar('*');
		passwordTextField.setBounds(60, 162, 130, 26);
		getContentPane().add(passwordTextField);
		passwordTextField.setColumns(10);

		viewPassword = new JButton("View");
		viewPassword.setBounds(194, 162, 70, 29);
		getContentPane().add(viewPassword);

		JButton signUpButton = new JButton("Sign Up");
		signUpButton.setBounds(60, 221, 117, 29);
		getContentPane().add(signUpButton);
		
		errorMsg = new JLabel("");
		errorMsg.setBounds(60, 54, 200, 16);
		getContentPane().add(errorMsg);

		signUpButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				signUpButtonActionPerformed(evt);
			}
		});

		viewPassword.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				viewPasswordButtonActionPerformed(evt);
			}
		});

	}
	
	// Button that toggles the display of the user's password (hidden or shown)
	/** 
	 * @author antoninguerre
	 */
	private void viewPasswordButtonActionPerformed(ActionEvent evt) {		

		if (passwordTextField.getEchoChar() == '*') {
			passwordTextField.setEchoChar((char)0);
		}
		else 
			passwordTextField.setEchoChar('*');
	}


	// Button that signs up a customer after having checked that the fields are correct
	/**
	 * @author egekaradibak
	 * @param evt
	 */
	private void signUpButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		error = null;

//		System.out.println(usernameTextField.getText().equals("owner"));
		
		if (usernameTextField.getText().length() == 0 || passwordTextField.getPassword().length == 0 || (usernameTextField.getText().equals("owner") && FlexiBookApplication.getFlexibook().getOwner() != null)) {
			errorMsg.setForeground(Color.RED);
			errorMsg.setText("Invalid username or password");
			return;
		}

		if (FlexiBookApplication.getFlexibook().getCustomers().size() != 0) {
			for (int i = 0; i < FlexiBookApplication.getFlexibook().getCustomers().size(); i++) {
				if (usernameTextField.getText().equals(FlexiBookApplication.getFlexibook().getCustomer(i).getUsername())) {
					errorMsg.setForeground(Color.RED);
					errorMsg.setText("Unavailable username");
					return;
				}
			}
		}
		
		String password = new String(passwordTextField.getPassword());
		
		// call the controller
		try {
			FlexiBookController.signUpCustomerAccount(usernameTextField.getText(), password);		
		} catch (InvalidInputException e) {
			errorMsg.setText(e.getMessage());;
		}

		if (usernameTextField.getText().equals("owner")) {
			this.setVisible(false);
			new ServicePage().setVisible(true);
		}

		else {
			this.setVisible(false);
			new AppointmentPage2().setVisible(true);
		}

	}
}
