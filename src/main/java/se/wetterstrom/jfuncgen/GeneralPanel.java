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
	/** trace toggle button */
	private final JToggleButton btTrace = new JToggleButton("Trace");
	/** enable output toggle button */
	private final JToggleButton btEnableOutput = new JToggleButton("Output");
	/** enable power out toggle button */
	private final JToggleButton btPowerOut = new JToggleButton("Power Out");

	/** Constructor */
	public GeneralPanel() {
		this.setup();
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
		} finally {
			setEnabled(true);
		}
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		controlPanel1.setEnabled(enable);
		controlPanel2.setEnabled(enable);
		btTrace.setEnabled(enable);
		btEnableOutput.setEnabled(enable);
		btPowerOut.setEnabled(enable);
	}

	private void setup() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();

		var chPanel = new JPanel(new GridLayout(1, 2));
		chPanel.add(controlPanel1);
		chPanel.add(controlPanel2);

		var btPanel = new JPanel();
		btTrace.addActionListener(e -> com.setTrace(btTrace.isSelected()));
		btPanel.add(btTrace);

		btEnableOutput.addActionListener(e->com.setEnableOutput(btEnableOutput.isSelected()));
		btPanel.add(btEnableOutput);

		btPowerOut.addActionListener(e->com.setPowerOut(btPowerOut.isSelected()));
		btPanel.add(btPowerOut);

		GuiUtils.addToGridBag(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST,gbc, this, chPanel);
		GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST,gbc, this, btPanel);
	}

	@Override
	public void reload() {
		controlPanel1.reload();
		controlPanel2.reload();
		btTrace.setSelected(com.getTrace() != 0);
		btPowerOut.setSelected(com.getPowerOut());
		btEnableOutput.setSelected(com.getEnableOutput());
	}
}
