package net.sf.openrocket.gui.main;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.configdialog.ComponentConfigDialog;
import net.sf.openrocket.gui.main.componenttree.ComponentTree;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.widgets.IconButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.AxialStage;
import net.sf.openrocket.rocketcomponent.PodSet;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static net.sf.openrocket.gui.main.BasicFrame.SHORTCUT_KEY;

/**
 * Construct the "Rocket design" tab.  This contains a horizontal split pane
 * with the left component the design tree and the right component buttons
 * for adding components.
 */
public class DesignPanel extends JSplitPane {
    private static final Translator trans = Application.getTranslator();
    private final Component tree;

    public DesignPanel(final BasicFrame parent, final OpenRocketDocument document, final ComponentTree tree) {
        super(JSplitPane.HORIZONTAL_SPLIT, true);
        setResizeWeight(0.5);
        this.tree = tree;

        //  Upper-left segment, component tree
        JPanel panel = new JPanel(new MigLayout("fill, flowy", "[grow][grow 0]","[grow]"));

        // Remove JTree key events that interfere with menu accelerators
        InputMap im = SwingUtilities.getUIInputMap(tree, JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_KEY), null);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_N, SHORTCUT_KEY), null);

        // Highlight all child components of a stage/rocket/podset when it is selected
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                highlightAssemblyChildren(tree, parent);
            }
        });

        // Add a mouse listener for when the sustainer is selected at startup, to ensure that its children are highlighted.
        // This is necessary because we force the children to not be highlighted when the tree is first created, and
        // re-clicking the sustainer would not fire a change event in the tree (which normally highlights the children).
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tree.getSelectionPath() != null &&
                        tree.getSelectionPath().getLastPathComponent() == document.getRocket().getChild(0)) {
                    highlightAssemblyChildren(tree, parent);
                }
                // Delete the listener again. We only need it at start-up, i.e. when the first click is registered.
                tree.removeMouseListener(this);
            }
        };
        tree.addMouseListener(mouseAdapter);

        // Double-click opens config dialog
        MouseListener ml = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (selPath == null) return;

                    // Double-click
                    if ((e.getButton() == MouseEvent.BUTTON1) && (e.getClickCount() == 2) && !ComponentConfigDialog.isDialogVisible()) {
                        RocketComponent component = (RocketComponent) selPath.getLastPathComponent();
                        component.clearConfigListeners();

                        // Multi-component edit if shift/meta key is pressed
                        if ((e.isShiftDown() || e.isMetaDown()) && tree.getSelectionPaths() != null) {
                            // Add the other selected components as listeners to the last selected component
                            for (TreePath p : tree.getSelectionPaths()) {
                                if (p != null) {
                                    if (p.getLastPathComponent() == component) continue;
                                    RocketComponent c = (RocketComponent) p.getLastPathComponent();
                                    c.clearConfigListeners();
                                    component.addConfigListener(c);
                                }
                            }

                            // Add the selection path to the tree selection
                            List<TreePath> paths = new LinkedList<>(Arrays.asList(tree.getSelectionPaths()));
                            paths.add(selPath);
                            tree.setSelectionPaths(paths.toArray(new TreePath[0]));
                        }

                        ComponentConfigDialog.showDialog(parent, document, component);
                    }
                    // Context menu
                    else if ((e.getButton() == MouseEvent.BUTTON3) && (e.getClickCount() == 1)) {
                        if (!tree.isPathSelected(selPath)) {
                            // Select new path
                            tree.setSelectionPath(selPath);
                        }

                        parent.doComponentTreePopup(e);
                    }
                } else {	// Clicked on blank space
                    tree.clearSelection();
                }
            }
        };
        tree.addMouseListener(ml);

        // Update dialog when selection is changed
        tree.getSelectionModel().addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                // Scroll tree to the selected item
                TreePath[] paths = tree.getSelectionModel().getSelectionPaths();
                if (paths == null || paths.length == 0)
                    return;

                for (TreePath path : paths) {
                    tree.scrollPathToVisible(path);
                }

                if (!ComponentConfigDialog.isDialogVisible())
                    return;
                else
                    ComponentConfigDialog.disposeDialog();

                RocketComponent c = (RocketComponent) paths[0].getLastPathComponent();
                c.clearConfigListeners();
                for (int i = 1; i < paths.length; i++) {
                    RocketComponent listener = (RocketComponent) paths[i].getLastPathComponent();
                    listener.clearConfigListeners();
                    c.addConfigListener(listener);
                }
                ComponentConfigDialog.showDialog(parent, document, c);
            }
        });

        // Place tree inside scroll pane
        JScrollPane scroll = new JScrollPane(tree);
        panel.add(scroll, "spany, wmin 140px, grow, wrap");


        // Buttons
        JButton button = new IconButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        RocketActions.tieActionToButton(button, parent.getRocketActions().getMoveUpAction());
        panel.add(button, "sizegroup buttons, aligny 65%");

        button = new IconButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        RocketActions.tieActionToButton(button, parent.getRocketActions().getMoveDownAction());
        panel.add(button, "sizegroup buttons, aligny 0%");

        button = new IconButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        RocketActions.tieActionToButton(button, parent.getRocketActions().getEditAction());
        button.setMnemonic(0);
        panel.add(button, "sizegroup buttons, gaptop 20%");

        button = new IconButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        RocketActions.tieActionToButton(button, parent.getRocketActions().getDuplicateAction());
        button.setMnemonic(0);
        panel.add(button, "sizegroup buttons");

        button = new IconButton();
        button.setHorizontalAlignment(SwingConstants.LEFT);
        RocketActions.tieActionToButton(button, parent.getRocketActions().getDeleteAction());
        button.setMnemonic(0);
        panel.add(button, "sizegroup buttons");

        this.setLeftComponent(panel);


        //  Upper-right segment, component addition buttons

        panel = new JPanel(new MigLayout("fill, insets 0", "[0::]"));

        scroll = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setViewportView(new ComponentAddButtons(document, tree.getSelectionModel(),
                scroll.getViewport()));
        scroll.setBorder(null);
        scroll.setViewportBorder(null);

        TitledBorder border = BorderFactory.createTitledBorder(trans.get("BasicFrame.title.Addnewcomp"));
        GUIUtil.changeFontStyle(border, Font.BOLD);
        scroll.setBorder(border);

        panel.add(scroll, "grow");

        this.setRightComponent(panel);
    }

    /**
     * Highlight all child components of a stage/rocket/podset when it is selected
     * @param tree the tree in which the component selection took place
     * @param parent the parent frame to highlight the components in
     */
    private static void highlightAssemblyChildren(ComponentTree tree, BasicFrame parent) {
        if (tree == null || tree.getSelectionPaths() == null || tree.getSelectionPaths().length == 0
                || parent.getRocketPanel() == null) return;

        // Get all the components that need to be selected = currently selected components + children of stages/boosters/podsets
        List<RocketComponent> children = new ArrayList<>(Arrays.asList(parent.getRocketPanel().getFigure().getSelection()));
        for (TreePath p : tree.getSelectionPaths()) {
            if (p != null) {
                RocketComponent c = (RocketComponent) p.getLastPathComponent();
                if (c instanceof AxialStage || c instanceof Rocket || c instanceof PodSet) {
                    Iterator<RocketComponent> iter = c.iterator(false);
                    while (iter.hasNext()) {
                        RocketComponent child = iter.next();
                        children.add(child);
                    }
                }
            }
        }

        // Select all the child components
        if (parent.getRocketPanel().getFigure() != null && parent.getRocketPanel().getFigure3d() != null) {
            parent.getRocketPanel().getFigure().setSelection(children.toArray(new RocketComponent[0]));
            parent.getRocketPanel().getFigure3d().setSelection(children.toArray(new RocketComponent[0]));
        }
    }

    /**
     * Focus on the component tree.
     */
    public void takeTheSpotlight() {
        tree.requestFocusInWindow();
    }

}
