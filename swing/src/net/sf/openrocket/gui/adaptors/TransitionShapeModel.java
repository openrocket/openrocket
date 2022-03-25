package net.sf.openrocket.gui.adaptors;

import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketcomponent.Transition;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class TransitionShapeModel extends AbstractListModel<Transition.Shape>
        implements ComboBoxModel<Transition.Shape>, ComponentChangeListener {
    private final RocketComponent component;
    private final Transition.Shape[] typeList = Transition.Shape.values();
    private Transition.Shape previousType;

    public TransitionShapeModel(RocketComponent component) {
        this.component = component;
        if (component instanceof Transition) {
            previousType = ((Transition) component).getType();
            setSelectedItem(previousType);
            component.addComponentChangeListener(this);
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if (!(component instanceof Transition) || !(item instanceof Transition.Shape)) {
            return;
        }

        ((Transition) component).setType((Transition.Shape) item);
    }

    @Override
    public Object getSelectedItem() {
        if (component instanceof Transition) {
            return ((Transition) component).getType();
        }
        return null;
    }

    @Override
    public int getSize() {
        return typeList.length;
    }

    @Override
    public Transition.Shape getElementAt(int index) {
        return typeList[index];
    }

    @Override
    public void componentChanged(ComponentChangeEvent e) {
        if (!(component instanceof Transition)) {
            return;
        }

        if (previousType != ((Transition) component).getType()) {
            previousType = ((Transition) component).getType();
            fireContentsChanged(this, 0, 0);
        }
    }
}
