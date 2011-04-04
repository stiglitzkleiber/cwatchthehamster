package com.googlemail.christian667.cWatchTheHamster;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class DeviceController extends Thread implements Runnable {
	private final boolean DEBUG;
	private boolean stop = false;
	private PictureGrabber picGrab;
	private ConfigurationHolder configH;
	private HamsterClient firstClient;
	private List<HamsterClient> clients;
	private boolean clientFpsChanged = false;
	private byte deviceNumber;
	private long sleeptime;
	private ClientController callback;

	public DeviceController(ClientController callback, PictureGrabber picGrab,
			ConfigurationHolder configH, byte deviceNumber, boolean DEBUG) {
		this.picGrab = picGrab;
		this.callback = callback;
		this.DEBUG = DEBUG;
		this.deviceNumber = deviceNumber;
		this.configH = configH;
		// Synchronized is much better
		this.clients = Collections
				.synchronizedList(new ArrayList<HamsterClient>());
		this.sleeptime = this.configH.getMinimumSleeptime();
		this.start();
	}

	@Override
	public void run() {
		while (!this.stop) {
			// Sleep until the first client logs in
			synchronized (this) {
				try {
					this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// First client connected, set resolution and fps
			boolean skip = false;
			try {
				this.firstClient = this.clients.iterator().next();
			} catch (ConcurrentModificationException e) {
				skip = true;
			}

			if (!skip) {
				if (this.firstClient.getWidth() > 0
						&& this.firstClient.getHeight() > 0) {
					this.configH.setWidth(this.firstClient.getWidth());
					this.configH.setHeight(this.firstClient.getHeight());
				}
				if (this.firstClient.getFps() > 0)
					this.configH.setFpsOfDevice(this.deviceNumber,
							this.firstClient.getFps());
			}
			// Start capturing
			try {
				this.picGrab.startCapturing();
			} catch (V4L4JException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			short currentFrameNumber = 0;

			// Calculate sleeptime first time
			this.sleeptime = this.calculateSleepTime();

			while (this.clients.size() > 0) {

				// Take picture and send it
				byte[] currentPic = this.getPicture();

				// Calculate frame number
				currentFrameNumber = (short) (currentFrameNumber % this.configH
						.getFpsOfDevice(this.deviceNumber));
				currentFrameNumber++;

				// Watch for changed maxFPS
				if (this.clientFpsChanged) {
					this.configH.setFpsOfDevice(this.deviceNumber,
							this.getMaxFps());
					this.clientFpsChanged = false;
				}

				// Check which client should get this picture
				try {
					Iterator<HamsterClient> clientIt = this.clients.iterator();
					while (clientIt.hasNext()) {
						HamsterClient tmpClient = clientIt.next();
						// Check frame window - Safety first
						if ((currentFrameNumber % (this.configH
								.getFpsOfDevice(this.deviceNumber) / tmpClient
								.getFps())) == 0)
							if (currentPic != null)
								tmpClient.sendImageByteArray(currentPic);
					}
				} catch (ConcurrentModificationException e) {
				}

				// Recalculate sleeptime
				if (this.configH.isChangesMade()) {
					System.out.println("Device " + this.deviceNumber
							+ ":\t\tNew maximum FPS set to "
							+ this.configH.getFpsOfDevice(this.deviceNumber));
					this.sleeptime = this.calculateSleepTime();
				}

				try {
					Thread.sleep(this.sleeptime);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// Last client disconnected
			this.picGrab.stopCapturing();
		}
	}

	private byte getMaxFps() {
		if (clients.size() > 0) {
			byte maxFps = 0;
			byte checkFps = 0;
			try {
				Iterator<HamsterClient> clientIt = this.clients.iterator();
				while (clientIt.hasNext()) {
					checkFps = clientIt.next().getFps();
					if (checkFps > maxFps)
						maxFps = checkFps;
				}
			} catch (ConcurrentModificationException e) {
			}
			if (maxFps == 0)
				maxFps = 1;

			return maxFps;
		} else
			return 1;
	}

	/**
	 * Calculates sleeptime between the frames in millis
	 * 
	 * @return
	 */
	private long calculateSleepTime() {
		// Calculate time for one frame
		long fpsStart = System.currentTimeMillis();
		this.getPicture();
		long fpsDelay = System.currentTimeMillis() - fpsStart;
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "fpsDelay:" + fpsDelay);

		// Calculate for fps
		long sleeptime = ((1000 / this.configH
				.getFpsOfDevice(this.deviceNumber)) - fpsDelay);

		// Correct for network delay
		if (sleeptime > 0)
			sleeptime -= (sleeptime / 10) * this.clients.size();

		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "FPS calculated sleeptime:"
					+ sleeptime);

		// Check minimum
		if (sleeptime < this.configH.getMinimumSleeptime())
			sleeptime = this.configH.getMinimumSleeptime();

		return (sleeptime);
	}

	private byte[] getPicture() {
		try {
			return this.picGrab.getImageByteArray();
		} catch (V4L4JException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public void clientChangedFps() {
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "Client changed fps");
		this.clientFpsChanged = true;
	}

	public void clientChangedDevice(HamsterClient client, byte deviceNumber) {
		if (!client.isKilled()) {
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server", "Client changed device");
			// Remove from own list
			this.clientDisconnected(client);
			// Send to callback for new deviceController
			this.callback.clientChangedDevice(client, deviceNumber);
		}
	}

	public void addClient(Socket socket) {
		// Build new hamster client and add it with next method
		this.addClient(new HamsterClient(socket, this.configH, this, this.DEBUG));
	}

	public void addClient(HamsterClient client) {
		if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server", "Client "
					+ client.getSocket().getInetAddress().getHostAddress()
					+ " waits for its place in list " + this.deviceNumber);
		// Check if the client is still connected && still wants this device
		if (!client.isKilled() && client.getDeviceNumber() == this.deviceNumber) {
			// Set the deviceController as new callback for the client
			client.setCallback(this); // Needed after changing the device
			// Add to list
			this.clients.add(client);
			// Unlock the list
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server", "Client "
						+ client.getSocket().getInetAddress().getHostAddress()
						+ " added to list of device " + this.deviceNumber);
			// Recalculate new max fps
			this.configH.setFpsOfDevice(this.deviceNumber, this.getMaxFps());
			// Wake up
			synchronized (this) {
				this.notifyAll();
			}
		}
	}

	public void clientDisconnected(HamsterClient client) {
		// Remove from list
		if (this.clients.contains(client)) {
			this.clients.remove(client);
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server", "Client "
						+ client.getSocket().getInetAddress().getHostAddress()
						+ " with device " + client.getDeviceNumber()
						+ " removed from device list " + this.deviceNumber);
			// Recalculate new max fps
			this.configH.setFpsOfDevice(this.deviceNumber, this.getMaxFps());
		}
	}

	public short getNumberOfConnectedClients() {
		return (short) this.clients.size();
	}

	public byte getDeviceNumber() {
		return deviceNumber;
	}
}
