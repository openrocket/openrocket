package info.openrocket.core.document;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import info.openrocket.core.util.AbstractChangeSource;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.DecalNotFoundException;

/**
 * 
 * Class handler of documents attachments
 *
 */
public abstract class Attachment extends AbstractChangeSource implements Comparable<Attachment>, ChangeSource {

	private final String name;

	/**
	 * default constructor
	 * 
	 * @param name the attachment name
	 */
	public Attachment(String name) {
		super();
		this.name = name;
	}

	/**
	 * returns the name of attachment
	 * 
	 * @return name of attachment
	 */
	public String getName() {
		return name;
	}

	/**
	 * returns the stream of bytes representing the attachment
	 * 
	 * @return the stream of bytes representing the attachment
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public abstract InputStream getBytes() throws FileNotFoundException, IOException, DecalNotFoundException;

	/**
	 * {@inheritDoc}
	 * considers only the name to equals
	 */
	@Override
	public int compareTo(Attachment o) {
		return this.name.compareTo(o.name);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void fireChangeEvent() {
		super.fireChangeEvent();
	}

}