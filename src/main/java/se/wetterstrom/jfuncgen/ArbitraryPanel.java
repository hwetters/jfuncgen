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

	/** Wave function types */
	private enum WaveFunction {
		SIN("sin", "Sinus"),
		COS("cos", "Cosinus"),
		TAN("tan", "Tangent"),
		SQUARE("Square", "Square wave"),
		RANDOM("rnd", "Random data"),
		SAW_RISING("SawR", "Sawtooth (raising)"),
		SAW_FALLING("SawF", "Sawtooth (falling)"),
		TRIANGLE("triangle", "Triangle wave"),
		FLAT("Flat", "Flat line"),
		ABS("Abs", "Absolute values"),
		INVERT("Invert", "Invert data"),
		GAIN("Gain", "Gain data"),
		INCREASE("+", "Increase (up)"),
		DECREASE("-", "Decrease (down)");

		final String label;
		final String tooltip;

		private WaveFunction(String label, String tooltip) {
			this.label = label;
			this.tooltip = tooltip;
		}
	}

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

		// Add combo box for arbitrary data set selection
		panel.add(cbArbNum);

		// Configure and add data operation buttons
		configureDataButtons();
		panel.add(btRead);
		panel.add(btWrite);
		panel.add(btExport);
		panel.add(btImport);
		panel.add(btEditData);
		return panel;
	}

	private void configureDataButtons() {
		btRead.addActionListener(e -> readData(cbArbNum.getSelectedIndex()));
		btWrite.addActionListener(e -> writeData(cbArbNum.getSelectedIndex()));
		btExport.addActionListener(e -> exportFile());
		btImport.addActionListener(e -> importFile());
		btEditData.addActionListener(e -> openEditData());
	}

	private JPanel getFuncButtonPanel() {
		createWaveFunctionButtons();

		MouseWheelListener ml = e -> {
			int notches = e.getWheelRotation();
			int sa = e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL ? Math.abs(e.getScrollAmount()) : 1;
			int u = Math.abs(e.getUnitsToScroll());
			dgc.move(notches * sa * u);
		};

		// Add mouse wheel listeners to increase/decrease buttons
		JButton incButton = funcButtons.stream()
				.filter(b -> b.getText().equals(WaveFunction.INCREASE.label))
				.findFirst().orElse(null);
		JButton decButton = funcButtons.stream()
				.filter(b -> b.getText().equals(WaveFunction.DECREASE.label))
				.findFirst().orElse(null);

		if (incButton != null) incButton.addMouseWheelListener(ml);
		if (decButton != null) decButton.addMouseWheelListener(ml);

		// Initialize X range fields
		initializeRangeFields();

		// Create button panel with all function buttons
		var bPanel = new JPanel(new FlowLayout());
		funcButtons.forEach(bPanel::add);

		// Create evaluate button and function input
		var bEval = new JButton("Evaluate");
		bEval.addActionListener(e -> evaluateFunction(tfFunction.getText()));
		setupFunctionTooltip();

		// Create the panel with GridBagLayout
		var panel = new JPanel(new GridBagLayout());
		var gbc = new GridBagConstraints();

		// Add components to panel
		addComponentsToPanel(panel, gbc, bPanel, bEval);

		return panel;
	}

	private void initializeRangeFields() {
		tfXmin.setValue(-Math.PI);
		tfXmax.setValue(Math.PI);
		tfXmin.setHorizontalAlignment(SwingConstants.RIGHT);
		tfXmax.setHorizontalAlignment(SwingConstants.RIGHT);
		tfXmin.setColumns(5);
		tfXmax.setColumns(5);
	}

	private void setupFunctionTooltip() {
		var index = new AtomicInteger(-1);
		tfFunction.setToolTipText("<html><dl><dt><strong>Constants:</strong></dt><dd> e, &pi;, pi, &phi; </dd>"
				+ "<dt><strong>Functions:</strong></dt><dd><i>"
				+ Stream.of("abs", "acos", "asin", "atan", "cbrt", "ceil", "cos", "cosh", "cot", "exp", "expm1",
						"floor", "log", "log10", "log2", "log1p", "pow", "signum", "sin", "sinh", "sqrt", "tan", "tanh")
						.map(s -> (index.getAndIncrement() % 6 >= 5 ? "<br>" : "") + s)
						.collect(Collectors.joining(", "))
				+ "</i></dd></dl>");
	}

	private void addComponentsToPanel(JPanel panel, GridBagConstraints gbc, JPanel buttonPanel, JButton evalButton) {
		// Add button panel
		GuiUtils.addToGridBag(0, 0, 5, 1, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, panel,
				buttonPanel);

		// Add function input and evaluate button
		GuiUtils.addToGridBag(0, 1, 5, 1, 1.0, 0.0, GridBagConstraints.BOTH, GridBagConstraints.NORTHWEST, gbc, panel,
				tfFunction);
		GuiUtils.addToGridBag(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NONE, GridBagConstraints.NORTHWEST, gbc, panel,
				evalButton);

		// Add X range controls
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
	}

	private void evaluateFunction(String f) {
		if (Utils.isEmpty(f)) {
			serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, "No function expression specified!");
			return;
		}

		try {
			double xmin = getDoubleValue(tfXmin, 0.0);
			double xmax = getDoubleValue(tfXmax, 0.0);

			if (!validateRange(xmin, xmax)) {
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
	}

	private boolean validateSerialConnection() {
		if (!serialCom.isOnline()) {
			serialCom.getStatusConsumer().accept(StatusBar.Status.OFFLINE, "Offline!");
			return false;
		}
		return true;
	}

	private boolean validateRange(double min, double max) {
		if (max - min <= 0.0) {
			serialCom.getStatusConsumer().accept(StatusBar.Status.ERROR, "Invalid X range!");
			return false;
		}
		return true;
	}

	private double getDoubleValue(JFormattedTextField field, double defaultValue) {
		return Optional.ofNullable(field.getValue())
			.map(Number.class::cast)
			.map(Number::doubleValue)
			.filter(d -> !Double.isNaN(d))
			.orElse(defaultValue);
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

	private void createWaveFunctionButtons() {
		// Trigonometric functions
		funcButtons.add(createFuncbutton(
			WaveFunction.SIN.label,
			e -> dgc.plot(x -> (int) ((Math.sin((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0) * serialCom.getArbMax() / 2)),
			WaveFunction.SIN.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.COS.label,
			e -> dgc.plot(x -> (int) ((Math.cos((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0) * serialCom.getArbMax() / 2)),
			WaveFunction.COS.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.TAN.label,
			e -> dgc.plot(x -> (int) ((Math.tan((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0) * serialCom.getArbMax() / 2)),
			WaveFunction.TAN.tooltip));

		// Other wave types
		funcButtons.add(createFuncbutton(
			WaveFunction.SQUARE.label,
			e -> dgc.plot(x -> Integer.signum(x - serialCom.getArbSize() / 2) * (serialCom.getArbMax() / 2) + (serialCom.getArbMax() / 2)),
			WaveFunction.SQUARE.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.RANDOM.label,
			e -> dgc.plot(x -> RANDOM.nextInt(serialCom.getArbMax())),
			WaveFunction.RANDOM.tooltip));

		// Line-based waveforms
		funcButtons.add(createFuncbutton(
			WaveFunction.SAW_RISING.label,
			e -> {
				dgc.drawDataLine(0, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
				dgc.repaint();
			},
			WaveFunction.SAW_RISING.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.SAW_FALLING.label,
			e -> {
				dgc.drawDataLine(0, serialCom.getArbMax() - 1, serialCom.getArbSize() - 1, 0);
				dgc.repaint();
			},
			WaveFunction.SAW_FALLING.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.TRIANGLE.label,
			e -> {
				dgc.drawDataLine(0, serialCom.getArbMax() - 1, (serialCom.getArbSize() / 2) - 1, 0);
				dgc.drawDataLine(serialCom.getArbSize() / 2, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
				dgc.repaint();
			},
			WaveFunction.TRIANGLE.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.FLAT.label,
			e -> {
				var y = (serialCom.getArbMax() - serialCom.getArbMin()) / 2;
				dgc.drawDataLine(0, y, serialCom.getArbSize() - 1, y);
				dgc.repaint();
			},
			WaveFunction.FLAT.tooltip));

		// Data manipulation functions
		funcButtons.add(createFuncbutton(
			WaveFunction.ABS.label,
			e -> dgc.plot((x, y) -> Math.abs(y - serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2),
			WaveFunction.ABS.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.INVERT.label,
			e -> dgc.plot((x, y) -> ((serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2) - y),
			WaveFunction.INVERT.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.GAIN.label,
			e -> dgc.gain(),
			WaveFunction.GAIN.tooltip));

		// Movement buttons
		funcButtons.add(createFuncbutton(
			WaveFunction.INCREASE.label,
			e -> dgc.move(10),
			WaveFunction.INCREASE.tooltip));

		funcButtons.add(createFuncbutton(
			WaveFunction.DECREASE.label,
			e -> dgc.move(-10),
			WaveFunction.DECREASE.tooltip));
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
		if (!validateSerialConnection()) return;

		executeWithProgress("Reading data", pm -> {
			var data = serialCom.getArbData(num, pm);
			if (!pm.isCanceled() && data != null) {
				dgc.setData(data);
			}
		});
	}

	private void writeData(int num) {
		if (!validateSerialConnection()) return;
		executeWithProgress("Writing data", pm -> {
			int[] data = dgc.getData();
			serialCom.setArbData(num, data, pm);
		});
	}

	private void executeWithProgress(String title, ProgressAction action) {
		var pm = new ProgressMonitor(this, title, "", 0, serialCom.getArbSize());
		pm.setProgress(0);
		pm.setMillisToDecideToPopup(100);

		new Thread(() -> {
			try {
				action.execute(pm);
			} finally {
				pm.close();
			}
		}).start();
	}

	@FunctionalInterface
	private interface ProgressAction {
		void execute(ProgressMonitor monitor);
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
