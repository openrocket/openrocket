package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class Stage extends ComponentAssembly {

	private static final Translator trans = Application.getTranslator();

    @Override
    public String getComponentName () {
    	//// Stage
        return trans.get("Stage.Stage");
    }


	@Override
	public boolean allowsChildren() {
		return true;
	}

    /**
	 * Check whether the given type can be added to this component.  A Stage allows
	 * only BodyComponents to be added.
     *
     * @param type The RocketComponent class type to add.
     *
     * @return Whether such a component can be added.
     */
    @Override
    public boolean isCompatible (Class<? extends RocketComponent> type) {
        return BodyComponent.class.isAssignableFrom(type);
    }
}
