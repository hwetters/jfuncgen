package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

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
public class ChannelControlPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	/** serial command */
	private transient AbstractSerialCom cmd = null;
	/** channel */
	private final int channel;
	/** wave type combo box */
	private final JComboBox<WaveType> cbWaveType = new JComboBox<>();
	/** frequency textfield */
	private final JTextField tfFreq = new JTextField("440.00", 10);
	/** jog dial */
	private final JogDial jdFreq = new JogDial();
	/** 0 dB radio button */
	private final JRadioButton rb0db = new JRadioButton("0dB");
	/** -20 dB radio button */
	private final JRadioButton rb20db = new JRadioButton("-20dB");
	/** double formatter */
	private final DecimalFormat doubleFormatter = new DecimalFormat();
	/** amplitude decimal text field */
	private final JFormattedTextField ttAmplitude = new JFormattedTextField(new DecimalFormat("##.##"));
	/** duty cycle decimal text field */
	private final JFormattedTextField ttDuty = new JFormattedTextField(new DecimalFormat("###.#"));
	/** offset decimal text field */
	private final JFormattedTextField ttOffset = new JFormattedTextField(new DecimalFormat("###.#"));
	/** invert toggle button */
	private final JToggleButton btInvert = new JToggleButton("Invert");
	/** enable toggle button */
	private final JToggleButton btEnable = new JToggleButton("Enable");
	/** increase speed button */
	private final JButton btSpeedInc = new JButton("+");
	/** decrease speed button */
	private final JButton btSpeedDec = new JButton("-");

	/**
	 * Constructor
	 *
	 * @param channel the channel
	 */
	public ChannelControlPanel(int channel) {
		this.channel = channel;
		doubleFormatter.setMinimumFractionDigits(1);
		setup();
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		cbWaveType.setEnabled(enable);
		tfFreq.setEnabled(enable);
		rb0db.setEnabled(enable);
		rb20db.setEnabled(enable);
		ttAmplitude.setEnabled(enable);
		ttDuty.setEnabled(enable);
		ttOffset.setEnabled(enable);
	}

	/** reload */
	public void reload() {
		tfFreq.setText("" + cmd.getFrequency(channel));
		ttAmplitude.setText("" + cmd.getAmplitude(channel));
		ttDuty.setText("" + cmd.getDutyCycle(channel));
		ttOffset.setText("" + cmd.getOffset(channel));

		int w = cmd.getWaveForm(channel);
		cbWaveType.setEnabled(false);
		cmd.getWaveTypes(channel).stream().filter(a -> a.id == w).findAny().ifPresent(cbWaveType::setSelectedItem);
		cbWaveType.setEnabled(true);

		int att = cmd.getAttenuation(channel);
		if (att == 0) {
			rb0db.setSelected(true);
		} else {
			rb20db.setSelected(true);
		}

		btInvert.setSelected(cmd.getInvert(channel));
		btEnable.setSelected(cmd.getEnableChannel(channel));
	}

	/**
	 * Set serial
	 * @param cmd the serial command
	 */
	public void setSerial(AbstractSerialCom cmd) {
		this.cmd = cmd;
		double f = cmd.getMaxFrequency();
		cbWaveType.setEnabled(false);
		cbWaveType.removeAllItems();
		cmd.getWaveTypes(channel).forEach(cbWaveType::addItem);
		cbWaveType.setEnabled(true);
		jdFreq.setMaxValue(f);
		tfFreq.setToolTipText(
				"The frequency in Hz (default), kHz, Mhz or GHz (0 to " + (((int) f) / 1000000) + " MHz).");
	}

	/**
	 * Set lap speed
	 * @param lapSpeed
	 */
	private void setLapSpeed(double lapSpeed) {
		double speed = Math.max(0.1, lapSpeed);
		jdFreq.setLapStep(speed);
		btSpeedInc.setToolTipText("Increase value by " + speed + " per lap");
		btSpeedDec.setToolTipText("Decrease value by " + speed + " per lap");
	}

	/** Setup */
	private void setup() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Channel " + channel));

		jdFreq.setMinValue(0.0);
		jdFreq.setMaxValue(25000000);
		setLapSpeed(100.0);

		ttAmplitude.setValue(5.0);
		ttDuty.setValue(50.0);
		ttOffset.setValue(0.0);
		rb0db.setSelected(true);

		btSpeedInc.setMargin(new Insets(0, 0, 0, 0));
		btSpeedInc.addActionListener(e -> setLapSpeed(jdFreq.getLapStep() * 10.0));
		btSpeedDec.setMargin(new Insets(0, 0, 0, 0));
		btSpeedDec.addActionListener(e -> setLapSpeed(jdFreq.getLapStep() / 10.0));
		btSpeedDec.setPreferredSize(btSpeedInc.getPreferredSize());

		var v1 = new JPanel(new BorderLayout());
		v1.add(btSpeedInc, BorderLayout.NORTH);

		var v2 = new JPanel(new BorderLayout());
		v2.add(btSpeedDec, BorderLayout.NORTH);

		var speedPanel = new JPanel(new BorderLayout());
		speedPanel.add(v1, BorderLayout.WEST);
		speedPanel.add(jdFreq, BorderLayout.CENTER);
		speedPanel.add(v2, BorderLayout.EAST);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this,
				getEditFieldsPanel());
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc,
				this, getButtonPanel());
		GuiUtils.addToGridBag(1, 0, 1, 2, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, gbc, this,
				speedPanel);

		GuiUtils.fillGridBag(0, 2, 1, 1, gbc, this);

		var df = new DecimalFormat("#0.00");
		jdFreq.addJogDialListener(ev -> tfFreq.setText(df.format(ev.getValue())));
		jdFreq.addMouseButtonListener(ev -> handleFreqChange());

		MouseWheelListener ml = e -> {
			try {
				double f = Utils.parseFreq(tfFreq.getText()) + jdFreq.getLapStep()
						* (e.getWheelRotation() * e.getScrollAmount() * Math.abs(e.getUnitsToScroll()));
				tfFreq.setText(df.format(Math.max(f, 0)));
				handleFreqChange();
			} catch (ParseException e1) {
				// ignore
			}
		};
		jdFreq.addMouseWheelListener(ml);
		tfFreq.addMouseWheelListener(ml);
	}

	/** Get edit fields panel */
	private JPanel getEditFieldsPanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();

		tfFreq.setHorizontalAlignment(SwingConstants.RIGHT);
		ttAmplitude.setHorizontalAlignment(SwingConstants.RIGHT);
		ttDuty.setHorizontalAlignment(SwingConstants.RIGHT);
		ttOffset.setHorizontalAlignment(SwingConstants.RIGHT);

		ttAmplitude.setToolTipText("The amplitude in volts (0 to 20 volts).");
		ttDuty.setToolTipText("The PWM duty cycle in percent (0  to 99%).");
		ttOffset.setToolTipText("The offset (-120\u00b0 to 120\u00b0).");

		var dbButtonGroup = new ButtonGroup();
		dbButtonGroup.add(rb0db);
		dbButtonGroup.add(rb20db);
		var rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		rbPanel.add(rb0db);
		rbPanel.add(rb20db);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Waveform"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, cbWaveType);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Frequency"));
		GuiUtils.addToGridBag(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, tfFreq);

		GuiUtils.addToGridBag(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Amplitude"));
		GuiUtils.addToGridBag(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttAmplitude);

		GuiUtils.addToGridBag(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Duty"));
		GuiUtils.addToGridBag(0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttDuty);

		GuiUtils.addToGridBag(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Offset"));
		GuiUtils.addToGridBag(0, 9, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttOffset);

		GuiUtils.addToGridBag(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel("Attenuation"));
		GuiUtils.addToGridBag(0, 11, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, rbPanel);

		rb0db.addActionListener(e -> cmd.setAttenuation(channel, 0));
		rb20db.addActionListener(e -> cmd.setAttenuation(channel, 1));

		tfFreq.addActionListener(e -> handleFreqChange());
		cbWaveType.addActionListener(e -> {
			if (cbWaveType.isEnabled()) {
				cmd.setWaveForm(channel, cbWaveType.getItemAt(cbWaveType.getSelectedIndex()));
			}
		});

		String propertyName = "value";

		ttAmplitude.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttAmplitude.getValue())
				.map(Number.class::cast).map(Number::doubleValue).ifPresent(n -> cmd.setAmplitude(channel, n)));

		ttDuty.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttDuty.getValue())
				.map(Number.class::cast).map(Number::doubleValue).ifPresent(n -> cmd.setDutyCycle(channel, n)));

		ttOffset.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttOffset.getValue())
				.map(Number.class::cast).map(Number::intValue).ifPresent(n -> cmd.setOffset(channel, n)));

		return panel;
	}

	private JPanel getButtonPanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();

		btEnable.addActionListener(e -> cmd.setEnableChannel(channel, btEnable.isSelected()));

		btInvert.addActionListener(e -> cmd.setInvert(channel, btInvert.isSelected()));

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				btEnable);
		GuiUtils.addToGridBag(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				btInvert);

		return panel;
	}

	private void handleFreqChange() {
		try {
			double f = Utils.parseFreq(tfFreq.getText());
			if (f >= 0 && f <= cmd.getMaxFrequency()) {
				tfFreq.setForeground(Color.BLACK);
				jdFreq.setValue(f);
				cmd.setFrequency(channel, f);
			} else {
				tfFreq.setForeground(Color.RED);
			}
		} catch (ParseException ex) {
			tfFreq.setForeground(Color.RED);
		}
	}
}
