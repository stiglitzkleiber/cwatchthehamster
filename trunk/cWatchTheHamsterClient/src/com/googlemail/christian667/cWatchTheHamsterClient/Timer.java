package com.googlemail.christian667.cWatchTheHamsterClient;

public class Timer extends Thread implements Runnable {

	private int timeOut;
	private int unixTime;
	private ITimerCallback callback;
	private boolean armed = true;

	public Timer(int timeOut, ITimerCallback callback) {
		this.timeOut = timeOut;
		this.callback = callback;
		this.start();
	}

	@Override
	public void run() {
		this.reset();
		while (HamsterToolkit.getCurrentUnixTime() - this.unixTime < this.timeOut)
			this.sleep1Second();
		if (this.armed) {
			this.callback.timedOut();
		}
	}

	private void sleep1Second() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setTimeOut(short timeOut) {
		this.timeOut = timeOut;
		this.reset();
	}

	public void reset() {
		this.unixTime = HamsterToolkit.getCurrentUnixTime();
	}

	public void disarm() {
		this.armed = false;
	}

}
