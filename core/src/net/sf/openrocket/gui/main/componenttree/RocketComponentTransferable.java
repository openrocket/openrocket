package net.sf.openrocket.gui.main.componenttree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A transferable that provides a reference to a (JVM-local) RocketComponent object.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketComponentTransferable implements Transferable {
	
	public static final DataFlavor ROCKET_COMPONENT_DATA_FLAVOR = new DataFlavor(
			DataFlavor.javaJVMLocalObjectMimeType + "; class=" + RocketComponent.class.getCanonicalName(),
			"OpenRocket component");
	

	private final RocketComponent component;
	
	public RocketComponentTransferable(RocketComponent component) {
		this.component = component;
	}
	
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return component;
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { ROCKET_COMPONENT_DATA_FLAVOR };
	}
	
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(ROCKET_COMPONENT_DATA_FLAVOR);
	}
	
}
