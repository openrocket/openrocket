package net.sf.openrocket.logging;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * An error message wrapper.
 */
public abstract class Error extends Message {
    private static final Translator trans = Application.getTranslator();

    /**
     * @return an Error with the specific text.
     */
    public static Error fromString(String text) {
        return new Error.Other(text);
    }


    /////////////  Specific Error classes  /////////////


    /**
     * An unspecified error type.  This error type holds a <code>String</code>
     * describing it.  Two errors of this type are considered equal if the strings
     * are identical.
     */
    public static class Other extends Error {
        private final String description;

        public Other(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof Other))
                return false;

            Other o = (Other) other;
            return (o.description.equals(this.description));
        }

        @Override
        public int hashCode() {
            return description.hashCode();
        }

        @Override
        public boolean replaceBy(Message other) {
            return false;
        }
    }
}
