package info.openrocket.core.file.openrocket.importt;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.logging.WarningSet;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.position.*;
import info.openrocket.core.util.Reflection;

class FinTabPositionSetter extends DoubleSetter {
	private static final Logger log = LoggerFactory.getLogger(FinTabPositionSetter.class);

	public FinTabPositionSetter() {
		super(Reflection.findMethod(FinSet.class, "setTabOffset", double.class));
	}

	@Override
	public void set(RocketComponent c, String s, HashMap<String, String> attributes,
			WarningSet warnings) {

		if (!(c instanceof FinSet)) {
			throw new IllegalStateException("FinTabPositionSetter called for component " + c);
		}

		String relative = attributes.get("relativeto");

		if (relative == null) {
			warnings.add("Required attribute 'relativeto' not found for fin tab position.");
		} else {
			// translate from old enum names to current enum names
			if (relative.contains("front")) {
				relative = "top";
			} else if (relative.contains("center")) {
				relative = "middle";
			} else if (relative.contains("end")) {
				relative = "bottom";
			}

			AxialMethod position = (AxialMethod) DocumentConfig.findEnum(relative, AxialMethod.class);

			if (null == position) {
				warnings.add("Illegal attribute value '" + relative + "' encountered.");
			} else {
				((FinSet) c).setTabOffsetMethod(position);
				super.set(c, s, attributes, warnings);
			}

		}

	}

}
