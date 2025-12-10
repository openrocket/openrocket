package info.openrocket.swing.gui.figure3d.core.geography;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;

/**
 * Fetches map tile images from a web service.
 */
public class MapTileService {

	private final HttpClient httpClient;
	// This URL template requires 3 arguments: zoom, y, and x.
	private final String tileServerUrlTemplate = "https://server.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer/tile/%d/%d/%d";

	public MapTileService() {
		this.httpClient = HttpClient.newBuilder()
				.version(HttpClient.Version.HTTP_2)
				.connectTimeout(Duration.ofSeconds(10))
				.build();
	}

	/**
	 * Downloads a map tile as a ByteBuffer containing the compressed image (e.g., PNG).
	 * @param zoom The zoom level.
	 * @param x The X tile number.
	 * @param y The Y tile number.
	 * @return A ByteBuffer with the image data.
	 * @throws IOException If the download fails.
	 * @throws InterruptedException If the download is interrupted.
	 */
	public ByteBuffer downloadTile(int zoom, int x, int y) throws IOException, InterruptedException {
		// Add the 'zoom' variable to the String.format call to match the template.
		String url = String.format(tileServerUrlTemplate, zoom, y, x);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.header("User-Agent", "LWJGL Engine Example/1.0") // A user agent is good practice
				.GET()
				.build();

		HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

		if (response.statusCode() != 200) {
			throw new IOException("Failed to download tile. Server responded with status: " + response.statusCode());
		}

		ByteBuffer buffer = ByteBuffer.allocateDirect(response.body().length);
		buffer.put(response.body()).flip();
		return buffer;
	}
}