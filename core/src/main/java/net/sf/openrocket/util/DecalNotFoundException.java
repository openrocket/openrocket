package net.sf.openrocket.util;

import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

import java.text.MessageFormat;

/**
 * Exception for decals without a valid source file.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DecalNotFoundException extends Exception {
    private final DecalImage decal;
    private final Translator trans = Application.getTranslator();

    /**
     * Exception for decals without a valid source file.
     * @param message the file path or decal name of the faulty DecalImage
     * @param decal DecalImage that has an issue with its source file
     */
    public DecalNotFoundException(String message, DecalImage decal) {
        super(message);
        this.decal = decal;
    }

    /**
     * Get the DecalImage that was the cause of this DecalNotFoundException.
     * @return DecalImage that had an issue with its source file
     */
    public DecalImage getDecal() {
        return decal;
    }

    /**
     * Automatically combine the exception message with the DecalImage file name.
     */
    @Override
    public String getMessage() {
        return MessageFormat.format(trans.get("ExportDecalDialog.source.exception"), super.getMessage());
    }
}
