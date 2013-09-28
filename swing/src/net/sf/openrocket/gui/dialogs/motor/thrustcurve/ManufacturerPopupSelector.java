package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import net.sf.openrocket.gui.util.CheckList;
import net.sf.openrocket.motor.Manufacturer;

public abstract class ManufacturerPopupSelector extends JPopupMenu implements ActionListener {
	
	Map<String, Manufacturer> componentMap = new HashMap<String, Manufacturer>();
	CheckList list;
	
	public ManufacturerPopupSelector(Collection<Manufacturer> allManufacturers, Collection<Manufacturer> unselectedManufacturers) {
		
		JPanel root = new JPanel(new BorderLayout(3, 3));
		root.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		root.setPreferredSize(new Dimension(250, 150)); // default popup size
		
		Box commands = new Box(BoxLayout.LINE_AXIS);
		
		commands.add(Box.createHorizontalStrut(5));
		commands.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
		commands.setBackground(UIManager.getColor("Panel.background"));
		commands.setOpaque(true);
		
		JButton closeButton = new JButton("close");
		closeButton.addActionListener(this);
		commands.add(closeButton);
		
		List<String> manufacturers = new ArrayList<String>();
		for (Manufacturer m : allManufacturers) {
			manufacturers.add(m.getSimpleName());
			componentMap.put(m.getSimpleName(), m);
		}
		
		Collections.sort(manufacturers);
		
		list = new CheckList.Builder().build();
		list.setData(manufacturers);
		
		if (unselectedManufacturers != null)
		{
			for (Manufacturer m : unselectedManufacturers) {
				manufacturers.remove(m.getSimpleName());
			}
		}
		list.setCheckedItems(manufacturers);
		
		root.add(new JScrollPane(list.getList()), BorderLayout.CENTER);
		root.add(commands, BorderLayout.SOUTH);
		
		this.add(root);
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		List<Manufacturer> selectedManufacturers = new ArrayList<Manufacturer>();
		List<Manufacturer> unselectedManufacturers = new ArrayList<Manufacturer>();
		
		Collection<String> selected = list.getCheckedItems();
		for (String s : selected) {
			selectedManufacturers.add(componentMap.get(s));
		}
		
		Collection<String> unselected = list.getUncheckedItems();
		for (String s : unselected) {
			unselectedManufacturers.add(componentMap.get(s));
		}
		
		onDismissed(selectedManufacturers, unselectedManufacturers);
		setVisible(false);
	}
	
	public abstract void onDismissed(List<Manufacturer> selectedManufacturers, List<Manufacturer> unselectedManufacturers);
	
}
