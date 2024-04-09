package info.openrocket.swing.gui.print;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.MathUtil;

/**
 * This is the core Swing representation of a fin marking guide.  It can handle multiple fin and/or tube fin sets
 * on the same or different body tubes. One marking guide will be created for each body tube that has a fin set.
 * If a tube has multiple fin and/or tube fin sets, then they are combined onto one marking guide. It also includes
 * launch lugs and/or rail button marking line(s) if lugs or buttons are present. If (and only if) a launch lug and/or
 * rail button exists, then the word 'Front' is affixed to the leading edge of the guide to give orientation.
 * <p/>
 */
@SuppressWarnings("serial")
public class FinMarkingGuide extends JPanel {
	
	/**
	 * The stroke of normal lines.
	 */
	private final static BasicStroke thinStroke = new BasicStroke(1.0f);
	
	/**
	 * The size of the arrow in points.
	 */
	private static final int ARROW_SIZE = 10;
	
	/**
	 * Typical thickness of a piece of printer paper (~20-24 lb paper). Wrapping paper around a tube results in the
	 * radius being increased by the thickness of the paper. The smaller the tube, the more pronounced this becomes as a
	 * percentage of circumference.  Using 1/10mm as an approximation here.
	 */
	private static final double PAPER_THICKNESS_IN_METERS = PrintUnit.MILLIMETERS.toMeters(0.1d);
	
	/**
	 * The default guide width in inches.
	 */
	public final static double DEFAULT_GUIDE_WIDTH = 3d;
	
	/**
	 * 2 PI radians (represents a circle).
	 */
	public final static double TWO_PI = 2 * Math.PI;
	public final static double PI = Math.PI;
	
	/**
	 * The I18N translator.
	 */
	private static final Translator trans = Application.getTranslator();
	
	/**
	 * The margin.
	 */
	private static final int MARGIN = (int) PrintUnit.INCHES.toPoints(0.25f);
	
	/**
	 * The height (circumference) of the biggest body tube with a fin and/or tube fin set.
	 */
	private int maxHeight = 0;
	
	/**
	 * A map of body tubes, to a list of components that contain fin and/or tube fin sets and launch lugs and/or
	 * rail buttons.
	 */
	private Map<BodyTube, java.util.List<ExternalComponent>> markingGuideItems;
	
	/**
	 * Constructor.
	 *
	 * @param rocket the rocket instance
	 */
	public FinMarkingGuide(Rocket rocket) {
		super(false);
		setBackground(Color.white);
		markingGuideItems = init(rocket);
		//Max of 2 drawing guides horizontally per page.
		setSize((int) PrintUnit.INCHES.toPoints(DEFAULT_GUIDE_WIDTH) * 2 + 3 * MARGIN, maxHeight);
	}
	
	/**
	 * Initialize the marking guide class by iterating over a rocket and finding all fin and/or tube fin sets.
	 *
	 * @param component the root rocket component - this is iterated to find all fin and/or tube fin sets and
	 * launch lugs and/or rail buttons.
	 *
	 * @return a map of body tubes to lists of fin and/or tube fin sets and launch lugs and/or rail buttons.
	 */
	private Map<BodyTube, java.util.List<ExternalComponent>> init(Rocket component) {
		Iterator<RocketComponent> iter = component.iterator(false);
		Map<BodyTube, java.util.List<ExternalComponent>> results = new LinkedHashMap<BodyTube, List<ExternalComponent>>();
		BodyTube current = null;
		int totalHeight = 0;
		int iterationHeight = 0;
		int count = 0;
		
		while (iter.hasNext()) {
			RocketComponent next = iter.next();
			if (next instanceof BodyTube) {
				current = (BodyTube) next;
			}

			// IF Existence of FinSet or TubeFinSet or LaunchLug or RailButton
			else if (next instanceof FinSet || next instanceof TubeFinSet || next instanceof LaunchLug || next instanceof RailButton) {
				java.util.List<ExternalComponent> list = results.get(current);
				if (list == null && current != null) {
					list = new ArrayList<ExternalComponent>();
					results.put(current, list);
					
					double radius = current.getOuterRadius();
					int circumferenceInPoints = (int) PrintUnit.METERS.toPoints(radius * TWO_PI);
					
					// Find the biggest body tube circumference.
					if (iterationHeight < (circumferenceInPoints + MARGIN)) {
						iterationHeight = circumferenceInPoints + MARGIN;
					}
					//At most, two marking guides horizontally.  After that, move down and back to the left margin.
					count++;
					if (count % 2 == 0) {
						totalHeight += iterationHeight;
						iterationHeight = 0;
					}
				}
				if (list != null) {
					list.add((ExternalComponent) next);
				}
			}
		}
		maxHeight = totalHeight + iterationHeight;
		return results;
	}
	
