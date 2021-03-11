package ca.mcgill.ecse.flexibook.view;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.sql.*;
import java.time.LocalDate;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Properties;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import  ca.mcgill.ecse.flexibook.application.FlexiBookApplication;
import ca.mcgill.ecse.flexibook.controller.*;

public class AppointmentCalendar extends JFrame{
	
	
	private static final long serialVersionUID = -4426310869335015542L;
	
	//logout button
	private JButton logout;
	
	//appointment calendar
	private JTable appointmentCalendar;
	private JScrollPane calendarScrollPane;
	private JLabel errorMessage;
	private DefaultTableModel calendarDtm;
	private JDatePickerImpl calendarDatePicker;
	private JLabel calendarDateLabel;
	private String columnNames[] = {"Date","StartTime","EndTime", "Available/Busy"};
	private static final int HEIGHT_CALENDAR_TABLE = 100;
	
	private String error = null;
	
	
	// Create the appointment calendar page
	/**
	 * @author lukasalatalo
	 */
	public AppointmentCalendar() {
		initComponents();
		refreshData();
		refreshCalendar();
	}
	
	
	//Initialize the page's components
	/**
	 * @author lukasalatalo
	 */
	private void initComponents() {
		errorMessage = new JLabel();
		errorMessage.setForeground(Color.RED);
		
		//logout
		logout = new JButton();
		logout.setText("Back");
		logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		//appointment calendar
		SqlDateModel calendarModel = new SqlDateModel();
		LocalDate now = LocalDate.now();
		calendarModel.setDate(now.getYear(),now.getMonthValue()-1,now.getDayOfMonth());
		calendarModel.setSelected(true);
		Properties pO = new Properties();
		pO.put("text.today","Today");
		pO.put("text.month", "Month");
		pO.put("text.year", "Year");
		JDatePanelImpl calendarDatePanel = new JDatePanelImpl(calendarModel, pO);
		calendarDatePicker = new JDatePickerImpl(calendarDatePanel, new DateLabelFormatter());
		calendarDateLabel = new JLabel();
		calendarDateLabel.setText("Choose Week/Date:");
		
		appointmentCalendar = new JTable() {
		    private static final long serialVersionUID = 1L;
		    @Override
		    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		    	 Component c = super.prepareRenderer(renderer, row, column);
		         if (!c.getBackground().equals(getSelectionBackground())) {
		             Object obj = getModel().getValueAt(row, column);
		             if (obj instanceof java.lang.String) {
		                 String str = (String)obj;
		                 c.setBackground(str.equals("Busy") ? Color.RED : 
		                     str.equals("Available") ? Color.GREEN : Color.WHITE);
		             }
		             else {
		                 c.setBackground(Color.WHITE);
		             }
		         }
		         return c;
		    }
		};
		
		calendarScrollPane = new JScrollPane(appointmentCalendar);
		Dimension d = calendarScrollPane.getPreferredSize();
		calendarScrollPane.setPreferredSize(new Dimension(d.width, HEIGHT_CALENDAR_TABLE));
		calendarScrollPane.setVerticalScrollBarPolicy(
		    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		setTitle("Appointment Calendar");
		
		calendarDatePicker.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				refreshCalendar();
			}
		});
		
		logout.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				logoutActionPerformed(evt);
			}
		});
		//horizontal line elements
		JSeparator horizontalLineTop = new JSeparator();
		
		
		//layout
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);
		layout.setHorizontalGroup(
				layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup()
						.addComponent(errorMessage)
						.addComponent(horizontalLineTop)
						.addComponent(calendarScrollPane)
						.addGroup(layout.createSequentialGroup()
								.addGroup(layout.createParallelGroup()
										.addComponent(calendarDateLabel))
								.addGroup(layout.createParallelGroup()
										.addComponent(calendarDatePicker)))
						.addGroup(layout.createParallelGroup()
								.addComponent(logout))));
		
		layout.linkSize(SwingConstants.HORIZONTAL, new java.awt.Component[] 
				{calendarDatePicker});
		
		layout.setVerticalGroup(
				layout.createParallelGroup()
				.addGroup(layout.createSequentialGroup()
						.addComponent(logout)
						.addComponent(horizontalLineTop)
						.addComponent(errorMessage)
						.addGroup(layout.createParallelGroup()
									.addComponent(calendarDateLabel)
									.addComponent(calendarDatePicker))
						.addComponent(calendarScrollPane))

						);
		pack();
	}
	
	
	// Refresh the page's data
	/**
	 * @author lukasalatalo
	 */
	private void refreshData() {
		//error
		errorMessage.setText(error);
		refreshCalendar();
		pack();
			
	}
	
	
	// Refresh the appointment calendar
	/**
	 * @author lukasalatalo
	 */
	private void refreshCalendar() {
		calendarDtm = new DefaultTableModel(0, 0);
		calendarDtm.setColumnIdentifiers(columnNames);
		appointmentCalendar.setModel(calendarDtm);
		if (calendarDatePicker.getModel().getValue()!=null) {
			ArrayList<TOAppointmentCalender> appointments = (ArrayList) FlexiBookController.viewAppointmentCalender((Date) 
					calendarDatePicker.getModel().getValue());
			if (appointments!=null) {
				for (TOAppointmentCalender item: appointments){
					String date = item.getDate();
					String startTime = item.getStartTime();
					String endTime = item.getEndTime();
					String busy = null;
					if (item.getIsAvailable()) {
						busy= "Available";
					}
					else {
						busy="Busy";
					}
					String[] obj = {date, startTime, endTime, busy};
					calendarDtm.addRow(obj);
				}
			}
		}
		Dimension d = appointmentCalendar.getPreferredSize();
		calendarScrollPane.setPreferredSize(new Dimension(d.width, HEIGHT_CALENDAR_TABLE));
	}
	
	
	// Button that closes the view calendar page
	/**
	 * @author lukasalatalo
	 */
	private void logoutActionPerformed(java.awt.event.ActionEvent evt) {
		this.setVisible(false);
	}

	
}