package se.wetterstrom.jfuncgen;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import com.fazecast.jSerialComm.SerialPort;

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
public class ConfigPanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	/** device type combo box */
	private final JComboBox<DeviceType> cbDeviceType = new JComboBox<>(DeviceType.values());
	/** serial port combo box */
	private final JComboBox<SerialPort> cbSerialPort = new JComboBox<>();
	/** font size combo box */
	private final JComboBox<Integer> cbFontSize = new JComboBox<>();
	/** Look &amp; Feel combo box */
	private final JComboBox<LookAndFeelInfo> cbLook = new JComboBox<>();
	/** model text field */
	private final JTextField tfModel = new JTextField();
	/** serial text field */
	private final JTextField tfSerial = new JTextField();
	/** firmware text field */
	private final JTextField tfFirmware = new JTextField();

	private final transient Consumer<AbstractSerialCom> serialChangeConsumer;

	/**
	 * Constructor
	 * @param serialChangeConsumer change listener
	 */
	public ConfigPanel(Consumer<AbstractSerialCom> serialChangeConsumer) {
		this.serialChangeConsumer = serialChangeConsumer;
		this.setup();
	}

	private void setup() {
		setLayout(new GridBagLayout());
		var gbc = new GridBagConstraints();

		cbDeviceType.setSelectedItem(Config.getDeviceType());
		cbDeviceType.addActionListener(ev -> Config.setDeviceType((DeviceType) cbDeviceType.getSelectedItem()));

		Function<Object,String> lafMapper = obj -> Optional.ofNullable(obj)
				.filter(LookAndFeelInfo.class::isInstance).map(LookAndFeelInfo.class::cast)
				.map(p->p.getName()).orElse("");		
		cbLook.setRenderer(new MappingListRenderer(lafMapper));
		
		Function<Object,String> portMapper = obj -> Optional.ofNullable(obj)
				.filter(SerialPort.class::isInstance).map(SerialPort.class::cast)
				.map(p->p.getSystemPortName() + " - " + p.getDescriptivePortName()).orElse("");
		cbSerialPort.setRenderer(new MappingListRenderer(portMapper));
		cbSerialPort.addActionListener(ev -> Config.setPort((SerialPort) cbSerialPort.getSelectedItem()));

		tfModel.setEditable(false);
		tfSerial.setEditable(false);
		tfFirmware.setEditable(false);

		var connectBtPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		connectBtPanel.getInsets().top = 0;
		connectBtPanel.getInsets().bottom = 0;
		var connect = new JButton("Connect");
		connect.addActionListener(e-> openPort(true));
		connectBtPanel.add(connect);
		var disconnect = new JButton("Disconnect");
		disconnect.addActionListener(e->  getSerial().disconnect());
		connectBtPanel.add(disconnect);
		var refresh = new JButton("Refresh");
		refresh.addActionListener(e-> populatePortBox());
		connectBtPanel.add(refresh);

		var deviceInfoPanel = new JPanel(new GridLayout(2,3));
		deviceInfoPanel.add(new JLabel("Model"));
		deviceInfoPanel.add(new JLabel("Product"));
		deviceInfoPanel.add(new JLabel("Firmware"));
		deviceInfoPanel.add(tfModel);
		deviceInfoPanel.add(tfSerial);
		deviceInfoPanel.add(tfFirmware);

		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, this, new JLabel("Device type"));
		GuiUtils.addToGridBag(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc, this, new JLabel("Serial port"));

		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, this, cbDeviceType);
		GuiUtils.addToGridBag(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc, this, cbSerialPort);

		GuiUtils.addToGridBag(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.CENTER, gbc, this, connectBtPanel);

		GuiUtils.addToGridBag(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc, this, deviceInfoPanel);

		GuiUtils.addToGridBag(0, 4, 2, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.CENTER, gbc, this, createGuiSetupPanel());
		GuiUtils.fillGridBag(0, 5, 2, 1, gbc, this);

		populatePortBox();
	}


	private JPanel createGuiSetupPanel() {
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();

		Arrays.asList(8, 9, 10, 11, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 32, 36, 40, 44, 48, 54, 60, 66, 72, 80, 88, 96)
			.forEach(cbFontSize::addItem);
		cbFontSize.setSelectedItem(Config.getFontSize());
		cbFontSize.addActionListener(e -> {
			int fs = Optional.ofNullable(cbFontSize.getSelectedItem()).map(Integer.class::cast).orElse(11);
			Config.setFontSize(fs);
			GuiUtils.setDefaultFonts(fs);
			updateGUI( SwingUtilities.getWindowAncestor(this));
		});
		
		Arrays.asList(UIManager.getInstalledLookAndFeels()).forEach(cbLook::addItem);
		cbLook.setSelectedItem(Config.getLook());
		cbLook.addActionListener(e -> {
			var look = cbLook.getItemAt(cbLook.getSelectedIndex());
			Config.setLook(look);
			GuiUtils.setLookAndFeel(look);
			updateGUI( SwingUtilities.getWindowAncestor(this));
		});


		GuiUtils.addToGridBag(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Font size"));
		GuiUtils.addToGridBag(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL,  GridBagConstraints.NORTHWEST,  gbc, panel, cbFontSize);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel, new JLabel("Look"));
		GuiUtils.addToGridBag(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.HORIZONTAL,  GridBagConstraints.NORTHWEST,  gbc, panel, cbLook);
		return panel;
	}

	private void updateGUI(Component parent) {
		SwingUtilities.updateComponentTreeUI(parent);
		parent.invalidate();
		parent.repaint();
	}

	private void populatePortBox() {
		cbSerialPort.removeAllItems();
		cbSerialPort.addItem(null);
		// available serial ports

		var cmd =  getSerial();
		String defaultName = Optional.ofNullable(Config.getPort()).map(SerialPort::getSystemPortName).orElse(cmd.getDefaultPortName());
		for (var p : AbstractSerialCom.getPorts()) {
			cbSerialPort.addItem(p);
			if (defaultName.equals(p.getSystemPortName())) {
				cbSerialPort.setSelectedItem(p);
				cmd.setPort(p);
			}
		}
	}

	private AbstractSerialCom getSerial() {
		return ((DeviceType) cbDeviceType.getSelectedItem()).getSerialCom();
	}

	private void openPort(boolean connect) {
		var cmd = getSerial();
		cmd.setPort((SerialPort) cbSerialPort.getSelectedItem());
		if (connect) {
			serialChangeConsumer.accept(cmd);
			cmd.connect();

			tfModel.setText(getSerial().getModel());
			tfSerial.setText(getSerial().getProduct());
			tfFirmware.setText(getSerial().getFirmware());
		}
	}

	@Override
	public void reload() {
		// ignore
	}

}
