package net.sf.openrocket.util;

import java.util.Locale;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * A strategy that performs computations on WorldCoordinates.
 * <p>
 * The directions of the coordinate is:
 *   positive X = EAST
 *   positive Y = NORTH
 *   positive Z = UPWARDS
 */
public enum GeodeticComputationStrategy {
	
	
	/**
	 * Perform computations using a flat Earth approximation.  addCoordinate computes the
	 * location using a direct meters-per-degree scaling and getCoriolisAcceleration always
	 * returns NUL.
	 */
	FLAT {
		private static final double METERS_PER_DEGREE_LATITUDE = 111325; // "standard figure"
		private static final double METERS_PER_DEGREE_LONGITUDE_EQUATOR = 111050;
		
		
		@Override
		public WorldCoordinate addCoordinate(WorldCoordinate location, Coordinate delta) {
			
			double metersPerDegreeLongitude = METERS_PER_DEGREE_LONGITUDE_EQUATOR * Math.cos(location.getLatitudeRad());
			// Limit to 1 meter per degree near poles
			metersPerDegreeLongitude = MathUtil.max(metersPerDegreeLongitude, 1);
			
			double newLat = location.getLatitudeDeg() + delta.y / METERS_PER_DEGREE_LATITUDE;
			double newLon = location.getLongitudeDeg() + delta.x / metersPerDegreeLongitude;
			double newAlt = location.getAltitude() + delta.z;
			
			return new WorldCoordinate(newLat, newLon, newAlt);
		}
		
		@Override
		public Coordinate getCoriolisAcceleration(WorldCoordinate location, Coordinate velocity) {
			return Coordinate.NUL;
		}
	},
	
	/**
	 * Perform geodetic computations with a spherical Earth approximation.
	 */
	SPHERICAL {
		
		@Override
		public WorldCoordinate addCoordinate(WorldCoordinate location, Coordinate delta) {
			double newAlt = location.getAltitude() + delta.z;
			
			// bearing (in radians, clockwise from north);
			// d/R is the angular distance (in radians), where d is the distance traveled and R is the earth's radius
			double d = MathUtil.hypot(delta.x, delta.y);
			
			// Check for zero movement before computing bearing
			if (MathUtil.equals(d, 0)) {
				return new WorldCoordinate(location.getLatitudeDeg(), location.getLongitudeDeg(), newAlt);
			}
			
			double bearing = Math.atan2(delta.x, delta.y);
			
			// Calculate the new lat and lon		
			double newLat, newLon;
			double sinLat = Math.sin(location.getLatitudeRad());
			double cosLat = Math.cos(location.getLatitudeRad());
			double sinDR = Math.sin(d / WorldCoordinate.REARTH);
			double cosDR = Math.cos(d / WorldCoordinate.REARTH);
			
			newLat = Math.asin(sinLat * cosDR + cosLat * sinDR * Math.cos(bearing));
			newLon = location.getLongitudeRad() + Math.atan2(Math.sin(bearing) * sinDR * cosLat, cosDR - sinLat * Math.sin(newLat));
			
			if (Double.isNaN(newLat) || Double.isNaN(newLon)) {
				throw new BugException("addCoordinate resulted in NaN location:  location=" + location + " delta=" + delta
						+ " newLat=" + newLat + " newLon=" + newLon);
			}
			
			return new WorldCoordinate(Math.toDegrees(newLat), Math.toDegrees(newLon), newAlt);
		}
		
		@Override
		public Coordinate getCoriolisAcceleration(WorldCoordinate location, Coordinate velocity) {
			return computeCoriolisAcceleration(location, velocity);
		}
		
	},
	
	/**
	 * Perform geodetic computations on a WGS84 reference ellipsoid using Vincenty Direct Solution.
	 */
	WGS84 {
		
		@Override
		public WorldCoordinate addCoordinate(WorldCoordinate location, Coordinate delta) {
			double newAlt = location.getAltitude() + delta.z;
			
			// bearing (in radians, clockwise from north);
			// d/R is the angular distance (in radians), where d is the distance traveled and R is the earth's radius
			double d = MathUtil.hypot(delta.x, delta.y);
			
			// Check for zero movement before computing bearing
			if (MathUtil.equals(d, 0)) {
				return new WorldCoordinate(location.getLatitudeDeg(), location.getLongitudeDeg(), newAlt);
			}
			
			double bearing = Math.atan(delta.x / delta.y);
			if (delta.y < 0)
				bearing = bearing + Math.PI;
			
			// Calculate the new lat and lon		
			double newLat, newLon;
			double ret[] = dirct1(location.getLatitudeRad(), location.getLongitudeRad(), bearing, d, 6378137, 1.0 / 298.25722210088);
			newLat = ret[0];
			newLon = ret[1];
			
			if (Double.isNaN(newLat) || Double.isNaN(newLon)) {
				throw new BugException("addCoordinate resulted in NaN location:  location=" + location + " delta=" + delta
						+ " newLat=" + newLat + " newLon=" + newLon);
			}
			
			return new WorldCoordinate(Math.toDegrees(newLat), Math.toDegrees(newLon), newAlt);
		}
		
		@Override
		public Coordinate getCoriolisAcceleration(WorldCoordinate location, Coordinate velocity) {
			return computeCoriolisAcceleration(location, velocity);
		}
	};
	
	
	private static final Translator trans = Application.getTranslator();
	
	private static final double PRECISION_LIMIT = 0.5e-13;
	
	
	/**
	 * Return the name of this geodetic computation method.
	 */
	public String getName() {
		return trans.get(name().toLowerCase(Locale.ENGLISH) + ".name");
	}
	
