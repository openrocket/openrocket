package info.openrocket.swing.gui.adaptors;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.util.Invalidatable;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class TransitionShapeModel extends AbstractListModel<Transition.Shape>
        implements ComboBoxModel<Transition.Shape>, ComponentChangeListener, Invalidatable {
    private final ModelInvalidator modelInvalidator;
    private final RocketComponent component;
    private final Transition.Shape[] typeList = Transition.Shape.values();
    private Transition.Shape previousType;

    public TransitionShapeModel(RocketComponent component) {
        this.modelInvalidator = new ModelInvalidator(component, this);
        this.component = component;
        if (component instanceof Transition) {
            previousType = ((Transition) component).getShapeType();
            setSelectedItem(previousType);
            component.addComponentChangeListener(this);
        }
    }

    @Override
    public void setSelectedItem(Object item) {
        if (!(component instanceof Transition) || !(item instanceof Transition.Shape)) {
            return;
        }

        ((Transition) component).setShapeType((Transition.Shape) item);
    }

    @Override
    public Object getSelectedItem() {
        if (component instanceof Transition) {
            return ((Transition) component).getShapeType();
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

        if (previousType != ((Transition) component).getShapeType()) {
            previousType = ((Transition) component).getShapeType();
            fireContentsChanged(this, 0, 0);
        }
    }

    @Override
    public void invalidateMe() {
        modelInvalidator.invalidateMe();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        modelInvalidator.finalize();
    }
}
