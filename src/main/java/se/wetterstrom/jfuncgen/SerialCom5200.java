package se.wetterstrom.jfuncgen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
public class SerialCom5200 extends AbstractSerialCom{

	/** max supported frequency - 25 MHz */
	public static final int MAX_FREQ = 25000000;

	// Serial port settings
	/** default port */
	private static final String DEFAULT_PORT_NAME = "ttyUSB0";
	/** baud rate - 57600 */
	private static final int BAUD_RATE = 57600;
	/** data bits - 8 */
	private static final int DATA_BITS = 8;
	/** parity - none */
	private static final Parity PARITY = Parity.NONE;
	/** stop bits - 1 */
	private static final int STOP_BITS = 1;
	/** flow control - RTS/CTS */
	private static final int FLOW_CTRL = SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED;

	/** Number of milliseconds to wait between each sample slice */
	private static final int ARB_READ_SLICE_DELAY = 100;
	/** Number of milliseconds to wait between each sample slice */
	private static final int ARB_WRITE_SLICE_DELAY = 200;
	/** Number of sample slices per wave form */
	protected static final int SLICE_COUNT = 16;
	/** Number of samples per slice */
	protected static final int SAMPLES_PER_SLICE = 128;

	/**
	 * Constructor
	 */
	public SerialCom5200() {
		super(new PortSettings(DEFAULT_PORT_NAME, BAUD_RATE, DATA_BITS, PARITY, STOP_BITS, FLOW_CTRL));
	}

	@Override
	public double getAmplitude(int channel) {
		// :r[12]a[0-9]+ - return amplitude (0 - 2000=20.00V)
		String s = requestReply(String.format(":r%da\n", channel));
		if (s != null && s.startsWith(":")) {
			return Double.parseDouble(s.replaceFirst("^:r\\da\\s*", "").trim()) / 100.0;
		}
		return 0.0;
	}

