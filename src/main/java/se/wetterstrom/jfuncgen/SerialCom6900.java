package se.wetterstrom.jfuncgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ProgressMonitor;

import com.fazecast.jSerialComm.SerialPort;

import se.wetterstrom.jfuncgen.AdvancedPanel.MeasureMode;
import se.wetterstrom.jfuncgen.AdvancedPanel.SweepDirection;
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
public class SerialCom6900 extends AbstractSerialCom{

	/** Max frequency */
	public static final int MAX_FREQ = 100000000;

	// Serial port settings
	private static final String DEFAULT_PORT_NAME = "ttyUSB0";
	private static final int BAUD_RATE = 115200;
	private static final int DATA_BITS = 8;
	private static final Parity PARITY = Parity.NONE;
	private static final int STOP_BITS = 1;
	private static final int FLOW_CTRL = SerialPort.FLOW_CONTROL_DISABLED;

	/** Constructor */
	public SerialCom6900() {
		super(new PortSettings(DEFAULT_PORT_NAME, BAUD_RATE, DATA_BITS, PARITY, STOP_BITS, FLOW_CTRL));
	}

	@Override
	public double getAmplitude(int channel) {
		return requestReplyDouble((channel&1) == 0 ? "RMA\n" : "RFA\n", 0.0) / 10000.0;
	}

	@Override
	public int[] getArbData(int num, ProgressMonitor pm) {
		// FIXME
		return new int[0];
	}

	@Override
	public int getArbMax() {
		return 8191;
	}

	@Override
	public int getArbMin() {
		return 0;
	}

	@Override
	public int getArbOffset() {
		return 2048;
	}

	@Override
	public int getArbSize() {
		return 8192;
	}

	@Override
	public int getAttenuation(int channel) {
		// FIXME
		return 0;
	}

	@Override
	public int getCount() {
		return requestReplyInt("RCC\n", 0);
	}

	@Override
	public double getDutyCycle(int channel) {
		return requestReplyDouble((channel&1) == 0 ? "RMD\n" : "RFD\n", 50.0);
	}

	@Override
	public boolean getEnableChannel(int channel) {
		// FIXME
		return true;
	}


	@Override
	public boolean getEnableOutput() {
		return false;
	}

	@Override
	public int getExtTtl() {
		// FIXME
		return 0;
	}

	@Override
	public String getFirmware() {
		return requestReply("UVE\n");
	}

	@Override
	public double getFrequency(int channel) {
		return requestReplyDouble((channel &1 ) == 0 ? "RFF\n" : "RMF\n", 0.0);
	}

	@Override
	public int getGateValue() {
		return requestReplyInt("RCG\n", 0);
	}

	@Override
	public List<IdName> getGateValues() {
		var list = new ArrayList<IdName>();
		list.add(new IdName(0, "1 s"));
		list.add(new IdName(1, "10 s"));
		list.add(new IdName(2, "100 s"));
		return Collections.unmodifiableList(list);
	}

	@Override
	public boolean getInvert(int channel) {
		return false;
	}

	@Override
	public double getMaxFrequency() {
		return MAX_FREQ;
	}

	@Override
	public MeasureMode getMeasureMode() {
		return MeasureMode.FREQUENCY;
	}

	@Override
	public String getModel() {
		return requestReply("UMO\n");
	}

	@Override
	public int getOffset(int channel) {
		return requestReplyInt((channel&1) == 0 ? "RFO\n" : "RMO\n", 0);
	}

	@Override
	public int getPhase(int channel) {
		// FIXME
		return 0;
	}

	@Override
	public boolean getPowerOut() {
		return false;
	}

	@Override
	public String getProduct() {
		return requestReply("UID\n");
	}

	@Override
	public double getSweepEnd() {
		return 0.0;
	}

	@Override
	public int getSweepLinLog() {
		return 0;
	}

	@Override
	public double getSweepStart() {
		return 0.0;
	}

	@Override
	public double getSweepTime() {
		return 0.0;
	}

	@Override
	public int getTrace() {
		// FIXME
		return 0;
	}

