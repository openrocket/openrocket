package info.openrocket.core.logging;

import info.openrocket.core.util.BugException;

import java.util.ArrayList;
import java.util.List;

/**
 * A set that contains multiple <code>Warning</code>s. When adding a
 * {@link Warning} to this set, the contents is checked for a warning of the
 * same type. If one is found, then the warning left in the set is determined
 * by the method {@link Warning#replaceBy(Message)}.
 * <p>
 * A WarningSet can be made immutable by calling {@link #immute()}.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class WarningSet extends MessageSet<Warning> {
	/**
	 * Add a <code>Warning</code> with the specified text to the set.  The Warning object
	 * is created using the {@link Message#fromString(String)} method.  If a warning of the
	 * same type exists in the set, the warning that is left in the set is defined by the
	 * method {@link Warning#replaceBy(Message)}.
	 *
	 * @param s		the message text.
	 * @throws IllegalStateException	if this message set has been made immutable.
	 */
    public boolean add(String s) {
        mutable.check();
        return add(Warning.fromString(s));
    }

    public int getNrOfCriticalWarnings() {
        return getNrOfMessagesWithPriority(MessagePriority.HIGH);
    }

    public int getNrOfNormalWarnings() {
        return getNrOfMessagesWithPriority(MessagePriority.NORMAL);
    }

    public int getNrOfInformativeWarnings() {
        return getNrOfMessagesWithPriority(MessagePriority.LOW);
    }

    public List<Warning> getCriticalWarnings() {
        List<Message> list = getMessagesWithPriority(MessagePriority.HIGH);
        return convertMessageListToWarningList(list);
    }

    public List<Warning> getNormalWarnings() {
        List<Message> list = getMessagesWithPriority(MessagePriority.NORMAL);
        return convertMessageListToWarningList(list);
    }

    public List<Warning> getInformativeWarnings() {
        List<Message> list = getMessagesWithPriority(MessagePriority.LOW);
        return convertMessageListToWarningList(list);
    }

    private static List<Warning> convertMessageListToWarningList(List<Message> list) {
        List<Warning> warnings = new ArrayList<>(list.size());
        for (Message m : list) {
            warnings.add((Warning) m);
        }
        return warnings;
    }

    @Override
    public WarningSet clone() {
        try {
            WarningSet newSet = (WarningSet) super.clone();
            newSet.messages = this.messages.clone();
            newSet.mutable = this.mutable.clone();
            return newSet;

        } catch (CloneNotSupportedException e) {
            throw new BugException("CloneNotSupportedException occurred, report bug!", e);
        }
    }
}
