package se.wetterstrom.jfuncgen;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RadialGradientPaint;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.UIManager;

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
public class JogDial extends JComponent implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;
	private static final float[] COLOR_DIST = {0.0f, 0.3f, 1.0f};

	private static final Color BG_COLOR = UIManager.getColor("Panel.background");
	private static final Color[] DIAL_SHADE = { BG_COLOR.brighter(), BG_COLOR, BG_COLOR.darker() };
	private static final Color[] MARKER_SHADE = { BG_COLOR.darker(), BG_COLOR, BG_COLOR.brighter() };

	/** radius */
	private int dialRadius = 50;
	/** marker radius */
	private int markerRadius = 10;

	/** dial color */
	private Color dialColor = Color.GRAY;
	/** marker color */
	private Color markerColor = Color.BLACK;

	/** current angle (radians) */
	private double lastAngle = 0.0;
	/** previous angle (radians) */
	private double previousAngle = 0.0;
	/** marker is clicked and hold */
	private boolean clickedMarker = false;
	/** difference between current and previous angle (radians) */
	private double angleDiff = 0.0;

	/** min value */
	private double minValue = 0.0;
	/** max value */
	private double maxValue = Integer.MAX_VALUE;
	/** value */
	private double value = 0.0;
	/** increase value by this per lap */
	private double lapStep = 10.0;

	/** change listeners */
	private final transient List<JogDialListener> changeListeners = new ArrayList<>();
	/** mouse button listeners */
	private final transient List<JogDialMouseListener> mouseButtonListeners = new ArrayList<>();
	/**
	 * Default constructor.
	 */
	public JogDial() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}

	@Override
	public void paint(Graphics g) {

		dialRadius = Math.max(8, Math.min(getSize().width, getSize().height) / 2);
		markerRadius = Math.max(2, dialRadius/5);
		var g2d = (Graphics2D) g;

		// draw dial
	    var rgp1 = new RadialGradientPaint((float) dialRadius - markerRadius, (float) dialRadius - markerRadius, dialRadius, COLOR_DIST, DIAL_SHADE);
		g2d.setPaint(rgp1);
        g2d.fill(new Ellipse2D.Double(0.0,0.0, dialRadius * 2.0, dialRadius * 2.0));

		// Find the center of the marker
		var pt = getMarkerCenter();
		int xc = (int) pt.getX();
		int yc = (int) pt.getY();

		// draw the marker
	    var rgp2 = new RadialGradientPaint(xc, yc, dialRadius, COLOR_DIST, MARKER_SHADE);
		g2d.setPaint(rgp2);
        g2d.fill(new Ellipse2D.Double((double) xc - markerRadius, (double) yc-markerRadius, markerRadius * 2.0, markerRadius * 2.0));
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(2 * dialRadius, 2 * dialRadius);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(2 * dialRadius, 2 * dialRadius);
	}

	@Override
	public void setForeground(Color fg) {
		this.dialColor = fg;
	}

	@Override
	public Color getForeground() {
		return dialColor;
	}

	/**
	 * Get the current angular position of the dial.
	 *
	 * @return the current angular position of the dial.
	 */
	public double getAngle() {
		return lastAngle;
	}

	private Point getMarkerCenter() {
		int r = dialRadius - markerRadius;
		int xcp = (int) (r * Math.sin(lastAngle));
		int ycp = (int) (r * Math.cos(lastAngle));
		return new Point(dialRadius + xcp, dialRadius - ycp);
	}

	/**
	 * @return true if point is on the marker
	 */
	private boolean isOnMarker(Point pt) {
		return (pt.distance(getMarkerCenter()) < markerRadius);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// no action
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// no action
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// no action
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clickedMarker = isOnMarker(e.getPoint());
		if (!clickedMarker) {
			// clicked outside marker. Set absolute value.
			handleMouseEvent(e);
		}
	}

	/**
	 * When the button is released, the dragging of the marker is disabled.
	 *
	 * @param e reference to a MouseEvent object describing the mouse release.
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		clickedMarker = false;
		mouseButtonListeners.forEach(ml -> ml.mouseButtonReleased(new JogDialEvent(this, lastAngle, angleDiff, value, 0)));
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// no action. handled by mouseDragged.
	}

	/**
	 * Compute the new angle for the marker and repaint the dial. The new angle is
	 * computed based on the new mouse position.
	 *
	 * @param e reference to a MouseEvent object describing the mouse drag.
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		if (clickedMarker) {
			handleMouseEvent(e);
		}
	}

	private void handleMouseEvent(MouseEvent e) {
		int mx = e.getX();
		int my = e.getY();

		previousAngle = lastAngle;

		// mouse position relative to center of dial
		int mxp = mx - dialRadius;
		int myp = dialRadius - my;
		lastAngle = Math.atan2(mxp, myp);
		angleDiff = lastAngle - previousAngle;
		if (angleDiff > Math.PI) {
			angleDiff -= Math.PI * 2;
		}
		if (angleDiff < -Math.PI) {
			angleDiff += Math.PI * 2;
		}

		double diffValue = 0.0;
		if (angleDiff != 0) {
			var newValue = Math.clamp(value + lapStep * (angleDiff / Math.PI), minValue, maxValue);
			diffValue = newValue - value;
			value = newValue;
		}

		repaint();

		var event = new JogDialEvent(this, lastAngle, angleDiff, value, diffValue);
		changeListeners.forEach(cl -> cl.jobDialAdjusted(event));
	}

	/**
	 * Add listener
	 * @param listener the listener
	 */
	public void addJogDialListener(JogDialListener listener) {
		changeListeners.add(listener);
	}

	/**
	 * Add mouse button listener
	 * @param listener the listener
	 */
	public void addMouseButtonListener(JogDialMouseListener listener) {
		mouseButtonListeners.add(listener);
	}

	/**
	 * Get dial radius
	 * @return the radius
	 */
	public int getDialRadius() {
		return dialRadius;
	}

	/**
	 * Set dial radius
	 * @param dialRadius the radius
	 */
	public void setDialRadius(int dialRadius) {
		this.dialRadius = dialRadius;
	}

	/**
	 * Get marker radius
	 * @return marker radius
	 */
	public int getMarkerRadius() {
		return markerRadius;
	}

	/**
	 * Set marker radius
	 * @param markerRadius marker radius
	 */
	public void setMarkerRadius(int markerRadius) {
		this.markerRadius = markerRadius;
	}

	/**
	 * Get dial color
	 * @return color
	 */
	public Color getDialColor() {
		return dialColor;
	}

	/**
	 * Set dial color
	 * @param dialColor color
	 */
	public void setDialColor(Color dialColor) {
		this.dialColor = dialColor;
	}

	/**
	 * Get marker color
	 * @return marker color
	 */
	public Color getMarkerColor() {
		return markerColor;
	}

	/**
	 * Set marker color
	 * @param markerColor marker color
	 */
	public void setMarkerColor(Color markerColor) {
		this.markerColor = markerColor;
	}

	/**
	 * Get minimum value
	 * @return min value
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * Set minimum value
	 * @param minValue the min value
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	/**
	 * Get max value
	 * @return the max value */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * Set max value
	 * @param maxValue the max value */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * Get value
	 * @return the value */
	public double getValue() {
		return value;
	}

	/**
	 * Set value
	 * @param value the value*/
	public void setValue(double value) {
		this.value = value;
	}

	/**
	 * Get lap step
	 * @return the lap step */
	public double getLapStep() {
		return lapStep;
	}
	/**
	 * Set lap step
	 * @param lapStep the lap step*/
	public void setLapStep(double lapStep) {
		this.lapStep = lapStep;
	}

	/**
	 * Event
	 */
	public class JogDialEvent extends java.util.EventObject {

		private static final long serialVersionUID = 1L;
		/** the angle */
		private double angle;
		/** the change */
		private double change;
		/** the value */
		private double value;
		/** the changed value */
		private double valueChange;

		/**
		 * Constructor
		 * @param source the source component
		 * @param angle the angle
		 * @param change change
		 * @param value the value
		 * @param valueChange the changed value
		 */
	    public JogDialEvent(JogDial source, double angle, double change, double value, double valueChange) {
	        super(source);
	        this.angle = angle;
	        this.change = change;
	        this.value = value;
	        this.valueChange = valueChange;
	    }

	    /**
	     * Get dial angle
	     * @return the angle */
	    public double getAngle() {
	        return angle;
	    }

	    /**
	     * Get changed value
	     * @return the change */
	    public double getChange() {
	        return change;
	    }

	    /**
	     * Get value
	     * @return the value */
	    public double getValue() {
	        return value;
	    }

	    /**
	     * Get chaged value
	     * @return the changed value */
	    public double getValueChange() {
	    	return valueChange;
	    }
	}

	/**
	 * Listener for rotating dial
	 */
	public interface JogDialListener extends java.util.EventListener {
		/**
		 * Dial adjusted event handler
		 * @param e the jog dial event
		 */
	    void jobDialAdjusted(JogDialEvent e);
	}

	/**
	 * Listener for mouse click
	 */
	public interface JogDialMouseListener extends java.util.EventListener {
		/**
		 * mouse button release event handler
		 * @param e the job dial event
		 */
	    void mouseButtonReleased(JogDialEvent e);
	}
}