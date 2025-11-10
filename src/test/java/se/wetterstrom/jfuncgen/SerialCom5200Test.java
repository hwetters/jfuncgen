package se.wetterstrom.jfuncgen;

import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fazecast.jSerialComm.SerialPort;

import se.wetterstrom.jfuncgen.AdvancedPanel.MeasureMode;


public class SerialCom5200Test {

	private void getBooleanTest(String name, String req, String res, boolean expected, SerialCom5200 com, Function<Integer,Boolean> func) {
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);
		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes(req.getBytes(), req.length(), 0)).thenReturn(req.length());
		Mockito.when(listenerMock.poll(10)).thenReturn(res);
		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		boolean actual = func.apply(1);

		Assert.assertEquals(name, expected, actual);
		Mockito.verify(listenerMock, Mockito.times(1)).flush();
		Mockito.verify(listenerMock, Mockito.times(1)).poll(10);
		Mockito.verify(portMock,  Mockito.times(1)).writeBytes(req.getBytes(), req.length(), 0);
	}

	private void getDoubleTest(String name, String req, String res, double expected, SerialCom5200 com, Function<Integer,Double> func) {
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);
		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes(req.getBytes(), req.length(), 0)).thenReturn(req.length());
		Mockito.when(listenerMock.poll(10)).thenReturn(res);
		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		double actual = func.apply(1);

		Assert.assertEquals(name, expected, actual, 0.01);
		Mockito.verify(listenerMock, Mockito.times(1)).flush();
		Mockito.verify(listenerMock, Mockito.times(1)).poll(10);
		Mockito.verify(portMock,  Mockito.times(1)).writeBytes(req.getBytes(), req.length(), 0);
	}

	private void getIntTest(String name, String req, String res, int expected, SerialCom5200 com, Function<Integer,Integer> func) {
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);
		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes(req.getBytes(), req.length(), 0)).thenReturn(req.length());
		Mockito.when(listenerMock.poll(10)).thenReturn(res);
		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		int actual = func.apply(1);

		Assert.assertEquals(name, expected, actual);
		Mockito.verify(listenerMock, Mockito.times(1)).flush();
		Mockito.verify(listenerMock, Mockito.times(1)).poll(10);
		Mockito.verify(portMock,  Mockito.times(1)).writeBytes(req.getBytes(), req.length(), 0);
	}

	private void getStringTest(String name, String req, String res, String expected, SerialCom5200 com, Function<Integer,String> func) {
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);
		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes(req.getBytes(), req.length(), 0)).thenReturn(req.length());
		Mockito.when(listenerMock.poll(10)).thenReturn(res);
		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		String actual = func.apply(1);

		Assert.assertEquals(name, expected, actual);
		Mockito.verify(listenerMock, Mockito.times(1)).flush();
		Mockito.verify(listenerMock, Mockito.times(1)).poll(10);
		Mockito.verify(portMock,  Mockito.times(1)).writeBytes(req.getBytes(), req.length(), 0);
	}

	@Test
	public void testGetAmplitude() {
		var com = new SerialCom5200();
		getDoubleTest("amplitude", ":r1a\n", ":r1a1234\n", 12.34, com, com::getAmplitude);
	}

	@Test
	public void testGetAttenuation() {
		var com = new SerialCom5200();
		getIntTest("attenuation", ":r1y\n", ":r1y1\n", 1, com, com::getAttenuation);
	}

	@Test
	public void testGetCount() {
		var com = new SerialCom5200();
		getIntTest("count", ":r0e\n", ":r0e0001234\n", 1234, com, a -> com.getCount());
	}

	@Test
	public void testGetDutyCycle() {
		var com = new SerialCom5200();
		getDoubleTest("dutycycle", ":r1d\n", ":r1d1234\n", 123.4, com, com::getDutyCycle);
	}

	@Test
	public void testGetEnableChannel() {
		var com = new SerialCom5200();
		getBooleanTest("enable", ":r1b\n", ":r1b1\n", true, com, com::getEnableChannel);
	}

	@Test
	public void testGetExtTtl() {
		var com = new SerialCom5200();
		getIntTest("extttl", ":r4b\n", ":r4b1\n", 1, com, a -> com.getExtTtl());
	}

	@Test
	public void testGetFirmware() {
		var com = new SerialCom5200();
		getStringTest("firmware", ":r2c\n", ":r2c1234\n", "1234", com, a -> com.getFirmware());
	}

	@Test
	public void testGetFrequency() {
		var com = new SerialCom5200();
		getDoubleTest("frequency", ":r1f\n", ":r1f12345678\n", 123456.78, com, com::getFrequency);
	}

	@Test
	public void testGetGateValue() {
		var com = new SerialCom5200();
		getIntTest("gatevalue", ":r1g\n", ":r2g\n", 2, com, a -> com.getGateValue());
	}


	@Test
	public void testGetGateValues() {
		Assert.assertEquals("getValues.count", 4, new SerialCom5200().getGateValues().size());

	}

	@Test
	public void testGetMeasureRunState() {
		var com = new SerialCom5200();
		getIntTest("measurerunstate", ":r6b\n", ":r6b1\n", 1, com, a -> com.getMeasureRunState());
	}

	@Test
	public void testGetModel() {
		var com = new SerialCom5200();
		getStringTest("model", ":r0c\n", "MDL-A\n", "MDL-A", com, a -> com.getModel());
	}

	@Test
	public void testGetOffset() {
		var com = new SerialCom5200();
		getDoubleTest("offset", ":r1o\n", ":r1o123\n", 3, com, com::getOffset);
	}

	@Test
	public void testGetPhase() {
		var com = new SerialCom5200();
		getDoubleTest("phase", ":r1p\n", ":r1p201\n", 201, com, com::getPhase);
	}


	@Test
	public void testGetPowerOut() {
		var com = new SerialCom5200();
		getBooleanTest("powerout", ":r9b\n", ":r9b1\n", true, com, a -> com.getPowerOut());
	}

	@Test
	public void testGetProduct() {
		var com = new SerialCom5200();
		getStringTest("product", ":r1c\n", "JFA\n", "JFA", com, a -> com.getProduct());
	}

	@Test
	public void testGetSweepEnd() {
		var com = new SerialCom5200();
		getDoubleTest("sweepend", ":r4f\n", ":r4f12345678\n", 123456.78, com, a -> com.getSweepEnd());
	}

	@Test
	public void testGetSweepLinLog() {
		var com = new SerialCom5200();
		getIntTest("getSweepLinLog", ":r7b\n", ":r7b1\n", 1, com, a -> com.getSweepLinLog());
	}

	@Test
	public void testGetSweepStart() {
		var com = new SerialCom5200();
		getDoubleTest("sweepstart", ":r3f\n", ":r3f12345678\n", 123456.78, com, a -> com.getSweepStart());
	}

	@Test
	public void testGetSweepState() {
		var com = new SerialCom5200();
		getBooleanTest("sweepstate", ":r8b\n", ":r8b1\n", true, com, a -> com.getSweepState());
	}

	@Test
	public void testGetSweepTime() {
		var com = new SerialCom5200();
		getDoubleTest("sweeptime", ":r5t\n", ":r5t12345678\n", 123456.78, com, a -> com.getSweepTime());
	}

	@Test
	public void testGetTrace() {
		var com = new SerialCom5200();
		getIntTest("trace", ":r3b\n", ":r3b1\n", 1, com, a -> com.getTrace());
	}

	@Test
	public void testGetWaveForm() {
		var com = new SerialCom5200();
		getIntTest("waveform", ":r1w\n", ":r1w123\n", 123, com, com::getWaveForm);
	}

	@Test
	public void testGetWaveTypes() {
		var com = new SerialCom5200();
		Assert.assertEquals("wavetypes.count", 21, com.getWaveTypes(0).size());
		Assert.assertEquals("wavetypes.count", 21, com.getWaveTypes(1).size());
	}

	@Test
	public void testResetCounter() {
		var com = new SerialCom5200();
		getIntTest("resetcounter", ":r5b\n", ":r5b123\n", 123, com, a -> com.getResetCounter());
	}

	@Test
	public void testGetMeasureMode() {
		String req = ":r1m\n";
		String res = ":r2m\n";
		var com = new SerialCom5200();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);
		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes(req.getBytes(), req.length(), 0)).thenReturn(req.length());
		Mockito.when(listenerMock.poll(10)).thenReturn(res);
		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		MeasureMode actual = com.getMeasureMode();

		Assert.assertEquals("measuremode", MeasureMode.PWM_POS, actual);
		Mockito.verify(listenerMock, Mockito.times(1)).flush();
		Mockito.verify(listenerMock, Mockito.times(1)).poll(10);
		Mockito.verify(portMock,  Mockito.times(1)).writeBytes(req.getBytes(), req.length(), 0);
	}

}
