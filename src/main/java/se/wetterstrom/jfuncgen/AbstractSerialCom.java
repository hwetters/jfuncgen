package se.wetterstrom.jfuncgen;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.swing.ProgressMonitor;

import com.fazecast.jSerialComm.SerialPort;

import se.wetterstrom.jfuncgen.AdvancedPanel.MeasureMode;
import se.wetterstrom.jfuncgen.AdvancedPanel.SweepObject;
import se.wetterstrom.jfuncgen.AdvancedPanel.SweepSource;

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
public abstract class AbstractSerialCom {

	private final PortSettings portSettings;

	/** the listener */
	protected SerialListener serialListener = new SerialListener();
	private final Set<Consumer<String>> outputConsumers = new HashSet<>();

	private Optional<SerialPort> port = Optional.empty();

	/** the consumer */
	protected BiConsumer<StatusBar.Status, String> statusConsumer = (a, b) -> System.out.println(a + ":" + b);
	private DeviceType deviceType;

	/**
	 * Constructor
	 *
	 * @param portSettings the settings
	 */
	protected AbstractSerialCom(PortSettings portSettings) {
		this.portSettings = portSettings;
	}

	/**
	 * @return ports
	 */
	protected static SerialPort[] getPorts() {
		return SerialPort.getCommPorts();
	}

	/**
	 * @param serialListener the listener
	 */
	protected void setSerialListener(SerialListener serialListener) {
		this.serialListener = serialListener;
	}

	/**
	 * @param port the port
	 */
	public void setPort(SerialPort port) {
		this.port.filter(p -> !p.equals(port)).ifPresent(p -> {
			p.removeDataListener();
			p.closePort();
		});
		this.port = Optional.ofNullable(port);
		connect();
	}

	/**
	 * @param str the string to be written
	 * @return true if successful
	 */
	public boolean writeSerial(String str) {
		return port.filter(SerialPort::isOpen).map(p -> {
			var data = str.getBytes(StandardCharsets.ISO_8859_1);
			outputConsumers.forEach(c -> c.accept(str));
			int count = p.writeBytes(data, data.length, 0);
			if (count == -1) {
				statusConsumer.accept(StatusBar.Status.ERROR, "Write error");
				return false;
			} else {
				statusConsumer.accept(StatusBar.Status.ONLINE, "");
				return true;
			}
		}).orElseGet(() -> {
			statusConsumer.accept(StatusBar.Status.OFFLINE, "Port is not open");
			return false;
		});
	}

	/**
	 * @param data the bytes to be written
	 * @return true if successful
	 */
	public boolean writeSerial(byte[] data) {
		return port.filter(SerialPort::isOpen).map(p -> {
			outputConsumers.forEach(c -> c.accept(Utils.hexDump(data)));
			int count = p.writeBytes(data, data.length, 0);
			if (count == -1) {
				statusConsumer.accept(StatusBar.Status.ERROR, "Write error");
				return false;
			} else {
				statusConsumer.accept(StatusBar.Status.ONLINE, "");
				return true;
			}
		}).orElseGet(() -> {
			statusConsumer.accept(StatusBar.Status.OFFLINE, "Port is not open");
			return false;
		});
	}

	/**
	 * @param req the request
	 * @return the reply string
	 */
	public String requestReply(String req) {
		serialListener.flush();
		if (writeSerial(req)) {
			return serialListener.poll(10);
		}
		return null;
	}

	/**
	 * @param req          the request
	 * @param defaultValue the default value
	 * @return the value as integer
	 */
	public int requestReplyInt(String req, int defaultValue) {
		try {
			return Integer.parseInt(requestReply(req));
		} catch (NullPointerException | NumberFormatException e) {
			// ignore
		}
		return defaultValue;
	}

	/**
	 * @param req          the request
	 * @param defaultValue the default value
	 * @return the value as double
	 */
	public double requestReplyDouble(String req, double defaultValue) {
		try {
			return Double.parseDouble(requestReply(req));
		} catch (NullPointerException | NumberFormatException e) {
			// ignore
		}
		return defaultValue;
	}

	/**
	 * @param req the request
	 * @return the response
	 */
	public String requestReply(byte[] req) {
		serialListener.flush();
		if (writeSerial(req)) {
			return serialListener.poll(10);
		}
		return null;
	}

	/**
	 * @param fmt  the format string
	 * @param args the arguments
	 */
	public void formatSerial(String fmt, Object... args) {
		writeSerial(String.format(fmt, args));
	}

	/**
	 * @param consumer the consumer
	 */
	public void addOutputConsumer(Consumer<String> consumer) {
		outputConsumers.add(consumer);
	}

	/**
	 * @param consumer the consumer
	 */
	public void addConsumer(Consumer<String> consumer) {
		serialListener.addConsumer(consumer);
	}

