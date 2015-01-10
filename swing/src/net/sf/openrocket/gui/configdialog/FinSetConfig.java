package net.sf.openrocket.gui.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.Coaxial;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class FinSetConfig extends RocketComponentConfig {
	private static final Logger log = LoggerFactory.getLogger(FinSetConfig.class);
	private static final Translator trans = Application.getTranslator();
	
	private JButton split = null;
	
	public FinSetConfig(OpenRocketDocument d, RocketComponent component) {
		super(d, component);
		
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
					
					ComponentConfigDialog.hideDialog();
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
				
				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						RocketComponent parent = component.getParent();
						int index = parent.getChildPosition(component);
						int count = ((FinSet) component).getFinCount();
						double base = ((FinSet) component).getBaseRotation();
						if (count <= 1)
							return;
						
						document.addUndoPosition("Split fin set");
						parent.removeChild(index);
						for (int i = 0; i < count; i++) {
							FinSet copy = (FinSet) component.copy();
							copy.setFinCount(1);
							copy.setBaseRotation(base + i * 2 * Math.PI / count);
							copy.setName(copy.getName() + " #" + (i + 1));
							parent.addChild(copy, index + i);
						}
					}
				});
				
				ComponentConfigDialog.hideDialog();
			}
		});
		split.setEnabled(((FinSet) component).getFinCount() > 1);
		
		if (convert == null)
			addButtons(split);
		else
			addButtons(split, convert);
		
	}
	
	public JPanel finTabPanel() {
		JPanel panel = new JPanel(
				new MigLayout("align 50% 20%, fillx, gap rel unrel, ins 20lp 10% 20lp 10%",
						"[150lp::][65lp::][30lp::][200lp::]", ""));
		//		JPanel panel = new JPanel(new MigLayout("fillx, align 20% 20%, gap rel unrel",
		//				"[40lp][80lp::][30lp::][100lp::]",""));
		
		//// Through-the-wall fin tabs:
		panel.add(new StyledLabel(trans.get("FinSetConfig.lbl.Through-the-wall"), Style.BOLD),
				"spanx, wrap 30lp");
		
		JLabel label;
		DoubleModel length;
		DoubleModel length2;
		DoubleModel length_2;
		JSpinner spin;
		JButton autoCalc;
		
		length = new DoubleModel(component, "Length", UnitGroup.UNITS_LENGTH, 0);
		length2 = new DoubleModel(component, "Length", 0.5, UnitGroup.UNITS_LENGTH, 0);
		length_2 = new DoubleModel(component, "Length", -0.5, UnitGroup.UNITS_LENGTH, 0);
		
		register(length);
		register(length2);
		register(length_2);
		
		////  Tab length
		//// Tab length:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tablength"));
		//// The length of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tablength"));
		panel.add(label, "gapleft para, gapright 40lp, growx 1");
		
		final DoubleModel mtl = new DoubleModel(component, "TabLength", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(mtl.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx 1");
		
		panel.add(new UnitSelector(mtl), "growx 1");
		panel.add(new BasicSlider(mtl.getSliderModel(DoubleModel.ZERO, length)),
				"w 100lp, growx 5, wrap");
		

		////  Tab length
		//// Tab height:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabheight"));
		//// The spanwise height of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabheight"));
		panel.add(label, "gapleft para");
		
		final DoubleModel mth = new DoubleModel(component, "TabHeight", UnitGroup.UNITS_LENGTH, 0);
		
		spin = new JSpinner(mth.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(mth), "growx");
		panel.add(new BasicSlider(mth.getSliderModel(DoubleModel.ZERO, length2)),
				"w 100lp, growx 5, wrap");
		
		////  Tab position:
		label = new JLabel(trans.get("FinSetConfig.lbl.Tabposition"));
		//// The position of the fin tab.
		label.setToolTipText(trans.get("FinSetConfig.ttip.Tabposition"));
		panel.add(label, "gapleft para");
		
		final DoubleModel mts = new DoubleModel(component, "TabShift", UnitGroup.UNITS_LENGTH);
		
		spin = new JSpinner(mts.getSpinnerModel());
		spin.setEditor(new SpinnerEditor(spin));
		panel.add(spin, "growx");
		
		panel.add(new UnitSelector(mts), "growx");
		panel.add(new BasicSlider(mts.getSliderModel(length_2, length2)), "w 100lp, growx 5, wrap");
		

		//// relative to
		label = new JLabel(trans.get("FinSetConfig.lbl.relativeto"));
		panel.add(label, "right, gapright unrel");
		
		final EnumModel<FinSet.TabRelativePosition> em =
				new EnumModel<FinSet.TabRelativePosition>(component, "TabRelativePosition");
		
		panel.add(new JComboBox(em), "spanx 3, growx, wrap para");
		

		// Calculate fin tab height, length, and position
		autoCalc = new JButton(trans.get("FinSetConfig.but.AutoCalc"));
		
		autoCalc.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				log.info(Markers.USER_MARKER, "Computing " + component.getComponentName() + " tab height.");
				
				RocketComponent parent = component.getParent();
				if (parent instanceof Coaxial) {
					try {
						document.startUndo("Compute fin tabs");
						
						List<CenteringRing> rings = new ArrayList<CenteringRing>();
                        //Do deep recursive iteration
                        Iterator<RocketComponent> iter = parent.iterator(false);
                        while (iter.hasNext()) {
                            RocketComponent rocketComponent =  iter.next();
							if (rocketComponent instanceof InnerTube) {
								InnerTube it = (InnerTube) rocketComponent;
								if (it.isMotorMount()) {
									double depth = ((Coaxial) parent).getOuterRadius() - it.getOuterRadius();
									//Set fin tab depth
									if (depth >= 0.0d) {
										mth.setValue(depth);
										mth.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
									}
								}
							} else if (rocketComponent instanceof CenteringRing) {
								rings.add((CenteringRing) rocketComponent);
							}
						}
						//Figure out position and length of the fin tab
						if (!rings.isEmpty()) {
							FinSet.TabRelativePosition temp = (FinSet.TabRelativePosition) em.getSelectedItem();
							em.setSelectedItem(FinSet.TabRelativePosition.FRONT);
							double len = computeFinTabLength(rings, component.asPositionValue(RocketComponent.Position.TOP, parent),
										component.getLength(), mts, parent);
							mtl.setValue(len);
							//Be nice to the user and set the tab relative position enum back the way they had it.
							em.setSelectedItem(temp);
						}
						
					} finally {
						document.stopUndo();
					}
				}
			}
		});
		panel.add(autoCalc, "skip 1, spanx");
		
		return panel;
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
		List<SortableRing> positionsFromTop = new ArrayList<SortableRing>();
		
		//Fin tabs will be computed between the last two rings that meet the criteria, represented by top and bottom here.
		SortableRing top = null;
		SortableRing bottom = null;
		
		if (rings != null) {
			//Sort rings from top of parent to bottom
			Collections.sort(rings, new Comparator<CenteringRing>() {
				@Override
				public int compare(CenteringRing centeringRing, CenteringRing centeringRing1) {
					return (int) (1000d * (centeringRing.asPositionValue(RocketComponent.Position.TOP, relativeTo) -
							centeringRing1.asPositionValue(RocketComponent.Position.TOP, relativeTo)));
						}
			});
			
			for (int i = 0; i < rings.size(); i++) {
				CenteringRing centeringRing = rings.get(i);
				//Handle centering rings that overlap or are adjacent by synthetically merging them into one virtual ring.
				if (!positionsFromTop.isEmpty() &&
						positionsFromTop.get(positionsFromTop.size() - 1).bottomSidePositionFromTop() >=
                                centeringRing.asPositionValue(RocketComponent.Position.TOP, relativeTo)) {
					SortableRing adjacent = positionsFromTop.get(positionsFromTop.size() - 1);
					adjacent.merge(centeringRing, relativeTo);
				} else {
					positionsFromTop.add(new SortableRing(centeringRing, relativeTo));
				}
			}
			
			for (int i = 0; i < positionsFromTop.size(); i++) {
				SortableRing sortableRing = positionsFromTop.get(i);
				if (top == null) {
					top = sortableRing;
				} else if (sortableRing.bottomSidePositionFromTop() <= finPositionFromTop) {
					top = sortableRing;
					bottom = null;
				} else if (top.bottomSidePositionFromTop() <= finPositionFromTop) {
					if (bottom == null) {
						//If the current ring is in the upper half of the root chord, make it the top ring
						if (sortableRing.bottomSidePositionFromTop() < finPositionFromTop + finLength / 2d) {
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

        double resultFinTabLength = 0d;

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
            resultFinTabLength = 0d;
        }
        return resultFinTabLength;
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
			positionFromTop = r.asPositionValue(RocketComponent.Position.TOP, relativeTo);
		}
		
		/**
		 * Merge an adjacent ring.
		 *
		 * @param adjacent the adjacent ring
		 */
		public void merge(CenteringRing adjacent, RocketComponent relativeTo) {
			double v = adjacent.asPositionValue(RocketComponent.Position.TOP, relativeTo);
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
	    
	    JPanel filletPanel=new JPanel(new MigLayout("", "[][65lp::][30lp::]"));
	    String tip = trans.get("FinsetCfg.ttip.Finfillets1") +
		    	trans.get("FinsetCfg.ttip.Finfillets2") +
		    	trans.get("FinsetCfg.ttip.Finfillets3");
	    filletPanel.setBorder(BorderFactory.createTitledBorder("Root Fillets"));
	    filletPanel.add(new JLabel(trans.get("FinSetCfg.lbl.Filletradius")));
		
	    DoubleModel m = new DoubleModel(component, "FilletRadius", UnitGroup.UNITS_LENGTH, 0);
		
	    JSpinner spin = new JSpinner(m.getSpinnerModel());
	    spin.setEditor(new SpinnerEditor(spin));
	    spin.setToolTipText(tip);
	    filletPanel.add(spin, "growx, w 40");
	    UnitSelector us = new UnitSelector(m); 
	    filletPanel.add(us, "growx");
	    us.setToolTipText(tip);
	    BasicSlider bs =new BasicSlider(m.getSliderModel(0, 10));
	    filletPanel.add(bs, "w 100lp, wrap para");
	    bs.setToolTipText(tip);
	    
	    JLabel label = new JLabel(trans.get("FinSetCfg.lbl.Finfilletmaterial"));
	    label.setToolTipText(tip);
	    //// The component material affects the weight of the component.
	    label.setToolTipText(trans.get("RocketCompCfg.lbl.ttip.componentmaterialaffects"));
	    filletPanel.add(label, "spanx 4, wrap rel");
		
	    JComboBox combo = new JComboBox(new MaterialModel(filletPanel, component, Material.Type.BULK, "FilletMaterial"));
	    //// The component material affects the weight of the component.
	    combo.setToolTipText(trans.get("RocketCompCfg.combo.ttip.componentmaterialaffects"));
	    filletPanel.add(combo, "spanx 4, growx, wrap paragraph");
	    filletPanel.setToolTipText(tip);
	    return filletPanel;
	}
}