	/**
	 * Return a description of the geodetic computation methods.
	 */
	public String getDescription() {
		return trans.get(name().toLowerCase(Locale.ENGLISH) + ".desc");
	}
	
	@Override
	public String toString() {
		return getName();
	}
	
	
	/**
	 * Add a cartesian movement coordinate to a WorldCoordinate.
	 */
	public abstract WorldCoordinate addCoordinate(WorldCoordinate location, Coordinate delta);
	
	
	/**
	 * Compute the coriolis acceleration at a specified WorldCoordinate and velocity.
	 */
	public abstract Coordinate getCoriolisAcceleration(WorldCoordinate location, Coordinate velocity);
	
	
	
	
	
	private static Coordinate computeCoriolisAcceleration(WorldCoordinate latlon, Coordinate velocity) {
		
		double sinlat = Math.sin(latlon.getLatitudeRad());
		double coslat = Math.cos(latlon.getLatitudeRad());
		
		double v_n = velocity.y;
		double v_e = -1 * velocity.x;
		double v_u = velocity.z;
		
		// Not exactly sure why I have to reverse the x direction, but this gives the precession in the
		// correct direction (e.g, flying north in northern hemisphere should cause defection to the east (+ve x))
		// All the directions are very confusing because they are tied to the wind direction (to/from?), in which
		// +ve x or east according to WorldCoordinate is what everything is relative to.
		// The directions of everything need so thought, ideally the wind direction and launch rod should be
		// able to be set independently and in terms of bearing with north == +ve y.
		
		Coordinate coriolis = new Coordinate(2.0 * WorldCoordinate.EROT * (v_n * sinlat - v_u * coslat),
				2.0 * WorldCoordinate.EROT * (-1.0 * v_e * sinlat),
				2.0 * WorldCoordinate.EROT * (v_e * coslat)
				);
		return coriolis;
	}
	
	
	
	// ******************************************************************** //
	// The Vincenty Direct Solution.
	// Code from GeoConstants.java, Ian Cameron Smith, GPL
	// ******************************************************************** //
	
	/**
	 * Solution of the geodetic direct problem after T. Vincenty.
	 * Modified Rainsford's method with Helmert's elliptical terms.
	 * Effective in any azimuth and at any distance short of antipodal.
	 *
	 * Programmed for the CDC-6600 by lcdr L. Pfeifer, NGS Rockville MD,
	 * 20 Feb 1975.
	 *
	 * @param       glat1           The latitude of the starting point, in radians,
	 *                                              positive north.
	 * @param       glon1           The latitude of the starting point, in radians,
	 *                                              positive east.
	 * @param       azimuth         The azimuth to the desired location, in radians
	 *                                              clockwise from north.
	 * @param       dist            The distance to the desired location, in meters.
	 * @param       axis            The semi-major axis of the reference ellipsoid,
	 *                                              in meters.
	 * @param       flat            The flattening of the reference ellipsoid.
	 * @return                              An array containing the latitude and longitude
	 *                                              of the desired point, in radians, and the
	 *                                              azimuth back from that point to the starting
	 *                                              point, in radians clockwise from north.
	 */
	private static double[] dirct1(double glat1, double glon1,
			double azimuth, double dist,
			double axis, double flat) {
		double r = 1.0 - flat;
		
		double tu = r * Math.sin(glat1) / Math.cos(glat1);
		
		double sf = Math.sin(azimuth);
		double cf = Math.cos(azimuth);
		
		double baz = 0.0;
		
		if (cf != 0.0)
			baz = Math.atan2(tu, cf) * 2.0;
		
		double cu = 1.0 / Math.sqrt(tu * tu + 1.0);
		double su = tu * cu;
		double sa = cu * sf;
		double c2a = -sa * sa + 1.0;
		
		double x = Math.sqrt((1.0 / r / r - 1.0) * c2a + 1.0) + 1.0;
		x = (x - 2.0) / x;
		double c = 1.0 - x;
		c = (x * x / 4.0 + 1) / c;
		double d = (0.375 * x * x - 1.0) * x;
		tu = dist / r / axis / c;
		double y = tu;
		
		double sy, cy, cz, e;
		do {
			sy = Math.sin(y);
			cy = Math.cos(y);
			cz = Math.cos(baz + y);
			e = cz * cz * 2.0 - 1.0;
			
			c = y;
			x = e * cy;
			y = e + e - 1.0;
			y = (((sy * sy * 4.0 - 3.0) * y * cz * d / 6.0 + x) *
					d / 4.0 - cz) * sy * d + tu;
		} while (Math.abs(y - c) > PRECISION_LIMIT);
		
		baz = cu * cy * cf - su * sy;
		c = r * Math.sqrt(sa * sa + baz * baz);
		d = su * cy + cu * sy * cf;
		double glat2 = Math.atan2(d, c);
		c = cu * cy - su * sy * cf;
		x = Math.atan2(sy * sf, c);
		c = ((-3.0 * c2a + 4.0) * flat + 4.0) * c2a * flat / 16.0;
		d = ((e * cy * c + cz) * sy * c + y) * sa;
		double glon2 = glon1 + x - (1.0 - c) * d * flat;
		baz = Math.atan2(sa, baz) + Math.PI;
		
		double[] ret = new double[3];
		ret[0] = glat2;
		ret[1] = glon2;
		ret[2] = baz;
		return ret;
	}
	
	
}
