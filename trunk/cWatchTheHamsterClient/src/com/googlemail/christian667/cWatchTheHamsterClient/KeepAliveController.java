package com.googlemail.christian667.cWatchTheHamsterClient;

import java.io.BufferedOutputStream;
import java.io.IOException;

public class KeepAliveController extends Thread implements Runnable {

	private CWatchTheHamsterClient callback;
	private boolean stop = false;
	private BufferedOutputStream byteOutData;
	private byte[] txData;

	public KeepAliveController(CWatchTheHamsterClient callback) {
		this.callback = callback;
		this.byteOutData = callback.getByteOutData();
		this.start();
	}

	@Override
	public void run() {
		txData = new byte[14];
		byte increment = 0;
		final byte[] protocolSign = { 3, 1, 4, 1 };

		// Set the protocolSign
		txData[0] = protocolSign[0];
		txData[1] = protocolSign[1];
		txData[2] = protocolSign[2];
		txData[3] = protocolSign[3];

		// Height and width from backend
		byte[] height = HamsterToolkit
				.unsignedShortToShortByteArray(this.callback.getHeight());
		byte[] width = HamsterToolkit
				.unsignedShortToShortByteArray(this.callback.getWidth());
		txData[5] = width[0];
		txData[6] = width[1];
		txData[7] = height[0];
		txData[8] = height[1];

		// The control bits default 0
		txData[11] = 0;
		txData[12] = 0;
		txData[13] = 0;

		while (!this.stop) {
			// Generate 10-ByteCoding
			txData[4] = this.callback.getFps();

			// Incrementing byte for protocol
			txData[9] = increment++;

			// The device number
			txData[10] = this.callback.getDeviceNumber();

			// And send to the server
			try {
				this.byteOutData.write(txData);
				this.byteOutData.flush();
			} catch (IOException e) {
				this.kill();
				System.out.println("Connection closed by server");
			}

			// Sleep connectionTimeout / 2, to ensure connection stays opened
			try {
				Thread.sleep(this.callback.getConnectionTimeOut() * 500);
			} catch (InterruptedException e) {
				this.kill();
			}
		}
	}

	public void setControlBytes(byte[] controlBytes) {
		if (controlBytes.length == 3) {
			// The control bits
			this.txData[11] = controlBytes[0];
			this.txData[12] = controlBytes[1];
			this.txData[13] = controlBytes[2];
		}
	}

	public void kill() {
		if (!this.stop) {
			this.stop = true;
			synchronized (this) {
				this.notifyAll();
			}
			this.callback.kill();
		}
	}
}
