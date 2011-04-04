package com.googlemail.christian667.cWatchTheHamsterClient;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import javax.imageio.ImageIO;

/**
 * The cWatchTheHamsterClient
 * 
 * @author christian667@googlemail.com
 * 
 */
public class CWatchTheHamsterClient implements ITimerCallback {

	public final boolean DEBUG;
	public final static String Version = "Version 1.0 rc7 (02/04/2011)";

	// The constants
	private static final int BUFFERSIZE = 200000;
	private static final int CHUNKSIZE = 1200;
	private static final int MAXIMAGESIZE = 50000;
	private static final short MINIMAGESIZE = 1000;
	private static final short SLEEPTIME = 5;
	private static final short AUTHENTICATIONTIMEOUT = 10;
	private static final short SOCKETTIMEOUT = 5000;

	// The privates
	private byte[] imageSizeByte;
	private BufferedInputStream byteInData;
	private BufferedOutputStream byteOutData;
	private Socket socket;
	private short port;
	private String serverAddress;
	private String username;
	private String password;
	private short width;
	private short height;
	private byte fps;
	private KeepAliveController liveControl;
	private IcHamsterGUI gUI;
	private boolean stop = false;
	private ByteArrayOutputStream tmpStream;
	private byte[] chunk;
	private byte deviceNumber = 0;

	// Set by the server
	private byte connectionTimeOut = 1;
	private byte numberOfDevices = 1;