	/**
	 * Returns a generated image of the fin marking guide.  May then be used wherever AWT images can be used, or
	 * converted to another image/picture format and used accordingly.
	 *
	 * @return an awt image of the fin marking guide
	 */
	public Image createImage() {
		int width = getWidth() + 25;
		int height = getHeight() + 25;
		// Create a buffered image in which to draw
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// Create a graphics context on the buffered image
		Graphics2D g2d = bufferedImage.createGraphics();
		// Draw graphics
		g2d.setBackground(Color.white);
		g2d.clearRect(0, 0, width, height);
		paintComponent(g2d);
		// Graphics context no longer needed so dispose it
		g2d.dispose();
		return bufferedImage;
	}
	
	/**
	 * <pre>
	 *   ---------------------- Page Edge --------------------------------------------------------
	 *   |                                        ^
	 *   |                                        |
	 *   |
	 *   |                                        y
	 *   |
	 *   |                                        |
	 *   P                                        v
	 *   a      ---                 +----------------------------+  <- radialOrigin (in radians)
	 *   g<------^-- x ------------>+                            +
	 *   e       |                  +                            +
	 *           |                  +                            +
	 *   E       |                  +                            +
	 *   d       |                  +<----------Fin------------->+ <- y+ (finRadialPosition - radialOrigin) / TWO_PI * circumferenceInPoints
	 *   g       |                  +                            +
	 *   e       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +<----------Fin------------->+
	 *   |       |                  +                            +
	 *   | circumferenceInPoints    +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +<------Launch Lug --------->+
	 *   |       |                  +         and/or             +
	 *   |       |                  +      Rail Buttons          +
	 *   |       |                  +                            +
	 *   |       |                  +<----------Fin------------->+
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       |                  +                            +
	 *   |       v                  +                            +
	 *   |      ---                 +----------------------------+ <- radialOrigin + TWO_PI
	 *
	 *                              |<-------- width ----------->|
	 *
	 * yLLOffset is computed from the difference between the base rotation of the fin and the radial direction of the
	 * lug.
	 *
	 * Note: There is a current limitation that a tube with multiple launch lugs and/or rail buttons may not render
	 * the lug and/or button lines correctly.
	 * </pre>
	 *
	 * @param g the Graphics context
	 */
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		paintFinMarkingGuide(g2);
	}
	
	private void paintFinMarkingGuide(Graphics2D g2) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		final Color lineColor = Color.BLACK;

		g2.setColor(lineColor);
		g2.setStroke(thinStroke);
		int x = MARGIN;
		int y = MARGIN;
		
		int width = (int) PrintUnit.INCHES.toPoints(DEFAULT_GUIDE_WIDTH);
		int length;

		int column = 0;
		
		for (BodyTube next : markingGuideItems.keySet()) {
			double circumferenceInPoints = PrintUnit.METERS.toPoints((next.getOuterRadius() + PAPER_THICKNESS_IN_METERS) *
					TWO_PI);
			List<ExternalComponent> componentList = markingGuideItems.get(next);
			//Don't draw the lug if there are no fins.
			if (hasFins(componentList)) {
				length = (int) Math.ceil(circumferenceInPoints);
				drawMarkingGuide(g2, x, y, length, width);
				
				double radialOrigin = findRadialOrigin(componentList);
				
				boolean hasMultipleComponents = componentList.size() > 1;
				
				//fin1: 42  fin2: 25
				for (ExternalComponent externalComponent : componentList) {

					// BEGIN If FinSet instance
					if (externalComponent instanceof FinSet) {
						FinSet fins = (FinSet) externalComponent;
						int finCount = fins.getFinCount();
						double baseAngularSpacing = (TWO_PI / finCount);
						double baseAngularOffset = fins.getBaseRotation();
						//Draw the fin marking lines.
						for (int fin = 0; fin < finCount; fin++) {
							double angle = baseAngularOffset + fin * baseAngularSpacing - radialOrigin;
							// Translate angle into pixels using a linear transformation:
							// radialOrigin -> y
							// radialOrigin + TWO_PI -> y + circumferenceInPoints
							
							while (angle < 0) {
								angle += TWO_PI;
							}
							while (angle > TWO_PI) {
								angle -= TWO_PI;
							}

							final int yFinCenter = (int) Math.round(y + angle / TWO_PI * circumferenceInPoints);
							final int yStart;
							final int yEnd;

							// Account for canted fins
							/*
							The arrow will be rotated around the aft base end of the fin.
							This is because the aft end will most likely be at the aft end of the body tube.
							If we were to rotate around the fore end, there's a good chance that the marking guide
							extends beyond the body tube aft end and thus you cannot draw the arrow.
							 */
							final double cantAngle = fins.getCantAngle();
							final boolean isCanted = !MathUtil.equals(cantAngle, 0);
							if (isCanted) {
								// We want to end the arrow at the aft end of the fin, so we need add an offset to
								// the end to account for the y-shift of the aft end of the fin due to the cant.
								final double finBaseHalfWidth = PrintUnit.METERS.toPoints(fins.getLength()) / 2;
								final int yFinForeEndOffset = - (int) Math.round(finBaseHalfWidth * Math.sin(cantAngle));
								yStart = yFinCenter + yFinForeEndOffset;

								// Calculate y offset of end point
								int yOffset = (int) Math.round(width * Math.tan(cantAngle));
								yEnd = yStart + yOffset;
							} else {
								yStart = yFinCenter;
								yEnd = yFinCenter;
							}

							// Draw double arrow
							drawDoubleArrowLine(g2, x, yStart, x + width, yEnd, cantAngle);

							// Draw horizontal dotted line where fin aft end is, vertical dotted line where the fore end is
							// and cross at the fin center
							if (isCanted) {
								//// -- Aft end dashed line
								// Dashed stroke settings
								float originalLineWidth = thinStroke.getLineWidth();
								float[] dashPattern = {10, 10};  // 10 pixel dash, 10 pixel space
								Stroke dashedStroke = new BasicStroke(
										originalLineWidth,
										thinStroke.getEndCap(),
										thinStroke.getLineJoin(),
										thinStroke.getMiterLimit(),
										dashPattern,
										0
								);

								// Set color and stroke
								g2.setColor(new Color(200, 200, 200));
								g2.setStroke(dashedStroke);

								// Draw aft end horizontal dashed line
								// 		We draw from right to left to ensure that the side where the side does not touch
								// 		with an arrow point (fore end) has the dashed line touching the marking guide edge
								//		(is useful for marking the fin position)
								g2.drawLine(x + width, yStart, x, yStart);

								//// -- Fore end dashed line
								// Dashed stroke settings
								dashPattern = new float[] {4, 6};  // 4 pixel dash, 6 pixel space
								dashedStroke = new BasicStroke(
										originalLineWidth * 0.7f,
										thinStroke.getEndCap(),
										thinStroke.getLineJoin(),
										thinStroke.getMiterLimit(),
										dashPattern,
										0
								);

								// Set color and stroke
								g2.setColor(new Color(220, 220, 220));
								g2.setStroke(dashedStroke);

								// Draw fore end vertical dashed line
								final int finBaseWidth = (int) PrintUnit.METERS.toPoints(fins.getLength());
								if (finBaseWidth < width) {
									g2.drawLine(x + finBaseWidth, y, x + finBaseWidth, y + length);
								}

								// Reset stroke
								g2.setStroke(thinStroke);

								//// -- Cross
								final double finBaseHalfWidth = PrintUnit.METERS.toPoints(fins.getLength()) / 2;

								// The cant also has an x-shift. We want the aft end to be perfectly flush with the
								// left of the marking guide, so apply an x-shift to fin center position
								int xFinCenter = x + (int) Math.round(finBaseHalfWidth);
								int xFinCenterOffset = - (int) Math.round(finBaseHalfWidth * (1 - Math.cos(cantAngle)));
								xFinCenter += xFinCenterOffset;

								// Draw a cross where the center of the fin should be
								int crossSize = 3;
								g2.drawLine(xFinCenter-crossSize, yFinCenter-crossSize, xFinCenter+crossSize, yFinCenter+crossSize);
								g2.drawLine(xFinCenter-crossSize, yFinCenter+crossSize, xFinCenter+crossSize, yFinCenter-crossSize);

								// Reset color
								g2.setColor(lineColor);
							}

							// Draw fin name
							final int xText = x + (width / 3);
							int yText = yStart - 2;
							if (isCanted) {
								int yTextOffset = (int) Math.round((xText - x) * Math.tan(cantAngle));
								yText += yTextOffset;

								AffineTransform orig = g2.getTransform();
								g2.rotate(cantAngle, xText, yText);       	// Rotate text for canted fins
								g2.drawString(externalComponent.getName(), xText, yText);
								g2.setTransform(orig);						// Stop rotation
							} else {
								g2.drawString(externalComponent.getName(), xText, yText);
							}
						}
					}
					// END If FinSet instance

					// BEGIN If TubeFinSet instance
					if (externalComponent instanceof TubeFinSet) {
						TubeFinSet fins = (TubeFinSet) externalComponent;
						int finCount = fins.getFinCount();
						double baseAngularSpacing = (TWO_PI / finCount);
						double baseAngularOffset = fins.getBaseRotation();
						//Draw the fin marking lines.
						for (int fin = 0; fin < finCount; fin++) {
							double angle = baseAngularOffset + fin * baseAngularSpacing - radialOrigin;
							// Translate angle into pixels using a linear transformation:
							// radialOrigin -> y
							// radialOrigin + TWO_PI -> y + circumferenceInPoints

							while (angle < 0) {
								angle += TWO_PI;
							}
							while (angle > TWO_PI) {
								angle -= TWO_PI;
							}

							int offset = (int) Math.round(y + angle / TWO_PI * circumferenceInPoints);

							drawDoubleArrowLine(g2, x, offset, x + width, offset);
							g2.drawString(externalComponent.getName(), x + (width / 3), offset - 2);
						}
					}
					// END If TubeFinSet instance

					// BEGIN If LaunchLug instance
					else if (externalComponent instanceof LaunchLug) {
						LaunchLug lug = (LaunchLug) externalComponent;
						double angle = lug.getAngleOffset() - radialOrigin;
						while (angle < 0) {
							angle += TWO_PI;
						}
						int yLLOffset = (int) Math.round(y + angle / TWO_PI * circumferenceInPoints);
						drawDoubleArrowLine(g2, x, (int) yLLOffset, x + width, (int) yLLOffset);
						g2.drawString(lug.getName(), x + (width / 3), (int) yLLOffset - 2);
						
					}
					// END If LaunchLug instance

					// BEGIN If RailButton instance
					else if (externalComponent instanceof RailButton) {
						RailButton button = (RailButton) externalComponent;
						double angle = button.getAngleOffset() - radialOrigin;
						while (angle < 0) {
							angle += TWO_PI;
						}
						int yLLOffset = (int) Math.round(y + angle / TWO_PI * circumferenceInPoints);
						drawDoubleArrowLine(g2, x, (int) yLLOffset, x + width, (int) yLLOffset);
						g2.drawString(button.getName(), x + (width / 3), (int) yLLOffset - 2);

					}
					// END If RailButton instance
				}
				// Only if the tube has a lug or button or multiple fin and/or tube fin sets does the orientation of
				// the marking guide matter. So print 'Fore end'.
				if (hasMultipleComponents) {
					drawFrontIndication(g2, x, y + length, width);
				}
				
				// At most, two marking guides horizontally.  After that, move down and back to the left margin.
				column++;
				if (column % 2 == 0) {
					x = MARGIN;
					y += circumferenceInPoints + MARGIN;
				}
				else {
					x += MARGIN + width;
				}
			}
		}
	}
	
	/**
	 * This function finds an origin in radians for the template so no component is on the template seam.
	 * 
	 * If no fin, or launch lug or rail button is at 0.0 radians, then the origin is 0.  If there is one, then half
	 * the distance between the two are taken.
	 * 
	 * @param components
	 * @return
	 */
	private double findRadialOrigin(List<ExternalComponent> components) {
		
		ArrayList<Double> positions = new ArrayList<Double>(3 * components.size());
		
		for (ExternalComponent component : components) {

			if (component instanceof LaunchLug) {  			      // Instance of LaunchLug
				double componentPosition = ((LaunchLug) component).getAngleOffset();
				positions.add(makeZeroTwoPi(componentPosition));
			}

			if (component instanceof RailButton) { 			      // Instance of RailButton
				double componentPosition = ((RailButton) component).getAngleOffset();
				positions.add(makeZeroTwoPi(componentPosition));
			}

			if (component instanceof FinSet) {                // Instance of FinSet
				FinSet fins = (FinSet) component;
				double basePosition = fins.getBaseRotation();
				double angle = TWO_PI / fins.getFinCount();
				for (int i = fins.getFinCount(); i > 0; i--) {
					positions.add(makeZeroTwoPi(basePosition));
					basePosition += angle;
				}
			}

			if (component instanceof TubeFinSet) {            // Instance of TubeFinSet
				TubeFinSet fins = (TubeFinSet) component;
				double basePosition = fins.getBaseRotation();
				double angle = TWO_PI / fins.getFinCount();
				for (int i = fins.getFinCount(); i > 0; i--) {
					positions.add(makeZeroTwoPi(basePosition));
					basePosition += angle;
				}
			}
		}
		
		Collections.sort(positions);
		
		Double[] pos = positions.toArray(new Double[0]);
		
		if (pos.length == 1) {
			
			return makeZeroTwoPi(pos[0] + PI);
			
		}
		
		double biggestDistance = TWO_PI - pos[pos.length - 1] + pos[0];
		double center = makeZeroTwoPi(pos[0] - biggestDistance / 2.0);
		
		for (int i = 1; i < pos.length; i++) {
			
			double d = pos[i] - pos[i - 1];
			if (d > biggestDistance) {
				biggestDistance = d;
				center = makeZeroTwoPi(pos[i - 1] + biggestDistance / 2.0);
			}
		}
		
		return center;
	}
	
	private static double makeZeroTwoPi(double value) {
		
		double v = value;
		while (v < 0) {
			v += TWO_PI;
		}
		while (v > TWO_PI) {
			v -= TWO_PI;
		}
		
		return v;
	}
	
	/**
	 * Determine if the list contains a FinSet or TubeFinSet.
	 *
	 * @param list a list of ExternalComponent
	 *
	 * @return true if the list contains at least one FinSet or TubeFinSet
	 */
	private boolean hasFins(List<ExternalComponent> list) {
		for (ExternalComponent externalComponent : list) {
			if (externalComponent instanceof FinSet) {           // ACTION Existence of FinSet
				return true;
			}
			if (externalComponent instanceof TubeFinSet) {       // ACTION Existence of TubeFinSet
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Draw the marking guide outline.
	 *
	 * @param g2     the graphics context
	 * @param x      the starting x coordinate
	 * @param y      the starting y coordinate
	 * @param length the length, or height, in print units of the marking guide; should be equivalent to the outer tube
	 *               circumference
	 * @param width  the width of the marking guide in print units; somewhat arbitrary
	 */
	private void drawMarkingGuide(Graphics2D g2, int x, int y, int length, int width) {
		Path2D outline = new Path2D.Float(GeneralPath.WIND_EVEN_ODD, 4);
		outline.moveTo(x, y);
		outline.lineTo(width + x, y);
		outline.lineTo(width + x, length + y);
		outline.lineTo(x, length + y);
		outline.closePath();
		g2.draw(outline);
		
		//Draw tick marks for alignment, 1/4 of the width in from either edge
		int fromEdge = (width) / 4;
		final int tickLength = 8;
		//Upper left
		g2.drawLine(x + fromEdge, y, x + fromEdge, y + tickLength);
		//Upper right
		g2.drawLine(x + width - fromEdge, y, x + width - fromEdge, y + tickLength);
		//Lower left
		g2.drawLine(x + fromEdge, y + length - tickLength, x + fromEdge, y + length);
		//Lower right
		g2.drawLine(x + width - fromEdge, y + length - tickLength, x + width - fromEdge, y + length);

		drawFrontIndication(g2, x, y + length, width);
	}
	
	/**
	 * Draw a tab indicating the fore end of the rocket.  This is necessary when a launch lug and/or
	 * rail button exists to give proper orientation of the guide (assuming that the lug and/or button is
	 * asymmetrically positioned with respect to a fin). Also necessary for canted fins.
	 *
	 * @param g2      the graphics context
	 * @param x       the starting x coordinate
	 * @param y       the starting y coordinate
	 * @param width   the width of the marking guide in print units; somewhat arbitrary
	 */
	private void drawFrontIndication(Graphics2D g2, int x, int y, int width) {
		// Draw a tab at the bottom of the marking guide to indicate the fore end of the rocket
		int tabWidth = (int) Math.round(width * 0.8);
		int tabSpacing = (width - tabWidth) / 2;
		int tabHeight = 20;
		float strokeWidth = 1.0f;
		float strokeOffset = thinStroke.getLineWidth() / 2;		// Offset to not draw over the marking guide stroke

		Stroke origStroke = g2.getStroke();
		Stroke stroke = new BasicStroke(strokeWidth, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL);
		g2.setStroke(stroke);

		// Draw the tab outline
		Path2D tab = new Path2D.Float(GeneralPath.WIND_EVEN_ODD, 4);
		tab.moveTo(x, y + strokeOffset);
		tab.lineTo(x + width, y + strokeOffset);
		tab.lineTo(x + width - tabSpacing, y + tabHeight);
		tab.lineTo(x + tabSpacing, y + tabHeight);
		tab.closePath();
		g2.draw(tab);

		// Reset the stroke
		g2.setStroke(origStroke);

		// Fill in the tab
		Color color = g2.getColor();
		g2.setColor(new Color(220, 220, 220));
		g2.fill(tab);
		g2.setColor(color);

		// Draw an arrow to the left and the text "Fore"
		final int arrowXStart = x + width - tabSpacing - 5;
		final int arrowWidth = 50;
		final int arrowY = y + (tabHeight / 2);
		final int textY = arrowY + g2.getFontMetrics().getHeight() / 2 - 3;
		drawRightArrowLine(g2, arrowXStart - arrowWidth, arrowY, arrowXStart, arrowY);
		String frontText = trans.get("FinMarkingGuide.lbl.Front");
		final int textWidth = g2.getFontMetrics().stringWidth(frontText);
		g2.drawString(frontText, arrowXStart - arrowWidth - textWidth - 3, textY);
	}

	/**
	 * Draw a horizontal line with arrows on only the right endpoint.
	 *
	 * @param g2 the graphics context
	 * @param x1 the starting x coordinate
	 * @param y1 the starting y coordinate
	 * @param x2 the ending x coordinate
	 * @param y2 the ending y coordinate
	 */
	void drawRightArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
		drawArrowLine(g2, x1, y1, x2, y2, 0, false, true);
	}
	
	/**
	 * Draw a horizontal line with arrows on both endpoints.  Depicts a fin alignment.
	 *
	 * @param g2 the graphics context
	 * @param x1 the starting x coordinate
	 * @param y1 the starting y coordinate
	 * @param x2 the ending x coordinate
	 * @param y2 the ending y coordinate
	 * @param angle angle to rotate the arrow header to
	 * @param leftArrow true if the left arrow should be drawn
	 * @param rightArrow true if the right arrow should be drawn
	 */
	void drawArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2, double angle, boolean leftArrow, boolean rightArrow) {
		int len = x2 - x1;

		// Draw line
		int xOffset = (int) Math.round(ARROW_SIZE * Math.abs(Math.cos(angle)));
		int yOffset = (int) Math.round(ARROW_SIZE * Math.sin(angle));
		g2.drawLine(x1 + xOffset, y1 + yOffset, x1 + len - xOffset, y2 - yOffset);

		// Rotate for the right arrow
		AffineTransform orig = g2.getTransform();
		g2.rotate(angle, x1 + len, y2);

		// Draw right arrow
		if (rightArrow) {
			g2.fillPolygon(new int[]{x1 + len, x1 + len - ARROW_SIZE, x1 + len - ARROW_SIZE, x1 + len},
					new int[]{y2, y2 - ARROW_SIZE / 2, y2 + ARROW_SIZE / 2, y2}, 4);
		}

		// Rotate for the left arrow
		g2.setTransform(orig);
		g2.rotate(angle, x1, y1);

		// Draw left arrow
		if (leftArrow) {
			g2.fillPolygon(new int[]{x1, x1 + ARROW_SIZE, x1 + ARROW_SIZE, x1},
					new int[]{y1, y1 - ARROW_SIZE / 2, y1 + ARROW_SIZE / 2, y1}, 4);
		}

		g2.setTransform(orig);
	}

	/**
	 * Draw a horizontal line with arrows on both endpoints.  Depicts a fin alignment.
	 *
	 * @param g2 the graphics context
	 * @param x1 the starting x coordinate
	 * @param y1 the starting y coordinate
	 * @param x2 the ending x coordinate
	 * @param y2 the ending y coordinate
	 * @param angle angle to rotate the arrow header to
	 */
	void drawDoubleArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2, double angle) {
		drawArrowLine(g2, x1, y1, x2, y2, angle, true, true);
	}

	/**
	 * Draw a horizontal line with arrows on both endpoints.  Depicts a fin alignment.
	 *
	 * @param g2 the graphics context
	 * @param x1 the starting x coordinate
	 * @param y1 the starting y coordinate
	 * @param x2 the ending x coordinate
	 * @param y2 the ending y coordinate
	 */
	void drawDoubleArrowLine(Graphics2D g2, int x1, int y1, int x2, int y2) {
		drawDoubleArrowLine(g2, x1, y1, x2, y2, 0);
	}
}
