package pl.polidea.treeview;

/**
 * The node being added is already in the tree.
 * 
 */
public class NodeAlreadyInTreeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NodeAlreadyInTreeException(final String id, final String oldNode) {
        super("The node has already been added to the tree: " + id + ". Old node is:" + oldNode);
    }

}
