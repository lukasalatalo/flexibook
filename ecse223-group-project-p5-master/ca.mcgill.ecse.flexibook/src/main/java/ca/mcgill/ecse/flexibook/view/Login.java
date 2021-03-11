package ca.mcgill.ecse.flexibook.view;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.*;

import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.FlexiBookController;
import ca.mcgill.ecse.flexibook.controller.InvalidInputException;
public class Login extends JFrame{
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	
	// Create login page
	/**
	 * @author lukasalatalo
	 */
	public Login() {
		initComponents();
		refreshData();
	}
	
	private JButton login;
	private JLabel usernameLabel;
	private JTextField usernameText;
	private JLabel passwordLabel;
	private JPasswordField passwordText;
	private JButton signUp;
	private JLabel or;
	private JLabel errorMessage;
	private String error = null;

	
	//Initialize the page's components
	/**
	 * @author lukasalatalo
	 */
	private void initComponents() {
		
		setBounds(450,200,100,100);
		setPreferredSize(new Dimension(150, 235));
		login = new JButton("Login");
		usernameLabel = new JLabel();
		usernameLabel.setText("Username:           ");
		usernameText = new JTextField();
		passwordLabel = new JLabel();
		passwordLabel.setText("Password:");
		passwordText = new JPasswordField();
		passwordText.setEchoChar('*');
		signUp = new JButton("Sign up");
		or = new JLabel("OR");
		errorMessage=new JLabel();
		errorMessage.setForeground(Color.RED);

		
		login.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loginActionPerformed(evt);
			}
		});
		signUp.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				signUpActionPerformed(evt);
			}
		});
		
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setTitle("Login");
		
		// layout
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addGap(WIDTH/2)
						.addGroup(layout.createParallelGroup()
								.addComponent(errorMessage)
								.addComponent(usernameLabel)
								.addComponent(usernameText)
								.addComponent(passwordLabel)
								.addComponent(passwordText)
								.addComponent(login)
								.addComponent(or)
								.addComponent(signUp))
						));
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] 
				{usernameLabel,usernameText,passwordLabel, passwordText});
		
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(errorMessage)
						.addComponent(usernameLabel)
						.addComponent(usernameText)
						.addComponent(passwordLabel)
						.addComponent(passwordText)
						.addComponent(login)
						.addComponent(or)
						.addComponent(signUp)

						));
		pack();										
	}
	
	// Refresh the page's data
	/**
	 * @author lukasalatalo
	 */
	private void refreshData() {
		errorMessage.setText(error);
		if (error == null || error.length() == 0) {
			// populate page with data
			passwordText.setText("");
			usernameText.setText("");
		}
		pack();
	}
	
	
	// Button that logs the user in after checking if the fields are entered correctly
	/** 
	 * @author lukasalatalo
	 */
	private void loginActionPerformed(java.awt.event.ActionEvent evt) {
		error=null;
		
		String password = new String(passwordText.getPassword());
		
		try {
			FlexiBookController.logIn(usernameText.getText(), password);
			if (usernameText.getText().equals(FlexiBookApplication.getFlexibook().getOwner().getUsername())) {
				FlexiBookApplication.getFlexibook().getOwner().setUsername("owner");
				this.setVisible(false);
				new OwnerWelcomePage().setVisible(true);
			}
			else {
				this.setVisible(false);
				new AppointmentPage2().setVisible(true);
			}
			
		}catch (InvalidInputException e) {
			error=e.getMessage();
		}	
		refreshData();
	}
	
	
	// Button that redirects the user to the sign up page
	/**
	 * @author lukasalatalo
	 */
	private void signUpActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
		new SignUpPage().setVisible(true);
	}
	
}
