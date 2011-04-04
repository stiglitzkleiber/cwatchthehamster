package com.googlemail.christian667.cWatchTheHamster;

import java.net.Socket;

public class ClientController {

	private ConfigurationHolder configH;
	private PictureGrabber[] picGrabbber;
	private DeviceController[] devices;
	private final boolean DEBUG;

	public ClientController(ConfigurationHolder configH,
			PictureGrabber[] picGrabber, boolean DEBUG) {
		this.picGrabbber = picGrabber;
		this.DEBUG = DEBUG;
		this.configH = configH;
		// Start the deviceController
		this.devices = new DeviceController[this.configH.getDevices().length];
		for (byte i = 0; i < this.devices.length; i++)
			this.devices[i] = new DeviceController(this, this.picGrabbber[i],
					this.configH, i, this.DEBUG);
	}

	public void addClient(Socket socket) {
		if (socket.isConnected())
			// Add client to first deviceController
			this.devices[0].addClient(socket);
	}

	public short getNumberOfConnectedClients() {
		short sum = 0;
		for (int i = 0; i < this.devices.length; i++)
			sum += this.devices[i].getNumberOfConnectedClients();
		return sum;
	}

	public void clientChangedDevice(HamsterClient client, byte deviceNumber) {
		System.out.println("Client "
				+ client.getSocket().getInetAddress().getHostAddress()
				+ " switched to device " + deviceNumber);
		// Add client to new deviceController
		this.devices[deviceNumber].addClient(client);
	}

	public short getNumberOfDevices() {
		return (short) this.devices.length;
	}

}
