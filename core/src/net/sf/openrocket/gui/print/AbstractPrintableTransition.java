package net.sf.openrocket.gui.print;

import net.sf.openrocket.rocketcomponent.Transition;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public abstract class AbstractPrintableTransition extends PrintableComponent {
    /**
     * The stroke of the transition arc.
     */
    private final static BasicStroke thinStroke = new BasicStroke(1.0f);

    /**
     * The X margin.
     */
    protected int marginX = (int) PrintUnit.INCHES.toPoints(0.25f);

    /**
     * The Y margin.
     */
    protected int marginY = (int) PrintUnit.INCHES.toPoints(0.25f);
    
    /**
     * Constructor. Initialize this printable with the component to be printed.
     *
     * @param isDoubleBuffered  a boolean, true for double-buffering
     * @param transition  the component to be printed
     */
    public AbstractPrintableTransition(boolean isDoubleBuffered, Transition transition) {
        init(transition);
    }

    /**
     * Compute the basic values of each arc of the transition/shroud.  This is adapted from
     * <a href="http://www.rocketshoppe.com/info/Transitions.pdf">The Properties of
     * Model Rocket Body Tube Transitions, by J.R. Brohm</a>
     *
     * @param component the transition component
     */
    protected abstract void init(Transition component);

    /**
     * Draw the component onto the graphics context.
     *
     * @param g2 the graphics context
     */
    protected abstract void draw(Graphics2D g2);
    
    /**
     * Returns a generated image of the transition.  May then be used wherever AWT images can be used, or converted to
     * another image/picture format and used accordingly.
     *
     * @return an awt image of the fin set
     */
    public Image createImage() {
        int width = getWidth() + marginX;
        int height = getHeight() + marginY;
        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        // Draw graphics
        g2d.setBackground(Color.white);
        g2d.clearRect(0, 0, width, height);
        paintComponent(g2d);
        // Graphics context no longer needed so dispose it
        g2d.dispose();
        return bufferedImage;
    }

    @Override
    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(Color.BLACK);
        g2.setStroke(thinStroke);
		g2.translate(getOffsetX(), getOffsetY());

        draw(g2);
    }
}
