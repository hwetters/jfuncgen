package se.wetterstrom.jfuncgen;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
public enum DeviceType {
	/** dummy type */
	NONE("", new SerialComDummy()),
	/** MHS 5200 */
	MHS5200("MHS5200", new SerialCom5200()),
	/** FY6900 */
	FY6900("FY6900", new SerialCom6900());

	/** the device name */
	public final String deviceName;
	private final AbstractSerialCom serialCom;

	/**
	 * Hidden constructor
	 * @param name the name
	 * @param serialCom the device setup
	 */
	private DeviceType(String name, AbstractSerialCom serialCom) {
		this.deviceName = name;
		this.serialCom = serialCom;
		serialCom.setDeviceType(this);
	}

	/**
	 * Get serial
	 * @return the serial com
	 */
	public AbstractSerialCom getSerialCom() {
		return serialCom;
	}

	/**
	 * Set consumer of serial port status changes.
	 * @param statusConsumer the consumer
	 */
	protected static void setStatusConsumer(BiConsumer<StatusBar.Status, String> statusConsumer) {
		for (var ft : values()) {
			ft.getSerialCom().setStatusConsumer(statusConsumer);
		}
	}

	@Override
	public String toString() {
		return deviceName;
	}

	/**
	 * Set consumer of data being read from serial port.
	 * @param consumer the consumer
	 */
	protected static void setDataConsumer(Consumer<String> consumer) {
		for (var ft : values()) {
			ft.getSerialCom().addConsumer(consumer);
		}
	}

	/**
	 * Set consumer of data being written to serial port.
	 * @param outputConsumer the serial port
	 */
	protected static void setOutputConsumer(Consumer<String> outputConsumer) {
		for (var ft : values()) {
			ft.getSerialCom().addOutputConsumer(outputConsumer);
		}
	}
}
