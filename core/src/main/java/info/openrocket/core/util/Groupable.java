package info.openrocket.core.util;

/**
 * Interface for objects that can be grouped into a <Group> object.
 * @param <G> the group type
 */
public interface Groupable<G extends Group> {
	G getGroup();
}
