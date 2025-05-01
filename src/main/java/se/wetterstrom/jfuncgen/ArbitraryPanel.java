package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.tokenizer.UnknownFunctionOrVariableException;

/**
 * The arbitrary panel
 */
public class ArbitraryPanel extends JPanel implements FuncTab {

	private static final long serialVersionUID = 1L;

	/** random seed */
	private static final Random RANDOM = new SecureRandom();
	/** data graph */
	private final DataGraphComponent dgc = new DataGraphComponent();
	/** mouse adapter */
	private final transient PanelMouseAdapter mouseAdapter = new PanelMouseAdapter();

	/** the arb data set number */
	private final JComboBox<WaveType> cbArbNum = new JComboBox<>();
	/** read button */
	private final JButton btRead = new JButton("Read");
	/** write button */
	private final JButton btWrite = new JButton("Write");
	/** export button */
	private final JButton btExport = new JButton("Export");
	/** import button */
	private final JButton btImport = new JButton("Import");
	/** edit button */
	private final JButton btEditData = new JButton("EditData");
	/** List of wave form function buttons */
	private final List<JButton> funcButtons = new ArrayList<>();
	/** Function expression field */
	private final JTextField tfFunction = new JTextField("abs(sin(xx*x))");
	/** Decimal number formatter */
	private static final DecimalFormat DOUBLE_FORMATTER = new DecimalFormat("##.####");
	/** xmin field */
	private final JFormattedTextField tfXmin = new JFormattedTextField(DOUBLE_FORMATTER);
	/** xmax field */
	private final JFormattedTextField tfXmax = new JFormattedTextField(DOUBLE_FORMATTER);
	/** the serial command */
	private transient AbstractSerialCom serialCom;

