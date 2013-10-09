/*
 * CheckTreeSelectionModel.java
 */
package net.sf.openrocket.gui.print.components;

import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.util.ArrayList;
import java.util.Stack;

/**
 * This class implements the selection model for the checkbox tree.  This specifically is used to keep
 * track of the TreePaths that have a selected CheckBox.
 */
public class CheckTreeSelectionModel extends DefaultTreeSelectionModel {

    /**
     * The tree model.
     */
    private TreeModel model;

    /**
     * Constructor.
     *
     * @param theModel the model in use for the tree
     */
    public CheckTreeSelectionModel (TreeModel theModel) {
        model = theModel;
        setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }

    /**
     * Tests whether there is any selected node in the subtree of given path.
     *
     * @param path the path to walk
     *
     * @return true if any item in the path or its descendants are selected
     */
    public boolean isPartiallySelected (TreePath path) {
        if (isPathSelected(path, true)) {
            return false;
        }
        TreePath[] selectionPaths = getSelectionPaths();
        if (selectionPaths == null) {
            return false;
        }
        for (TreePath selectionPath : selectionPaths) {
            if (isDescendant(selectionPath, path)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tells whether given path is selected. If dig is true, then a path is assumed to be selected, if one of its
     * ancestor is selected.
     *
     * @param path the path to interrogate
     * @param dig  if true then check if an ancestor is selected
     *
     * @return true if the path is selected
     */
    public boolean isPathSelected (TreePath path, boolean dig) {
        if (!dig) {
            return super.isPathSelected(path);
        }
        while (path != null && !super.isPathSelected(path)) {
            path = path.getParentPath();
        }
        return path != null;
    }

    /**
     * Determines if path1 is a descendant of path2.
     *
     * @param path1 descendant?
     * @param path2 ancestor?
     *
     * @return true if path1 is a descendant of path2
     */
    private boolean isDescendant (TreePath path1, TreePath path2) {
        Object obj1[] = path1.getPath();
        Object obj2[] = path2.getPath();
        for (int i = 0; i < obj2.length; i++) {
            if (i < obj1.length) {
                if (obj1[i] != obj2[i]) {
                    return false;
                }
            }
            else {
                return false;
            }
        }
        return true;
    }


    /**
     * Unsupported exception.
     *
     * @param pPaths an array of paths
     */
    @Override
    public void setSelectionPaths (TreePath[] pPaths) {
        TreePath selected[] = getSelectionPaths();
        for (TreePath aSelected : selected) {
            removeSelectionPath(aSelected);
        }
        for (TreePath pPath : pPaths) {
            addSelectionPath(pPath);
        }
    }

    /**
     * Add a set of TreePath nodes to the selection model.
     *
     * @param paths an array of tree path nodes
     */
    @Override
    public void addSelectionPaths (TreePath[] paths) {
        // deselect all descendants of paths[] 
        for (TreePath path : paths) {
            TreePath[] selectionPaths = getSelectionPaths();
            if (selectionPaths == null) {
                break;
            }
            ArrayList<TreePath> toBeRemoved = new ArrayList<TreePath>();
            for (TreePath selectionPath : selectionPaths) {
                if (isDescendant(selectionPath, path)) {
                    toBeRemoved.add(selectionPath);
                }
            }
            super.removeSelectionPaths(toBeRemoved.toArray(new TreePath[toBeRemoved.size()]));
        }

        // if all siblings are selected then deselect them and select parent recursively 
        // otherwise just select that path. 
        for (TreePath path : paths) {
            TreePath temp = null;
            while (areSiblingsSelected(path)) {
                temp = path;
                if (path.getParentPath() == null) {
                    break;
                }
                path = path.getParentPath();
            }
            if (temp != null) {
                if (temp.getParentPath() != null) {
                    addSelectionPath(temp.getParentPath());
                }
                else {
                    if (!isSelectionEmpty()) {
                        removeSelectionPaths(getSelectionPaths());
                    }
                    super.addSelectionPaths(new TreePath[]{temp});
                }
            }
            else {
                super.addSelectionPaths(new TreePath[]{path});
            }
        }
    }

    /**
     * Tells whether all siblings of given path are selected.
     *
     * @param path the tree path node
     *
     * @return true if all sibling nodes are selected
     */
    private boolean areSiblingsSelected (TreePath path) {
        TreePath parent = path.getParentPath();
        if (parent == null) {
            return true;
        }
        Object node = path.getLastPathComponent();
        Object parentNode = parent.getLastPathComponent();

        int childCount = model.getChildCount(parentNode);
        for (int i = 0; i < childCount; i++) {
            Object childNode = model.getChild(parentNode, i);
            if (childNode == node) {
                continue;
            }
            if (!isPathSelected(parent.pathByAddingChild(childNode))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Remove paths from the selection model.
     *
     * @param paths the array of path nodes
     */
    @Override
    public void removeSelectionPaths (TreePath[] paths) {
        for (TreePath path : paths) {
            if (path.getPathCount() == 1) {
                super.removeSelectionPaths(new TreePath[]{path});
            }
            else {
                toggleRemoveSelection(path);
            }
        }
    }

    /**
     * If any ancestor node of given path is selected then deselect it and selection all its descendants except given
     * path and descendants. otherwise just deselect the given path.
     *
     * @param path the tree path node
     */
    private void toggleRemoveSelection (TreePath path) {
        Stack<TreePath> stack = new Stack<TreePath>();
        TreePath parent = path.getParentPath();
        while (parent != null && !isPathSelected(parent)) {
            stack.push(parent);
            parent = parent.getParentPath();
        }
        if (parent != null) {
            stack.push(parent);
        }
        else {
            super.removeSelectionPaths(new TreePath[]{path});
            return;
        }

        while (!stack.isEmpty()) {
            TreePath temp = stack.pop();
            TreePath peekPath = stack.isEmpty() ? path : stack.peek();
            Object node = temp.getLastPathComponent();
            Object peekNode = peekPath.getLastPathComponent();
            int childCount = model.getChildCount(node);
            for (int i = 0; i < childCount; i++) {
                Object childNode = model.getChild(node, i);
                if (childNode != peekNode) {
                    super.addSelectionPaths(new TreePath[]{temp.pathByAddingChild(childNode)});
                }
            }
        }
        super.removeSelectionPaths(new TreePath[]{parent});
    }
}