	@Override
	public int getWaveForm(int channel) {
		return requestReplyInt((channel&1) == 0 ? "RFW\n" : "RMW\n", 0);
	}


	@Override
	public List<WaveType> getWaveTypes(int channel) {
		var list = new ArrayList<WaveType>();
		int i=0;
		list.add(new WaveType(i++, "Sine"));
		list.add(new WaveType(i++, "Square"));
		list.add(new WaveType(i++, "Retangle"));
		list.add(new WaveType(i++, "Trapezoid"));
		list.add(new WaveType(i++, "CMOS"));
		if ((channel&1) == 1) {
			list.add(new WaveType(i++, "Adj-Pulse")); // not available for CH2
		}
		list.add(new WaveType(i++, "DC"));
		list.add(new WaveType(i++, "TRGL"));
		list.add(new WaveType(i++, "Ramp"));
		list.add(new WaveType(i++, "NegRamp"));
		list.add(new WaveType(i++, "Stair-TRGL"));
		list.add(new WaveType(i++, "StairStep"));
		list.add(new WaveType(i++, "NegStair"));
		list.add(new WaveType(i++, "PosExponent"));
		list.add(new WaveType(i++, "NegExponent"));
		list.add(new WaveType(i++, "P-Fall-Exp"));
		list.add(new WaveType(i++, "N-Fall-Exp"));
		list.add(new WaveType(i++, "PosLogaritm"));
		list.add(new WaveType(i++, "NegLogaritm"));
		list.add(new WaveType(i++, "P-Fall-Log"));
		list.add(new WaveType(i++, "N-Fall-Log"));
		list.add(new WaveType(i++, "P-Full-Wave"));
		list.add(new WaveType(i++, "N-Full-Wave"));
		list.add(new WaveType(i++, "P-Half-Wave"));
		list.add(new WaveType(i++, "N-Half-Wave"));
		list.add(new WaveType(i++, "Lorentz-Pulse"));
		list.add(new WaveType(i++, "Multitone"));
		list.add(new WaveType(i++, "Random-Noise"));
		list.add(new WaveType(i++, "ECG"));
		list.add(new WaveType(i++, "Trapezoid"));
		list.add(new WaveType(i++, "Sinc-Pulse"));
		list.add(new WaveType(i++, "Impulse"));
		list.add(new WaveType(i++, "AWGN"));
		list.add(new WaveType(i++, "AM"));
		list.add(new WaveType(i++, "FM"));
		list.add(new WaveType(i++, "Chirp"));
		for (int j = 1; j <= 64; j++) {
	        list.add(new WaveType(i++, "Arbitrary "+j));
		}
		return Collections.unmodifiableList(list);
	}

	@Override
	public void loadSettings(int num) {
		formatSerial("ULN%d\n", num);
	}

	@Override
	public void saveSettings(int num) {
		formatSerial("USN%d\n", num);
	}

	@Override
	public void setAmplitude(int channel, double amplitude) {
		formatSerial((channel&1) == 0 ? "WFA%fd\n" : "WMA%f\n", (float) amplitude);
	}

	@Override
	public void setArbData(int num, int[] data, ProgressMonitor pm) {
		String res1 = requestReply(String.format("DDS_WAVE%d\n", (num+1) ));
		if ("W".equals(res1)) {
			statusConsumer.accept(StatusBar.Status.ONLINE, "OK to write data.");

			byte[] bd = new byte[data.length * 2];
			for (int i = 0; i < data.length; i++) {
				int d = Math.clamp(getArbMax(), data[i], getArbMin());
				bd[i*2] = (byte)(d & 0x7f);
				bd[i*2+1] = (byte)((d >> 7) & 0xff);
			}

			try {
				serialListener.setLineBreakWait(false);
				String res2 = requestReply(bd);

				if ("H".equals(res2)) {
					statusConsumer.accept(StatusBar.Status.ONLINE, "Data acknowledged.");
					// Data received
					var res3 = poll(); // Wait for write to storage
					if ("N".equals(res3)) {
						statusConsumer.accept(StatusBar.Status.ONLINE, "Data stored OK.");
					} else {
						statusConsumer.accept(StatusBar.Status.ONLINE, "Data failed. "+res3);
					}
				} else if ("N".equals(res2)) {
					// Data stored. Done.
					statusConsumer.accept(StatusBar.Status.ONLINE, "Data stored OK.");
				} else if ("HN".equals(res2)) {
					// Received data and stored. Done.
					statusConsumer.accept(StatusBar.Status.ONLINE, "Data receiver and stored OK.");
				} else {
					statusConsumer.accept(StatusBar.Status.ONLINE, "Write data failed. "+res2+" num");
				}
			} finally {
				serialListener.setLineBreakWait(true);
			}

			pm.setProgress(data.length);
		} else {
			pm.close();
			statusConsumer.accept(StatusBar.Status.ONLINE, "Data write failed. "+res1);
		}
	}

