package info.openrocket.swing.gui.configdialog;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.SpinnerEditor;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.adaptors.EnumModel;
import info.openrocket.swing.gui.adaptors.IntegerModel;
import info.openrocket.swing.gui.components.BasicSlider;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.AxialMethod;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

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
 * Panel for configuring a component's duplication instances.
 */
public class InstancesPanel extends JPanel {
    private static final Translator trans = Application.getTranslator();

    public InstancesPanel(RocketComponent component, List<Component> order) {
        super(new MigLayout("gap rel unrel", "[][65lp::][30lp::]"));
        this.setBorder(BorderFactory.createTitledBorder(trans.get("InstancesPanel.title.Instances")));

        {//// Instance Count
            add(new JLabel(trans.get("InstancesPanel.lbl.InstanceCount")));
            IntegerModel countModel = new IntegerModel(component, "InstanceCount", 1);
            JSpinner countSpinner = new JSpinner( countModel.getSpinnerModel());
            countSpinner.setEditor(new SpinnerEditor(countSpinner));
            add(countSpinner, "growx, wrap rel");
            order.add(((SpinnerEditor) countSpinner.getEditor()).getTextField());
        }

        { //// Instance separation
            add(new JLabel(trans.get("InstancesPanel.lbl.InstanceSeparation")));
            DoubleModel separationModel = new DoubleModel(component, "InstanceSeparation", UnitGroup.UNITS_LENGTH);
            JSpinner separationSpinner = new JSpinner( separationModel.getSpinnerModel());
            separationSpinner.setEditor(new SpinnerEditor(separationSpinner));
            add(separationSpinner, "growx");
            order.add(((SpinnerEditor) separationSpinner.getEditor()).getTextField());
            add(new UnitSelector(separationModel), "growx");
            double maxSeparationDistance = 0.1;
            if (component.getParent() != null && component.getParent().getLength() > 0) {
                maxSeparationDistance = component.getParent().getLength();
            }
            add(new BasicSlider(separationModel.getSliderModel(-maxSeparationDistance, maxSeparationDistance)), "w 100lp, wrap para");
        }
    }
}
