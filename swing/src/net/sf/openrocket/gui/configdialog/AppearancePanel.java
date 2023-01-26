package net.sf.openrocket.gui.configdialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.EventObject;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.appearance.Appearance;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.appearance.defaults.DefaultAppearance;
import net.sf.openrocket.arch.SystemInfo;
import net.sf.openrocket.arch.SystemInfo.Platform;
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
import net.sf.openrocket.gui.util.EditDecalHelper.EditDecalHelperException;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.InsideColorComponent;
import net.sf.openrocket.rocketcomponent.InsideColorComponentHandler;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.GeneralUnit;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.LineStyle;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.gui.widgets.SelectColorButton;

public class AppearancePanel extends JPanel {
	private static final long serialVersionUID = 2709187552673202019L;

	private static final Translator trans = Application.getTranslator();

	final private EditDecalHelper editDecalHelper = Application.getInjector()
			.getInstance(EditDecalHelper.class);

	// Outside and inside appearance builder
	final private AppearanceBuilder ab;
	private AppearanceBuilder insideAb;

	// We hang on to the user selected appearance when switching to default
	// appearance.
	// this appearance is restored if the user unchecks the "default" button.
	private Appearance previousUserSelectedAppearance = null;
	private Appearance previousUserSelectedInsideAppearance = null;

	// We cache the default appearance for this component to make switching
	// faster.
	private Appearance defaultAppearance = null;

	private JTabbedPane outsideInsidePane = null;

	private JCheckBox customInside = null;

	private final List<Component> order;	// Component traversal order

	/**
	 * A non-unit that adjusts by a small amount, suitable for values that are
	 * on the 0-1 scale
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

	public void clearConfigListeners() {
		if (ab != null) {
			ab.clearConfigListeners();
		}
		if (insideAb != null) {
			insideAb.clearConfigListeners();
		}
	}

	private class ColorActionListener implements ActionListener {
		private final String valueName;
		private final Object o;

		ColorActionListener(final Object o, final String valueName) {
			this.valueName = valueName;
			this.o = o;
		}

		/**
		 Changes the color of the selected component to <color>
		 @param color: color to change the component to
		 */
		private void changeComponentColor(Color color) {
			try {
				final Method setMethod = o.getClass().getMethod(
						"set" + valueName, net.sf.openrocket.util.Color.class);
				if (color == null)
					return;
				try {
					setMethod.invoke(o, ColorConversion
							.fromAwtColor(color));
				} catch (Throwable e1) {
					Application.getExceptionHandler()
							.handleErrorCondition(e1);
				}
			} catch (Throwable e1) {
				Application.getExceptionHandler().handleErrorCondition(e1);
			}

		}

