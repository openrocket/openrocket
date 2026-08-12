package info.openrocket.swing.gui.simulation.currentconditions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

class OpenMeteoClientTest {
	private static final String RESPONSE = """
			{"latitude":33.25,"longitude":-117.25,"elevation":29.0,"current":{
			"time":"2026-08-11T21:45","temperature_2m":22.7,"surface_pressure":1010.4,
			"relative_humidity_2m":83,"wind_speed_10m":1.3,"wind_direction_10m":257,
			"wind_gusts_10m":2.1,"wind_speed_80m":2.0,"wind_direction_80m":260}}
			""";

	private HttpServer server;
	private AtomicInteger requests;

	@BeforeEach
	void startServer() throws IOException {
		OpenMeteoClient.clearCachesForTesting();
		requests = new AtomicInteger();
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		server.createContext("/forecast", exchange -> {
			requests.incrementAndGet();
			byte[] body = RESPONSE.getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(200, body.length);
			try (var response = exchange.getResponseBody()) {
				response.write(body);
			}
		});
		server.setExecutor(Executors.newCachedThreadPool());
		server.start();
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
		OpenMeteoClient.clearCachesForTesting();
	}

	@Test
	void fetchPreservesRequestedAndModelCoordinatesAndUsesTenMetreSurfaceHeight() throws Exception {
		Instant requestedAt = Instant.now();
		OpenMeteoClient.FetchResult result = client().fetchWithCacheInfo(33.2448, -117.27761);
		CurrentConditions conditions = result.conditions();

		assertEquals(33.2448, conditions.latitude());
		assertEquals(-117.27761, conditions.longitude());
		assertEquals(33.25, conditions.modelLatitude());
		assertEquals(-117.25, conditions.modelLongitude());
		assertEquals(39.0, conditions.windLayers().get(0).altitude());
		assertEquals(109.0, conditions.windLayers().get(1).altitude());
		assertEquals(Math.toRadians(257), conditions.windLayers().get(0).direction(), 0.000001);
		assertEquals(0.2, conditions.windLayers().get(1).standardDeviation(), 0.000001);
		long cacheMinutes = Duration.between(requestedAt, result.refreshAvailableAt()).toMinutes();
		assertTrue(cacheMinutes >= 9 && cacheMinutes <= 25);
	}

	@Test
	void nearbyCoordinatesReuseTheSamePracticalModelCell() throws Exception {
		client().fetchWithCacheInfo(33.2448, -117.27761);
		OpenMeteoClient.FetchResult second = client().fetchWithCacheInfo(33.2449, -117.2779);

		assertTrue(second.cached());
		assertEquals(1, requests.get());
		assertEquals(33.2449, second.conditions().latitude());
	}

	@Test
	void failedForceRefreshDoesNotConsumeTheCooldown() throws Exception {
		OpenMeteoClient failing = new OpenMeteoClient("http://127.0.0.1:1/forecast", "http://127.0.0.1:1/search", "");

		assertThrows(IOException.class, () -> failing.forceFetch(10, 20));
		assertThrows(IOException.class, () -> failing.forceFetch(10, 20));
	}

	@Test
	void concurrentIdenticalRequestsShareOneHttpCall() throws Exception {
		OpenMeteoClient client = client();
		CompletableFuture<OpenMeteoClient.FetchResult> first = CompletableFuture.supplyAsync(() -> fetch(client));
		CompletableFuture<OpenMeteoClient.FetchResult> second = CompletableFuture.supplyAsync(() -> fetch(client));

		CompletableFuture.allOf(first, second).get();
		assertEquals(1, requests.get());
	}

	@Test
	void windRequestHasTheExpectedDetailedVariables() {
		String variables = OpenMeteoClient.currentVariables();

		assertTrue(variables.contains("wind_speed_10m"));
		assertTrue(variables.contains("geopotential_height_30hPa"));
		assertEquals(69, variables.split(",").length);
	}

	private OpenMeteoClient client() {
		String endpoint = "http://127.0.0.1:" + server.getAddress().getPort() + "/forecast";
		return new OpenMeteoClient(endpoint, endpoint, "");
	}

	private static OpenMeteoClient.FetchResult fetch(OpenMeteoClient client) {
		try {
			return client.fetchWithCacheInfo(33.2448, -117.27761);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
