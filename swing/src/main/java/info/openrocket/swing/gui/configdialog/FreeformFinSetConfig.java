package info.openrocket.swing.gui.configdialog;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
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
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import info.openrocket.swing.gui.adaptors.CustomFocusTraversalPolicy;
import info.openrocket.swing.gui.util.Icons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.IllegalFinPointException;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.dialogs.ScaleDialog;
import info.openrocket.swing.gui.scalefigure.FinPointFigure;
import info.openrocket.swing.gui.scalefigure.ScaleScrollPane;
import info.openrocket.swing.gui.scalefigure.ScaleSelector;
import info.openrocket.swing.gui.util.CustomFinImporter;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;

@SuppressWarnings("serial")
public class FreeformFinSetConfig extends FinSetConfig {

	private static final Logger log = LoggerFactory.getLogger(FreeformFinSetConfig.class);
	private static final Translator trans = Application.getTranslator();
	
	private JTable table = null;
	private FinPointTableModel tableModel = null;
	private JPopupMenu pm;
	
	private int dragIndex = -1;
	private Point dragPoint = null;

	private FinPointFigure figure = null;
	private ScaleScrollPane figurePane = null;
	private ScaleSelector selector;

	private FinPointAction insertFinPointAction;
	private FinPointAction deleteFinPointAction;
	
	public FreeformFinSetConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
		super(d, component, parent);
		
		//// General and General properties
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.General"), null, generalPane(), trans.get("FreeformFinSetCfg.tab.ttip.General"), 0);
		//// Shape and Fin shape
		tabbedPane.insertTab(trans.get("FreeformFinSetCfg.tab.Shape"), null, shapePane(), trans.get("FreeformFinSetCfg.tab.ttip.Finshape"), 1);
		tabbedPane.setSelectedIndex(0);
		
		addFinSetButtons();

