package net.sf.openrocket.gui.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.imageio.ImageIO;

import net.sf.openrocket.l10n.LocalizedIOException;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.Coordinate;

public class CustomFinImporter {
	
	private enum FacingDirections {
		UP, DOWN, LEFT, RIGHT
	}
	
	private int startX;
	private FacingDirections facing;
	private int currentX, currentY;
	
	
	
	public List<Coordinate> getPoints(File file) throws IOException {
		ArrayList<Coordinate> points = new ArrayList<Coordinate>();
		
		BufferedImage pic = ImageIO.read(file);
		
		// Set initial values for parsing
		startX = -1;
		facing = FacingDirections.UP;
		
		if (validateImage(pic)) {
			points.add(Coordinate.NUL);
			loadFin(pic, points);
		} else {
			throw new LocalizedIOException("CustomFinImport.error.badimage");
		}
		
		optimizePoints(points);
		return points;
	}
	
	
	private boolean validateImage(BufferedImage pic) {
		int height = pic.getHeight();
		int width = pic.getWidth();
		Boolean bottomEdgeFound = false;
		
		for (int x = 0; x < width; ++x) {
			for (int y = 0; y < height; ++y) {
				int pixel = pic.getRGB(x, y) & 0x00FFFFFF; // Clear alpha, we don't care about it
				if ((pixel == 0xFFFFFF) || (pixel == 0)) // black or white only
				{
					if ((x == 0) || (x == width - 1) || (y == 0)) {
						// Left, right and top must have no black (fin)
						if (pixel == 0)
							return false;
					} else if (y == height - 1) {
						if (pixel == 0) {
							bottomEdgeFound = true;
							if (startX == -1)
								startX = x;
						}
					}
				} else {
					// Found something other than a black or white pixel
					return false;
				}
			}
		}
		return bottomEdgeFound;
	}
	
	private void loadFin(BufferedImage pic, ArrayList<Coordinate> points) {
		boolean calledTurnedAround = false;
		int height = pic.getHeight();
		
		currentX = startX;
		currentY = pic.getHeight() - 1;
		
		do {
			if (CheckLeftIsFin(pic, currentX, currentY))
				RotateLeft();
			else if (CheckForwardIsFin(pic, currentX, currentY)) {
				// Do nothing
			} else if (CheckRightIsFin(pic, currentX, currentY))
				RotateRight();
			else {
				TurnAround();
				calledTurnedAround = true;
			}
			
			MoveForward(pic);
			if (pixelIsFin(pic, currentX, currentY)) {
				if (!calledTurnedAround) {
					double x = (currentX - startX) * 0.001;
					double y = (height - currentY - 1) * 0.001;
					points.add(new Coordinate(x, y));
				} else
					calledTurnedAround = false;
			}
		} while (currentY < height - 1 && currentY >= 0);
	}
	
	private boolean pixelIsFin(BufferedImage pic, int x, int y) {
		int height = pic.getHeight();
		int width = pic.getWidth();
		
		if ((x >= 0) && (x < width) && (y >= 0) && (y < height)) {
			int pixel = pic.getRGB(x, y) & 0x00FFFFFF; // Clear alpha, we don't care about it
			
			if (pixel == 0) // black is fin
				return true;
		}
		return false;
	}
	
	private boolean CheckLeftIsFin(BufferedImage pic, int x, int y) {
		if (facing == FacingDirections.DOWN)
			return pixelIsFin(pic, x + 1, y);
		else if (facing == FacingDirections.UP)
			return pixelIsFin(pic, x - 1, y);
		else if (facing == FacingDirections.LEFT)
			return pixelIsFin(pic, x, y + 1);
		else if (facing == FacingDirections.RIGHT)
			return pixelIsFin(pic, x, y - 1);
		else
			return false;
	}
	
	private Boolean CheckRightIsFin(BufferedImage pic, int x, int y) {
		if (facing == FacingDirections.DOWN)
			return pixelIsFin(pic, x - 1, y);
		else if (facing == FacingDirections.UP)
			return pixelIsFin(pic, x + 1, y);
		else if (facing == FacingDirections.LEFT)
			return pixelIsFin(pic, x, y - 1);
		else if (facing == FacingDirections.RIGHT)
			return pixelIsFin(pic, x, y + 1);
		else
			return false;
	}
	
