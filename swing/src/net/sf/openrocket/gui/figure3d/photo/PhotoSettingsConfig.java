package net.sf.openrocket.gui.figure3d.photo;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;
import java.util.EventObject;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jogamp.opengl.GL2;
import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ColorIcon;
import net.sf.openrocket.gui.components.EditableSpinner;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.figure3d.TextureCache;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky;
import net.sf.openrocket.gui.figure3d.photo.sky.Sky.Credit;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Lake;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Meadow;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Miramar;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Mountains;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Orbit;
import net.sf.openrocket.gui.figure3d.photo.sky.builtin.Storm;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.StateChangeListener;
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class PhotoSettingsConfig extends JTabbedPane {
	private final Translator trans = Application.getTranslator();

	private static final JColorChooser colorChooser = new JColorChooser();

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
				final Method setMethod = o.getClass().getMethod("set" + valueName, net.sf.openrocket.util.Color.class);
				if (color == null)
					return;
				try {
					setMethod.invoke(o, ColorConversion.fromAwtColor(color));
				} catch (Throwable e1) {
					Application.getExceptionHandler().handleErrorCondition(e1);
				}
			} catch (Throwable e1) {
				Application.getExceptionHandler().handleErrorCondition(e1);
			}

		}


		@Override
		public void actionPerformed(ActionEvent colorClickEvent) {
			try {
				final Method getMethod = o.getClass().getMethod("get" + valueName);
				net.sf.openrocket.util.Color c = (net.sf.openrocket.util.Color) getMethod.invoke(o);
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

				JDialog d = JColorChooser.createDialog(PhotoSettingsConfig.this,
						trans.get("PhotoSettingsConfig.colorChooser.title"), true, colorChooser, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent okEvent) {
								changeComponentColor(colorChooser.getColor());
								// Unbind listener to avoid the current component's appearance to change with other components
								model.removeChangeListener(changeListener);
							}
						}, new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
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

	public PhotoSettingsConfig(PhotoSettings p, OpenRocketDocument document) {
		super();

		setPreferredSize(new Dimension(240, 320));

		final JButton sunLightColorButton = new SelectColorButton();
		sunLightColorButton.setMaximumSize(new Dimension(35, 25));

		final JButton skyColorButton = new SelectColorButton();
		skyColorButton.setMaximumSize(new Dimension(35, 25));

		final JButton smokeColorButton = new SelectColorButton();
		smokeColorButton.setMaximumSize(new Dimension(35, 25));

		final JButton flameColorButton = new SelectColorButton();
		flameColorButton.setMaximumSize(new Dimension(35, 25));

		p.addChangeListener(new StateChangeListener() {
			{
				stateChanged(null);
			}

			@Override
			public void stateChanged(EventObject e) {
				sunLightColorButton.setIcon(new ColorIcon(p.getSunlight()));
				skyColorButton.setIcon(new ColorIcon(p.getSkyColor()));
				smokeColorButton.setIcon(new ColorIcon(p.getSmokeColor()));
				flameColorButton.setIcon(new ColorIcon(p.getFlameColor()));
			}
		});
		sunLightColorButton.addActionListener(new ColorActionListener(p, "Sunlight"));
		skyColorButton.addActionListener(new ColorActionListener(p, "SkyColor"));
		smokeColorButton.addActionListener(new ColorActionListener(p, "SmokeColor"));
		flameColorButton.addActionListener(new ColorActionListener(p, "FlameColor"));

		addTab(trans.get("PhotoSettingsConfig.tab.orientation"), new JPanel(new MigLayout("fill", "[]100[]5[]")) {
			{
				// Rocket
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.rocket"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Pitch
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.pitch")));
				DoubleModel pitchModel = new DoubleModel(p, "Pitch", UnitGroup.UNITS_ANGLE);
				add(new EditableSpinner(pitchModel.getSpinnerModel()), "growx");
				add(new UnitSelector(pitchModel), "growx");
				add(new BasicSlider(pitchModel.getSliderModel(0, 2 * Math.PI)), "pushx, left, wrap");

				/// Yaw
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.yaw")));
				DoubleModel yawModel = new DoubleModel(p, "Yaw", UnitGroup.UNITS_ANGLE);
				add(new EditableSpinner(yawModel.getSpinnerModel()), "growx");
				add(new UnitSelector(yawModel), "growx");
				add(new BasicSlider(yawModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// Roll
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.roll")));
				DoubleModel rollModel = new DoubleModel(p, "Roll", UnitGroup.UNITS_ANGLE);
				add(new EditableSpinner(rollModel.getSpinnerModel()), "growx");
				add(new UnitSelector(rollModel), "growx");
				add(new BasicSlider(rollModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// Advance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.advance")));
				DoubleModel advanceModel = new DoubleModel(p, "Advance", UnitGroup.UNITS_LENGTH);
				add(new EditableSpinner(advanceModel.getSpinnerModel()), "growx");
				add(new UnitSelector(advanceModel), "growx");
				add(new BasicSlider(advanceModel.getSliderModel(-document.getRocket().getLength(), document.getRocket().getLength())), "wrap");

				// Camera
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.camera"), Style.BOLD), "split, gapright para, span");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// View azimuth
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vAz")));
				DoubleModel viewAzModel = new DoubleModel(p, "ViewAz", UnitGroup.UNITS_ANGLE);
				add(new EditableSpinner(viewAzModel.getSpinnerModel()), "growx");
				add(new UnitSelector(viewAzModel), "growx");
				add(new BasicSlider(viewAzModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// View altitude
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vAlt")));
				DoubleModel viewAltModle = new DoubleModel(p, "ViewAlt", UnitGroup.UNITS_ANGLE, -Math.PI / 2, Math.PI / 2);
				add(new EditableSpinner(viewAltModle.getSpinnerModel()), "growx");
				add(new UnitSelector(viewAltModle), "growx");
				add(new BasicSlider(viewAltModle.getSliderModel(-Math.PI / 2, Math.PI / 2)), "wrap");

				/// View distance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vDist")));
				DoubleModel viewDistanceModel = new DoubleModel(p, "ViewDistance", UnitGroup.UNITS_LENGTH);
				add(new EditableSpinner(viewDistanceModel.getSpinnerModel()), "growx");
				add(new UnitSelector(viewDistanceModel), "growx");
				add(new BasicSlider(viewDistanceModel.getSliderModel(0, 2 * document.getRocket().getLength())), "wrap");

				/// FoV
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.fov")));
				DoubleModel fovModel = new DoubleModel(p, "Fov", UnitGroup.UNITS_ANGLE, Math.PI * 57.3/180, Math.PI * 160/180);
				add(new EditableSpinner(fovModel.getSpinnerModel()), "growx");
				add(new UnitSelector(fovModel), "growx");
				add(new BasicSlider(fovModel.getSliderModel(Math.PI * 57.3/180, Math.PI * 160/180)), "wrap");
			}
		});

		addTab(trans.get("PhotoSettingsConfig.tab.environment"), new JPanel(new MigLayout("fill", "[]100[]5[]")) {
			{
				// Light
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.light"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Sun light
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.sun")));
				add(sunLightColorButton, "wrap");

				/// Ambiance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.amb")));
				DoubleModel ambianceModel = new DoubleModel(p, "Ambiance", UnitGroup.UNITS_RELATIVE, 0, 1);
				add(new EditableSpinner(ambianceModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(ambianceModel));
				add(new BasicSlider(ambianceModel.getSliderModel(0, 1)), "pushx, left, wrap");

				/// Light azimuth
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.lightAz")));
				DoubleModel lightAzModel = new DoubleModel(p, "LightAz", UnitGroup.UNITS_ANGLE);
				add(new EditableSpinner(lightAzModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(lightAzModel));
				add(new BasicSlider(lightAzModel.getSliderModel(-Math.PI, Math.PI)), "wrap");

				/// Light altitude
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.lightAlt")));
				DoubleModel lightAltModle = new DoubleModel(p, "LightAlt", UnitGroup.UNITS_ANGLE, -Math.PI / 2, Math.PI / 2);
				add(new EditableSpinner(lightAltModle.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(lightAltModle));
				add(new BasicSlider(lightAltModle.getSliderModel(-Math.PI / 2, Math.PI / 2)), "wrap");

				// Sky
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.sky"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Sky color
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.skyColor")));
				add(skyColorButton, "wrap");

				/// Sky image
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.skyImage")));

				Sky noSky = new Sky() {		// Dummy sky for 'none' selection option
					@Override
					public void draw(GL2 gl, TextureCache cache) { }

					@Override
					public String toString() {
						return trans.get("DecalModel.lbl.select");
					}
				};
				add(new JComboBox<Sky>(new DefaultComboBoxModel<Sky>(new Sky[] { noSky, Mountains.instance, Meadow.instance,
						Storm.instance, Lake.instance, Orbit.instance, Miramar.instance }) {
				}) {
					{
						addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								@SuppressWarnings("unchecked")
								Object s = ((JComboBox<Sky>) e.getSource()).getSelectedItem();
								if (s instanceof Sky && s != noSky) {
									p.setSky((Sky) s);
									skyColorButton.setEnabled(false);
								} else if (s == noSky) {
									p.setSky(null);
									skyColorButton.setEnabled(true);
								}
							}
						});

						if (p.getSky() != null) {
							setSelectedItem(p.getSky());
						}
						else {
							setSelectedItem(noSky);
						}
					}
				}, "spanx, wrap");

				/// Image credit
				final JLabel creditLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.skyCredit"));
				add(creditLabel);

				final JTextArea credit = new JTextArea();
				credit.setEditable(false);
				credit.setCursor(null);
				credit.setOpaque(false);
				credit.setFocusable(false);
				credit.setFont(creditLabel.getFont());
				add(credit, "spanx");

				final StateChangeListener skyChange = new StateChangeListener() {
					@Override
					public void stateChanged(EventObject e) {
						if (p.getSky() instanceof Sky.Credit) {
							credit.setText(((Credit) p.getSky()).getCredit());
						} else {
							credit.setText("");
						}
					}
				};
				p.addChangeListener(skyChange);

				skyChange.stateChanged(null);

			}
		});

		addTab(trans.get("PhotoSettingsConfig.tab.effects"), new JPanel(new MigLayout("fill", "[]100[]5[]")) {
			{
				// Smoke & Flame
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.smokeFlame"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Smoke
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.smoke")));
				BooleanModel smokeModel = new BooleanModel(p, "Smoke");
				add(new JCheckBox(smokeModel), "split 2, spanx");

				add(smokeColorButton, "wrap");
				smokeModel.addEnableComponent(smokeColorButton);

				/// Smoke opacity
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.smokeOpacity")));
				DoubleModel smokeOpacityModel = new DoubleModel(p, "SmokeOpacity", UnitGroup.UNITS_RELATIVE, 0, 1);
				EditableSpinner opacitySpinner = new EditableSpinner(smokeOpacityModel.getSpinnerModel());
				UnitSelector opacitySelector = new UnitSelector(smokeOpacityModel);
				BasicSlider opacitySlider = new BasicSlider(smokeOpacityModel.getSliderModel(0, 1));
				add(opacitySpinner, "growx");
				add(opacitySelector);
				add(opacitySlider, "wrap");
				smokeModel.addEnableComponent(opacitySpinner);
				smokeModel.addEnableComponent(opacitySelector);
				smokeModel.addEnableComponent(opacitySlider);

				/// Flame
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.flame")));
				BooleanModel fireModel = new BooleanModel(p, "Flame");
				add(new JCheckBox(fireModel), "split 2, spanx");

				add(flameColorButton, "wrap");
				fireModel.addEnableComponent(flameColorButton);

				/// Flame aspect ratio
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.flameAspect")));
				DoubleModel flameAspectModel = new DoubleModel(p, "FlameAspectRatio", 100, UnitGroup.UNITS_NONE, 25,
						250);
				EditableSpinner flameAspectSpinner = new EditableSpinner(flameAspectModel.getSpinnerModel());
				BasicSlider flameAspectSlider = new BasicSlider(flameAspectModel.getSliderModel(25, 250));
				add(flameAspectSpinner, "growx");
				add(flameAspectSlider, "skip 1, wrap");
				fireModel.addEnableComponent(flameAspectSpinner);
				fireModel.addEnableComponent(flameAspectSlider);

				/// Sparks
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.sparks")));
				BooleanModel sparksModel = new BooleanModel(p, "Sparks");
				JCheckBox sparksCheck = new JCheckBox(sparksModel);
				add(sparksCheck, "wrap");
				fireModel.addEnableComponent(sparksCheck);

				/// Sparks concentration
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.sparkConcentration")));
				DoubleModel sparkConcentrationModel = new DoubleModel(p, "SparkConcentration",
						UnitGroup.UNITS_RELATIVE, 0, 1);
				EditableSpinner sparkConcentrationSpinner = new EditableSpinner(sparkConcentrationModel.getSpinnerModel());
				UnitSelector sparkConcentrationSelector = new UnitSelector(sparkConcentrationModel);
				BasicSlider sparkConcentrationSlider = new BasicSlider(sparkConcentrationModel.getSliderModel(0, 1));
				add(sparkConcentrationSpinner, "growx");
				add(sparkConcentrationSelector);
				add(sparkConcentrationSlider, "wrap");
				sparksModel.addEnableComponent(sparkConcentrationSpinner);
				sparksModel.addEnableComponent(sparkConcentrationSelector);
				sparksModel.addEnableComponent(sparkConcentrationSlider);

				/// Spark weight
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.sparkWeight")));
				DoubleModel sparkWeightModel = new DoubleModel(p, "SparkWeight", UnitGroup.UNITS_RELATIVE, 0, 1);
				EditableSpinner sparkWeightSpinner = new EditableSpinner(sparkWeightModel.getSpinnerModel());
				UnitSelector sparkWeightSelector = new UnitSelector(sparkWeightModel);
				BasicSlider sparkWeightSlider = new BasicSlider(sparkWeightModel.getSliderModel(0, 1));
				add(sparkWeightSpinner, "growx");
				add(sparkWeightSelector);
				add(sparkWeightSlider, "wrap");
				sparksModel.addEnableComponent(sparkWeightSpinner);
				sparksModel.addEnableComponent(sparkWeightSelector);
				sparksModel.addEnableComponent(sparkWeightSlider);

				/// Exhaust scale
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.exhaustScale")));
				DoubleModel exhaustScaleModel = new DoubleModel(p, "ExhaustScale", 100, UnitGroup.UNITS_NONE, 0, 1000);
				add(new EditableSpinner(exhaustScaleModel.getSpinnerModel()), "growx");
				add(new BasicSlider(exhaustScaleModel.getSliderModel(0, 1000)), "skip 1, wrap");

				// Effects
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.effects"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Speed
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.speed")));
				add(new JCheckBox(new BooleanModel(p, "MotionBlurred")), "wrap");
			}
		});

	}
}
