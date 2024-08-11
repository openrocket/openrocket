package info.openrocket.core.file.openrocket.importt;

import info.openrocket.core.file.simplesax.AbstractElementHandler;
import info.openrocket.core.util.ArrayList;

import java.util.List;

/**
 * Handler for entries that have a key and type attribute, and a value.
 * For example <entry key="foo" type="string">bar</entry>
 */
public abstract class EntryHandler extends AbstractElementHandler {
	protected EntryHandler listHandler;
	protected final List<Object> list = new ArrayList<>();

	public List<Object> getNestedList() {
		if (listHandler != null) {
			return listHandler.list;
		}
		return null;
	}
}