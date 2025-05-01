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
 * Dummy serial
 */
public class SerialComDummy extends AbstractSerialCom {

	// Serial port settings
	private static final String DEFAULT_PORT_NAME = "ttyUSB0";
	private static final int BAUD_RATE = 57600;
	private static final int DATA_BITS = 8;
	private static final Parity PARITY = Parity.NONE;
	private static final int STOP_BITS = 1;
	private static final int FLOW_CTRL = SerialPort.FLOW_CONTROL_DISABLED;

	/**
	 * Constructor
	 */
	public SerialComDummy() {
		super(new PortSettings(DEFAULT_PORT_NAME, BAUD_RATE, DATA_BITS, PARITY, STOP_BITS, FLOW_CTRL));
	}

	@Override
	public void connect() {
		statusConsumer.accept(StatusBar.Status.OFFLINE, "");
	}

	@Override
	public void disconnect() {
		statusConsumer.accept(StatusBar.Status.OFFLINE, "");
	}

	@Override
	public double getAmplitude(int channel) {
		return 0.0;
	}

	@Override
	public int[] getArbData(int num, ProgressMonitor pm) {
		return new int[0];
	}

	@Override
	public int getAttenuation(int channel) {
		return 0;
	}

	@Override
	public int getCount() {
		return 0;
	}

	//

	@Override
	public double getDutyCycle(int channel) {
		return 50.0;
	}

	@Override
	public boolean getEnableChannel(int channel) {
		return false;
	}

	@Override
	public int getExtTtl() {
		return 0;
	}

	@Override
	public String getFirmware() {
		return "";
	}

	@Override
	public double getFrequency(int channel) {
		return 0.0;
	}

	@Override
	public int getGateValue() {
		return 0;
	}

	@Override
	public List<IdName> getGateValues() {
		return Collections.unmodifiableList(new ArrayList<IdName>());
	}

	@Override
	public double getMaxFrequency() {
		return 0;
	}

	@Override
	public String getModel() {
		return "";
	}

	@Override
	public int getOffset(int channel) {
		return 0;
	}

	@Override
	public int getPhase(int channel) {
		return 0;
	}

	@Override
	public String getProduct() {
		return "";
	}

	@Override
	public int getTrace() {
		return 0;
	}

	@Override
	public int getWaveForm(int channel) {
		return 0;
	}

	@Override
	public List<WaveType> getWaveTypes(int channel) {
		return Collections.unmodifiableList(new ArrayList<WaveType>());
	}

	@Override
	public void loadSettings(int num) {/* unused */}

	@Override
	public void saveSettings(int num) {/* unused */}

	@Override
	public void setAmplitude(int channel, double amplitude) {/* unused */}

	@Override
	public void setArbData(int num, int[] data, ProgressMonitor pm) { /* unused */ }

	@Override
	public void setAttenuation(int channel, int atten) {/* unused */}

	@Override
	public void setDutyCycle(int channel, double duty) {/* unused */}

	@Override
	public void setEnableChannel(int channel, boolean enabled) {/* unused */}

	@Override
	public void setEnableOutput(boolean enable) {/* unused */}

	@Override
	public void setExtTtl(boolean useTtl) {/* unused */}

	@Override
	public void setFrequency(int channel, double frequency) {/* unused */}

	@Override
	public void setGateValue(int value) {/* unused */}

	@Override
	public void setInvert(int ch, boolean enable) {/* unused */}

	@Override
	public void setMeasureMode(MeasureMode mode) {/* unused */}

	@Override
	public void setMeasureRunState(int num) {/* unused */}

	@Override
	public void setOffset(int channel, int offset) {/* unused */}

	@Override
	public void setPhase(int channel, int phase) {/* unused */}

	@Override
	public void setPort(SerialPort port) {/* unused */}

	@Override
	public void setPowerOut(boolean enable) {/* unused */}

	@Override
	public void setResetCounter(int num) {/* unused */}

	@Override
	public void setSweepEnd(double frequency) {/* unused */}

	@Override
	public void setSweepLinLog(int i) {/* unused */}

	@Override
	public void setSweepMode(SweepObject sweepMode) { /* unused */ }

	@Override
	public void setSweepSource(SweepSource source) {/* unused */}

	@Override
	public void setSweepStart(double frequency) {/* unused */}

	@Override
	public void setSweepState(boolean run) {/* unused */}

	@Override
	public void setSweepTime(double seconds) {/* unused */}

	@Override
	public void setTrace(boolean enable) {/* unused */}

	@Override
	public void setWaveForm(int channel, WaveType waveform) {/* unused */}

	@Override
	public boolean writeSerial(String str) {
		return false;
	}

	@Override
	public boolean isOnline() {
		return false;
	}

	@Override
	public int getArbSize() {
		return 0;
	}


	@Override
	public int getArbMin() {
		return 0;
	}


	@Override
	public int getArbMax() {
		return 0;
	}


	@Override
	public int getArbOffset() {
		return 0;
	}

	@Override
	public double getSweepEnd() {
		return 0;
	}

	@Override
	public double getSweepStart() {
		return 0;
	}

	@Override
	public double getSweepTime() {
		return 0;
	}

	@Override
	public MeasureMode getMeasureMode() {
		return null;
	}

	@Override
	public int getSweepLinLog() {
		return 0;
	}

	@Override
	public boolean getInvert(int channel) {
		return false;
	}

	@Override
	public boolean getPowerOut() {
		return false;
	}

	@Override
	public boolean getEnableOutput() {
		return false;
	}

	@Override
	public void setSweepDirection(SweepDirection dir) {	/* unused */ }
}
