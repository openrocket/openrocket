package net.sf.openrocket.gui.dialogs;

import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.gui.components.SelectableLabel;
import net.sf.openrocket.util.GUIUtil;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Prefs;

public class BugReportDialog extends JDialog {
	
	private static final String REPORT_EMAIL = "openrocket-bugs@lists.sourceforge.net";
	
	private static final String REPORT_URL = 
		"http://openrocket.sourceforge.net/actions/reportbug";

	private static final String REPORT_VERSION_PARAM = "version";
	private static final String REPORT_PARAM = "content";
	private static final int REPORT_RESPONSE_CODE = HttpURLConnection.HTTP_CREATED;
	private static final int REPORT_TIMEOUT = 10000;  // in milliseconds
	

	public BugReportDialog(Window parent, String labelText, String message) {
		super(parent, "Bug report", Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// Some fscking Swing bug that makes html labels initially way too high
		JLabel label = new JLabel(labelText);
		Dimension d = label.getPreferredSize();
		d.width = 100000;
		label.setMaximumSize(d);
		panel.add(label, "gapleft para, wrap para");

		label = new JLabel("<html>If connected to the Internet, you can simply click " +
		"<em>Send bug report</em>.");
		d = label.getPreferredSize();
		d.width = 100000;
		label.setMaximumSize(d);
		panel.add(label, "gapleft para, wrap");
		
		panel.add(new JLabel("Otherwise, send the text below to the address: "), 
				"gapleft para, split 2, gapright rel");
		panel.add(new SelectableLabel(REPORT_EMAIL), "growx, wrap para");
		

		final JTextArea textArea = new JTextArea(message, 20, 70);
		textArea.setEditable(true);
		panel.add(new JScrollPane(textArea), "grow, wrap");
		
		
		panel.add(new ResizeLabel("The information above may be included in a public " +
				"bug report.  Make sure it does not contain any sensitive information you " +
				"do not want to be made public.", -1), "wrap para");
		
		
		
		////  Close button
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				BugReportDialog.this.dispose();
			}
		});
		panel.add(close, "right, sizegroup buttons, split");
		
		
		////  Mail button
