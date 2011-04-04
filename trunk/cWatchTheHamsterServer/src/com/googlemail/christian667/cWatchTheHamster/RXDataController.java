package com.googlemail.christian667.cWatchTheHamster;

import java.io.BufferedInputStream;
import java.io.IOException;

public class RXDataController extends Thread implements Runnable {

	private BufferedInputStream byteInData;
	private Timer timer;
	private HamsterClient client;
	private boolean stop = false;
	private byte[] rxData;
	private final boolean DEBUG;
	private byte oldFps = 0;
	private short oldWidth = 0;
	private short oldHeight = 0;
	private byte[] protocol;
	private byte increment = 13;
	private byte[] widthBytes;
	private byte[] heightBytes;
	private byte[] controlBytes;

	public RXDataController(Timer timer, BufferedInputStream byteInData,
			HamsterClient client, boolean DEBUG) {

		// Initialization

		this.DEBUG = DEBUG;
		this.timer = timer;
		this.client = client;
		this.controlBytes = new byte[3];
		this.byteInData = byteInData;
		// Data array
		this.rxData = new byte[14];
		// Protocol bytes
		this.protocol = this.client.getConfigH().getProtocolSign();
		// Dimension
		this.widthBytes = new byte[2];
		this.heightBytes = new byte[2];

		// Read first package and get it
		this.readNextPackage();
		if (this.checkProtocol()) {
			this.getValues();
			if (!this.stop) {
				// Reset the timer
				this.timer.reset();
				this.start();
			}
		} else {
			if (this.DEBUG)
				HamsterToolkit
						.debug("cWTH-Server",
								"Client "
										+ this.client.getSocket()
												.getInetAddress()
												.getHostAddress()
										+ " wrong protocolsign ON FIRST PACKAGE, closing connection");
			this.kill();
		}
	}

	@Override
	public void run() {
		while (!this.stop) {
			// Get byte array
			this.readNextPackage();

			// Check for correctness
			if (this.checkProtocol()) {
				this.getValues();
				if (!this.stop) {
					// Reset the timer
					this.timer.reset();
				}
			} else {
				if (this.DEBUG)
					HamsterToolkit.debug("cWTH-Server", "Client "
							+ this.client.getSocket().getInetAddress()
									.getHostAddress()
							+ " wrong protocolsign, closing connection");
				this.kill();
			}
		}
	}

	private void readNextPackage() {
		// Read data array
		try {
			this.byteInData.read(this.rxData);
		} catch (IOException e) {
			this.kill();
		}
	}

	private void getValues() {
		widthBytes[0] = rxData[5];
		widthBytes[1] = rxData[6];
		heightBytes[0] = rxData[7];
		heightBytes[1] = rxData[8];
		increment = rxData[9];
		if (rxData[10] > -1
				&& rxData[10] < this.client.getConfigH().getDevices().length)
			this.client.setDeviceNumber(rxData[10]);
		controlBytes[0] = rxData[11];
		controlBytes[1] = rxData[12];
		controlBytes[2] = rxData[13];

		// Check for new fps
		if (this.rxData[4] != this.oldFps) {
			this.oldFps = this.rxData[4];
			this.client.setFps(this.oldFps);
		}

		// Check for new width
		if (this.oldWidth != HamsterToolkit
				.unsignedShortByteArrayToShort(widthBytes)) {
			this.oldWidth = HamsterToolkit
					.unsignedShortByteArrayToShort(widthBytes);
			this.client.setWidth(this.oldWidth);
		}

		// Check for new height
		if (this.oldHeight != HamsterToolkit
				.unsignedShortByteArrayToShort(heightBytes)) {
			this.oldHeight = HamsterToolkit
					.unsignedShortByteArrayToShort(heightBytes);
			this.client.setHeight(this.oldHeight);

		}
	}

	private boolean checkProtocol() {
		if (rxData[0] == protocol[0] && rxData[1] == protocol[1]
				&& rxData[2] == protocol[2] && rxData[3] == protocol[3]
				&& rxData[9] != increment)
			return true;
		else
			return false;
	}

	public void kill() {
		if (!this.stop) {
			this.stop = true;
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server", "Client "
						+ this.client.getSocket().getInetAddress()
								.getHostAddress() + " RXDataController killed");
			synchronized (this) {
				this.notifyAll();
			}
			this.client.kill();
		}
	}

	public byte[] getControlBytes() {
		return controlBytes;
	}
}
