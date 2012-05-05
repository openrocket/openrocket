package net.sf.openrocket.gui.preset;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.print.PrintUnit;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPresetFactory;
import net.sf.openrocket.preset.InvalidComponentPresetException;
import net.sf.openrocket.preset.TypedPropertyMap;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.startup.Application;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PresetEditorDialog extends JDialog implements ItemListener {

    private static Translator trans = Application.getTranslator();

    private static LogHelper log = Application.getLogger();

    private static final String NON_NEGATIVE_DECIMAL_FIELD = "(\\d){1,10}\\.(\\d){1,10}";

    /**
     * Input of non-negative decimals.
     */
    final PresetInputVerifier NON_NEGATIVE_DECIMAL =
            new PresetInputVerifier(Pattern.compile(NON_NEGATIVE_DECIMAL_FIELD));

    private final JPanel contentPanel = new JPanel();
    private JComboBox typeCombo;
    private JTextField mfgTextField;
    private JComboBox materialChooser;
    private JComboBox massUnitCombo;
    private JComboBox lenUnitCombo;

    private JTextField ncPartNoTextField;
    private JTextField ncDescTextField;
    private JTextField ncLengthTextField;
    private JCheckBox ncFilledCB;
    private JComboBox ncShapeCB;
    private JTextField ncAftDiaTextField;
    private JTextField ncAftShoulderDiaTextField;
    private JTextField ncAftShoulderLenTextField;
    private JTextField ncMassTextField;
    private ImageIcon ncImage;
    private JButton ncImageBtn;

    private JTextField trPartNoTextField;
    private JTextField trDescTextField;
    private JTextField trLengthTextField;
    private JTextField trAftDiaTextField;
    private JTextField trAftShoulderDiaTextField;
    private JTextField trAftShoulderLenTextField;
    private JTextField trForeDiaTextField;
    private JTextField trForeShoulderDiaTextField;
    private JTextField trForeShoulderLenTextField;
    private JTextField trMassTextField;
    private ImageIcon trImage;
    private JCheckBox trFilledCB;
    private JComboBox trShapeCB;
    private JButton trImageBtn;

    private JTextField btPartNoTextField;
    private JTextField btDescTextField;
    private JTextField btMassTextField;
    private JTextField btInnerDiaTextField;
    private JTextField btOuterDiaTextField;
    private JTextField btLengthTextField;
    private ImageIcon btImage;
    private JButton btImageBtn;

    private JTextField tcPartNoTextField;
    private JTextField tcDescTextField;
    private JTextField tcMassTextField;
    private JTextField tcInnerDiaTextField;
    private JTextField tcOuterDiaTextField;
    private JTextField tcLengthTextField;
    private ImageIcon tcImage;
    private JButton tcImageBtn;

    private JTextField bhPartNoTextField;
    private JTextField bhDescTextField;
    private JTextField bhOuterDiaTextField;
    private JTextField bhLengthTextField;
    private JTextField bhMassTextField;
    private ImageIcon bhImage;
    private JButton bhImageBtn;

    private JTextField crPartNoTextField;
    private JTextField crDescTextField;
    private JTextField crOuterDiaTextField;
    private JTextField crInnerDiaTextField;
    private JTextField crThicknessTextField;
    private JTextField crMassTextField;
    private ImageIcon crImage;
    private JButton crImageBtn;

    private JTextField ebPartNoTextField;
    private JTextField ebDescTextField;
    private JTextField ebOuterDiaTextField;
    private JTextField ebInnerDiaTextField;
    private JTextField ebThicknessTextField;
    private JTextField ebMassTextField;
    private ImageIcon ebImage;
    private JButton ebImageBtn;

    private final JFileChooser imageChooser = createImageChooser();

    private JPanel componentOverlayPanel;

    private PresetResultListener resultListener;

    private static Map<String, String> componentMap = new HashMap<String, String>();
    private static Map<String, PrintUnit> lengthMap = new HashMap<String, PrintUnit>();

    private static final String NOSE_CONE_KEY = "NoseCone.NoseCone";
    private static final String BODY_TUBE_KEY = "BodyTube.BodyTube";
    private static final String TUBE_COUPLER_KEY = "TubeCoupler.TubeCoupler";
    private static final String TRANSITION_KEY = "Transition.Transition";
    private static final String CR_KEY = "ComponentIcons.Centeringring";
    private static final String BULKHEAD_KEY = "Bulkhead.Bulkhead";
    private static final String EB_KEY = "ComponentIcons.Engineblock";


    static {
        componentMap.put(trans.get(NOSE_CONE_KEY), "NOSECONE");
        componentMap.put(trans.get(BODY_TUBE_KEY), "BODYTUBE");
        componentMap.put(trans.get(TUBE_COUPLER_KEY), "TUBECOUPLER");
        componentMap.put(trans.get(TRANSITION_KEY), "TRANSITION");
        componentMap.put(trans.get(CR_KEY), "CENTERINGRING");
        componentMap.put(trans.get(BULKHEAD_KEY), "BULKHEAD");
        componentMap.put(trans.get(EB_KEY), "ENGINEBLOCK");

        lengthMap.put("m", PrintUnit.METERS);
        lengthMap.put("cm", PrintUnit.CENTIMETERS);
        lengthMap.put("mm", PrintUnit.MILLIMETERS);
        lengthMap.put("in", PrintUnit.INCHES);
        lengthMap.put("ft", PrintUnit.FOOT);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            Application.setPreferences(new SwingPreferences());
            PresetEditorDialog dialog = new PresetEditorDialog();
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public PresetEditorDialog() {
        this(new PresetResultListener() {
            @Override
            public void notifyResult(final ComponentPreset preset) {
            }
        });
    }

    public PresetEditorDialog(PresetResultListener theCallback) {
        this(theCallback, null);
        typeCombo.setEditable(true);
    }

    public PresetEditorDialog(PresetResultListener theCallback, ComponentPreset toEdit) {
        resultListener = theCallback;
        getContentPane().setMinimumSize(new Dimension(200, 200));
        setBounds(100, 100, 720, 610);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new MigLayout("", "[][grow][94.00,grow][232.0,grow][130.00][grow]", "[][][20.00,grow][grow]"));
        JLabel lblManufacturer = new JLabel("Manufacturer:");
        contentPanel.add(lblManufacturer, "cell 2 0,alignx left,aligny center");

        mfgTextField = new JTextField();
        contentPanel.add(mfgTextField, "cell 3 0,growx");
        mfgTextField.setColumns(10);

        JLabel lenUnitLabel = new JLabel("Length Unit:");
        contentPanel.add(lenUnitLabel, "cell 4 0,alignx left,aligny center");

        lenUnitCombo = new JComboBox();
        lenUnitCombo.setModel(new DefaultComboBoxModel(new String[]{"m", "cm", "mm", "in", "ft"}));
        contentPanel.add(lenUnitCombo, "cell 5 0,growx");

        JLabel typeLabel = new JLabel("Type:");
        contentPanel.add(typeLabel, "cell 2 1,alignx left,aligny center");

        typeCombo = new JComboBox();
        typeCombo.addItemListener(this);
        typeCombo.setModel(new DefaultComboBoxModel(new String[]{
                trans.get(NOSE_CONE_KEY), trans.get(BODY_TUBE_KEY), trans.get(TUBE_COUPLER_KEY), trans.get(TRANSITION_KEY),
                trans.get(CR_KEY), trans.get(BULKHEAD_KEY), trans.get(EB_KEY)}));
        contentPanel.add(typeCombo, "cell 3 1,growx");

        JLabel massUnitLabel = new JLabel("Mass Unit:");
        contentPanel.add(massUnitLabel, "cell 4 1,alignx left,aligny center");

        massUnitCombo = new JComboBox();
        massUnitCombo.setModel(new DefaultComboBoxModel(new String[]{"kg", "g", "oz", "lb"}));
        contentPanel.add(massUnitCombo, "cell 5 1,growx");

        JLabel bhMaterialLabel = new JLabel("Material:");
        contentPanel.add(bhMaterialLabel, "cell 2 2, alignx left");

        materialChooser = new JComboBox(new MaterialModel(this, Material.Type.BULK));
        contentPanel.add(materialChooser, "cell 3 2,growx");

        componentOverlayPanel = new JPanel();
        contentPanel.add(componentOverlayPanel, "cell 1 3 5 2,grow");
        componentOverlayPanel.setLayout(new CardLayout(0, 0));

        {
            JPanel ncPanel = new JPanel();
            componentOverlayPanel.add(ncPanel, "NOSECONE");
            ncPanel.setLayout(new MigLayout("", "[61px][159.00,grow][35.00][109.00,grow][189.00,grow][grow]", "[16px][][][][][]"));
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

            JLabel ncMaterialLabel = new JLabel("Material:");
            ncPanel.add(ncMaterialLabel, "cell 0 1,alignx left");

            JLabel ncMassLabel = new JLabel("Mass:");
            ncPanel.add(ncMassLabel, "cell 3 1,alignx left");

            ncMassTextField = new JTextField();
            ncMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ncPanel.add(ncMassTextField, "cell 4 1,growx");
            ncMassTextField.setColumns(10);

            JLabel ncShapeLabel = new JLabel("Shape:");
            ncPanel.add(ncShapeLabel, "cell 0 2,alignx left");

            ncShapeCB = new JComboBox();
            ncShapeCB.setModel(new DefaultComboBoxModel(new String[]{Transition.Shape.OGIVE.getName(),
                    Transition.Shape.CONICAL.getName(), Transition.Shape.PARABOLIC.getName(),
                    Transition.Shape.ELLIPSOID.getName(), Transition.Shape.HAACK.getName()}));
            ncPanel.add(ncShapeCB, "cell 1 2,growx");

            JLabel ncLengthLabel = new JLabel("Length:");
            ncPanel.add(ncLengthLabel, "cell 3 2,alignx left");

            ncLengthTextField = new JTextField();
            ncLengthTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ncPanel.add(ncLengthTextField, "cell 4 2,growx");
            ncLengthTextField.setColumns(10);

            JLabel ncAftDiaLabel = new JLabel("Aft Dia.:");
            ncPanel.add(ncAftDiaLabel, "cell 0 3,alignx left, aligny top");

            ncAftDiaTextField = new JTextField();
            ncAftDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ncPanel.add(ncAftDiaTextField, "cell 1 3,growx, aligny top");
            ncAftDiaTextField.setColumns(10);

            JLabel ncAftShoulderLenLabel = new JLabel("Aft Shoulder Len:");
            ncPanel.add(ncAftShoulderLenLabel, "cell 0 4,alignx left, aligny top");

            ncAftShoulderLenTextField = new JTextField();
            ncAftShoulderLenTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ncPanel.add(ncAftShoulderLenTextField, "cell 1 4,growx,aligny top");
            ncAftShoulderLenTextField.setColumns(10);

            JLabel ncAftShoulderDiaLabel = new JLabel("Aft Shoulder Dia.:");
            ncPanel.add(ncAftShoulderDiaLabel, "cell 0 5,alignx left, aligny top");

            ncAftShoulderDiaTextField = new JTextField();
            ncAftShoulderDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ncPanel.add(ncAftShoulderDiaTextField, "cell 1 5,growx, aligny top");
            ncAftShoulderDiaTextField.setColumns(10);

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
            trPanel.setLayout(new MigLayout("", "[][grow][][grow]", "[][][28.00][31.00][][]"));

            JLabel trPartNoLabel = new JLabel("Part No:");
            trPanel.add(trPartNoLabel, "cell 0 0,alignx left");

            trPartNoTextField = new JTextField();
            trPanel.add(trPartNoTextField, "cell 1 0,growx");
            trPartNoTextField.setColumns(10);

            JLabel trDescLabel = new JLabel("Description:");
            trPanel.add(trDescLabel, "cell 2 0,alignx left");

            trDescTextField = new JTextField();
            trPanel.add(trDescTextField, "cell 3 0,growx");
            trDescTextField.setColumns(10);

            trFilledCB = new JCheckBox("Filled");
            trPanel.add(trFilledCB, "cell 1 1");

            JLabel trMassLabel = new JLabel("Mass:");
            trPanel.add(trMassLabel, "cell 2 1,alignx left");

            trMassTextField = new JTextField();
            trMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trMassTextField, "cell 3 1,growx");
            trMassTextField.setColumns(10);

            JLabel trShapeLabel = new JLabel("Shape:");
            trPanel.add(trShapeLabel, "cell 0 2,alignx left");

            trShapeCB = new JComboBox();
            trShapeCB.setModel(new DefaultComboBoxModel(new String[]{Transition.Shape.OGIVE.getName(),
                    Transition.Shape.CONICAL.getName(), Transition.Shape.PARABOLIC.getName(),
                    Transition.Shape.ELLIPSOID.getName(), Transition.Shape.HAACK.getName()}));
            trPanel.add(trShapeCB, "cell 1 2,growx");

            JLabel trLengthLabel = new JLabel("Length:");
            trPanel.add(trLengthLabel, "cell 2 2,alignx left");

            trLengthTextField = new JTextField();
            trLengthTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trLengthTextField, "cell 3 2,growx");
            trLengthTextField.setColumns(10);

            JLabel trAftDiaLabel = new JLabel("Aft Dia.:");
            trPanel.add(trAftDiaLabel, "cell 0 3,alignx left");

            trAftDiaTextField = new JTextField();
            trAftDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trAftDiaTextField, "cell 1 3,growx");
            trAftDiaTextField.setColumns(10);

            JLabel trForeDiaLabel = new JLabel("Fore Dia.:");
            trPanel.add(trForeDiaLabel, "cell 2 3,alignx left");

            trForeDiaTextField = new JTextField();
            trForeDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trForeDiaTextField, "cell 3 3,growx");
            trForeDiaTextField.setColumns(10);

            JLabel trAftShouldDiaLabel = new JLabel("Aft Shoulder Dia.:");
            trPanel.add(trAftShouldDiaLabel, "cell 0 4,alignx left");

            trAftShoulderDiaTextField = new JTextField();
            trAftShoulderDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trAftShoulderDiaTextField, "cell 1 4,growx");
            trAftShoulderDiaTextField.setColumns(10);

            JLabel trForeShouldDiaLabel = new JLabel("Fore Shoulder Dia.:");
            trPanel.add(trForeShouldDiaLabel, "cell 2 4,alignx left");

            trForeShoulderDiaTextField = new JTextField();
            trForeShoulderDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trForeShoulderDiaTextField, "cell 3 4,growx");
            trForeShoulderDiaTextField.setColumns(10);

            JLabel trAftShoulderLenLabel = new JLabel("Aft Shoulder Len.:");
            trPanel.add(trAftShoulderLenLabel, "cell 0 5,alignx left");

            trAftShoulderLenTextField = new JTextField();
            trAftShoulderLenTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trAftShoulderLenTextField, "cell 1 5,growx");
            trAftShoulderLenTextField.setColumns(10);

            JLabel lblForeShoulderLen = new JLabel("Fore Shoulder Len.:");
            trPanel.add(lblForeShoulderLen, "cell 2 5,alignx left");

            trForeShoulderLenTextField = new JTextField();
            trForeShoulderLenTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            trPanel.add(trForeShoulderLenTextField, "cell 3 5,growx");
            trForeShoulderLenTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            trPanel.add(panel, "cell 3 6");
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
            btPanel.add(btDescLabel, "cell 2 0,alignx left");

            btDescTextField = new JTextField();
            btPanel.add(btDescTextField, "cell 3 0,growx");
            btDescTextField.setColumns(10);

            JLabel btMassLabel = new JLabel("Mass:");
            btPanel.add(btMassLabel, "cell 2 1,alignx left");

            btMassTextField = new JTextField();
            btMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            btPanel.add(btMassTextField, "cell 3 1,growx");
            btMassTextField.setColumns(10);

            JLabel btInnerDiaLabel = new JLabel("Inner Dia.:");
            btPanel.add(btInnerDiaLabel, "cell 0 2,alignx left");

            btInnerDiaTextField = new JTextField();
            btInnerDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            btPanel.add(btInnerDiaTextField, "cell 1 2,growx");
            btInnerDiaTextField.setColumns(10);

            JLabel btOuterDiaLabel = new JLabel("Outer Dia.:");
            btPanel.add(btOuterDiaLabel, "cell 2 2,alignx left");

            btOuterDiaTextField = new JTextField();
            btOuterDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            btPanel.add(btOuterDiaTextField, "cell 3 2,growx");
            btOuterDiaTextField.setColumns(10);

            JLabel btLengthLabel = new JLabel("Length:");
            btPanel.add(btLengthLabel, "cell 0 1,alignx left");

            btLengthTextField = new JTextField();
            btLengthTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            btPanel.add(btLengthTextField, "cell 1 1,growx");
            btLengthTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            btPanel.add(panel, "cell 3 3");
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
            tcPanel.add(tcDescLabel, "cell 2 0,alignx left");

            tcDescTextField = new JTextField();
            tcPanel.add(tcDescTextField, "cell 3 0,growx");
            tcDescTextField.setColumns(10);

            JLabel tcMassLabel = new JLabel("Mass:");
            tcPanel.add(tcMassLabel, "cell 2 1,alignx left");

            tcMassTextField = new JTextField();
            tcMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            tcPanel.add(tcMassTextField, "cell 3 1,growx");
            tcMassTextField.setColumns(10);

            JLabel tcInnerDiaLabel = new JLabel("Inner Dia.:");
            tcPanel.add(tcInnerDiaLabel, "cell 0 2,alignx left, aligny top");

            tcInnerDiaTextField = new JTextField();
            tcInnerDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            tcPanel.add(tcInnerDiaTextField, "cell 1 2,growx, aligny top");
            tcInnerDiaTextField.setColumns(10);

            JLabel tcOuterDiaLabel = new JLabel("Outer Dia.:");
            tcPanel.add(tcOuterDiaLabel, "cell 2 2,alignx left, aligny top");

            tcOuterDiaTextField = new JTextField();
            tcOuterDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            tcPanel.add(tcOuterDiaTextField, "cell 3 2,growx, aligny top");
            tcOuterDiaTextField.setColumns(10);

            JLabel tcLengthLabel = new JLabel("Length:");
            tcPanel.add(tcLengthLabel, "cell 0 1,alignx left");

            tcLengthTextField = new JTextField();
            tcLengthTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            tcPanel.add(tcLengthTextField, "cell 1 1,growx");
            tcLengthTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            tcPanel.add(panel, "cell 3 3");
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
            bhPanel.add(bhDescLabel, "cell 2 0,alignx left");

            bhDescTextField = new JTextField();
            bhPanel.add(bhDescTextField, "cell 3 0,growx");
            bhDescTextField.setColumns(10);

            JLabel bhOuterDiaLabel = new JLabel("Outer Dia.:");
            bhPanel.add(bhOuterDiaLabel, "cell 0 2,alignx left, aligny top");

            bhOuterDiaTextField = new JTextField();
            bhPanel.add(bhOuterDiaTextField, "cell 1 2,growx, aligny top");
            bhOuterDiaTextField.setColumns(10);

            JLabel bhMassLabel = new JLabel("Mass:");
            bhPanel.add(bhMassLabel, "cell 2 1,alignx left");

            bhMassTextField = new JTextField();
            bhPanel.add(bhMassTextField, "cell 3 1,growx");
            bhMassTextField.setColumns(10);

            JLabel bhLengthLabel = new JLabel("Thickness:");
            bhPanel.add(bhLengthLabel, "cell 0 1,alignx left");

            bhLengthTextField = new JTextField();
            bhPanel.add(bhLengthTextField, "cell 1 1,growx");
            bhLengthTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            bhPanel.add(panel, "cell 3 2");
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
            crPanel.add(crDescLabel, "cell 2 0,alignx left");

            crDescTextField = new JTextField();
            crPanel.add(crDescTextField, "cell 3 0, growx");
            crDescTextField.setColumns(10);

            JLabel crMassLabel = new JLabel("Mass:");
            crPanel.add(crMassLabel, "cell 2 1,alignx left");

            crMassTextField = new JTextField();
            crMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            crPanel.add(crMassTextField, "cell 3 1, growx");
            crMassTextField.setColumns(10);

            JLabel crOuterDiaLabel = new JLabel("Outer Dia.:");
            crPanel.add(crOuterDiaLabel, "cell 0 2,alignx left");

            crOuterDiaTextField = new JTextField();
            crOuterDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            crPanel.add(crOuterDiaTextField, "cell 1 2, growx");
            crOuterDiaTextField.setColumns(10);

            JLabel crInnerDiaLabel = new JLabel("Inner Dia.:");
            crPanel.add(crInnerDiaLabel, "cell 2 2,alignx left");

            crInnerDiaTextField = new JTextField();
            crInnerDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            crPanel.add(crInnerDiaTextField, "cell 3 2, growx");
            crInnerDiaTextField.setColumns(10);

            JLabel crThicknessLabel = new JLabel("Thickness:");
            crPanel.add(crThicknessLabel, "cell 0 1,alignx left");

            crThicknessTextField = new JTextField();
            crThicknessTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            crPanel.add(crThicknessTextField, "cell 1 1, growx");
            crThicknessTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            crPanel.add(panel, "cell 3 3");
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
            ebPanel.add(ebDescLabel, "cell 2 0,alignx left");

            ebDescTextField = new JTextField();
            ebPanel.add(ebDescTextField, "cell 3 0,growx");
            ebDescTextField.setColumns(10);

            JLabel ebMassLabel = new JLabel("Mass:");
            ebPanel.add(ebMassLabel, "cell 2 1,alignx left");

            ebMassTextField = new JTextField();
            ebMassTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ebPanel.add(ebMassTextField, "cell 3 1,growx");
            ebMassTextField.setColumns(10);

            JLabel ebOuterDiaLabel = new JLabel("Outer Dia.:");
            ebPanel.add(ebOuterDiaLabel, "cell 0 2,alignx left");

            ebOuterDiaTextField = new JTextField();
            ebOuterDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ebPanel.add(ebOuterDiaTextField, "cell 1 2,growx");
            ebOuterDiaTextField.setColumns(10);

            JLabel ebInnerDiaLabel = new JLabel("Inner Dia.:");
            ebPanel.add(ebInnerDiaLabel, "cell 2 2,alignx left");

            ebInnerDiaTextField = new JTextField();
            ebInnerDiaTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ebPanel.add(ebInnerDiaTextField, "cell 3 2,growx");
            ebInnerDiaTextField.setColumns(10);

            JLabel ebThicknessLabel = new JLabel("Thickness:");
            ebPanel.add(ebThicknessLabel, "cell 0 1,alignx left");

            ebThicknessTextField = new JTextField();
            ebThicknessTextField.setInputVerifier(NON_NEGATIVE_DECIMAL);
            ebPanel.add(ebThicknessTextField, "cell 1 1,growx");
            ebThicknessTextField.setColumns(10);

            JPanel panel = new JPanel();
            panel.setMinimumSize(new Dimension(200, 200));
            ebPanel.add(panel, "cell 3 3");
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
            public void actionPerformed(ActionEvent event) {
                saveResult();
                dispose();
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton, "cell 1 0,alignx left,aligny top");
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton("Close");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                dispose();
            }
        });
        cancelButton.setActionCommand("Close");
        buttonPane.add(cancelButton, "cell 6 0,alignx right,aligny top");

        if (toEdit != null) {
            fillEditor(toEdit);
            typeCombo.setEditable(false);
        }
        else {
            typeCombo.setEditable(true);
        }
    }

    private JFileChooser createImageChooser() {
        final JFileChooser chooser = new JFileChooser();
        ImagePreviewPanel preview = new ImagePreviewPanel();
        chooser.setAccessory(preview);
        chooser.addPropertyChangeListener(preview);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(".png") || f.getName().endsWith(".jpg");
            }

            @Override
            public String getDescription() {
                return "Image Files";
            }
        });
        return chooser;
    }

    private void fillEditor(ComponentPreset preset) {
        ComponentPreset.Type t = preset.getType();

        mfgTextField.setText(preset.get(ComponentPreset.MANUFACTURER).getDisplayName());
        materialChooser.getModel().setSelectedItem(preset.get(ComponentPreset.MATERIAL));
        switch (t) {
            case BODY_TUBE:
                typeCombo.setSelectedItem(trans.get(BODY_TUBE_KEY));
                btDescTextField.setText(preset.get(ComponentPreset.DESCRIPTION));

                if (preset.has(ComponentPreset.INNER_DIAMETER)) {
                    btInnerDiaTextField.setText(preset.get(ComponentPreset.INNER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    btLengthTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    btMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
                    btOuterDiaTextField.setText(preset.get(ComponentPreset.OUTER_DIAMETER).toString());
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
                    bhLengthTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    bhMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
                    bhOuterDiaTextField.setText(preset.get(ComponentPreset.OUTER_DIAMETER).toString());
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
                    crInnerDiaTextField.setText(preset.get(ComponentPreset.INNER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    crThicknessTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    crMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
                    crOuterDiaTextField.setText(preset.get(ComponentPreset.OUTER_DIAMETER).toString());
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
                    ebInnerDiaTextField.setText(preset.get(ComponentPreset.INNER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    ebThicknessTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    ebMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
                    ebOuterDiaTextField.setText(preset.get(ComponentPreset.OUTER_DIAMETER).toString());
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
                    ncAftDiaTextField.setText(preset.get(ComponentPreset.AFT_OUTER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
                    ncAftShoulderDiaTextField.setText(preset.get(ComponentPreset.AFT_SHOULDER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
                    ncAftShoulderLenTextField.setText(preset.get(ComponentPreset.AFT_SHOULDER_LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    ncMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.SHAPE)) {
                    ncShapeCB.setSelectedItem(preset.get(ComponentPreset.SHAPE).toString());
                }
                if (preset.has(ComponentPreset.FILLED)) {
                    ncFilledCB.setSelected((preset.get(ComponentPreset.FILLED)));
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    ncLengthTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
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
                    trAftDiaTextField.setText(preset.get(ComponentPreset.AFT_OUTER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.AFT_SHOULDER_DIAMETER)) {
                    trAftShoulderDiaTextField.setText(preset.get(ComponentPreset.AFT_SHOULDER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.AFT_SHOULDER_LENGTH)) {
                    trAftShoulderLenTextField.setText(preset.get(ComponentPreset.AFT_SHOULDER_LENGTH).toString());
                }
                if (preset.has(ComponentPreset.FORE_OUTER_DIAMETER)) {
                    trForeDiaTextField.setText(preset.get(ComponentPreset.FORE_OUTER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.FORE_SHOULDER_DIAMETER)) {
                    trForeShoulderDiaTextField.setText(preset.get(ComponentPreset.FORE_SHOULDER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.FORE_SHOULDER_LENGTH)) {
                    trForeShoulderLenTextField.setText(preset.get(ComponentPreset.FORE_SHOULDER_LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    trMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.SHAPE)) {
                    trShapeCB.setSelectedItem(preset.get(ComponentPreset.SHAPE).toString());
                }
                if (preset.has(ComponentPreset.FILLED)) {
                    trFilledCB.setSelected((preset.get(ComponentPreset.FILLED)));
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    trLengthTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
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
                    tcInnerDiaTextField.setText(preset.get(ComponentPreset.INNER_DIAMETER).toString());
                }
                if (preset.has(ComponentPreset.LENGTH)) {
                    tcLengthTextField.setText(preset.get(ComponentPreset.LENGTH).toString());
                }
                if (preset.has(ComponentPreset.MASS)) {
                    tcMassTextField.setText(preset.get(ComponentPreset.MASS).toString());
                }
                if (preset.has(ComponentPreset.OUTER_DIAMETER)) {
                    tcOuterDiaTextField.setText(preset.get(ComponentPreset.OUTER_DIAMETER).toString());
                }
                tcPartNoTextField.setText(preset.get(ComponentPreset.PARTNO));
                if (preset.has(ComponentPreset.IMAGE)) {
                    tcImage = new ImageIcon(byteArrayToImage(preset.get(ComponentPreset.IMAGE)));
                    tcImageBtn.setIcon(tcImage);
                }
                break;
            default:
        }
    }

    private void saveResult() {
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
        resultListener.notifyResult(result);
    }

    private ComponentPreset extractNoseCone() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());

            props.put(ComponentPreset.TYPE, ComponentPreset.Type.NOSE_CONE);
            if (!ncAftDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(ncAftDiaTextField.getText())));
            }
            if (!ncAftShoulderDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, lpu.toMeters(Double.parseDouble(ncAftShoulderDiaTextField.getText())));
            }
            if (!ncAftShoulderLenTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_SHOULDER_LENGTH, lpu.toMeters(Double.parseDouble(ncAftShoulderLenTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, ncDescTextField.getText());
            props.put(ComponentPreset.PARTNO, ncPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!ncLengthTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(ncLengthTextField.getText())));
            }
            props.put(ComponentPreset.SHAPE, Transition.Shape.toShape((String) ncShapeCB.getSelectedItem()));
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!ncMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(ncMassTextField.getText())));
            }
            props.put(ComponentPreset.FILLED, ncFilledCB.isSelected());
            if (ncImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(ncImage.getImage()));
            }

            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert nose cone attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory nose cone attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearNoseCone() {
        ncAftDiaTextField.setText("");
        ncAftShoulderDiaTextField.setText("");
        ncAftShoulderLenTextField.setText("");
        ncDescTextField.setText("");
        ncPartNoTextField.setText("");
        ncLengthTextField.setText("");
        ncMassTextField.setText("");
        ncFilledCB.setSelected(false);
        ncImage = null;
        ncImageBtn.setIcon(null);
    }

    private ComponentPreset extractTransition() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.TRANSITION);
            if (!trAftDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(trAftDiaTextField.getText())));
            }
            if (!trAftShoulderDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_SHOULDER_DIAMETER, lpu.toMeters(Double.parseDouble(trAftShoulderDiaTextField.getText())));
            }
            if (!trAftShoulderLenTextField.getText().equals("")) {
                props.put(ComponentPreset.AFT_SHOULDER_LENGTH, lpu.toMeters(Double.parseDouble(trAftShoulderLenTextField.getText())));
            }
            if (!trForeDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.FORE_OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(trForeDiaTextField.getText())));
            }
            if (!trForeShoulderDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.FORE_SHOULDER_DIAMETER, lpu.toMeters(Double.parseDouble(trForeShoulderDiaTextField.getText())));
            }
            if (!trForeShoulderLenTextField.getText().equals("")) {
                props.put(ComponentPreset.FORE_SHOULDER_LENGTH, lpu.toMeters(Double.parseDouble(trForeShoulderLenTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, trDescTextField.getText());
            props.put(ComponentPreset.PARTNO, trPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));

            if (!trLengthTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(trLengthTextField.getText())));
            }
            props.put(ComponentPreset.SHAPE, Transition.Shape.toShape((String) trShapeCB.getSelectedItem()));
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!trMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(trMassTextField.getText())));
            }
            props.put(ComponentPreset.FILLED, trFilledCB.isSelected());
            if (trImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(trImage.getImage()));
            }

            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert transition attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory transition attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearTransition() {
        trAftDiaTextField.setText("");
        trAftShoulderDiaTextField.setText("");
        trAftShoulderLenTextField.setText("");
        trForeDiaTextField.setText("");
        trForeShoulderDiaTextField.setText("");
        trForeShoulderLenTextField.setText("");
        trDescTextField.setText("");
        trPartNoTextField.setText("");
        trLengthTextField.setText("");
        trMassTextField.setText("");
        trFilledCB.setSelected(false);
        trImage = null;
        trImageBtn.setIcon(null);
    }

    private ComponentPreset extractBodyTube() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.BODY_TUBE);
            if (!btOuterDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(btOuterDiaTextField.getText())));
            }
            if (!btInnerDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.INNER_DIAMETER, lpu.toMeters(Double.parseDouble(btInnerDiaTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, btDescTextField.getText());
            props.put(ComponentPreset.PARTNO, btPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!btLengthTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(btLengthTextField.getText())));
            }
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!btMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(btMassTextField.getText())));
            }
            if (btImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(btImage.getImage()));
            }
            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            nfe.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not convert body tube attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory body tube attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearBodyTube() {
        btOuterDiaTextField.setText("");
        btInnerDiaTextField.setText("");
        btDescTextField.setText("");
        btPartNoTextField.setText("");
        btLengthTextField.setText("");
        btMassTextField.setText("");
        btImage = null;
        btImageBtn.setIcon(null);
    }

    public ComponentPreset extractTubeCoupler() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.TUBE_COUPLER);
            if (!tcOuterDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(tcOuterDiaTextField.getText())));
            }
            if (!tcInnerDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.INNER_DIAMETER, lpu.toMeters(Double.parseDouble(tcInnerDiaTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, tcDescTextField.getText());
            props.put(ComponentPreset.PARTNO, tcPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!tcLengthTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(tcLengthTextField.getText())));
            }
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!tcMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(tcMassTextField.getText())));
            }
            if (tcImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(tcImage.getImage()));
            }

            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert tube coupler attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory tube coupler attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearTubeCoupler() {
        tcOuterDiaTextField.setText("");
        tcInnerDiaTextField.setText("");
        tcDescTextField.setText("");
        tcPartNoTextField.setText("");
        tcLengthTextField.setText("");
        tcMassTextField.setText("");
        tcImage = null;
        tcImageBtn.setIcon(null);
    }

    private ComponentPreset extractBulkhead() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.BULK_HEAD);
            if (!bhOuterDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(bhOuterDiaTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, bhDescTextField.getText());
            props.put(ComponentPreset.PARTNO, bhPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!bhLengthTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(bhLengthTextField.getText())));
            }
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!bhMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(bhMassTextField.getText())));
            }
            if (bhImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(bhImage.getImage()));
            }
            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert bulkhead attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory bulkhead attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearBulkhead() {
        bhOuterDiaTextField.setText("");
        bhDescTextField.setText("");
        bhPartNoTextField.setText("");
        bhLengthTextField.setText("");
        bhMassTextField.setText("");
        bhImage = null;
        bhImageBtn.setIcon(null);
    }

    private ComponentPreset extractCenteringRing() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.CENTERING_RING);
            if (!crOuterDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(crOuterDiaTextField.getText())));
            }
            if (!crInnerDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.INNER_DIAMETER, lpu.toMeters(Double.parseDouble(crInnerDiaTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, crDescTextField.getText());
            props.put(ComponentPreset.PARTNO, crPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!crThicknessTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(crThicknessTextField.getText())));
            }
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!crMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(crMassTextField.getText())));
            }
            if (crImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(crImage.getImage()));
            }
            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert centering ring attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory centering ring attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearCenteringRing() {
        crOuterDiaTextField.setText("");
        crInnerDiaTextField.setText("");
        crDescTextField.setText("");
        crPartNoTextField.setText("");
        crThicknessTextField.setText("");
        crMassTextField.setText("");
        crImage = null;
        crImageBtn.setIcon(null);
    }

    public ComponentPreset extractEngineBlock() {
        TypedPropertyMap props = new TypedPropertyMap();
        try {
            PrintUnit lpu = lengthMap.get(lenUnitCombo.getSelectedItem());
            props.put(ComponentPreset.TYPE, ComponentPreset.Type.ENGINE_BLOCK);
            if (!ebOuterDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.OUTER_DIAMETER, lpu.toMeters(Double.parseDouble(ebOuterDiaTextField.getText())));
            }
            if (!ebInnerDiaTextField.getText().equals("")) {
                props.put(ComponentPreset.INNER_DIAMETER, lpu.toMeters(Double.parseDouble(ebInnerDiaTextField.getText())));
            }
            props.put(ComponentPreset.DESCRIPTION, ebDescTextField.getText());
            props.put(ComponentPreset.PARTNO, ebPartNoTextField.getText());
            props.put(ComponentPreset.MANUFACTURER, Manufacturer.getManufacturer(mfgTextField.getText()));
            if (!ebThicknessTextField.getText().equals("")) {
                props.put(ComponentPreset.LENGTH, lpu.toMeters(Double.parseDouble(ebThicknessTextField.getText())));
            }
            final Material material = (Material) materialChooser.getSelectedItem();
            if (material != null) {
                props.put(ComponentPreset.MATERIAL, material);
            }
            else {
                JOptionPane.showMessageDialog(null, "A material must be selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return null;
            }
            if (!ebMassTextField.getText().equals("")) {
                props.put(ComponentPreset.MASS, lpu.toMeters(Double.parseDouble(ebMassTextField.getText())));
            }
            if (ebImage != null) {
                props.put(ComponentPreset.IMAGE, imageToByteArray(ebImage.getImage()));
            }
            return ComponentPresetFactory.create(props);
        }
        catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(null, "Could not convert engine block attribute.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        catch (InvalidComponentPresetException e) {
            JOptionPane.showMessageDialog(null, "Mandatory engine block attribute not set.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }

    private void clearEngineBlock() {
        ebOuterDiaTextField.setText("");
        ebInnerDiaTextField.setText("");
        ebDescTextField.setText("");
        ebPartNoTextField.setText("");
        ebThicknessTextField.setText("");
        ebMassTextField.setText("");
        ebImage = null;
        ebImageBtn.setIcon(null);
    }

    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout) (componentOverlayPanel.getLayout());
        cl.show(componentOverlayPanel, componentMap.get((String) evt.getItem()));

    }

    /**
     * Convert an image to a byte array in png format.
     *
     * @param originalImage
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
        }
        catch (IOException e) {
            log.error("Could not read image.");
        }
        return imageInByte;
    }

    private BufferedImage imageToBufferedImage(final Image originalImage) {
        BufferedImage bi = new BufferedImage(
                originalImage.getWidth(null),
                originalImage.getHeight(null),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g2 = bi.createGraphics();
        g2.drawImage(originalImage, 0, 0, null);
        return bi;
    }

    private BufferedImage byteArrayToImage(byte[] src) {
        // convert byte array back to BufferedImage
        InputStream in = new ByteArrayInputStream(src);
        try {
            return ImageIO.read(in);
        }
        catch (IOException e) {
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
        public boolean verify(JComponent aComponent) {
            JTextComponent textComponent = (JTextComponent) aComponent;
            matcher.reset(textComponent.getText());
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
}
