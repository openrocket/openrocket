package net.sf.openrocket.file.rocksim.export;

/**
 */
public interface AttachedParts {
    void removeAttachedPart(BasePartDTO part);

    void addAttachedPart(BasePartDTO part);
}
