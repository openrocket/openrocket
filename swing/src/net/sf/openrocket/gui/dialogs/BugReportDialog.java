package net.sf.openrocket.gui.dialogs;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import com.jogamp.opengl.JoglVersion;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.URLLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LogLevelBufferLogger;
import net.sf.openrocket.logging.LogLine;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class BugReportDialog extends JDialog {
	
	private static final String NEW_ISSUES_URL = "https://github.com/openrocket/openrocket/issues/new";
	private static final String REPORT_EMAIL = "openrocket-bugs@lists.sourceforge.net";
	private static final String REPORT_EMAIL_URL = "mailto:" + REPORT_EMAIL;
	
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences preferences = (SwingPreferences) Application.getPreferences();
	
	
	public BugReportDialog(Window parent, String labelText, final String message, final boolean sendIfUnchanged) {
		//// Bug report
		super(parent, trans.get("bugreport.dlg.title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Some fscking Swing bug that makes html labels initially way too high
		StyledLabel titleLabel = new StyledLabel(labelText, 0);
		Dimension d = titleLabel.getPreferredSize();
		d.width = 100000;
		titleLabel.setMaximumSize(d);
		panel.add(titleLabel, "gapleft para, wrap para");
		
		//// <html>If connected to the Internet, you can simply click 
		//// <em>Send bug report</em>.
		JLabel label = new JLabel(trans.get("bugreport.dlg.connectedInternet"));
		panel.add(label, "gapleft para, split 2, gapright rel");
		
		panel.add(new URLLabel(NEW_ISSUES_URL), "growx, wrap para");
		
		//// Otherwise, send the text below to the address:
		panel.add(new JLabel(trans.get("bugreport.dlg.otherwise") + " "),
				  "gapleft para, split 2, gapright rel");
		panel.add(new URLLabel(REPORT_EMAIL_URL, REPORT_EMAIL), "growx, wrap para");

		final JEditorPane editorPane = new JEditorPane("text/html", formatNewlineHTML(message));
		editorPane.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, true);
		editorPane.setFont(UIManager.getFont("Label.font"));
		editorPane.setPreferredSize(new Dimension(600, 400));
		editorPane.setEditable(true);
		editorPane.setCaretPosition(0);		// Scroll to the top by default
		panel.add(new JScrollPane(editorPane), "grow, wrap");
		
		panel.add(new StyledLabel(trans.get("bugreport.lbl.Theinformation"), -1), "wrap para");
		
		////Close button
		JButton close = new SelectColorButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BugReportDialog.this.dispose();
			}
		});
		panel.add(close, "right, sizegroup buttons, split");
		
		this.add(panel);
		
		this.validate();
		this.pack();
		this.pack();
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
	/**
	 * Show a general bug report dialog allowing the user to input information about
	 * the bug they encountered.
	 * 
	 * @param parent	the parent window (may be null).
	 */
	public static void showBugReportDialog(Window parent) {
		StringBuilder sb = new StringBuilder();

		// ---------- Bug report ----------
		addBugReportInformation(sb);
		
		sb.append("(Do not modify anything below this line.)\n");
		sb.append("---------- System information ----------\n");
		addSystemInformation(sb);
		sb.append("---------- Error log ----------\n");
		addErrorLog(sb);
		sb.append("---------- End of bug report ----------</html>\n");
		sb.append('\n');
		
		BugReportDialog reportDialog = new BugReportDialog(parent,
				trans.get("bugreport.reportDialog.txt"), sb.toString(), false);
		reportDialog.setVisible(true);
	}
	
	
	/**
	 * Show a dialog presented when an uncaught exception occurs.
	 * 
	 * @param parent	the parent window (may be null).
	 * @param t			the thread that encountered the exception (may be null).
	 * @param e			the exception.
	 */
	public static void showExceptionDialog(Window parent, Thread t, Throwable e) {
		StringBuilder sb = new StringBuilder();

		// ---------- Bug report ----------
		addBugReportInformation(sb);
		
		sb.append("(Do not modify anything below this line.)\n");
		sb.append("---------- Exception stack trace ----------\n");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String stackTrace = unformatHTML(String.valueOf(sw.getBuffer()));
		sb.append(stackTrace);
		sb.append('\n');
		
		
		sb.append("---------- Thread information ----------\n");
		if (t == null) {
			sb.append("Thread is not specified.");
		} else {
			sb.append(t + "\n");
		}
		sb.append('\n');
		
		
		sb.append("---------- System information ----------\n");
		addSystemInformation(sb);
		sb.append("---------- Error log ----------\n");
		addErrorLog(sb);
		sb.append("---------- End of bug report ----------</html>\n");
		sb.append('\n');
		
		BugReportDialog reportDialog =
				//// <html><b>Please include a short description about what you were doing when the exception occurred.</b>
				new BugReportDialog(parent, trans.get("bugreport.reportDialog.txt2"), sb.toString(), true);
		reportDialog.setVisible(true);
	}

	private static void addBugReportInformation(StringBuilder sb) {
		sb.append("<html>---------- Bug report ----------\n");
		sb.append('\n');
		sb.append("<b style='color:rgb(210, 20, 5)'>Please include a description about what actions you were " +
				"performing when the exception occurred:</b>\n");
		sb.append("<i>(You can edit text directly in this window)</i>\n");
		sb.append('\n');
		sb.append("1. \n");
		sb.append("2. \n");
		sb.append("3. \n");

		sb.append("\n");
		sb.append("<b>If possible, please send us the .ork file that caused the bug.</b>\n");
		sb.append('\n');


		sb.append("Include your email address (optional; it helps if we can " +
				"contact you in case we need additional information):\n");
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
	}
	
	private static void addSystemInformation(StringBuilder sb) {
		StringBuilder sbTemp = new StringBuilder();
		sbTemp.append("OpenRocket version: " + BuildProperties.getVersion() + "\n");
		sbTemp.append("OpenRocket source: " + BuildProperties.getBuildSource() + "\n");
		sbTemp.append("OpenRocket location: " + JarUtil.getCurrentJarFile() + "\n");
		sbTemp.append("User-defined thrust curves location: " + preferences.getUserThrustCurveFilesAsString() + "\n");
		sbTemp.append("JOGL version: " + JoglVersion.getInstance().getImplementationVersion() + "\n");
		sbTemp.append("Current default locale: " + Locale.getDefault() + "\n");
		sbTemp.append("System properties:\n");

		// Sort the keys
		SortedSet<String> keys = new TreeSet<String>();
		for (Object key : System.getProperties().keySet()) {
			keys.add((String) key);
		}

		for (String key : keys) {
			String value = System.getProperty(key);
			sbTemp.append("  " + key + "=");
			if (key.equals("line.separator")) {
				for (char c : value.toCharArray()) {
					sbTemp.append(String.format("\\u%04x", (int) c));
				}
			} else {
				sbTemp.append(value);
			}
			sbTemp.append('\n');
		}

		String message = unformatHTML(sbTemp.toString());
		sb.append(message);
	}

	private static void addErrorLog(StringBuilder sb) {
		StringBuilder sbTemp = new StringBuilder();
		LogLevelBufferLogger buffer = LoggingSystemSetup.getBufferLogger();
		List<LogLine> logs = buffer.getLogs();
		for (LogLine l : logs) {
			sbTemp.append(l.toString()).append('\n');
		}

		String message = unformatHTML(sbTemp.toString());
		sb.append(message);
	}

	/**
	 * Replace newline character \n to an HTML newline. Instead of just using a <br> tag, we replace newlines with a
	 * paragraph tag with zero margin. This is so that when you copy the HTML text and paste it somewhere, that the
	 * HTML newlines are also interpreted as newlines in the new text. A <br> tag would just be replaced by a space.
	 * @param text text to be formatted
	 * @return text with HTML newlines
	 */
	private static String formatNewlineHTML(String text) {
		return text.replaceAll("\n(.*?)(?=(\n|$))", "<p style=\"margin-top: 0\">$1</p>");
	}

	/**
	 * Makes text HTML unformatted by replacing '<' and '>' by the HTML character equivalent
	 * @param text text to be replaced
	 * @return HTML unformatted text
	 */
	private static String unformatHTML(String text) {
		return text.replace("<", "&lt;").replace(">", "&gt;");
	}
	
}
