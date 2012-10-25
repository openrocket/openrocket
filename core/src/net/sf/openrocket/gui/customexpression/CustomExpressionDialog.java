package net.sf.openrocket.gui.customexpression;

import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;

public class CustomExpressionDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(CustomExpressionDialog.class);
	
	private final Window parentWindow;
	private final OpenRocketDocument doc;
	
	public CustomExpressionDialog(OpenRocketDocument doc, Window parent){
		super(parent, trans.get("customExpressionPanel.lbl.CustomExpressions"));
		
		this.doc = doc;
		this.parentWindow = parent;
		
		JPanel panel = new CustomExpressionPanel(doc, this);
		this.add( panel );
		
		GUIUtil.setDisposableDialogOptions(this, null);
	}
}
