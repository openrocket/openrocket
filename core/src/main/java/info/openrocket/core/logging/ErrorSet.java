package info.openrocket.core.logging;

import info.openrocket.core.util.BugException;

public class ErrorSet extends MessageSet<Error> {
    /**
     * Add an <code>Error</code> with the specified text to the set. The Error
     * object
     * is created using the {@link Error#fromString(String)} method. If an error of
     * the
     * same type exists in the set, the error that is left in the set is defined by
     * the
     * method {@link Error#replaceBy(Message)}.
     *
     * @param s the message text.
     * @throws IllegalStateException if this message set has been made immutable.
     */
    public boolean add(String s) {
        mutable.check();
        return add(Error.fromString(s));
    }

    @Override
    public ErrorSet clone() {
        try {
            ErrorSet newSet = (ErrorSet) super.clone();
            newSet.messages = this.messages.clone();
            newSet.mutable = this.mutable.clone();
            return newSet;

        } catch (CloneNotSupportedException e) {
            throw new BugException("CloneNotSupportedException occurred, report bug!", e);
        }
    }
}
