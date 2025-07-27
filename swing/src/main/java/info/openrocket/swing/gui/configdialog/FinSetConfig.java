package info.openrocket.swing.gui.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import info.openrocket.core.material.MaterialGroup;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.swing.gui.components.SVGOptionPanel;
import info.openrocket.swing.gui.dialogs.preferences.PreferencesDialog;
import info.openrocket.swing.gui.main.BasicFrame;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.GroupableAndSearchableComboBox;
import info.openrocket.swing.gui.widgets.MaterialComboBox;
import net.miginfocom.swing.MigLayout;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.svg.export.SVGBuilder;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.material.Material;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.MathUtil;

import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.MaterialModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.components.UnitSelector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SuppressWarnings("serial")
public abstract class FinSetConfig extends RocketComponentConfig {
	private static final Logger log = LoggerFactory.getLogger(FinSetConfig.class);
	private static final Translator trans = Application.getTranslator();
	private static final ApplicationPreferences prefs = Application.getPreferences();

	private JButton split = null;
	
	public FinSetConfig(OpenRocketDocument d, RocketComponent component, JDialog parent) {
		super(d, component, parent);
		
		//// Fin tabs and Through-the-wall fin tabs
		tabbedPane.insertTab(trans.get("FinSetConfig.tab.Fintabs"), null, finTabPanel(),
				trans.get("FinSetConfig.tab.Through-the-wall"), 0);
	}
	
	
	protected void addFinSetButtons() {
		JButton convert = null;
		
		//// Convert buttons
		if (!(component instanceof FreeformFinSet)) {
			//// Convert to freeform
			convert = new JButton(trans.get("FinSetConfig.but.Converttofreeform"));
			//// Convert this fin set into a freeform fin set
			convert.setToolTipText(trans.get("FinSetConfig.but.Converttofreeform.ttip"));
			convert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					log.info(Markers.USER_MARKER, "Converting " + component.getComponentName() + " into freeform fin set");
					
					// Do change in future for overall safety
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							//// Convert fin set
							document.addUndoPosition(trans.get("FinSetConfig.Convertfinset"));

							RocketComponent freeform =
									FreeformFinSet.convertFinSet((FinSet) component);

							ComponentConfigDialog.showDialog(freeform);
						}
					});
					
					ComponentConfigDialog.disposeDialog();
				}
			});
		}
		
		//// Split fins
		split = new JButton(trans.get("FinSetConfig.but.Splitfins"));
		//// Split the fin set into separate fins
		split.setToolTipText(trans.get("FinSetConfig.but.Splitfins.ttip"));
		split.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Splitting " + component.getComponentName() + " into separate fins, fin count=" +
						((FinSet) component).getFinCount());

				// This is a bit awkward, we need to store the listeners before closing the dialog, because closing it
				// will remove them. We then add them back before the split and remove them afterwards.
				List<RocketComponent> listeners = new ArrayList<>(component.getConfigListeners());

				ComponentConfigDialog.disposeDialog();

				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						document.addUndoPosition("Split fin set");
						for (RocketComponent listener : listeners) {
							component.addConfigListener(listener);
						}
						((FinSet) component).splitFins();
						component.clearConfigListeners();
					}
				});
			}
		});
		split.setEnabled(((FinSet) component).getFinCount() > 1);

		//// Export to SVG
		JButton exportSVGBtn = new JButton(trans.get("FinSetConfig.lbl.exportSVG"));
		exportSVGBtn.setToolTipText(trans.get("FinSetConfig.lbl.exportSVG.ttip"));
		exportSVGBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Export CSV free-form fin");

				JFileChooser chooser = new JFileChooser();
				chooser.setFileFilter(FileHelper.SVG_FILTER);
				chooser.setAccessory(new SVGOptionPanel());
				chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

				if (JFileChooser.APPROVE_OPTION == chooser.showSaveDialog(FinSetConfig.this)){
					File selectedFile= chooser.getSelectedFile();
					selectedFile = FileHelper.forceExtension(selectedFile, "svg");
					if (!FileHelper.confirmWrite(selectedFile, buttonPanel)) {
						return;
					}

					((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
					SVGOptionPanel svgOptions = (SVGOptionPanel) chooser.getAccessory();
					prefs.setSVGStrokeColor(svgOptions.getStrokeColor());
					prefs.setSVGStrokeWidth(svgOptions.getStrokeWidth());

					try {
						FinSetConfig.writeSVGFile((FinSet) component, selectedFile, svgOptions);
					} catch (Exception svgErr) {
						JOptionPane.showMessageDialog(FinSetConfig.this,
								String.format(trans.get("FinSetConfig.errorSVG.msg"), svgErr.getMessage()),
								trans.get("FinSetConfig.errorSVG.title"), JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});

		if (convert == null) {
			addButtons(split, exportSVGBtn);
			order.add(split);
			order.add(exportSVGBtn);
		}
		else {
			addButtons(split, convert, exportSVGBtn);
			order.add(split);
			order.add(convert);
			order.add(exportSVGBtn);
		}
	}
	
	private JPanel finTabPanel() {
		JPanel panel = new JPanel(
				new MigLayout("gap rel unrel, ins 25lp",
						"[100lp::][65lp::][30lp::][200lp::]", ""));
		//		JPanel panel = new JPanel(new MigLayout("fillx, align 20% 20%, gap rel unrel",
		//				"[40lp][80lp::][30lp::][100lp::]",""));
		
		//// Through-the-wall fin tabs:
		panel.add(new StyledLabel(trans.get("FinSetConfig.lbl.Through-the-wall"), Style.BOLD),
				"spanx, wrap 30lp");
		
		JLabel label;
		DoubleModel length;
		DoubleModel length2;
		DoubleModel maxTabHeight;
		DoubleModel length_2;
		JSpinner spin;
		JButton autoCalc;
		
		length = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		maxTabHeight = new DoubleModel(component, "MaxTabHeight", 1, UnitGroup.UNITS_LENGTH, 0);
		length2 = new DoubleModel(component, "Length", 0.5, UnitGroup.UNITS_LENGTH, 0);
		length_2 = new DoubleModel(component, "Length", -0.5, UnitGroup.UNITS_LENGTH, 0);
		
		register(length);
		register(length2);
		register(maxTabHeight);
		register(length_2);
		
		////  Tab length
		//// Tab length:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tablength"));
		//// The length of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tablength"));
		panel.add(label);
		
		final DoubleModel tabLength = new DoubleModel(component, "TabLength", UnitGroup.UNITS_LENGTH, 0);
		register(tabLength);

		spin = new JSpinner(tabLength.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx 1");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(tabLength), "growx 1");
		panel.add(new BasicSlider(tabLength.getSliderModel(DoubleModel.ZERO, length)),
				"w 100lp, growx 5, wrap");
		

		//// Tab height:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabheight"));
		//// The span-wise height of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabheight"));
		panel.add(label);
		
		final DoubleModel tabHeightModel = new DoubleModel(component, "TabHeight", UnitGroup.UNITS_LENGTH, 0, ((FinSet)component).getMaxTabHeight());
		register(tabHeightModel);
		component.addChangeListener( tabHeightModel );
		spin = new JSpinner(tabHeightModel.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(tabHeightModel), "growx");
		panel.add(new BasicSlider(tabHeightModel.getSliderModel(DoubleModel.ZERO, maxTabHeight)),
				"w 100lp, growx 5, wrap");
		
		////  Tab position:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabposition"));
		//// The position of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabposition"));
		panel.add(label);
		
		final DoubleModel tabOffset = new DoubleModel(component, "TabOffset", UnitGroup.UNITS_LENGTH);
		register(tabOffset);
		component.addChangeListener( tabOffset);
		spin = new JSpinner(tabOffset.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());
		
		panel.add(new UnitSelector(tabOffset), "growx");
		panel.add(new BasicSlider(tabOffset.getSliderModel(length_2, length2)), "w 100lp, growx 5, wrap");
		
		//// relative to
		label = new JLabel(trans.get("FinSetConfig.lbl.relativeto"));
		panel.add(label, "right, gapright unrel");
		

		final EnumModel<AxialMethod> tabOffsetMethod = new EnumModel<>(component, "TabOffsetMethod");
		register(tabOffsetMethod);

		JComboBox<AxialMethod> enumCombo = new JComboBox<>(tabOffsetMethod);
		
		panel.add( enumCombo, "spanx 3, growx, wrap para");
		order.add(enumCombo);

		// Calculate fin tab height, length, and position
		autoCalc = new JButton(trans.get("FinSetConfig.but.AutoCalc"));
		autoCalc.setToolTipText(trans.get("FinSetConfig.but.AutoCalc.ttip"));
		
		autoCalc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				calculateAutoTab(tabOffsetMethod, tabOffset, tabLength, tabHeightModel);
			}
		});
		panel.add(autoCalc, "skip 1, spanx");
    	order.add(autoCalc);
		
		return panel;
	}

	private void calculateAutoTab(EnumModel<AxialMethod> tabOffsetMethod, DoubleModel tabOffset, DoubleModel tabLength,
								  DoubleModel tabHeightModel) {
		log.info(Markers.USER_MARKER, "Computing " + component.getComponentName() + " tab height.");

		double maxTubeRad = 0.0;
		double maxRingRad = 0.0;
		RocketComponent parent = component.getParent();
		if (parent instanceof SymmetricComponent){
			try {
				document.startUndo("Compute fin tabs");

				List<CenteringRing> rings = new ArrayList<>();
				// Do deep recursive iteration to find centering rings and determine radius of inner tube
				for (RocketComponent child : parent.getChildren()) {
					if (child instanceof InnerTube) {
						if (!isComponentInsideFinSpan(child)) {
							continue;
						}
						InnerTube tube = (InnerTube) child;
						if (tube.getOuterRadius() > maxTubeRad) {
							maxTubeRad = tube.getOuterRadius();
						}
					} else if (child instanceof CenteringRing) {
						CenteringRing ring = (CenteringRing) child;
						if (ring.getOuterRadius() > maxRingRad) {
							maxRingRad = ring.getOuterRadius();
						}
						rings.add(ring);
					}
				}

				// Remove rings that are smaller than the maximum inner tube radius
				for (CenteringRing ring : new ArrayList<>(rings)) {
					if (ring.getOuterRadius() <= maxTubeRad) {
						rings.remove(ring);
					}
				}

				//Figure out position and length of the fin tab
				if (maxRingRad > maxTubeRad && !rings.isEmpty()) {
					AxialMethod temp = (AxialMethod) tabOffsetMethod.getSelectedItem();
					tabOffsetMethod.setSelectedItem(AxialMethod.TOP);
					double len = computeFinTabLength(rings, component.getAxialOffset(AxialMethod.TOP),
								component.getLength(), tabOffset, parent);
					tabLength.setValue(len);
					//Be nice to the user and set the tab relative position enum back the way they had it.
					tabOffsetMethod.setSelectedItem(temp);
				} else {
					tabOffsetMethod.setSelectedItem(AxialMethod.TOP);
					tabOffset.setValue(0);
					tabLength.setValue(component.getLength());
				}

				// Compute tab height
				final Coordinate finFront = ((FinSet) component).getFinFront();
				double finStart = finFront.x + ((FinSet) component).getTabFrontEdge();
				double finEnd = finFront.x + ((FinSet) component).getTabTrailingEdge();
				double parentMinRadius = MathUtil.min(((SymmetricComponent)parent).getRadius(finStart),
						((SymmetricComponent)parent).getRadius(finEnd));
				double height = parentMinRadius - maxTubeRad;

				// Set tab height
				if (height >= 0) {
					tabHeightModel.setValue(height);
					tabHeightModel.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
				}
			} finally {
				document.stopUndo();
			}
		}
	}

	/**
	 * Scenarios:
	 * <p/>
	 * 1. All rings ahead of start of fin.
	 * 2. First ring ahead of start of fin.  Second ring ahead of end of fin.
	 * 3. First ring ahead of start of fin.  Second ring behind end of fin.
	 * 4. First ring equal or behind start of fin.  Second ring ahead of, or equal to, end of fin.
	 * 5. First ring equal or behind start of fin. Second ring behind end of fin.
	 * 6. All rings behind end of fin.
	 *
	 * @param rings              an unordered list of centering rings attached to the parent of the fin set
	 * @param finPositionFromTop the position from the top of the parent of the start of the fin set root
	 * @param finLength          the length of the root chord
	 * @param mts                the model for the tab shift (position); the model's value is modified as a result of this method call
     * @param relativeTo         the parent component of the finset
     * 
	 * @return the length of the fin tab
	 */
	private static double computeFinTabLength(List<CenteringRing> rings, Double finPositionFromTop, Double finLength, DoubleModel mts,
                                              final RocketComponent relativeTo) {
		List<SortableRing> positionsFromTop = new ArrayList<>();
		
		//Fin tabs will be computed between the last two rings that meet the criteria, represented by top and bottom here.
		SortableRing top = null;
		SortableRing bottom = null;
		
		if (rings != null) {
			//Sort rings from top of parent to bottom
			rings.sort(new Comparator<>() {
				@Override
				public int compare(CenteringRing centeringRing, CenteringRing centeringRing1) {
					return (int) (1000.0d * (centeringRing.getAxialOffset(AxialMethod.TOP) -
							centeringRing1.getAxialOffset(AxialMethod.TOP)));
				}
			});

			for (CenteringRing centeringRing : rings) {
				//Handle centering rings that overlap or are adjacent by synthetically merging them into one virtual ring.
				if (!positionsFromTop.isEmpty() &&
						positionsFromTop.get(positionsFromTop.size() - 1).bottomSidePositionFromTop() >=
								centeringRing.getAxialOffset(AxialMethod.TOP)) {
					SortableRing adjacent = positionsFromTop.get(positionsFromTop.size() - 1);
					adjacent.merge(centeringRing, relativeTo);
				} else {
					positionsFromTop.add(new SortableRing(centeringRing, relativeTo));
				}
			}

			for (SortableRing sortableRing : positionsFromTop) {
				if (top == null) {
					top = sortableRing;
				} else if (sortableRing.bottomSidePositionFromTop() <= finPositionFromTop) {
					top = sortableRing;
					bottom = null;
				} else if (top.bottomSidePositionFromTop() <= finPositionFromTop) {
					if (bottom == null) {
						//If the current ring is in the upper half of the root chord, make it the top ring
						if (sortableRing.bottomSidePositionFromTop() < finPositionFromTop + finLength / 2.0d) {
							top = sortableRing;
						} else {
							bottom = sortableRing;
						}
					}
					//Is the ring even with or above the end of the root chord? If so, make the existing bottom the top ring,
					//and the current ring the bottom
					else if (sortableRing.positionFromTop() <= finPositionFromTop + finLength) {
						top = bottom;
						bottom = sortableRing;
					}
				} else {
					if (bottom == null) {
						bottom = sortableRing;
					}
				}
			}
		}

        double resultFinTabLength = 0.0d;

		// Edge case where there are no centering rings or for some odd reason top and bottom are identical.
		if (top == null || top == bottom) {
			mts.setValue(0);
			resultFinTabLength = finLength;
		} else if (bottom == null) {
			// If there is no bottom ring and the top ring's bottom edge is within the span of the root chord, then
			// set the position of the fin tab starting at the bottom side of the top ring.
			if (top.bottomSidePositionFromTop() >= finPositionFromTop) {
				mts.setValue(top.bottomSidePositionFromTop() - finPositionFromTop);
				resultFinTabLength = (finPositionFromTop + finLength - top.bottomSidePositionFromTop());
			} else {
				mts.setValue(0);
                double diffLen = top.positionFromTop() - finPositionFromTop;
                if (diffLen < 0) {
                // Otherwise the top ring is outside the span of the root chord so set the tab length to be the entire
                // root chord.
                    resultFinTabLength = finLength;
                }
                else {
                    // Otherwise there is one ring within the span. Return the length from the start of the fin to the top
                    // side of the ring.
                    resultFinTabLength = diffLen;
                }
            }
		}
		// If the bottom edge of the top centering ring is above the start of the fin's root chord, then make the
		// fin tab align with the start of the root chord.
		else if (top.bottomSidePositionFromTop() < finPositionFromTop) {
			mts.setValue(0);

            double lenToBottomRing = bottom.positionFromTop - finPositionFromTop;
            // If the bottom ring lies farther back (down) than the trailing edge of the fin, then the tab should
            // only be as long as the fin.
            if (lenToBottomRing > finLength) {
                resultFinTabLength = finLength;
            }
            else {
                resultFinTabLength = lenToBottomRing;
            }
		} else {
			mts.setValue(top.bottomSidePositionFromTop() - finPositionFromTop);
            // The bottom ring is beyond the trailing edge of the fin.
            if (bottom.positionFromTop() > finLength + finPositionFromTop) {
                resultFinTabLength = (finLength + finPositionFromTop - top.bottomSidePositionFromTop());
            }
			// The rings are within the span of the root chord.  Place the tab between them.
            else {
			    resultFinTabLength = (bottom.positionFromTop() - top.bottomSidePositionFromTop());
            }
        }
        if (resultFinTabLength < 0) {
            resultFinTabLength = 0.0d;
        }
        return resultFinTabLength;
	}

	/**
	 * Check whether a component lays within the fin set.
	 * @param c component to check
	 * @return True if the component lays inside the fin span, or partially inside, False if completely outside the span.
	 */
	private boolean isComponentInsideFinSpan(RocketComponent c) {
		final double finXMin = component.getAxialOffset(AxialMethod.ABSOLUTE);
		final double finXMax = finXMin + component.getLength();
		final double compXMin = c.getAxialOffset(AxialMethod.ABSOLUTE);
		final double compXMax = compXMin + c.getLength();
		return (compXMin >= finXMin && compXMin < finXMax) || (compXMax > finXMin && compXMax <= finXMax) ||
				(compXMin <= finXMin && compXMax >= finXMax);
	}
	
	@Override
	public void updateFields() {
		super.updateFields();
		if (split != null)
			split.setEnabled(((FinSet) component).getFinCount() > 1);
	}
	
	/**
	 * A container class to store pertinent info about centering rings.  This is used in the computation to figure
	 * out tab length and position.
	 */
	static class SortableRing {
		
		/**
		 * The length of the ring (more commonly called the thickness).
		 */
		private double thickness;
		/**
		 * The position of the ring from the top of the parent.
		 */
		private double positionFromTop;
		
		/**
		 * Constructor.
		 *
		 * @param r the source centering ring
		 */
		SortableRing(CenteringRing r, RocketComponent relativeTo) {
			thickness = r.getLength();
			positionFromTop = r.getAxialOffset(AxialMethod.TOP);
		}
		
		/**
		 * Merge an adjacent ring.
		 *
		 * @param adjacent the adjacent ring
		 */
		public void merge(CenteringRing adjacent, RocketComponent relativeTo) {
			double v = adjacent.getAxialOffset(AxialMethod.TOP);
			if (positionFromTop < v) {
				thickness = (v + adjacent.getLength()) - positionFromTop;
			} else {
				double tmp = positionFromTop + thickness;
				positionFromTop = v;
				thickness = tmp - v;
			}
		}
		
		/**
		 * Compute the position of the bottom edge of the ring, relative to the top of the parent.
		 *
		 * @return the distance from the top of the parent to the bottom edge of the ring
		 */
		public double bottomSidePositionFromTop() {
			return positionFromTop + thickness;
		}
		
		/**
		 * Compute the position of the top edge of the ring, relative to the top of the parent.
		 *
		 * @return the distance from the top of the parent to the top edge of the ring
		 */
		public double positionFromTop() {
			return positionFromTop;
		}
	}
	
	protected JPanel filletMaterialPanel(){
	    
	    JPanel filletPanel = new JPanel(new MigLayout("", "[][65lp::][30lp::]"));
	    String tip = trans.get("FinsetConfig.ttip.Finfillets1") +
		    	trans.get("FinsetConfig.ttip.Finfillets2") +
		    	trans.get("FinsetConfig.ttip.Finfillets3");
	    filletPanel.setBorder(BorderFactory.createTitledBorder(trans.get("FinSetConfig.title.RootFillets")));

		// Fillet Radius:
	    filletPanel.add(new JLabel(trans.get("FinSetConfig.lbl.Filletradius")));
		
	    DoubleModel m = new DoubleModel(component, "FilletRadius", UnitGroup.UNITS_LENGTH, 0);
		register(m);

	    JSpinner spin = new JSpinner(m.getSpinnerModel());
	    spin.setEditor(new SpinnerEditor(spin));
	    spin.setToolTipText(tip);
	    filletPanel.add(spin, "growx, w 40");
		order.add(((SpinnerEditor) spin.getEditor()).getTextField());

	    UnitSelector us = new UnitSelector(m);
	    filletPanel.add(us, "growx");
	    us.setToolTipText(tip);

	    BasicSlider bs = new BasicSlider(m.getSliderModel(0, 0.1));
	    filletPanel.add(bs, "w 100lp, wrap para");
	    bs.setToolTipText(tip);

		// Fillet Material:
	    JLabel label = new JLabel(trans.get("FinSetConfig.lbl.Finfilletmaterial"));
	    label.setToolTipText(tip);
	    //// The component material affects the weight of the component.
	    label.setToolTipText(trans.get("MaterialPanel.lbl.ttip.ComponentMaterialAffects"));
	    filletPanel.add(label, "spanx 4, wrap rel");

		MaterialModel mm = new MaterialModel(filletPanel, document, component, Material.Type.BULK, "FilletMaterial");
		register(mm);

		// Material selection combo box
		GroupableAndSearchableComboBox<MaterialGroup, Material> materialCombo = MaterialComboBox.createComboBox(document, mm);
		materialCombo.setToolTipText(trans.get("MaterialPanel.combo.ttip.ComponentMaterialAffects"));
	    filletPanel.add(materialCombo, "spanx 4, growx");
		order.add(materialCombo);
	    filletPanel.setToolTipText(tip);

	    return filletPanel;
	}

	/**
	 * Writes the FinSet object to an SVG file.
	 *
	 * @param finSet      the FinSet object to write to the SVG file
	 * @param file        the File object representing the SVG file to be written
	 * @param svgOptions  the SVGOptionPanel object containing the options for writing the SVG file
	 * @throws Exception if there is an error writing the SVG file
	 */
	public static void writeSVGFile(FinSet finSet, File file, SVGOptionPanel svgOptions) throws ParserConfigurationException, TransformerException {
		Coordinate[] points = finSet.generateContinuousFinAndTabShape();

		SVGBuilder builder = new SVGBuilder();
		builder.addPath(points, null, svgOptions.getStrokeColor(), svgOptions.getStrokeWidth());

		// Export fin tab separately if it's beyond the fin
		if (finSet.isTabBeyondFin()) {
			Coordinate[] tabPoints = finSet.getTabPointsWithRoot();
			Coordinate finFront = finSet.getFinFront();
			// Need to offset to the fin front because the tab points are relative to the fin front
			builder.addPath(tabPoints, finFront.x, finFront.y, null, svgOptions.getStrokeColor(), svgOptions.getStrokeWidth());
		}

		builder.writeToFile(file);
	}
}
