package net.sf.openrocket.gui.configdialog;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.dialogs.ScaleDialog;
import net.sf.openrocket.gui.scalefigure.FinPointFigure;
import net.sf.openrocket.gui.scalefigure.ScaleScrollPane;
import net.sf.openrocket.gui.scalefigure.ScaleSelector;
import net.sf.openrocket.gui.util.CustomFinImporter;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.IllegalFinPointException;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.Coordinate;
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class FreeformFinSetConfig extends FinSetConfig {

	private static final Logger log = LoggerFactory.getLogger(FreeformFinSetConfig.class);
	private static final Translator trans = Application.getTranslator();
	
	private JTable table = null;
	private FinPointTableModel tableModel = null;
	
	private int dragIndex = -1;
	private Point dragPoint = null;

	private FinPointFigure figure = null;
	private ScaleSelector selector;
	
	public FreeformFinSetConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.General"), null, generalPane(), trans.get("FreeformFinSetCfg.tab.ttip.General"), 0);
		//// Shape and Fin shape
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.Shape"), null, shapePane(), trans.get("FreeformFinSetCfg.tab.ttip.Finshape"), 1);
		tabbedPane.setSelectedIndex(0);
		
		addFinSetButtons();
	}
	
	
	
	private JPanel generalPane() {
		

		JPanel mainPanel = new JPanel(new MigLayout("fill"));
		
		JPanel panel = new JPanel(new MigLayout("fill, gap rel unrel", "[][65lp::][30lp::]", ""));

		{ ////  Number of fins:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Numberoffins")));

			final IntegerModel finCountModel = new IntegerModel(component, "FinCount", 1, 8);

			JSpinner spin = new JSpinner(finCountModel.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx, wrap");
		}

		{ ////  Base rotation
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Finrotation")));

			DoubleModel m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);

			JSpinner spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");

			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
		}


		{ ////  Fin cant
			JLabel label = new JLabel(trans.get("FreeformFinSetCfg.lbl.Fincant"));
			//// The angle that the fins are canted with respect to the rocket body.
			label.setToolTipText(trans.get("FreeformFinSetCfg.lbl.ttip.Fincant"));
			panel.add(label);

			final DoubleModel cantAngleModel = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE, -FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS);

			final JSpinner cantAngleSpinner = new JSpinner(cantAngleModel.getSpinnerModel());
			cantAngleSpinner.setEditor(new SpinnerEditor(cantAngleSpinner));
			panel.add(cantAngleSpinner, "growx");

			panel.add(new UnitSelector(cantAngleModel), "growx");
			panel.add(new BasicSlider(cantAngleModel.getSliderModel(-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS)), "w 100lp, wrap 40lp");
		}


		{ ////  Position
			//// Position relative to:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Posrelativeto")));


			final EnumModel<AxialMethod> axialMethodModel = new EnumModel<AxialMethod>(component, "AxialMethod", AxialMethod.axialOffsetMethods);
			final JComboBox<AxialMethod> axialMethodCombo = new JComboBox<AxialMethod>(axialMethodModel);
			panel.add(axialMethodCombo, "spanx 3, growx, wrap");

			//// plus
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.plus")), "right");

			final DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
			final JSpinner axialOffsetSpinner = new JSpinner(axialOffsetModel.getSpinnerModel());
			axialOffsetSpinner.setEditor(new SpinnerEditor(axialOffsetSpinner));
			panel.add(axialOffsetSpinner, "growx");

			panel.add(new UnitSelector(axialOffsetModel), "growx");
			panel.add(new BasicSlider(axialOffsetModel.getSliderModel(new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE), new DoubleModel(component.getParent(), "Length"))), "w 100lp, wrap");

			axialMethodCombo.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					axialOffsetModel.stateChanged(new EventObject(e));
				}
			});
		}

		mainPanel.add(panel, "aligny 20%");
		mainPanel.add(new JSeparator(SwingConstants.VERTICAL), "growy, height 150lp");
		
		
		panel = new JPanel(new MigLayout("gap rel unrel", "[][65lp::][30lp::]", ""));


		{ ////  Cross section
			//// Fin cross section:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.FincrossSection")), "span, split");
			JComboBox<FinSet.CrossSection> sectionCombo = new JComboBox<>(new EnumModel<FinSet.CrossSection>(component, "CrossSection"));
			panel.add(sectionCombo, "growx, wrap unrel");


			////  Thickness:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Thickness")));

			final DoubleModel m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);

			final JSpinner spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");

			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");
		}

		{ //// Material
			panel.add(materialPanel(Material.Type.BULK), "span, wrap");
			panel.add(filletMaterialPanel(), "span, wrap");
		}
		
		mainPanel.add(panel, "aligny 20%");
		
		return mainPanel;
	}
	
	
	// edit fin points directly here
	private JPanel shapePane() {
		JPanel panel = new JPanel(null);

		final FreeformFinSet finset = (FreeformFinSet)component; 
		
		// Create the figure
		figure = new FinPointFigure(finset);
		ScaleScrollPane figurePane = new FinPointScrollPane( figure);
		
		// Create the table
		tableModel = new FinPointTableModel();
		table = new JTable(tableModel);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i = 0; i < Columns.values().length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(Columns.values()[i].getWidth());
		}
		table.addMouseListener(new MouseAdapter() {
		    @Override
            public void mouseClicked(MouseEvent ev) {
				figure.setSelectedIndex(table.getSelectedRow());
				figure.updateFigure();
			}

		});
		JScrollPane tablePane = new JScrollPane(table);
		
		JButton scaleButton = new SelectColorButton(trans.get("FreeformFinSetConfig.lbl.scaleFin"));
		scaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Scaling free-form fin");
				ScaleDialog dialog = new ScaleDialog(document, new ArrayList<>(List.of(finset)), SwingUtilities.getWindowAncestor(FreeformFinSetConfig.this), true);
				dialog.setVisible(true);
				dialog.dispose();
			}
		});
		
		//		panel.add(new JLabel("Coordinates:"), "aligny bottom, alignx 50%");
		//		panel.add(new JLabel("    View:"), "wrap, aligny bottom");
		
		JButton exportCsvButton = new SelectColorButton(trans.get("FreeformFinSetConfig.lbl.exportCSV"));
		exportCsvButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Export CSV free-form fin");
				
				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(FileHelper.CSV_FILTER);
				chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

                if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(FreeformFinSetConfig.this)){
                	File selectedFile= chooser.getSelectedFile();
					selectedFile = FileHelper.forceExtension(selectedFile, "csv");
					if (!FileHelper.confirmWrite(selectedFile, panel)) {
						return;
					}

				    FreeformFinSetConfig.writeCSVFile(table, selectedFile);
				}
			}
		});
        JButton importButton = new SelectColorButton(trans.get("CustomFinImport.button.label"));
            importButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    importImage();
                }
            });
        selector = new ScaleSelector(figurePane);
        // fit on first start-up
        figurePane.setFitting(true);
        
        panel.setLayout(new MigLayout("fill, gap 5!","", "[nogrid, fill, sizegroup display, growprio 200]5![sizegroup text, growprio 5]5![sizegroup buttons, align top, growprio 5]0!"));
        
        // first row: main display
        panel.add(tablePane, "width 100lp:100lp:, growy");        
        panel.add(figurePane, "width 200lp:400lp:, gap unrel, grow, height 100lp:250lp:, wrap");
        
		// row of text directly below figure
		panel.add(new StyledLabel(trans.get("lbl.doubleClick1")+" "+trans.get("FreeformFinSetConfig.lbl.doubleClick2"), -2), "spanx 3");
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.clickDrag"), -2), "spanx 3");
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.ctrlClick"), -2), "spanx 3, wrap");
        
        // row of controls at the bottom of the tab:
        panel.add(selector, "aligny bottom, gap unrel");
        panel.add(scaleButton, "");
        panel.add(importButton, "");
        panel.add(exportCsvButton, "");
		
		//		panel.add(new CustomFinBmpImporter(finset), "spany 2, bottom");
		
		return panel;
	}

	private static void writeCSVFile(JTable table, final File outputFile){
        int nRow = table.getRowCount();
		int nCol = table.getColumnCount();

		try {
			final Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile), "utf-8"));

			//write the header information
			StringBuilder bufferHeader = new StringBuilder();
			for (int j = 0; j < nCol; j++) {
				bufferHeader.append(table.getColumnName(j));
				bufferHeader.append(", ");
			}
			writer.write(bufferHeader.toString() + "\r\n");

			//write row information
			for (int i = 0; i < nRow; i++) {
				StringBuilder buffer = new StringBuilder();
				for (int j = 0; j < nCol; j++) {
					buffer.append(table.getValueAt(i, j));
					buffer.append(", ");
				}
				writer.write(buffer.toString() + "\r\n");
			}
			writer.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}


	private void importImage() {
		final FreeformFinSet finset = (FreeformFinSet)component;
		
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(FileHelper.getImageFileFilter());
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
		
		JPanel desc = new JPanel(new MigLayout("fill, ins 0 para 0 para"));
		desc.add(new DescriptionArea(trans.get("CustomFinImport.description"), 5, 0), "grow, wmin 150lp");
		chooser.setAccessory(desc);
		
		int option = chooser.showOpenDialog(this);
		
		if (option == JFileChooser.APPROVE_OPTION) {
			try {
				CustomFinImporter importer = new CustomFinImporter();
				ArrayList<Coordinate> points = importer.getPoints(chooser.getSelectedFile());
				document.startUndo(trans.get("CustomFinImport.undo"));
				finset.setPoints( points);
			} catch (IOException e) {
				log.warn("Error loading file", e);
				JOptionPane.showMessageDialog(this, e.getLocalizedMessage(),
						trans.get("CustomFinImport.error.title"), JOptionPane.ERROR_MESSAGE);
			} finally {
				document.stopUndo();
			}
		}	
	}
	
	
	@Override
	public void updateFields() {
		super.updateFields();
		
		if (tableModel != null) {
			tableModel.fireTableDataChanged();
		
			// make sure to do this *after* the table data is updated.
		    if( 0 <= this.dragIndex ) {
		        table.setRowSelectionInterval(dragIndex, dragIndex);
		    }else {
		        table.clearSelection();
		    }
		}
		
		if (figure != null) {
		    if( 0 <= this.dragIndex ) {
		        figure.setSelectedIndex(dragIndex);
		    }else{
		        figure.resetSelectedIndex();
		    }
            figure.updateFigure();
		}
		
		revalidate();
		repaint();
	}
	
	private class FinPointScrollPane extends ScaleScrollPane {

		private static final int ANY_MASK = (MouseEvent.ALT_DOWN_MASK | MouseEvent.ALT_GRAPH_DOWN_MASK | MouseEvent.META_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK);


		private FinPointScrollPane(final FinPointFigure _figure) {
			super(_figure);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			int mods = event.getModifiersEx();

			final FreeformFinSet finset = (FreeformFinSet) component;

			final int pressIndex = getPoint(event);
			if (pressIndex >= 0) {
				dragIndex = pressIndex;
				dragPoint = event.getPoint();

				updateFields();
				return;
			}

			final int segmentIndex = getSegment(event);
			if (segmentIndex >= 0) {
				Point2D.Double point = getCoordinates(event);
				finset.addPoint(segmentIndex, point);

				dragIndex = segmentIndex;
				dragPoint = event.getPoint();

				updateFields();
				return;
			}

			super.mousePressed(event);
		}

		@Override
		public void mouseDragged(MouseEvent event) {
			int mods = event.getModifiersEx();
			if (dragIndex < 0 || (mods & (ANY_MASK | MouseEvent.BUTTON1_DOWN_MASK)) != MouseEvent.BUTTON1_DOWN_MASK) {
				super.mouseDragged(event);
				return;
			}

			Point2D.Double point = getCoordinates(event);
			final FreeformFinSet finset = (FreeformFinSet) component;
			finset.setPoint(dragIndex, point.x, point.y);

			dragPoint.x = event.getX();
			dragPoint.y = event.getY();

			updateFields();

			// if point is within borders of figure _AND_ outside borders of the ScrollPane's view:
			final Rectangle dragRectangle = viewport.getViewRect();
			final Point canvasPoint = new Point( dragPoint.x + dragRectangle.x, dragPoint.y + dragRectangle.y);
			if( (figure.getBorderWidth() < canvasPoint.x) && (canvasPoint.x < (figure.getWidth() - figure.getBorderWidth()))
			    && (figure.getBorderHeight() < canvasPoint.y) && (canvasPoint.y < (figure.getHeight() - figure.getBorderHeight())))
			{
				boolean hitBorder = false;
				if(dragPoint.x < figure.getBorderWidth()){
					hitBorder = true;
					dragRectangle.x += dragPoint.x - figure.getBorderWidth();
				} else if(dragPoint.x >(dragRectangle.width -figure.getBorderWidth())) {
					hitBorder = true;
					dragRectangle.x += dragPoint.x - (dragRectangle.width - figure.getBorderWidth());
				}

				if (dragPoint.y<figure.getBorderHeight()) {
					hitBorder = true;
					dragRectangle.y += dragPoint.y - figure.getBorderHeight();
				} else if(dragPoint.y >(dragRectangle.height -figure.getBorderHeight())) {
					hitBorder = true;
					dragRectangle.y += dragPoint.y - (dragRectangle.height - figure.getBorderHeight());
				}

				if (hitBorder) {
					super.setFitting(false);
					selector.update();
					figure.scrollRectToVisible(dragRectangle);
					revalidate();
				}
			}
		}

		@Override
		public void componentResized(ComponentEvent e) {
			if (fit) {
				// if we're fitting the whole figure in the ScrollPane, the parent behavior is fine
				super.componentResized(e);
			} else if (0 > dragIndex) {
				// if we're not _currently_ dragging a point, the parent behavior is fine
				super.componentResized(e);
			} else {
				// currently dragging a point.
				// ... and if we drag out-of-bounds, we want to move the viewport to keep up
				boolean hitBorder = false;
				final Rectangle dragRectangle = viewport.getViewRect();

				if(dragPoint.x<figure.getBorderWidth()){
					hitBorder = true;
					dragRectangle.x += dragPoint.x - figure.getBorderWidth();
				} else if(dragPoint.x >(dragRectangle.width -figure.getBorderWidth())) {
					hitBorder = true;
					dragRectangle.x += dragPoint.x - (dragRectangle.width - figure.getBorderWidth());
				}

				if (dragPoint.y<figure.getBorderHeight()) {
					hitBorder = true;
					dragRectangle.y += dragPoint.y - figure.getBorderHeight();
				} else if(dragPoint.y >(dragRectangle.height -figure.getBorderHeight())) {
					hitBorder = true;
					dragRectangle.y += dragPoint.y - (dragRectangle.height - figure.getBorderHeight());
				}

				if (hitBorder) {
					super.setFitting(false);
					selector.update();
					figure.scrollRectToVisible(dragRectangle);
					revalidate();
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			dragIndex = -1;
			dragPoint = null;
			super.mouseReleased(event);
		}
		
		@Override
		public void mouseClicked(MouseEvent event) {
            int mods = event.getModifiersEx();
            if(( event.getButton() == MouseEvent.BUTTON1) && (0 < (MouseEvent.CTRL_DOWN_MASK & mods))) {
                int clickIndex = getPoint(event);
                if ( 0 < clickIndex) {
                    // if ctrl+click, delete point
                    try {
                        final FreeformFinSet finset = (FreeformFinSet)component;
                        finset.removePoint(clickIndex);
                    } catch (IllegalFinPointException ignore) {
                        log.error("Ignoring IllegalFinPointException while dragging, dragIndex=" + dragIndex + ".  This is likely an internal error.");
                    }
                    return;
                }
            }
            
            super.mouseClicked(event);
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
		X {
			@Override
			public String toString() {
				return "X / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toString();
			}
			
			@Override
			public String getValue(FreeformFinSet finset, int row) {
				return UnitGroup.UNITS_LENGTH.getDefaultUnit().toString(finset.getFinPoints()[row].x);
			}
		},
		Y {
			@Override
			public String toString() {
				return "Y / " + UnitGroup.UNITS_LENGTH.getDefaultUnit().toString();
			}
			
			@Override
			public String getValue(FreeformFinSet finset, int row) {
				return UnitGroup.UNITS_LENGTH.getDefaultUnit().toString(finset.getFinPoints()[row].y);
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
			return ((FreeformFinSet)component).getPointCount(); 
		}
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return Columns.values()[columnIndex].getValue( (FreeformFinSet)component, rowIndex);
		}
		
		@Override
		public String getColumnName(int columnIndex) {
			return Columns.values()[columnIndex].toString();
		}
		
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			if (rowIndex == 0 || rowIndex == getRowCount() - 1) {
				return (columnIndex == Columns.X.ordinal());
			}
			
			return (columnIndex == Columns.X.ordinal() || columnIndex == Columns.Y.ordinal());
		}
		
		@Override
		public void setValueAt(Object o, int rowIndex, int columnIndex) {
			if (!(o instanceof String))
				return;

			final FreeformFinSet finset = (FreeformFinSet)component;

			// bounds check that indices are valid
			if (rowIndex < 0 || rowIndex >= finset.getFinPoints().length || columnIndex < 0 || columnIndex >= Columns.values().length) {
				throw new IllegalArgumentException("Index out of bounds, row=" + rowIndex + " column=" + columnIndex + " fin point count=" + finset.getFinPoints().length);
			}
			
			String str = (String) o;
			try {
				
				double value = UnitGroup.UNITS_LENGTH.fromString(str);
				Coordinate c = finset.getFinPoints()[rowIndex];
				if (columnIndex == Columns.X.ordinal()){
					c = c.setX(value);
				}else{
					c = c.setY(value);
				}
			
				finset.setPoint(rowIndex, c.x, c.y);
				
				updateFields();
			} catch (NumberFormatException ignore) {
			    log.warn("ignoring NumberFormatException while editing a Freeform Fin");
			}
		}
	}
}