	/** Constructor */
	public ArbitraryPanel() {
		setLayout(new BorderLayout());

		setEnabled(false);
		add(getFuncButtonPanel(), BorderLayout.NORTH);
		add(dgc, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		mouseAdapter.setEnabled(false);
		dgc.addMouseListener(mouseAdapter);
		dgc.addMouseMotionListener(mouseAdapter);
	}

	@Override
	public void setEnabled(boolean enable) {
		super.setEnabled(enable);
		dgc.setEnabled(enable);
		mouseAdapter.setEnabled(enable);
		cbArbNum.setEnabled(enable);
		btRead.setEnabled(enable);
		btWrite.setEnabled(enable);
		btExport.setEnabled(enable);
		btImport.setEnabled(enable);
		btEditData.setEnabled(enable);

		funcButtons.stream().forEach(b -> b.setEnabled(enable));
	}

	/**
	 * Set serial
	 * @param serialCom the serial com
	 */
	public void setSerial(AbstractSerialCom serialCom) {
		this.serialCom = serialCom;
		cbArbNum.removeAllItems();
		serialCom.getWaveTypes(1).stream().filter(w -> w.getName().startsWith("Arb")).forEach(cbArbNum::addItem);
		dgc.initData(serialCom.getArbMax(), serialCom.getArbOffset(), serialCom.getArbSize());
		setEnabled(serialCom.getArbSize() > 0);
	}

	private JPanel createButtonPanel() {
		var panel = new JPanel();

		panel.add(cbArbNum);

		btRead.addActionListener(e -> readData(cbArbNum.getSelectedIndex()));
		panel.add(btRead);
		btWrite.addActionListener(e -> writeData(cbArbNum.getSelectedIndex()));
		panel.add(btWrite);

		btExport.addActionListener(e -> exportFile());
		panel.add(btExport);

		btImport.addActionListener(e -> importFile());
		panel.add(btImport);

		btEditData.addActionListener(e -> openEditData());
		panel.add(btEditData);

		return panel;
	}

	private JPanel getFuncButtonPanel() {
		// sin
		IntUnaryOperator sin = x -> (int) ((Math.sin((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0)
				* serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("sin", e -> dgc.plot(sin), "Sinus"));
		// cos
		IntUnaryOperator cos = x -> (int) ((Math.cos((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0)
				* serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("cos", e -> dgc.plot(cos), "Cosinus"));
		// tan
		IntUnaryOperator tan = x -> (int) ((Math.tan((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0)
				* serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("tan", e -> dgc.plot(tan), "Tangent"));
		// square
		IntUnaryOperator square = x -> Integer.signum(x - serialCom.getArbSize() / 2) * (serialCom.getArbMax() / 2)
				+ (serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("Square", e -> dgc.plot(square), "Square wave"));
		// rand
		IntUnaryOperator rnd = x -> RANDOM.nextInt(serialCom.getArbMax());
		funcButtons.add(createFuncbutton("rnd", e -> dgc.plot(rnd), "Random data"));
		// saw raising
		ActionListener sawR = e -> {
			dgc.drawDataLine(0, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("SawR", sawR, "Sawtooth (raising)"));
		// saw falling
		ActionListener sawF = e -> {
			dgc.drawDataLine(0, serialCom.getArbMax() - 1, serialCom.getArbSize() - 1, 0);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("SawF", sawF, "Sawtooth (falling)"));
		// triangle
		ActionListener triangle = e -> {
			dgc.drawDataLine(0, serialCom.getArbMax() - 1, (serialCom.getArbSize() / 2) - 1, 0);
			dgc.drawDataLine(serialCom.getArbSize() / 2, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("triangle", triangle, "Triangle wave"));
		// flatline
		ActionListener flat = e -> {
			var y = (serialCom.getArbMax() - serialCom.getArbMin()) / 2;
			dgc.drawDataLine(0, y, serialCom.getArbSize() - 1, y);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("Flat", flat, "Flat line"));
		// abs
		IntBinaryOperator abs = (x, y) -> Math.abs(y - serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2;
		funcButtons.add(createFuncbutton("Abs", e -> dgc.plot(abs), "Absolute values"));
		// invert
		IntBinaryOperator inv = (x, y) -> ((serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2) - y;
		funcButtons.add(createFuncbutton("Invert", e -> dgc.plot(inv), "Invert data"));

		//
		funcButtons.add(createFuncbutton("Gain", e -> dgc.gain(), "Gain data"));

		var incBut = createFuncbutton("+", e -> dgc.move(10), "Increase (up)");
		var decBut = createFuncbutton("-", e -> dgc.move(-10), "Decrease (down)");

		MouseWheelListener ml = e -> {
			int notches = e.getWheelRotation();
			int sa = e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ? Math.abs(e.getScrollAmount()) : 1;
			int u = Math.abs(e.getUnitsToScroll());
			dgc.move(notches * sa * u);
		};
		incBut.addMouseWheelListener(ml);
		decBut.addMouseWheelListener(ml);
		funcButtons.add(incBut);
		funcButtons.add(decBut);

		tfXmin.setValue(-Math.PI);
		tfXmax.setValue(Math.PI);
		tfXmin.setHorizontalAlignment(SwingConstants.RIGHT);
		tfXmax.setHorizontalAlignment(SwingConstants.RIGHT);
		tfXmin.setColumns(5);
		tfXmax.setColumns(5);

		var bPanel = new JPanel(new FlowLayout());
		funcButtons.stream().forEach(bPanel::add);

		var bEval = new JButton("Evaluate");
		bEval.addActionListener(e -> evaluateFunction(tfFunction.getText()));

		var index = new AtomicInteger(-1);
		tfFunction.setToolTipText("<html><dl><dt><strong>Constants:</strong></dt><dd> e, &pi;, pi, &phi; </dd>"
				+ "<dt><strong>Functions:</strong></dt><dd><i>"
				+ Stream.of("abs", "acos", "asin", "atan", "cbrt", "ceil", "cos", "cosh", "cot", "exp", "expm1",
						"floor", "log", "log10", "log2", "log1p", "pow", "signum", "sin", "sinh", "sqrt", "tan", "tanh")
						.map(s -> (index.getAndIncrement() % 6 >= 5 ? "<br>" : "") + s)
						.collect(Collectors.joining(", "))
				+ "</i></dd></dl>");

		var gbc = new GridBagConstraints();

		var panel = new JPanel(new GridBagLayout());
		GuiUtils.addToGridBag(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, panel,
				bPanel);

		GuiUtils.addToGridBag(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, panel,
				tfFunction);
		GuiUtils.addToGridBag(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				bEval);

		GuiUtils.addToGridBag(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel(" X "));
		GuiUtils.addToGridBag(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				tfXmin);
		GuiUtils.addToGridBag(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				new JLabel(" - "));
		GuiUtils.addToGridBag(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				tfXmax);
		GuiUtils.addToGridBag(4, 2, 1, 1, 1.0, 0.0, GridBagConstraints.HORIZONTAL, GridBagConstraints.NORTHWEST, gbc,
				panel, new JPanel());

		return panel;
	}

	private void evaluateFunction(String f) {
		if (!Utils.isEmpty(f)) {
			try {
				if (!serialCom.isOnline()) {
					serialCom.getStatusConsumer().accept(StatusBar.Status.OFFLINE, "Offline!");
					return;
				}
				double xmin = Optional.ofNullable(tfXmin.getValue()).map(Number.class::cast).map(Number::doubleValue)
						.filter(d -> !Double.isNaN(d)).orElse(0.0);
				double xmax = Optional.ofNullable(tfXmax.getValue()).map(Number.class::cast).map(Number::doubleValue)
						.filter(d -> !Double.isNaN(d)).orElse(0.0);

				if (xmax - xmin <= 0.0) {
					serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, "Invalid X range!");
					return;
				}

				int size = serialCom.getArbSize();
				if (size < 1) {
					serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, "No data size!");
					return;
				}
				var expr = new ExpressionBuilder(f).variables("x").build();
				dgc.setData(fx(expr, xmin, xmax, size));
			} catch (UnknownFunctionOrVariableException ex) {
				serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, ex.getMessage());
			}
		} else {
			serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, "No function expression specified!");
		}
	}

	private int[] fx(Expression expr, double xmin, double xmax, int size) {

		double gx = (xmax - xmin) / size;
		double[] cy = new double[size];

		double cymin = Double.MAX_VALUE;
		double cymax = Double.MIN_VALUE;
		for (int n = 0; n < size; n++) {
			try {
				double x = xmin + n * gx;
				expr.setVariable("x", x);
				cy[n] = expr.evaluate();
				if (!Double.isNaN(cy[n])) {
					cymin = Math.min(cy[n], cymin);
					cymax = Math.max(cy[n], cymax);
				}
			} catch (ArithmeticException ex) {
				cy[n] = Double.NaN;
			}
		}

		int symin = serialCom.getArbMin();
		int symax = serialCom.getArbMax();
		double gy = (symax - symin) / (cymax - cymin);
		int ax = (int) (symin - cymin * gy);
		int[] d = new int[size];
		for (int n = 0; n < cy.length; n++) {
			double y = Double.isNaN(cy[n]) ? 0.0 : cy[n];
			d[n] = (int) (gy * y) + ax;
		}

		return d;
	}

	private JButton createFuncbutton(String label, ActionListener listener, String tooltip) {
		var b = new JButton(label);
		b.setToolTipText(tooltip);
		b.addActionListener(listener);
		return b;
	}

	private void openEditData() {
		var panel = new ArbDataPanel();
		panel.addHierarchyListener(e -> GuiUtils.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(panel)));
		panel.setData(dgc.getData());

		int res = JOptionPane.showConfirmDialog(this, panel, "Edit Data", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (res == JOptionPane.OK_OPTION) {
			var data = panel.getData();
			for (int i = 0; i < data.length; i++) {
				dgc.setValue(i, data[i]);
			}
			dgc.repaint();
		}
	}

	private void readData(int num) {
		var pm = new ProgressMonitor(this, "Reading data", "", 0, serialCom.getArbSize());
		pm.setProgress(0);
		pm.setMillisToDecideToPopup(100);
		var worker = new Thread() {
			@Override
			public void run() {
				var data = serialCom.getArbData(num, pm);
				if (!pm.isCanceled() && data != null) {
					dgc.setData(data);
				}
				pm.close();
			}
		};
		worker.start();
	}

	private void writeData(int num) {
		var pm = new ProgressMonitor(this, "Writing data", "", 0, serialCom.getArbSize());
		pm.setProgress(0);
		pm.setMillisToDecideToPopup(100);
		var worker = new Thread() {
			@Override
			public void run() {
				int[] data = dgc.getData();
				serialCom.setArbData(num, data, pm);
				pm.close();
			}
		};
		worker.start();
	}

	private void importFile() {
		String fileName = GuiUtils.openTextFileDialog("Import CSV file", null, "", false,
				new String[] { ".csv", ".txt" });
		if (fileName == null) {
			return;
		}
		String str = Utils.readFile(new File(fileName));
		int i = 0;
		for (String s : str.split("\\s*[;\\n\\r]+\\s*")) {
			if (!s.isEmpty()) {
				dgc.setValue(i++, Double.parseDouble(s));
			}
		}
		dgc.repaint();
	}

	private void exportFile() {
		String f = GuiUtils.openTextFileDialog("Export CSV file", null, "", true, new String[] { ".csv", ".txt" });
		if (f != null) {
			try {
				Files.writeString(new File(f).toPath(), dgc.getCSV(), StandardCharsets.ISO_8859_1);
			} catch (IOException ex) {
				GuiUtils.showException(this, "Export to CSV failed", ex,
						"Failed to export arbitrary data to CSV file.");
			}
		}
	}

	/**
	 * mouse adapter
	 */
	class PanelMouseAdapter extends MouseAdapter {
		private Point lastPt = null;
		private boolean enabled = true;

		@Override
		public void mousePressed(MouseEvent e) {
			mouseDragged(e);
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (enabled) {
				var p = e.getPoint();
				p.y = dgc.getHeight() - p.y;
				lastPt = dgc.handlePoint(lastPt, p);
				repaint();
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (enabled) {
				lastPt = null;
				repaint();
			}
		}

		/**
		 * @param enabled set enabled
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}
	}

	@Override
	public void reload() {
		// not used yet
	}
}
