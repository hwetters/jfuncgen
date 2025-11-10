package se.wetterstrom.jfuncgen;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JToggleButton;

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
public class GeneralPanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	private transient AbstractSerialCom com = null;
	/** channel 1 controls */
	private final ChannelControlPanel controlPanel1 = new ChannelControlPanel(1);
	/** channel 2 controls */
	private final ChannelControlPanel controlPanel2 = new ChannelControlPanel(2);
	/** Control buttons */
	private final ControlButtons controlButtons = new ControlButtons();

	/** Constructor */
	public GeneralPanel() {
		initializeUI();
	}

	/**
	 * Set serial
	 * @param com the com
	 */
	public void setSerial(AbstractSerialCom com) {
		try {
			setEnabled(false);
			this.com = com;
			controlPanel1.setSerial(com);
			controlPanel2.setSerial(com);
			controlButtons.setSerialCom(com);
		} finally {
			setEnabled(true);
		}
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		controlPanel1.setEnabled(enable);
		controlPanel2.setEnabled(enable);
		controlButtons.setEnabled(enable);
	}

	private void initializeUI() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();
		var channelsPanel = createChannelsPanel();

		GuiUtils.addToGridBag(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH, 
				GridBagConstraints.NORTHWEST, gbc, this, channelsPanel);
		GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, 
				GridBagConstraints.NORTHWEST, gbc, this, controlButtons);
	}

	private JPanel createChannelsPanel() {
		var panel = new JPanel(new GridLayout(1, 2));
		panel.add(controlPanel1);
		panel.add(controlPanel2);
		return panel;
	}

	@Override
	public void reload() {
		controlPanel1.reload();
		controlPanel2.reload();
		controlButtons.reload(com);
	}

	/**
	 * Control buttons panel
	 */
	private static class ControlButtons extends JPanel {
		private static final long serialVersionUID = 1L;

		/** trace toggle button */
		private final JToggleButton btTrace = new JToggleButton("Trace");
		/** enable output toggle button */
		private final JToggleButton btEnableOutput = new JToggleButton("Output");
		/** enable power out toggle button */
		private final JToggleButton btPowerOut = new JToggleButton("Power Out");

		/** Constructor */
		public ControlButtons() {
			initializeUI();
		}

		private void initializeUI() {
			add(btTrace);
			add(btEnableOutput);
			add(btPowerOut);
		}

		/**
		 * Set serial communication
		 * @param com the serial communication
		 */
		public void setSerialCom(AbstractSerialCom com) {
			btTrace.addActionListener(e -> com.setTrace(btTrace.isSelected()));
			btEnableOutput.addActionListener(e -> com.setEnableOutput(btEnableOutput.isSelected()));
			btPowerOut.addActionListener(e -> com.setPowerOut(btPowerOut.isSelected()));
		}

		@Override
		public void setEnabled(boolean enable) {
			super.setEnabled(enable);
			btTrace.setEnabled(enable);
			btEnableOutput.setEnabled(enable);
			btPowerOut.setEnabled(enable);
		}

		/**
		 * Reload button states
		 * @param com the serial communication
		 */
		public void reload(AbstractSerialCom com) {
			btTrace.setSelected(com.getTrace() != 0);
			btPowerOut.setSelected(com.getPowerOut());
			btEnableOutput.setSelected(com.getEnableOutput());
		}
	}
}
