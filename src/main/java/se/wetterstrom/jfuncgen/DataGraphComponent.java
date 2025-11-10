package se.wetterstrom.jfuncgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import javax.swing.JComponent;
import javax.swing.JPanel;

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
public class DataGraphComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	private static final String SPLIT_REGEXP = "\\s*[,;]\\s*";

	/** Data model for the graph */
	private final transient DataModel dataModel = new DataModel();

	/** x scale */
	private double xScale = 4.0;
	/** y scale */
	private double yScale = 8.0;

	/** Constructor */
	public DataGraphComponent() {
		super();
	}

	/**
	 * Initialize data
	 * @param max      maximum
	 * @param offset   the offset
	 * @param dataSize the data size
	 */
	public void initData(int max, int offset, int dataSize) {
		dataModel.initialize(max, dataSize);
	}

	/**
	 * Set integer values
	 * @param data the data
	 */
	public void setData(int[] data) {
		dataModel.setData(data);
		repaint();
	}

	/**
	 * Set double value
	 * @param index the index
	 * @param value the value
	 */
	public void setValue(int index, Double value) {
		dataModel.setValue(index, value);
	}

	/**
	 * Set double value
	 * @param index the index
	 * @param value the value
	 */
	public void setValue(int index, Integer value) {
		dataModel.setValue(index, value);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		int ymax = getHeight();
		xScale = ((double) dataModel.getSize()) / getWidth();
		yScale = ((double) dataModel.getMax()) / ymax;

		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), ymax);

		var g2d = (Graphics2D) g;
		drawAxes(g2d, ymax);
		drawData(g2d, ymax);
	}

	/**
	 * Draw the horizontal and vertical axes
	 * @param g2d the graphics context
	 * @param ymax the maximum y value
	 */
	private void drawAxes(Graphics2D g2d, int ymax) {
		g2d.setPaint(Color.BLUE);
		int hy = (int) (dataModel.getMax() / (2.0 * yScale));
		int vx = (int) (dataModel.getSize() / (2.0 * xScale));
		g2d.drawLine(0, hy, (int) ((dataModel.getSize() - 1) / xScale), hy);
		g2d.drawLine(vx, 0, vx, (int) ((dataModel.getMax() - 1) / yScale));
	}

	/**
	 * Draw the data points
	 * @param g2d the graphics context
	 * @param ymax the maximum y value
	 */
	private void drawData(Graphics2D g2d, int ymax) {
		g2d.setPaint(Color.GRAY);
		int[] data = dataModel.getData();
		for (int x0 = 0, x1 = 0; x1 < dataModel.getSize(); x1++) {
			g2d.drawLine(
					(int) (x0 / xScale),
					ymax - ((int) (data[x0] / yScale)),
					(int) (x1 / xScale),
					ymax - ((int) (data[x1] / yScale)));
			x0 = x1;
		}
	}

	/**
	 * Draw line
	 * @param pt0 start point
	 * @param pt1 end point
	 */
	public void drawDataLine(Point pt0, Point pt1) {
		int x0 = trim(pt0.x, 0, dataModel.getSize() - 1, xScale);
		int y0 = trim(pt0.y, 0, dataModel.getMax(), yScale);
		int x1 = trim(pt1.x, 0, dataModel.getSize() - 1, xScale);
		int y1 = trim(pt1.y, 0, dataModel.getMax(), yScale);
		drawDataLine(x0, y0, x1, y1);
	}

	/**
	 * Draw line
	 * @param x1 x1
	 * @param y1 y1
	 * @param x2 x2
	 * @param y2 y2
	 */
	public void drawDataLine(int x1, int y1, int x2, int y2) {
		dataModel.drawLine(x1, y1, x2, y2);
	}

	/**
	 * Handle point
	 * @param pt0 point 1
	 * @param pt1 point 2
	 * @return the point
	 */
	public Point handlePoint(Point pt0, Point pt1) {
		if (pt0 != null) {
			if (pt0.x <= pt1.x) {
				drawDataLine(pt0, pt1);
			} else {
				drawDataLine(pt1, pt0);
			}
		} else {
			dataModel.setValue(
				trim(pt1.x, 0, dataModel.getSize() - 1, xScale),
				trim(pt1.y, 0, dataModel.getMax(), yScale)
			);
		}
		return pt1;
	}

	/**
	 * Plot integer function
	 * @param func the plotter
	 */
	public void plot(IntUnaryOperator func) {
		dataModel.applyFunction(func);
		repaint();
	}

	/**
	 * Plot integer function
	 * @param func the plotter
	 */
	public void plot(IntBinaryOperator func) {
		dataModel.applyFunction(func);
		repaint();
	}

	/** Scale up waveform */
	public void gain() {
		dataModel.gain();
		repaint();
	}

	/**
	 * Move data up/down
	 * @param dist the distance to move
	 */
	public void move(int dist) {
		dataModel.move(dist);
		repaint();
	}

	/**
	 * reset
	 */
	public void reset() {
		dataModel.reset();
	}

	/**
	 * Get data
	 * @return the data
	 */
	public int[] getData() {
		return dataModel.getData();
	}

	/**
	 * Trim
	 * @param value the value
	 * @param min   min
	 * @param max   max
	 * @param scale scale
	 * @return trim
	 */
	private int trim(int value, int min, int max, double scale) {
		return Math.clamp((int) (value * scale), min, max);
	}

	/**
	 * Get CSV
	 * @return data as CSV string
	 */
	public String getCSV() {
		return dataModel.toCSV();
	}

	/**
	 * Parse CSV
	 * @param csv the CSV string to parse
	 * @return number of values
	 */
	public int parseCSV(String csv) {
		return dataModel.fromCSV(csv, SPLIT_REGEXP);
	}

	/**
	 * Resize
	 * @param proportion the proportion
	 */
	public void resize(int proportion) {
		var parent = (JComponent) getParent();
		var insets = parent.getInsets();
		int width = parent.getWidth() - insets.left - insets.right;
		int height = parent.getHeight() - insets.top - insets.bottom;
		width = Math.min(width, height / proportion);
		height = Math.min(width * proportion, height);
		setPreferredSize(new Dimension(width, height));
	}

	/**
	 * Data model for the graph component
	 */
	private static class DataModel {
		/** data */
		private int[] data = new int[0];
		/** size of data */
		private int size = 0;
		/** maximum */
		private int max = 4096;

		/**
		 * Initialize data
		 * @param max maximum value
		 * @param size data size
		 */
		public void initialize(int max, int size) {
			this.size = size;
			this.data = new int[size];
			this.max = max;
			reset();
		}

		/**
		 * Reset data to zeros
		 */
		public void reset() {
			Arrays.fill(data, 0);
		}

		/**
		 * Get data array
		 * @return the data array
		 */
		public int[] getData() {
			return data;
		}

		/**
		 * Get data size
		 * @return the data size
		 */
		public int getSize() {
			return size;
		}

		/**
		 * Get maximum value
		 * @return the maximum value
		 */
		public int getMax() {
			return max;
		}

		/**
		 * Set data from array
		 * @param newData the new data
		 */
		public void setData(int[] newData) {
			for (int i = 0; i < newData.length && i < this.data.length; i++) {
				this.data[i] = newData[i];
			}
		}

		/**
		 * Set value at index
		 * @param index the index
		 * @param value the value
		 */
		public void setValue(int index, Integer value) {
			if (index >= 0 && index < data.length) {
				data[index] = value;
			}
		}

		/**
		 * Set value at index
		 * @param index the index
		 * @param value the value
		 */
		public void setValue(int index, Double value) {
			if (index >= 0 && index < data.length) {
				data[index] = value.intValue();
			}
		}

		/**
		 * Draw line between points
		 * @param x1 start x
		 * @param y1 start y
		 * @param x2 end x
		 * @param y2 end y
		 */
		public void drawLine(int x1, int y1, int x2, int y2) {
			int dx = Math.abs(x2 - x1);
			int dy = Math.abs(y2 - y1);
			int dx2 = 2 * dx;
			int dy2 = 2 * dy;
			int xi = x1 < x2 ? 1 : -1;
			int yi = y1 < y2 ? 1 : -1;
			int x = x1;
			int y = y1;
			int d = 0;
			if (dx >= dy) {
				while (true) {
					data[x] = y;
					if (x == x2) {
						break;
					}
					x += xi;
					d += dy2;
					if (d > dx) {
						y += yi;
						d -= dx2;
					}
				}
			} else {
				while (true) {
					data[x] = y;
					if (y == y2) {
						break;
					}
					y += yi;
					d += dx2;
					if (d > dy) {
						x += xi;
						d -= dy2;
					}
				}
			}
		}

		/**
		 * Apply function to data
		 * @param func the function
		 */
		public void applyFunction(IntUnaryOperator func) {
			for (int x = 0; x < size; x++) {
				data[x] = func.applyAsInt(x);
			}
		}

		/**
		 * Apply binary function to data
		 * @param func the function
		 */
		public void applyFunction(IntBinaryOperator func) {
			for (int x = 0; x < size; x++) {
				data[x] = func.applyAsInt(x, data[x]);
			}
		}

		/**
		 * Scale up waveform
		 */
		public void gain() {
			int h = max / 2;
			int m = 0;
			for (int i = 0; i < size; i++) {
				m = Math.max(m, Math.abs(data[i] - h));
			}
			double k = ((double) m) / ((double) h);
			if (k > 0.0) {
				for (int i = 0; i < size; i++) {
					data[i] = ((int) ((data[i] - h) / k)) + h;
				}
			}
		}

		/**
		 * Move data up/down
		 * @param dist the distance to move
		 */
		public void move(int dist) {
			for (int i = 0; i < size; i++) {
				data[i] = data[i] + dist;
			}
		}

		/**
		 * Convert data to CSV
		 * @return CSV string
		 */
		public String toCSV() {
			var b = new StringBuilder();
			for (var i : data) {
				b.append(i).append('\n');
			}
			return b.toString();
		}

		/**
		 * Parse CSV data
		 * @param csv the CSV string
		 * @param splitRegexp the split regexp
		 * @return number of values parsed
		 */
		public int fromCSV(String csv, String splitRegexp) {
			reset();
			var a = csv.split(splitRegexp);
			for (var i = 0; i < a.length && i < data.length; i++) {
				data[i] = Integer.parseInt(a[i]);
			}
			return a.length;
		}
	}
}
