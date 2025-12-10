package info.openrocket.swing.gui.figure3d.core.geography;

import info.openrocket.swing.gui.figure3d.constants.GeometryConstants;
import info.openrocket.swing.gui.figure3d.core.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.core.geometry.basic.PlaneGenerator;
import info.openrocket.swing.gui.figure3d.materials.Appearance3D;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.scene.core.SceneObject;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

/**
 * A factory class for creating terrain SceneObjects from various data sources.
 */
public class TerrainGenerator {

	private static final Logger log = LoggerFactory.getLogger(TerrainGenerator.class);

	private final MapTileService mapTileService;

	public TerrainGenerator() {
		this.mapTileService = new MapTileService();
	}

	/**
	 * Creates a tileable terrain SceneObject from a local image file.
	 * @param filePath The path to the image file.
	 * @param size The size of the ground plane in world units.
	 * @param tiling The number of times the texture should tile across the plane (e.g., 10.0f).
	 * @return A SceneObject representing the terrain floor.
	 * @throws Exception if the image file cannot be found or loaded.
	 */
	public SceneObject createTerrainFromFile(String filePath, float size, float tiling) throws Exception {
		Texture terrainTexture = new Texture(filePath);
		return createTerrain(terrainTexture, size, tiling);
	}

	public SceneObject createTerrainFromFile(String filePath, float size) throws Exception {
		return createTerrainFromFile(filePath, size, 1.0f);
	}

	/**
	 * Creates a non-tiled terrain SceneObject from a GPS coordinate by downloading a satellite image.
	 * @param latitude The latitude of the location.
	 * @param longitude The longitude of the location.
	 * @param zoom The map zoom level (e.g., 1 to 18).
	 * @param size The size of the ground plane in world units.
	 * @return A SceneObject representing the terrain floor.
	 * @throws Exception if the map tile cannot be downloaded or processed.
	 */
	public SceneObject createGpsTerrain(double latitude, double longitude, int zoom, float size) throws Exception {
		int tileX = GpsUtils.long2tilex(longitude, zoom);
		int tileY = GpsUtils.lat2tiley(latitude, zoom);
		log.debug("Fetching map tile for lat={}, lon={} at zoom {} -> Tile({}, {})",
				String.format("%.4f", latitude), String.format("%.4f", longitude), zoom, tileX, tileY);
		ByteBuffer tileData = mapTileService.downloadTile(zoom, tileX, tileY);
		Texture terrainTexture = new Texture(tileData);
		return createTerrain(terrainTexture, size);
	}

	/**
	 * The core private method that creates a terrain plane from a texture and parameters.
	 * @param texture The texture to apply to the ground plane.
	 * @param size The size of the ground plane in world units.
	 * @param tiling The number of times the texture tiles across the plane.
	 * @return A SceneObject representing the terrain floor.
	 */
	private SceneObject createTerrain(Texture texture, float size, float tiling) {
		Appearance3D terrainAppearance = new Appearance3D(texture, Appearance3D.RenderStyle.TEXTURED);
		terrainAppearance.setShine(0.1f);
		terrainAppearance.setUnlit(true);

		Mesh planeMesh = PlaneGenerator.create(size, size, tiling, tiling, GeometryConstants.WindingOrder.CLOCKWISE);

		// Create the object and then set it to be non-selectable
		SceneObject terrainObject = new SceneObject(planeMesh, new Vector3f(0, 0, 0), terrainAppearance);
		terrainObject.setSelectable(false);
		return terrainObject;
	}

	private SceneObject createTerrain(Texture texture, float size) {
		return createTerrain(texture, size, 1.0f);
	}
}