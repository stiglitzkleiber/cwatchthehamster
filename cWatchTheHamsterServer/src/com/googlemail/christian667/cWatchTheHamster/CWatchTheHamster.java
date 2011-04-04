package com.googlemail.christian667.cWatchTheHamster;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

import au.edu.jcu.v4l4j.exceptions.V4L4JException;

/**
 * The cWatchTheHamsterServer
 * 
 * @author christian667@googlemail.com
 * 
 */
public class CWatchTheHamster implements ITimerCallback {

	public final boolean DEBUG;
	public final static String Version = "Version 1.0 rc16 (04/04/2011)";

	private ServerSocket serverSocket;
	private Socket currentSocket;
	private ConfigurationHolder configH;
	private PictureGrabber[] picGrabber;
	private Timer timer;
	private HashMap<InetAddress, Integer> blacklist;
	private ClientController clientController;
	private String debug;

	public static void main(String[] args) {
		boolean DEBUG = false;
		if (args.length > 0 && args[0].contains("--verbose"))
			DEBUG = true;

		if (args.length > 0
				&& (args[0].contains("--help") || args[0].contains("--version")))
			System.out.println("cWatchTheHamster Server "
					+ CWatchTheHamster.Version);
		else
			new CWatchTheHamster(DEBUG);
	}

	public CWatchTheHamster(boolean DEBUG) {
		this.configH = new ConfigurationHolder();
		this.DEBUG = DEBUG;
		if (this.DEBUG)
			this.debug = "cWTH-Server";
		this.blacklist = new HashMap<InetAddress, Integer>();
		System.out.println("cWatchTheHamster Server - " + Version + "[@"
				+ this.configH.getDevices().length + " devices]");
		boolean initializationSuccess = true;
		try {
			this.serverSocket = new ServerSocket(this.configH.getPort());
		} catch (IOException e1) {
			initializationSuccess = false;
			System.out.println("Initialization failed: " + e1.getMessage());
		}

		if (initializationSuccess) {
			this.picGrabber = new PictureGrabber[this.configH.getDevices().length];

			try {
				for (byte i = 0; i < this.picGrabber.length; i++)
					this.picGrabber[i] = new PictureGrabber(this.configH, i);
			} catch (V4L4JException e1) {
				initializationSuccess = false;
				System.out.println("Initialization failed: " + e1.getMessage());
			}

			this.clientController = new ClientController(this.configH,
					this.picGrabber, this.DEBUG);
		}

		while (initializationSuccess) {

			this.handleConnections();

			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server",
						"Going to sleep for ConnectionBreak");
			try {
				Thread.sleep(this.configH.getConnectionBreak() * 1000);
			} catch (InterruptedException e) {
				System.out.println("Thread.sleep throws error:"
						+ e.getMessage());
				initializationSuccess = false;
			}
		}
		// Close the serverSocket, exit
		if (this.serverSocket != null)
			try {
				this.serverSocket.close();
			} catch (IOException e) {
			}
		System.exit(-1);
	}

	private void handleConnections() {
		try {
			// Wait for incomming connection => blocking!
			this.currentSocket = this.serverSocket.accept();
		} catch (IOException e) {
			System.out.println("Initialization failed: " + e.getMessage());
		}
		// Watch for max clients
		if (this.clientController.getNumberOfConnectedClients() < this.configH
				.getMaxClients()) {

			// New connection
			System.out.println("\t\tNew client connected at "
					+ HamsterToolkit.getCurrentTime() + " from "
					+ this.currentSocket.getInetAddress().getHostAddress());

			// Start the Timer for authentication
			this.timer = new Timer(this.configH.getAuthenticationTimeOut(),
					this);

			// Check blacklist
			if (!this.blacklisted(this.currentSocket.getInetAddress())) {

				// Authentication process
				boolean authenticationSuccess = false;
				try {
					authenticationSuccess = HamsterToolkit
							.serverAuthentication(this.currentSocket,
									this.debug, this.configH.getLogins());
				} catch (IOException e) {
					authenticationSuccess = false;
				}

				if (authenticationSuccess) {
					this.timer.disarm();
					// Remove from blacklist if needed
					this.authenticationSuccess(this.currentSocket
							.getInetAddress());
					System.out.println("Client "
							+ currentSocket.getInetAddress().getHostAddress()
							+ " authenticated successfully");
					// Add to default start device = 0
					this.clientController.addClient(currentSocket);
				} else {
					// Blacklist the host
					this.timer.disarm();
					this.authenticationFailed(this.currentSocket
							.getInetAddress());
					System.out
							.println("Client "
									+ currentSocket.getInetAddress()
											.getHostAddress()
									+ " failed in authentication, now added to blacklist with failed attemps "
									+ this.blacklist.get(this.currentSocket
											.getInetAddress()));
					// And close the socket
					try {
						this.currentSocket.close();
					} catch (IOException e) {
					}
				}
			} else {
				this.timer.disarm();
				System.out.println("Client "
						+ currentSocket.getInetAddress().getHostAddress()
						+ " is blacklisted, connection closed");
				// Close the socket
				try {
					this.currentSocket.close();
				} catch (IOException e) {
				}
			}
		} else {
			this.timer.disarm();
			System.out
					.println("Maximum amount of clients connected, connection closed");
			// Close the socket
			try {
				this.currentSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private void authenticationSuccess(InetAddress address) {
		if (this.blacklist.containsKey(address))
			this.blacklist.remove(address);
	}

	private void authenticationFailed(InetAddress address) {
		if (this.blacklist.containsKey(address)) {
			int countTmp = this.blacklist.get(address);
			countTmp++;
			this.blacklist.remove(address);
			this.blacklist.put(address, countTmp);
		} else
			this.blacklist.put(address, 1);
	}

	private boolean blacklisted(InetAddress address) {
		if (this.blacklist.containsKey(address))
			if (this.blacklist.get(address) > this.configH
					.getBlacklistThreshold())
				return true;
		return false;
	}

	@Override
	public void timedOut() {
		synchronized (this) {
			this.notifyAll();
		}
		// Kill the socket
		System.out.println("Client "
				+ currentSocket.getInetAddress().getHostAddress()
				+ " timed out while authentication");
		try {
			this.currentSocket.close();
		} catch (IOException e) {
		}
	}

}
