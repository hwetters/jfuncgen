package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

/**
 * <pre style='font-family:sans-serif;'>
 *   JFuncGen - A java GUI for the MHS5200 and FY6900 function generators
 *   Copyright (C) 2021 Henrik Wetterstrom
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * </pre>
 */
public class MainPanel {

	private final JFrame frame;
	private final JTabbedPane tabPane = new JTabbedPane();

	private final StatusBar statusBar = new StatusBar();
	// tabs
	private final ConfigPanel configPanel = new ConfigPanel(this::setSerial);
	private final GeneralPanel generalPanel = new GeneralPanel();
	private final AdvancedPanel advancedPanel = new AdvancedPanel();
	private final ArbitraryPanel arbitraryPanel = new ArbitraryPanel();
	private final ConsolePanel consolePanel = new ConsolePanel();
	private final AboutPanel aboutPanel = new AboutPanel();

	/**
	 * Constructor
	 * @param frame the frame
	 */
	private MainPanel(JFrame frame) {
		this.frame = frame;
		statusBar.setParentFrame(frame);
		setSerial(DeviceType.NONE.getSerialCom());
		DeviceType.setStatusConsumer(statusBar::set);
		DeviceType.setDataConsumer(s -> {
			try {
				consolePanel.getConsoleStream().write(s.getBytes());
			} catch (IOException unlikely) {
				// ignore
			}
		});
		DeviceType.setOutputConsumer(str -> consolePanel.getConsoleStream().append(str));
		setup();
	}

	private void setSerial(AbstractSerialCom cmd) {
		generalPanel.setSerial(cmd);
		advancedPanel.setSerial(cmd);
		arbitraryPanel.setSerial(cmd);
		consolePanel.setSerial(cmd);
	}

	/**
	 * Create panel
	 * @return the main panel
	 */
	public static MainPanel create() {
		GuiUtils.setDefaultFonts(Config.getFontSize());
		GuiUtils.setLookAndFeel(Config.getLook());
		var frame = new JFrame("JFuncGen");
		var mp = new MainPanel(frame);
		GuiUtils.setLocation(frame, 4, 4);
		return mp;
	}

	/**
	 * Set panel visible
	 * @param visible if visible
	 */
	public void setVisible(boolean visible) {
		frame.setVisible(visible);
	}

	/**
	 * Setup
	 */
	private void setup() {

		GuiUtils.setOwner(frame);
		tabPane.add("Config", configPanel);
		tabPane.add("General", generalPanel);
		tabPane.add("Advanced", advancedPanel);
		tabPane.add("Arbitrary", arbitraryPanel);
		tabPane.add("Console", consolePanel);
		tabPane.add("About", aboutPanel);

		var panel = new JPanel(new BorderLayout());
		panel.add(tabPane, BorderLayout.CENTER);
		panel.add(statusBar, BorderLayout.SOUTH);
		frame.setContentPane(panel);

		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				shutdown();
			}
		});
		frame.pack();

		tabPane.addChangeListener(ev -> {
		    var sourceTabbedPane = (JTabbedPane) ev.getSource();
		    var tab = (FuncTab) sourceTabbedPane.getSelectedComponent();
		    tab.reload();
		});
	}

	/** Shut down */
	private void shutdown() {
		try {
			Config.save();
		} catch (IOException ignore) {
			// ignored
		}
		frame.dispose();
		System.exit(0);
	}

}