	/**
	 * connect
	 */
	public void connect() {
		port.ifPresent(p -> {
			p.setBaudRate(portSettings.baudRate);
			p.setNumDataBits(portSettings.dataBits);
			p.setNumStopBits(portSettings.stopBits);
			p.setParity(portSettings.parity.id);
			p.setFlowControl(portSettings.flowCtrl);
			boolean res = p.openPort();
			if (!res) {
				statusConsumer.accept(StatusBar.Status.OFFLINE, "Failed to connect");
			} else {
				statusConsumer.accept(StatusBar.Status.ONLINE,
						"Connected " + getDeviceType() + " on " + p.getSystemPortName());
				p.removeDataListener();
				p.addDataListener(serialListener);
			}
		});
	}

	/**
	 * @return the device type
	 */
	protected DeviceType getDeviceType() {
		return deviceType;
	}

	/**
	 * @param deviceType the device type
	 */
	protected void setDeviceType(DeviceType deviceType) {
		this.deviceType = deviceType;
	}

	/**
	 * Disconnect
	 */
	public void disconnect() {
		if (port.isPresent()) {
			statusConsumer.accept(StatusBar.Status.OFFLINE, "Disconnected");
			port.get().closePort();
			port = Optional.empty();
		} else {
			statusConsumer.accept(StatusBar.Status.OFFLINE, "Already offline");
		}
	}

	/**
	 * @return the default port name
	 */
	public String getDefaultPortName() {
		return portSettings.defaultPortName;
	}

	/**
	 * @param consumer the status consumer
	 */
	public void setStatusConsumer(BiConsumer<StatusBar.Status, String> consumer) {
		this.statusConsumer = consumer;
	}

	/**
	 * @return true if online
	 */
	public boolean isOnline() {
		return port.map(SerialPort::isOpen).orElse(false);
	}

	/** @return number of samples in one wave form */
	public abstract int getArbSize();

	/** @return the lowest valid value of one sample */
	public abstract int getArbMin();

	/** @return the highest valid value of one sample */
	public abstract int getArbMax();

	/** @return (arbMax - arbMin) / 2 */
	public abstract int getArbOffset();

	/** @return max frequency */
	public abstract double getMaxFrequency();

	/**
	 * @param channel the channel
	 * @return the wave types
	 */
	public abstract List<WaveType> getWaveTypes(int channel);

	/** @return gate values */
	public abstract List<IdName> getGateValues();

	/**
	 * @param num the arb number
	 * @param pm  the progress monitor
	 * @return the arb data
	 */
	public abstract int[] getArbData(int num, ProgressMonitor pm);

	/**
	 * @param num  the arb num
	 * @param data the data
	 * @param pm   the progress monitor
	 */
	public abstract void setArbData(int num, int[] data, ProgressMonitor pm);

	/**
	 * @param channel the channel
	 * @return true if inverted
	 */
	public abstract boolean getInvert(int channel);

	/**
	 * @param channel the channel
	 * @param enabled enabled
	 */
	public abstract void setEnableChannel(int channel, boolean enabled);

	/**
	 * @param enable enable
	 */
	public abstract void setEnableOutput(boolean enable);

	/**
	 * @param enable enable
	 */
	public abstract void setPowerOut(boolean enable);

	/**
	 * @param channel the channel
	 * @return enabled
	 */
	public abstract boolean getEnableChannel(int channel);

	/**
	 * @param channel   the channel
	 * @param frequency the frequency
	 */
	public abstract void setFrequency(int channel, double frequency);

	/**
	 * @param channel the channel
	 * @return the frequency
	 */
	public abstract double getFrequency(int channel);

	/**
	 * @param channel  the channel
	 * @param waveform the waveform
	 */
	public abstract void setWaveForm(int channel, WaveType waveform);

	/**
	 * @param channel the channel
	 * @return the waveform
	 */
	public abstract int getWaveForm(int channel);

	/** @return measure mode */
	public abstract MeasureMode getMeasureMode();

	/** @return model */
	public abstract String getModel();

	/** @return product */
	public abstract String getProduct();

	/** @return firmware */
	public abstract String getFirmware();

	/**
	 * @param channel the channel
	 * @return duty cycle
	 */
	public abstract double getDutyCycle(int channel);

	/**
	 * @param channel the channel
	 * @param duty    the duty cycle
	 */
	public abstract void setDutyCycle(int channel, double duty);

	/**
	 * @param channel the channel
	 * @return the offset
	 */
	public abstract int getOffset(int channel);

	/**
	 * @param channel the channel
	 * @param offset  the offset
	 */
	public abstract void setOffset(int channel, int offset);

	/**
	 * @param channel the channel
	 * @return the phase
	 */
	public abstract int getPhase(int channel);

	/**
	 * @param channel the channel
	 * @param phase   the phase
	 */
	public abstract void setPhase(int channel, int phase);

	/**
	 * @param channel the channel
	 * @return the attenuation
	 */
	public abstract int getAttenuation(int channel);

