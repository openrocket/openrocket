package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.rocketcomponent.ExternalComponent;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Coordinate;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.StateChangeListener;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppearanceFactoryTest {

	@AfterEach
	void resetTextureCache() {
		AppearanceFactory.clearCachedDecalTexturesForTesting(false);
	}

	@Test
	void reusesCachedDecalTextureAcrossAppearances() {
		TestDecalImage image = new TestDecalImage("decal.png");
		Texture texture = mock(Texture.class);
		AtomicInteger loads = new AtomicInteger();
		AppearanceFactory.setTextureLoaderForTesting(decalImage -> {
			loads.incrementAndGet();
			return texture;
		});

		Appearance3D first = AppearanceFactory.createFrom(componentWithDecal(image));
		Appearance3D second = AppearanceFactory.createFrom(componentWithDecal(image));

		assertSame(texture, first.getTexture());
		assertSame(texture, second.getTexture());
		assertEquals(1, loads.get());

		first.cleanup();
		second.cleanup();
		verify(texture, never()).cleanup();

		AppearanceFactory.clearCachedDecalTexturesForTesting(true);
		verify(texture).cleanup();
	}

	@Test
	void reloadsCachedTextureWhenDecalImageVersionChanges() {
		TestDecalImage image = new TestDecalImage("decal.png");
		Texture firstTexture = mock(Texture.class);
		Texture secondTexture = mock(Texture.class);
		AtomicInteger loads = new AtomicInteger();
		AppearanceFactory.setTextureLoaderForTesting(decalImage ->
				loads.incrementAndGet() == 1 ? firstTexture : secondTexture);

		Appearance3D appearance = AppearanceFactory.createFrom(componentWithDecal(image));
		assertSame(firstTexture, appearance.getTexture());

		image.fireChangeEvent(this);
		AppearanceFactory.updateFrom(appearance, componentWithDecal(image));

		assertSame(secondTexture, appearance.getTexture());
		assertEquals(2, loads.get());
		verify(firstTexture, never()).cleanup();

		AppearanceFactory.clearCachedDecalTexturesForTesting(true);
		verify(firstTexture).cleanup();
		verify(secondTexture).cleanup();
	}

	@Test
	void internalComponentsRenderWithoutSurfaceRoughness() {
		RocketComponent internal = mock(RocketComponent.class);
		when(internal.getAppearance()).thenReturn(new Appearance(new ORColor(200, 200, 200), 0.3));

		Appearance3D appearance = AppearanceFactory.createFrom(internal);

		assertEquals(0.0f, appearance.getRoughnessAmount());
	}

	@Test
	void capturedAppearanceDoesNotReadLaterComponentState() {
		RocketComponent component = mock(RocketComponent.class);
		Appearance capturedAppearance = new Appearance(new ORColor(0, 0, 255), 0.3);
		when(component.getAppearance()).thenReturn(capturedAppearance);

		AppearanceFactory.ComponentAppearanceSnapshot snapshot =
				AppearanceFactory.captureComponentAppearance(component);
		when(component.getAppearance()).thenReturn(new Appearance(new ORColor(255, 0, 0), 0.8));

		assertSame(capturedAppearance, snapshot.appearance());
	}

	@Test
	void externalComponentsKeepTheRoughnessOfTheirFinish() {
		ExternalComponent external = mock(ExternalComponent.class);
		when(external.getAppearance()).thenReturn(new Appearance(new ORColor(200, 200, 200), 0.3));
		when(external.getFinish()).thenReturn(ExternalComponent.Finish.ROUGH);

		Appearance3D appearance = AppearanceFactory.createFrom(external);

		assertTrue(appearance.getRoughnessAmount() > 0.0f,
				"a rough finish must still produce a bumpy surface");
	}

	private static RocketComponent componentWithDecal(DecalImage image) {
		RocketComponent component = mock(RocketComponent.class);
		when(component.getAppearance()).thenReturn(new Appearance(
				new ORColor(255, 255, 255),
				0.0,
				new Decal(Coordinate.ZERO, Coordinate.ZERO, new Coordinate(1, 1), 0.0, image, Decal.EdgeMode.CLAMP)));
		return component;
	}

	private static class TestDecalImage implements DecalImage {
		private final String name;
		private final List<StateChangeListener> listeners = new ArrayList<>();

		private TestDecalImage(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public InputStream getBytes() {
			return new ByteArrayInputStream(new byte[0]);
		}

		@Override
		public void exportImage(File file) throws IOException, DecalNotFoundException {
		}

		@Override
		public void fireChangeEvent(Object source) {
			EventObject event = new EventObject(source);
			for (StateChangeListener listener : List.copyOf(listeners)) {
				listener.stateChanged(event);
			}
		}

		@Override
		public File getDecalFile() {
			return null;
		}

		@Override
		public void setDecalFile(File file) {
		}

		@Override
		public boolean isIgnored() {
			return false;
		}

		@Override
		public void setIgnored(boolean ignored) {
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
			listeners.add(listener);
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
			listeners.remove(listener);
		}

		@Override
		public int compareTo(DecalImage other) {
			return name.compareTo(other.getName());
		}
	}
}
