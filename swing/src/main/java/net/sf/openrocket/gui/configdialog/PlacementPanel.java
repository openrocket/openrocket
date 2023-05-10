package net.sf.openrocket.gui.configdialog;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.SpinnerEditor;
import net.sf.openrocket.gui.adaptors.DoubleModel;
import net.sf.openrocket.gui.adaptors.EnumModel;
import net.sf.openrocket.gui.components.BasicSlider;
import net.sf.openrocket.gui.components.UnitSelector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.position.AxialMethod;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;
import java.util.List;

/**
 * Panel for configuring a component's placement relative to its parent.
 */
public class PlacementPanel extends JPanel {
    private static final Translator trans = Application.getTranslator();

    public PlacementPanel(RocketComponent component, List<Component> order) {
        super(new MigLayout("gap rel unrel", "[][65lp::][30lp::]"));
        this.setBorder(BorderFactory.createTitledBorder(trans.get("PlacementPanel.title.Placement")));

        this.add(new JLabel(trans.get("PlacementPanel.lbl.PosRelativeTo")));

        final EnumModel<AxialMethod> axialMethodModel = new EnumModel<>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
        final JComboBox<AxialMethod> axialMethodCombo = new JComboBox<>(axialMethodModel);
        this.add(axialMethodCombo, "spanx, growx, wrap");
        order.add(axialMethodCombo);

        //// plus
        this.add(new JLabel(trans.get("PlacementPanel.lbl.plus")), "right");

        final DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
        final JSpinner axialOffsetSpinner = new JSpinner(axialOffsetModel.getSpinnerModel());
        axialOffsetSpinner.setEditor(new SpinnerEditor(axialOffsetSpinner));

        this.add(axialOffsetSpinner, "growx");
        order.add(((SpinnerEditor) axialOffsetSpinner.getEditor()).getTextField());

        axialMethodCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                axialOffsetModel.stateChanged(new EventObject(e));
            }
        });

        this.add(new UnitSelector(axialOffsetModel), "growx");
        this.add(new BasicSlider(axialOffsetModel.getSliderModel(
                        new DoubleModel(component.getParent(), "Length", -1.0, UnitGroup.UNITS_NONE),
                        new DoubleModel(component.getParent(), "Length"))),
                "w 100lp");
    }
}
