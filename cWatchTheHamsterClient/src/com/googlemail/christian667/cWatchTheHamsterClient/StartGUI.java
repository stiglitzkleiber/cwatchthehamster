package com.googlemail.christian667.cWatchTheHamsterClient;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import javax.swing.WindowConstants;
import javax.swing.SwingUtilities;

public class StartGUI extends javax.swing.JFrame implements Runnable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5768584587065927794L;
	private JLabel passLabel;
	private JLabel serverLabel;
	private JTextField userField;
	private JButton cancelButton;
	private JButton loginButton;
	private JTextField fpsField;
	private JTextField resField;
	private JTextField portField;
	private JTextField serverField;
	private JPasswordField passwordField;
	private JLabel fpsLabel;
	private JLabel resolutionLabel;
	private JLabel portLabel;
	private JLabel userLabel;
	private String[] newArgs;
	private Image logoImage;
	private Properties configFile;
	private File fileCheck;

	public static void main(String[] args) {
		if (args.length > 0
				&& (args[0].contains("--version") || args[0].contains("--help")))
			SwingGUI.main(args);
		else if (args.length < 14) {
			System.out
					.println("\nusage:\t cWatchTheHamsterClient --username USER"
							+ " --password PASSWORD --server WWW.EXAMPLE.COM "
							+ "--port 6666 --resolution 320x240 --fps 1 --dev 0"
							+ " [--verbose]");

			StartGUI inst = new StartGUI();
			try {
				SwingUtilities.invokeAndWait(inst);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SwingGUI.main(inst.getNewArgs());
		} else
			SwingGUI.main(args);
	}

	public StartGUI() {
		super();
		this.logoImage = Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("/img/logo.png"));
	}

	public void run() {
		initGUI();
		this.setVisible(true);
	}

	private void initGUI() {
		try {
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			this.setTitle("cWatchTheHamster (" + CWatchTheHamsterClient.Version
					+ ")");
			this.setIconImage(this.logoImage);
			getContentPane().setLayout(null);
			{
				userLabel = new JLabel();
				getContentPane().add(userLabel);
				userLabel.setText("username");
				userLabel.setBounds(12, 15, 170, 15);
			}
			{
				passLabel = new JLabel();
				getContentPane().add(passLabel);
				passLabel.setText("password");
				passLabel.setBounds(12, 43, 170, 15);
			}
			{
				serverLabel = new JLabel();
				getContentPane().add(serverLabel);
				serverLabel.setText("server");
				serverLabel.setBounds(12, 71, 170, 15);
			}
			{
				portLabel = new JLabel();
				getContentPane().add(portLabel);
				portLabel.setText("port");
				portLabel.setBounds(12, 99, 170, 15);
			}
			{
				resolutionLabel = new JLabel();
				getContentPane().add(resolutionLabel);
				resolutionLabel.setText("resolution");
				resolutionLabel.setBounds(12, 127, 170, 15);
			}
			{
				fpsLabel = new JLabel();
				getContentPane().add(fpsLabel);
				fpsLabel.setText("frames per second");
				fpsLabel.setBounds(12, 155, 170, 15);
			}
			{
				userField = new JTextField();
				getContentPane().add(userField);
				userField.setBounds(194, 12, 255, 22);
			}
			{
				passwordField = new JPasswordField();
				getContentPane().add(passwordField);
				passwordField.setBounds(194, 40, 255, 22);
			}
			{
				serverField = new JTextField();
				getContentPane().add(serverField);
				serverField.setBounds(194, 68, 255, 22);
			}
			{
				portField = new JTextField();
				getContentPane().add(portField);
				portField.setBounds(194, 96, 255, 22);
			}
			{
				resField = new JTextField();
				getContentPane().add(resField);
				resField.setBounds(194, 124, 255, 22);
			}
			{
				fpsField = new JTextField();
				getContentPane().add(fpsField);
				fpsField.setBounds(194, 152, 255, 22);
			}
			{
				loginButton = new JButton();
				getContentPane().add(loginButton);
				loginButton.setText("login");
				loginButton.setBounds(305, 214, 144, 43);
				loginButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						loginButtonMouseClicked(evt);
					}
				});
			}
			{
				cancelButton = new JButton();
				getContentPane().add(cancelButton);
				cancelButton.setText("cancel");
				cancelButton.setBounds(12, 216, 143, 40);
				cancelButton.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						cancelButtonMouseClicked(evt);
					}
				});
			}
			this.loadConfig();
			pack();
			this.setSize(500, 300);
			this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width
					/ 2 - this.getSize().width / 2, Toolkit.getDefaultToolkit()
					.getScreenSize().height / 2 - this.getSize().height / 2);
		} catch (Exception e) {
			// add your error handling code here
			e.printStackTrace();
		}
	}

	private void loginButtonMouseClicked(MouseEvent evt) {
		newArgs = new String[14];
		newArgs[0] = "--username";
		newArgs[1] = this.userField.getText();
		newArgs[2] = "--password";
		newArgs[3] = new String(this.passwordField.getPassword());
		newArgs[4] = "--server";
		newArgs[5] = this.serverField.getText();
		newArgs[6] = "--port";
		newArgs[7] = this.portField.getText();
		newArgs[8] = "--resolution";
		newArgs[9] = this.resField.getText();
		newArgs[10] = "--fps";
		newArgs[11] = this.fpsField.getText();
		newArgs[12] = "--dev";
		newArgs[13] = String.valueOf("0");
		// Store the config
		this.storeConfig();
		this.dispose();
		synchronized (this) {
			this.notifyAll();
		}
	}

	private void loadConfig() {
		// check for file
		this.fileCheck = new File("cHamster.config");
		this.configFile = new Properties();
		// check for existing config
		if (fileCheck.exists()) {
			try {
				this.configFile.load(new FileReader(fileCheck));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Load configuration
			userField.setText(this.configFile.getProperty("username"));
			passwordField.setText(this.configFile.getProperty("password"));
			serverField.setText(this.configFile.getProperty("server"));
			portField.setText(this.configFile.getProperty("port"));
			resField.setText(this.configFile.getProperty("resolution"));
			fpsField.setText(this.configFile.getProperty("fps"));
		} else {
			// Create new default configuration and store it
			userField.setText("big");
			passwordField.setText("brother");
			serverField.setText("www.example.com");
			portField.setText("6666");
			resField.setText("320x240");
			fpsField.setText("5");
			this.storeConfig();
			// (Default values allready in the fields)
		}
	}

	private void storeConfig() {
		this.configFile.put("username", userField.getText());
		this.configFile
				.put("password", new String(passwordField.getPassword()));
		this.configFile.put("server", serverField.getText());
		this.configFile.put("port", portField.getText());
		this.configFile.put("resolution", resField.getText());
		this.configFile.put("fps", fpsField.getText());
		try {
			this.configFile.store(new FileWriter(this.fileCheck),
					"cWatchTheHamster Client");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void cancelButtonMouseClicked(MouseEvent evt) {
		this.dispose();
		System.exit(0);
	}

	public String[] getNewArgs() {
		synchronized (this) {
			try {
				wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return newArgs;
	}

}
