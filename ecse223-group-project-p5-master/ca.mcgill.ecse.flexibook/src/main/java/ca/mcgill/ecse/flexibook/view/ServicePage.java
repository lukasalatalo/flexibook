package ca.mcgill.ecse.flexibook.view;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;


import ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.*;
import ca.mcgill.ecse.flexibook.controller.TOService;

import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Font;

public class ServicePage extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JLabel errorMessage;

	private JPanel contentPane;
	private JTextField newNameTextField;
	private JTextField newDurationTextField;
	private JTextField newDStartTextField;
	private JTextField newDDurationTextField;
	private JPanel AccSettings;
	private JPanel DeleteService;
	private JTextField serviceNameTextField;
	private JTextField serviceDurationTextField;
	private JTextField serviceDStartTextField;
	private JTextField serviceDDurationTextField;
	private JLabel serviceDurationLabel;
	private JButton updateServiceBtn;
	private JComboBox<String> serviceToUpdateList;
	private JLabel newNameLabel;
	private JLabel selectServiceUpdateLabel;
	private JButton addAServiceBtn;
	private JButton updateAServiceBtn;
	private JButton deleteAServiceBtn;
	private JLabel serviceNameLabel;
	private JLabel serviceDStartLabel;
	private JLabel serviceDDurationLabel;
	private JButton addServiceBtn;
	private JLabel defaultLabel;
	private JLabel selectServiceDeleteLabel;
	private JComboBox<String> serviceToDeleteList;
	private JButton deleteServiceBtn;
	private JLabel newDurationLabel;
	private JLabel newDStartLabel;
	private JLabel newDDurationLabel;
	private JPanel UpdateService;
	private JPanel AddService;
	private JLayeredPane layeredPane;
	private JButton accSettingsBtn;
	private JButton backBtn;

	private JLabel errorMsg;
	private JLabel errorMsgDelete;
	private JLabel errorMsgUpdate;


	// Data elements
	private String errorAdd = null;
	private String errorUpdate = null;
	private String errorDelete = null;
	private String successAdd = null;
	private String successUpdate = null;
	private String successDelete = null;
	// Service List
	private HashMap<Integer, String> services;
	private JLabel successAddLabel;
	private JLabel successDeleteLabel;
	private JLabel successUpdateLabel;




	/** Creates new form ServicePage */
	/**
	 * @author juliettedebray
	 */
	public ServicePage() {
		initComponents();
		refreshData();
	}

	// Initialize the Service page's components
	/**
	 * @author juliettedebray
	 */
	private void initComponents() {


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(450, 200, 254, 23);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setPreferredSize(new Dimension(500, 250));
		contentPane.setLayout(null);

		layeredPane = new JLayeredPane();
		layeredPane.setBounds(6, 64, 471, 147);
		contentPane.add(layeredPane);
		layeredPane.setLayout(new CardLayout(0, 0));

		AddService = new JPanel();
		layeredPane.add(AddService, "name_2856209725978647");
		AddService.setLayout(null);

		serviceNameLabel = new JLabel("Service Name :");
		serviceNameLabel.setBounds(6, 6, 92, 16);
		AddService.add(serviceNameLabel);

		serviceNameTextField = new JTextField();
		serviceNameTextField.setBounds(6, 25, 149, 26);
		serviceNameTextField.setColumns(10);
		AddService.add(serviceNameTextField);

		serviceDurationLabel = new JLabel("Service Duration :");
		serviceDurationLabel.setBounds(6, 63, 111, 16);
		AddService.add(serviceDurationLabel);

		serviceDurationTextField = new JTextField();
		serviceDurationTextField.setBounds(6, 80, 149, 26);
		serviceDurationTextField.setColumns(10);
		AddService.add(serviceDurationTextField);

		serviceDStartLabel = new JLabel("Downtime Start :");
		serviceDStartLabel.setBounds(162, 63, 137, 16);
		AddService.add(serviceDStartLabel);

		serviceDStartTextField = new JTextField();
		serviceDStartTextField.setBounds(161, 80, 149, 26);
		serviceDStartTextField.setColumns(10);
		AddService.add(serviceDStartTextField);

		serviceDDurationLabel = new JLabel("Downtime Duration:");
		serviceDDurationLabel.setBounds(316, 63, 165, 16);
		AddService.add(serviceDDurationLabel);

		serviceDDurationTextField = new JTextField();
		serviceDDurationTextField.setBounds(316, 80, 149, 26);
		serviceDDurationTextField.setColumns(10);
		AddService.add(serviceDDurationTextField);

		addServiceBtn = new JButton("Add Service");
		addServiceBtn.setBounds(6, 118, 459, 29);
		AddService.add(addServiceBtn);

		// error message
		errorMsg = new JLabel("");
		errorMsg.setBounds(16, 103, 449, 16);
		errorMsg.setForeground(Color.RED);
		AddService.add(errorMsg);

		successAddLabel = new JLabel("");
		successAddLabel.setBounds(16, 103, 449, 16);
		successAddLabel.setForeground(new Color(50, 205, 50));
		AddService.add(successAddLabel);

		UpdateService = new JPanel();
		layeredPane.add(UpdateService, "name_2856855122899510");
		UpdateService.setLayout(null);

		newNameLabel = new JLabel("New Name :");
		newNameLabel.setBounds(161, 6, 92, 16);
		UpdateService.add(newNameLabel);

		newNameTextField = new JTextField();
		newNameTextField.setBounds(161, 25, 149, 26);
		newNameTextField.setColumns(10);
		UpdateService.add(newNameTextField);

		newDurationLabel = new JLabel("New Duration :");
		newDurationLabel.setBounds(6, 63, 111, 16);
		UpdateService.add(newDurationLabel);

		newDurationTextField = new JTextField();
		newDurationTextField.setBounds(6, 80, 149, 26);
		newDurationTextField.setColumns(10);
		UpdateService.add(newDurationTextField);

		newDStartTextField = new JTextField();
		newDStartTextField.setBounds(161, 80, 149, 26);
		newDStartTextField.setColumns(10);
		UpdateService.add(newDStartTextField);

		newDStartLabel = new JLabel("New Downtime Start :");
		newDStartLabel.setBounds(162, 63, 137, 16);
		UpdateService.add(newDStartLabel);

		newDDurationLabel = new JLabel("New Downtime Duration:");
		newDDurationLabel.setBounds(311, 63, 165, 16);
		UpdateService.add(newDDurationLabel);

		newDDurationTextField = new JTextField();
		newDDurationTextField.setBounds(316, 80, 149, 26);
		newDDurationTextField.setColumns(10);
		UpdateService.add(newDDurationTextField);

		selectServiceUpdateLabel = new JLabel("Select Service :");
		selectServiceUpdateLabel.setBounds(6, 6, 143, 16);
		UpdateService.add(selectServiceUpdateLabel);

		serviceToUpdateList = new JComboBox<String>();
		serviceToUpdateList.setBounds(6, 26, 149, 27);
		UpdateService.add(serviceToUpdateList);

		updateServiceBtn = new JButton("Update Service");
		updateServiceBtn.setBounds(6, 118, 459, 29);
		UpdateService.add(updateServiceBtn);

		errorMsgUpdate = new JLabel("");
		errorMsgUpdate.setBounds(16, 104, 449, 16);
		errorMsgUpdate.setForeground(Color.RED);
		UpdateService.add(errorMsgUpdate);

		successUpdateLabel = new JLabel("");
		successUpdateLabel.setBounds(16, 104, 449, 16);
		successUpdateLabel.setForeground(new Color(50, 205, 50));
		UpdateService.add(successUpdateLabel);

		DeleteService = new JPanel();
		layeredPane.add(DeleteService, "name_2857718514846943");
		DeleteService.setLayout(null);

		selectServiceDeleteLabel = new JLabel("Select a Service :");
		selectServiceDeleteLabel.setBounds(161, 18, 149, 16);
		DeleteService.add(selectServiceDeleteLabel);

		serviceToDeleteList = new JComboBox<String>();
		serviceToDeleteList.setBounds(161, 52, 149, 27);
		DeleteService.add(serviceToDeleteList);

		deleteServiceBtn = new JButton("Delete Service");
		deleteServiceBtn.setBounds(161, 97, 149, 29);
		DeleteService.add(deleteServiceBtn);

		errorMsgDelete = new JLabel("");
		errorMsgDelete.setBounds(109, 81, 253, 16);
		errorMsgDelete.setForeground(Color.RED);
		DeleteService.add(errorMsgDelete);

		successDeleteLabel = new JLabel("");
		successDeleteLabel.setBounds(146, 81, 184, 16);
		successDeleteLabel.setForeground(new Color(50, 205, 50));
		DeleteService.add(successDeleteLabel);

		AccSettings = new JPanel();
		layeredPane.add(AccSettings, "name_2859958996592658");
		AccSettings.setLayout(null);

		defaultLabel = new JLabel("Please select an action to perform");
		defaultLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 16));
		defaultLabel.setBounds(98, 65, 274, 16);
		AccSettings.add(defaultLabel);

		addAServiceBtn = new JButton("Add a Service");
		addAServiceBtn.setBounds(7, 23, 151, 29);
		contentPane.add(addAServiceBtn);

		updateAServiceBtn = new JButton("Update a Service");
		updateAServiceBtn.setBounds(165, 23, 151, 29);
		contentPane.add(updateAServiceBtn);

		deleteAServiceBtn = new JButton("Delete a Service");
		deleteAServiceBtn.setBounds(323, 23, 151, 29);
		contentPane.add(deleteAServiceBtn);

		accSettingsBtn = new JButton("...");
		accSettingsBtn.setBounds(427, 0, 70, 29);
		contentPane.add(accSettingsBtn);

		backBtn = new JButton("Back");
		backBtn.setBounds(323, 0, 70, 29);
		contentPane.add(backBtn);

		JLabel lblNewLabel = new JLabel("Service Manager");
		lblNewLabel.setBounds(6, 5, 152, 16);
		contentPane.add(lblNewLabel);

		// Listeners for the layered frame
		addAServiceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(AddService);
				layeredPane.repaint();
				layeredPane.revalidate();
				successAddLabel.setText("");
				successDeleteLabel.setText("");
				successUpdateLabel.setText("");
				errorMsg.setText("");
				errorMsgDelete.setText("");
				errorMsgUpdate.setText("");
			}
		});
		deleteAServiceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(DeleteService);
				layeredPane.repaint();
				layeredPane.revalidate();
				successAddLabel.setText("");
				successDeleteLabel.setText("");
				successUpdateLabel.setText("");
				errorMsg.setText("");
				errorMsgDelete.setText("");
				errorMsgUpdate.setText("");
			}
		});
		updateAServiceBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				layeredPane.removeAll();
				layeredPane.add(UpdateService);
				layeredPane.repaint();
				layeredPane.revalidate();
				successAddLabel.setText("");
				successDeleteLabel.setText("");
				successUpdateLabel.setText("");
				errorMsg.setText("");
				errorMsgDelete.setText("");
				errorMsgUpdate.setText("");
			}
		});

		//listeners for account settings button
		accSettingsBtn.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				accSettingsBtnActionPerfomed(evt);
			}
		});


		// Listener for adding a service
		addServiceBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				addServiceButtonActionPerformed(evt);
			}
		});

		// Listener for updating a service
		updateServiceBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				updateServiceButtonActionPerformed(evt);
			}
		});

		// Listener for deleting a service
		deleteServiceBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				deleteServiceButtonActionPerformed(evt);
			}
		});

		// Listener for back button
		backBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				backButtonActionPerformed(evt);
			}
		});

		pack();
	}



	// Refresh the page's data
	/**
	 * @author juliettedebray
	 */
	private void refreshData() {
		// error
		errorMsg.setText(errorAdd);
		errorMsgUpdate.setText(errorUpdate);
		errorMsgDelete.setText(errorDelete);

		successAddLabel.setText(successAdd);
		successUpdateLabel.setText(successUpdate);
		successDeleteLabel.setText(successDelete);

		if ((errorAdd == null || errorAdd.length() == 0)&&(errorUpdate == null || errorUpdate.length() == 0)
				&&(errorDelete == null || errorDelete.length() == 0)) {
			// populate page with data

			// resetting the text fields
			serviceNameTextField.setText("");
			newNameTextField.setText("");

			newDurationTextField.setText("");
			serviceDurationTextField.setText("");

			newDDurationTextField.setText("");
			serviceDDurationTextField.setText("");

			newDStartTextField.setText("");
			serviceDStartTextField.setText("");

			// update service
			services = new HashMap<Integer, String>();
			serviceToUpdateList.removeAllItems();
			Integer index = 0;
			for (TOService service : FlexiBookController.getServices()) {
				services.put(index, service.getName());
				serviceToUpdateList.addItem(service.getName() + ": " + 
						service.getDuration() + ", " + 
						service.getDowntimeDuration() + ", " +
						service.getDowntimeStart());
				index++;
			};
			serviceToUpdateList.setSelectedIndex(-1);

			// delete service
			serviceToDeleteList.removeAllItems();
			index = 0;
			for (TOService service : FlexiBookController.getServices()) {
				services.put(index, service.getName());
				serviceToDeleteList.addItem(service.getName()+ ": " + 
						service.getDuration() + ", " + 
						service.getDowntimeDuration() + ", " +
						service.getDowntimeStart());
				index++;
			};
		}
		serviceToDeleteList.setSelectedIndex(-1);

		pack();

	}

	// Button that adds a service by checking that the inputs are valid
	// and caalling the controller
	/**
	 * @author juliettedebray
	 */
	private void addServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {
		UIManager.put("OptionPane.yesButtonText", "Yes");
		UIManager.put("OptionPane.noButtonText", "No");

		// clear error message
		errorAdd = "";
		successAdd = "";
		if (serviceNameTextField.getText().compareToIgnoreCase("")==0)
			errorAdd = "Please input a service name.";

		if (errorAdd.length()==0) {
			try {
				int a = Integer.parseInt(serviceDurationTextField.getText());
				int b = Integer.parseInt(serviceDStartTextField.getText());
				int c = Integer.parseInt(serviceDDurationTextField.getText());
			} catch (NumberFormatException e) {
				errorAdd = "Input must be a valid number.";
			}
		}

		if (errorAdd.length() == 0) {
			int response = JOptionPane.showConfirmDialog(null, "Add service?", "Swing Tester", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				// call the controller
				try {
					FlexiBookController.addService("owner", serviceNameTextField.getText(),
							serviceDurationTextField.getText(), serviceDDurationTextField.getText(), serviceDStartTextField.getText());
					successAdd = "Service added successfully.";
				} catch (InvalidInputException e) {
					errorAdd = e.getMessage();
					//errorMsg.setText(error);
				}
			}
		}
		// update visuals
		refreshData();
	}


	// Opens a "settings" popup when the settings button is pressed
	/**
	 * @author antoninguerre
	 */
	private void accSettingsBtnActionPerfomed(ActionEvent evt) {
		UIManager.put("OptionPane.cancelButtonText", "Cancel");
		UIManager.put("OptionPane.yesButtonText", "Logout");
		UIManager.put("OptionPane.noButtonText", "Account Settings");
		int dialogButton = JOptionPane.YES_NO_CANCEL_OPTION;
		int dialogResult = JOptionPane.showConfirmDialog(null, null,"Settings",dialogButton, JOptionPane.PLAIN_MESSAGE);

		if (dialogResult == JOptionPane.YES_OPTION) {			
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

	
	// Button that updates a service by checking that all the inputs are valid
	// and calling the controller
	/**
	 * @author juliettedebray
	 */
	private void updateServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message and basic input validation
		errorUpdate = "";
		successUpdate = "";
		int selectedService = serviceToUpdateList.getSelectedIndex();

		if (selectedService < 0)
			errorUpdate = "Service needs to be selected to update!";

		if (errorUpdate.length() == 0) {
			try {
				int a = Integer.parseInt(newDurationTextField.getText());
				int b = Integer.parseInt(newDStartTextField.getText());
				int c = Integer.parseInt(newDDurationTextField.getText());
			} catch (NumberFormatException e) {
				errorUpdate = "Input must be a number.";
			}
		}

		if (errorUpdate.length() == 0) {
			UIManager.put("OptionPane.yesButtonText", "Update");
			UIManager.put("OptionPane.noButtonText", "Cancel");
			int response = JOptionPane.showConfirmDialog(null, "Update service?", "Swing Tester", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				// call the controller
				try {
					FlexiBookController.updateService("owner", services.get(selectedService),
							newNameTextField.getText(), newDurationTextField.getText(), newDDurationTextField.getText(), newDStartTextField.getText());
					successUpdate = "Service updated successfully.";
				} catch (InvalidInputException e) {
					errorUpdate = e.getMessage();
				}
			}
		}
		// update visuals
		refreshData();
	}

	
	// Button that deletes a selected service
	/**
	 * @author juliettedebray
	 */
	private void deleteServiceButtonActionPerformed(java.awt.event.ActionEvent evt) {
		// clear error message and basic input validation
		errorDelete = "";
		successDelete = "";
		int selectedService = serviceToDeleteList.getSelectedIndex();
		if (selectedService < 0)
			errorDelete = "Service needs to be selected to delete!";

		if (errorDelete.length() == 0) {
			UIManager.put("OptionPane.yesButtonText", "Delete");
			UIManager.put("OptionPane.noButtonText", "Cancel");
			int response = JOptionPane.showConfirmDialog(null, "Delete service?", "Swing Tester", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (response == JOptionPane.YES_OPTION) {
				// call the controller
				try {
					FlexiBookController.deleteService("owner", services.get(selectedService));
					successDelete = "Service deleted successfully.";
				} catch (InvalidInputException e) {
					errorDelete = e.getMessage();
				}
			}
		}
		// update visuals
		refreshData();
	}

	
	// Button that brings the owner back to the welcome page
	/**
	 * @author antoninguerre
	 */
	private void backButtonActionPerformed(ActionEvent evt) {
		this.setVisible(false);
		new OwnerWelcomePage().setVisible(true);

	}
}