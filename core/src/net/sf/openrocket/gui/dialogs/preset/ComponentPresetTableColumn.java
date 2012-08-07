package net.sf.openrocket.gui.dialogs.preset;

import java.util.Set;

import javax.swing.table.TableColumn;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Unit;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;

public abstract class ComponentPresetTableColumn extends TableColumn {

	private static final Translator trans = Application.getTranslator();

	protected ComponentPresetTableColumn( String header, int modelIndex ) {
		this.setHeaderValue(header);
		this.setModelIndex(modelIndex);
	
	}
	
	public abstract Object getValueFromPreset( Set<String> favorites, ComponentPreset preset );
	
	public static class Favorite extends ComponentPresetTableColumn {

		public Favorite(int modelIndex) {
			super(trans.get("table.column.Favorite"), modelIndex);
		}
		
		@Override
		public Object getValueFromPreset( Set<String> favorites, ComponentPreset preset ) {
			return Boolean.valueOf(favorites.contains(preset.preferenceKey()));
		}

	}

	public static class Parameter extends ComponentPresetTableColumn {

		protected final TypedKey<?> key;
		
		public Parameter( TypedKey<?> key, int modelIndex ) {
			super( trans.get("table.column." + key.getName()), modelIndex );
			this.key = key;
		}

		@Override
		public Object getValueFromPreset(Set<String> favorites, ComponentPreset preset) {
			return preset.has(key) ? preset.get(key) : null;
		}
		
	}


	public static class DoubleWithUnit extends Parameter {

		UnitGroup unitGroup;
		Unit selectedUnit;
		
		public DoubleWithUnit( TypedKey<Double> key, int modelIndex ) {
			super(key,modelIndex);
			this.unitGroup = key.getUnitGroup();
			this.selectedUnit = unitGroup.getDefaultUnit();
		}

		@Override
		public Object getValueFromPreset(Set<String> favorites, ComponentPreset preset) {
			Double value = (Double) super.getValueFromPreset(favorites, preset);
			if ( value != null ) {
				return new Value((Double)super.getValueFromPreset(favorites, preset),selectedUnit);
			} else {
				return null;
			}
		}
		
		
	}

}



