package com.googlemail.christian667.cWatchTheHamster;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class HamsterClient implements ITimerCallback {

	private Socket socket;
	private byte fps = 1;
	private byte requestedFps;
	private short width = 0;
	private short height = 0;
	private DeviceController callback;
	private RXDataController rxData;
	private Timer timer;
	private BufferedInputStream byteInData;
	private BufferedOutputStream byteOutData;
	private boolean stop = false;
	private TXDataController txData;
	private ConfigurationHolder configH;
	private byte fpsNetworkCorrection = 0;
	private byte deviceNumber = 0;
	private final boolean DEBUG;

	public HamsterClient(Socket socket, ConfigurationHolder configH,
			DeviceController callback, boolean DEBUG) {
		this.DEBUG = DEBUG;
		this.socket = socket;
		this.callback = callback;
		this.configH = configH;

		// The readers and writers
		try {
			// Read and write buffered
			this.byteOutData = new BufferedOutputStream(
					this.socket.getOutputStream());
			this.byteInData = new BufferedInputStream(
					this.socket.getInputStream());
		} catch (IOException e) {
			this.kill();
		}

		// Send the connection configuration
		this.sendConnectionConfiguration();

		// The timer
		this.timer = new Timer(this.configH.getConnectionTimeOut(), this);

		// The rxDataController (Blocking until the first package received!)
		this.rxData = new RXDataController(this.timer, this.byteInData, this,
				this.DEBUG);

		// The imageSender
		this.txData = new TXDataController(this.byteOutData, this,
				this.configH, this.DEBUG);

	}

	public void sendImageByteArray(byte[] imageByteArray) {
		this.txData.addImageByteArray(imageByteArray);
		// Check for slow network
		if (this.txData.networkTooSlow()) {
			// Increase networkCorrection
			this.fpsNetworkCorrection++;
			// And decrease fps
			this.setFps(this.requestedFps);
			System.out.println("Network to slow, decreasing fps for "
					+ this.socket.getInetAddress().getHostAddress()
					+ " at device " + this.deviceNumber);
		} else // Decrease networkCorrection
		if (this.fpsNetworkCorrection > 0 && this.txData.queueSize() == 0) {
			System.out.println("Network fast enough, reincreasing fps for "
					+ this.socket.getInetAddress().getHostAddress()
					+ " at device " + this.deviceNumber);
			// Decrease networkCorrection
			this.fpsNetworkCorrection--;
			// And increase fps
			this.setFps(this.requestedFps);
		}
	}

	private void sendConnectionConfiguration() {
		byte[] config = new byte[2];
		config[0] = this.configH.getConnectionTimeOut();
		config[1] = (byte) this.configH.getDevices().length;
		try {
			this.byteOutData.write(config, 0, config.length);
			this.byteOutData.flush();
		} catch (IOException e) {
			this.kill();
		}
	}

	public void kill() {
		if (!this.stop) {
			this.stop = true;
			this.callback.clientDisconnected(this);
			synchronized (this) {
				this.notifyAll();
			}
			try {
				if (this.rxData != null)
					this.txData.kill();
				if (this.txData != null)
					this.rxData.kill();
				this.byteInData.close();
				this.byteOutData.close();
				this.socket.close();
			} catch (IOException e) {
			}
			// Disarm the timer, not needed anymore
			this.timer.disarm();
			System.out.println("\t\tClient "
					+ this.socket.getInetAddress().getHostAddress()
					+ " disconnected");
		}
	}

	public ConfigurationHolder getConfigH() {
		return configH;
	}

	public void setFps(byte fps) {
		if (!this.isKilled()) {
			this.requestedFps = fps;
			byte newFps = (byte) (fps - this.fpsNetworkCorrection);
			if (newFps < 1)
				newFps = 1;
			this.fps = newFps;
			System.out.println("Client "
					+ this.socket.getInetAddress().getHostAddress()
					+ " framerate set to " + this.fps + " fps");
			this.callback.clientChangedFps();
		}
	}

	public void setWidth(short width) {
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "Client "
					+ this.socket.getInetAddress().getHostAddress()
					+ " width received:" + width);
		this.width = width;
	}

	public void setHeight(short height) {
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "Client "
					+ this.socket.getInetAddress().getHostAddress()
					+ " height received:" + height);
		this.height = height;
	}

	@Override
	public void timedOut() {
		HamsterToolkit.debug("cWTH-Server", "Client "
				+ this.socket.getInetAddress().getHostAddress() + " timed out");
		this.kill();
	}

	public byte getFps() {
		return fps;
	}

	public short getWidth() {
		return width;
	}

	public short getHeight() {
		return height;
	}

	public void setDeviceNumber(byte deviceNumber) {
		// Switch only the device if the last switch is finished
		if (this.callback.getDeviceNumber() == this.deviceNumber) {
			// Switch only to a new device
			if (deviceNumber != this.deviceNumber) {
				// The new devicenumber, the target
				this.deviceNumber = deviceNumber;
				// Message the deviceController the switch
				this.callback.clientChangedDevice(this, deviceNumber);
			}
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setCallback(DeviceController callback) {
		this.callback = callback;
	}

	public boolean isKilled() {
		return this.stop;
	}

	public byte getDeviceNumber() {
		return deviceNumber;
	}
}
