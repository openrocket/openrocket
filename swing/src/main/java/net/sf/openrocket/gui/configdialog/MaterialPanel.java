package net.sf.openrocket.gui.configdialog;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.adaptors.MaterialModel;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.rocketcomponent.ExternalComponent;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

/**
 * Panel for configuring a component's material and finish properties.
 */
public class MaterialPanel extends JPanel {
    private static final Translator trans = Application.getTranslator();

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, String materialString, String finishString,
                         String partName,  List<Component> order) {
        super(new MigLayout());
        this.setBorder(BorderFactory.createTitledBorder(trans.get("MaterialPanel.title.Material")));

        JLabel label = new JLabel(materialString);
        //// The component material affects the weight of the component.
        label.setToolTipText(trans.get("MaterialPanel.lbl.ttip.ComponentMaterialAffects"));
        this.add(label, "spanx 4, wrap rel");

        JComboBox<Material> materialCombo = new JComboBox<>(new MaterialModel(this, component, type, partName));
        //// The component material affects the weight of the component.
        materialCombo.setToolTipText(trans.get("MaterialPanel.combo.ttip.ComponentMaterialAffects"));
        this.add(materialCombo, "spanx 4, growx, wrap paragraph");
        order.add(materialCombo);

        if (!(component instanceof ExternalComponent)) {
            return;
        }
        label = new JLabel(finishString);
        ////<html>The component finish affects the aerodynamic drag of the component.<br>
        String tip = trans.get("MaterialPanel.lbl.ComponentFinish.ttip.longA1")
                //// The value indicated is the average roughness height of the surface.
                + trans.get("MaterialPanel.lbl.ComponentFinish.ttip.longA2");
        label.setToolTipText(tip);
        this.add(label, "spanx 4, wmin 220lp, wrap rel");

        JComboBox<ExternalComponent.Finish> finishCombo = new JComboBox<ExternalComponent.Finish>(
                new EnumModel<>(component, "Finish"));
        finishCombo.setToolTipText(tip);
        this.add(finishCombo, "spanx 4, growx, split");
        order.add(finishCombo);

        //// Set for all
        JButton button = new SelectColorButton(trans.get("MaterialPanel.but.SetForAll"));
        //// Set this finish for all components of the rocket.
        button.setToolTipText(trans.get("MaterialPanel.but.SetForAll.ttip"));
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExternalComponent.Finish f = ((ExternalComponent) component).getFinish();
                try {
                    document.startUndo("Set rocket finish");

                    // Do changes
                    Iterator<RocketComponent> iter = component.getRoot().iterator();
                    while (iter.hasNext()) {
                        RocketComponent c = iter.next();
                        if (c instanceof ExternalComponent) {
                            ((ExternalComponent) c).setFinish(f);
                        }
                    }
                } finally {
                    document.stopUndo();
                }
            }
        });
        this.add(button);
        order.add(button);
    }

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, String partName, List<Component> order) {
        this(component, document, type, trans.get("MaterialPanel.lbl.ComponentMaterial"),
                trans.get("MaterialPanel.lbl.ComponentFinish"), partName, order);
    }

    public MaterialPanel(RocketComponent component, OpenRocketDocument document,
                         Material.Type type, List<Component> order) {
        this(component, document, type, trans.get("MaterialPanel.lbl.ComponentMaterial"),
                trans.get("MaterialPanel.lbl.ComponentFinish"), "Material", order);
    }
}
