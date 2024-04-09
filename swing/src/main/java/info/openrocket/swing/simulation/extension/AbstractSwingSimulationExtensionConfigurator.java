package info.openrocket.swing.simulation.extension;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import info.openrocket.core.simulation.extension.SimulationExtension;
import net.miginfocom.swing.MigLayout;
import info.openrocket.core.document.Simulation;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.core.l10n.Translator;
import info.openrocket.swing.gui.widgets.SelectColorButton;

import com.google.inject.Inject;

public abstract class AbstractSwingSimulationExtensionConfigurator<E extends SimulationExtension> implements SwingSimulationExtensionConfigurator {
	
	@Inject
	protected Translator trans;
	
	private final Class<E> extensionClass;
	
	private JDialog dialog;
	
	protected AbstractSwingSimulationExtensionConfigurator(Class<E> extensionClass) {
		this.extensionClass = extensionClass;
	}
	
	
	@Override
	public boolean support(SimulationExtension extension) {
		return extensionClass.isInstance(extension);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void configure(SimulationExtension extension, Simulation simulation, Window parent) {
		dialog = new JDialog(parent, getTitle(extension, simulation), ModalityType.APPLICATION_MODAL);
		JPanel panel = new JPanel(new MigLayout("fill"));
		JPanel sub = new JPanel(new MigLayout("fill, ins 0"));
		
		panel.add(getConfigurationComponent((E) extension, simulation, sub), "grow, wrap para");
		
		JButton close = new SelectColorButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		panel.add(close, "right");

		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		dialog.add(panel);
		GUIUtil.setDisposableDialogOptions(dialog, close);
		dialog.setVisible(true);
	}
	
	/**
	 * Return a title for the dialog window.  By default uses the extension's name.
	 */
	protected String getTitle(SimulationExtension extension, Simulation simulation) {
		return extension.getName();
	}
	
	/**
	 * Return the dialog currently open.
	 */
	protected JDialog getDialog() {
		return dialog;
	}
	
	/**
	 * Called when the default dialog is closed.  By default hides the dialog and cleans up the component models.
	 */
	protected void close() {
		dialog.setVisible(false);
		GUIUtil.setNullModels(dialog);
		dialog = null;
	}
	
	protected abstract JComponent getConfigurationComponent(E extension, Simulation simulation, JPanel panel);
	
}
