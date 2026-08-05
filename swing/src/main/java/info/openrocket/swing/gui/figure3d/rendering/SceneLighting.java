package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.swing.gui.figure3d.particles.ParticleEmitter;
import info.openrocket.swing.gui.figure3d.particles.flame.FlameEmitter;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.GradientBackground;
import info.openrocket.swing.gui.figure3d.rendering.backgrounds.SolidColorBackground;
import info.openrocket.swing.gui.figure3d.rendering.passes.ShadowPass;
import info.openrocket.swing.gui.figure3d.scene.controllers.LightController;
import info.openrocket.swing.gui.figure3d.scene.graph.Camera;
import info.openrocket.swing.gui.figure3d.scene.graph.Light;
import info.openrocket.swing.gui.figure3d.scene.graph.SceneView;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;

/**
 * Synchronizes dynamic lights and binds frame-wide lighting uniforms.
 */
final class SceneLighting {

	private static final int SHADOW_TEXTURE_UNIT = 2;
	private static final int NO_SHADOW_LIGHT = -1;

	private final GLShader shader;
	private final MainShaderUniforms uniforms;
	private final TextureBinder textureBinder;
	private final ShadowPass shadowPass;
	private final RenderingConfiguration config;
	private final Vector3f cameraPosition = new Vector3f();
	private final Vector3f fogColor = new Vector3f();

	SceneLighting(GLShader shader, MainShaderUniforms uniforms, TextureBinder textureBinder,
				  ShadowPass shadowPass, RenderingConfiguration config) {
		this.shader = shader;
		this.uniforms = uniforms;
		this.textureBinder = textureBinder;
		this.shadowPass = shadowPass;
		this.config = config;
	}

	static void updateFlameLights(SceneView scene) {
		LightController lightController = scene.getLightController();
		for (ParticleEmitter emitter : scene.getParticleEmitters()) {
			if (!(emitter instanceof FlameEmitter flameEmitter)) {
				continue;
			}

			Light oldLight = flameEmitter.getFlameLight();
			flameEmitter.updateFlameLight();
			Light newLight = flameEmitter.getFlameLight();

			if (oldLight != newLight) {
				if (oldLight != null) {
					lightController.removeLight(oldLight);
				}
				if (newLight != null && !lightController.getLights().contains(newLight)) {
					lightController.addLight(newLight);
				}
			} else if (newLight != null) {
				if (lightController.getLights().contains(newLight)) {
					lightController.refreshVisualizer(newLight);
				} else {
					lightController.addLight(newLight);
				}
			}
		}
	}

	void bindFrameUniforms(SceneView scene, Camera camera, Matrix4f viewMatrix,
						   Matrix4f projectionMatrix, boolean reduceInteractionEffects) {
		shader.use();
		shader.setUniformMatrix4f(uniforms.projection, projectionMatrix);
		shader.setUniformMatrix4f(uniforms.view, viewMatrix);
		Vector3f currentCameraPosition = camera.getPosition(cameraPosition);
		glUniform3f(uniforms.viewPos, currentCameraPosition.x, currentCameraPosition.y, currentCameraPosition.z);

		bindShadowUniforms();
		bindLights(scene.getLightController().getLights());
		bindFog(scene);

		glUniform1f(uniforms.ambientLightFactor, config.getVisualEffects().getAmbientLightFactor());

		boolean roughnessBump = config.getQuality().isRoughnessBumpRendered() && !reduceInteractionEffects;
		glUniform1i(uniforms.enableRoughnessBump, roughnessBump ? 1 : 0);
	}

	private void bindShadowUniforms() {
		shader.setUniformMatrix4f(uniforms.lightSpaceMatrix, shadowPass.getLightSpaceMatrix());
		textureBinder.bindTexture(SHADOW_TEXTURE_UNIT, GL_TEXTURE_2D, shadowPass.getDepthMapTexture());
		glUniform1i(uniforms.shadowMap, SHADOW_TEXTURE_UNIT);
		if (shadowPass.hasShadowMap()) {
			glUniform1i(uniforms.shadowsEnabled, 1);
			glUniform1i(uniforms.shadowLightIndex, shadowPass.getShadowCastingLightIndex());
			glUniform1f(uniforms.shadowStrength, shadowPass.getShadowStrength());
		} else {
			glUniform1i(uniforms.shadowsEnabled, 0);
			glUniform1i(uniforms.shadowLightIndex, NO_SHADOW_LIGHT);
			glUniform1f(uniforms.shadowStrength, 0.0f);
		}
	}

	private void bindLights(List<Light> lights) {
		int activeLightCount = Math.min(lights.size(), uniforms.lights.length);
		glUniform1i(uniforms.numLights, activeLightCount);

		for (int i = 0; i < activeLightCount; i++) {
			Light light = lights.get(i);
			MainShaderUniforms.LightUniforms lightUniforms = uniforms.lights[i];
			Vector3f position = light.getPosition();
			Vector3f direction = light.getDirection();
			Vector3f color = light.getColor();
			glUniform1i(lightUniforms.type, light.getType().ordinal());
			glUniform3f(lightUniforms.position, position.x, position.y, position.z);
			glUniform3f(lightUniforms.direction, direction.x, direction.y, direction.z);
			glUniform3f(lightUniforms.color, color.x, color.y, color.z);
		}
	}

	private void bindFog(SceneView scene) {
		if (!scene.isFogEnabled()) {
			glUniform1i(uniforms.fogEnabled, 0);
			return;
		}

		glUniform1i(uniforms.fogEnabled, 1);
		if (scene.getBackground() instanceof SolidColorBackground solidBackground) {
			Vector4f backgroundColor = solidBackground.getColor();
			fogColor.set(backgroundColor.x, backgroundColor.y, backgroundColor.z);
		} else if (scene.getBackground() instanceof GradientBackground gradientBackground) {
			fogColor.set(gradientBackground.getBottomColor());
		} else {
			fogColor.set(0.5f, 0.6f, 0.7f);
		}
		glUniform3f(uniforms.fogColor, fogColor.x, fogColor.y, fogColor.z);
		glUniform1f(uniforms.fogDensity, scene.getFogDensity());
	}
}
