package se.wetterstrom.jfuncgen;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;

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
public class SerialListener implements SerialPortDataListener, Runnable {

	/** The list of consumers being notified of incoming data */
	private final List<Consumer<String>> consumers = new ArrayList<>();
	/** The FIFO queue used to put incoming data from the port. */
	private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
	/** The thread reading from the FIFO queue and notifying the listeners */
	private final Thread runner = new Thread(this);
	/** Set to false to terminate the listener thread */
	private boolean runnerEnabled = true;

	/** the queue of lines */
	private final BlockingQueue<String> lineQueue = new LinkedBlockingQueue<>();

	/** break waiting */
	private boolean lineBreakWait = true;

	/**
	 * Constructor
	 */
	public SerialListener() {
		super();
		runner.start();
	}


	/**
	 * @param timeOut the timeout
	 * @return the value
	 */
	public String poll(long timeOut) {
		try {
			return lineQueue.poll(timeOut, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
		    Thread.currentThread().interrupt();
			return null;
		}
	}

	@Override
	public int getListeningEvents() {
		return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		// Keep this simple. Use the queue.
		if (runnerEnabled) {
			queue.add(new String(event.getReceivedData(), StandardCharsets.ISO_8859_1));
		}
	}

	/**
	 * @param consumer the consumer
	 */
	public void addConsumer(Consumer<String> consumer) {
		consumers.add(consumer);
	}

	/**
	 * @param consumer the consumer
	 */
	public void removeConsumer(Consumer<String> consumer) {
		consumers.remove(consumer);
	}

	/** remove all aconsumers */
	public void removeAllConsumers() {
		consumers.clear();
	}

	/** stop */
	public void stop() {
		runnerEnabled = false;
		flush();
	}

	/** @return queue size */
	public int size() {
		return queue.size();
	}

	/** flush queue */
	public void flush() {
		queue.clear();
		lineQueue.clear();
	}

	/**
	 * @param lineBreakWait the line break wait
	 */
	public void setLineBreakWait(boolean lineBreakWait) {
		this.lineBreakWait = lineBreakWait;
	}

	@Override
	public void run() {
		var buf = new StringBuilder();
		while (runnerEnabled) {
			try {
				String s = queue.take();
				if (s != null && !s.isEmpty()) {
					buf.append(s);
					if (!lineBreakWait || s.endsWith("\n")) {
						String cmd = buf.toString().trim();
						buf.setLength(0);
						if (!cmd.isEmpty()) {
							lineQueue.add(cmd);
						}
					}
					consumers.forEach(c -> c.accept(s));
				}
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}
		}
	}
}
