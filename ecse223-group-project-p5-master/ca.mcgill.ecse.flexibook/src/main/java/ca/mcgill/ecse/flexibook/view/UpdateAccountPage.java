package ca.mcgill.ecse.flexibook.view;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import java.awt.Window.Type;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.synth.SynthStyleFactory;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;

public class UpdateAccountPage extends JFrame{

	private JPanel frame;
	private JLabel errorMessage;
	private JTextField usernameTextField;
	private JPasswordField passwordTextField;
	private String error = null;
	private JButton back;
	private JLabel currentUsernameLabel;
	private JLabel currentUsernameLabel2;
	private JButton viewPassword;


	// Create the update account page
	/**
	 * @author egekaradibak
	 */
	public UpdateAccountPage() {
		initComponents();
		refreshData();
	}


	// Intialize the page's components
	/**
	 * @author egekaradibak
	 */
	private void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 200, 650, 300);
		frame = new JPanel();
		frame.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(frame);
		frame.setPreferredSize(new Dimension(300, 320));
		frame.setLayout(null);


		errorMessage=new JLabel();
		errorMessage.setForeground(Color.RED);
		errorMessage.setBounds(26, 58, 395, 16);
		frame.add(errorMessage);

		currentUsernameLabel = new JLabel("Current username:");
		currentUsernameLabel.setForeground(Color.DARK_GRAY);
		currentUsernameLabel.setBounds(26, 10 , 170, 13);
		getContentPane().add(currentUsernameLabel);

		currentUsernameLabel2 = new JLabel(FlexiBookApplication.getCurrentUser().getUsername());
		currentUsernameLabel2.setBounds(26, 27 , 170, 13);
		getContentPane().add(currentUsernameLabel2);

		back = new JButton("Back");
		back.setBounds(200, 15, 70, 29);
		getContentPane().add(back);

		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(0, 40, 300, 12);
		frame.add(separator);

		JLabel usernameLabel = new JLabel("New Username");
		usernameLabel.setBounds(32, 85, 107, 16);
		getContentPane().add(usernameLabel);

		usernameTextField = new JTextField();
		usernameTextField.setBounds(32, 105, 130, 26);
		getContentPane().add(usernameTextField);
		usernameTextField.setColumns(10);

		JLabel passwordLabel = new JLabel("New Password");
		passwordLabel.setBounds(32, 146, 246, 16);
		getContentPane().add(passwordLabel);

		passwordTextField = new JPasswordField();
		passwordTextField.setBounds(32, 166, 130, 26);
		passwordTextField.setEchoChar('*');
		getContentPane().add(passwordTextField);
		passwordTextField.setColumns(10);

		viewPassword = new JButton("View");
		viewPassword.setBounds(170,166,70,27);
		getContentPane().add(viewPassword);


		JButton updateButton = new JButton("Update Account");
		updateButton.setBounds(32, 220, 229, 29);
		getContentPane().add(updateButton);

		JButton deleteButton = new JButton("Delete Account");
		deleteButton.setBounds(32, 271, 229, 29);
		frame.add(deleteButton);

		updateButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				updateButtonActionPerformed(evt);
			}
		});
		deleteButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				deleteButtonActionPerformed(evt);
			}
		});
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Update Account");


		// Listener for back button
		back.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				backActionPerformed(evt);
			}
		});

		// Listener for view password
		viewPassword.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				viewPasswordButtonActionPerformed(evt);
			}
		});


	}


	// Button that allows the user to go back to the page where he came from
	/**
	 * @author antoninguerre
	 */
	private void backActionPerformed(ActionEvent evt) {
		this.setVisible(false);

		if (FlexiBookApplication.getCurrentUser().getUsername().equals(FlexiBookApplication.getFlexibook().getOwner().getUsername())) {
			new OwnerWelcomePage().setVisible(true);
		}
		else
			new AppointmentPage2().setVisible(true);
	}


	// Refresh the page's data
	/**
	 * @author egekaradibak
	 */
	private void refreshData() {
		pack();
	}


	// Update a user's account by first checking that the inputs are valid
	// and then calling the controller
	/**
	 * @author egekaradibak
	 */
	private void updateButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		errorMessage.setText("");

		String password = new String(passwordTextField.getPassword());

		if (usernameTextField.getText().length() == 0 && password.length() == 0) {
			errorMessage.setText("Enter new username or password");
			return;
		}
		// call the controller
		try {
			FlexiBookController.updateAccount(usernameTextField.getText(), password);	
			currentUsernameLabel2.setText(FlexiBookApplication.getCurrentUser().getUsername());
			errorMessage.setForeground(Color.GREEN);
			errorMessage.setText("Update successful");
		} catch (InvalidInputException e) {
			errorMessage.setForeground(Color.RED);
			errorMessage.setText(e.getMessage());;
		}

		refreshData();

	}


	// Delete a user's account by calling the controller
	/**
	 * @author egekaradibak
	 */
	private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message
		error = null;

		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.okButtonText", "Delete Account");
		int dialogButton = JOptionPane.OK_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, null,"Delete Account",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if(dialogResult == JOptionPane.YES_OPTION) {
			try {
				FlexiBookController.deleteCustomerAccount(FlexiBookApplication.getCurrentUser().getUsername());
				this.setVisible(false);
				new Login().setVisible(true);
			} catch (InvalidInputException e) {
				error=e.getMessage();
			}
		}
		refreshData();

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
}
