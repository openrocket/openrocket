package info.openrocket.swing.gui.dialogs.flightconfiguration;

import java.awt.Color;
import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.configdialog.CommonStrings;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SelectColorButton;

public class RenameConfigDialog extends JDialog {
	private static final long serialVersionUID = -5423008694485357248L;
	private static final Translator trans = Application.getTranslator();

	private static Color dimTextColor;

	static {
		initColors();
	}

	public RenameConfigDialog(final Window parent, final Rocket rocket, final FlightConfigurationId fcid) {
		super(parent, trans.get("RenameConfigDialog.title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		panel.add(new JLabel(trans.get("RenameConfigDialog.lbl.name") + " " + CommonStrings.dagger), "span, wrap rel");
		
		final JTextField textbox = new JTextField(rocket.getFlightConfiguration(fcid).getNameRaw());
		panel.add(textbox, "span, w 200lp, growx, wrap para");
		
		panel.add(new JPanel(), "growx");
		
		JButton okButton = new SelectColorButton(trans.get("button.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newName = textbox.getText();
				rocket.getFlightConfiguration(fcid).setName( newName);
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(okButton);
		
		JButton resetToDefaultButton = new SelectColorButton(trans.get("RenameConfigDialog.but.reset"));
		resetToDefaultButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				rocket.getFlightConfiguration(fcid).setName(null);
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(resetToDefaultButton);
		
		JButton cancel = new SelectColorButton(trans.get("button.cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RenameConfigDialog.this.setVisible(false);
			}
		});
		panel.add(cancel, "wrap para");

		// {motors} & {manufacturers} info
		String text = "<html>" + CommonStrings.dagger + " " + trans.get("RenameConfigDialog.lbl.infoMotors")
				+ trans.get("RenameConfigDialog.lbl.infoManufacturers")
				+ trans.get("RenameConfigDialog.lbl.infoCases")
				+ trans.get("RenameConfigDialog.lbl.infoCombination");
		StyledLabel info = new StyledLabel(text, -2);
		info.setFontColor(dimTextColor);
		panel.add(info, "spanx, growx, wrap");
		
		this.add(panel);
		
		GUIUtil.setDisposableDialogOptions(this, okButton);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(RenameConfigDialog::updateColors);
	}

	private static void updateColors() {
		dimTextColor = GUIUtil.getUITheme().getDimTextColor();
	}
}