	/**
	 * @param gUI
	 * @param options
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public CWatchTheHamsterClient(IcHamsterGUI gUI, String[] options)
			throws UnknownHostException, IOException {
		this.gUI = gUI;

		// Parse the arguments
		this.username = options[1];
		this.password = options[3];
		this.serverAddress = options[5];
		this.port = Short.valueOf(options[7]);
		this.width = Short.valueOf(options[9].split("x")[0]);
		this.height = Short.valueOf(options[9].split("x")[1]);
		this.fps = Byte.valueOf(options[11]);
		if (options.length > 12)
			this.deviceNumber = Byte.valueOf(options[13]);
		this.gUI.setBackend(this);
		this.tmpStream = new ByteArrayOutputStream();
		this.chunk = new byte[CHUNKSIZE];
		this.imageSizeByte = new byte[4];

		// search for verbose
		if (options.length > 14 && options[14].contains("--verbose"))
			this.DEBUG = true;
		else
			this.DEBUG = false;

		// Build socket
		SocketAddress sockaddr = new InetSocketAddress(this.serverAddress,
				this.port);
		this.socket = new Socket();

		// Connect with timeout
		try {
			socket.connect(sockaddr, SOCKETTIMEOUT);
		} catch (Exception e) {
			this.kill();
		}

		// If connection established, go on
		if (!this.stop && this.socket.isConnected()) {
			// Start the timer for authentication
			Timer timer = new Timer(AUTHENTICATIONTIMEOUT, this);

			// Initializise the byte ready/writer
			this.byteOutData = new BufferedOutputStream(
					this.socket.getOutputStream());
			this.byteInData = new BufferedInputStream(
					this.socket.getInputStream(), BUFFERSIZE);

			// Set the debug string
			String debug = null;
			if (this.DEBUG)
				debug = "cWTH-Client";

			// Authenticate
			if (HamsterToolkit.clientAuthentication(this.socket, debug,
					this.username, this.password)) {
				if (this.DEBUG)
					HamsterToolkit.debug("cWTH-Client",
							"Successful authenticated");
				// Disarm the timer, authentication completed
				timer.disarm();
				// Read connectionConfiguration
				this.readServerConfiguration();
				// And start receiving images
				this.displayImages();
			} else {
				if (this.DEBUG)
					HamsterToolkit.debug("cWTH-Client",
							"Authentication FAILURE!");
				// Kill the client, authentication failed
				this.kill();
				// Disarm, not needed anymore
				timer.disarm();
			}
		} else
			// Socket connection failed
			this.kill();
	}

	private void readServerConfiguration() {
		byte[] config = new byte[2];
		// Read the 2 bytes configuration send by the server
		try {
			this.byteInData.read(config, 0, config.length);
		} catch (IOException e) {
			this.kill();
		}
		// Set the configuration values
		this.connectionTimeOut = config[0];
		this.numberOfDevices = config[1];
	}

	private void displayImages() {
		// Start the keepalive sender
		this.liveControl = new KeepAliveController(this);

		// Get image after image and set it to the gui
		while (!this.stop) {
			BufferedImage bufIm = this.getNextImage(); // Next image
			if (bufIm != null)
				this.gUI.setImage(bufIm); // And set it
		}
		// Cliend stopped
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Client", "Client closed");
	}

	private BufferedImage getNextImage() {
		// Get the size of the next image from the server
		try {
			this.byteInData.read(this.imageSizeByte);
		} catch (IOException e) {
			this.kill();
		}
		// Set the size
		int imageSize = HamsterToolkit.unsignedIntByteArrayToInt(imageSizeByte);

		// Check if the size fits the limits
		if (imageSize > MINIMAGESIZE && imageSize < MAXIMAGESIZE && !this.stop) {
			// Reset the byteArray stream
			this.tmpStream.reset();

			// First read the chunks and then read the rest
			try {
				// Read chunks
				while (this.tmpStream.size() < imageSize
						- (imageSize % this.chunk.length)) {
					// Wait for chunk available
					while (this.byteInData.available() < this.chunk.length)
						sleep();
					// Read chunk
					this.byteInData.read(this.chunk, 0, this.chunk.length);
					// Write to tmpStream
					this.tmpStream.write(chunk, 0, this.chunk.length);
				}

				// Read the remaining bytes for the image
				if (imageSize % this.chunk.length > 0) {
					// Malloc byte array in size of the last bytes
					byte[] lastBytes = new byte[imageSize % this.chunk.length];
					// Wait for last bytes available
					while (this.byteInData.available() < lastBytes.length)
						sleep();
					// Read last bytes
					this.byteInData.read(lastBytes, 0, lastBytes.length);
					// Write to tmpStream
					this.tmpStream.write(lastBytes, 0, lastBytes.length);
				}

				// flush stream
				this.tmpStream.flush();

				// Write to BufferedImage and return
				return ImageIO.read(new ByteArrayInputStream(this.tmpStream
						.toByteArray()));
			} catch (IOException e) {
				// IO killed
				this.kill();
			}
		} // Image out of the limits
		return null;
	}

	private void sleep() {
		try {
			Thread.sleep(SLEEPTIME);
		} catch (InterruptedException e) {
			this.kill();
		}
	}

	public void kill() {
		if (!this.stop) { // Kill everything and close the socket
			this.stop = true;
			synchronized (this) {
				this.notifyAll();
			}
			if (this.liveControl != null)
				this.liveControl.kill();
			try {
				if (this.byteInData != null)
					this.byteInData.close();
				if (this.byteOutData != null)
					this.byteOutData.close();
				if (this.socket != null) {
					this.socket.shutdownInput();
					this.socket.shutdownOutput();
					this.socket.close();
				}
			} catch (IOException e) {
			}
		}
		// Notify the gui to close
		this.gUI.connectionClosedByServer();
	}

	public void setControlBytes(byte[] controlBytes) {
		if (this.liveControl != null)
			this.liveControl.setControlBytes(controlBytes);
	}

	public short getWidth() {
		return width;
	}

	public short getHeight() {
		return height;
	}

	public byte getFps() {
		return this.fps;
	}

	public void setFps(byte fps) {
		this.fps = fps;
	}

	public BufferedOutputStream getByteOutData() {
		return byteOutData;
	}

	public short getConnectionTimeOut() {
		return connectionTimeOut;
	}

	@Override
	public void timedOut() {
		this.kill();
	}

	public byte getNumberOfDevices() {
		return numberOfDevices;
	}

	public byte getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(byte deviceNumber) {
		// Gui request for other device, check limits
		if (deviceNumber < this.numberOfDevices && deviceNumber > -1) {
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Client", "Device changed to "
						+ deviceNumber);
			this.deviceNumber = deviceNumber;
		}
	}
}
