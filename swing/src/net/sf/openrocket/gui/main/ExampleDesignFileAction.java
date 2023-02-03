package net.sf.openrocket.gui.main;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

/**
 * Implements a menu for the example Open Rocket design files.
 */
@SuppressWarnings("serial")
public final class ExampleDesignFileAction extends JMenu {

    /**
     * The window to which an open design file action will be parented to (typically an instance of BasicFrame).
     */
    private final BasicFrame parent;

    /**
     * Order in which the example files should be displayed in the menu.
     * A null items means there should be a separator.
     * <p>
     * NOTE: update this list if you add a new example file, or update the name of an existing one!!.
     */
    private static final String[] exampleFileOrder = {
        // Examples of basic rockets
        "A simple model rocket",
        "Two-stage rocket",
        "Three-stage rocket",
        "TARC payload rocket",
        "Tube fin rocket",
        null,
        // Examples demonstrating complex rocket features
        "Airstart timing",
        "Base drag hack (short-wide)",
        "Chute release",
        "Dual parachute deployment",
        "Clustered motors",
        "Parallel booster staging",
        "Pods--airframes and winglets",
        "Pods--powered with recovery deployment",
        null,
        // Examples demonstrating customized functionality
        "Presets",
        "Simulation extensions",
        "Simulation scripting"
    };

    /**
     * Constructor.
     *
     * @param s         the I18N menu string
     * @param theParent the window to which an open design file action will be parented to (typically an instance of
     *                  BasicFrame).
     */
    public ExampleDesignFileAction(String s, BasicFrame theParent) {
        super(s);

        parent = theParent;
        updateMenu();
    }

    /**
     * Create menu items.
     */
    private void updateMenu() {
        removeAll();
        ExampleDesignFile[] examples = ExampleDesignFile.getExampleDesigns();
        List<JMenuItem> itemList = new ArrayList<>();

        // First create the menu items
        for (ExampleDesignFile file : examples) {
            Action action = createAction(file);
            action.putValue(Action.NAME, file.toString());
            JMenuItem menuItem = new JMenuItem(action);
            itemList.add(menuItem);
        }

        // Then add them according to their order
        for (String s : exampleFileOrder) {
            if (s == null) {
                addSeparator();
            } else {
                for (JMenuItem item : itemList) {
                    if (item.getText().equals(s)) {
                        add(item);
                        itemList.remove(item);
                        break;
                    }
                }
            }
        }
        
        // Add the remaining (unordered) items to the end
        if (itemList.size() > 0) {
            addSeparator();
            for (JMenuItem item : itemList) {
                add(item);
            }
        }
    }

    /**
     * When a user clicks on one of the recently used design files, open it.
     *
     * @param file the design file name (absolute path)
     *
     * @return the action to open a design file
     */
    private Action createAction(final ExampleDesignFile example) {
        Action action = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                String command = e.getActionCommand();
                BasicFrame.open(example.getURL(), parent);
            }
        };

        action.putValue(Action.ACTION_COMMAND_KEY, example.toString());
        return action;
    }

}
