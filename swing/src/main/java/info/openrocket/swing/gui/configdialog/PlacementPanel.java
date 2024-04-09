package info.openrocket.swing.gui.configdialog;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Invalidatable;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Panel for configuring a component's placement relative to its parent.
 */
public class PlacementPanel extends JPanel implements Invalidatable, InvalidatingWidget {
    private static final Translator trans = Application.getTranslator();
    private final List<Invalidatable> invalidatables = new ArrayList<>();

    public PlacementPanel(RocketComponent component, List<Component> order) {
        super(new MigLayout("gap rel unrel", "[][65lp::][30lp::]"));
        this.setBorder(BorderFactory.createTitledBorder(trans.get("PlacementPanel.title.Placement")));

        this.add(new JLabel(trans.get("PlacementPanel.lbl.PosRelativeTo")));

        final EnumModel<AxialMethod> axialMethodModel = new EnumModel<>(component, "AxialMethod", AxialMethod.axialOffsetMethods );
        register(axialMethodModel);
        final JComboBox<AxialMethod> axialMethodCombo = new JComboBox<>(axialMethodModel);
        this.add(axialMethodCombo, "spanx, growx, wrap");
        order.add(axialMethodCombo);

        //// plus
        this.add(new JLabel(trans.get("PlacementPanel.lbl.plus")), "right");

        final DoubleModel axialOffsetModel = new DoubleModel(component, "AxialOffset", UnitGroup.UNITS_LENGTH);
        register(axialOffsetModel);
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

    @Override
    public void register(Invalidatable model) {
        this.invalidatables.add(model);
    }

    @Override
    public void invalidateMe() {
        super.invalidate();
        for (Invalidatable i : invalidatables) {
            i.invalidateMe();
        }
    }
}
