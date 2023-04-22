package net.sf.openrocket.logging;

/**
 * Baseclass for logging messages (warnings, errors...)
 */
public abstract class Message {
    /**
     * @return a Message with the specific text.
     */
    public static Message fromString(String text) {
        return new Warning.Other(text);
    }

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
     * Two <code>Message</code>s are by default considered equal if they are of
     * the same class.  Therefore only one instance of a particular message type
     * is stored in a {@link MessageSet}.  Subclasses may override this method for
     * more specific functionality.
     */
    @Override
    public boolean equals(Object o) {
        return o != null && (o.getClass() == this.getClass());
    }

    /**
     * A <code>hashCode</code> method compatible with the <code>equals</code> method.
     */
    @Override
    public int hashCode() {
        return this.getClass().hashCode();
    }
}
