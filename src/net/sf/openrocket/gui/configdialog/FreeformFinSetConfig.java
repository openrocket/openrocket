package net.sf.openrocket.gui.configdialog;


import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.scalefigure.FinPointFigure;
import net.sf.openrocket.gui.scalefigure.ScaleScrollPane;
import net.sf.openrocket.gui.scalefigure.ScaleSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;

public class FreeformFinSetConfig extends FinSetConfig {

	private final FreeformFinSet finset;
	private JTable table = null;
	private FinPointTableModel tableModel = null;
	
	private FinPointFigure figure = null;
	private static final Translator trans = Application.getTranslator();

	
	public FreeformFinSetConfig(RocketComponent component) {
		super(component);
		this.finset = (FreeformFinSet)component;

		//// General and General properties
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.General"), null, generalPane(), 
				trans.get("FreeformFinSetCfg.tab.ttip.General"), 0);
		//// Shape and Fin shape
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.Shape"), null, shapePane(), 
				trans.get("FreeformFinSetCfg.tab.ttip.Finshape"), 1);
		tabbedPane.setSelectedIndex(0);
		
		addFinSetButtons();
	}
	
	
	
	private JPanel generalPane() {

		DoubleModel m;
		JSpinner spin;
		JComboBox combo;
		
		JPanel mainPanel = new JPanel(new MigLayout("fill"));
		
		JPanel panel = new JPanel(new MigLayout("fill, gap rel unrel","[][65lp::][30lp::]",""));
		
		
		
		////  Number of fins:
		panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Numberoffins")));
		
		IntegerModel im = new IntegerModel(component,"FinCount",1,8);
		
		spin = new JSpinner(im.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx, wrap");
		
		
		////  Base rotation
		panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Finrotation")));
		
		m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE,-Math.PI,Math.PI);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(-Math.PI,Math.PI)),"w 100lp, wrap");
		
		

		////  Fin cant
		JLabel label = new JLabel(trans.get("FreeformFinSetCfg.lbl.Fincant"));
		//// The angle that the fins are canted with respect to the rocket body.
		label.setToolTipText(trans.get("FreeformFinSetCfg.lbl.ttip.Fincant"));
		panel.add(label);
		
		m = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE,
				-FinSet.MAX_CANT,FinSet.MAX_CANT);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(-FinSet.MAX_CANT,FinSet.MAX_CANT)),
				"w 100lp, wrap 40lp");
		
		
		
		////  Position
		//// Position relative to:
		panel.add(new JLabel("FreeformFinSetCfg.lbl.Posrelativeto"));

		combo = new JComboBox(
				new EnumModel<RocketComponent.Position>(component, "RelativePosition",
						new RocketComponent.Position[] {
						RocketComponent.Position.TOP,
						RocketComponent.Position.MIDDLE,
						RocketComponent.Position.BOTTOM,
						RocketComponent.Position.ABSOLUTE
				}));
		panel.add(combo,"spanx 3, growx, wrap");
				//// plus
		panel.add(new JLabel("FreeformFinSetCfg.lbl.plus"),"right");

		m = new DoubleModel(component,"PositionValue",UnitGroup.UNITS_LENGTH);
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(
				new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
				new DoubleModel(component.getParent(), "Length"))),
				"w 100lp, wrap");

		
		
		

		mainPanel.add(panel, "aligny 20%");
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy, height 150lp");
		
		
		panel = new JPanel(new MigLayout("gap rel unrel","[][65lp::][30lp::]",""));
		
		
		
		
		////  Cross section
		//// Fin cross section:
		panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.FincrossSection")),"span, split");
		combo = new JComboBox(
				new EnumModel<FinSet.CrossSection>(component,"CrossSection"));
		panel.add(combo,"growx, wrap unrel");
		

		////  Thickness:
		panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Thickness")));
		
		m = new DoubleModel(component,"Thickness",UnitGroup.UNITS_LENGTH,0);
		
		spin = new JSpinner(m.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin,"growx");
		
		panel.add(new UnitSelector(m),"growx");
		panel.add(new BasicSlider(m.getSliderModel(0,0.01)),"w 100lp, wrap 30lp");
		

		//// Material
		materialPanel(panel, Material.Type.BULK);
		
		
		
		mainPanel.add(panel, "aligny 20%");
		
		return mainPanel;
	}
	
	
	
	private JPanel shapePane() {
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		
		// Create the figure
		figure = new FinPointFigure(finset);
		ScaleScrollPane figurePane = new FinPointScrollPane();
		figurePane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		figurePane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		// Create the table
		tableModel = new FinPointTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i=0; i < Columns.values().length; i++) {
			table.getColumnModel().getColumn(i).
				setPreferredWidth(Columns.values()[i].getWidth());
		}
		JScrollPane tablePane = new JScrollPane(table);
		
		
