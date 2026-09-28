package info.openrocket.swing.gui.adaptors;

import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.AppearanceBuilder;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.appearance.defaults.ResourceDecalImage;
import info.openrocket.core.document.Attachment;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.core.document.events.DocumentChangeListener;
import info.openrocket.core.file.FileSystemAttachmentFactory;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.InsideColorComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.NativeFileChooser;

public class DecalModel extends AbstractListModel<DecalImage> implements ComboBoxModel<DecalImage> {
	private static final long serialVersionUID = -3922419344990421156L;
	private static final Translator trans = Application.getTranslator();
	private static final int PREVIEW_MAX_SIZE = 140;
	
	private static final ResourceDecalImage NONE_SELECTED = new ResourceDecalImage(trans.get("lbl.select"));
	
	private final OpenRocketDocument document;
	private final Component parent;
	private final AppearanceBuilder ab;
	private final DocumentChangeListener documentChangeListener;
	
	private static File lastImageDir = null;
	
	private DecalImage[] decals;
	private final Map<DecalImage, Icon> previewCache = new HashMap<>();
	private final Map<DecalImage, List<String>> usageCache = new HashMap<>();
	
	public DecalModel(Component parent, OpenRocketDocument document, AppearanceBuilder ab) {
		this.document = document;
		this.parent = parent;
		this.ab = ab;
		decals = document.getDecalList().toArray(new DecalImage[0]);
		documentChangeListener = new DocumentChangeListener() {
			@Override
			public void documentChanged(DocumentChangeEvent event) {
				clearCaches();
			}
		};
		document.addDocumentChangeListener(documentChangeListener);
	}
	
	@Override
	public int getSize() {
		return decals.length + 1;
	}
	
	@Override
	public DecalImage getElementAt(int index) {
		if (index <= 0) {
			return NONE_SELECTED;
		}
		return decals[index - 1];
	}
	
	@Override
	public void setSelectedItem(Object item) {
		
		if (item == null || item.equals(NONE_SELECTED)) {
			ab.setImage(null);
		} else {
			ab.setImage((DecalImage) item);
		}
	}

	public DecalPreviewData getPreviewData(DecalImage decal) {
		if (decal == null) {
			return DecalPreviewData.placeholder(trans.get("DecalModel.preview.placeholder"));
		}
		if (decal.equals(NONE_SELECTED)) {
			return DecalPreviewData.placeholder(trans.get("DecalModel.preview.noneSelected"));
		}
		Icon preview = previewCache.computeIfAbsent(decal, this::loadPreviewIcon);
		List<String> usage = usageCache.computeIfAbsent(decal, this::buildUsageList);
		return new DecalPreviewData(decal.getName(), preview, usage);
	}

	public void promptForFileSelection() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				File current = lastImageDir;

