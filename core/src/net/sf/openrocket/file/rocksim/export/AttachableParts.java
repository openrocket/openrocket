package net.sf.openrocket.file.rocksim.export;

/**
 * An interface that defines methods for attaching and detaching child components.  Rocksim has a special
 * XML element that acts as a container, called <pre><AttachedParts></AttachedParts></pre>.  Implementors of
 * this interface are those Rocksim DTO classes that support the attached parts element.
 */
public interface AttachableParts {
    /**
     * Remove a previously attached part from this component.
     *
     * @param part  the instance to remove
     */
    void removeAttachedPart(BasePartDTO part);

    /**
     * Attach (associate) a DTO with this component.
     * @param part
     */
    void addAttachedPart(BasePartDTO part);
}
