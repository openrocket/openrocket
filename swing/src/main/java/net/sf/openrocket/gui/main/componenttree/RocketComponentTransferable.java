package net.sf.openrocket.gui.main.componenttree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.List;

import net.sf.openrocket.rocketcomponent.RocketComponent;

/**
 * A transferable that provides a reference to a (JVM-local) RocketComponent object.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class RocketComponentTransferable implements Transferable {

	/**
	 * Data flavor that allows a RocketComponent to be extracted from a transferable object
	 */
	public static final DataFlavor ROCKET_COMPONENT_DATA_FLAVOR = new DataFlavor(RocketComponentTransferable.class,
			"Drag and drop list");
	

	private final List<RocketComponent> components;
	
	public RocketComponentTransferable(List<RocketComponent> components) {
		this.components = components;
	}
	
	
	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor);
		}
		return this;
	}

	public List<RocketComponent> getComponents() {
		return components;
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
