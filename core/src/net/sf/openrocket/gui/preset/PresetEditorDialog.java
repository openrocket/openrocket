package net.sf.openrocket.gui.preset;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.preset.loader.MaterialHolder;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

/**
 * Preset editor for creating new preset components.
 */
public class PresetEditorDialog extends JDialog implements ItemListener {
	
	private static Translator trans = Application.getTranslator();
	
	private static final Logger log = LoggerFactory.getLogger(PresetEditorDialog.class);
	
	private static final String NON_NEGATIVE_INTEGER_FIELD = "(\\d){0,10}";
	
	/**
	 * Input of non-negative decimals.
	 */
	final PresetInputVerifier NON_NEGATIVE_INTEGER = new PresetInputVerifier(Pattern.compile(NON_NEGATIVE_INTEGER_FIELD));
	
	private final JPanel contentPanel = new JPanel();
	private DeselectableComboBox typeCombo;
	private JTextField mfgTextField;
	private MaterialChooser materialChooser;
	private MaterialHolder holder = null;
	
	private JTextField ncPartNoTextField;
	private JTextField ncDescTextField;
	private DoubleModel ncLength;
	private JCheckBox ncFilledCB;
	private JComboBox ncShapeCB;
	private DoubleModel ncAftDia;
	private DoubleModel ncAftShoulderDia;
	private DoubleModel ncAftShoulderLen;
	private DoubleModel ncMass;
	private ImageIcon ncImage;
	private JButton ncImageBtn;
	
	private JTextField trPartNoTextField;
	private JTextField trDescTextField;
	private DoubleModel trLength;
	private DoubleModel trAftDia;
	private DoubleModel trAftShoulderDia;
	private DoubleModel trAftShoulderLen;
	private DoubleModel trForeDia;
	private DoubleModel trForeShoulderDia;
	private DoubleModel trForeShoulderLen;
	private DoubleModel trMass;
	private ImageIcon trImage;
	private JCheckBox trFilledCB;
	private JComboBox trShapeCB;
	private JButton trImageBtn;
	
	private JTextField btPartNoTextField;
	private JTextField btDescTextField;
	private DoubleModel btMass;
	private DoubleModel btInnerDia;
	private DoubleModel btOuterDia;
	private DoubleModel btLength;
	private ImageIcon btImage;
	private JButton btImageBtn;
	
	private JTextField tcPartNoTextField;
	private JTextField tcDescTextField;
	private DoubleModel tcMass;
	private DoubleModel tcInnerDia;
	private DoubleModel tcOuterDia;
	private DoubleModel tcLength;
	private ImageIcon tcImage;
	private JButton tcImageBtn;
	
	private JTextField bhPartNoTextField;
	private JTextField bhDescTextField;
	private DoubleModel bhOuterDia;
	private DoubleModel bhLength;
	private DoubleModel bhMass;
	private ImageIcon bhImage;
	private JButton bhImageBtn;
	
	private JTextField crPartNoTextField;
	private JTextField crDescTextField;
	private DoubleModel crOuterDia;
	private DoubleModel crInnerDia;
	private DoubleModel crThickness;
	private DoubleModel crMass;
	private ImageIcon crImage;
	private JButton crImageBtn;
	
	private JTextField ebPartNoTextField;
	private JTextField ebDescTextField;
	private DoubleModel ebOuterDia;
	private DoubleModel ebInnerDia;
	private DoubleModel ebThickness;
	private DoubleModel ebMass;
	private ImageIcon ebImage;
	private JButton ebImageBtn;
	
	private JTextField llPartNoTextField;
	private JTextField llDescTextField;
	private DoubleModel llOuterDia;
	private DoubleModel llInnerDia;
	private DoubleModel llLength;
	private DoubleModel llMass;
	private ImageIcon llImage;
	private JButton llImageBtn;
	
	private JTextField stPartNoTextField;
	private JTextField stDescTextField;
	private DoubleModel stThickness;
	private DoubleModel stWidth;
	private DoubleModel stLength;
	private DoubleModel stMass;
	private ImageIcon stImage;
	private JButton stImageBtn;
	
	private JTextField pcPartNoTextField;
	private JTextField pcDescTextField;
	private JTextField pcSides;
	private JTextField pcLineCount;
	private DoubleModel pcDiameter;
	private DoubleModel pcLineLength;
	private MaterialChooser pcLineMaterialChooser;
	private DoubleModel pcMass;
	private ImageIcon pcImage;
	private JButton pcImageBtn;
	
	private final JFileChooser imageChooser = createImageChooser();
	
	private JPanel componentOverlayPanel;
	
	private PresetResultListener resultListener;
	
	private static Map<String, String> componentMap = new HashMap<String, String>();
	
	private static final String NOSE_CONE_KEY = "NoseCone.NoseCone";
	private static final String BODY_TUBE_KEY = "BodyTube.BodyTube";
	private static final String TUBE_COUPLER_KEY = "TubeCoupler.TubeCoupler";
	private static final String TRANSITION_KEY = "Transition.Transition";
	private static final String CR_KEY = "ComponentIcons.Centeringring";
	private static final String BULKHEAD_KEY = "Bulkhead.Bulkhead";
	private static final String EB_KEY = "ComponentIcons.Engineblock";
	private static final String LAUNCH_LUG_KEY = "ComponentIcons.Launchlug";
	private static final String STREAMER_KEY = "ComponentIcons.Streamer";
	private static final String PARACHUTE_KEY = "ComponentIcons.Parachute";
	
	
	static {
		componentMap.put(trans.get(NOSE_CONE_KEY), "NOSECONE");
		componentMap.put(trans.get(BODY_TUBE_KEY), "BODYTUBE");
		componentMap.put(trans.get(TUBE_COUPLER_KEY), "TUBECOUPLER");
		componentMap.put(trans.get(TRANSITION_KEY), "TRANSITION");
		componentMap.put(trans.get(CR_KEY), "CENTERINGRING");
		componentMap.put(trans.get(BULKHEAD_KEY), "BULKHEAD");
		componentMap.put(trans.get(EB_KEY), "ENGINEBLOCK");
		componentMap.put(trans.get(LAUNCH_LUG_KEY), "LAUNCHLUG");
		componentMap.put(trans.get(PARACHUTE_KEY), "PARACHUTE");
		componentMap.put(trans.get(STREAMER_KEY), "STREAMER");
	}
	
	/**
	 * Create the dialog.
	 *
	 * @param theCallback the listener that gets the results of editing the presets
	 */
	public PresetEditorDialog(PresetResultListener theCallback) {
		this(theCallback, null, null);
	}
	
	/**
	 * Create the dialog.
	 *
	 * @param theCallback the listener that gets the results of editing the presets
	 * @param toEdit      the ComponentPreset to be edited; or null if a new one is being added
	 * @param matHolder   the set of materials; if null then use system materials
	 */
	public PresetEditorDialog(PresetResultListener theCallback, ComponentPreset toEdit, MaterialHolder matHolder) {
		resultListener = theCallback;
		getContentPane().setMinimumSize(new Dimension(200, 200));
		setBounds(100, 100, 825, 610);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new MigLayout("", "[][grow][94.00,grow][232.0,grow][130.00][grow]", "[][][20.00,grow][grow]"));
		JLabel lblManufacturer = new JLabel("Manufacturer:");
		contentPanel.add(lblManufacturer, "cell 2 0,alignx left,aligny center");
		
		mfgTextField = new JTextField();
		contentPanel.add(mfgTextField, "cell 3 0,growx");
		mfgTextField.setColumns(10);
		
		JLabel typeLabel = new JLabel("Type:");
		contentPanel.add(typeLabel, "cell 2 1,alignx left,aligny center");
		
		componentOverlayPanel = new JPanel();
		contentPanel.add(componentOverlayPanel, "cell 1 3 5 2,grow");
		componentOverlayPanel.setLayout(new CardLayout(0, 0));
		
		typeCombo = new DeselectableComboBox();
		typeCombo.addItemListener(this);
		typeCombo.setModel(new DefaultComboBoxModel());
		setItems(typeCombo, toEdit);
		contentPanel.add(typeCombo, "cell 3 1,growx");
		
		JLabel bhMaterialLabel = new JLabel("Material:");
		contentPanel.add(bhMaterialLabel, "cell 2 2, alignx left");
		
		materialChooser = new MaterialChooser(new MaterialModel(this, Material.Type.BULK));
		
