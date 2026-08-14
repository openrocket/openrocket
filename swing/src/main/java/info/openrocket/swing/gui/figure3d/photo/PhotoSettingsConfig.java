package info.openrocket.swing.gui.figure3d.photo;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.swing.gui.adaptors.BooleanModel;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.ColorChooserButton;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.components.StyledLabel.Style;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky;
import info.openrocket.swing.gui.figure3d.photo.sky.Sky.Credit;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Lake;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Meadow;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Miramar;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Mountains;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Orbit;
import info.openrocket.swing.gui.figure3d.photo.sky.builtin.Storm;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.ColorConversion;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.Invalidatable;
import info.openrocket.core.util.StateChangeListener;

@SuppressWarnings("serial")
public class PhotoSettingsConfig extends JTabbedPane {
	private final Translator trans = Application.getTranslator();
	private final PhotoSettings settings;
	private final OpenRocketDocument document;
	private final List<Invalidatable> models = new ArrayList<>();
	private final AtomicBoolean disposed = new AtomicBoolean(false);
	private final StateChangeListener colorChangeListener;
	private StateChangeListener motorEffectsSettingsListener;
	private StateChangeListener rocketChangeListener;

	public PhotoSettingsConfig(PhotoSettings p, OpenRocketDocument document) {
		super();
		this.settings = p;
		this.document = document;

		setPreferredSize(new Dimension(240, 320));

		final ColorChooserButton sunLightColorButton = createColorButton(p::getSunlight, p::setSunlight);
		sunLightColorButton.setMaximumSize(new Dimension(35, 25));

		final ColorChooserButton skyColorButton = createColorButton(p::getSkyColor, p::setSkyColor);
		skyColorButton.setMaximumSize(new Dimension(35, 25));

		final ColorChooserButton gradTopColorButton = createColorButton(p::getGradientTopColor, p::setGradientTopColor);
		gradTopColorButton.setMaximumSize(new Dimension(35, 25));

		final ColorChooserButton gradBottomColorButton = createColorButton(p::getGradientBottomColor, p::setGradientBottomColor);
		gradBottomColorButton.setMaximumSize(new Dimension(35, 25));

		final ColorChooserButton smokeColorButton = createColorButton(p::getSmokeColor, p::setSmokeColor);
		smokeColorButton.setMaximumSize(new Dimension(35, 25));

		final ColorChooserButton flameColorButton = createColorButton(p::getFlameColor, p::setFlameColor);
		flameColorButton.setMaximumSize(new Dimension(35, 25));

		colorChangeListener = new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				sunLightColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getSunlight()));
				skyColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getSkyColor()));
				gradTopColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getGradientTopColor()));
				gradBottomColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getGradientBottomColor()));
				smokeColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getSmokeColor()));
				flameColorButton.setSelectedColor(ColorConversion.toAwtColor(p.getFlameColor()));
			}
		};
		colorChangeListener.stateChanged(null);
		p.addChangeListener(colorChangeListener);

		addTab(trans.get("PhotoSettingsConfig.tab.orientation"), new JPanel(new MigLayout("fill", "[][][]")) {
			{
				// Rocket
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.rocket"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Pitch
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.pitch")));
				DoubleModel pitchModel = track(new DoubleModel(p, "Pitch", UnitGroup.UNITS_ANGLE));
				add(new EditableSpinner(pitchModel.getSpinnerModel()), "growx");
				add(new UnitSelector(pitchModel), "growx");
				add(photoSlider(pitchModel.getSliderModel(0, 2 * Math.PI)), "pushx, left, wrap");

				/// Yaw
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.yaw")));
				DoubleModel yawModel = track(new DoubleModel(p, "Yaw", UnitGroup.UNITS_ANGLE));
				add(new EditableSpinner(yawModel.getSpinnerModel()), "growx");
				add(new UnitSelector(yawModel), "growx");
				add(photoSlider(yawModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// Roll
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.roll")));
				DoubleModel rollModel = track(new DoubleModel(p, "Roll", UnitGroup.UNITS_ANGLE));
				add(new EditableSpinner(rollModel.getSpinnerModel()), "growx");
				add(new UnitSelector(rollModel), "growx");
				add(photoSlider(rollModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// Advance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.advance")));
				DoubleModel advanceModel = track(new DoubleModel(p, "Advance", UnitGroup.UNITS_LENGTH));
				add(new EditableSpinner(advanceModel.getSpinnerModel()), "growx");
				add(new UnitSelector(advanceModel), "growx");
				add(photoSlider(advanceModel.getSliderModel(-document.getRocket().getLength(), document.getRocket().getLength())), "wrap");

				// Camera
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.camera"), Style.BOLD), "split, gapright para, span");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// View azimuth
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vAz")));
				DoubleModel viewAzModel = track(new DoubleModel(p, "ViewAz", UnitGroup.UNITS_ANGLE));
				add(new EditableSpinner(viewAzModel.getSpinnerModel()), "growx");
				add(new UnitSelector(viewAzModel), "growx");
				add(photoSlider(viewAzModel.getSliderModel(0, 2 * Math.PI)), "wrap");

				/// View altitude
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vAlt")));
				DoubleModel viewAltModle = track(
						new DoubleModel(p, "ViewAlt", UnitGroup.UNITS_ANGLE, -Math.PI, Math.PI));
				add(new EditableSpinner(viewAltModle.getSpinnerModel()), "growx");
				add(new UnitSelector(viewAltModle), "growx");
				add(photoSlider(viewAltModle.getSliderModel()), "wrap");

				/// View distance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.vDist")));
				DoubleModel viewDistanceModel = track(new DoubleModel(p, "ViewDistance", UnitGroup.UNITS_LENGTH));
				add(new EditableSpinner(viewDistanceModel.getSpinnerModel()), "growx");
				add(new UnitSelector(viewDistanceModel), "growx");
				add(photoSlider(viewDistanceModel.getSliderModel(0, 2 * document.getRocket().getLength())), "wrap");

				/// FoV
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.fov")));
				DoubleModel fovModel = track(new DoubleModel(p, "Fov", UnitGroup.UNITS_ANGLE,
						Math.PI * 10 / 180, Math.PI * 160 / 180));
				add(new EditableSpinner(fovModel.getSpinnerModel()), "growx");
				add(new UnitSelector(fovModel), "growx");
				add(photoSlider(fovModel.getSliderModel()), "wrap");
			}
		});

		addTab(trans.get("PhotoSettingsConfig.tab.environment"), new JPanel(new MigLayout("fill", "[][][]")) {
			{
				// Light
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.light"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Sun light
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.sun")));
				add(sunLightColorButton, "wrap");

				/// Light strength
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.lightStrength")));
				DoubleModel lightStrengthModel = track(
						new DoubleModel(p, "LightStrength", UnitGroup.UNITS_RELATIVE, 0, 2));
				add(new EditableSpinner(lightStrengthModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(lightStrengthModel));
				add(photoSlider(lightStrengthModel.getSliderModel(0, 2)), "pushx, left, wrap");

				/// Ambiance
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.amb")));
				DoubleModel ambianceModel = track(
						new DoubleModel(p, "Ambiance", UnitGroup.UNITS_RELATIVE, 0, 1));
				add(new EditableSpinner(ambianceModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(ambianceModel));
				add(photoSlider(ambianceModel.getSliderModel(0, 1)), "pushx, left, wrap");

				/// Light azimuth
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.lightAz")));
				DoubleModel lightAzModel = track(new DoubleModel(p, "LightAz", UnitGroup.UNITS_ANGLE));
				add(new EditableSpinner(lightAzModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(lightAzModel));
				add(photoSlider(lightAzModel.getSliderModel(-Math.PI, Math.PI)), "wrap");

				/// Light altitude
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.lightAlt")));
				DoubleModel lightAltModel = track(new DoubleModel(p, "LightAlt", UnitGroup.UNITS_ANGLE,
						-Math.PI / 2, Math.PI / 2));
				add(new EditableSpinner(lightAltModel.getSpinnerModel()), "growx, split 2");
				add(new UnitSelector(lightAltModel));
				add(photoSlider(lightAltModel.getSliderModel(-Math.PI / 2, Math.PI / 2)), "wrap");

				// Background
				add(new StyledLabel(trans.get("PhotoSettingsConfig.lbl.background"), Style.BOLD), "split, span, gapright para");
				add(new JSeparator(SwingConstants.HORIZONTAL), "wrap, growx");

				/// Background type combobox
				add(new JLabel(trans.get("PhotoSettingsConfig.lbl.backgroundType")));
				String[] bgTypeLabels = {
						trans.get("PhotoSettingsConfig.backgroundType.solidColor"),
						trans.get("PhotoSettingsConfig.backgroundType.gradient"),
						trans.get("PhotoSettingsConfig.backgroundType.texture")
				};
				JComboBox<String> bgTypeCombo = new JComboBox<>(bgTypeLabels);
				add(bgTypeCombo, "spanx, wrap");

				/// Card panel
				CardLayout cardLayout = new CardLayout();
				JPanel cards = new JPanel(cardLayout);

				// -- Solid color card --
				JPanel solidCard = new JPanel(new MigLayout("fill, insets 0", "[][][]"));
				solidCard.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.skyColor")));
				solidCard.add(skyColorButton, "wrap");
				solidCard.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.skyColorOpacity")));
				DoubleModel skyColorOpacityModel = track(
						new DoubleModel(p, "SkyColorOpacity", UnitGroup.UNITS_RELATIVE, 0, 1));
				EditableSpinner skyColorOpacitySpinner = new EditableSpinner(skyColorOpacityModel.getSpinnerModel());
				solidCard.add(skyColorOpacitySpinner, "growx, split 2");
				UnitSelector skyColorOpacityUnitSelector = new UnitSelector(skyColorOpacityModel);
				solidCard.add(skyColorOpacityUnitSelector);
				BasicSlider skyColorOpacitySlider = photoSlider(skyColorOpacityModel.getSliderModel());
				solidCard.add(skyColorOpacitySlider, "wrap");
				// -- Gradient card --
				JPanel gradientCard = new JPanel(new MigLayout("fill, insets 0", "[][][]"));
				gradientCard.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.gradientTopColor")));
				gradientCard.add(gradTopColorButton, "wrap");
				gradientCard.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.gradientBottomColor")));
				gradientCard.add(gradBottomColorButton, "wrap");

				// -- Texture card --
				JPanel textureCard = new JPanel(new MigLayout("fill, insets 0", "[][][]"));
				textureCard.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.skyImage")));
				JComboBox<Sky> skyCombo = new JComboBox<>(new DefaultComboBoxModel<>(
						new Sky[]{Mountains.instance, Meadow.instance, Storm.instance,
								Lake.instance, Orbit.instance, Miramar.instance}));
				textureCard.add(skyCombo, "spanx, wrap");

				final JLabel creditLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.skyCredit"));
				textureCard.add(creditLabel);
				final JTextArea credit = new JTextArea();
				credit.setEditable(false);
				credit.setCursor(null);
				credit.setOpaque(false);
				credit.setFocusable(false);
				credit.setFont(creditLabel.getFont());
				textureCard.add(credit, "spanx");

				skyCombo.addActionListener(e -> {
					Sky selected = (Sky) skyCombo.getSelectedItem();
					p.setSky(selected);
					if (selected instanceof Sky.Credit) {
						credit.setText(((Credit) selected).getCredit());
					} else {
						credit.setText("");
					}
				});
				// Initialize sky combo and credit
				if (p.getSky() != null) {
					skyCombo.setSelectedItem(p.getSky());
				} else {
					skyCombo.setSelectedIndex(0);
					p.setSky((Sky) skyCombo.getSelectedItem());
				}
				if (p.getSky() instanceof Sky.Credit) {
					credit.setText(((Credit) p.getSky()).getCredit());
				}

				cards.add(solidCard, PhotoSettings.BackgroundType.SOLID_COLOR.name());
				cards.add(gradientCard, PhotoSettings.BackgroundType.GRADIENT.name());
				cards.add(textureCard, PhotoSettings.BackgroundType.TEXTURE.name());
				add(cards, "spanx, wrap");

				// Initialize combobox selection from current setting
				bgTypeCombo.setSelectedIndex(p.getBackgroundType().ordinal());
				cardLayout.show(cards, p.getBackgroundType().name());

				bgTypeCombo.addActionListener(e -> {
					PhotoSettings.BackgroundType type = PhotoSettings.BackgroundType.values()[bgTypeCombo.getSelectedIndex()];
					p.setBackgroundType(type);
					cardLayout.show(cards, type.name());
				});

			}
		});

		addTab(trans.get("PhotoSettingsConfig.tab.effects"), createVerticalScrollPane(new JPanel(
				new MigLayout("fillx, insets dialog", "[grow,fill]")) {
			{
				JPanel particleEffectsPanel = new JPanel(new MigLayout(
						"fillx, insets 0, hidemode 3", "[][grow,fill][][grow,fill]"));

				StyledLabel noMotorsInfo = new StyledLabel(
						trans.get("PhotoSettingsConfig.lbl.effectsDisabledNoMotors"), -1, Style.ITALIC);
				noMotorsInfo.setFontColor(GUIUtil.getUITheme().getInformationColor());
				particleEffectsPanel.add(noMotorsInfo, "spanx, growx, wrap para");

				// Exhaust scale applies to smoke, flame, and sparks, so keep it above them.
				JLabel exhaustScaleLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.exhaustScale"));
				particleEffectsPanel.add(exhaustScaleLabel);
				DoubleModel exhaustScaleModel = track(
						new DoubleModel(p, "ExhaustScale", 100, UnitGroup.UNITS_NONE, 0, 1000));
				EditableSpinner exhaustScaleSpinner = new EditableSpinner(exhaustScaleModel.getSpinnerModel());
				BasicSlider exhaustScaleSlider = photoSlider(exhaustScaleModel.getSliderModel(0, 1000));
				particleEffectsPanel.add(exhaustScaleSpinner, "growx");
				particleEffectsPanel.add(exhaustScaleSlider, "skip 1, wrap");
				particleEffectsPanel.add(new JSeparator(SwingConstants.HORIZONTAL),
						"spanx, growx, wrap, gaptop 4, gapbottom 4");

				/// Smoke
				JPanel smokePanel = createTitledControlPanel(trans.get("PhotoSettingsConfig.lbl.smoke"));
				JLabel smokeLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.enabled"));
				smokePanel.add(smokeLabel);
				BooleanModel smokeModel = track(new BooleanModel(p, "Smoke"));
				JCheckBox smokeCheck = new JCheckBox(smokeModel);
				smokePanel.add(smokeCheck);

				smokePanel.add(smokeColorButton, "gapleft unrel");
				smokeModel.addEnableComponent(smokeColorButton);

				/// Smoke opacity
				JLabel smokeOpacityLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.smokeOpacity"));
				DoubleModel smokeOpacityModel = track(
						new DoubleModel(p, "SmokeOpacity", UnitGroup.UNITS_RELATIVE, 0, 1));
				EditableSpinner opacitySpinner = new EditableSpinner(smokeOpacityModel.getSpinnerModel());
				UnitSelector opacitySelector = new UnitSelector(smokeOpacityModel);
				BasicSlider opacitySlider = photoSlider(smokeOpacityModel.getSliderModel(0, 1));
				smokePanel.add(smokeOpacityLabel, "gapleft unrel");
				smokePanel.add(opacitySpinner, "growx");
				smokePanel.add(opacitySelector);
				smokePanel.add(opacitySlider);
				smokeModel.addEnableComponent(opacitySpinner);
				smokeModel.addEnableComponent(opacitySelector);
				smokeModel.addEnableComponent(opacitySlider);
				particleEffectsPanel.add(smokePanel, "spanx, growx, wrap");

				/// Flame
				JPanel flamePanel = createTitledControlPanel(trans.get("PhotoSettingsConfig.lbl.flame"));
				JLabel flameLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.enabled"));
				flamePanel.add(flameLabel);
				BooleanModel fireModel = track(new BooleanModel(p, "Flame"));
				JCheckBox flameCheck = new JCheckBox(fireModel);
				flamePanel.add(flameCheck);

				flamePanel.add(flameColorButton, "gapleft unrel");
				fireModel.addEnableComponent(flameColorButton);

				/// Flame aspect ratio
				JLabel flameAspectLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.flameAspect"));
				DoubleModel flameAspectModel = track(new DoubleModel(p, "FlameAspectRatio", 100,
						UnitGroup.UNITS_NONE, 25, 250));
				EditableSpinner flameAspectSpinner = new EditableSpinner(flameAspectModel.getSpinnerModel());
				BasicSlider flameAspectSlider = photoSlider(flameAspectModel.getSliderModel(25, 250));
				flamePanel.add(flameAspectLabel, "gapleft unrel");
				flamePanel.add(flameAspectSpinner, "growx");
				flamePanel.add(flameAspectSlider, "skip 1, wrap");
				fireModel.addEnableComponent(flameAspectSpinner);
				fireModel.addEnableComponent(flameAspectSlider);
				particleEffectsPanel.add(flamePanel, "spanx, growx, wrap");

				/// Sparks
				JPanel sparksPanel = createTitledControlPanel(trans.get("PhotoSettingsConfig.lbl.sparks"));
				JLabel sparksLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.enabled"));
				sparksPanel.add(sparksLabel);
				BooleanModel sparksModel = track(new BooleanModel(p, "Sparks"));
				JCheckBox sparksCheck = new JCheckBox(sparksModel);
				sparksPanel.add(sparksCheck, "wrap");
				fireModel.addEnableComponent(sparksCheck);

				/// Sparks concentration
				JLabel sparkConcentrationLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.sparkConcentration"));
				DoubleModel sparkConcentrationModel = track(new DoubleModel(p, "SparkConcentration",
						UnitGroup.UNITS_RELATIVE, 0, 1));
				EditableSpinner sparkConcentrationSpinner = new EditableSpinner(sparkConcentrationModel.getSpinnerModel());
				UnitSelector sparkConcentrationSelector = new UnitSelector(sparkConcentrationModel);
				BasicSlider sparkConcentrationSlider = photoSlider(sparkConcentrationModel.getSliderModel(0, 1));
				sparksPanel.add(sparkConcentrationLabel);
				sparksPanel.add(sparkConcentrationSpinner, "growx");
				sparksPanel.add(sparkConcentrationSelector);
				sparksPanel.add(sparkConcentrationSlider, "wrap");
				sparksModel.addEnableComponent(sparkConcentrationSpinner);
				sparksModel.addEnableComponent(sparkConcentrationSelector);
				sparksModel.addEnableComponent(sparkConcentrationSlider);

				/// Spark weight
				JLabel sparkWeightLabel = new JLabel(trans.get("PhotoSettingsConfig.lbl.sparkWeight"));
				DoubleModel sparkWeightModel = track(
						new DoubleModel(p, "SparkWeight", UnitGroup.UNITS_RELATIVE, 0, 1));
				EditableSpinner sparkWeightSpinner = new EditableSpinner(sparkWeightModel.getSpinnerModel());
				UnitSelector sparkWeightSelector = new UnitSelector(sparkWeightModel);
				BasicSlider sparkWeightSlider = photoSlider(sparkWeightModel.getSliderModel(0, 1));
				sparksPanel.add(sparkWeightLabel);
				sparksPanel.add(sparkWeightSpinner, "growx");
				sparksPanel.add(sparkWeightSelector);
				sparksPanel.add(sparkWeightSlider, "wrap");
				sparksModel.addEnableComponent(sparkWeightSpinner);
				sparksModel.addEnableComponent(sparkWeightSelector);
				sparksModel.addEnableComponent(sparkWeightSlider);
				particleEffectsPanel.add(sparksPanel, "spanx, growx, wrap");

				add(particleEffectsPanel, "growx, wrap para");

				JPanel motionBlurPanel = createTitledControlPanel(
						trans.get("PhotoSettingsConfig.lbl.motionBlur"));

				/// Motion blur enabled
				motionBlurPanel.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.enabled")));
				BooleanModel speedModel = track(new BooleanModel(p, "MotionBlurred"));
				motionBlurPanel.add(new JCheckBox(speedModel), "wrap");

				/// Motion blur amount
				motionBlurPanel.add(new JLabel(trans.get("PhotoSettingsConfig.lbl.motionBlurAmount")));
				DoubleModel motionBlurAmountModel = track(
						new DoubleModel(p, "MotionBlurAmount", UnitGroup.UNITS_NONE, 0, 20));
				EditableSpinner motionBlurAmountSpinner = new EditableSpinner(motionBlurAmountModel.getSpinnerModel());
				BasicSlider motionBlurAmountSlider = photoSlider(motionBlurAmountModel.getSliderModel(0, 20));
				motionBlurPanel.add(motionBlurAmountSpinner, "growx");
				motionBlurPanel.add(motionBlurAmountSlider, "skip 1, wrap");
				speedModel.addEnableComponent(motionBlurAmountSpinner);
				speedModel.addEnableComponent(motionBlurAmountSlider);
				add(motionBlurPanel, "growx, wrap");

				Runnable refreshMotorEffectsAvailability = () -> updateMotorEffectsAvailability(document, p,
						noMotorsInfo,
						smokePanel, smokeLabel, smokeCheck, smokeColorButton, smokeOpacityLabel, opacitySpinner, opacitySelector,
						opacitySlider,
						flamePanel, flameLabel, flameCheck, flameColorButton, flameAspectLabel, flameAspectSpinner, flameAspectSlider,
						sparksPanel, sparksLabel, sparksCheck, sparkConcentrationLabel, sparkConcentrationSpinner,
						sparkConcentrationSelector,
						sparkConcentrationSlider, sparkWeightLabel, sparkWeightSpinner, sparkWeightSelector, sparkWeightSlider,
						exhaustScaleLabel, exhaustScaleSpinner, exhaustScaleSlider);
				motorEffectsSettingsListener = e -> refreshMotorEffectsAvailability.run();
				rocketChangeListener = e -> SwingUtilities.invokeLater(() -> {
					if (!disposed.get()) {
						refreshMotorEffectsAvailability.run();
					}
				});
				p.addChangeListener(motorEffectsSettingsListener);
				document.getRocket().addChangeListener(rocketChangeListener);
				refreshMotorEffectsAvailability.run();
			}
		}));

	}

	private <T extends Invalidatable> T track(T model) {
		models.add(model);
		return model;
	}

	/** Releases listeners and adapter models owned by this settings pane. */
	public void dispose() {
		if (!disposed.compareAndSet(false, true)) {
			return;
		}
		settings.removeChangeListener(colorChangeListener);
		if (motorEffectsSettingsListener != null) {
			settings.removeChangeListener(motorEffectsSettingsListener);
		}
		if (rocketChangeListener != null) {
			document.getRocket().removeChangeListener(rocketChangeListener);
		}
		for (Invalidatable model : models) {
			model.invalidateMe();
		}
		models.clear();
	}

	private void updateMotorEffectsAvailability(OpenRocketDocument document, PhotoSettings settings,
			StyledLabel noMotorsInfo,
			JComponent smokePanel, JComponent smokeLabel, JCheckBox smokeCheck, JComponent smokeColorButton,
			JComponent smokeOpacityLabel,
			JComponent opacitySpinner, JComponent opacitySelector, JComponent opacitySlider,
			JComponent flamePanel, JComponent flameLabel, JCheckBox flameCheck, JComponent flameColorButton,
			JComponent flameAspectLabel,
			JComponent flameAspectSpinner, JComponent flameAspectSlider,
			JComponent sparksPanel, JComponent sparksLabel, JCheckBox sparksCheck, JComponent sparkConcentrationLabel,
			JComponent sparkConcentrationSpinner, JComponent sparkConcentrationSelector, JComponent sparkConcentrationSlider,
			JComponent sparkWeightLabel, JComponent sparkWeightSpinner, JComponent sparkWeightSelector,
			JComponent sparkWeightSlider,
			JComponent exhaustScaleLabel, JComponent exhaustScaleSpinner, JComponent exhaustScaleSlider) {
		boolean motorsAvailable = hasMotorsInSelectedConfiguration(document);
		boolean smokeEnabled = motorsAvailable && settings.isSmoke();
		boolean flameEnabled = motorsAvailable && settings.isFlame();
		boolean sparksEnabled = motorsAvailable && settings.isSparks();
		boolean particleEffectsEnabled = smokeEnabled || flameEnabled || sparksEnabled;

		noMotorsInfo.setVisible(!motorsAvailable);

		smokePanel.setEnabled(motorsAvailable);
		smokeLabel.setEnabled(motorsAvailable);
		smokeCheck.setEnabled(motorsAvailable);
		smokeColorButton.setEnabled(smokeEnabled);
		smokeOpacityLabel.setEnabled(smokeEnabled);
		opacitySpinner.setEnabled(smokeEnabled);
		opacitySelector.setEnabled(smokeEnabled);
		opacitySlider.setEnabled(smokeEnabled);

		flamePanel.setEnabled(motorsAvailable);
		flameLabel.setEnabled(motorsAvailable);
		flameCheck.setEnabled(motorsAvailable);
		flameColorButton.setEnabled(flameEnabled);
		flameAspectLabel.setEnabled(flameEnabled);
		flameAspectSpinner.setEnabled(flameEnabled);
		flameAspectSlider.setEnabled(flameEnabled);

		sparksPanel.setEnabled(motorsAvailable);
		sparksLabel.setEnabled(motorsAvailable);
		sparksCheck.setEnabled(motorsAvailable);
		sparkConcentrationLabel.setEnabled(sparksEnabled);
		sparkConcentrationSpinner.setEnabled(sparksEnabled);
		sparkConcentrationSelector.setEnabled(sparksEnabled);
		sparkConcentrationSlider.setEnabled(sparksEnabled);
		sparkWeightLabel.setEnabled(sparksEnabled);
		sparkWeightSpinner.setEnabled(sparksEnabled);
		sparkWeightSelector.setEnabled(sparksEnabled);
		sparkWeightSlider.setEnabled(sparksEnabled);

		exhaustScaleLabel.setEnabled(particleEffectsEnabled);
		exhaustScaleSpinner.setEnabled(particleEffectsEnabled);
		exhaustScaleSlider.setEnabled(particleEffectsEnabled);
	}

	private boolean hasMotorsInSelectedConfiguration(OpenRocketDocument document) {
		return document != null
				&& document.getSelectedConfiguration() != null
				&& document.getSelectedConfiguration().hasMotors();
	}

	private JPanel createTitledControlPanel(String title) {
		JPanel panel = new JPanel(new MigLayout(
				"fillx, insets 6", "[][grow,fill][][grow,fill]"));
		panel.setBorder(BorderFactory.createTitledBorder(title));
		return panel;
	}

	private JScrollPane createVerticalScrollPane(JPanel panel) {
		JScrollPane scrollPane = new JScrollPane(panel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		return scrollPane;
	}

	/**
	 * Creates a Photo Studio slider whose model changes flow through immediately.
	 * The render queue coalesces updates when a drag produces changes faster than
	 * frames can be drawn.
	 */
	private BasicSlider photoSlider(BoundedRangeModel model) {
		return new BasicSlider(model);
	}

	private ColorChooserButton createColorButton(Supplier<ORColor> getter, Consumer<ORColor> setter) {
		ColorChooserButton button = new ColorChooserButton(ColorConversion.toAwtColor(getter.get()));
		button.addColorPropertyChangeListener(e -> {
			Color color = button.getSelectedColor();
			if (color != null) {
				setter.accept(ColorConversion.fromAwtColor(color));
			}
		});
		return button;
	}
}
