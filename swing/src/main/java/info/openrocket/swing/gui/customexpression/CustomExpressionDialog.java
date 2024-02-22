package info.openrocket.swing.gui.customexpression;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.util.GUIUtil;


@SuppressWarnings("serial")
public class CustomExpressionDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	
	@SuppressWarnings("unused")
	private final Window parentWindow;
	private final OpenRocketDocument doc;
	
	public CustomExpressionDialog(OpenRocketDocument doc, Window parent) {
		super(parent, trans.get("customExpressionPanel.lbl.CustomExpressions"));
		
		this.doc = doc;
		this.parentWindow = parent;
		
		JPanel panel = new CustomExpressionPanel(this.doc, this);
		this.add(panel);
		
		GUIUtil.setDisposableDialogOptions(this, null);
	}
}
