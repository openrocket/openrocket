package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import info.openrocket.swing.gui.scalefigure.RocketPanel;
import info.openrocket.swing.gui.util.GUIUtil;
import net.miginfocom.swing.MigLayout;

public class ComponentAnalysisDialog extends JDialog {
	private static final long serialVersionUID = 9131240570600307935L;
	private static ComponentAnalysisDialog singletonDialog = null;
	private static final Translator trans = Application.getTranslator();

	private JButton okButton;

	public ComponentAnalysisDialog(OpenRocketDocument document, RocketPanel rocketPanel) {
		//// Component analysis
		super(SwingUtilities.getWindowAncestor(rocketPanel), trans.get("ComponentAnalysisDialog.componentanalysis"));

		JPanel panel = new JPanel(new MigLayout("fill, hidemode 3"));
		add(panel);

		JTabbedPane tabbedPane = new JTabbedPane();

		// General tab
		ComponentAnalysisGeneralPanel generalTab = new ComponentAnalysisGeneralPanel(this, rocketPanel);
		tabbedPane.addTab(trans.get("ComponentAnalysisDialog.tab.General"), generalTab);

		// Plot export tab
		ComponentAnalysisPlotExportPanel plotExportTab = new ComponentAnalysisPlotExportPanel(this, document,
				generalTab.getParameters(), generalTab.getAerodynamicCalculator(), generalTab.getRocket());
		tabbedPane.addTab(trans.get("ComponentAnalysisDialog.tab.PlotExport"), plotExportTab);

		panel.add(tabbedPane, "span, pushy, grow, wrap");

		// Reset the singleton
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				singletonDialog = null;
			}
		});

		// Close button
		JButton closeBtn = new JButton(trans.get("dlg.but.close"));
		closeBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentAnalysisDialog.this.dispose();
			}
		});
		panel.add(closeBtn, "span, gapbefore push, split 2, right, tag cancel");

		// OK button
		this.okButton = plotExportTab.getOkButton();
		this.okButton.setVisible(false);
		panel.add(okButton, "tag cancel, wrap");

		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				showOkButton(tabbedPane.getSelectedComponent() == plotExportTab);
			}
		});

		this.setLocationByPlatform(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		validate();
		pack();

		GUIUtil.setDisposableDialogOptions(this, null);
	}

	public void showOkButton(boolean visible) {
		this.okButton.setVisible(visible);
		revalidate();
		repaint();
	}

	/////////  Singleton implementation

	public static void showDialog(OpenRocketDocument document, RocketPanel rocketpanel) {
		if (singletonDialog != null)
			singletonDialog.dispose();
		singletonDialog = new ComponentAnalysisDialog(document, rocketpanel);
		singletonDialog.setVisible(true);
	}

	public static void hideDialog() {
		if (singletonDialog != null)
			singletonDialog.dispose();
	}
}
