package info.openrocket.core.file.rocksim.export;

/**
 * An interface that defines methods for attaching and detaching child
 * components. RockSim has a special
 * XML element that acts as a container, called
 * 
 * <pre>
 * <AttachedParts></AttachedParts>
 * </pre>
 * 
 * . Implementors of
 * this interface are those RockSim DTO classes that support the attached parts
 * element.
 */
public interface AttachableParts {
    /**
     * Remove a previously attached part from this component.
     *
     * @param part the instance to remove
     */
    void removeAttachedPart(BasePartDTO part);

    /**
     * Attach (associate) a DTO with this component.
     * 
     * @param part
     */
    void addAttachedPart(BasePartDTO part);
}