		/**
		 * Change the component's preview color upon a change in color pick. If the user clicks 'OK' in the
		 * dialog window, the selected color is assigned to the component. If 'Cancel' was clicked, the component's
		 * color will be reverted to its initial color (color before the appearance editor opened).
		 */
		@Override
		public void actionPerformed(ActionEvent colorClickEvent) {
			try {
				final Method getMethod = o.getClass().getMethod(
						"get" + valueName);
				net.sf.openrocket.util.Color c = (net.sf.openrocket.util.Color) getMethod
						.invoke(o);

				Color awtColor = ColorConversion.toAwtColor(c);
				colorChooser.setColor(awtColor);

				// Bind a change of color selection to a change in the components color
				ColorSelectionModel model = colorChooser.getSelectionModel();
				ChangeListener changeListener = new ChangeListener() {
					public void stateChanged(ChangeEvent changeEvent) {
						Color selected = colorChooser.getColor();
						changeComponentColor(selected);
					}
				};
				model.addChangeListener(changeListener);

				JDialog d = JColorChooser.createDialog(AppearancePanel.this,
						trans.get("RocketCompCfg.lbl.Choosecolor"), true,
						colorChooser, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent okEvent) {
								changeComponentColor(colorChooser.getColor());
								// Unbind listener to avoid the current component's appearance to change with other components
								model.removeChangeListener(changeListener);
							}
						}, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent cancelEvent) {
								changeComponentColor(awtColor);
								// Unbind listener to avoid the current component's appearance to change with other components
								model.removeChangeListener(changeListener);
							}
						});
				d.setVisible(true);
			} catch (Throwable e1) {
				Application.getExceptionHandler().handleErrorCondition(e1);
			}
		}
	}

	/**
	 * Appearance panel for the appearance of a rocket component.
	 * @param document current document
	 * @param c component to change the appearance of
	 * @param parent parent dialog
	 * @param order component traversal order object of the component config dialog
	 */
	public AppearancePanel(final OpenRocketDocument document, final RocketComponent c, final JDialog parent, List<Component> order) {
		super(new MigLayout("fillx", "[150][grow][150][grow]"));

		this.order = order;
		defaultAppearance = DefaultAppearance.getDefaultAppearance(c);

		previousUserSelectedAppearance = c.getAppearance();
		if (previousUserSelectedAppearance == null) {
			previousUserSelectedAppearance = new AppearanceBuilder().getAppearance();
			ab = new AppearanceBuilder(defaultAppearance);
		} else {
			ab = new AppearanceBuilder(previousUserSelectedAppearance);
		}
		for (RocketComponent listener : c.getConfigListeners()) {
			Appearance a = listener.getAppearance();
			AppearanceBuilder appearanceBuilder = new AppearanceBuilder(a);
			ab.addConfigListener(listener, appearanceBuilder);
		}

		// Check if all InsideColorComponent
		boolean allInsideColor = c instanceof InsideColorComponent;
		if (allInsideColor) {
			for (RocketComponent listener :  c.getConfigListeners()) {
				if (!(listener instanceof InsideColorComponent)) {
					allInsideColor = false;
					break;
				}
			}
		}

		if (allInsideColor) {
			previousUserSelectedInsideAppearance = ((InsideColorComponent) c).getInsideColorComponentHandler()
					.getInsideAppearance();
			if (previousUserSelectedInsideAppearance == null) {
				previousUserSelectedInsideAppearance = new AppearanceBuilder().getAppearance();
				insideAb = new AppearanceBuilder(defaultAppearance);
			} else {
				insideAb = new AppearanceBuilder(previousUserSelectedInsideAppearance);
			}

			for (RocketComponent listener : c.getConfigListeners()) {
				Appearance a = ((InsideColorComponent) listener).getInsideColorComponentHandler()
						.getInsideAppearance();
				AppearanceBuilder appearanceBuilder = new AppearanceBuilder(a);
				insideAb.addConfigListener(listener, appearanceBuilder);
			}
		}

		net.sf.openrocket.util.Color figureColor = c.getColor();
		if (figureColor == null) {
			figureColor = Application.getPreferences().getDefaultColor(
					c.getClass());
		}
		final JButton figureColorButton = new SelectColorButton(
				new ColorIcon(figureColor));

		ab.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) { figureColorButton.setIcon(new ColorIcon(c.getColor())); }
		});

		c.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				net.sf.openrocket.util.Color col = c.getColor();
				if (col == null) {
					col = Application.getPreferences().getDefaultColor(
							c.getClass());
				}
				figureColorButton.setIcon(new ColorIcon(col));
			}
		});

		figureColorButton
				.addActionListener(new ColorActionListener(c, "Color"));

		BooleanModel fDefault = new BooleanModel(c.getColor() == null);

		final JButton saveAsDefault;
		{// Style Header Row
			final JCheckBox colorDefault = new JCheckBox(fDefault);
			colorDefault.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (colorDefault.isSelected()) {
						c.setColor(null);
						c.setLineStyle(null);
					} else {
						c.setColor(((SwingPreferences) Application
								.getPreferences()).getDefaultColor(c.getClass()));
						c.setLineStyle(((SwingPreferences) Application
								.getPreferences()).getDefaultLineStyle(c
								.getClass()));
					}
				}
			});
			colorDefault.setText(trans
					.get("RocketCompCfg.checkbox.Usedefaultcolor"));
			add(new StyledLabel(trans.get("RocketCompCfg.lbl.Figurestyle"),
					Style.BOLD));
			add(colorDefault);
			order.add(colorDefault);

			saveAsDefault = new SelectColorButton(
					trans.get("RocketCompCfg.but.Saveasdefstyle"));
			saveAsDefault.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (c.getColor() != null) {
						((SwingPreferences) Application.getPreferences())
								.setDefaultColor(c.getClass(), c.getColor());
						c.setColor(null);
					}
					if (c.getLineStyle() != null) {
						Application.getPreferences().setDefaultLineStyle(
								c.getClass(), c.getLineStyle());
						c.setLineStyle(null);
					}
				}
			});
			fDefault.addEnableComponent(saveAsDefault, false);
			add(saveAsDefault, "span 2, align right, wrap");
		}

		{// Figure Color
			add(new JLabel(trans.get("RocketCompCfg.lbl.Componentcolor")));
			fDefault.addEnableComponent(figureColorButton, false);
			add(figureColorButton);
			order.add(figureColorButton);
		}

		order.add(saveAsDefault);

		{// Line Style
			add(new JLabel(trans.get("RocketCompCfg.lbl.Complinestyle")));

			LineStyle[] list = new LineStyle[LineStyle.values().length + 1];
			System.arraycopy(LineStyle.values(), 0, list, 1,
					LineStyle.values().length);

			final JComboBox<LineStyle> combo = new JComboBox<LineStyle>(new EnumModel<LineStyle>(c,
					"LineStyle",
					// // Default style
					list, trans.get("LineStyle.Defaultstyle")));

			fDefault.addEnableComponent(combo, false);

			add(combo, "growx, wrap");
			order.add(combo);
		}

		add(new JSeparator(SwingConstants.HORIZONTAL), "span, wrap, growx");

		// Display a tabbed panel for choosing the outside and inside appearance, if the object is of type InsideColorComponent
		if (allInsideColor) {
			InsideColorComponentHandler handler = ((InsideColorComponent)c).getInsideColorComponentHandler();

			// Get translator keys
			String tr_outside, tr_inside, tr_insideOutside, tr_insideOutside_ttip;
			if (c instanceof FinSet) {
				tr_outside = "RocketCompCfg.tab.LeftSide";
				tr_inside = "RocketCompCfg.tab.RightSide";
				tr_insideOutside = "AppearanceCfg.lbl.separateLeftSideRightSide";
				tr_insideOutside_ttip = "AppearanceCfg.lbl.ttip.separateLeftSideRightSide";
			}
			else {
				tr_outside = "RocketCompCfg.tab.Outside";
				tr_inside = "RocketCompCfg.tab.Inside";
				tr_insideOutside = "AppearanceCfg.lbl.separateInsideOutside";
				tr_insideOutside_ttip = "AppearanceCfg.lbl.ttip.separateInsideOutside";
			}

			// Checkbox for using separate outside/inside appearance
			BooleanModel b_customInside = new BooleanModel(handler.isSeparateInsideOutside());
			this.customInside = new JCheckBox(b_customInside);
			customInside.setText(trans.get(tr_insideOutside));
			customInside.setToolTipText(trans.get(tr_insideOutside_ttip));
			add(customInside, "span 2");
			order.add(customInside);

			// Combobox for setting the edge appearance from inside/outside appearance
			JLabel edgesText = new JLabel(trans.get("AppearanceCfg.lbl.AppearanceEdges"));
			add(edgesText);
			String[] options = new String[] {trans.get(tr_outside), trans.get(tr_inside)};
			JComboBox<String> edgesComboBox = new JComboBox<>(options);
			if (handler.isEdgesSameAsInside()) {
				edgesComboBox.setSelectedItem(trans.get(tr_inside));
			}
			else {
				edgesComboBox.setSelectedItem(trans.get(tr_outside));
			}
			add(edgesComboBox, "growx, left, wrap");
			order.add(edgesComboBox);
			edgesText.setToolTipText(trans.get("AppearanceCfg.lbl.ttip.AppearanceEdges"));
			edgesComboBox.setToolTipText(trans.get("AppearanceCfg.lbl.ttip.AppearanceEdges"));

			outsideInsidePane = new JTabbedPane();
			JPanel outsidePanel = new JPanel(new MigLayout("fill", "[150][grow][150][grow]"));
			JPanel insidePanel = new JPanel(new MigLayout("fill", "[150][grow][150][grow]"));

			appearanceSection(document, c, false, outsidePanel);
			appearanceSection(document, c, true, insidePanel);

			outsideInsidePane.addTab(trans.get(tr_outside), null, outsidePanel,
					"Outside Tool Tip");
			outsideInsidePane.addTab(trans.get(tr_inside), null, insidePanel,
					"Inside Tool Tip");
			add(outsideInsidePane, "span 4, growx, wrap");

			// Show the outside/inside tabbed display when customInside is selected
			customInside.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					handler.setSeparateInsideOutside(customInside.isSelected());
					edgesText.setEnabled(customInside.isSelected());
					edgesComboBox.setEnabled(customInside.isSelected());
					if (customInside.isSelected()) {
						remove(outsidePanel);
						MigLayout layout = (MigLayout) outsidePanel.getLayout();
						layout.setLayoutConstraints("fill");
						outsideInsidePane.insertTab(trans.get(tr_outside), null, outsidePanel,
								"Outside Tool Tip", 0);
						outsideInsidePane.setSelectedIndex(0);
						add(outsideInsidePane, "span 4, growx, wrap");
					}
					else {
						remove(outsideInsidePane);
						MigLayout layout = (MigLayout) outsidePanel.getLayout();
						layout.setLayoutConstraints("fill, ins 0");
						add(outsidePanel, "span 4, growx, wrap");
					}

					// Repaint to fit to the new size
					if (parent != null) {
						parent.pack();
					} else {
						updateUI();
					}

					if (e == null) return;	// When e == null, you just want an update of the UI components, not a component change
					c.fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
				}
			});

			// Change the edge appearance upon item selection
			edgesComboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (edgesComboBox.getSelectedItem() == null) return;
					if (edgesComboBox.getSelectedItem().equals(trans.get(tr_outside))) {
						handler.setEdgesSameAsInside(false);
					}
					else if (edgesComboBox.getSelectedItem().equals(trans.get(tr_inside))) {
						handler.setEdgesSameAsInside(true);
					}
					else {
						return;
					}
					if (e != null) {	// When e == null, you just want an update of the UI components, not a component change
						c.fireComponentChangeEvent(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
					}
				}
			});

			customInside.getActionListeners()[0].actionPerformed(null);
			edgesComboBox.getActionListeners()[0].actionPerformed(null);
		}
		else
			appearanceSection(document, c, false, this);
	}

	/**
	 *
	 * @param document
	 * @param c
	 * @param insideBuilder flag to check whether you are on the inside builder (true) or outside builder
	 * @param panel
	 */
	private void appearanceSection(OpenRocketDocument document, RocketComponent c,
								   boolean insideBuilder, JPanel panel) {
		AppearanceBuilder builder;
		BooleanModel mDefault;
		if (!insideBuilder) {
			builder = ab;
			mDefault = new BooleanModel(c.getAppearance() == null || defaultAppearance.equals(c.getAppearance()));
		} else if (c instanceof InsideColorComponent) {
			builder = insideAb;
			Appearance appearance = ((InsideColorComponent) c).getInsideColorComponentHandler().getInsideAppearance();
			mDefault = new BooleanModel(appearance == null || defaultAppearance.equals(appearance));
		} else return;

		DecalModel decalModel = new DecalModel(panel, document, builder);
		JComboBox<DecalImage> textureDropDown = new JComboBox<DecalImage>(decalModel);

		// We need to add this action listener that triggers a decalModel update when the same item is selected, because
		// for multi-comp edits, the listeners' decals may not be updated otherwise
		textureDropDown.addActionListener(new ActionListener() {
			private DecalImage previousSelection = (DecalImage) decalModel.getSelectedItem();

			@Override
			public void actionPerformed(ActionEvent e) {
				DecalImage decal = (DecalImage) textureDropDown.getSelectedItem();
				if (decal == previousSelection) {
					decalModel.setSelectedItem(decal);
				}
				previousSelection = decal;
			}
		});

		JButton colorButton = new SelectColorButton(new ColorIcon(builder.getPaint()));

		colorButton.addActionListener(new ColorActionListener(builder, "Paint"));

		// Texture Header Row
		panel.add(new StyledLabel(trans.get("AppearanceCfg.lbl.Appearance"),
				Style.BOLD));
		JCheckBox materialDefault = new JCheckBox(mDefault);
		materialDefault.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (materialDefault.isSelected()) {
					if (!insideBuilder) {
						previousUserSelectedAppearance = (builder == null) ? null
								: builder.getAppearance();
					} else {
						previousUserSelectedInsideAppearance = (builder == null) ? null
								: builder.getAppearance();
					}

					// Set the listeners' appearance to the default appearance
					for (RocketComponent listener : builder.getConfigListeners().keySet()) {
						builder.getConfigListeners().get(listener).setAppearance(defaultAppearance);
						listener.setAppearance(null);
					}

					// Set this component's appearance to the default appearance
					builder.setAppearance(defaultAppearance);
					c.setAppearance(null);
				} else {
					if (!insideBuilder) {
						// Set the listeners' appearance to the previous user selected appearance
						for (AppearanceBuilder listener : builder.getConfigListeners().values()) {
							listener.setAppearance(previousUserSelectedAppearance);
						}
						builder.setAppearance(previousUserSelectedAppearance);
					} else {
						// Set the listeners' inside appearance to the previous user selected appearance
						for (AppearanceBuilder listener : builder.getConfigListeners().values()) {
							listener.setAppearance(previousUserSelectedInsideAppearance);
						}
						builder.setAppearance(previousUserSelectedInsideAppearance);
					}
				}
			}
		});
		materialDefault.setText(trans.get("AppearanceCfg.lbl.Usedefault"));
		panel.add(materialDefault, "wrap");
		order.add(materialDefault);

		// Texture File
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.Texture")));
		JPanel p = new JPanel(new MigLayout("fill, ins 0", "[grow][]"));
		mDefault.addEnableComponent(textureDropDown, false);
		p.add(textureDropDown, "grow");
		panel.add(p, "spanx 3, growx, wrap");
		order.add(textureDropDown);
			
		//// Edit button
		if ((SystemInfo.getPlatform() != Platform.UNIX) || !SystemInfo.isConfined()) {
			JButton editBtn = new SelectColorButton(
					trans.get("AppearanceCfg.but.edit"));
			// Enable the editBtn only when the appearance builder has an Image
			// assigned to it.
			editBtn.setEnabled(!materialDefault.isSelected() && builder.getImage() != null);
			builder.addChangeListener(new StateChangeListener() {
				@Override
				public void stateChanged(EventObject e) {
					editBtn.setEnabled(!materialDefault.isSelected() && builder.getImage() != null);
				}
			});
			materialDefault.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					editBtn.setEnabled(!materialDefault.isSelected() && builder.getImage() != null);
				}
			});

			editBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						DecalImage newImage = editDecalHelper.editDecal(
								SwingUtilities
										.getWindowAncestor(panel),
								document, c, builder.getImage(), insideBuilder);
						builder.setImage(newImage);
					} catch (EditDecalHelperException ex) {
						JOptionPane.showMessageDialog(panel,
								ex.getMessage(), "", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			p.add(editBtn);
		}

		// TODO: move the separate columns in two separate panels instead of adding them in a zig-zag way
		// Color
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.color.Color")));
		mDefault.addEnableComponent(colorButton, false);
		panel.add(colorButton);
		order.add(colorButton);

		// Scale
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.texture.scale")), "gapleft para");

		panel.add(new JLabel("x:"), "split 4");
		JSpinner scaleU = new JSpinner(new DoubleModel(builder, "ScaleX",
				TEXTURE_UNIT).getSpinnerModel());
		scaleU.setEditor(new SpinnerEditor(scaleU));
		mDefault.addEnableComponent(scaleU, false);
		panel.add(scaleU, "w 50lp");
		order.add(((SpinnerEditor) scaleU.getEditor()).getTextField());

		panel.add(new JLabel("y:"));
		JSpinner scaleV = new JSpinner(new DoubleModel(builder, "ScaleY",
				TEXTURE_UNIT).getSpinnerModel());
		scaleV.setEditor(new SpinnerEditor(scaleV));
		mDefault.addEnableComponent(scaleV, false);
		panel.add(scaleV, "wrap, w 50lp");
		order.add(((SpinnerEditor) scaleV.getEditor()).getTextField());

		// Shine
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.shine")));
		DoubleModel shineModel = new DoubleModel(builder, "Shine",
				UnitGroup.UNITS_RELATIVE, 0, 1);
		// Set the initial value to the reset state, not the shine value of the default appearance of this component
		if (mDefault.getValue() && previousUserSelectedAppearance != null)
			shineModel.setValue(previousUserSelectedAppearance.getShine());
		final JSpinner spinShine = new JSpinner(shineModel.getSpinnerModel());
		spinShine.setEditor(new SpinnerEditor(spinShine));
		final BasicSlider slideShine = new BasicSlider(shineModel.getSliderModel(0, 1));
		final UnitSelector unitShine = new UnitSelector(shineModel);

		mDefault.addEnableComponent(slideShine, false);
		mDefault.addEnableComponent(spinShine, false);
		mDefault.addEnableComponent(unitShine, false);

		panel.add(spinShine, "split 3, w 60");
		panel.add(unitShine);
		panel.add(slideShine, "w 100lp");
		order.add(order.indexOf(colorButton) + 1, ((SpinnerEditor) spinShine.getEditor()).getTextField());

		// Offset
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.texture.offset")), "gapleft para");

		panel.add(new JLabel("x:"), "split 4");
		JSpinner offsetU = new JSpinner(new DoubleModel(builder, "OffsetU",
				TEXTURE_UNIT).getSpinnerModel());
		offsetU.setEditor(new SpinnerEditor(offsetU));
		mDefault.addEnableComponent(offsetU, false);
		panel.add(offsetU, "w 50lp");
		order.add(((SpinnerEditor) offsetU.getEditor()).getTextField());

		panel.add(new JLabel("y:"));
		JSpinner offsetV = new JSpinner(new DoubleModel(builder, "OffsetV",
				TEXTURE_UNIT).getSpinnerModel());
		offsetV.setEditor(new SpinnerEditor(offsetV));
		mDefault.addEnableComponent(offsetV, false);
		panel.add(offsetV, "wrap, w 50lp");
		order.add(((SpinnerEditor) offsetV.getEditor()).getTextField());

		// Opacity
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.opacity")));
		DoubleModel opacityModel = new DoubleModel(builder, "Opacity",
				UnitGroup.UNITS_RELATIVE, 0, 1);
		JSpinner spinOpacity = new JSpinner(opacityModel.getSpinnerModel());
		spinOpacity.setEditor(new SpinnerEditor(spinOpacity));
		BasicSlider slideOpacity = new BasicSlider(opacityModel.getSliderModel(0, 1));
		UnitSelector unitOpacity = new UnitSelector(opacityModel);

		mDefault.addEnableComponent(slideOpacity, false);
		mDefault.addEnableComponent(spinOpacity, false);
		mDefault.addEnableComponent(unitOpacity, false);

		panel.add(spinOpacity, "split 3, w 60");
		panel.add(unitOpacity);
		panel.add(slideOpacity, "w 100lp");
		order.add(order.indexOf(((SpinnerEditor) spinShine.getEditor()).getTextField()) + 1,
				((SpinnerEditor) spinOpacity.getEditor()).getTextField());

		// Rotation
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.texture.rotation")), "gapleft para");
		DoubleModel rotationModel = new DoubleModel(builder, "Rotation",
				UnitGroup.UNITS_ANGLE);
		JSpinner rotation = new JSpinner(rotationModel.getSpinnerModel());
		rotation.setEditor(new SpinnerEditor(rotation));
		mDefault.addEnableComponent(rotation, false);
		panel.add(rotation, "split 3, w 50");
		order.add(((SpinnerEditor) rotation.getEditor()).getTextField());
		panel.add(new UnitSelector(rotationModel));
		BasicSlider bs = new BasicSlider(rotationModel.getSliderModel(
				-Math.PI, Math.PI));
		mDefault.addEnableComponent(bs, false);
		panel.add(bs, "w 100lp, wrap");

		// Repeat
		panel.add(new JLabel(trans.get("AppearanceCfg.lbl.texture.repeat")), "skip 2, gapleft para");
		EdgeMode[] list = new EdgeMode[EdgeMode.values().length];
		System.arraycopy(EdgeMode.values(), 0, list, 0,
				EdgeMode.values().length);
		JComboBox<EdgeMode> combo = new JComboBox<EdgeMode>(new EnumModel<EdgeMode>(builder,
				"EdgeMode", list));
		mDefault.addEnableComponent(combo, false);
		panel.add(combo, "wrap");
		order.add(combo);

		builder.addChangeListener(new StateChangeListener() {
			double lastOpacity = builder.getOpacity();
			@Override
			public void stateChanged(EventObject e) {
				colorButton.setIcon(new ColorIcon(builder.getPaint()));
				if (lastOpacity != builder.getOpacity()) {
					opacityModel.stateChanged(null);
					lastOpacity = builder.getOpacity();
				}
				if (!insideBuilder) {
					// Set the listeners' outside appearance
					for (RocketComponent listener : builder.getConfigListeners().keySet()) {
						listener.setAppearance(builder.getConfigListeners().get(listener).getAppearance());
					}
					// Set this component's outside appearance
					c.setAppearance(builder.getAppearance());
				}
				else {
					// Set the listeners' inside appearance
					for (RocketComponent listener : builder.getConfigListeners().keySet()) {
						if (!(listener instanceof InsideColorComponent)) continue;
						((InsideColorComponent) listener).getInsideColorComponentHandler()
								.setInsideAppearance(builder.getConfigListeners().get(listener).getAppearance());
					}
					// Set this component's inside appearance
					((InsideColorComponent) c).getInsideColorComponentHandler().setInsideAppearance(builder.getAppearance());
				}
				decalModel.refresh();
			}
		});
	}
}
