package net.sf.openrocket.gui.configdialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import net.sf.openrocket.rocketcomponent.FinSet;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.RocketComponent;


public abstract class FinSetConfig extends RocketComponentConfig {

	private JButton split = null;
	
	public FinSetConfig(RocketComponent component) {
		super(component);
	}

	
	
	protected void addFinSetButtons() {
		JButton convert=null;
		
		//// Convert buttons
		if (!(component instanceof FreeformFinSet)) {
			convert = new JButton("Convert to freeform");
			convert.setToolTipText("Convert this fin set into a freeform fin set");
			convert.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// Do change in future for overall safety
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							FreeformFinSet freeform = new FreeformFinSet((FinSet)component);
							String name = component.getComponentName();
							
							if (freeform.getName().startsWith(name)) {
								freeform.setName(freeform.getComponentName() + 
										freeform.getName().substring(name.length()));
							}
							
							RocketComponent parent = component.getParent();
							int index = parent.getChildPosition(component);

							ComponentConfigDialog.addUndoPosition("Convert fin set");
							parent.removeChild(index);
							parent.addChild(freeform, index);
							ComponentConfigDialog.showDialog(freeform);
						}
					});

					ComponentConfigDialog.hideDialog();
				}
			});
		}

		split = new JButton("Split fins");
		split.setToolTipText("Split the fin set into separate fins");
		split.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Do change in future for overall safety
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						RocketComponent parent = component.getParent();
						int index = parent.getChildPosition(component);
						int count = ((FinSet)component).getFinCount();
						double base = ((FinSet)component).getBaseRotation();
						if (count <= 1)
							return;
						
						ComponentConfigDialog.addUndoPosition("Split fin set");
						parent.removeChild(index);
						for (int i=0; i<count; i++) {
							FinSet copy = (FinSet)component.copy();
							copy.setFinCount(1);
							copy.setBaseRotation(base + i*2*Math.PI/count);
							copy.setName(copy.getName() + " #" + (i+1));
							parent.addChild(copy, index+i);
						}
					}
				});

				ComponentConfigDialog.hideDialog();
			}
		});
		split.setEnabled(((FinSet)component).getFinCount() > 1);
		
		if (convert==null)
			addButtons(split);
		else
			addButtons(split,convert);

	}


	@Override
	public void updateFields() {
		super.updateFields();
		if (split != null)
			split.setEnabled(((FinSet)component).getFinCount() > 1);
	}
}
