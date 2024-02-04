package se.wetterstrom.jfuncgen;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Optional;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

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
public class AdvancedPanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	// Measure
	/** measure mode combo box */
	private final JComboBox<MeasureMode> cbMeasureMode = new JComboBox<>(MeasureMode.values());
	/** gatevalue combo box */
	private final JComboBox<IdName> cbGateValue = new JComboBox<>();
	/** Ext/TTL toggle button */
	private final JToggleButton btExtTTL = new JToggleButton("TTL");
	/** Run/Measure toggle button */
	private final JToggleButton btRunMeasure = new JToggleButton("Run");
	/** reset button */
	private final JButton btResetCounter = new JButton("Reset");

	// Sweep
	/** sweep start */
	private final JTextField tfSweepStart = new JTextField("440.00", 10);
	/** sweep end */
	private final JTextField tfSweepEnd = new JTextField("880.00", 10);
	/** sweep time */
	private final JFormattedTextField tfSweepTime = new JFormattedTextField(new DecimalFormat("##.##"));
	/** Linear sweep mode */
	private final JRadioButton rbSweepModeLin = new JRadioButton("Linear");
	/** Logarithmic sweep mode */
	private final JRadioButton rbSweepModeLog = new JRadioButton("Logarithm");
	/** Run sweep toggle button */
	private final JToggleButton btRunSweep = new JToggleButton("Run sweep");
	/** sweep object combobox */
	private final JComboBox<SweepObject> cbSweepObject = new JComboBox<>(SweepObject.values());
	/** sweep source combobox */
	private final JComboBox<SweepSource> cbSweepSource = new JComboBox<>(SweepSource.values());

	// Settings
	/** load button */
	private final JButton btLoadSettings = new JButton("Load");
	/** save button */
	private final JButton btSaveSettings = new JButton("Save");
	/** setting number combobox */
	private final JComboBox<Integer> cbSettingStore = new JComboBox<>( IntStream.range(0, 16).boxed().toArray(Integer[]::new));

	// Phase
	/** phase number field */
	private final JFormattedTextField tfPhase = new JFormattedTextField(NumberFormat.getIntegerInstance());

	/** serial command */
	private transient AbstractSerialCom cmd = null;
	/** true when disabled */
	private boolean disabled = false;

	/** Constructor */
	public AdvancedPanel() {

		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();

		var sweepModeGrp = new ButtonGroup();
		sweepModeGrp.add(rbSweepModeLin);
		sweepModeGrp.add(rbSweepModeLog);
		rbSweepModeLin.setSelected(true);

		btResetCounter.setEnabled(false);
		btRunMeasure.setEnabled(false);
		cbGateValue.setEnabled(true);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.5, 1.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this, setupMeasurePanel());
		GuiUtils.addToGridBag(1, 0, 1, 1, 0.5, 1.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this, setupSweepPanel());
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.5, 1.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this, setupSettingsPanel());
		GuiUtils.addToGridBag(1, 1, 1, 1, 0.5, 1.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, this, setupPhasePanel());

		// measure
		cbMeasureMode.addActionListener(e -> {
			if (!disabled) {
				var m = cbMeasureMode.getItemAt(cbMeasureMode.getSelectedIndex());
				cmd.setMeasureMode(m);
				btResetCounter.setEnabled(m == MeasureMode.COUNTER);
				btRunMeasure.setEnabled(m == MeasureMode.COUNTER);
				cbGateValue.setEnabled(m == MeasureMode.FREQUENCY);
			}
		});
		
		Function<Object,String> gateNameMapper = obj -> Optional.ofNullable(obj)
				.filter(IdName.class::isInstance).map(IdName.class::cast)
				.map(p->p.value).orElse("");		
		cbGateValue.setRenderer(new MappingListRenderer(gateNameMapper));
		cbGateValue.addActionListener(e -> {
			if (!disabled) {
				cmd.setGateValue(((IdName)cbGateValue.getSelectedItem()).key);
			}
		});
		btExtTTL.addActionListener(e -> {
			if (!disabled) {
				cmd.setExtTtl(btExtTTL.isSelected());
				setTtlExt(btExtTTL.isSelected());
			}
		});

		btResetCounter.addActionListener(e -> cmd.setResetCounter(1));

		btRunMeasure.addActionListener(e -> cmd.setMeasureRunState(btRunMeasure.isSelected() ? 1 : 0));

		// sweep
		tfSweepStart.addActionListener(e -> handleFreqChange(tfSweepStart, cmd::setSweepStart));
		tfSweepEnd.addActionListener(e -> handleFreqChange(tfSweepEnd, cmd::setSweepEnd));
		tfSweepTime.setValue(10);
		tfSweepTime.addPropertyChangeListener("value", e ->
			Optional.ofNullable(tfSweepTime.getValue())
				.map(Number.class::cast)
				.map(Number::doubleValue)
				.ifPresent(cmd::setSweepTime));
		rbSweepModeLin.addActionListener(e -> cmd.setSweepLinLog(0));
		rbSweepModeLog.addActionListener(e -> cmd.setSweepLinLog(1));
		btRunSweep.setSelected(false);
		btRunSweep.addActionListener(e -> cmd.setSweepState(btRunSweep.isSelected()));
		cbSweepObject.addActionListener(e -> cmd.setSweepMode((SweepObject)cbSweepObject.getSelectedItem()));
		cbSweepSource.addActionListener(e -> {
			var source = (SweepSource) cbSweepSource.getSelectedItem();
			tfSweepTime.setEditable(SweepSource.TIME == source);
			cmd.setSweepSource(source);
		});

		// Settings
		btLoadSettings.addActionListener(e -> cmd.loadSettings((int)cbSettingStore.getSelectedItem()));
		btSaveSettings.addActionListener(e -> cmd.saveSettings((int)cbSettingStore.getSelectedItem()));

		// Phase
		tfPhase.setValue(0);
		tfPhase.setColumns(4);
		tfPhase.addActionListener(e ->
			Optional.ofNullable(tfPhase.getValue())
				.map(Number.class::cast)
				.map(Number::intValue)
				.ifPresent(p -> cmd.setPhase(0, p)));
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);

		btResetCounter.setEnabled(enable);
		btRunMeasure.setEnabled(enable);
		cbGateValue.setEnabled(enable);
		btExtTTL.setEnabled(enable);
		btResetCounter.setEnabled(enable);
		btRunMeasure.setEnabled(enable);

		tfSweepStart.setEnabled(enable);
		tfSweepEnd.setEnabled(enable);
		tfSweepTime.setEnabled(enable);
		rbSweepModeLin.setEnabled(enable);
		rbSweepModeLog.setEnabled(enable);
		btRunSweep.setEnabled(enable);
		cbSweepObject.setEnabled(enable);
		cbSweepSource.setEnabled(enable);

		btLoadSettings.setEnabled(enable);
		btSaveSettings.setEnabled(enable);
		cbSettingStore.setEnabled(enable);
		cbMeasureMode.setEnabled(enable);
		tfPhase.setEnabled(enable);
	}

	/**
	 * @param cmd the cmd
	 */
	public void setSerial(AbstractSerialCom cmd) {
		this.cmd = cmd;
		cbGateValue.removeAllItems();
		cmd.getGateValues().forEach(cbGateValue::addItem);
	}

	private JPanel setupSweepPanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();

		tfSweepStart.setHorizontalAlignment(SwingConstants.RIGHT);
		tfSweepEnd.setHorizontalAlignment(SwingConstants.RIGHT);
		tfSweepTime.setHorizontalAlignment(SwingConstants.RIGHT);

		tfSweepStart.setToolTipText("Start sweep frequency.");
		tfSweepEnd.setToolTipText("End sweep frequency.");
		tfSweepTime.setToolTipText("The time in seconds from start to end sweep frequency.");

		var modePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		modePanel.add(rbSweepModeLin);
		modePanel.add(rbSweepModeLog);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep start"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, tfSweepStart);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep end"));
		GuiUtils.addToGridBag(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, tfSweepEnd);

		GuiUtils.addToGridBag(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep time"));
		GuiUtils.addToGridBag(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, tfSweepTime);

		GuiUtils.addToGridBag(0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep mode"));
		GuiUtils.addToGridBag(0, 7, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, modePanel);

		GuiUtils.addToGridBag(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep object"));
		GuiUtils.addToGridBag(0, 9, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, cbSweepObject);

		GuiUtils.addToGridBag(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Sweep source"));
		GuiUtils.addToGridBag(0, 11, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, cbSweepSource);

		GuiUtils.addToGridBag(0, 12, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JPanel());
		GuiUtils.addToGridBag(0, 13, 1, 1, 1.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.CENTER, gbc, panel, btRunSweep);

		GuiUtils.fillGridBag(0, 14, 1, 1, gbc, panel);

		panel.setBorder(BorderFactory.createTitledBorder("Sweep Function"));

		tfSweepTime.setPreferredSize(tfSweepStart.getPreferredSize());

		return panel;
	}

	private JPanel setupMeasurePanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		panel.setBorder(BorderFactory.createTitledBorder("Measure Function"));

		var btPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		btPanel.add(btExtTTL);
		btPanel.add(btResetCounter);
		btPanel.add(btRunMeasure);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Measure Mode"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, cbMeasureMode);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Gate Time"));
		GuiUtils.addToGridBag(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, cbGateValue);

		GuiUtils.addToGridBag(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JPanel());
		GuiUtils.addToGridBag(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, btPanel);

		GuiUtils.fillGridBag(0, 6, 1, 1, gbc, panel);

		return panel;
	}

	private JPanel setupSettingsPanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		panel.setBorder(BorderFactory.createTitledBorder("Settings"));

		btLoadSettings.setToolTipText("Load settings.");
		btSaveSettings.setToolTipText("Save settings.");

		var btPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		btPanel.add(btLoadSettings);
		btPanel.add(btSaveSettings);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Memory"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, cbSettingStore);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, btPanel);

		GuiUtils.fillGridBag(0, 3, 1, 1, gbc, panel);
		return panel;
	}

	private JPanel setupPhasePanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();
		panel.setBorder(BorderFactory.createTitledBorder("Phase"));

		tfPhase.setHorizontalAlignment(SwingConstants.RIGHT);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Phase"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, tfPhase);
		GuiUtils.fillGridBag(0, 2, 1, 1, gbc, panel);

		return panel;
	}

	private void handleFreqChange(JTextField field, DoubleConsumer setter) {
		try {
			double f = Utils.parseFreq(field.getText());
			if (f >= 0 && f <= SerialCom5200.MAX_FREQ) {
				field.setForeground(Color.BLACK);
				setter.accept(f);
			} else {
				field.setForeground(Color.RED);
			}
		} catch (ParseException ex) {
			field.setForeground(Color.RED);
		}
	}

	private void setTtlExt(boolean value) {
		btExtTTL.setText(value ? "EXT" : "TTL");
		if (btExtTTL.isSelected() != value) {
			btExtTTL.setSelected(value);
		}
	}

	/**
	 * MeasureMode
	 */
	enum MeasureMode {
		/** 0 - frequency */
		FREQUENCY(0, "Frequency"),
		/** 1 - counter */
		COUNTER(1, "Counter"),
		/** 2 - positive PVM */
		PWM_POS(2, "Positive PWM"),
		/** 3- negative PVM */
		PWM_NEG(3, "Negative PWM"),
		/** 4 - period */
		PERIOD(4, "Period"),
		/** 5 - duty cycle */
		DUTY(5, "Duty cycle");

		final int id;
		final String name;

		private MeasureMode(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * SweepObject
	 */
	enum SweepObject {
		FREQUENCY(0, "Frequency"),
		AMPLITUDE(1, "Amplitude"),
		OFFSET(2,    "Offset"),
		DUTYCYCLE(3, "Duty cycle");
		final int id;
		final String name;

		private SweepObject(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	/**
	 * SweepSource
	 */
	enum SweepSource {
		TIME(0, "Time"),
		VCO(1, "VCO IN");
		final int id;
		final String name;

		private SweepSource(int id, String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	@Override
	public void reload() {
		try {
			disabled = true;
			tfSweepStart.setText(""+cmd.getSweepStart());
			tfSweepEnd.setText(""+cmd.getSweepEnd());
			tfSweepTime.setText(""+cmd.getSweepTime());
			var measureMode = cmd.getMeasureMode();
			cbMeasureMode.setEnabled(false);
			cbMeasureMode.setSelectedItem(measureMode);
			cbMeasureMode.setEnabled(true);
			if (MeasureMode.FREQUENCY.equals(measureMode)) {
				int v = cmd.getGateValue();
				cbGateValue.setEnabled(false);
				cmd.getGateValues().stream().filter(i -> i.key == v).findAny().ifPresent(cbGateValue::setSelectedItem);
				cbGateValue.setEnabled(true);
			}
			tfPhase.setText("" + cmd.getPhase(0));
			setTtlExt(cmd.getExtTtl() == 0);

			int linlog = cmd.getSweepLinLog();
			if (linlog == 0) {
				rbSweepModeLin.setSelected(true);
			} else {
				rbSweepModeLog.setSelected(true);
			}
		} finally {
			disabled = false;
		}
	}

}
