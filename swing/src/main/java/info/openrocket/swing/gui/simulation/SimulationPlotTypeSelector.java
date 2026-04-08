package info.openrocket.swing.gui.simulation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.JPanel;
import javax.swing.UIManager;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightDataTypeGroup;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.LineStyle;
import info.openrocket.swing.gui.components.ColorChooserButton;
import info.openrocket.swing.gui.plot.PlotTypeSelector;
import info.openrocket.swing.gui.util.Icons;
import net.miginfocom.swing.MigLayout;

public class SimulationPlotTypeSelector extends PlotTypeSelector<FlightDataType, FlightDataTypeGroup> {
	private static final long serialVersionUID = 8745908975963777281L;
	private static final Translator trans = Application.getTranslator();

	private final ColorChooserButton colorButton;
	private final JComboBox<LineStyle> lineStyleSelector;
	private final JButton setDefaultButton;
	private final JButton resetToFactoryButton;

	public SimulationPlotTypeSelector(int plotIndex, FlightDataType type, Unit unit, int position,
									  List<FlightDataType> availableTypes, Color initialColor,
									  LineStyle initialLineStyle) {
		super(plotIndex, type, unit, position, availableTypes);

		JPanel appearancePanel = new JPanel(new MigLayout("ins 0"));
		appearancePanel.add(new JLabel(trans.get("simplotpanel.lbl.Color")), "top");
		colorButton = new ColorChooserButton(initialColor);
		appearancePanel.add(colorButton, "top");

		appearancePanel.add(new JLabel(trans.get("simplotpanel.lbl.LineStyle")), "gapleft para, top");
		lineStyleSelector = new JComboBox<>(LineStyle.values());
		lineStyleSelector.setSelectedItem(initialLineStyle != null ? initialLineStyle : LineStyle.SOLID);
		lineStyleSelector.setRenderer(new LineStyleRenderer());
		appearancePanel.add(lineStyleSelector, "top");

		setDefaultButton = new JButton(Icons.SET_AS_DEFAULT);
		setDefaultButton.setToolTipText(trans.get("simplotpanel.but.SetAsDefault.ttip"));
		appearancePanel.add(setDefaultButton, "gapleft para, top");

		resetToFactoryButton = new JButton(Icons.RESET);
		resetToFactoryButton.setToolTipText(trans.get("simplotpanel.but.ResetToFactory.ttip"));
		appearancePanel.add(resetToFactoryButton, "top");

		this.add(appearancePanel, "newline, gapleft para, spanx, wrap");
	}

	public Color getSelectedColor() {
		return colorButton.getSelectedColor();
	}

	public void setSelectedColor(Color color) {
		colorButton.setSelectedColor(color);
	}

	public LineStyle getSelectedLineStyle() {
		return (LineStyle) lineStyleSelector.getSelectedItem();
	}

	public void setSelectedLineStyle(LineStyle style) {
		lineStyleSelector.setSelectedItem(style);
	}

	public void addColorSelectionListener(PropertyChangeListener listener) {
		colorButton.addColorPropertyChangeListener(listener);
	}

	public void addLineStyleSelectionListener(ActionListener listener) {
		lineStyleSelector.addActionListener(listener);
	}

	public void addSetAsDefaultListener(ActionListener listener) {
		setDefaultButton.addActionListener(listener);
	}

	public void addResetToFactoryListener(ActionListener listener) {
		resetToFactoryButton.addActionListener(listener);
	}

	// Render a small stroke preview alongside the localized line style name.
	private static class LineStyleRenderer implements ListCellRenderer<LineStyle> {
		private final ListCellRenderer<? super LineStyle> delegate = new javax.swing.DefaultListCellRenderer();

		@Override
		public Component getListCellRendererComponent(JList<? extends LineStyle> list, LineStyle value, int index,
													  boolean isSelected, boolean cellHasFocus) {
			JLabel label = (JLabel) delegate.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			LineStyle style = value != null ? value : LineStyle.SOLID;
			label.setText(style.toString());
			label.setIcon(new LineStyleIcon(style, label.getForeground()));
			label.setIconTextGap(8);
			return label;
		}
	}

	private static class LineStyleIcon implements Icon {
		private static final int WIDTH = 48;
		private static final int HEIGHT = 12;
		private static final float STROKE_WIDTH = 2.0f;

		private final LineStyle style;
		private final Color color;

		private LineStyleIcon(LineStyle style, Color color) {
			this.style = style != null ? style : LineStyle.SOLID;
			this.color = color != null ? color : UIManager.getColor("Label.foreground");
		}

		@Override
		public int getIconWidth() {
			return WIDTH;
		}

		@Override
		public int getIconHeight() {
			return HEIGHT;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2 = (Graphics2D) g.create();
			try {
				g2.setColor(color != null ? color : Color.BLACK);
				float[] dashes = style == LineStyle.SOLID ? null : style.getDashes();
				Stroke stroke = new BasicStroke(STROKE_WIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0.0f, dashes, 0.0f);
				g2.setStroke(stroke);
				int midY = y + HEIGHT / 2;
				g2.drawLine(x, midY, x + WIDTH, midY);
			} finally {
				g2.dispose();
			}
		}
	}
}