	@Override
	public void setAttenuation(int channel, int atten) {
		// FIXME
	}

	@Override
	public void setDutyCycle(int channel, double duty) {
		formatSerial((channel&1) == 0 ? "WFD%f\n" : "WMD%f\n", duty);
	}

	@Override
	public void setEnableChannel(int channel, boolean enabled) {
		formatSerial((channel&1) == 0 ? "WFN%d\n" : "WMN%d\n", enabled ? 1 : 0);
	}

	@Override
	public void setEnableOutput(boolean enable) {
		// FIXME
	}

	@Override
	public void setExtTtl(boolean useTtl) {
		// FIXME
	}

	@Override
	public void setFrequency(int channel, double frequency) {
		if (frequency >= 0 && frequency <= MAX_FREQ) {
			formatSerial((channel&1) == 0 ? "WFF%08d\n" : "WMF%08d\n", (long) frequency);
		}
	}

	@Override
	public void setGateValue(int value) {
		writeSerial(String.format("WCG%d\n", value));
	}

	@Override
	public void setInvert(int ch, boolean enable) {
		// FIXME
	}

	@Override
	public void setMeasureMode(MeasureMode mode) {
		// FIXME
	}

	@Override
	public void setMeasureRunState(int num) {
		formatSerial("WCP%d\n", num);
	}

	@Override
	public void setOffset(int channel, int offset) {
		formatSerial((channel&1) == 0 ? "WFO%08d\n" : "WMO%08d\n", (long) offset);
	}

	@Override
	public void setPhase(int channel, int phase) {
		formatSerial((channel&1) == 0 ? "WFP%d\n" : "WMP%d\n", phase);
	}


	@Override
	public void setPowerOut(boolean enable) {
		// FIXME
	}


	@Override
	public void setResetCounter(int num) {
		writeSerial("WCZ0\n");
	}

	@Override
	public void setSweepEnd(double frequency) {
		if (frequency > 0 && frequency <= MAX_FREQ) {
			formatSerial("SEN%f\n", frequency);
		}
	}

	@Override
	public void setSweepLinLog(int i) {
		formatSerial("SMO%d\n", i & 1);
	}

	@Override
	public void setSweepMode(SweepObject sweepMode) {
		formatSerial("SOB%d\n", sweepMode.id);
	}

	public void setSweepDirection(SweepDirection sweepDirection) {
		// FIXME
	}

	@Override
	public void setSweepSource(SweepSource sweepMode) {
		formatSerial("SXY%d\n", sweepMode.id);
	}

	@Override
	public void setSweepStart(double frequency) {
		if (frequency > 0 && frequency <= MAX_FREQ) {
			formatSerial("SST%f\n", frequency);
		}
	}

	@Override
	public void setSweepState(boolean run) {
		formatSerial("SBE%d\n",run ? 1 : 0);
	}

	@Override
	public void setSweepTime(double seconds) {
		formatSerial("STI%f\n",seconds);
	}

	@Override
	public void setTrace(boolean enable) {
		// FIXME
	}

	@Override
	public void setWaveForm(int channel, WaveType waveform) {
		if (waveform != null) {
			formatSerial((channel&1) == 0 ? "WFW%d\n" : "WMW%d\n", waveform.getId());
		}
	}
}
