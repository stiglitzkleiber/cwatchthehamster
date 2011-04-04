package com.googlemail.christian667.cWatchTheHamsterClient;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;

public class SwingGUI extends javax.swing.JFrame implements Runnable,
		IcHamsterGUI {

	private static final long serialVersionUID = -4998863590061528817L;
	private JLabel imageLabel;
	private CWatchTheHamsterClient backend;
	private JButton fpsButton;
	private JTextField fpsTextField;
	private JLabel fpsLabel;
	private JPanel fpsPanel;
	private short initialWidth;
	private short initialHeight;
	private byte initialFps;
	private int[] oldDim;
	private int[] screenSize;
	private JButton deviceButton;
	private int widgetHeight = 100;
	private int widgetWidth = 100;
	private short widgetDistance = 20;
	private Image logoImage;

	public static void main(String[] args) {
		if (args.length > 0 && (args[0].contains("--version")))
			System.out.println("cWatchTheHamster (Client) "
					+ CWatchTheHamsterClient.Version);
		else if (args.length > 0 && (args[0].contains("--help"))
				|| args.length < 14)
			System.out
					.println("\nusage:\t cWatchTheHamsterClient --username USER"
							+ " --password PASSWORD --server WWW.EXAMPLE.COM "
							+ "--port 6666 --resolution 320x240 --fps 1 --dev 0"
							+ " [--verbose]");
		else
			SwingGUI.initiate(args);
	}

	public static void initiate(String[] options) {
		SwingGUI inst = new SwingGUI(Short.valueOf(options[9].split("x")[0]),
				Short.valueOf(options[9].split("x")[1]),
				Byte.valueOf(options[11]));

		SwingUtilities.invokeLater(inst);

		// Blocking! :
		try {
			new CWatchTheHamsterClient(inst, options);
		} catch (UnknownHostException e) {
			System.exit(-1);
		} catch (IOException e) {
			System.exit(-1);
		}
	}

	public SwingGUI(short width, short height, byte fps) {
		this.initialWidth = width;
		this.initialHeight = height;
		this.initialFps = fps;
		this.logoImage = Toolkit.getDefaultToolkit().getImage(
				this.getClass().getResource("/img/logo.png"));
	}

	public void run() {
		initGUI();
		this.setVisible(true);
	}

	private void initGUI() {
		this.oldDim = new int[4];
		this.screenSize = new int[2];
		this.screenSize[0] = Toolkit.getDefaultToolkit().getScreenSize().width;
		this.screenSize[1] = Toolkit.getDefaultToolkit().getScreenSize().height;
		this.widgetWidth = this.screenSize[0] / 6;
		this.widgetHeight = this.widgetWidth * 3 / 4;
		this.widgetDistance = (short) (this.widgetWidth / 6);
		try {
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			this.setTitle("cWatchTheHamster");
			this.setIconImage(this.logoImage);
			this.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent evt) {
					thisWindowClosing(evt);
				}
			});
			{
				imageLabel = new JLabel();
				getContentPane().add(imageLabel, BorderLayout.CENTER);
				imageLabel.setPreferredSize(new Dimension(this.initialWidth,
						this.initialHeight));
				imageLabel.setBorder(BorderFactory
						.createBevelBorder(BevelBorder.LOWERED));
				imageLabel.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						toggleWidgetMode();
					}
				});
			}
			{
				fpsPanel = new JPanel();
				BoxLayout fpsPanelLayout = new BoxLayout(fpsPanel,
						javax.swing.BoxLayout.X_AXIS);
				fpsPanel.setLayout(fpsPanelLayout);
				fpsPanel.setBorder(BorderFactory
						.createBevelBorder(BevelBorder.LOWERED));
				getContentPane().add(fpsPanel, BorderLayout.SOUTH);
				{
					fpsLabel = new JLabel();
					fpsPanel.add(fpsLabel);
					fpsLabel.setText("Frames per second:\t");
				}
				{
					fpsTextField = new JTextField();
					fpsTextField.setText(String.valueOf(this.initialFps));
					fpsPanel.add(fpsTextField);
					fpsTextField.addKeyListener(new KeyAdapter() {
						public void keyPressed(KeyEvent evt) {
							fpsTextFieldKeyPressed(evt);
						}
					});
				}
				{
					fpsButton = new JButton();
					fpsPanel.add(fpsButton);
					fpsButton.setText("set");
					fpsButton.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent evt) {
							fpsButtonMouseClicked(evt);
						}
					});
				}
				{
					deviceButton = new JButton();
					fpsPanel.add(deviceButton);
					deviceButton.setText("device");
					deviceButton.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent evt) {
							deviceButtonMouseClicked(evt);
						}
					});
				}
			}
			pack();
			this.setLocation(Toolkit.getDefaultToolkit().getScreenSize().width
					/ 2 - this.getSize().width / 2, Toolkit.getDefaultToolkit()
					.getScreenSize().height / 2 - this.getSize().height / 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setImage(BufferedImage bufImage) {
		if (this.isVisible())
			this.imageLabel.getGraphics().drawImage(bufImage, 0, 0,
					this.getWidth(), this.getHeight(), null);
	}

	private void thisWindowClosing(WindowEvent evt) {
		if (this.backend != null)
			this.backend.kill();
	}

	@Override
	public void connectionClosedByServer() {
		this.dispose();
	}

	public void setBackend(CWatchTheHamsterClient backend) {
		this.backend = backend;
	}

	private void fpsButtonMouseClicked(MouseEvent evt) {
		if (!this.fpsTextField.getText().isEmpty()) {
			byte fpsTmp = Byte.valueOf(this.fpsTextField.getText());
			if (fpsTmp < 30 && fpsTmp > 0)
				this.backend.setFps(fpsTmp);
		}
	}

	private void fpsTextFieldKeyPressed(KeyEvent evt) {
		if (evt.getKeyCode() == 27)
			toggleWidgetMode();
	}

	private void toggleWidgetMode() {
		if (this.isUndecorated()) {
			// Widget => to window
			this.setVisible(false);
			this.dispose();
			this.setUndecorated(false);
			this.setAlwaysOnTop(false);
			this.setResizable(true);
			this.getContentPane().add(fpsPanel, BorderLayout.SOUTH);
			this.setLocation(this.oldDim[0], this.oldDim[1]);
			this.setSize(this.oldDim[2], this.oldDim[3]);
			this.setVisible(true);
			this.backend.setFps(Byte.valueOf(this.fpsTextField.getText()));
		} else {
			// Window => to widget
			this.backend.setFps((byte) 1);
			this.setVisible(false);
			this.dispose();
			this.getContentPane().remove(this.fpsPanel);
			this.oldDim[0] = this.getLocation().x;
			this.oldDim[1] = this.getLocation().y;
			this.oldDim[2] = this.getSize().width;
			this.oldDim[3] = this.getSize().height;
			this.setUndecorated(true);
			this.setAlwaysOnTop(true);
			this.setResizable(false);
			this.setSize(this.widgetWidth, this.widgetHeight);
			this.setLocation(this.screenSize[0] - this.widgetWidth
					- this.widgetDistance, this.widgetDistance);
			this.setVisible(true);
		}
	}

	private void deviceButtonMouseClicked(MouseEvent evt) {
		byte tmpDevice = backend.getDeviceNumber();
		if (tmpDevice + 1 < backend.getNumberOfDevices())
			tmpDevice++;
		else
			tmpDevice = 0;
		this.backend.setDeviceNumber(tmpDevice);
	}
}
