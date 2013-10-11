package net.sf.openrocket.gui;

/**
 * An interface for GUI elements with a resettable model.  The resetModel() method in 
 * this interface resets the model to some default model, releasing the old model 
 * listening connections.
 * 
 * Some components that don't have a settable model simply release the current model.
 * These components cannot therefore be reused after calling resetModel().
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface Resettable {
	public void resetModel();
}
