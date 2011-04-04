package com.googlemail.christian667.cWatchTheHamsterClient;

import java.awt.image.BufferedImage;

public interface IcHamsterGUI {

	public void setImage(BufferedImage bufImage);

	public void connectionClosedByServer();

	public void setBackend(CWatchTheHamsterClient backend);
}
