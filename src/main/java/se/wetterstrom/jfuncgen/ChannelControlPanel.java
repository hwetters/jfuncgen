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

	// UI Components grouped by function
	/** Frequency control widgets */
	private final transient FrequencyControls frequencyControls = new FrequencyControls();
	/** Waveform widgets */
	private final transient WaveformControls waveformControls = new WaveformControls(frequencyControls);
	/** Channel buttons */
	private final transient ChannelButtons channelButtons = new ChannelButtons();

	/**
	 * Constructor
	 *
	 * @param channel the channel
	 */
	public ChannelControlPanel(int channel) {
		this.channel = channel;
		initializeUI();
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		waveformControls.setEnabled(enable);
		frequencyControls.setEnabled(enable);
		channelButtons.setEnabled(enable);
	}

	/** reload */
	public void reload() {
		if (cmd == null)
			return;

		waveformControls.reload(cmd, channel);
		frequencyControls.reload(cmd, channel);
		channelButtons.reload(cmd, channel);
	}

	/**
	 * Set serial
	 *
	 * @param cmd the serial command
	 */
	public void setSerial(AbstractSerialCom cmd) {
		this.cmd = cmd;
		waveformControls.setSerial(cmd, channel);
		frequencyControls.setSerial(cmd, channel);
		channelButtons.setSerial(cmd, channel);
	}

	/** Initialize UI */
	private void initializeUI() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				"Channel " + channel));

		// Initialize component panels
		JPanel controlsPanel = waveformControls.createPanel();
		JPanel buttonsPanel = channelButtons.createPanel();
		JPanel dialPanel = frequencyControls.createDialPanel();

		// Add panels to layout
		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this,
				controlsPanel);
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc,
				this, buttonsPanel);
		GuiUtils.addToGridBag(1, 0, 1, 2, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.CENTER, gbc, this,
				dialPanel);

		GuiUtils.fillGridBag(0, 2, 1, 1, gbc, this);
	}

	/**
	 * Waveform controls component
	 */
	private class WaveformControls {
		/** wave type combo box */
		private final JComboBox<WaveType> cbWaveType = new JComboBox<>();
		/** amplitude decimal text field */
		private final JFormattedTextField ttAmplitude = new JFormattedTextField(new DecimalFormat("##.##"));
		/** duty cycle decimal text field */
		private final JFormattedTextField ttDuty = new JFormattedTextField(new DecimalFormat("###.#"));
		/** offset decimal text field */
		private final JFormattedTextField ttOffset = new JFormattedTextField(new DecimalFormat("##.###"));
		/** 0 dB radio button */
		private final JRadioButton rb0db = new JRadioButton("0dB");
		/** -20 dB radio button */
		private final JRadioButton rb20db = new JRadioButton("-20dB");

		private final FrequencyControls frequencyControls;
		public WaveformControls(FrequencyControls frequencyControls) {
			this.frequencyControls=frequencyControls;
		}

		public JPanel createPanel() {
			var panel = new JPanel(new GridBagLayout());
			var gbc = new GridBagConstraints();

			initializeComponents();
			addComponentsToPanel(panel, gbc);
			setupEventHandlers();

			return panel;
		}

		private void initializeComponents() {
			ttAmplitude.setValue(5.0);
			ttDuty.setValue(50.0);
			ttOffset.setValue(0.0);
			rb0db.setSelected(true);

			ttAmplitude.setHorizontalAlignment(SwingConstants.RIGHT);
			ttDuty.setHorizontalAlignment(SwingConstants.RIGHT);
			ttOffset.setHorizontalAlignment(SwingConstants.RIGHT);

			ttAmplitude.setToolTipText("The amplitude in volts (0 to 20 volts).");
			ttDuty.setToolTipText("The PWM duty cycle in percent (0 to 99%).");
			ttOffset.setToolTipText("The offset (-120\u00b0 to 120\u00b0).");
		}

		private void addComponentsToPanel(JPanel panel, GridBagConstraints gbc) {
			var dbButtonGroup = new ButtonGroup();
			dbButtonGroup.add(rb0db);
			dbButtonGroup.add(rb20db);
			var rbPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			rbPanel.add(rb0db);
			rbPanel.add(rb20db);

			GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Waveform"));
			GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, cbWaveType);

			GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Frequency"));
			GuiUtils.addToGridBag(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, frequencyControls.getTfFreq());

			GuiUtils.addToGridBag(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Amplitude"));
			GuiUtils.addToGridBag(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttAmplitude);

			GuiUtils.addToGridBag(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Duty"));
			GuiUtils.addToGridBag(0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttDuty);

			GuiUtils.addToGridBag(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Offset"));
			GuiUtils.addToGridBag(0, 9, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, ttOffset);

			GuiUtils.addToGridBag(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, new JLabel("Attenuation"));
			GuiUtils.addToGridBag(0, 11, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, gbc, panel, rbPanel);
		}

		private void setupEventHandlers() {
			String propertyName = "value";

			ttAmplitude.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttAmplitude.getValue())
					.map(Number.class::cast).map(Number::doubleValue).ifPresent(n -> {
						if (cmd != null)
							cmd.setAmplitude(channel, n);
					}));

			ttDuty.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttDuty.getValue())
					.map(Number.class::cast).map(Number::doubleValue).ifPresent(n -> {
						if (cmd != null)
							cmd.setDutyCycle(channel, n);
					}));

			ttOffset.addPropertyChangeListener(propertyName, e -> Optional.ofNullable(ttOffset.getValue())
					.map(Number.class::cast).map(Number::doubleValue).ifPresent(n -> {
						if (cmd != null)
							cmd.setOffset(channel, n);
					}));

			cbWaveType.addActionListener(e -> {
				if (cbWaveType.isEnabled() && cmd != null) {
					cmd.setWaveForm(channel, cbWaveType.getItemAt(cbWaveType.getSelectedIndex()));
				}
			});

			rb0db.addActionListener(e -> {
				if (cmd != null)
					cmd.setAttenuation(channel, 0);
			});
			rb20db.addActionListener(e -> {
				if (cmd != null)
					cmd.setAttenuation(channel, 1);
			});
		}

		public void setEnabled(boolean enable) {
			cbWaveType.setEnabled(enable);
			ttAmplitude.setEnabled(enable);
			ttDuty.setEnabled(enable);
			ttOffset.setEnabled(enable);
			rb0db.setEnabled(enable);
			rb20db.setEnabled(enable);
		}

		public void reload(AbstractSerialCom cmd, int channel) {
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
		}

		public void setSerial(AbstractSerialCom cmd, int channel) {
			cbWaveType.setEnabled(false);
			cbWaveType.removeAllItems();
			cmd.getWaveTypes(channel).forEach(cbWaveType::addItem);
			cbWaveType.setEnabled(true);
		}
	}

	/**
	 * Frequency controls component
	 */
	private class FrequencyControls {
		/** frequency textfield */
		private final JTextField tfFreq = new JTextField("440.00", 10);
		/** jog dial */
		private final JogDial jdFreq = new JogDial();
		/** increase speed button */
		private final JButton btSpeedInc = new JButton("+");
		/** decrease speed button */
		private final JButton btSpeedDec = new JButton("-");

		public JPanel createDialPanel() {
			initializeComponents();

			var v1 = new JPanel(new BorderLayout());
			v1.add(btSpeedInc, BorderLayout.NORTH);

			var v2 = new JPanel(new BorderLayout());
			v2.add(btSpeedDec, BorderLayout.NORTH);

			var speedPanel = new JPanel(new BorderLayout());
			speedPanel.add(v1, BorderLayout.WEST);
			speedPanel.add(jdFreq, BorderLayout.CENTER);
			speedPanel.add(v2, BorderLayout.EAST);

			setupEventHandlers();

			return speedPanel;
		}

		private void initializeComponents() {
			jdFreq.setMinValue(0.0);
			jdFreq.setMaxValue(25000000);
			setLapSpeed(100.0);

			tfFreq.setHorizontalAlignment(SwingConstants.RIGHT);

			btSpeedInc.setMargin(new Insets(0, 0, 0, 0));
			btSpeedDec.setMargin(new Insets(0, 0, 0, 0));
			btSpeedDec.setPreferredSize(btSpeedInc.getPreferredSize());
		}

		private void setupEventHandlers() {
			var df = new DecimalFormat("#0.00");

			btSpeedInc.addActionListener(e -> setLapSpeed(jdFreq.getLapStep() * 10.0));
			btSpeedDec.addActionListener(e -> setLapSpeed(jdFreq.getLapStep() / 10.0));

			jdFreq.addJogDialListener(ev -> tfFreq.setText(df.format(ev.getValue())));
			jdFreq.addMouseButtonListener(ev -> handleFreqChange());

			tfFreq.addActionListener(e -> handleFreqChange());

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

		protected JTextField getTfFreq() {
			return tfFreq;
		}

		private void setLapSpeed(double lapSpeed) {
			double speed = Math.max(0.1, lapSpeed);
			jdFreq.setLapStep(speed);
			btSpeedInc.setToolTipText("Increase value by " + speed + " per lap");
			btSpeedDec.setToolTipText("Decrease value by " + speed + " per lap");
		}

		private void handleFreqChange() {
			if (cmd == null)
				return;

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

		public void setEnabled(boolean enable) {
			tfFreq.setEnabled(enable);
			jdFreq.setEnabled(enable);
			btSpeedInc.setEnabled(enable);
			btSpeedDec.setEnabled(enable);
		}

		public void reload(AbstractSerialCom cmd, int channel) {
			tfFreq.setText("" + cmd.getFrequency(channel));
		}

		public void setSerial(AbstractSerialCom cmd, int channel) {
			double f = cmd.getMaxFrequency();
			jdFreq.setMaxValue(f);
			tfFreq.setToolTipText(
					"The frequency in Hz (default), kHz, Mhz or GHz (0 to " + (((int) f) / 1000000) + " MHz).");
		}
	}

	/**
	 * Channel buttons component
	 */
	private class ChannelButtons {
		/** invert toggle button */
		private final JToggleButton btInvert = new JToggleButton("Invert");
		/** enable toggle button */
		private final JToggleButton btEnable = new JToggleButton("Enable");

		public JPanel createPanel() {
			var panel = new JPanel(new GridBagLayout());
			var gbc = new GridBagConstraints();

			GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, btEnable);
			GuiUtils.addToGridBag(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc,
					panel, btInvert);

			setupEventHandlers();

			return panel;
		}

		private void setupEventHandlers() {
			btEnable.addActionListener(e -> {
				if (cmd != null)
					cmd.setEnableChannel(channel, btEnable.isSelected());
			});

			btInvert.addActionListener(e -> {
				if (cmd != null)
					cmd.setInvert(channel, btInvert.isSelected());
			});
		}

		public void setEnabled(boolean enable) {
			btEnable.setEnabled(enable);
			btInvert.setEnabled(enable);
		}

		public void reload(AbstractSerialCom cmd, int channel) {
			btInvert.setSelected(cmd.getInvert(channel));
			btEnable.setSelected(cmd.getEnableChannel(channel));
		}

		public void setSerial(AbstractSerialCom cmd, int channel) {
			// Nothing specific needed here
		}
	}
}