	private boolean CheckForwardIsFin(BufferedImage pic, int x, int y) {
		if (facing == FacingDirections.DOWN)
			return pixelIsFin(pic, x, y + 1);
		else if (facing == FacingDirections.UP)
			return pixelIsFin(pic, x, y - 1);
		else if (facing == FacingDirections.LEFT)
			return pixelIsFin(pic, x - 1, y);
		else if (facing == FacingDirections.RIGHT)
			return pixelIsFin(pic, x + 1, y);
		else
			return false;
	}
	
	private void RotateLeft() {
		if (facing == FacingDirections.UP)
			facing = FacingDirections.LEFT;
		else if (facing == FacingDirections.RIGHT)
			facing = FacingDirections.UP;
		else if (facing == FacingDirections.DOWN)
			facing = FacingDirections.RIGHT;
		else if (facing == FacingDirections.LEFT)
			facing = FacingDirections.DOWN;
	}
	
	private void RotateRight() {
		if (facing == FacingDirections.UP)
			facing = FacingDirections.RIGHT;
		else if (facing == FacingDirections.RIGHT)
			facing = FacingDirections.DOWN;
		else if (facing == FacingDirections.DOWN)
			facing = FacingDirections.LEFT;
		else if (facing == FacingDirections.LEFT)
			facing = FacingDirections.UP;
	}
	
	private void MoveForward(BufferedImage pic) {
		if (facing == FacingDirections.UP) {
			if (currentY > 0)
				currentY--;
		} else if (facing == FacingDirections.RIGHT) {
			if (currentX < pic.getWidth() - 1)
				currentX++;
		} else if (facing == FacingDirections.DOWN) {
			if (currentY < pic.getHeight() - 1)
				currentY++;
		} else if (facing == FacingDirections.LEFT) {
			if (currentX > 0)
				currentX--;
		}
	}
	
	private void TurnAround() {
		if (facing == FacingDirections.UP)
			facing = FacingDirections.DOWN;
		else if (facing == FacingDirections.DOWN)
			facing = FacingDirections.UP;
		else if (facing == FacingDirections.RIGHT)
			facing = FacingDirections.LEFT;
		else if (facing == FacingDirections.LEFT)
			facing = FacingDirections.RIGHT;
	}
	
	private void optimizePoints(ArrayList<Coordinate> points) {
		int startIx;
		ListIterator<Coordinate> start, entry, entry2;
		Coordinate startPoint, endPoint, testPoint;
		
		startIx = 0;
		start = points.listIterator();
		startPoint = start.next();
		while ((start.hasNext()) && (startPoint != points.get(points.size() - 1))) {
			entry = points.listIterator(points.size());
			endPoint = entry.previous();
			for (; endPoint != startPoint; endPoint = entry.previous()) {
				entry2 = points.listIterator(start.nextIndex());
				testPoint = entry2.next();
				for (; testPoint != endPoint; testPoint = entry2.next()) {
					if (pointDistanceFromLine(startPoint, endPoint, testPoint) > 0.001) {
						break;
					}
				}
				if ((testPoint == endPoint) && (endPoint != startPoint)) {
					// Entire segment was within distance, it's a strait line.
					// Remove all but the first and last point
					entry2 = points.listIterator(start.nextIndex());
					int nextIx = entry2.nextIndex();
					Coordinate check = entry2.next();
					while ((entry2.nextIndex() != points.size()) && (check != endPoint)) {
						entry2.remove();
						nextIx = entry2.nextIndex();
						check = entry2.next();
					}
					startIx = nextIx;
					start = points.listIterator(startIx);
					startPoint = start.next();
					break;
				}
			}
			if (endPoint == startPoint) {
				startIx = start.nextIndex();
				if (start.hasNext())
					startPoint = start.next();
			}
		}
	}
	
	private double pointDistanceFromLine(Coordinate startPoint, Coordinate endPoint, Coordinate testPoint) {
		Coordinate pt = closestPointOnSegment(startPoint, endPoint, testPoint);
		
		return testPoint.sub(pt).length();
	}
	
	private Coordinate closestPointOnSegment(Coordinate a, Coordinate b, Coordinate p) {
		Coordinate D = b.sub(a);
		double numer = p.sub(a).dot(D);
		if (numer <= 0.0f)
			return a;
		double denom = D.dot(D);
		if (numer >= denom)
			return b;
		return a.add(D.multiply(numer / denom));
	}
}