//		panel.add(new JLabel("Coordinates:"), "aligny bottom, alignx 50%");
//		panel.add(new JLabel("    View:"), "wrap, aligny bottom");
		
		
		panel.add(tablePane,"growy, width 100lp:100lp:, height 100lp:250lp:");
		panel.add(figurePane,"gap unrel, spanx, growx, growy 1000, height 100lp:250lp:, wrap");
		
		panel.add(new StyledLabel("Double-click", -2), "alignx 50%");
		
		panel.add(new ScaleSelector(figurePane),"spany 2");
		panel.add(new StyledLabel("Click+drag: Add and move points   " +
				"Ctrl+click: Remove point", -2), "spany 2, right, wrap");
		
		
		panel.add(new StyledLabel("to edit", -2), "alignx 50%");
		
		return panel;
	}
	
	
	
	
	
	@Override
	public void updateFields() {
		super.updateFields();
		
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		}
		if (figure != null) {
			figure.updateFigure();
		}
	}
	
	
	
	
	private class FinPointScrollPane extends ScaleScrollPane {
		private static final int ANY_MASK = 
			(MouseEvent.ALT_DOWN_MASK | MouseEvent.ALT_GRAPH_DOWN_MASK | 
					MouseEvent.META_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | 
					MouseEvent.SHIFT_DOWN_MASK);
		
		private int dragIndex = -1;
		
		public FinPointScrollPane() {
			super(figure, false);   // Disallow fitting as it's buggy
		}

		@Override
		public void mousePressed(MouseEvent event) {
			int mods = event.getModifiersEx();
			
			if (event.getButton() != MouseEvent.BUTTON1 ||
					(mods & ANY_MASK) != 0) {
				super.mousePressed(event);
				return;
			}
			
			int index = getPoint(event);
			if (index >= 0) {
				dragIndex = index;
				return;
			}
			index = getSegment(event);
			if (index >= 0) {
				Point2D.Double point = getCoordinates(event);
				finset.addPoint(index);
				try {
					finset.setPoint(index, point.x, point.y);
				} catch (IllegalFinPointException ignore) { }
				dragIndex = index;
				
				return;
			}
			
			super.mousePressed(event);
			return;
		}

		
		@Override
		public void mouseDragged(MouseEvent event) {
			int mods = event.getModifiersEx();
			if (dragIndex < 0 ||
					(mods & (ANY_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != 
						MouseEvent.BUTTON1_DOWN_MASK) {
				super.mouseDragged(event);
				return;
			}
			Point2D.Double point = getCoordinates(event);
			
			try {
				finset.setPoint(dragIndex, point.x, point.y);
			} catch (IllegalFinPointException ignore) {
				System.out.println("IAE:"+ignore);
			}
		}
		
		
		@Override
		public void mouseReleased(MouseEvent event) {
			dragIndex = -1;
			super.mouseReleased(event);
		}
		
		@Override
		public void mouseClicked(MouseEvent event) {
			int mods = event.getModifiersEx();
			if (event.getButton() != MouseEvent.BUTTON1 ||
					(mods & ANY_MASK) != MouseEvent.CTRL_DOWN_MASK) {
				super.mouseClicked(event);
				return;
			}
			
			int index = getPoint(event);
			if (index < 0) {
				super.mouseClicked(event);
				return;
			}

			try {
				finset.removePoint(index);
			} catch (IllegalFinPointException ignore) {
			}
		}
		
		
		private int getPoint(MouseEvent event) {
			Point p0 = event.getPoint();
			Point p1 = this.getViewport().getViewPosition();
			int x = p0.x + p1.x;
			int y = p0.y + p1.y;
			
			return figure.getIndexByPoint(x, y);
		}
		
		private int getSegment(MouseEvent event) {
			Point p0 = event.getPoint();
			Point p1 = this.getViewport().getViewPosition();
			int x = p0.x + p1.x;
			int y = p0.y + p1.y;
			
			return figure.getSegmentByPoint(x, y);
		}
		
		private Point2D.Double getCoordinates(MouseEvent event) {
			Point p0 = event.getPoint();
			Point p1 = this.getViewport().getViewPosition();
			int x = p0.x + p1.x;
			int y = p0.y + p1.y;
			
			return figure.convertPoint(x, y);
		}
		
		
	}
	
	

	
	
	private enum Columns {
//		NUMBER {
//			@Override
//			public String toString() {
//				return "#";
//			}
//			@Override
//			public String getValue(FreeformFinSet finset, int row) {
//				return "" + (row+1) + ".";
//			}
//			@Override
//			public int getWidth() {
//				return 10;
//			}
//		}, 
		X {
			@Override
			public String toString() {
				return "X / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toString();
			}
			@Override
			public String getValue(FreeformFinSet finset, int row) {
				return UnitGroup.UNITS_LENGTH.getDefaultUnit()
					.toString(finset.getFinPoints()[row].x);
			}
		}, 
		Y {
			@Override
			public String toString() {
				return "Y / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toString();
			}
			@Override
			public String getValue(FreeformFinSet finset, int row) {
				return UnitGroup.UNITS_LENGTH.getDefaultUnit()
					.toString(finset.getFinPoints()[row].y);
			}
		};
		
		public abstract String getValue(FreeformFinSet finset, int row);
		@Override
		public abstract String toString();
		public int getWidth() {
			return 20;
		}
	}
	
	private class FinPointTableModel extends AbstractTableModel {
		
		@Override
		public int getColumnCount() {
			return Columns.values().length;
		}

		@Override
		public int getRowCount() {
			return finset.getPointCount();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return Columns.values()[columnIndex].getValue(finset, rowIndex);
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return Columns.values()[columnIndex].toString();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (rowIndex == 0 || rowIndex == getRowCount()-1) {
				return (columnIndex == Columns.X.ordinal());
			}
			
			return (columnIndex == Columns.X.ordinal() || columnIndex == Columns.Y.ordinal());
		}
		
		@Override
		public void setValueAt(Object o, int rowIndex, int columnIndex) {
			if (!(o instanceof String))
				return;
			
			if (rowIndex < 0 || rowIndex >= finset.getFinPoints().length ||
					columnIndex < 0 || columnIndex >= Columns.values().length) {
				throw new IllegalArgumentException("Index out of bounds, row="+rowIndex+
						" column="+columnIndex+" fin point count="+finset.getFinPoints().length);
			}
			
			String str = (String)o;
			try {
				
				double value = UnitGroup.UNITS_LENGTH.fromString(str);
				Coordinate c = finset.getFinPoints()[rowIndex];
				if (columnIndex == Columns.X.ordinal())
					c = c.setX(value);
				else
					c = c.setY(value);
				
				finset.setPoint(rowIndex, c.x, c.y);
			
			} catch (NumberFormatException ignore) {
			} catch (IllegalFinPointException ignore) {
			}
		}
	}
}
