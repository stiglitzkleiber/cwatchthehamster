package com.googlemail.christian667.cWatchTheHamster;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

import au.edu.jcu.v4l4j.V4L4JConstants;

public class ConfigurationHolder {

	// Default port
	private short port = 6666;
	// Size set by client
	private short width = 320;
	private short height = 240;
	// Webcam type
	private short std = V4L4JConstants.STANDARD_WEBCAM;
	// Channel
	private byte channel = 0;
	// JPEG quality in percent
	private byte qty = 60;
	// Sleeptime between connections
	private short connectionBreak = 10;
	// Timeout for authentication process
	private short authenticationTimeOut = 10;
	// Client timeout
	private byte connectionTimeOut = 5;
	// The protocolsign - it's pi ;)
	private final byte[] protocolSign = { 3, 1, 4, 1 };
	// Changes made by client?!
	private boolean changesMade = false;
	// Max image size
	private int maxImageSize = 50000;
	// Min image size
	private short minImageSize = 1000;
	// Network to slow - threshold to react
	private byte slowDownThreshold = 3;
	// Minimum sleeptime between taking two pictures
	private short minimumSleeptime = 3;
	// The configuration
	private Properties configFile;
	// Maximum amount of clients
	private short maxClients = 10;
	// Maximum number of failed login attemps
	private short blacklistThreshold = 3;
	// Login tuple - username/password;username/pass...
	private HashMap<String, String> logins;
	// Array holding the local video devices /dev/videoXX
	private String[] devices;
	// Array holding the fps, depending on the devices
	private byte[] fps;

	public ConfigurationHolder() {
		// Init the properties
		File fileCheck = new File("cHamster.config");
		this.configFile = new Properties();
		// The login tuple map
		this.logins = new HashMap<String, String>();
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
			// Load everything
			this.port = Short.valueOf(this.configFile.getProperty("Port"));
			this.devices = this.configFile.getProperty(
					"Comma_separate_video_devices").split(",");
			this.maxImageSize = Integer.valueOf(this.configFile
					.getProperty("Maximum_image_size_in_bytes"));
			this.minImageSize = Short.valueOf(this.configFile
					.getProperty("Minimum_image_size_in_bytes"));
			this.qty = Byte.valueOf(this.configFile.getProperty("JPG_Quality"));
			this.connectionBreak = Short.valueOf(this.configFile
					.getProperty("Sleep_in_s_between_connections"));
			this.connectionTimeOut = Byte.valueOf(this.configFile
					.getProperty("Clients_timeout_in_s"));
			this.authenticationTimeOut = Short.valueOf(this.configFile
					.getProperty("Authentication_timeout_in_s"));
			this.slowDownThreshold = Byte.valueOf(this.configFile
					.getProperty("Slow-Down-Threshold"));
			this.minimumSleeptime = Short.valueOf(this.configFile
					.getProperty("Minimum_sleep_between_pictures_in_ms"));
			this.maxClients = Short.valueOf(this.configFile
					.getProperty("Maximum_clients_allowed_to_connect"));
			this.blacklistThreshold = Short
					.valueOf(this.configFile
							.getProperty("Maximum_failed_authentication_attemps_to_blacklist"));
			this.loadLoginsString(this.configFile
					.getProperty("USERNAME/PASSWORD;USERNAME/PASS..."));
		} else {
			// Store default values in new file
			this.configFile.setProperty("Port", String.valueOf(this.port));
			this.configFile.setProperty("Comma_separate_video_devices",
					"/dev/video0,");
			this.devices = "/dev/video0,".split(",");
			this.configFile.setProperty("Maximum_image_size_in_bytes",
					String.valueOf(this.maxImageSize));
			this.configFile.setProperty("Minimum_image_size_in_bytes",
					String.valueOf(this.minImageSize));
			this.configFile
					.setProperty("JPG_Quality", String.valueOf(this.qty));
			this.configFile.setProperty("Sleep_in_s_between_connections",
					String.valueOf(this.connectionBreak));
			this.configFile.setProperty("Clients_timeout_in_s",
					String.valueOf(this.connectionTimeOut));
			this.configFile.setProperty("Authentication_timeout_in_s",
					String.valueOf(this.authenticationTimeOut));
			this.configFile.setProperty("Slow-Down-Threshold",
					String.valueOf(this.slowDownThreshold));
			this.configFile.setProperty("Minimum_sleep_between_pictures_in_ms",
					String.valueOf(this.minimumSleeptime));
			this.configFile.setProperty("Maximum_clients_allowed_to_connect",
					String.valueOf(this.maxClients));
			this.configFile.setProperty(
					"Maximum_failed_authentication_attemps_to_blacklist",
					String.valueOf(this.blacklistThreshold));
			this.configFile.setProperty("USERNAME/PASSWORD;USERNAME/PASS...",
					"big/brother;");
			this.loadLoginsString("big/brother;");
			try {
				String comments = "\t\t   cWatchTheHamster\n\t"
						+ "-----------------------------\n\n\t"
						+ "Copyright by Christian Wohlert\n\t"
						+ "   christian667@googlemail.com\n\n\t"
						+ "This are initial settings,\n\t"
						+ "the image size and FPS are\n\t"
						+ "influenced by the client.\n\n\t"
						+ "Have a look at the cHamster authentication!\n\t"
						+ "...and a lot of fun\n\n";
				this.configFile.store(new FileWriter(fileCheck), comments);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		// loading or storing doesn't matter, create fps list in size of devices
		this.fps = new byte[this.devices.length];
		for (int i = 0; i < this.fps.length; i++)
			this.fps[i] = 1;
	}

	private void loadLoginsString(String loginsString) {
		String[] loginsArray = loginsString.split(";");
		for (int i = 0; i < loginsArray.length; i++) {
			if (loginsArray[i].length() > 1)
				this.logins.put(loginsArray[i].split("/")[0],
						loginsArray[i].split("/")[1]);
		}
	}

	public short getPort() {
		return port;
	}

	public short getStd() {
		return std;
	}

	public byte getQty() {
		return qty;
	}

	public byte getChannel() {
		return channel;
	}

	public short getConnectionBreak() {
		return connectionBreak;
	}

	public short getAuthenticationTimeOut() {
		return authenticationTimeOut;
	}

	public void setWidth(short w) {
		this.width = w;
		this.changesMade = true;
	}

	public void setHeight(short h) {
		this.height = h;
		this.changesMade = true;
	}

	public short getWidth() {
		return width;
	}

	public short getHeight() {
		return height;
	}

	public byte getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public byte[] getProtocolSign() {
		return protocolSign;
	}

	public boolean isChangesMade() {
		if (this.changesMade) {
			this.changesMade = false;
			return true;
		} else
			return false;
	}

	public int getMaxImageSize() {
		return maxImageSize;
	}

	public short getMinImageSize() {
		return minImageSize;
	}

	public byte getSlowDownThreshold() {
		return slowDownThreshold;
	}

	public short getMinimumSleeptime() {
		return minimumSleeptime;
	}

	public short getMaxClients() {
		return maxClients;
	}

	public short getBlacklistThreshold() {
		return blacklistThreshold;
	}

	public HashMap<String, String> getLogins() {
		return logins;
	}

	public String[] getDevices() {
		return devices;
	}

	public void setFpsOfDevice(byte deviceNumber, byte fps) {
		if (deviceNumber < this.fps.length) {
			this.fps[deviceNumber] = fps;
			this.changesMade = true;
		}
	}

	public byte getFpsOfDevice(byte deviceNumber) {
		if (deviceNumber < this.fps.length)
			return this.fps[deviceNumber];
		else
			return 0;
	}
}