		contentPanel.add(materialChooser, "cell 3 2,growx");
		
		{
			JPanel ncPanel = new JPanel();
			componentOverlayPanel.add(ncPanel, "NOSECONE");
			ncPanel.setLayout(new MigLayout("", "[61px][159.00,grow][45.00][109.00,grow][189.00,grow][grow]", "[16px][][][][][]"));
			JLabel ncPartNoLabel = new JLabel("Part No:");
			ncPanel.add(ncPartNoLabel, "cell 0 0,alignx left,aligny center");
			
			ncPartNoTextField = new JTextField();
			ncPanel.add(ncPartNoTextField, "cell 1 0,growx");
			ncPartNoTextField.setColumns(10);
			
			JLabel ncDescLabel = new JLabel("Description:");
			ncPanel.add(ncDescLabel, "cell 3 0,alignx left,aligny center");
			
			ncDescTextField = new JTextField();
			ncPanel.add(ncDescTextField, "cell 4 0,growx");
			ncDescTextField.setColumns(10);
			
			ncFilledCB = new JCheckBox("Filled");
			ncPanel.add(ncFilledCB, "cell 1 1");
			
			JLabel ncMaterialLabel = new JLabel(trans.get("RocketCompCfg.lbl.Componentmaterial"));
			ncPanel.add(ncMaterialLabel, "cell 0 1,alignx left");
			
			JLabel ncMassLabel = new JLabel(trans.get("RocketCompCfg.lbl.Componentmass"));
			ncPanel.add(ncMassLabel, "cell 3 1,alignx left");
			
			ncMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			JSpinner spin = new JSpinner(ncMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ncPanel.add(spin, "cell 4 1, growx");
			ncPanel.add(new UnitSelector(ncMass), "growx");
			
			JLabel ncShapeLabel = new JLabel(trans.get("NoseConeCfg.lbl.Noseconeshape"));
			ncPanel.add(ncShapeLabel, "cell 0 2,alignx left");
			
			ncShapeCB = new JComboBox();
			ncShapeCB.setModel(new DefaultComboBoxModel(new String[] { Transition.Shape.OGIVE.getName(), Transition.Shape.CONICAL.getName(), Transition.Shape.PARABOLIC.getName(),
					Transition.Shape.ELLIPSOID.getName(), Transition.Shape.HAACK.getName() }));
			ncPanel.add(ncShapeCB, "cell 1 2,growx");
			
			JLabel ncLengthLabel = new JLabel(trans.get("NoseConeCfg.lbl.Noseconelength"));
			ncPanel.add(ncLengthLabel, "cell 3 2,alignx left");
			
			ncLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(ncLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ncPanel.add(spin, "cell 4 2, growx");
			ncPanel.add(new UnitSelector(ncLength), "growx");
			
			JLabel ncAftDiaLabel = new JLabel("Aft Dia.:");
			ncPanel.add(ncAftDiaLabel, "cell 0 3,alignx left");
			
			ncAftDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(ncAftDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ncPanel.add(spin, "cell 1 3, growx");
			ncPanel.add(new UnitSelector(ncAftDia), "growx");
			
			JLabel ncAftShoulderLenLabel = new JLabel("Aft Shoulder Len:");
			ncPanel.add(ncAftShoulderLenLabel, "cell 0 4,alignx left");
			
			ncAftShoulderLen = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(ncAftShoulderLen.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ncPanel.add(spin, "cell 1 4, growx");
			ncPanel.add(new UnitSelector(ncAftShoulderLen), "growx");
			
			JLabel ncAftShoulderDiaLabel = new JLabel("Aft Shoulder Dia.:");
			ncPanel.add(ncAftShoulderDiaLabel, "cell 0 5,alignx left, aligny top, pad 7 0 0 0");
			
			ncAftShoulderDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(ncAftShoulderDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ncPanel.add(spin, "cell 1 5, growx, aligny top");
			ncPanel.add(new UnitSelector(ncAftShoulderDia), "growx, aligny top, pad 7 0 0 0");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			ncPanel.add(panel, "cell 4 3, span 1 3");
			panel.setLayout(null);
			ncImageBtn = new JButton("No Image");
			ncImageBtn.setMaximumSize(new Dimension(75, 75));
			ncImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(ncImageBtn);
			ncImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			ncImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						ncImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						ncImageBtn.setIcon(ncImage);
					}
				}
			});
			
		}
		{
			JPanel trPanel = new JPanel();
			componentOverlayPanel.add(trPanel, "TRANSITION");
			trPanel.setLayout(new MigLayout("", "[61px][159.00,grow][45.00][109.00,grow][189.00,grow][grow]", "[16px][][][][][]"));
			
			JLabel trPartNoLabel = new JLabel("Part No:");
			trPanel.add(trPartNoLabel, "cell 0 0,alignx left");
			
			trPartNoTextField = new JTextField();
			trPanel.add(trPartNoTextField, "cell 1 0,growx");
			trPartNoTextField.setColumns(10);
			
			JLabel trDescLabel = new JLabel("Description:");
			trPanel.add(trDescLabel, "cell 3 0,alignx left");
			
			trDescTextField = new JTextField();
			trPanel.add(trDescTextField, "cell 4 0,growx");
			trDescTextField.setColumns(10);
			
			trFilledCB = new JCheckBox("Filled");
			trPanel.add(trFilledCB, "cell 1 1");
			
			JLabel trMassLabel = new JLabel("Mass:");
			trPanel.add(trMassLabel, "cell 3 1,alignx left");
			
			trMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			JSpinner spin = new JSpinner(trMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 4 1, growx");
			trPanel.add(new UnitSelector(trMass), "growx");
			
			JLabel trShapeLabel = new JLabel("Shape:");
			trPanel.add(trShapeLabel, "cell 0 2,alignx left");
			
			trShapeCB = new JComboBox();
			trShapeCB.setModel(new DefaultComboBoxModel(new String[] { Transition.Shape.OGIVE.getName(), Transition.Shape.CONICAL.getName(), Transition.Shape.PARABOLIC.getName(),
					Transition.Shape.ELLIPSOID.getName(), Transition.Shape.HAACK.getName() }));
			trPanel.add(trShapeCB, "cell 1 2,growx");
			
			JLabel trLengthLabel = new JLabel("Length:");
			trPanel.add(trLengthLabel, "cell 3 2,alignx left");
			
			trLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 4 2, growx");
			trPanel.add(new UnitSelector(trLength), "growx");
			
			JLabel trAftDiaLabel = new JLabel("Aft Dia.:");
			trPanel.add(trAftDiaLabel, "cell 0 3,alignx left");
			
			trAftDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trAftDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 1 3, growx");
			trPanel.add(new UnitSelector(trAftDia), "growx");
			
			JLabel trForeDiaLabel = new JLabel("Fore Dia.:");
			trPanel.add(trForeDiaLabel, "cell 3 3,alignx left");
			
			trForeDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trForeDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 4 3, growx");
			trPanel.add(new UnitSelector(trForeDia), "growx");
			
			JLabel trAftShouldDiaLabel = new JLabel("Aft Shoulder Dia.:");
			trPanel.add(trAftShouldDiaLabel, "cell 0 4,alignx left");
			
			trAftShoulderDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trAftShoulderDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 1 4, growx");
			trPanel.add(new UnitSelector(trAftShoulderDia), "growx");
			
			JLabel trForeShouldDiaLabel = new JLabel("Fore Shoulder Dia.:");
			trPanel.add(trForeShouldDiaLabel, "cell 3 4,alignx left");
			
			trForeShoulderDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trForeShoulderDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 4 4, growx");
			trPanel.add(new UnitSelector(trForeShoulderDia), "growx");
			
			JLabel trAftShoulderLenLabel = new JLabel("Aft Shoulder Len.:");
			trPanel.add(trAftShoulderLenLabel, "cell 0 5,alignx left");
			
			trAftShoulderLen = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trAftShoulderLen.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 1 5, growx");
			trPanel.add(new UnitSelector(trAftShoulderLen), "growx");
			
			JLabel lblForeShoulderLen = new JLabel("Fore Shoulder Len.:");
			trPanel.add(lblForeShoulderLen, "cell 3 5,alignx left");
			
			trForeShoulderLen = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0, 2);
			spin = new JSpinner(trForeShoulderLen.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			trPanel.add(spin, "cell 4 5, growx");
			trPanel.add(new UnitSelector(trForeShoulderLen), "growx");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			trPanel.add(panel, "cell 4 6");
			panel.setLayout(null);
			trImageBtn = new JButton("No Image");
			trImageBtn.setMaximumSize(new Dimension(75, 75));
			trImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(trImageBtn);
			trImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			trImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						trImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						trImageBtn.setIcon(trImage);
					}
				}
			});
			
		}
		{
			JPanel btPanel = new JPanel();
			componentOverlayPanel.add(btPanel, "BODYTUBE");
			btPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			JLabel btPartNoLabel = new JLabel("Part No:");
			btPanel.add(btPartNoLabel, "cell 0 0,alignx left");
			
			btPartNoTextField = new JTextField();
			btPanel.add(btPartNoTextField, "cell 1 0,growx");
			btPartNoTextField.setColumns(10);
			
			JLabel btDescLabel = new JLabel("Description:");
			btPanel.add(btDescLabel, "cell 3 0,alignx left");
			
			btDescTextField = new JTextField();
			btPanel.add(btDescTextField, "cell 4 0,growx");
			btDescTextField.setColumns(10);
			
			JLabel btLengthLabel = new JLabel("Length:");
			btPanel.add(btLengthLabel, "cell 0 1,alignx left");
			
			btLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(btLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			btPanel.add(spin, "cell 1 1, growx");
			btPanel.add(new UnitSelector(btLength), "growx");
			
			JLabel btMassLabel = new JLabel("Mass:");
			btPanel.add(btMassLabel, "cell 3 1,alignx left");
			
			btMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(btMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			btPanel.add(spin, "cell 4 1, growx");
			btPanel.add(new UnitSelector(btMass), "w 34lp!");
			
			JLabel btInnerDiaLabel = new JLabel("Inner Dia.:");
			btPanel.add(btInnerDiaLabel, "cell 0 2,alignx left");
			
			btInnerDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(btInnerDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			btPanel.add(spin, "cell 1 2, growx");
			btPanel.add(new UnitSelector(btInnerDia), "growx");
			
			JLabel btOuterDiaLabel = new JLabel("Outer Dia.:");
			btPanel.add(btOuterDiaLabel, "cell 3 2,alignx left");
			
			btOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(btOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			btPanel.add(spin, "cell 4 2, growx");
			btPanel.add(new UnitSelector(btOuterDia), "w 34lp!");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			btPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			btImageBtn = new JButton("No Image");
			btImageBtn.setMaximumSize(new Dimension(75, 75));
			btImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(btImageBtn);
			btImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			btImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						btImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						btImageBtn.setIcon(btImage);
					}
				}
			});
			
		}
		{
			JPanel tcPanel = new JPanel();
			componentOverlayPanel.add(tcPanel, "TUBECOUPLER");
			tcPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			JLabel tcPartNoLabel = new JLabel("Part No:");
			tcPanel.add(tcPartNoLabel, "cell 0 0,alignx left");
			
			tcPartNoTextField = new JTextField();
			tcPanel.add(tcPartNoTextField, "cell 1 0,growx");
			tcPartNoTextField.setColumns(10);
			
			JLabel tcDescLabel = new JLabel("Description:");
			tcPanel.add(tcDescLabel, "cell 3 0,alignx left");
			
			tcDescTextField = new JTextField();
			tcPanel.add(tcDescTextField, "cell 4 0,growx");
			tcDescTextField.setColumns(10);
			
			JLabel tcLengthLabel = new JLabel("Length:");
			tcPanel.add(tcLengthLabel, "cell 0 1,alignx left");
			
			tcLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(tcLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			tcPanel.add(spin, "cell 1 1, growx");
			tcPanel.add(new UnitSelector(tcLength), "growx");
			
			JLabel tcMassLabel = new JLabel("Mass:");
			tcPanel.add(tcMassLabel, "cell 3 1,alignx left");
			
			tcMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(tcMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			tcPanel.add(spin, "cell 4 1, growx");
			tcPanel.add(new UnitSelector(tcMass), "w 34lp!");
			
			JLabel tcInnerDiaLabel = new JLabel("Inner Dia.:");
			tcPanel.add(tcInnerDiaLabel, "cell 0 2,alignx left");
			
			tcInnerDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(tcInnerDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			tcPanel.add(spin, "cell 1 2, growx");
			tcPanel.add(new UnitSelector(tcInnerDia), "growx");
			
			JLabel tcOuterDiaLabel = new JLabel("Outer Dia.:");
			tcPanel.add(tcOuterDiaLabel, "cell 3 2,alignx left");
			
			tcOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(tcOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			tcPanel.add(spin, "cell 4 2, growx");
			tcPanel.add(new UnitSelector(tcOuterDia), "w 34lp!");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			tcPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			tcImageBtn = new JButton("No Image");
			tcImageBtn.setMaximumSize(new Dimension(75, 75));
			tcImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(tcImageBtn);
			tcImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			tcImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						tcImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						tcImageBtn.setIcon(tcImage);
					}
				}
			});
			
			
		}
		{
			JPanel bhPanel = new JPanel();
			componentOverlayPanel.add(bhPanel, "BULKHEAD");
			bhPanel.setLayout(new MigLayout("", "[][157.00,grow 79][65.00][grow]", "[][][][]"));
			
			JLabel bhPartNoLabel = new JLabel("Part No:");
			bhPanel.add(bhPartNoLabel, "cell 0 0,alignx left");
			
			bhPartNoTextField = new JTextField();
			bhPanel.add(bhPartNoTextField, "cell 1 0,growx");
			bhPartNoTextField.setColumns(10);
			
			JLabel bhDescLabel = new JLabel("Description:");
			bhPanel.add(bhDescLabel, "cell 3 0,alignx left");
			
			bhDescTextField = new JTextField();
			bhPanel.add(bhDescTextField, "cell 4 0,growx");
			bhDescTextField.setColumns(10);
			
			JLabel bhLengthLabel = new JLabel("Thickness:");
			bhPanel.add(bhLengthLabel, "cell 0 1,alignx left");
			
			bhLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(bhLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			bhPanel.add(spin, "cell 1 1, growx");
			bhPanel.add(new UnitSelector(bhLength), "w 34lp!");
			
			JLabel bhMassLabel = new JLabel("Mass:");
			bhPanel.add(bhMassLabel, "cell 3 1,alignx left");
			
			bhMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(bhMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			bhPanel.add(spin, "cell 4 1, growx");
			bhPanel.add(new UnitSelector(bhMass), "growx");
			
			JLabel bhOuterDiaLabel = new JLabel("Outer Dia.:");
			bhPanel.add(bhOuterDiaLabel, "cell 0 2,alignx left, aligny top, pad 7 0 0 0");
			
			bhOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(bhOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			bhPanel.add(spin, "cell 1 2, growx, aligny top");
			bhPanel.add(new UnitSelector(bhOuterDia), "w 34lp!, h 27lp!, aligny top, pad 7 0 0 0");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			bhPanel.add(panel, "cell 4 2");
			panel.setLayout(null);
			bhImageBtn = new JButton("No Image");
			bhImageBtn.setMaximumSize(new Dimension(75, 75));
			bhImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(bhImageBtn);
			bhImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			bhImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						bhImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						bhImageBtn.setIcon(bhImage);
					}
				}
			});
			
		}
		{
			JPanel crPanel = new JPanel();
			componentOverlayPanel.add(crPanel, "CENTERINGRING");
			crPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			
			JLabel crPartNoLabel = new JLabel("Part No:");
			crPanel.add(crPartNoLabel, "cell 0 0,alignx left");
			
			crPartNoTextField = new JTextField();
			crPanel.add(crPartNoTextField, "cell 1 0, growx");
			crPartNoTextField.setColumns(10);
			
			JLabel crDescLabel = new JLabel("Description:");
			crPanel.add(crDescLabel, "cell 3 0,alignx left");
			
			crDescTextField = new JTextField();
			crPanel.add(crDescTextField, "cell 4 0, growx");
			crDescTextField.setColumns(10);
			
			JLabel crThicknessLabel = new JLabel("Thickness:");
			crPanel.add(crThicknessLabel, "cell 0 1,alignx left");
			
			crThickness = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(crThickness.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			crPanel.add(spin, "cell 1 1, growx");
			crPanel.add(new UnitSelector(crThickness), "growx");
			
			JLabel crMassLabel = new JLabel("Mass:");
			crPanel.add(crMassLabel, "cell 3 1,alignx left");
			
			crMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(crMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			crPanel.add(spin, "cell 4 1, growx");
			crPanel.add(new UnitSelector(crMass), "w 34lp!");
			
			JLabel crOuterDiaLabel = new JLabel("Outer Dia.:");
			crPanel.add(crOuterDiaLabel, "cell 0 2,alignx left");
			
			crOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(crOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			crPanel.add(spin, "cell 1 2, growx");
			crPanel.add(new UnitSelector(crOuterDia), "w 34lp!");
			
			JLabel crInnerDiaLabel = new JLabel("Inner Dia.:");
			crPanel.add(crInnerDiaLabel, "cell 3 2,alignx left");
			
			crInnerDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(crInnerDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			crPanel.add(spin, "cell 4 2, growx");
			crPanel.add(new UnitSelector(crInnerDia), "w 34lp!");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			crPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			crImageBtn = new JButton("No Image");
			crImageBtn.setMaximumSize(new Dimension(75, 75));
			crImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(crImageBtn);
			crImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			crImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						crImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						crImageBtn.setIcon(crImage);
					}
				}
			});
			
		}
		{
			JPanel ebPanel = new JPanel();
			componentOverlayPanel.add(ebPanel, "ENGINEBLOCK");
			ebPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			JLabel ebPartNoLabel = new JLabel("Part No:");
			ebPanel.add(ebPartNoLabel, "cell 0 0,alignx left");
			
			ebPartNoTextField = new JTextField();
			ebPanel.add(ebPartNoTextField, "cell 1 0,growx");
			ebPartNoTextField.setColumns(10);
			
			JLabel ebDescLabel = new JLabel("Description:");
			ebPanel.add(ebDescLabel, "cell 3 0,alignx left");
			
			ebDescTextField = new JTextField();
			ebPanel.add(ebDescTextField, "cell 4 0,growx");
			ebDescTextField.setColumns(10);
			
			JLabel ebThicknessLabel = new JLabel("Thickness:");
			ebPanel.add(ebThicknessLabel, "cell 0 1,alignx left");
			
			ebThickness = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(ebThickness.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ebPanel.add(spin, "cell 1 1, growx");
			ebPanel.add(new UnitSelector(ebThickness), "growx");
			
			JLabel ebMassLabel = new JLabel("Mass:");
			ebPanel.add(ebMassLabel, "cell 3 1,alignx left");
			
			ebMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(ebMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ebPanel.add(spin, "cell 4 1, growx");
			ebPanel.add(new UnitSelector(ebMass), "w 34lp!");
			
			JLabel ebOuterDiaLabel = new JLabel("Outer Dia.:");
			ebPanel.add(ebOuterDiaLabel, "cell 0 2,alignx left");
			
			ebOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(ebOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ebPanel.add(spin, "cell 1 2, growx");
			ebPanel.add(new UnitSelector(ebOuterDia), "growx");
			
			JLabel ebInnerDiaLabel = new JLabel("Inner Dia.:");
			ebPanel.add(ebInnerDiaLabel, "cell 3 2,alignx left");
			
			ebInnerDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(ebInnerDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			ebPanel.add(spin, "cell 4 2, growx");
			ebPanel.add(new UnitSelector(ebInnerDia), "w 34lp!");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			ebPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			ebImageBtn = new JButton("No Image");
			ebImageBtn.setMaximumSize(new Dimension(75, 75));
			ebImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(ebImageBtn);
			ebImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			ebImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						ebImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						ebImageBtn.setIcon(ebImage);
					}
				}
			});
		}
		
		{
			JPanel llPanel = new JPanel();
			componentOverlayPanel.add(llPanel, "LAUNCHLUG");
			llPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			JLabel llPartNoLabel = new JLabel("Part No:");
			llPanel.add(llPartNoLabel, "cell 0 0,alignx left");
			
			llPartNoTextField = new JTextField();
			llPanel.add(llPartNoTextField, "cell 1 0,growx");
			llPartNoTextField.setColumns(10);
			
			JLabel llDescLabel = new JLabel("Description:");
			llPanel.add(llDescLabel, "cell 3 0,alignx left");
			
			llDescTextField = new JTextField();
			llPanel.add(llDescTextField, "cell 4 0,growx");
			llDescTextField.setColumns(10);
			
			JLabel llLengthLabel = new JLabel("Length:");
			llPanel.add(llLengthLabel, "cell 0 1,alignx left");
			
			llLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(llLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			llPanel.add(spin, "cell 1 1, growx");
			llPanel.add(new UnitSelector(llLength), "growx");
			
			JLabel llMassLabel = new JLabel("Mass:");
			llPanel.add(llMassLabel, "cell 3 1,alignx left");
			
			llMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(llMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			llPanel.add(spin, "cell 4 1, growx");
			llPanel.add(new UnitSelector(llMass), "w 34lp!");
			
			JLabel llOuterDiaLabel = new JLabel("Outer Dia.:");
			llPanel.add(llOuterDiaLabel, "cell 0 2,alignx left");
			
			llOuterDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(llOuterDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			llPanel.add(spin, "cell 1 2, growx");
			llPanel.add(new UnitSelector(llOuterDia), "growx");
			
			JLabel llInnerDiaLabel = new JLabel("Inner Dia.:");
			llPanel.add(llInnerDiaLabel, "cell 3 2,alignx left");
			
			llInnerDia = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(llInnerDia.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			llPanel.add(spin, "cell 4 2, growx");
			llPanel.add(new UnitSelector(llInnerDia), "w 34lp!");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			llPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			llImageBtn = new JButton("No Image");
			llImageBtn.setMaximumSize(new Dimension(75, 75));
			llImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(llImageBtn);
			llImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			llImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						llImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						llImageBtn.setIcon(llImage);
					}
				}
			});
		}
		
		{
			JPanel stPanel = new JPanel();
			componentOverlayPanel.add(stPanel, "STREAMER");
			stPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][][]"));
			JLabel stPartNoLabel = new JLabel("Part No:");
			stPanel.add(stPartNoLabel, "cell 0 0,alignx left");
			
			stPartNoTextField = new JTextField();
			stPanel.add(stPartNoTextField, "cell 1 0,growx");
			stPartNoTextField.setColumns(10);
			
			JLabel stDescLabel = new JLabel("Description:");
			stPanel.add(stDescLabel, "cell 3 0,alignx left");
			
			stDescTextField = new JTextField();
			stPanel.add(stDescTextField, "cell 4 0,growx");
			stDescTextField.setColumns(10);
			
			JLabel stLengthLabel = new JLabel("Length:");
			stPanel.add(stLengthLabel, "cell 0 1,alignx left");
			
			stLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			JSpinner spin = new JSpinner(stLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			stPanel.add(spin, "cell 1 1, growx");
			stPanel.add(new UnitSelector(stLength), "growx");
			
			JLabel stMassLabel = new JLabel("Mass:");
			stPanel.add(stMassLabel, "cell 3 1,alignx left");
			
			stMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			spin = new JSpinner(stMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			stPanel.add(spin, "cell 4 1, growx");
			stPanel.add(new UnitSelector(stMass), "growx");
			
			JLabel stThicknessLabel = new JLabel("Thickness:");
			stPanel.add(stThicknessLabel, "cell 0 2,alignx left");
			
			stThickness = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(stThickness.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			stPanel.add(spin, "cell 1 2, growx");
			stPanel.add(new UnitSelector(stThickness), "growx");
			
			JLabel stWidthLabel = new JLabel("Width:");
			stPanel.add(stWidthLabel, "cell 3 2,alignx left");
			
			stWidth = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(stWidth.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			stPanel.add(spin, "cell 4 2, growx");
			stPanel.add(new UnitSelector(stWidth), "growx");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			stPanel.add(panel, "cell 4 3");
			panel.setLayout(null);
			stImageBtn = new JButton("No Image");
			stImageBtn.setMaximumSize(new Dimension(75, 75));
			stImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(stImageBtn);
			stImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			stImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						stImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						stImageBtn.setIcon(stImage);
					}
				}
			});
		}
		
		{
			JPanel pcPanel = new JPanel();
			componentOverlayPanel.add(pcPanel, "PARACHUTE");
			pcPanel.setLayout(new MigLayout("", "[][157.00,grow 79][65.00][grow][][]", "[][][][][][]"));
			JLabel pcPartNoLabel = new JLabel("Part No:");
			pcPanel.add(pcPartNoLabel, "cell 0 0,alignx left");
			
			pcPartNoTextField = new JTextField();
			pcPanel.add(pcPartNoTextField, "cell 1 0,growx");
			pcPartNoTextField.setColumns(10);
			
			JLabel pcDescLabel = new JLabel("Description:");
			pcPanel.add(pcDescLabel, "cell 3 0,alignx left");
			
			pcDescTextField = new JTextField();
			pcPanel.add(pcDescTextField, "cell 4 0,growx");
			pcDescTextField.setColumns(10);
			
			JLabel pcSidesLabel = new JLabel("Sides:");
			pcPanel.add(pcSidesLabel, "cell 0 1,alignx left");
			
			pcSides = new JTextField();
			pcPanel.add(pcSides, "cell 1 1, growx");
			pcSides.setInputVerifier(NON_NEGATIVE_INTEGER);
			pcSides.setColumns(10);
			
			JLabel pcMassLabel = new JLabel("Mass:");
			pcPanel.add(pcMassLabel, "cell 3 1,alignx left");
			
			pcMass = new DoubleModel(0, UnitGroup.UNITS_MASS, 0);
			JSpinner spin = new JSpinner(pcMass.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			pcPanel.add(spin, "cell 4 1, growx");
			pcPanel.add(new UnitSelector(pcMass), "growx");
			
			JLabel pcDiameterLabel = new JLabel("Diameter:");
			pcPanel.add(pcDiameterLabel, "cell 0 2,alignx left");
			
			pcDiameter = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(pcDiameter.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			pcPanel.add(spin, "cell 1 2, growx");
			pcPanel.add(new UnitSelector(pcDiameter));
			
			JLabel pcLineLengthLabel = new JLabel("Line Length:");
			pcPanel.add(pcLineLengthLabel, "cell 3 2,alignx left");
			
			pcLineLength = new DoubleModel(0, UnitGroup.UNITS_LENGTH, 0);
			spin = new JSpinner(pcLineLength.getSpinnerModel());
			spin.setEditor(new SpinnerEditor(spin));
			pcPanel.add(spin, "cell 4 2, growx");
			pcPanel.add(new UnitSelector(pcLineLength), "growx");
			
			JLabel pcLineCountLabel = new JLabel("Line Count:");
			pcPanel.add(pcLineCountLabel, "cell 3 3,alignx left");
			
			pcLineCount = new JTextField();
			pcLineCount.setInputVerifier(NON_NEGATIVE_INTEGER);
			pcPanel.add(pcLineCount, "cell 4 3, growx");
			pcLineCount.setColumns(10);
			
			JLabel pcLineMaterialLabel = new JLabel("Line Material:");
			pcPanel.add(pcLineMaterialLabel, "cell 3 4,alignx left, aligny top, pad 7 0 0 0 ");
			
			pcLineMaterialChooser = new MaterialChooser();
			pcLineMaterialChooser.setModel(new MaterialModel(PresetEditorDialog.this, Material.Type.LINE));
			pcPanel.add(pcLineMaterialChooser, "cell 4 4, span 3 1, growx, aligny top");
			
			JPanel panel = new JPanel();
			panel.setMinimumSize(new Dimension(200, 200));
			pcPanel.add(panel, "cell 1 3, span 1 3");
			panel.setLayout(null);
			pcImageBtn = new JButton("No Image");
			pcImageBtn.setMaximumSize(new Dimension(75, 75));
			pcImageBtn.setMinimumSize(new Dimension(75, 75));
			panel.add(pcImageBtn);
			pcImageBtn.setBounds(new Rectangle(6, 6, 132, 145));
			
			pcImageBtn.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					int returnVal = imageChooser.showOpenDialog(PresetEditorDialog.this);
					
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						File file = imageChooser.getSelectedFile();
						pcImage = scaleImage(new ImageIcon(file.getAbsolutePath()).getImage(), 155);
						pcImageBtn.setIcon(pcImage);
					}
				}
			});
		}
		
		JPanel buttonPane = new JPanel();
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new MigLayout("", "[130px][176.00px][131.00px]", "[29px]"));
		JButton btnSaveAndNew = new JButton("Save and New");
		btnSaveAndNew.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				saveResult();
			}
		});
		buttonPane.add(btnSaveAndNew, "cell 0 0,alignx left,aligny top");
		
		JButton okButton = new JButton("Save and Close");
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (saveResult()) {
					dispose();
				}
			}
		});
		okButton.setActionCommand("OK");
		buttonPane.add(okButton, "cell 1 0,alignx left,aligny top");
		getRootPane().setDefaultButton(okButton);
		
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Close");
		buttonPane.add(cancelButton, "cell 6 0,alignx right,aligny top");
		
		if (toEdit != null) {
			fillEditor(toEdit, matHolder);
		}
		holder = matHolder;
	}
	
	/**
	 * When an existing preset is edited, we want to disable the other types of presets.  If the user wants a different
	 * type of component, then they should delete this one and add a new one.
	 *
	 * @param cb     the combo box component
	 * @param preset the preset being edited
	 */
	private void setItems(DeselectableComboBox cb, ComponentPreset preset) {
		cb.addItem(trans.get(NOSE_CONE_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.NOSE_CONE));
		cb.addItem(trans.get(BODY_TUBE_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.BODY_TUBE));
		cb.addItem(trans.get(BULKHEAD_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.BULK_HEAD));
		cb.addItem(trans.get(CR_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.CENTERING_RING));
		cb.addItem(trans.get(EB_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.ENGINE_BLOCK));
		cb.addItem(trans.get(TRANSITION_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.TRANSITION));
		cb.addItem(trans.get(TUBE_COUPLER_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.TUBE_COUPLER));
		cb.addItem(trans.get(LAUNCH_LUG_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.LAUNCH_LUG));
		cb.addItem(trans.get(PARACHUTE_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.PARACHUTE));
		cb.addItem(trans.get(STREAMER_KEY), preset != null && !preset.get(ComponentPreset.TYPE).equals(ComponentPreset.Type.STREAMER));
	}
	
	/**
	 * Create an image chooser.  Currently png and jpg are supported.
	 *
	 * @return a file chooser that looks for image files
	 */
	private JFileChooser createImageChooser() {
		final JFileChooser chooser = new JFileChooser();
		ImagePreviewPanel preview = new ImagePreviewPanel();
		chooser.setAccessory(preview);
		chooser.addPropertyChangeListener(preview);
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg"));
		return chooser;
	}
	
	/**
	 * To support editing of an existing preset, the swing components need to be prepopulated with the field data.
	 *
	 * @param preset the preset to edit
	 */
	private void fillEditor(ComponentPreset preset, MaterialHolder matHolder) {
		ComponentPreset.Type t = preset.getType();
		
		mfgTextField.setText(preset.get(ComponentPreset.MANUFACTURER).getDisplayName());
		setMaterial(materialChooser, preset, matHolder, Material.Type.BULK, ComponentPreset.MATERIAL);
		switch (t) {
		case BODY_TUBE:
			typeCombo.setSelectedItem(trans.get(BODY_TUBE_KEY));
			btDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				btInnerDia.setValue(preset.get(ComponentPreset.INNER_DIAMETER));
				btInnerDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				btLength.setValue(preset.get(ComponentPreset.LENGTH));
				btLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				btMass.setValue(preset.get(ComponentPreset.MASS));
				btMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				btOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				btOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				btImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				btImageBtn.setIcon(btImage);
			}
			btPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case BULK_HEAD:
			typeCombo.setSelectedItem(trans.get(BULKHEAD_KEY));
			bhDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.LENGTH)) {
				bhLength.setValue(preset.get(ComponentPreset.LENGTH));
				bhLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				bhMass.setValue(preset.get(ComponentPreset.MASS));
				bhMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				bhOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				bhOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				bhImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				bhImageBtn.setIcon(bhImage);
			}
			bhPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case CENTERING_RING:
			typeCombo.setSelectedItem(trans.get(CR_KEY));
			crDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				crInnerDia.setValue(preset.get(ComponentPreset.INNER_DIAMETER));
				crInnerDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				crThickness.setValue(preset.get(ComponentPreset.LENGTH));
				crThickness.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				crMass.setValue(preset.get(ComponentPreset.MASS));
				crMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				crOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				crOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				crImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				crImageBtn.setIcon(crImage);
			}
			crPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case ENGINE_BLOCK:
			typeCombo.setSelectedItem(trans.get(EB_KEY));
			ebDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				ebInnerDia.setValue(preset.get(ComponentPreset.INNER_DIAMETER));
				ebInnerDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				ebThickness.setValue(preset.get(ComponentPreset.LENGTH));
				ebThickness.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				ebMass.setValue(preset.get(ComponentPreset.MASS));
				ebMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				ebOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				ebOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				ebImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				ebImageBtn.setIcon(ebImage);
			}
			ebPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case NOSE_CONE:
			typeCombo.setSelectedItem(trans.get(NOSE_CONE_KEY));
			ncDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.AFT_OUTER_DIAMETER)) {
				ncAftDia.setValue(preset.get(ComponentPreset.AFT_OUTER_DIAMETER));
				ncAftDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
				ncAftShoulderDia.setValue(preset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
				ncAftShoulderDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
				ncAftShoulderLen.setValue(preset.get(ComponentPreset.AFT_SHOULDER_LENGTH));
				ncAftShoulderLen.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				ncMass.setValue(preset.get(ComponentPreset.MASS));
				ncMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.SHAPE)) {
				ncShapeCB.setSelectedItem(preset.get(ComponentPreset.SHAPE).toString());
			}
			if (preset.has(ComponentPreset.FILLED)) {
				ncFilledCB.setSelected((preset.get(ComponentPreset.FILLED)));
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				ncLength.setValue(preset.get(ComponentPreset.LENGTH));
				ncLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				ncImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				ncImageBtn.setIcon(ncImage);
			}
			ncPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case TRANSITION:
			typeCombo.setSelectedItem(trans.get(TRANSITION_KEY));
			trDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.AFT_OUTER_DIAMETER)) {
				trAftDia.setValue(preset.get(ComponentPreset.AFT_OUTER_DIAMETER));
				trAftDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
				trAftShoulderDia.setValue(preset.get(ComponentPreset.AFT_SHOULDER_DIAMETER));
				trAftShoulderDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
				trAftShoulderLen.setValue(preset.get(ComponentPreset.AFT_SHOULDER_LENGTH));
				trAftShoulderLen.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.FORE_OUTER_DIAMETER)) {
				trForeDia.setValue(preset.get(ComponentPreset.FORE_OUTER_DIAMETER));
				trForeDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.FORE_SHOULDER_DIAMETER)) {
				trForeShoulderDia.setValue(preset.get(ComponentPreset.FORE_SHOULDER_DIAMETER));
				trForeShoulderDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.FORE_SHOULDER_LENGTH)) {
				trForeShoulderLen.setValue(preset.get(ComponentPreset.FORE_SHOULDER_LENGTH));
				trForeShoulderLen.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				trMass.setValue(preset.get(ComponentPreset.MASS));
				trMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.SHAPE)) {
				trShapeCB.setSelectedItem(preset.get(ComponentPreset.SHAPE).toString());
			}
			if (preset.has(ComponentPreset.FILLED)) {
				trFilledCB.setSelected((preset.get(ComponentPreset.FILLED)));
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				trLength.setValue(preset.get(ComponentPreset.LENGTH));
				trLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				trImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				trImageBtn.setIcon(trImage);
			}
			trPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		case TUBE_COUPLER:
			typeCombo.setSelectedItem(trans.get(TUBE_COUPLER_KEY));
			tcDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				tcInnerDia.setValue(preset.get(ComponentPreset.INNER_DIAMETER));
				tcInnerDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				tcLength.setValue(preset.get(ComponentPreset.LENGTH));
				tcLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				tcMass.setValue(preset.get(ComponentPreset.MASS));
				tcMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				tcOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				tcOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			tcPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			if (preset.has(ComponentPreset.IMAGE)) {
				tcImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				tcImageBtn.setIcon(tcImage);
			}
			break;
		case LAUNCH_LUG:
			typeCombo.setSelectedItem(trans.get(LAUNCH_LUG_KEY));
			llDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.INNER_DIAMETER)) {
				llInnerDia.setValue(preset.get(ComponentPreset.INNER_DIAMETER));
				llInnerDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LENGTH)) {
				llLength.setValue(preset.get(ComponentPreset.LENGTH));
				llLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				llMass.setValue(preset.get(ComponentPreset.MASS));
				llMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
				llOuterDia.setValue(preset.get(ComponentPreset.OUTER_DIAMETER));
				llOuterDia.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			llPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			if (preset.has(ComponentPreset.IMAGE)) {
				llImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				llImageBtn.setIcon(llImage);
			}
			break;
		case PARACHUTE:
			setMaterial(materialChooser, preset, matHolder, Material.Type.SURFACE, ComponentPreset.MATERIAL);
			typeCombo.setSelectedItem(trans.get(PARACHUTE_KEY));
			pcDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.LINE_COUNT)) {
				pcLineCount.setText(preset.get(ComponentPreset.LINE_COUNT).toString());
			}
			if (preset.has(ComponentPreset.SIDES)) {
				pcSides.setText(preset.get(ComponentPreset.SIDES).toString());
			}
			if (preset.has(ComponentPreset.MASS)) {
				pcMass.setValue(preset.get(ComponentPreset.MASS));
				pcMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.DIAMETER)) {
				pcDiameter.setValue(preset.get(ComponentPreset.DIAMETER));
				pcDiameter.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.LINE_LENGTH)) {
				pcLineLength.setValue(preset.get(ComponentPreset.LINE_LENGTH));
				pcLineLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			pcPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			if (preset.has(ComponentPreset.IMAGE)) {
				pcImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				pcImageBtn.setIcon(pcImage);
			}
			setMaterial(pcLineMaterialChooser, preset, matHolder, Material.Type.LINE, ComponentPreset.LINE_MATERIAL);
			//                pcLineMaterialChooser.setModel(new MaterialModel(PresetEditorDialog.this, Material.Type.LINE));
			
			//                pcLineMaterialChooser.getModel().setSelectedItem(preset.get(ComponentPreset.LINE_MATERIAL));
			break;
		case STREAMER:
			setMaterial(materialChooser, preset, matHolder, Material.Type.SURFACE, ComponentPreset.MATERIAL);
			typeCombo.setSelectedItem(trans.get(STREAMER_KEY));
			stDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));
			if (preset.has(ComponentPreset.LENGTH)) {
				stLength.setValue(preset.get(ComponentPreset.LENGTH));
				stLength.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.THICKNESS)) {
				stThickness.setValue(preset.get(ComponentPreset.LENGTH));
				stThickness.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.MASS)) {
				stMass.setValue(preset.get(ComponentPreset.MASS));
				stMass.setCurrentUnit(UnitGroup.UNITS_MASS.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.WIDTH)) {
				stWidth.setValue(preset.get(ComponentPreset.WIDTH));
				stWidth.setCurrentUnit(UnitGroup.UNITS_LENGTH.getDefaultUnit());
			}
			if (preset.has(ComponentPreset.IMAGE)) {
				stImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
				stImageBtn.setIcon(stImage);
			}
			stPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
			break;
		default:
		}
	}
	
	private void setMaterial(final JComboBox chooser, final ComponentPreset preset, final MaterialHolder holder,
			final Material.Type theType, final TypedKey<Material> key) {
		if (holder == null) {
			chooser.setModel(new MaterialModel(PresetEditorDialog.this, theType));
		}
		else {
			chooser.setModel(new MaterialModel(PresetEditorDialog.this, theType,
					holder.asDatabase(theType)));
		}
		if (preset != null) {
			chooser.getModel().setSelectedItem(preset.get(key));
		}
	}
	
	/**
	 * Extract the preset data from the UI fields, create a ComponentPreset instance, and notify the listener.
	 */
	private boolean saveResult() {
		String type = (String) typeCombo.getSelectedItem();
		
		ComponentPreset result = null;
		
		if (type.equals(trans.get(NOSE_CONE_KEY))) {
			result = extractNoseCone();
			if (result != null) {
				clearNoseCone();
			}
		}
		else if (type.equals(trans.get(TRANSITION_KEY))) {
			result = extractTransition();
			if (result != null) {
				clearTransition();
			}
		}
		else if (type.equals(trans.get(BODY_TUBE_KEY))) {
			result = extractBodyTube();
			if (result != null) {
				clearBodyTube();
			}
		}
		else if (type.equals(trans.get(TUBE_COUPLER_KEY))) {
			result = extractTubeCoupler();
			if (result != null) {
				clearTubeCoupler();
			}
		}
		else if (type.equals(trans.get(EB_KEY))) {
			result = extractEngineBlock();
			if (result != null) {
				clearEngineBlock();
			}
		}
		else if (type.equals(trans.get(CR_KEY))) {
			result = extractCenteringRing();
			if (result != null) {
				clearCenteringRing();
			}
		}
		else if (type.equals(trans.get(BULKHEAD_KEY))) {
			result = extractBulkhead();
			if (result != null) {
				clearBulkhead();
			}
		}
		else if (type.equals(trans.get(LAUNCH_LUG_KEY))) {
			result = extractLaunchLug();
			if (result != null) {
				clearLaunchLug();
			}
		}
		else if (type.equals(trans.get(PARACHUTE_KEY))) {
			result = extractParachute();
			if (result != null) {
				clearParachute();
			}
		}
		else if (type.equals(trans.get(STREAMER_KEY))) {
			result = extractStreamer();
			if (result != null) {
				clearStreamer();
			}
		}
		if (result != null) {
			resultListener.notifyResult(result);
			return true;
		}
		else {
			return false;
		}
	}
	
	private ComponentPreset extractNoseCone() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
			props.put(ComponentPreset.AFT_OUTER_DIAMETER, ncAftDia.getValue());
			props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, ncAftShoulderDia.getValue());
			props.put(ComponentPreset.AFT_SHOULDER_LENGTH, ncAftShoulderLen.getValue());
			props.put(ComponentPreset.DESCRIPTION, ncDescTextField.getText());
			props.put(ComponentPreset.PARTNO, ncPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, ncLength.getValue());
			props.put(ComponentPreset.SHAPE, Transition.Shape.toShape((String) ncShapeCB.getSelectedItem()));
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, ncMass.getValue());
			props.put(ComponentPreset.FILLED, ncFilledCB.isSelected());
			if (ncImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(ncImage.getImage()));
			}
			
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert nose cone attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory nose cone attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearNoseCone() {
		ncAftDia.setValue(0);
		ncAftShoulderDia.setValue(0);
		ncAftShoulderLen.setValue(0);
		ncDescTextField.setText("");
		ncPartNoTextField.setText("");
		ncLength.setValue(0);
		ncMass.setValue(0);
		ncFilledCB.setSelected(false);
		ncImage = null;
		ncImageBtn.setIcon(null);
	}
	
	private ComponentPreset extractTransition() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
			props.put(ComponentPreset.AFT_OUTER_DIAMETER, trAftDia.getValue());
			props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, trAftShoulderDia.getValue());
			props.put(ComponentPreset.AFT_SHOULDER_LENGTH, trAftShoulderLen.getValue());
			props.put(ComponentPreset.FORE_OUTER_DIAMETER, trForeDia.getValue());
			props.put(ComponentPreset.FORE_SHOULDER_DIAMETER, trForeShoulderDia.getValue());
			props.put(ComponentPreset.FORE_SHOULDER_LENGTH, trForeShoulderLen.getValue());
			props.put(ComponentPreset.DESCRIPTION, trDescTextField.getText());
			props.put(ComponentPreset.PARTNO, trPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			
			props.put(ComponentPreset.LENGTH, trLength.getValue());
			props.put(ComponentPreset.SHAPE, Transition.Shape.toShape((String) trShapeCB.getSelectedItem()));
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, trMass.getValue());
			props.put(ComponentPreset.FILLED, trFilledCB.isSelected());
			if (trImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(trImage.getImage()));
			}
			
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert transition attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory transition attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearTransition() {
		trAftDia.setValue(0);
		trAftShoulderDia.setValue(0);
		trAftShoulderLen.setValue(0);
		trForeDia.setValue(0);
		trForeShoulderDia.setValue(0);
		trForeShoulderLen.setValue(0);
		trDescTextField.setText("");
		trPartNoTextField.setText("");
		trLength.setValue(0);
		trMass.setValue(0);
		trFilledCB.setSelected(false);
		trImage = null;
		trImageBtn.setIcon(null);
	}
	
	private ComponentPreset extractBodyTube() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
			props.put(ComponentPreset.OUTER_DIAMETER, btOuterDia.getValue());
			props.put(ComponentPreset.INNER_DIAMETER, btInnerDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, btDescTextField.getText());
			props.put(ComponentPreset.PARTNO, btPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, btLength.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, btMass.getValue());
			if (btImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(btImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert body tube attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory body tube attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearBodyTube() {
		btOuterDia.setValue(0);
		btInnerDia.setValue(0);
		btDescTextField.setText("");
		btPartNoTextField.setText("");
		btLength.setValue(0);
		btMass.setValue(0);
		btImage = null;
		btImageBtn.setIcon(null);
	}
	
	public ComponentPreset extractTubeCoupler() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.TUBE_COUPLER);
			props.put(ComponentPreset.OUTER_DIAMETER, tcOuterDia.getValue());
			props.put(ComponentPreset.INNER_DIAMETER, tcInnerDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, tcDescTextField.getText());
			props.put(ComponentPreset.PARTNO, tcPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, tcLength.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, tcMass.getValue());
			if (tcImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(tcImage.getImage()));
			}
			
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert tube coupler attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory tube coupler attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearTubeCoupler() {
		tcOuterDia.setValue(0);
		tcInnerDia.setValue(0);
		tcDescTextField.setText("");
		tcPartNoTextField.setText("");
		tcLength.setValue(0);
		tcMass.setValue(0);
		tcImage = null;
		tcImageBtn.setIcon(null);
	}
	
	private ComponentPreset extractBulkhead() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.BULK_HEAD);
			props.put(ComponentPreset.OUTER_DIAMETER, bhOuterDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, bhDescTextField.getText());
			props.put(ComponentPreset.PARTNO, bhPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, bhLength.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, bhMass.getValue());
			if (bhImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(bhImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert bulkhead attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory bulkhead attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearBulkhead() {
		bhOuterDia.setValue(0);
		bhDescTextField.setText("");
		bhPartNoTextField.setText("");
		bhLength.setValue(0);
		bhMass.setValue(0);
		bhImage = null;
		bhImageBtn.setIcon(null);
	}
	
	private ComponentPreset extractCenteringRing() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.CENTERING_RING);
			props.put(ComponentPreset.OUTER_DIAMETER, crOuterDia.getValue());
			props.put(ComponentPreset.INNER_DIAMETER, crInnerDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, crDescTextField.getText());
			props.put(ComponentPreset.PARTNO, crPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, crThickness.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, crMass.getValue());
			if (crImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(crImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert centering ring attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory centering ring attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearCenteringRing() {
		crOuterDia.setValue(0);
		crInnerDia.setValue(0);
		crDescTextField.setText("");
		crPartNoTextField.setText("");
		crThickness.setValue(0);
		crMass.setValue(0);
		crImage = null;
		crImageBtn.setIcon(null);
	}
	
	public ComponentPreset extractEngineBlock() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
			props.put(ComponentPreset.OUTER_DIAMETER, ebOuterDia.getValue());
			props.put(ComponentPreset.INNER_DIAMETER, ebInnerDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, ebDescTextField.getText());
			props.put(ComponentPreset.PARTNO, ebPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, ebThickness.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, ebMass.getValue());
			if (ebImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(ebImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert engine block attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory engine block attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearEngineBlock() {
		ebOuterDia.setValue(0);
		ebInnerDia.setValue(0);
		ebDescTextField.setText("");
		ebPartNoTextField.setText("");
		ebThickness.setValue(0);
		ebMass.setValue(0);
		ebImage = null;
		ebImageBtn.setIcon(null);
	}
	
	public ComponentPreset extractLaunchLug() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.LAUNCH_LUG);
			props.put(ComponentPreset.OUTER_DIAMETER, llOuterDia.getValue());
			props.put(ComponentPreset.INNER_DIAMETER, llInnerDia.getValue());
			props.put(ComponentPreset.DESCRIPTION, llDescTextField.getText());
			props.put(ComponentPreset.PARTNO, llPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.LENGTH, llLength.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, llMass.getValue());
			if (llImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(llImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert launch lug attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory launch lug attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearLaunchLug() {
		llOuterDia.setValue(0);
		llInnerDia.setValue(0);
		llDescTextField.setText("");
		llPartNoTextField.setText("");
		llLength.setValue(0);
		llMass.setValue(0);
		llImage = null;
		llImageBtn.setIcon(null);
	}
	
	public ComponentPreset extractParachute() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.PARACHUTE);
			props.put(ComponentPreset.DIAMETER, pcDiameter.getValue());
			props.put(ComponentPreset.DESCRIPTION, pcDescTextField.getText());
			props.put(ComponentPreset.PARTNO, pcPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			if (!pcLineCount.getText().equals("")) {
				props.put(ComponentPreset.LINE_COUNT, Integer.parseInt(pcLineCount.getText()));
			}
			if (!pcSides.getText().equals("")) {
				props.put(ComponentPreset.SIDES, Integer.parseInt(pcSides.getText()));
			}
			props.put(ComponentPreset.LINE_LENGTH, pcLineLength.getValue());
			Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			material = (Material) pcLineMaterialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.LINE_MATERIAL, material);
			}
			props.put(ComponentPreset.MASS, pcMass.getValue());
			if (pcImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(pcImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert parachute attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory parachute attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearParachute() {
		ebOuterDia.setValue(0);
		ebInnerDia.setValue(0);
		ebDescTextField.setText("");
		ebPartNoTextField.setText("");
		ebThickness.setValue(0);
		ebMass.setValue(0);
		ebImage = null;
		ebImageBtn.setIcon(null);
	}
	
	public ComponentPreset extractStreamer() {
		TypedPropertyMap props = new TypedPropertyMap();
		try {
			props.put(ComponentPreset.TYPE, ComponentPreset.Type.STREAMER);
			props.put(ComponentPreset.DESCRIPTION, stDescTextField.getText());
			props.put(ComponentPreset.PARTNO, stPartNoTextField.getText());
			props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
			props.put(ComponentPreset.THICKNESS, stThickness.getValue());
			props.put(ComponentPreset.LENGTH, stLength.getValue());
			props.put(ComponentPreset.WIDTH, stWidth.getValue());
			final Material material = (Material) materialChooser.getSelectedItem();
			if (material != null) {
				props.put(ComponentPreset.MATERIAL, material);
			}
			else {
				JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
			props.put(ComponentPreset.MASS, stMass.getValue());
			if (stImage != null) {
				props.put(ComponentPreset.IMAGE, imageToByteArray(stImage.getImage()));
			}
			return ComponentPresetFactory.create(props);
		} catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(null, "Could not convert engine block attribute.", "Error", JOptionPane.ERROR_MESSAGE);
		} catch (InvalidComponentPresetException e) {
			JOptionPane.showMessageDialog(null, craftErrorMessage(e, "Mandatory engine block attribute not set."), "Error", JOptionPane.ERROR_MESSAGE);
		}
		return null;
	}
	
	private void clearStreamer() {
		stWidth.setValue(0);
		stLength.setValue(0);
		stDescTextField.setText("");
		stPartNoTextField.setText("");
		stThickness.setValue(0);
		stMass.setValue(0);
		stImage = null;
		stImageBtn.setIcon(null);
	}
	
	@Override
	public void itemStateChanged(ItemEvent evt) {
		CardLayout cl = (CardLayout) (componentOverlayPanel.getLayout());
		final String item = (String) evt.getItem();
		if (materialChooser != null && evt.getStateChange() == ItemEvent.SELECTED) {
			if (item.equals(trans.get(PARACHUTE_KEY)) || item.equals(trans.get(STREAMER_KEY))) {
				if (!((MaterialModel) materialChooser.getModel()).getType().equals(Material.Type.SURFACE)) {
					setMaterial(materialChooser, null, holder, Material.Type.SURFACE, ComponentPreset.MATERIAL);
				}
			}
			else {
				if (!((MaterialModel) materialChooser.getModel()).getType().equals(Material.Type.BULK)) {
					setMaterial(materialChooser, null, holder, Material.Type.BULK, ComponentPreset.MATERIAL);
				}
			}
		}
		cl.show(componentOverlayPanel, componentMap.get(item));
	}
	
	//Todo: I18N
	private String craftErrorMessage(InvalidComponentPresetException e, String baseMsg) {
		StringBuilder stringBuilder = new StringBuilder();
		List<String> invalids = e.getErrors();
		stringBuilder.append(baseMsg).append("\n");
		for (int i = 0; i < invalids.size(); i++) {
			String s = invalids.get(i);
			stringBuilder.append(s).append("\n");
		}
		
		return stringBuilder.toString();
	}
	
	/**
	 * Convert an image to a byte array in png format.
	 *
	 * @param originalImage
	 *
	 * @return
	 */
	private byte[] imageToByteArray(Image originalImage) {
		byte[] imageInByte = null;
		try {
			BufferedImage bi = imageToBufferedImage(originalImage);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bi, "png", baos);
			baos.flush();
			imageInByte = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			log.error("Could not read image.");
		}
		return imageInByte;
	}
	
	private BufferedImage imageToBufferedImage(final Image originalImage) {
		BufferedImage bi = new BufferedImage(originalImage.getWidth(null), originalImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
		
		Graphics2D g2 = bi.createGraphics();
		g2.drawImage(originalImage, 0, 0, null);
		return bi;
	}
	
	private BufferedImage byteArrayToImage(byte[] src) {
		// convert byte array back to BufferedImage
		InputStream in = new ByteArrayInputStream(src);
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			log.error("Could not convert image.");
		}
		return null;
	}
	
	private ImageIcon scaleImage(Image image, int targetDimension) {
		int width = image.getWidth(this);
		int height = image.getHeight(this);
		double ratio = 1.0;
		
		/*
		 * Determine how to scale the image. Since the accessory can expand
		 * vertically make sure we don't go larger than 150 when scaling
		 * vertically.
		 */
		if (width >= height) {
			ratio = (double) (targetDimension - 5) / width;
			width = targetDimension - 5;
			height = (int) (height * ratio);
		}
		else {
			if (getHeight() > 150) {
				ratio = (double) (targetDimension - 5) / height;
				height = targetDimension - 5;
				width = (int) (width * ratio);
			}
			else {
				ratio = (double) getHeight() / height;
				height = getHeight();
				width = (int) (width * ratio);
			}
		}
		
		return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_DEFAULT));
	}
	
	static class PresetInputVerifier extends InputVerifier {
		
		/**
		 * Matches user input against a regular expression.
		 */
		private Matcher matcher;
		
		PresetInputVerifier(final Pattern thePattern) {
			matcher = thePattern.matcher("");
		}
		
		/**
		 * Return true only if the untrimmed user input matches the regular expression provided to the constructor.
		 *
		 * @param aComponent must be an instance of JTextComponent.
		 */
		@Override
		public boolean verify(JComponent aComponent) {
			JTextComponent textComponent = (JTextComponent) aComponent;
			String text = textComponent.getText();
			matcher.reset(text);
			return matcher.matches();
		}
		
		/**
		 * Always returns <tt>true</tt>, in this implementation, such that focus can always transfer to another
		 * component whenever the validation fails.
		 * <p/>
		 * <P>If <tt>super.shouldYieldFocus</tt> returns <tt>false</tt>, then clear the text field.
		 *
		 * @param aComponent is a <tt>JTextComponent</tt>.
		 */
		@Override
		public boolean shouldYieldFocus(JComponent aComponent) {
			if (!super.shouldYieldFocus(aComponent)) {
				((JTextComponent) aComponent).setText("");
			}
			return true;
		}
	}
	
	class MaterialChooser extends JComboBox {
		
		public MaterialChooser() {
		}
		
		public MaterialChooser(MaterialModel model) {
			super(model);
		}
		
		/**
		 * Sets the data model that the <code>JComboBox</code> uses to obtain the list of items.
		 *
		 * @param aModel the <code>ComboBoxModel</code> that provides the displayed list of items
		 *
		 * @beaninfo bound: true description: Model that the combo box uses to get data to display.
		 */
		@Override
		public void setModel(final ComboBoxModel aModel) {
			if (getModel() instanceof MaterialModel) {
				MaterialModel old = (MaterialModel) getModel();
				old.removeListener();
			}
			super.setModel(aModel);
			
		}
	}
}
