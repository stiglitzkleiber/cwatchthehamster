package com.googlemail.christian667.cWatchTheHamster;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TXDataController extends Thread implements Runnable {

	private ConcurrentLinkedQueue<byte[]> byteArrayQueue;
	private BufferedOutputStream byteOutData;
	private boolean stop = false;
	private ConfigurationHolder configH;
	private final boolean DEBUG;
	private HamsterClient client;

	public TXDataController(BufferedOutputStream byteOutData,
			HamsterClient client, ConfigurationHolder configH, boolean DEBUG) {
		this.DEBUG = DEBUG;
		this.client = client;
		this.byteOutData = byteOutData;
		this.configH = configH;
		this.byteArrayQueue = new ConcurrentLinkedQueue<byte[]>();
		this.start();
	}

	@Override
	public void run() {
		while (!this.stop) {
			if (!this.byteArrayQueue.isEmpty())
				try {
					this.sendByteArray(this.byteArrayQueue.poll());
				} catch (IOException e1) {
					if (this.DEBUG)
						HamsterToolkit
								.debug("cWTH-Server",
										"Could not send byte array: "
												+ e1.getMessage());
					this.kill();
				}
			else {
				synchronized (this) {
					try {
						this.wait();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private void sendByteArray(byte[] b) throws IOException {
		this.byteOutData.write(
				HamsterToolkit.unsignedIntToIntByteArray(b.length), 0, 4);
		this.byteOutData.write(b, 0, b.length);
		this.byteOutData.flush();
	}

	public void addImageByteArray(byte[] b) {
		if (b.length > this.configH.getMinImageSize()
				&& b.length < this.configH.getMaxImageSize()) {
			this.byteArrayQueue.add(b);
			synchronized (this) {
				this.notifyAll();
			}
		} else if (this.DEBUG)
			HamsterToolkit.debug("cWTH-Server",
					"Image to big or to small, drop it");
	}

	public void kill() {
		if (!this.stop) {
			this.stop = true;
			if (this.DEBUG)
				HamsterToolkit.debug("cWTH-Server", "Client "
						+ this.client.getSocket().getInetAddress()
								.getHostAddress() + " TXDataController killed");
			this.byteArrayQueue.clear();
			synchronized (this) {
				this.notifyAll();
			}
			this.client.kill();
		}
	}

	public short queueSize() {
		return (short) this.byteArrayQueue.size();
	}

	public boolean networkTooSlow() {
		if (this.byteArrayQueue.size() > this.configH.getSlowDownThreshold())
			return true;
		else
			return false;
	}
}