		// Apply the custom focus travel policy to this panel
		//// Make sure the cancel & ok button is the last component
		order.add(cancelButton);
		order.add(okButton);
		CustomFocusTraversalPolicy policy = new CustomFocusTraversalPolicy(order);
		parent.setFocusTraversalPolicy(policy);
	}
	
	
	
	private JPanel generalPane() {
		JPanel mainPanel = new JPanel(new MigLayout());
		
		JPanel panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));

		{ ////  Number of fins:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Numberoffins")));

			final IntegerModel finCountModel = new IntegerModel(component, "FinCount", 1, 8);
			register(finCountModel);

			JSpinner spin = new JSpinner(finCountModel.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx, wrap");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		}

		{ ////  Fin cant
			JLabel label = new JLabel(trans.get("FreeformFinSetCfg.lbl.Fincant"));
			//// The angle that the fins are canted with respect to the rocket body.
			label.setToolTipText(trans.get("FreeformFinSetCfg.lbl.ttip.Fincant"));
			panel.add(label);

			final DoubleModel cantAngleModel = new DoubleModel(component, "CantAngle", UnitGroup.UNITS_ANGLE, -FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS);
			register(cantAngleModel);

			final JSpinner cantAngleSpinner = new JSpinner(cantAngleModel.getSpinnerModel());
			cantAngleSpinner.setEditor(new SpinnerEditor(cantAngleSpinner));
			panel.add(cantAngleSpinner, "growx");
			order.add(((SpinnerEditor) cantAngleSpinner.getEditor()).getTextField());

			panel.add(new UnitSelector(cantAngleModel), "growx");
			panel.add(new BasicSlider(cantAngleModel.getSliderModel(-FinSet.MAX_CANT_RADIANS, FinSet.MAX_CANT_RADIANS)), "w 100lp, wrap 30lp");
		}

		{ ////  Cross section
			//// Fin cross section:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.FincrossSection")), "span, split");
			EnumModel<FinSet.CrossSection> em = new EnumModel<>(component, "CrossSection");
			register(em);
			JComboBox<FinSet.CrossSection> sectionCombo = new JComboBox<>(em);
			panel.add(sectionCombo, "growx, wrap unrel");
			order.add(sectionCombo);


			////  Thickness:
			panel.add(new JLabel(trans.get("FreeformFinSetCfg.lbl.Thickness")));

			final DoubleModel m = new DoubleModel(component, "Thickness", UnitGroup.UNITS_LENGTH, 0);
			register(m);

			final JSpinner spin = new JSpinner(m.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			panel.add(spin, "growx");
			order.add(((SpinnerEditor) spin.getEditor()).getTextField());

			panel.add(new UnitSelector(m), "growx");
			panel.add(new BasicSlider(m.getSliderModel(0, 0.01)), "w 100lp, wrap 30lp");
		}

		mainPanel.add(panel, "aligny 0, gapright 40lp");

		// Right side panel
		panel = new JPanel(new MigLayout("gap rel unrel, ins 0", "[][65lp::][30lp::]", ""));

		{//// -------- Placement ------
			//// Position relative to:
			PlacementPanel placementPanel = new PlacementPanel(component, order);
			register(placementPanel);
			panel.add(placementPanel, "span, grow");

			{////  Fin rotation
				JLabel label = new JLabel(trans.get("FinSetCfg.lbl.FinRotation"));
				label.setToolTipText(trans.get("FinSetCfg.lbl.FinRotation.ttip"));
				placementPanel.add(label, "newline");

				DoubleModel m = new DoubleModel(component, "BaseRotation", UnitGroup.UNITS_ANGLE);
				register(m);

				JSpinner spin = new JSpinner(m.getSpinnerModel());
				spin.setEditor(new SpinnerEditor(spin));
				placementPanel.add(spin, "growx");
				order.add(((SpinnerEditor) spin.getEditor()).getTextField());

				placementPanel.add(new UnitSelector(m), "growx");
				placementPanel.add(new BasicSlider(m.getSliderModel(-Math.PI, Math.PI)), "w 100lp, wrap");
			}
		}

		{ //// Material
			MaterialPanel materialPanel = new MaterialPanel(component, document, Material.Type.BULK, order);
			register(materialPanel);
			panel.add(materialPanel, "span, grow, wrap");
		}

		{ //// Root fillets
			panel.add(filletMaterialPanel(), "span, grow, wrap");
		}
		
		mainPanel.add(panel, "aligny 0");

		return mainPanel;
	}
	
	
	// edit fin points directly here
	private JPanel shapePane() {
		JPanel panel = new JPanel(null);

		final FreeformFinSet finset = (FreeformFinSet)component; 
		
		// Create the figure
		figure = new FinPointFigure(finset);
		figurePane = new FinPointScrollPane( figure);
		
		// Create the table
		tableModel = new FinPointTableModel();
		table = new JTable(tableModel) {
			@Override
			public void changeSelection(int row, int column, boolean toggle, boolean extend) {
				super.changeSelection(row, column, toggle, extend);

				if (isCellEditable(row, column)) {
					editCellAt(row, column);
					Component editor = getEditorComponent();
					if (editor != null) {
						editor.requestFocus();
					}
				}
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i = 0; i < Columns.values().length; i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(Columns.values()[i].getWidth());
		}

		// Set custom editor for highlighting all text
		DefaultCellEditor editor = new DefaultCellEditor(new JTextField()) {
			@Override
			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
				JTextField textField = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
				SwingUtilities.invokeLater(textField::selectAll);
				return textField;
			}
		};

		// Apply the editor to all columns
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setCellEditor(editor);
		}

		table.addMouseListener(new MouseAdapter() {
		    @Override
            public void mouseClicked(MouseEvent e) {
				int row = table.rowAtPoint(e.getPoint());

				// Context menu on right-click
				if (e.getButton() == MouseEvent.BUTTON3 && e.getClickCount() == 1) {
					// Select new row
					if (!table.isRowSelected(row)) {
						if (row >= 0 && row < table.getRowCount()) {
							table.setRowSelectionInterval(row, row);
						} else {
							return;
						}
					}

					doPopup(e);
				}
			}

		});
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				figure.setSelectedIndex(table.getSelectedRow());
				figure.updateFigure();
			}
		});
		JScrollPane tablePane = new JScrollPane(table);

		// Remove focus from table when interacting on the figure
		figurePane.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (table.isEditing()) {
					table.getCellEditor().stopCellEditing();
				}
				table.clearSelection();
			}
		});
		
		JButton scaleButton = new JButton(trans.get("FreeformFinSetConfig.lbl.scaleFin"));
		scaleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Scaling free-form fin");
				ScaleDialog dialog = new ScaleDialog(document, new ArrayList<>(List.of(finset)), SwingUtilities.getWindowAncestor(FreeformFinSetConfig.this), true);
				dialog.setVisible(true);
				dialog.dispose();
			}
		});

		// Context menu for table
		insertFinPointAction = new InsertPointAction();
		deleteFinPointAction = new DeletePointAction();
		pm = new JPopupMenu();
		pm.add(insertFinPointAction);
		pm.add(deleteFinPointAction);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateActionStates();
			}
		});

		
		//		panel.add(new JLabel("Coordinates:"), "aligny bottom, alignx 50%");
		//		panel.add(new JLabel("    View:"), "wrap, aligny bottom");
		
		JButton exportCsvButton = new JButton(trans.get("FreeformFinSetConfig.lbl.exportCSV"));
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
					((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
				}
			}
		});
        JButton importButton = new JButton(trans.get("CustomFinImport.button.label"));
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
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePane, figurePane);
		splitPane.setResizeWeight(0.15);
		splitPane.setBorder(null);
		panel.add(splitPane, "width 300lp:500lp:, gap unrel, grow, height 100lp:250lp:, wrap");
		order.add(table);

		// row of text directly below figure
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.ctrlClick"), -2), "spanx 2");
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.clickDrag"), -2), "spanx 2, wrap");
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.shiftClickDrag"), -2), "spanx 2");
        panel.add(new StyledLabel(trans.get("FreeformFinSetConfig.lbl.ctrlShiftClickDrag"), -2), "spanx 2, wrap");
        
        // row of controls at the bottom of the tab:
        panel.add(selector.getAsPanel(), "aligny bottom, gap unrel");
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
			writer.write(bufferHeader + "\r\n");

			//write row information
			for (int i = 0; i < nRow; i++) {
				StringBuilder buffer = new StringBuilder();
				for (int j = 0; j < nCol; j++) {
					buffer.append(table.getValueAt(i, j));
					buffer.append(", ");
				}
				writer.write(buffer + "\r\n");
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
		desc.add(new DescriptionArea(trans.get("CustomFinImport.description"), 5, 0), "grow, wmin 100lp");
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
				((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
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

		if (figurePane != null) {
			figurePane.revalidate();
		}
	}

	/**
	 * Insert a new fin point between the currently selected point and the next point.
	 * The coordinates of the new point will be the average of the two points.
	 */
	private void insertPoint() throws IllegalFinPointException {
		int currentPointIdx = table.getSelectedRow();
		if (currentPointIdx == -1 || currentPointIdx >= table.getRowCount() - 1) {
			return;
		}
		final FreeformFinSet finSet = (FreeformFinSet) component;
		Coordinate currentPoint = finSet.getFinPoints()[currentPointIdx];
		Coordinate nextPoint = finSet.getFinPoints()[currentPointIdx + 1];
		Point2D.Double toAdd = new Point2D.Double((currentPoint.x + nextPoint.x) / 2, (currentPoint.y + nextPoint.y) / 2);
		finSet.addPoint(currentPointIdx + 1, toAdd);
	}

	/**
	 * Delete the currently selected fin point.
	 */
	private void deletePoint() {
		int currentPointIdx = table.getSelectedRow();
		if (currentPointIdx == -1) {
			return;
		}
		final FreeformFinSet finSet = (FreeformFinSet) component;
		try {
			finSet.removePoint(currentPointIdx);
		} catch (IllegalFinPointException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void doPopup(MouseEvent e) {
		pm.show(e.getComponent(), e.getX(), e.getY());
	}

	private void updateActionStates() {
		if (insertFinPointAction == null) {		// If one of the actions is null, the rest will be too
			return;
		}

		insertFinPointAction.updateEnabledState();
		deleteFinPointAction.updateEnabledState();
	}
	
	private class FinPointScrollPane extends ScaleScrollPane {

		private static final int ANY_MASK = (MouseEvent.ALT_DOWN_MASK | MouseEvent.ALT_GRAPH_DOWN_MASK | MouseEvent.META_DOWN_MASK | MouseEvent.CTRL_DOWN_MASK | MouseEvent.SHIFT_DOWN_MASK);


		private FinPointScrollPane(final FinPointFigure _figure) {
			super(_figure);
		}

		@Override
		public void mousePressed(MouseEvent event) {
			requestFocusInWindow();
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
				try {
					finset.addPoint(segmentIndex, point);
				} catch (IllegalFinPointException e) {
					throw new RuntimeException(e);
				}

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

			if (dragIndex < 0) {
				super.mouseDragged(event);
				return;
			}

			Point2D.Double point = getCoordinates(event);
			final FreeformFinSet finset = (FreeformFinSet) component;

			// If shift is held down, apply snapping
			if ((mods & MouseEvent.SHIFT_DOWN_MASK) != 0) {
				int lockIndex = getLockIndex(mods);

				if (lockIndex != -1) {
					point = snapPoint(point, finset.getFinPoints()[lockIndex]);
					int highlightIndex = getHighlightIndex(lockIndex);
					figure.setHighlightIndex(highlightIndex);
				}
			}

			try {
				finset.setPoint(dragIndex, point.x, point.y);
			} catch (IllegalFinPointException e) {
				throw new RuntimeException(e);
			}

			dragPoint.x = event.getX();
			dragPoint.y = event.getY();

			updateFields();

			// Handle scrolling if point is dragged out of view
			handleScrolling();
		}

		/**
		 * Get the index of the point that the current point should lock to.
		 * @param mods The modifiers of the mouse event
		 * @return The index of the point to lock to, or -1 if no point should be locked to
		 */
		private int getLockIndex(int mods) {
			int length = ((FreeformFinSet) component).getFinPoints().length;
			if ((mods & MouseEvent.CTRL_DOWN_MASK) != 0) {
				return (dragIndex > 0 && dragIndex < length - 1) ? dragIndex + 1 : -1;
			} else {
				return (dragIndex < length - 1 && dragIndex > 0) ? dragIndex - 1 : -1;
			}
		}

		private int getHighlightIndex(int lockIndex) {
			return (lockIndex == dragIndex + 1) ? dragIndex : lockIndex;
		}

		private Point2D.Double snapPoint(Point2D.Double point, Coordinate lockPoint) {
			Point2D.Double snappedPoint = new Point2D.Double(point.x, point.y);

			double diffX = point.x - lockPoint.x;
			double diffY = point.y - lockPoint.y;
			double distanceX = Math.abs(diffX);
			double distanceY = Math.abs(diffY);

			// Calculate distance to 45 or 135 degree line
			double a = 1;
			double b = (Math.signum(diffX) == Math.signum(diffY)) ? -1 : 1;
			double c = -(a * lockPoint.x + b * lockPoint.y);
			double distanceDiag = Math.abs(a * point.x + b * point.y + c) / Math.sqrt(2);

			// Snap to the closest constraint
			if (distanceX <= distanceY && distanceX <= distanceDiag) {
				// Snap horizontal
				snappedPoint.x = lockPoint.x;
			} else if (distanceY <= distanceX && distanceY <= distanceDiag) {
				// Snap vertical
				snappedPoint.y = lockPoint.y;
			} else {
				// Snap diagonal (45 degrees)
				double avgDist = (Math.abs(diffX) + Math.abs(diffY)) / 2;
				snappedPoint.x = lockPoint.x + Math.signum(diffX) * avgDist;
				snappedPoint.y = lockPoint.y + Math.signum(diffY) * avgDist;
			}

			return snappedPoint;
		}

		private void handleScrolling() {
			Rectangle dragRectangle = viewport.getViewRect();
			Point canvasPoint = new Point(dragPoint.x + dragRectangle.x, dragPoint.y + dragRectangle.y);

			if (isPointWithinFigureBounds(canvasPoint)) {
				boolean hitBorder = updateScrollPosition(dragRectangle);
				if (hitBorder) {
					super.setFitting(false);
					selector.update();
					figure.scrollRectToVisible(dragRectangle);
					revalidate();
				}
			}
		}

		private boolean isPointWithinFigureBounds(Point point) {
			return figure.getBorderWidth() < point.x && point.x < (figure.getWidth() - figure.getBorderWidth())
					&& figure.getBorderHeight() < point.y && point.y < (figure.getHeight() - figure.getBorderHeight());
		}

		private boolean updateScrollPosition(Rectangle dragRectangle) {
			boolean hitBorder = false;

			if (dragPoint.x < figure.getBorderWidth()) {
				hitBorder = true;
				dragRectangle.x += dragPoint.x - figure.getBorderWidth();
			} else if (dragPoint.x > (dragRectangle.width - figure.getBorderWidth())) {
				hitBorder = true;
				dragRectangle.x += dragPoint.x - (dragRectangle.width - figure.getBorderWidth());
			}

			if (dragPoint.y < figure.getBorderHeight()) {
				hitBorder = true;
				dragRectangle.y += dragPoint.y - figure.getBorderHeight();
			} else if (dragPoint.y > (dragRectangle.height - figure.getBorderHeight())) {
				hitBorder = true;
				dragRectangle.y += dragPoint.y - (dragRectangle.height - figure.getBorderHeight());
			}

			return hitBorder;
		}

		@Override
		public void mouseReleased(MouseEvent event) {
			dragIndex = -1;
			dragPoint = null;
			figure.setHighlightIndex(-1);
			figure.updateFigure();

			super.mouseReleased(event);
		}
		
		@Override
		public void mouseClicked(MouseEvent event) {
			int mods = event.getModifiersEx();
			if ((event.getButton() == MouseEvent.BUTTON1) && (0 < (MouseEvent.CTRL_DOWN_MASK & mods))) {
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
			} catch (IllegalFinPointException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private abstract static class FinPointAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public abstract void updateEnabledState();
	}

	private class InsertPointAction extends FinPointAction {
		public InsertPointAction() {
			putValue(NAME, trans.get("FreeformFinSetConfig.lbl.insertPoint"));
			this.putValue(SMALL_ICON, Icons.FILE_NEW);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				insertPoint();
			} catch (IllegalFinPointException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public void updateEnabledState() {
			// You can't add to the last fin point
			setEnabled(table.getSelectedRow() < table.getRowCount() - 1);
		}
	}

	private class DeletePointAction extends FinPointAction {
		public DeletePointAction() {
			putValue(NAME, trans.get("FreeformFinSetConfig.lbl.deletePoint"));
			this.putValue(SMALL_ICON, Icons.EDIT_DELETE);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			deletePoint();
		}

		@Override
		public void updateEnabledState() {
			// You can't delete the first or last fin point
			setEnabled(table.getSelectedRow() > 0 && table.getSelectedRow() < table.getRowCount() - 1);
		}
	}
}