//		if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.MAIL)) {
//			JButton mail = new JButton("Open email");
//			mail.setToolTipText("Open email client with the suitable email ready.");
//			mail.addActionListener(new ActionListener() {
//				@Override
//				public void actionPerformed(ActionEvent e) {
//					String text = textArea.getText();
//					openEmail(text);
//				}
//			});
//			panel.add(mail, "right, sizegroup buttons");
//		}
		
		
		////  Send button
		JButton send = new JButton("Send bug report");
		send.setToolTipText("Automatically send the bug report to the OpenRocket developers.");
		send.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = textArea.getText();
				try {
					
					sendReport(text);
					// Success if we came here
					JOptionPane.showMessageDialog(BugReportDialog.this,
							new Object[] { "Bug report successfully sent.",
							"Thank you for helping make OpenRocket better!" },
							"Bug report sent", JOptionPane.INFORMATION_MESSAGE);
					
				} catch (Exception ex) {
					// Sending the message failed.
					JOptionPane.showMessageDialog(BugReportDialog.this,
							new Object[] { "OpenRocket was unable to send the bug report:",
							ex.getClass().getSimpleName() + ": " + ex.getMessage(), " ",
							"Please send the report manually to " + REPORT_EMAIL },
							"Error sending report", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		panel.add(send, "right, sizegroup buttons");
		
		this.add(panel);
		
		this.validate();
		this.pack();
		this.pack();
		this.setLocationRelativeTo(parent);
		GUIUtil.installEscapeCloseOperation(this);
//		GUIUtil.setDefaultButton(close);
	}

	
	
	/**
	 * Show a general bug report dialog allowing the user to input information about
	 * the bug they encountered.
	 * 
	 * @param parent	the parent window (may be null).
	 */
	public static void showBugReportDialog(Window parent) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("---------- Bug report ----------\n");
		sb.append('\n');
		sb.append("Include detailed steps on how to trigger the bug:\n");
		sb.append('\n');
		sb.append("1. \n");
		sb.append("2. \n");
		sb.append("3. \n");
		sb.append('\n');
		
		sb.append("What does the software do and what in your opinion should it do in the " +
				"case described above:\n");
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		
		sb.append("Include your email address (optional; it helps if we can " +
		"contact you in case we need additional information):\n");
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		

		sb.append("(Do not modify anything below this line.)\n");
		sb.append("---------- System information ----------\n");
		addSystemInformation(sb);
		sb.append("---------- End of information ----------\n");
		sb.append('\n');
		
		BugReportDialog reportDialog = 
			new BugReportDialog(parent,
					"<html>You can report a bug in OpenRocket by filling in and submitting " +
					"the form below.<br>" +
					"You can also report bugs and include attachments on the project " +
					"web site.", sb.toString());
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
		
		sb.append("---------- Bug report ----------\n");
		sb.append('\n');
		sb.append("Please include a description about what actions you were " +
				"performing when the exception occurred:\n");
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		

		sb.append("Include your email address (optional; it helps if we can " +
				"contact you in case we need additional information):\n");
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');
		sb.append('\n');

		sb.append("(Do not modify anything below this line.)\n");
		sb.append("---------- Exception stack trace ----------\n");
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		sb.append(sw.getBuffer());
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
		sb.append("---------- End of information ----------\n");
		sb.append('\n');
		
		BugReportDialog reportDialog = 
			new BugReportDialog(parent, "Please include a short description about " +
					"what you were doing when the exception occurred.", sb.toString());
		reportDialog.setVisible(true);
	}
	
	
	private static void addSystemInformation(StringBuilder sb) {
		sb.append("OpenRocket version: " + Prefs.getVersion() + "\n");
		sb.append("OpenRocket source: " + Prefs.getBuildSource() + "\n");
		sb.append("OpenRocket location: " + JarUtil.getCurrentJarFile() + "\n");
		sb.append("System properties:\n");

		// Sort the keys
		SortedSet<String> keys = new TreeSet<String>();
		for (Object key: System.getProperties().keySet()) {
			keys.add((String)key);
		}
		
		for (String key: keys) {
			String value = System.getProperty(key);
			sb.append("  " + key + "=");
			if (key.equals("line.separator")) {
				for (char c: value.toCharArray()) {
					sb.append(String.format("\\u%04x", (int)c));
				}
			} else {
				sb.append(value);
			}
			sb.append('\n');
		}
	}
	
	
	
	/**
	 * Send the provided report to the OpenRocket bug report URL.  If the connection
	 * fails or the server does not respond with the correct response code, an
	 * exception is thrown.
	 * 
	 * @param report		the report to send.
	 * @throws IOException	if an error occurs while connecting to the server or
	 * 						the server responds with a wrong response code.
	 */
	private void sendReport(String report) throws IOException {
		URL url = new URL(REPORT_URL);
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setConnectTimeout(REPORT_TIMEOUT);
		connection.setInstanceFollowRedirects(true);
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		
		String post;
		post = (REPORT_VERSION_PARAM + "=" + URLEncoder.encode(Prefs.getVersion(), "UTF-8")
				+ "&" + REPORT_PARAM + "=" + URLEncoder.encode(report, "UTF-8"));
		
		OutputStreamWriter wr = null;
		try {
			// Send post information
			connection.setDoOutput(true);
			wr = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
			wr.write(post);
			wr.flush();
			
			if (connection.getResponseCode() != REPORT_RESPONSE_CODE) {
				throw new IOException("Server responded with code " + 
						connection.getResponseCode() + ", expecting " + REPORT_RESPONSE_CODE);
			}
		} finally {
			if (wr != null)
				wr.close();
			connection.disconnect();
		}
	}
	

	/**
	 * Open the default email client with the suitable bug report.
	 * Note that this does not work on some systems even if Desktop.isSupported()
	 * claims so.
	 * 
	 * @param text	the bug report text.
	 * @return		whether opening the client succeeded.
	 */
	private boolean openEmail(String text) {
		String version;
		
		try {
			text = URLEncoder.encode(text, "UTF-8");
			version = URLEncoder.encode(Prefs.getVersion(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		
		
		
		String mailto = "mailto:" + REPORT_EMAIL
			+ "?subject=Bug%20report%20for%20OpenRocket%20" + version 
			+ "?body=" + text;
		URI uri;
		try {
			uri = new URI(mailto);
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return false;
		}
		
		Desktop desktop = Desktop.getDesktop();
		try {
			desktop.mail(uri);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
}
