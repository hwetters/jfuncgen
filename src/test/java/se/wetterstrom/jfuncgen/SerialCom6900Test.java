package se.wetterstrom.jfuncgen;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.fazecast.jSerialComm.SerialPort;

public class SerialCom6900Test {

	@Test
	public void testGetWaveTypes() {
		var com = new SerialCom6900();
		Assert.assertEquals("count", 99, com.getWaveTypes(0).size());
		Assert.assertEquals("count", 100, com.getWaveTypes(1).size());
	}

	@Test
	public void testGetFirmware() {
		var com = new SerialCom6900();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);

		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes("UVE\n".getBytes(), 4, 0)).thenReturn(4);
		Mockito.when(listenerMock.poll(10)).thenReturn("2345");

		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		String fw = com.getFirmware();
		Assert.assertNotNull("fw null", fw);
		Assert.assertEquals("fw", "2345", fw);
	}


	@Test
	public void testGetProduct() {
		var com = new SerialCom6900();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);

		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes("UMO\n".getBytes(), 4, 0)).thenReturn(4);
		Mockito.when(listenerMock.poll(10)).thenReturn("JFB");

		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		String fw = com.getModel();
		Assert.assertNotNull("product null", fw);
		Assert.assertEquals("product", "JFB", fw);
	}

	@Test
	public void testGetModel() {
		var com = new SerialCom6900();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);

		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes("UID\n".getBytes(), 4, 0)).thenReturn(4);
		Mockito.when(listenerMock.poll(10)).thenReturn("MDL-B");

		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		String fw = com.getModel();
		Assert.assertNotNull("model null", fw);
		Assert.assertEquals("model", "MDL-B", fw);
	}


	@Test
	public void testGetAmplitude() {
		var com = new SerialCom6900();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);

		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes("RFA\n".getBytes(), 4, 0)).thenReturn(4);
		Mockito.when(listenerMock.poll(10)).thenReturn("23.45");

		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		double amp = com.getAmplitude(1);
		Assert.assertEquals("amplitude", 23.45, amp * 10000, 0.01);
	}


	@Test
	public void testGetWaveForm() {
		var com = new SerialCom6900();
		var listenerMock = Mockito.mock(SerialListener.class);
		var portMock = Mockito.mock(SerialPort.class);

		Mockito.when(portMock.isOpen()).thenReturn(true);
		Mockito.when(portMock.writeBytes("RMW\n".getBytes(), 5, 0)).thenReturn(5);
		Mockito.when(listenerMock.poll(10)).thenReturn("234");

		com.setPort(portMock);
		com.setSerialListener(listenerMock);

		int res = com.getWaveForm(1);
		Assert.assertEquals("waveform", 234, res);
	}

}
