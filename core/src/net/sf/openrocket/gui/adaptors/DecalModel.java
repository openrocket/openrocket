package net.sf.openrocket.gui.adaptors;

import java.awt.Component;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class DecalModel extends AbstractListModel implements ComboBoxModel {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final String NONE_SELECTED = trans.get("lbl.select");
	private static final String SELECT_FILE = trans.get("lbl.choose");
	
	private final Component parent;
	private final AppearanceBuilder ab;
	
	private static File lastImageDir = null;

	private String[] decals;
	
	public DecalModel(Component parent, OpenRocketDocument document, AppearanceBuilder ab) {
		this.parent = parent;
		this.ab = ab;
		decals = document.getDecalList().toArray( new String[0] );
	}
	
	@Override
	public int getSize() {
		return decals.length + 2;
	}
	
	@Override
	public Object getElementAt(int index) {
		if (index == 0) {
			return NONE_SELECTED;
		}
		if (index == getSize() - 1) {
			return SELECT_FILE;
		}
		return decals[index-1];
	}
	
	@Override
	public void setSelectedItem(Object item) {
		
		if (item == null || item.equals(NONE_SELECTED)) {
			ab.setImage(null);
		} else if (item.equals(SELECT_FILE)) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					File current = lastImageDir;
					lastImageDir = current;

					JFileChooser fc = new JFileChooser(current);
					int action = fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent));
					if ( action == JFileChooser.APPROVE_OPTION) {
						setSelectedItem(fc.getSelectedFile().getAbsolutePath());
					}
				}
			});
		} else {
			ab.setImage( (String) item);
		}
	}
	
	@Override
	public Object getSelectedItem() {
		String name = ab.getImage();
		if (name == null) {
			return NONE_SELECTED;
		} else {
			return name;
		}
	}
	
}