	@Override
	public int[] getArbData(int num, ProgressMonitor pm) {
		 // :b[0-f][0-f] - return arbitrary wave form data. Hexadecimal waveform number (0x0 - 0xf) followed by a hexadecimal slice number (0x0 - 0xf).
		 // There are 16 arbitrary waveforms. Each waveform consists of unsigned 12-bit samples stored as 16 slices with 128 samples in each.
		 // The arbitrary commands get/set one specified slice in the specified waveform.
		 // Each sample is an integer 0 to 4096, with 2048 being the zero offset.

		int[] data = new int[SLICE_COUNT * SAMPLES_PER_SLICE];
		Arrays.fill(data, 0);
		for (int slice = 0; slice < SLICE_COUNT && !pm.isCanceled(); slice++) {
			String[] arr = Optional.ofNullable(requestReply(String.format(":b%x%x\n", num & 0xf, slice)))
					.map(a->a.replaceFirst("^:b[0-9a-f][0-9a-f]", "")).orElse("").split("\\s*,\\s*");
			pm.setProgress(slice * SAMPLES_PER_SLICE);
			for (int i = 0; i< SAMPLES_PER_SLICE; i++) {
				int v;
				try {
					v = i<arr.length ? Integer.parseInt(arr[i]) : 0;
				} catch (NumberFormatException ex) {
					v = 0;
				}
				data[slice * SAMPLES_PER_SLICE + i] = v;
			}
			try {
				Thread.sleep(ARB_READ_SLICE_DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return data;
	}

	@Override
	public int getArbMax() {
		return 4096;
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
		return SLICE_COUNT * SAMPLES_PER_SLICE;
	}

	@Override
	public int getAttenuation(int channel) {
		// :r[12]y - return attenuation 0=0dB 1=-20dB
		return Optional.ofNullable(requestReply(String.format(":r%dy\n", channel))).map(s->s.replaceFirst("^:r\\dy\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public int getCount() {
		return Optional.ofNullable(requestReply(":r0e\n")).map(s->s.replaceFirst("^:r0e\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public double getDutyCycle(int channel) {
		// :r[12]d - return duty cycle ":r1d500" (50,0%)
		String s = requestReply(String.format(":r%dd\n", channel));
		if (s != null && s.startsWith(":")) {
			return Integer.parseInt(s.replaceFirst("^:r\\dd\\s*", "").trim()) / 10.0;
		}
		return 50.0;
	}

	@Override
	public boolean getEnableChannel(int channel) {
		// :r1b - show which channel is enabled
		String str = requestReply(String.format(":r%db\n", channel));
		int a = Optional.ofNullable(str)
				.map(s->s.replaceFirst(String.format("^:r%db(\\d)\\s*",channel), "$1")).map(String::trim).map(Integer::parseInt).orElse(0);
		return a == channel;
	}

	@Override
	public boolean getEnableOutput() {
		// :r1b - show which channel is enabled
		int a = Optional.ofNullable(requestReply(":r1b\n"))
				.map(s->s.replaceFirst("^:r1b(\\d)\\s*", "$1")).map(String::trim).map(Integer::parseInt).orElse(0);
		return a != 0;
	}

	@Override
	public int getExtTtl() {
		// :r4b - return TTL/EXT mode (0=EXT 1=TTL)
		return Optional.ofNullable(requestReply(":r4b\n"))
				.map(s->s.replaceFirst("^:r4b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public String getFirmware() {
		// r2c - return return firmware version ":r2c\n225A5040000"
		return Optional.ofNullable(requestReply(":r2c\n")).map(s->s.replaceFirst("^:r2c\\s*", "")).map(String::trim).orElse("");
	}

	@Override
	public double getFrequency(int channel) {
		// :r[12]f - return frequency "r1f0000088000" (880 Hz)
		String s = requestReply(String.format(":r%df\n", channel));
		if (s != null && s.startsWith(":")) {
			return Double.parseDouble(s.replaceFirst("^:r\\df\\s*", "").trim()) / 100.0;
		}
		return 0.0;
	}

	@Override
	public int getGateValue() {
		return Optional.ofNullable(requestReply(":r1g\n")).map(s->s.replaceAll("^:r(\\d)g\\s*", "$1")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public List<IdName> getGateValues() {
		var list = new ArrayList<IdName>();
		list.add(new IdName(0, "1 s"));
		list.add(new IdName(1, "10 s"));
		list.add(new IdName(2, "0.01 s"));
		list.add(new IdName(3, "0.1 s"));
		return Collections.unmodifiableList(list);
	}

	@Override
	public boolean getInvert(int channel) {
		return 0 != Optional.ofNullable(requestReply(String.format(":r%cb\n", (channel & 1) == 0 ? 'a' : 'b')))
				.map(s->s.replaceAll("^:r[ab]b(\\d)\\s*", "$1"))
				.map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public double getMaxFrequency() {
		return MAX_FREQ;
	}

	@Override
	public MeasureMode getMeasureMode() {
		int res = Optional.ofNullable(requestReply(":r1m\n")).map(s->s.replaceFirst("^:r(\\d)m\\s*", "$1")).map(String::trim).map(Integer::parseInt).orElse(0);
		for (var m : MeasureMode.values()) {
			if (m.id == res) {
				return m;
			}
		}
		return MeasureMode.COUNTER;
	}

	/**
	 * Get measure running state
	 * @return measure run state
	 */
	public int getMeasureRunState() {
		return Optional.ofNullable(requestReply(":r6b\n")).map(s->s.replaceFirst("^:r6b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public String getModel() {
		// :r0c - return mode number ":r0c5225A5040000"
		return Optional.ofNullable(requestReply(":r0c\n")).map(s->s.replaceFirst("^:r0c\\s*", "")).map(String::trim).orElse("");
	}

	@Override
	public int getOffset(int channel) {
		// :r[12]o - return offset (120=0% 0=-120% 240=120%)
		return Integer.parseInt( Optional.ofNullable(requestReply(String.format(":r%do\n", channel))).map(a->a.replaceFirst("^:r\\do\\s*", "")).map(String::trim).orElse("120")) - 120;
	}

	@Override
	public int getPhase(int channel) {
		// :r[12]p - return phase. 0 - 360 degrees.
		return Optional.ofNullable(requestReply(String.format(":r%dp\n", channel))).map(s->s.replaceFirst("^:r\\dp\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public boolean getPowerOut() {
		int res = Optional.ofNullable(requestReply(":r9b\n")).map(s->s.replaceFirst("^:r9b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
		return res != 0;
	}

	@Override
	public String getProduct() {
		// :r1c -return product number ":r1c\n225A5040000"
		return Optional.ofNullable(requestReply(":r1c\n")).map(s->s.replaceFirst("^:r1c\\s*", "")).map(String::trim).orElse("");
	}

	/**
	 * Reset counter
	 * @return reset counter
	 */
	public int getResetCounter() {
		return Optional.ofNullable(requestReply(":r5b\n")).map(s->s.replaceFirst("^:r5b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public double getSweepEnd() {
		String s = requestReply(":r4f\n");
		if (s != null && s.startsWith(":")) {
			return Double.parseDouble(s.replaceFirst("^:r\\df\\s*", "").trim()) / 100.0;
		}
		return 0.0;
	}

	@Override
	public int getSweepLinLog() {
		return Optional.ofNullable(requestReply(":r7b\n")).map(s->s.replaceFirst("^:r7b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public double getSweepStart() {
		String s = requestReply(":r3f\n");
		if (s != null && s.startsWith(":")) {
			return Double.parseDouble(s.replaceFirst("^:r\\df\\s*", "").trim()) / 100.0;
		}
		return 0.0;
	}

	/**
	 * Get sweep state
	 * @return true if sweepstate
	 */
	public boolean getSweepState() {
		int res = Optional.ofNullable(requestReply(":r8b\n")).map(s->s.replaceFirst("^:r8b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
		return res != 0;
	}

	@Override
	public double getSweepTime() {
		String s = requestReply(":r5t\n");
		if (s != null && s.startsWith(":")) {
			return Double.parseDouble(s.replaceFirst("^:r\\dt\\s*", "").trim()) / 100.0;
		}
		return 0.0;
	}


	@Override
	public int getTrace() {
		// :r3b - show if trace is enabled (:r3b[01])
		return Optional.ofNullable(requestReply(":r3b\n")).map(s->s.replaceFirst("^:r3b\\s*", "")).map(String::trim).map(Integer::parseInt).orElse(0);
	}

	@Override
	public int getWaveForm(int channel) {
		// :r[12]w - return wave type (see WaveType class)
		return Optional.ofNullable(
				requestReply(String.format(":r%dw\n", channel)))
				.map(s->s.replaceFirst("^:r\\dw\\s*", ""))
				.map(String::trim)
				.map(Integer::parseInt).orElse(0);
	}

	@Override
	public List<WaveType> getWaveTypes(int channel) {
		var list = new ArrayList<WaveType>();
		list.add(new WaveType(0,"Sine"));
		list.add(new WaveType(1, "Square"));
		list.add(new WaveType(2, "Triangle"));
		list.add(new WaveType(3, "Sawtooth Rise"));
		list.add(new WaveType(4, "Sawtooth Fall"));
		list.add(new WaveType(100, "Arbitrary 0"));
		list.add(new WaveType(101, "Arbitrary 1"));
		list.add(new WaveType(102, "Arbitrary 2"));
		list.add(new WaveType(103, "Arbitrary 3"));
		list.add(new WaveType(104, "Arbitrary 4"));
		list.add(new WaveType(105, "Arbitrary 5"));
		list.add(new WaveType(106, "Arbitrary 6"));
		list.add(new WaveType(107, "Arbitrary 7"));
		list.add(new WaveType(108, "Arbitrary 8"));
		list.add(new WaveType(109, "Arbitrary 9"));
		list.add(new WaveType(110, "Arbitrary 10"));
		list.add(new WaveType(111, "Arbitrary 11"));
		list.add(new WaveType(112, "Arbitrary 12"));
		list.add(new WaveType(113, "Arbitrary 13"));
		list.add(new WaveType(114, "Arbitrary 14"));
		list.add(new WaveType(115, "Arbitrary 15"));
		return Collections.unmodifiableList(list);
	}

	@Override
	public void loadSettings(int num) {
		formatSerial(":s%xv\n", num & 0xf);
	}

	@Override
	public void saveSettings(int num) {
		formatSerial(":s%xu\n", num & 0xf);
	}

	@Override
	public void setAmplitude(int channel, double amplitude) {
		// :s[12]a[0-9]+ - set amplitude
		formatSerial(":s%da%d\n", channel,  Math.clamp((int)(amplitude * 100),0, 2000));
	}

	@Override
	public void setArbData(int num, int[] data, ProgressMonitor pm) {
		var buf = new StringBuilder();
		for (int slice = 0; slice < SLICE_COUNT ; slice++) {
			buf.setLength(0);
			buf.append(String.format(":a%x%x", num, slice));
			for (int i = 0; i < SAMPLES_PER_SLICE; i++) {
				if (i > 0) {
					buf.append(',');
				}
				int x = slice * SAMPLES_PER_SLICE + i;
				buf.append(x < data.length ? data[x] : 0);
			}
			buf.append('\n');
			try {
				writeSerial(buf.toString());
				Thread.sleep(ARB_WRITE_SLICE_DELAY);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			pm.setProgress(slice * SAMPLES_PER_SLICE);
		}
	}

	@Override
	public void setAttenuation(int channel, int atten) {
		// :s[12]y[01] - set attenuation
		formatSerial(":s%dy%d\n", channel, atten);
	}

	@Override
	public void setDutyCycle(int channel, double duty) {
		// :s[12]d[0-9]+ - set duty cycle (123 = 12.3%)
		formatSerial(":s%dd%03d\n", channel, Math.clamp((int)(duty * 10), 0, 999));
	}

	@Override
	public void setEnableChannel(int channel, boolean enabled) {
		if (channel == 1 && enabled || channel == 2 && !enabled) {
			writeSerial(":s2b1\n");
		} else {
			writeSerial(":s2b2\n");
		}
	}

	@Override
	public void setEnableOutput(boolean enable) {
		formatSerial(":s1b%d\n", enable ? 1 : 0);
	}

	@Override
	public void setExtTtl(boolean useTtl) {
		// :s4b[01] set ETX/TTL mode
		formatSerial(":s4b%d\n", useTtl ? 0 : 1);
	}


	@Override
	public void setFrequency(int channel, double frequency) {
		if (frequency > 0 && frequency <= MAX_FREQ) {
			formatSerial(":s%df%08d\n", channel,  (long) (frequency * 100));
		}
	}

	@Override
	public void setGateValue(int value) {
		formatSerial(":s%dg\n", value);
	}

	@Override
	public void setInvert(int ch, boolean enable) {
		formatSerial(":s%cb%d\n", (ch & 1) == 0 ? 'a' : 'b', enable ? 1 : 0);
	}

	@Override
	public void setMeasureMode(MeasureMode mode) {
		formatSerial(":s%dm\n", mode.id);
	}

	@Override
	public void setMeasureRunState(int num) {
		formatSerial(":s6b%d\n", num);
	}

	@Override
	public void setOffset(int channel, int offset) {
		// :s[12]o[0-9]+ set offset
		formatSerial(":s%do%03d\n", channel, Math.clamp(offset + 120L, 0L, 240L));
	}

	@Override
	public void setPhase(int channel, int phase) {
		// :s[12]p[0-9]+ - set phase
		formatSerial(":s%dp%d\n", channel, Math.abs(phase % 360));
	}

	@Override
	public void setPowerOut(boolean enable) {
		formatSerial(":s9b%d\n", enable ? 1 : 0);
	}

	@Override
	public void setResetCounter(int num) {
		formatSerial(":s5b%d\n", num);
	}

	@Override
	public void setSweepEnd(double frequency) {
		// :s4[0-9]+ set sweep end frequency
		if (frequency > 0 && frequency <= MAX_FREQ) {
			formatSerial(":s4f%08d\n", (long) (frequency * 100));
		}
	}

	@Override
	public void setSweepLinLog(int i) {
		formatSerial(":s7b%d\n", i & 1);
	}

	@Override
	public void setSweepMode(SweepObject sweepMode) {
		// not used
	}

	@Override
	public void setSweepSource(SweepSource source) {
		// not used
	}

	@Override
	public void setSweepStart(double frequency) {
		if (frequency > 0 && frequency <= MAX_FREQ) {
			formatSerial(":s3f%08d\n", (long) (frequency * 100));
		}
	}

	@Override
	public void setSweepState(boolean run) {
		formatSerial(":s8b%d\n", run ? 1 : 0);
	}

	@Override
	public void setSweepTime(double seconds) {
		formatSerial(":s5t%d\n",Math.max((int)seconds, 0));
	}

	@Override
	public void setTrace(boolean enable) {
		// :s3b[01] - enable/disable trace
		formatSerial(":s3b%d\n", enable ? 1 : 0);
	}

	@Override
	public void setWaveForm(int channel, WaveType waveform) {
		if (waveform != null) {
			// :s[12]w[0-9]+ - set wave type
			formatSerial(":s%dw%03d\n", channel, waveform.getId());
		}
	}

	@Override
	public void setSweepDirection(SweepDirection dir) {
		// FXIME
	}
}
