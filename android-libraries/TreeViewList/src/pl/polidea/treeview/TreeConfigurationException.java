package pl.polidea.treeview;

/**
 * Exception thrown when there is a problem with configuring tree.
 * 
 */
public class TreeConfigurationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TreeConfigurationException(final String detailMessage) {
        super(detailMessage);
    }

}
