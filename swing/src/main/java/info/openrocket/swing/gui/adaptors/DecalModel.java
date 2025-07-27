package info.openrocket.swing.gui.adaptors;

import java.awt.Component;
import java.io.File;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import info.openrocket.core.appearance.AppearanceBuilder;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.document.Attachment;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.file.FileSystemAttachmentFactory;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.appearance.defaults.ResourceDecalImage;

import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;

public class DecalModel extends AbstractListModel<DecalImage> implements ComboBoxModel<DecalImage> {
	private static final long serialVersionUID = -3922419344990421156L;
	private static final Translator trans = Application.getTranslator();
	
	private static final ResourceDecalImage NONE_SELECTED = new ResourceDecalImage(trans.get("lbl.select"));
	
	private static final ResourceDecalImage SELECT_FILE = new ResourceDecalImage(trans.get("lbl.choose"));
	
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
	public DecalImage getElementAt(int index) {
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
					fc.setFileFilter(FileHelper.getImageFileFilter());
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
