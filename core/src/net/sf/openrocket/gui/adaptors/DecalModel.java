package net.sf.openrocket.gui.adaptors;

import java.awt.Component;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.FileSystemAttachmentFactory;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class DecalModel extends AbstractListModel implements ComboBoxModel {
	
	private static final Translator trans = Application.getTranslator();
	
	private static final String NONE_SELECTED = trans.get("lbl.select");
	private static final String SELECT_FILE = trans.get("lbl.choose");
	
	private final OpenRocketDocument document;
	private final Component parent;
	private final AppearanceBuilder ab;
	
	private static File lastImageDir = null;
	
	private DecalImage[] decals;
	
	public DecalModel(Component parent, OpenRocketDocument document, AppearanceBuilder ab) {
		this.document = document;
		this.parent = parent;
		this.ab = ab;
		decals = document.getDecalList().toArray(new DecalImage[0]);
	}
	
	@Override
	public int getSize() {
		return decals.length + 2;
	}
	
	@Override
	public Object getElementAt(int index) {
		if (index <= 0) {
			return NONE_SELECTED;
		}
		if (index == getSize() - 1) {
			return SELECT_FILE;
		}
		return decals[index - 1];
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
					fc.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
					int action = fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent));
					if (action == JFileChooser.APPROVE_OPTION) {
						((SwingPreferences) Application.getPreferences()).setDefaultDirectory(fc.getCurrentDirectory());
						File file = fc.getSelectedFile();
						Attachment a = (new FileSystemAttachmentFactory().getAttachment(file));
						setSelectedItem(document.getDecalImage(a));
					}
				}
			});
		} else {
			ab.setImage((DecalImage) item);
		}
	}
	
	@Override
	public Object getSelectedItem() {
		DecalImage decal = ab.getImage();
		if (decal == null || !document.getDecalList().contains(decal)) {
			return NONE_SELECTED;
		} else {
			return decal;
		}
	}
	
	public void refresh() {
		decals = document.getDecalList().toArray(new DecalImage[0]);
		fireContentsChanged(this, 0, decals.length);
	}
}
