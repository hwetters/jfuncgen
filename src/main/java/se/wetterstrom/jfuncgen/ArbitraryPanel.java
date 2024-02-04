package se.wetterstrom.jfuncgen;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

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
		funcButtons.add(createFuncbutton("sin", e -> dgc.plot(sin)));
		// cos
		IntUnaryOperator cos = x -> (int) ((Math.cos((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0)
				* serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("cos", e -> dgc.plot(cos)));
		// tan
		IntUnaryOperator tan = x -> (int) ((Math.tan((((double) x) / serialCom.getArbSize()) * 2 * Math.PI) + 1.0)
				* serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("tan", e -> dgc.plot(tan)));
		// square
		IntUnaryOperator square = x -> Integer.signum(x - serialCom.getArbSize() / 2) * (serialCom.getArbMax() / 2)
				+ (serialCom.getArbMax() / 2);
		funcButtons.add(createFuncbutton("Square", e -> dgc.plot(square)));
		// rand
		IntUnaryOperator rnd = x -> RANDOM.nextInt(serialCom.getArbMax());
		funcButtons.add(createFuncbutton("rnd", e -> dgc.plot(rnd)));
		// saw raising
		ActionListener sawR = e -> {
			dgc.drawDataLine(0, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("SawR", sawR));
		// saw falling
		ActionListener sawF = e -> {
			dgc.drawDataLine(0, serialCom.getArbMax() - 1, serialCom.getArbSize() - 1, 0);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("SawF", sawF));
		// triangle
		ActionListener triangle = e -> {
			dgc.drawDataLine(0, serialCom.getArbMax() - 1, (serialCom.getArbSize() / 2) - 1, 0);
			dgc.drawDataLine(serialCom.getArbSize() / 2, 0, serialCom.getArbSize() - 1, serialCom.getArbMax() - 1);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("triangle", triangle));
		// flatline
		ActionListener flat = e -> {
			var y = (serialCom.getArbMax() - serialCom.getArbMin()) / 2;
			dgc.drawDataLine(0, y, serialCom.getArbSize() - 1, y);
			dgc.repaint();
		};
		funcButtons.add(createFuncbutton("Flat", flat));
		// abs
		IntBinaryOperator abs = (x, y) -> Math.abs(y - serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2;
		funcButtons.add(createFuncbutton("Abs", e -> dgc.plot(abs)));
		// invert
		IntBinaryOperator inv = (x, y) -> ((serialCom.getArbMax() / 2) + serialCom.getArbMax() / 2) - y;
		funcButtons.add(createFuncbutton("Invert", e -> dgc.plot(inv)));

		//
		funcButtons.add(createFuncbutton("Gain", e -> dgc.gain()));

		var panel = new JPanel(new FlowLayout());
		funcButtons.stream().forEach(panel::add);
		return panel;
	}

	private JButton createFuncbutton(String label, ActionListener listener) {
		var b = new JButton(label);
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
				lastPt = dgc.handlePoint(lastPt, e.getPoint());
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
