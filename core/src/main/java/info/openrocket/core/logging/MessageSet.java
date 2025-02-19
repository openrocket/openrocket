package info.openrocket.core.logging;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.ModID;
import info.openrocket.core.util.Monitorable;
import info.openrocket.core.util.Mutable;

/**
 * A set that contains multiple <code>Message</code>s.  When adding a
 * {@link Message} to this set, the contents is checked for a message of the
 * same type.  If one is found, then the message left in the set is determined
 * by the method {@link Message#replaceBy(Message)}.
 * <p>
 * A MessageSet can be made immutable by calling {@link #immute()}.
 *
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class MessageSet<E extends Message> extends AbstractSet<E> implements Cloneable, Monitorable {
    /** the actual array of messages */
    protected ArrayList<E> messages = new ArrayList<>();

    protected Mutable mutable = new Mutable();
    private ModID modID = ModID.ZERO;

    /**
     * Add a <code>Message</code> to the set.  If a message of the same type
     * exists in the set, the message that is left in the set is defined by the
     * method {@link Message#replaceBy(Message)}.
     *
	 * If a new element is added to the set, returns true else returns false
	 * Replacing an old element with the new one is not regarded as adding the new
	 * element to the set (so it returns false in this case)
	 *
     * @throws IllegalStateException	if this message set has been made immutable.
     */
    @Override
    public boolean add(E m) {
        mutable.check();

        modID = new ModID();
        int index = messages.indexOf(m);

        if (index < 0) {
            return messages.add(m);
        }

        E old = messages.get(index);
        if (old.replaceBy(m)) {
            old.replaceContents(m);
        }

        return false;
    }

    /**
     * Add a <code>Message</code> with the specified text to the set.
     *
     * @param s		the message text.
     * @throws IllegalStateException	if this message set has been made immutable.
     */
    public abstract boolean add(String s);

    /**
     * Add a <code>Message</code> of the specified type with the specified message sources.
     * @param m the message
     * @param sources the sources of the message (rocket components that caused the message)
     *
     */
	public boolean add(E m, RocketComponent... sources) {
        mutable.check();
        try {
            m = (E) m.clone();
        } catch (CloneNotSupportedException e) {
            throw new BugException("CloneNotSupportedException occurred, report bug!", e);
        }
        m.setSources(sources);
        return add(m);
	}

    /**
     * Add a <code>Message</code> of the specified type with the specified discriminator to the
     * set.
     * @param m the message
     * @param d the extra discriminator
     *
     */
    public boolean add(E m, String d) {
        return this.add(m.toString() + ":  \"" + d + "\"");
    }

	/**
	 * obtain Message from set matching Message passed in
	 *
	 * @param m the message passed in
	 * @returns the matching message
	 */
	public Message get(Message m) {
		for (Message target : messages) {
			if (m.equals(target)) {
				return target;
			}
		}

		return null;
	}

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> iterator = messages.iterator();
        return new Iterator<>() {
			@Override
			public boolean hasNext() {
				return iterator.hasNext();
			}

			@Override
			public E next() {
				return iterator.next();
			}

			@Override
			public void remove() {
				mutable.check();
				iterator.remove();
			}
		};
    }

	/**
	 * filter out any messages of the given type.  Can't just use remove() inherited from
	 * AbstractCollection because the Message.equals() depends on message types, sources, and priority.
	 * @param type of message to filter
	 */
	  
	public void filterOut(E filter) {
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			if (i.next().getClass() == filter.getClass()) {
				i.remove();
			}
		}
	}
	
    @Override
    public int size() {
        return messages.size();
    }

    /**
     * Returns the number of messages with the specified priority.
     * @param priority the priority
     * @return the number of messages with the specified priority.
     */
    public int getNrOfMessagesWithPriority(MessagePriority priority) {
        int count = 0;
        for (E m : messages) {
            if (m.getPriority() == priority) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns a list of messages with the specified priority.
     *
     * @param priority the priority of the messages to retrieve
     * @return a list of messages with the specified priority
     */
    public List<Message> getMessagesWithPriority(MessagePriority priority) {
        List<Message> list = new ArrayList<>();
        for (E m : messages) {
            if (m.getPriority() == priority) {
                list.add(m);
            }
        }
        return list;
    }

	public Message findById(UUID id) {
		for (Message m : messages) {
			if (m.id.equals(id))
				return m;
		}
		throw new BugException("Message with id " + id + " not found");
	}

    public void immute() {
        mutable.immute();
    }


    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (Message m : messages) {
            if (s.length() > 0)
                s.append(",");
            s.append(m.toString());
        }
        return "Messages[" + s + "]";
    }

    @Override
    public ModID getModID() {
        return modID;
    }
}
