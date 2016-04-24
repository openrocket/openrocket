package net.sf.openrocket.gui.customexpression;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JPanel;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;


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
