package info.openrocket.swing.gui.figure3d.rendering;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

/** Owns a linked OpenGL shader program. */
public class GLShader implements GpuResource {

	private static final Logger log = LoggerFactory.getLogger(GLShader.class);

	private int programId;
	private final String vertexPath;
	private final String fragmentPath;

	// Reusable buffers to avoid allocations every frame
	private final float[] matrix4Buffer = new float[16];
	private final float[] matrix3Buffer = new float[9];

	/**
	 * @param vertexPath classpath path to the vertex shader
	 * @param fragmentPath classpath path to the fragment shader
	 * @throws ShaderException if loading, compilation, or linking fails
	 */
	public GLShader(String vertexPath, String fragmentPath) {
		this.vertexPath = vertexPath;
		this.fragmentPath = fragmentPath;

		log.debug("Loading shader: vertex={}, fragment={}", vertexPath, fragmentPath);

		int vertexShader = 0;
		int fragmentShader = 0;
		int linkedProgram = 0;
		boolean initialized = false;
		try {
			vertexShader = compileShader(GL_VERTEX_SHADER, loadResource(vertexPath), vertexPath);
			fragmentShader = compileShader(GL_FRAGMENT_SHADER, loadResource(fragmentPath), fragmentPath);
			linkedProgram = linkProgram(vertexShader, fragmentShader);
			GLErrors.check("shader program creation (" + vertexPath + ", " + fragmentPath + ")");
			initialized = true;
		} finally {
			// The linked program keeps its own copy of the compiled stages.
			if (vertexShader != 0) {
				glDeleteShader(vertexShader);
			}
			if (fragmentShader != 0) {
				glDeleteShader(fragmentShader);
			}
			if (!initialized && linkedProgram != 0) {
				glDeleteProgram(linkedProgram);
			}
		}

		programId = linkedProgram;
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.PROGRAM, programId, vertexPath + " | " + fragmentPath);
		log.debug("GLShader program created successfully: id={}", programId);
	}

	/**
	 * Links the compiled shader stages into a program.
	 *
	 * @param vertexShader The compiled vertex shader ID
	 * @param fragmentShader The compiled fragment shader ID
	 * @return The linked program ID
	 * @throws ShaderException If program creation or linking fails
	 */
	private int linkProgram(int vertexShader, int fragmentShader) {
		int newProgramId = glCreateProgram();
		if (newProgramId == 0) {
			throw new ShaderException("Failed to create shader program for: " + vertexPath + ", " + fragmentPath);
		}

		glAttachShader(newProgramId, vertexShader);
		glAttachShader(newProgramId, fragmentShader);
		glLinkProgram(newProgramId);

		if (glGetProgrami(newProgramId, GL_LINK_STATUS) == 0) {
			String linkError = glGetProgramInfoLog(newProgramId, 1024);
			glDeleteProgram(newProgramId);
			log.error("GLShader link error for {}, {}: {}", vertexPath, fragmentPath, linkError);
			throw new ShaderException("Error linking shader program (" + vertexPath + ", " + fragmentPath + "): " + linkError);
		}

		return newProgramId;
	}

	/**
	 * Activates this shader program for subsequent rendering operations.
	 */
	public void use() {
		glUseProgram(programId);
	}

	/**
	 * Deactivates the current shader program.
	 */
	public void unbind() {
		glUseProgram(0);
	}

	/**
	 * Gets the OpenGL program ID for this shader.
	 * 
	 * @return The OpenGL program ID
	 */
	public int getProgramId() {
		return programId;
	}

	/** Returns a uniform location, or {@code -1} if it is not exposed by the linked program. */
	public int getUniformLocation(String name) {
		return glGetUniformLocation(programId, name);
	}

	/**
	 * Resolves a uniform that is part of the shader's required Java/GLSL contract.
	 *
	 * @param name the uniform name
	 * @return the uniform location
	 * @throws ShaderException if the linked program does not expose the uniform
	 */
	public int requireUniformLocation(String name) {
		int location = getUniformLocation(name);
		if (location < 0) {
			throw new ShaderException("Required uniform '" + name + "' not found in shader program ("
					+ vertexPath + ", " + fragmentPath + ")");
		}
		return location;
	}

	/**
	 * Sets a Matrix4f uniform using a pre-cached location.
	 * @param location The cached uniform location
	 * @param matrix The matrix to set
	 */
	public void setUniformMatrix4f(int location, Matrix4f matrix) {
		if (location >= 0) {
			matrix.get(matrix4Buffer);
			glUniformMatrix4fv(location, false, matrix4Buffer);
		}
	}

	/**
	 * Sets a Matrix3f uniform using a pre-cached location.
	 * @param location The cached uniform location
	 * @param matrix The matrix to set
	 */
	public void setUniformMatrix3f(int location, Matrix3f matrix) {
		if (location >= 0) {
			matrix.get(matrix3Buffer);
			glUniformMatrix3fv(location, false, matrix3Buffer);
		}
	}

	public void setUniformVector4f(int location, Vector4f vector) {
		if (location >= 0) {
			glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
		}
	}

	public void setUniformVector3f(int location, Vector3f vector) {
		if (location >= 0) {
			glUniform3f(location, vector.x, vector.y, vector.z);
		}
	}

	public void setUniformFloat(int location, float value) {
		if (location >= 0) {
			glUniform1f(location, value);
		}
	}

	/**
	 * Sets an int uniform. Also required for sampler and bool uniforms:
	 * setting those with {@code glUniform1f} is a silent GL_INVALID_OPERATION
	 * and the uniform keeps its previous value.
	 * @param location the uniform location
	 * @param value The int value to set.
	 */
	public void setUniformInt(int location, int value) {
		if (location >= 0) {
			glUniform1i(location, value);
		}
	}

	@Override
	public void cleanup() {
		int releasedProgram = programId;
		if (releasedProgram != 0) {
			programId = 0;
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.PROGRAM, releasedProgram);
			glDeleteProgram(releasedProgram);
		}
	}

	/**
	 * Compiles a single shader stage from source code. Not limited to the stages
	 * used by this class; any valid GL shader type can be compiled.
	 *
	 * @param type The shader type (e.g. GL_VERTEX_SHADER, GL_FRAGMENT_SHADER, GL_GEOMETRY_SHADER)
	 * @param source The shader source code
	 * @param path The path to the shader file (for error reporting)
	 * @return The compiled shader ID
	 * @throws ShaderException If shader creation or compilation fails
	 */
	private static int compileShader(int type, String source, String path) {
		String shaderTypeName = shaderTypeName(type);
		int shaderId = glCreateShader(type);
		if (shaderId == 0) {
			log.error("Failed to create {} shader for: {}", shaderTypeName, path);
			throw new ShaderException("Error creating " + shaderTypeName + " shader for: " + path);
		}

		glShaderSource(shaderId, source);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			String error = glGetShaderInfoLog(shaderId, 2048);
			glDeleteShader(shaderId); // Clean up on error
			log.error("Compilation error in {} shader '{}': {}", shaderTypeName, path, error);
			throw new ShaderException("Error compiling " + shaderTypeName + " shader '" + path + "':\n" + error);
		}

		log.debug("Compiled {} shader: {}", shaderTypeName, path);
		return shaderId;
	}

	/**
	 * Maps a GL shader type constant to a human-readable name for error reporting.
	 */
	private static String shaderTypeName(int type) {
		return switch (type) {
			case GL_VERTEX_SHADER -> "vertex";
			case GL_FRAGMENT_SHADER -> "fragment";
			case GL_GEOMETRY_SHADER -> "geometry";
			case GL_TESS_CONTROL_SHADER -> "tessellation control";
			case GL_TESS_EVALUATION_SHADER -> "tessellation evaluation";
			case GL_COMPUTE_SHADER -> "compute";
			default -> String.format("unknown(0x%04X)", type);
		};
	}

	/**
	 * Loads a text resource from the classpath.
	 *
	 * @param fileName Path to the resource file
	 * @return The complete file contents as a string
	 * @throws ShaderException If the file cannot be read
	 */
	private static String loadResource(String fileName) {
		InputStream in = GLShader.class.getResourceAsStream(fileName);
		if (in == null) {
			log.error("GLShader resource not found: {}", fileName);
			throw new ShaderException("GLShader resource not found: " + fileName);
		}
		try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
			return scanner.useDelimiter("\\A").next();
		} catch (Exception e) {
			log.error("Failed to read shader resource '{}': {}", fileName, e.getMessage());
			throw new ShaderException("Failed to read shader resource '" + fileName + "'", e);
		}
	}
}