	/**
	 * @param channel the channel
	 * @param atten   the attenuation
	 */
	public abstract void setAttenuation(int channel, int atten);

	/**
	 * @param channel the channel
	 * @return the amplitude
	 */
	public abstract double getAmplitude(int channel);

	/**
	 * @param channel   the channel
	 * @param amplitude the amplitude
	 */
	public abstract void setAmplitude(int channel, double amplitude);

	/** @return trace mode */
	public abstract int getTrace();

	/** @return true if power output enabled */
	public abstract boolean getPowerOut();

	/** @return true if output enabled */
	public abstract boolean getEnableOutput();

	/** @param enable the enable to set */
	public abstract void setTrace(boolean enable);

	/** @return Ext/TTL */
	public abstract int getExtTtl();

	/** @param useTtl if using TTL */
	public abstract void setExtTtl(boolean useTtl);

	/** @return count */
	public abstract int getCount();

	/** @return gate value */
	public abstract int getGateValue();

	/** @param value the gate value */
	public abstract void setGateValue(int value);

	/** @param frequency the sweep start */
	public abstract void setSweepStart(double frequency);

	/** @param frequency the sweep end */
	public abstract void setSweepEnd(double frequency);

	/** @param seconds the sweep time */
	public abstract void setSweepTime(double seconds);

	/** @return linear/logarithm */
	public abstract int getSweepLinLog();

	/** @param i linear/logarithm */
	public abstract void setSweepLinLog(int i);

	/** @param run sweep runmode */
	public abstract void setSweepState(boolean run);

	/** @param mode the measure mode */
	public abstract void setMeasureMode(MeasureMode mode);

	/** @param num the reset counter value */
	public abstract void setResetCounter(int num);

	/** @param num the measure run state */
	public abstract void setMeasureRunState(int num);

	/** @param num the storage index to load */
	public abstract void loadSettings(int num);

	/** @param num the storage index to save */
	public abstract void saveSettings(int num);

	/** @param sweepMode the mode */
	public abstract void setSweepMode(SweepObject sweepMode);

	/** @param source the sweep source */
	public abstract void setSweepSource(SweepSource source);

	/** @return sweep end */
	public abstract double getSweepEnd();

	/** @return sweep start */
	public abstract double getSweepStart();

	/** @return sweep time */
	public abstract double getSweepTime();

	/**
	 * @param ch     the channel
	 * @param enable enable
	 */
	public abstract void setInvert(int ch, boolean enable);

}

/**
 * Serial Port settings
 */
class PortSettings {

	/** default port name */
	public final String defaultPortName;
	public final int baudRate;
	public final int dataBits;
	public final Parity parity;
	public final int stopBits;
	public final int flowCtrl;

	/**
	 * Constructor
	 * 
	 * @param defaultPortName the default port name
	 * @param baudRate        the baud rate
	 * @param dataBits        the data bits
	 * @param parity          the parity
	 * @param stopBits        the stop bits
	 * @param flowCtrl        the flow control
	 */
	protected PortSettings(String defaultPortName, int baudRate, int dataBits, Parity parity, int stopBits,
			int flowCtrl) {
		this.defaultPortName = defaultPortName;
		this.baudRate = baudRate;
		this.dataBits = dataBits;
		this.parity = parity;
		this.stopBits = stopBits;
		this.flowCtrl = flowCtrl;
	}

	/**
	 * @return flow control as string
	 */
	public String getFlowCtrlAsString() {
		return "" + ((flowCtrl & SerialPort.FLOW_CONTROL_RTS_ENABLED) != 0 ? "RTS " : "")
				+ ((flowCtrl & SerialPort.FLOW_CONTROL_CTS_ENABLED) != 0 ? "CTS " : "")
				+ ((flowCtrl & SerialPort.FLOW_CONTROL_DSR_ENABLED) != 0 ? "DSR " : "")
				+ ((flowCtrl & SerialPort.FLOW_CONTROL_DTR_ENABLED) != 0 ? "DTR " : "")
				+ ((flowCtrl & SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED) != 0 ? "XON-XOFF-IN " : "")
				+ ((flowCtrl & SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED) != 0 ? "XON-XOFF-OUT " : "");
	}

	@Override
	public String toString() {
		return "" + baudRate + " " + dataBits + parity.shortName + stopBits + " " + getFlowCtrlAsString();
	}
}

/**
 * Parity enums
 */
enum Parity {
	/** 0 - none */
	NONE(0, "None", "N"),
	/** 1 - Odd */
	ODD(1, "Odd", "O"),
	/** 2 - Even */
	EVEN(2, "Even", "E"),
	/** 3 - Mark */
	MARK(3, "Mark", "M"),
	/** 4 - Space */
	SPACE(4, "Space", "S");

	final int id;
	final String name;
	final String shortName;

	private Parity(int id, String name, String shortName) {
		this.id = id;
		this.name = name;
		this.shortName = shortName;
	}

	@Override
	public String toString() {
		return name;
	}
}
