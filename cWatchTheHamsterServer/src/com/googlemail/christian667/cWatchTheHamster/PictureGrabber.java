package com.googlemail.christian667.cWatchTheHamster;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import au.edu.jcu.v4l4j.CaptureCallback;
import au.edu.jcu.v4l4j.FrameGrabber;
import au.edu.jcu.v4l4j.VideoDevice;
import au.edu.jcu.v4l4j.VideoFrame;
import au.edu.jcu.v4l4j.exceptions.StateException;
import au.edu.jcu.v4l4j.exceptions.V4L4JException;

public class PictureGrabber implements CaptureCallback {

	private VideoDevice vd;
	private FrameGrabber fg;
	private boolean captureStarted = false;
	private VideoFrame frame;
	private BufferedImage bufIm;
	private ByteArrayOutputStream byteArrayOutStream;
	private ConfigurationHolder configH;
	private byte deviceNumber;

	public PictureGrabber(ConfigurationHolder configH, byte deviceNumber)
			throws V4L4JException {
		this.configH = configH;
		this.byteArrayOutStream = new ByteArrayOutputStream();
		this.deviceNumber = deviceNumber;
		this.vd = new VideoDevice(configH.getDevices()[deviceNumber]);
	}

	public byte[] getImageByteArray() throws V4L4JException, IOException {
		if (this.captureStarted && this.frame != null) {
			try {
				this.bufIm = this.frame.getBufferedImage();
			} catch (StateException e) {
				// Frame fetched from last capture still recycled
			}
			this.byteArrayOutStream.reset();
			ImageIO.write(this.bufIm, "jpg", this.byteArrayOutStream);
			this.frame.recycle();
			return this.byteArrayOutStream.toByteArray();
		}
		return null;
	}

	public void stopCapturing() {
		if (this.captureStarted) {
			System.out.println("Device " + this.deviceNumber + ":\t\tclosed");
			this.captureStarted = false;
			this.fg.stopCapture();
			this.vd.releaseFrameGrabber();
		}
	}

	public void startCapturing() throws V4L4JException {
		if (!this.captureStarted) {
			System.out.println("Device " + this.deviceNumber
					+ ":\t\tCapture resolution " + this.configH.getWidth()
					+ "x" + this.configH.getHeight());
			fg = vd.getJPEGFrameGrabber(this.configH.getWidth(),
					this.configH.getHeight(), this.configH.getChannel(),
					this.configH.getStd(), this.configH.getQty());
			fg.setCaptureCallback(this);
			fg.startCapture();
			this.captureStarted = true;
		}
	}

	@Override
	public void exceptionReceived(V4L4JException arg0) {
		System.out.println(arg0.getCause());
	}

	@Override
	public void nextFrame(VideoFrame arg0) {
		this.frame = arg0;
	}

}
