package info.openrocket.swing.gui.dialogs.preset;

import java.util.Set;

import javax.swing.table.TableColumn;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preset.ComponentPreset;
import info.openrocket.core.preset.TypedKey;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.unit.Value;

@SuppressWarnings("serial")
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
			return favorites.contains(preset.preferenceKey());
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
            Object rawValue = super.getValueFromPreset(favorites, preset);
            Double value = rawValue != null ? (Double) rawValue : null;

            // Calculate AREA from DIAMETER if key == SURFACE_AREA
            if (value == null && key == ComponentPreset.SURFACE_AREA && preset.has(ComponentPreset.DIAMETER)) {
                double diameter = preset.get(ComponentPreset.DIAMETER);
                value = Math.PI * Math.pow(diameter / 2.0, 2.0);
            }

            // Calculate CD_AREA only if this column is CD_AREA
            if (value == null && key == ComponentPreset.CD_AREA && preset.has(ComponentPreset.CD)) {
                Double area = null;
                Double cd = preset.get(ComponentPreset.CD);

                if (preset.has(ComponentPreset.SURFACE_AREA)) {
                    area = preset.get(ComponentPreset.SURFACE_AREA);
                } else if (preset.has(ComponentPreset.DIAMETER)) {
                    double diameter = preset.get(ComponentPreset.DIAMETER);
                    area = Math.PI * Math.pow(diameter / 2.0, 2.0);
                }

                if (area != null && cd != null) {
                    value = area * cd;
                }
            }

            return value != null ? new Value(value, selectedUnit) : null;
        }

    }

}



