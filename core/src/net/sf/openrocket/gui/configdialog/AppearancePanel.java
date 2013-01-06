package net.sf.openrocket.gui.configdialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DecalModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ColorIcon;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.gui.util.EditDecalHelper;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.GeneralUnit;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.StateChangeListener;

public class AppearancePanel extends JPanel {
	private static final Translator trans = Application.getTranslator();
	
	private AppearanceBuilder ab;
	
	/**
	 * A non-unit that adjusts by a small amount, suitable for
	 * values that are on the 0-1 scale
	 */
	private final static UnitGroup TEXTURE_UNIT = new UnitGroup();
	static {
		Unit no_unit = new GeneralUnit(1, "", 2) {
			@Override
			public double getNextValue(double value) {
				return value + .1;
			}
			
			@Override
			public double getPreviousValue(double value) {
				return value - .1;
			}
			
		};
		TEXTURE_UNIT.addUnit(no_unit);
	}
	
	private static final JColorChooser colorChooser = new JColorChooser();
	
	private class ColorActionListener implements ActionListener {
		private final String valueName;
		private final Object o;
		
		ColorActionListener(final Object o, final String valueName) {
			this.valueName = valueName;
			this.o = o;
		}
		
		@Override
		public void actionPerformed(ActionEvent colorClickEvent) {
			try {
				final Method getMethod = o.getClass().getMethod("get" + valueName);
				final Method setMethod = o.getClass().getMethod("set" + valueName, net.sf.openrocket.util.Color.class);
				net.sf.openrocket.util.Color c = (net.sf.openrocket.util.Color) getMethod.invoke(o);
				Color awtColor = ColorConversion.toAwtColor(c);
				colorChooser.setColor(awtColor);
				JDialog d = JColorChooser.createDialog(AppearancePanel.this,
						trans.get("RocketCompCfg.lbl.Choosecolor"), true, colorChooser, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent okEvent) {
								Color selected = colorChooser.getColor();
								if (selected == null)
									return;
								try {
									setMethod.invoke(o, ColorConversion.fromAwtColor(selected));
								} catch (Throwable e1) {
									Application.getExceptionHandler().handleErrorCondition(e1);
								}
							}
						}, null);
				d.setVisible(true);
			} catch (Throwable e1) {
				Application.getExceptionHandler().handleErrorCondition(e1);
			}
		}
	}
	
	public AppearancePanel(final OpenRocketDocument document, final RocketComponent c) {
		super(new MigLayout("fill", "[150][grow][150][grow]"));
		
		ab = new AppearanceBuilder(c.getAppearance());
		
		net.sf.openrocket.util.Color figureColor = c.getColor();
		if (figureColor == null) {
			figureColor = Application.getPreferences().getDefaultColor(c.getClass());
		}
		final JButton figureColorButton = new JButton(new ColorIcon(figureColor));
		
		final JButton colorButton = new JButton(new ColorIcon(ab.getPaint()));
		
		final JComboBox textureDropDown = new JComboBox(new DecalModel(this, document, ab));
		
		ab.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				figureColorButton.setIcon(new ColorIcon(c.getColor()));
				colorButton.setIcon(new ColorIcon(ab.getPaint()));
				c.setAppearance(ab.getAppearance());
			}
		});
		
		c.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				net.sf.openrocket.util.Color col = c.getColor();
				if (col == null) {
					col = Application.getPreferences().getDefaultColor(c.getClass());
				}
				figureColorButton.setIcon(new ColorIcon(col));
			}
		});
		
		figureColorButton.addActionListener(new ColorActionListener(c, "Color"));
		colorButton.addActionListener(new ColorActionListener(ab, "Paint"));
		
		BooleanModel fDefault = new BooleanModel(c.getColor() == null);
		
		
		{// Style Header Row
			final JCheckBox colorDefault = new JCheckBox(fDefault);
			colorDefault.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (colorDefault.isSelected()) {
						c.setColor(null);
						c.setLineStyle(null);
					} else {
						c.setColor(((SwingPreferences) Application.getPreferences()).getDefaultColor(c.getClass()));
						c.setLineStyle(((SwingPreferences) Application.getPreferences()).getDefaultLineStyle(c.getClass()));
					}
				}
			});
			colorDefault.setText(trans.get("RocketCompCfg.checkbox.Usedefaultcolor"));
			add(new StyledLabel(trans.get("RocketCompCfg.lbl.Figurestyle"), Style.BOLD));
			add(colorDefault);
			
			JButton button = new JButton(trans.get("RocketCompCfg.but.Saveasdefstyle"));
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (c.getColor() != null) {
						((SwingPreferences) Application.getPreferences()).setDefaultColor(c.getClass(), c.getColor());
						c.setColor(null);
					}
					if (c.getLineStyle() != null) {
						Application.getPreferences().setDefaultLineStyle(c.getClass(), c.getLineStyle());
						c.setLineStyle(null);
					}
				}
			});
			fDefault.addEnableComponent(button, false);
			add(button, "span 2, align right, wrap");
		}
		
		{// Figure Color
			add(new JLabel(trans.get("RocketCompCfg.lbl.Componentcolor")));
			fDefault.addEnableComponent(figureColorButton, false);
			add(figureColorButton);
		}
		
		{// Line Style
		
			add(new JLabel(trans.get("RocketCompCfg.lbl.Complinestyle")));
			
			LineStyle[] list = new LineStyle[LineStyle.values().length + 1];
			System.arraycopy(LineStyle.values(), 0, list, 1, LineStyle.values().length);
			
			JComboBox combo = new JComboBox(new EnumModel<LineStyle>(c, "LineStyle",
					//// Default style
					list, trans.get("LineStyle.Defaultstyle")));
			
			fDefault.addEnableComponent(combo, false);
			
			add(combo, "wrap");
		}
		
		add(new JSeparator(SwingConstants.HORIZONTAL), "span, wrap, growx");
		
		{// Texture Header Row
			add(new StyledLabel(trans.get("AppearanceCfg.lbl.Appearance"), Style.BOLD), "wrap");
		}
		
		{// Texture File
			add(new JLabel(trans.get("AppearanceCfg.lbl.Texture")));
			JPanel p = new JPanel(new MigLayout("fill, ins 0", "[grow][]"));
			p.add(textureDropDown, "grow");
			add(p, "span 3, growx, wrap");
			final JButton editBtn = new JButton(trans.get("AppearanceCfg.but.edit"));
			editBtn.setEnabled(ab.getImage() != null);
			ab.addChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(EventObject e) {
					editBtn.setEnabled(ab.getImage() == null);
				}
			});
			editBtn.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						EditDecalHelper.editDecal(SwingUtilities.getWindowAncestor(AppearancePanel.this), ab.getImage());
					} catch (IOException ex) {
						throw new BugException(ex);
					}
				}
				
			});
			p.add(editBtn);
		}
		
		{ // Color
			add(new JLabel(trans.get("AppearanceCfg.lbl.color.Color")));
			//mDefault.addEnableComponent(colorButton, false);
			colorButton.setEnabled(ab.getImage() == null || ab.getEdgeMode() == Decal.EdgeMode.STICKER);
			add(colorButton);
			ab.addChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(EventObject e) {
					colorButton.setEnabled(ab.getImage() == null || ab.getEdgeMode() == Decal.EdgeMode.STICKER);
				}
			});
		}
		
		{ // Scale
			add(new JLabel(trans.get("AppearanceCfg.lbl.texture.scale")));
			
			add(new JLabel("x:"), "split 4");
			JSpinner scaleU = new JSpinner(new DoubleModel(ab, "ScaleX", TEXTURE_UNIT).getSpinnerModel());
			scaleU.setEditor(new SpinnerEditor(scaleU));
			add(scaleU, "w 40");
			
			add(new JLabel("y:"));
			JSpinner scaleV = new JSpinner(new DoubleModel(ab, "ScaleY", TEXTURE_UNIT).getSpinnerModel());
			scaleV.setEditor(new SpinnerEditor(scaleV));
			add(scaleV, "wrap, w 40");
		}
		
		{// Shine
			add(new JLabel(trans.get("AppearanceCfg.lbl.shine")));
			DoubleModel shineModel = new DoubleModel(ab, "Shine", UnitGroup.UNITS_RELATIVE);
			JSpinner spin = new JSpinner(shineModel.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			JSlider slide = new JSlider(shineModel.getSliderModel(0, 1));
			UnitSelector unit = new UnitSelector(shineModel);
			
			add(spin, "split 3, w 50");
			add(unit, "growx");
			add(slide, "w 50");
		}
		
		
		{ // Offset
			add(new JLabel(trans.get("AppearanceCfg.lbl.texture.offset")));
			
			add(new JLabel("x:"), "split 4");
			JSpinner offsetU = new JSpinner(new DoubleModel(ab, "OffsetU", TEXTURE_UNIT).getSpinnerModel());
			offsetU.setEditor(new SpinnerEditor(offsetU));
			add(offsetU, "w 40");
			
			add(new JLabel("y:"));
			JSpinner offsetV = new JSpinner(new DoubleModel(ab, "OffsetV", TEXTURE_UNIT).getSpinnerModel());
			offsetV.setEditor(new SpinnerEditor(offsetV));
			add(offsetV, "wrap, w 40");
		}
		
		{ // Repeat
			add(new JLabel(trans.get("AppearanceCfg.lbl.texture.repeat")));
			EdgeMode[] list = new EdgeMode[EdgeMode.values().length + 1];
			System.arraycopy(EdgeMode.values(), 0, list, 1, EdgeMode.values().length);
			JComboBox combo = new JComboBox(new EnumModel<EdgeMode>(ab, "EdgeMode", list));
			add(combo);
		}
		
		
		{ // Rotation
			add(new JLabel(trans.get("AppearanceCfg.lbl.texture.rotation")));
			DoubleModel rotationModel = new DoubleModel(ab, "Rotation", UnitGroup.UNITS_ANGLE);
			JSpinner rotation = new JSpinner(rotationModel.getSpinnerModel());
			rotation.setEditor(new SpinnerEditor(rotation));
			add(rotation, "split 3, w 50");
			add(new UnitSelector(rotationModel));
			BasicSlider bs = new BasicSlider(rotationModel.getSliderModel(-Math.PI, Math.PI));
			add(bs, "w 50, wrap");
		}
		
		
	}
	
}
