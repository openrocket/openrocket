package net.sf.openrocket.simulation.extension;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;

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
		
		JButton close = new JButton(trans.get("button.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
			}
		});
		panel.add(close, "right");
		
		dialog.add(panel);
		GUIUtil.setDisposableDialogOptions(dialog, close);
		dialog.setVisible(true);
		close();
		GUIUtil.setNullModels(dialog);
		dialog = null;
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
	 * Called when the default dialog is closed.  By default does nothing.
	 */
	protected void close() {
		
	}
	
	protected abstract JComponent getConfigurationComponent(E extension, Simulation simulation, JPanel panel);
	
}
