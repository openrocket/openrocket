package net.sf.openrocket.gui.configdialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.BooleanModel;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.IntegerModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.ColorIcon;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.gui.util.ColorConversion;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.StateChangeListener;

public class AppearancePanel extends JPanel {
	private static final Translator trans = Application.getTranslator();

	private AppearanceBuilder ab;

	private static File lastImageDir = null;

	private class ColorActionListener implements ActionListener {
		private final String valueName;
		private final Object o;

		ColorActionListener(final Object o, final String valueName) {
			this.valueName = valueName;
			this.o = o;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				Method getMethod = o.getClass().getMethod("get" + valueName);
				Method setMethod = o.getClass().getMethod("set" + valueName, net.sf.openrocket.util.Color.class);
				net.sf.openrocket.util.Color c = (net.sf.openrocket.util.Color) getMethod.invoke(o);
				Color awtColor = ColorConversion.toAwtColor(c);
				awtColor = JColorChooser.showDialog( //
						AppearancePanel.this, //
						trans.get("RocketCompCfg.lbl.Choosecolor"), //
						awtColor);
				if (awtColor == null)
					return;
				setMethod.invoke(o, ColorConversion.fromAwtColor(awtColor));
			} catch (Throwable e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		}

	}

	public AppearancePanel(final RocketComponent c) {
		super(new MigLayout("fill", "[150][grow][150][grow]"));

		ab = new AppearanceBuilder(c.getAppearance());

		net.sf.openrocket.util.Color figureColor = c.getColor();
		if (figureColor == null) {
			figureColor = Application.getPreferences().getDefaultColor(c.getClass());
		}
		final JButton figureColorButton = new JButton(new ColorIcon(figureColor));
		final JButton diffuseColorButton = new JButton(new ColorIcon(ab.getDiffuse()));
		final JButton ambientColorButton = new JButton(new ColorIcon(ab.getAmbient()));
		final JButton specularColorButton = new JButton(new ColorIcon(ab.getSpecular()));

		final JTextField uriTextField = new JTextField(ab.getImage() != null ? ab.getImage().toString() : "");
		uriTextField.setEditable(false);

		ab.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				figureColorButton.setIcon(new ColorIcon(c.getColor()));
				diffuseColorButton.setIcon(new ColorIcon(ab.getDiffuse()));
				ambientColorButton.setIcon(new ColorIcon(ab.getAmbient()));
				specularColorButton.setIcon(new ColorIcon(ab.getSpecular()));
				c.setAppearance(ab.getAppearance());
				uriTextField.setText(ab.getImage() != null ? ab.getImage().toString() : "");
			}
		});

		c.addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				figureColorButton.setIcon(new ColorIcon(c.getColor()));
			}
		});

		figureColorButton.addActionListener(new ColorActionListener(c, "Color"));
		diffuseColorButton.addActionListener(new ColorActionListener(ab, "Diffuse"));
		ambientColorButton.addActionListener(new ColorActionListener(ab, "Ambient"));
		specularColorButton.addActionListener(new ColorActionListener(ab, "Specular"));

		BooleanModel mDefault = new BooleanModel(false);

		{// Style Header Row
			add(new StyledLabel("Figure Style", Style.BOLD));
			add(new JCheckBox(), "split 2");
			add(new JLabel("Use default"));
			add(new JButton("Set Default"), "span 2, align right, wrap");
		}

		{// Figure Color
			add(new JLabel("Figure Color:"));
			add(figureColorButton);
		}

		{// Line Style
			add(new JLabel("Line Style:"));
			add(new JLabel("[Default Style [v]]"), "wrap");
		}

		add(new JSeparator(SwingConstants.HORIZONTAL), "span, wrap, growx");

		{// Texture Header Row
			add(new StyledLabel("Appearance", Style.BOLD));
			add(new JCheckBox(mDefault), "split 2");
			add(new JLabel("Use default"));
			JButton setMDefault = new JButton("Set Default");
			mDefault.addEnableComponent(setMDefault, false);
			add(setMDefault, "span 2, align right, wrap");
		}

		{// Texture File
			JButton choose;
			add(new JLabel("Texture:"));
			JPanel p = new JPanel(new MigLayout("fill, ins 0", "[grow][]"));
			p.add(uriTextField, "grow");
			p.add(choose = new JButton("Choose"));
			mDefault.addEnableComponent(uriTextField, false);
			mDefault.addEnableComponent(choose, false);
			add(p, "span 3, growx, wrap");
			choose.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					URL u = ab.getImage();
					File current = lastImageDir;
					if (u != null && u.getProtocol().equals("file")) {
						try {
							current = new File(u.toURI()).getParentFile();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
					lastImageDir = current;

					JFileChooser fc = new JFileChooser(current);
					if (fc.showOpenDialog(AppearancePanel.this) == JFileChooser.APPROVE_OPTION) {
						try {
							ab.setImage(fc.getSelectedFile().toURI().toURL());
						} catch (MalformedURLException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
			});
		}

		{ // Diffuse
			add(new JLabel("Diffuse Color:"));
			mDefault.addEnableComponent(diffuseColorButton, false);
			add(diffuseColorButton);
		}

		{ // Scale
			add(new JLabel("Scale:"));

			add(new JLabel("x:"), "split 4");
			JSpinner scaleU = new JSpinner(new DoubleModel(ab, "ScaleU").getSpinnerModel());
			scaleU.setEditor(new SpinnerEditor(scaleU));
			mDefault.addEnableComponent(scaleU, false);
			add(scaleU, "w 40");

			add(new JLabel("y:"));
			JSpinner scaleV = new JSpinner(new DoubleModel(ab, "ScaleV").getSpinnerModel());
			scaleV.setEditor(new SpinnerEditor(scaleV));
			mDefault.addEnableComponent(scaleV, false);
			add(scaleV, "wrap, w 40");
		}

		{ // Ambient
			add(new JLabel("Ambient Color:"));
			mDefault.addEnableComponent(ambientColorButton, false);
			add(ambientColorButton);
		}

		{ // Offset
			add(new JLabel("Offset:"));

			add(new JLabel("x:"), "split 4");
			JSpinner offsetU = new JSpinner(new DoubleModel(ab, "OffsetU").getSpinnerModel());
			offsetU.setEditor(new SpinnerEditor(offsetU));
			mDefault.addEnableComponent(offsetU, false);
			add(offsetU, "w 40");

			add(new JLabel("y:"));
			JSpinner offsetV = new JSpinner(new DoubleModel(ab, "OffsetV").getSpinnerModel());
			offsetV.setEditor(new SpinnerEditor(offsetV));
			mDefault.addEnableComponent(offsetV, false);
			add(offsetV, "wrap, w 40");
		}

		{ // Specular
			add(new JLabel("Specular Color:"));
			mDefault.addEnableComponent(specularColorButton, false);
			add(specularColorButton);
		}

		{ // Center
			add(new JLabel("Center:"));

			add(new JLabel("x:"), "split 4");
			JSpinner centerU = new JSpinner(new DoubleModel(ab, "CenterU").getSpinnerModel());
			centerU.setEditor(new SpinnerEditor(centerU));
			mDefault.addEnableComponent(centerU, false);
			add(centerU, "w 40");

			add(new JLabel("y:"));
			JSpinner centerV = new JSpinner(new DoubleModel(ab, "CenterV").getSpinnerModel());
			centerV.setEditor(new SpinnerEditor(centerV));
			mDefault.addEnableComponent(centerV, false);
			add(centerV, "wrap, w 40");
		}

		{ // Shine
			add(new JLabel("Shine:"));
			IntegerModel shineModel = new IntegerModel(ab, "Shininess", 0, 128);
			JSpinner shine = new JSpinner(shineModel.getSpinnerModel());
			mDefault.addEnableComponent(shine, false);
			add(shine, "w 50");
		}

		{ // Rotation
			add(new JLabel("Rotation:"));
			DoubleModel rotationModel = new DoubleModel(ab, "Rotation", UnitGroup.UNITS_ANGLE);
			JSpinner rotation = new JSpinner(rotationModel.getSpinnerModel());
			rotation.setEditor(new SpinnerEditor(rotation));
			mDefault.addEnableComponent(rotation, false);
			add(rotation, "split 3, w 50");
			add(new UnitSelector(rotationModel));
			BasicSlider bs = new BasicSlider(rotationModel.getSliderModel(-Math.PI, Math.PI));
			mDefault.addEnableComponent(bs, false);
			add(bs, "w 100, wrap");
		}

		{// Placeholder
			add(new JLabel(""), "span 2");
		}

		{ // Repeat
			add(new JLabel("Repeat:"));
			EdgeMode[] list = new EdgeMode[EdgeMode.values().length + 1];
			System.arraycopy(EdgeMode.values(), 0, list, 1, EdgeMode.values().length);
			JComboBox combo = new JComboBox(new EnumModel<EdgeMode>(ab, "EdgeMode", list));
			mDefault.addEnableComponent(combo, false);
			add(combo);
		}

	}

}