				NativeFileChooser fc = new NativeFileChooser(current);
				fc.setFileFilter(FileHelper.getImageFileFilter());
				fc.setCurrentDirectory(Application.getPreferences().getDefaultDirectory());
				int action = fc.showOpenDialog(SwingUtilities.getWindowAncestor(parent));
				if (action == NativeFileChooser.APPROVE_OPTION) {
					Application.getPreferences().setDefaultDirectory(fc.getCurrentDirectory());
					File file = fc.getSelectedFile();
					lastImageDir = file.getParentFile();
					Attachment a = (new FileSystemAttachmentFactory().getAttachment(file));
					setSelectedItem(document.getDecalImage(a));
				}
			}
		});
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
		clearCaches();
		fireContentsChanged(this, 0, decals.length);
	}

	public DecalImage getActiveDecal() {
		Object selected = getSelectedItem();
		return (selected instanceof DecalImage && isDeletable((DecalImage) selected)) ? (DecalImage) selected : null;
	}

	public boolean isDeletable(DecalImage decal) {
		return decal != null && !decal.equals(NONE_SELECTED);
	}

	public boolean deleteDecal(DecalImage decal) {
		if (!isDeletable(decal)) {
			return false;
		}
		boolean removed = document.removeDecal(decal);
		if (removed) {
			setSelectedItem(NONE_SELECTED);
			refresh();
		}
		return removed;
	}

	public void dispose() {
		document.removeDocumentChangeListener(documentChangeListener);
		clearCaches();
	}

	private void clearCaches() {
		previewCache.clear();
		usageCache.clear();
	}

	private Icon loadPreviewIcon(DecalImage decal) {
		if (decal == null) {
			return null;
		}
		try (InputStream is = decal.getBytes()) {
			if (is == null) {
				return null;
			}
			BufferedImage image = ImageIO.read(is);
			if (image == null) {
				return null;
			}
			return new ImageIcon(scaleImage(image, PREVIEW_MAX_SIZE, PREVIEW_MAX_SIZE));
		} catch (IOException | DecalNotFoundException ex) {
			Application.getExceptionHandler().handleErrorCondition(ex);
			return null;
		}
	}

	private static BufferedImage scaleImage(BufferedImage source, int maxWidth, int maxHeight) {
		int originalWidth = source.getWidth();
		int originalHeight = source.getHeight();
		double scale = Math.min((double) maxWidth / originalWidth, (double) maxHeight / originalHeight);
		if (scale > 1d) {
			scale = 1d;
		}
		int targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));
		int targetHeight = Math.max(1, (int) Math.round(originalHeight * scale));
		Image scaled = source.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
		BufferedImage output = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
		output.getGraphics().drawImage(scaled, 0, 0, null);
		output.getGraphics().dispose();
		return output;
	}

	private List<String> buildUsageList(DecalImage decal) {
		if (decal == null) {
			return Collections.emptyList();
		}
		List<String> usage = new ArrayList<>();
		for (RocketComponent component : document.getRocket()) {
			if (usesOutsideDecal(component, decal)) {
				usage.add(formatComponentLabel(component, false));
			}
			if (usesInsideDecal(component, decal)) {
				usage.add(formatComponentLabel(component, true));
			}
		}
		return usage;
	}

	private boolean usesOutsideDecal(RocketComponent component, DecalImage decal) {
		Appearance appearance = component.getAppearance();
		if (appearance == null) {
			return false;
		}
		Decal texture = appearance.getTexture();
		return texture != null && decal.equals(texture.getImage());
	}

	private boolean usesInsideDecal(RocketComponent component, DecalImage decal) {
		if (!(component instanceof InsideColorComponent)) {
			return false;
		}
		Appearance appearance = ((InsideColorComponent) component).getInsideColorComponentHandler().getInsideAppearance();
		if (appearance == null) {
			return false;
		}
		Decal texture = appearance.getTexture();
		return texture != null && decal.equals(texture.getImage());
	}

	private String formatComponentLabel(RocketComponent component, boolean inside) {
		String name = component.getName();
		if (name == null || name.isEmpty()) {
			name = component.getComponentName();
		}
		if (inside) {
			return trans.get("DecalModel.preview.componentInside", name);
		}
		return trans.get("DecalModel.preview.componentOutside", name);
	}

	public static class DecalPreviewData {
		private final String displayName;
		private final Icon previewIcon;
		private final List<String> usage;
		private final boolean placeholder;

		private DecalPreviewData(String displayName, Icon previewIcon, List<String> usage) {
			this.displayName = displayName;
			this.previewIcon = previewIcon;
			this.usage = usage == null ? Collections.emptyList() : Collections.unmodifiableList(usage);
			this.placeholder = false;
		}

		private DecalPreviewData(String message) {
			this.displayName = message;
			this.previewIcon = null;
			this.usage = Collections.emptyList();
			this.placeholder = true;
		}

		public static DecalPreviewData placeholder(String message) {
			return new DecalPreviewData(message);
		}

		public String getDisplayName() {
			return displayName;
		}

		public Icon getPreviewIcon() {
			return previewIcon;
		}

		public List<String> getUsage() {
			return usage;
		}

		public boolean isPlaceholder() {
			return placeholder;
		}
	}
}
