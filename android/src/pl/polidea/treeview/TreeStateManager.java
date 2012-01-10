package pl.polidea.treeview;

import java.io.Serializable;
import java.util.List;

import android.database.DataSetObserver;

/**
 * Manages information about state of the tree. It only keeps information about
 * tree elements, not the elements themselves.
 * 
 * @param <T>
 *            type of the identifier for nodes in the tree
 */
public interface TreeStateManager<T> extends Serializable {

    /**
     * Returns array of integers showing the location of the node in hierarchy.
     * It corresponds to heading numbering. {0,0,0} in 3 level node is the first
     * node {0,0,1} is second leaf (assuming that there are two leaves in first
     * subnode of the first node).
     * 
     * @param id
     *            id of the node
     * @return textual description of the hierarchy in tree for the node.
     */
    Integer[] getHierarchyDescription(T id);

    /**
     * Returns level of the node.
     * 
     * @param id
     *            id of the node
     * @return level in the tree
     */
    int getLevel(T id);

    /**
     * Returns information about the node.
     * 
     * @param id
     *            node id
     * @return node info
     */
    TreeNodeInfo<T> getNodeInfo(T id);

    /**
     * Returns children of the node.
     * 
     * @param id
     *            id of the node or null if asking for top nodes
     * @return children of the node
     */
    List<T> getChildren(T id);

    /**
     * Returns parent of the node.
     * 
     * @param id
     *            id of the node
     * @return parent id or null if no parent
     */
    T getParent(T id);

    /**
     * Adds the node before child or at the beginning.
     * 
     * @param parent
     *            id of the parent node. If null - adds at the top level
     * @param newChild
     *            new child to add if null - adds at the beginning.
     * @param beforeChild
     *            child before which to add the new child
     */
    void addBeforeChild(T parent, T newChild, T beforeChild);

    /**
     * Adds the node after child or at the end.
     * 
     * @param parent
     *            id of the parent node. If null - adds at the top level.
     * @param newChild
     *            new child to add. If null - adds at the end.
     * @param afterChild
     *            child after which to add the new child
     */
    void addAfterChild(T parent, T newChild, T afterChild);

    /**
     * Removes the node and all children from the tree.
     * 
     * @param id
     *            id of the node to remove or null if all nodes are to be
     *            removed.
     */
    void removeNodeRecursively(T id);

    /**
     * Expands all children of the node.
     * 
     * @param id
     *            node which children should be expanded. cannot be null (top
     *            nodes are always expanded!).
     */
    void expandDirectChildren(T id);

    /**
     * Expands everything below the node specified. Might be null - then expands
     * all.
     * 
     * @param id
     *            node which children should be expanded or null if all nodes
     *            are to be expanded.
     */
    void expandEverythingBelow(T id);

    /**
     * Collapse children.
     * 
     * @param id
     *            id collapses everything below node specified. If null,
     *            collapses everything but top-level nodes.
     */
    void collapseChildren(T id);

    /**
     * Returns next sibling of the node (or null if no further sibling).
     * 
     * @param id
     *            node id
     * @return the sibling (or null if no next)
     */
    T getNextSibling(T id);

    /**
     * Returns previous sibling of the node (or null if no previous sibling).
     * 
     * @param id
     *            node id
     * @return the sibling (or null if no previous)
     */
    T getPreviousSibling(T id);

    /**
     * Checks if given node is already in tree.
     * 
     * @param id
     *            id of the node
     * @return true if node is already in tree.
     */
    boolean isInTree(T id);

    /**
     * Count visible elements.
     * 
     * @return number of currently visible elements.
     */
    int getVisibleCount();

    /**
     * Returns visible node list.
     * 
     * @return return the list of all visible nodes in the right sequence
     */
    List<T> getVisibleList();

    /**
     * Registers observers with the manager.
     * 
     * @param observer
     *            observer
     */
    void registerDataSetObserver(final DataSetObserver observer);

    /**
     * Unregisters observers with the manager.
     * 
     * @param observer
     *            observer
     */
    void unregisterDataSetObserver(final DataSetObserver observer);

    /**
     * Cleans tree stored in manager. After this operation the tree is empty.
     * 
     */
    void clear();

    /**
     * Refreshes views connected to the manager.
     */
    void refresh();
}
