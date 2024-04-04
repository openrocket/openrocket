package info.openrocket.core.logging;

import info.openrocket.core.rocketcomponent.RocketComponent;
import java.util.Arrays;

/**
 * Baseclass for logging messages (warnings, errors...)
 */
public abstract class Message implements Cloneable {
	/** The rocket component(s) that are the source of this message **/
	private RocketComponent[] sources = null;

	private MessagePriority priority = MessagePriority.NORMAL;

	/**
	 * Returns the message text + message source objects.
	 * @return the message text + message source objects.
	 */
	@Override
	public String toString() {
		return addSourcesToMessageText(getMessageDescription(), getSources());
	}

	/**
	 * Returns the message text + message source objects.
	 * @param text the message text
	 * @param sources the message source objects
	 * @return the message text + message source objects.
	 */
	protected static String addSourcesToMessageText(String text, RocketComponent[] sources) {
		if (sources != null && sources.length > 0) {
			String[] sourceNames = new String[sources.length];
			for (int i = 0; i < sources.length; i++) {
				sourceNames[i] = sources[i].getName();
			}
			return text + ": \"" + String.join(", ", sourceNames) + "\"";
		}
		return text;
	}

	/**
	 * Returns the message text (without message source information). The text should be short and descriptive.
	 * @return the message text.
	 */
	public abstract String getMessageDescription();

	/**
	 * Return <code>true</code> if the <code>other</code> warning should replace
	 * this message.  The method should return <code>true</code> if the other
	 *  indicates a "worse" condition than the current warning.
	 *
	 * @param other  the message to compare to
	 * @return       whether this message should be replaced
	 */
	public abstract boolean replaceBy(Message other);

	/**
	 * Return the rocket component(s) that are the source of this warning.
	 * @return the rocket component(s) that are the source of this warning. Returns null if no sources are specified.
	 */
	public RocketComponent[] getSources() {
		return sources;
	}

	/**
	 * Set the rocket component(s) that are the source of this warning.
	 * @param sources the rocket component(s) that are the source of this warning.
	 */
	public void setSources(RocketComponent[] sources) {
		this.sources = sources;
	}

	/**
	 * Return the priority of this message.
	 * @return the priority of this message.
	 */
	public MessagePriority getPriority() {
		return priority;
	}

	/**
	 * Set the priority of this message.
	 * @param priority the priority of this message.
	 */
	public void setPriority(MessagePriority priority) {
		this.priority = priority;
	}

	/**
	 * Two <code>Message</code>s are by default considered equal if they are of
	 * the same class.  Therefore only one instance of a particular message type
	 * is stored in a {@link MessageSet}.  Subclasses may override this method for
	 * more specific functionality.
	 */
	@Override
	public boolean equals(Object o) {
		return o != null && (o.getClass() == this.getClass() && sourcesEqual(((Message) o).sources, sources))
				&& ((Message) o).priority == priority;
	}

	/**
	 * Compare two arrays of rocket components for equality.
	 * @param a the first list of sources
	 * @param b the second list of sources
	 * @return true if the lists are equal
	 */
	protected boolean sourcesEqual(RocketComponent[] a, RocketComponent[] b) {
		return Arrays.equals(a, b);
	}

	/**
	 * A <code>hashCode</code> method compatible with the <code>equals</code> method.
	 */
	@Override
	public int hashCode() {
		return this.getClass().hashCode();
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		Message clone = (Message) super.clone();
		clone.sources = this.sources;
		clone.priority = this.priority;
		return clone;
	}
}
