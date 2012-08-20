package net.sf.openrocket.rocketcomponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.ComponentPreset.Type;
import net.sf.openrocket.startup.Application;


public class EngineBlock extends ThicknessRingComponent {
	
	private static final Translator trans = Application.getTranslator();

	public EngineBlock() {
		super();
		setOuterRadiusAutomatic(true);
		setThickness(0.005);
		setLength(0.005);
	}
	
	@Override
	public void setOuterRadiusAutomatic(boolean auto) {
		super.setOuterRadiusAutomatic(auto);
	}
	
	@Override
	public String getComponentName() {
		return trans.get ("EngineBlock.EngineBlock");
	}
	
	@Override
	public boolean allowsChildren() {
		return false;
	}
	
	@Override
	public boolean isCompatible(Class<? extends RocketComponent> type) {
		return false;
	}
	
	@Override
	public Type getPresetType() {
		return ComponentPreset.Type.ENGINE_BLOCK;
	}

}
