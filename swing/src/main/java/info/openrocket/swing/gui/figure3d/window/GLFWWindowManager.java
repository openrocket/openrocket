package info.openrocket.swing.gui.figure3d.window;

import info.openrocket.swing.gui.figure3d.input.InputHandler;
import info.openrocket.swing.gui.figure3d.input.KeyboardListener;
import info.openrocket.swing.gui.figure3d.utils.GLDebug;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.awt.event.KeyEvent;
import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MAJOR;
import static org.lwjgl.glfw.GLFW.GLFW_CONTEXT_VERSION_MINOR;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_CORE_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_FORWARD_COMPAT;
import static org.lwjgl.glfw.GLFW.GLFW_OPENGL_PROFILE;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_SRGB_CAPABLE;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos;
import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.glfw.GLFW.glfwGetMouseButton;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetFramebufferSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
import static org.lwjgl.glfw.GLFW.glfwSetScrollCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowFocusCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowTitle;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * GLFW-based implementation of the WindowManager interface.
 * Handles window creation, event management, and OpenGL context setup.
 */
public class GLFWWindowManager implements WindowManager, FramebufferAware, CursorQuery, KeyboardEventSource {
    
    private long window;
    private GLFWFramebufferSizeCallback framebufferSizeCallback;
    
    /**
     * Creates a new GLFW window manager instance.
     */
    public GLFWWindowManager() {
        // Initialize GLFW error handling
        GLFWErrorCallback.createPrint(System.err).set();
    }
    
    @Override
    public long createWindow(int width, int height, String title) {
        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }
        
        // Configure window hints
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);
        glfwWindowHint(GLFW_SRGB_CAPABLE, GLFW_TRUE);
        
        // Create window
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        
        return window;
    }

    @Override
    public boolean isMouseButtonPressed(int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }
    
    @Override
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }
    
    @Override
    public void swapBuffers() {
        glfwSwapBuffers(window);
    }
    
    @Override
    public void pollEvents() {
        glfwPollEvents();
    }
    
    @Override
    public void setupInputCallbacks(InputHandler inputHandler) {
        // Mouse button callback
        glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            inputHandler.handleMouseButton(button, action, mods);
        });
        
        // Cursor position callback
        glfwSetCursorPosCallback(window, (windowHandle, xpos, ypos) -> {
            inputHandler.handleMouseMovement(xpos, ypos);
        });
        
        // Scroll callback
        glfwSetScrollCallback(window, (windowHandle, xoffset, yoffset) -> {
            inputHandler.handleScroll(xoffset, yoffset);
        });
    }

    /**
     * Sets up keyboard callbacks.
     *
     * @param keyboardHandler Handler for keyboard events.
     */
    public void setupKeyboardCallbacks(KeyboardListener listener) {
        glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            listener.handleKeyEvent(toKeyEventCode(key), action);
        });

        // Clear key states when window loses focus to prevent stale key states
        glfwSetWindowFocusCallback(window, (windowHandle, focused) -> {
            if (!focused) {
                listener.clearAllKeyStates();
            }
        });
    }

    private static int toKeyEventCode(int glfwKey) {
        return switch (glfwKey) {
            case GLFW_KEY_LEFT_SHIFT, GLFW_KEY_RIGHT_SHIFT -> KeyEvent.VK_SHIFT;
            case GLFW_KEY_LEFT_CONTROL, GLFW_KEY_RIGHT_CONTROL -> KeyEvent.VK_CONTROL;
            case GLFW_KEY_LEFT_ALT, GLFW_KEY_RIGHT_ALT -> KeyEvent.VK_ALT;
            default -> glfwKey;
        };
    }
    
    /**
     * Sets up a framebuffer resize callback that will be called when the window is resized.
     * 
     * @param callback Callback function to handle resize events
     */
    @Override
    public void setupFramebufferSizeCallback(FramebufferAware.FramebufferSizeCallback callback) {
        this.framebufferSizeCallback = new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long windowHandle, int width, int height) {
                callback.invoke(width, height);
            }
        };
        glfwSetFramebufferSizeCallback(window, framebufferSizeCallback);
    }
    
    /**
     * Gets the current window size in screen coordinates.
     * 
     * @return Array containing [width, height] in screen coordinates
     */
    @Override
    public int[] getWindowSize() {
        IntBuffer widthBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer heightBuffer = MemoryUtil.memAllocInt(1);
        glfwGetWindowSize(window, widthBuffer, heightBuffer);
        int[] result = {widthBuffer.get(0), heightBuffer.get(0)};
        MemoryUtil.memFree(widthBuffer);
        MemoryUtil.memFree(heightBuffer);
		return result;
    }

    /**
     * Gets the current framebuffer size in pixels.
     * 
     * @return Array containing [width, height] in pixels
     */
    @Override
    public int[] getFramebufferSize() {
        IntBuffer widthBuffer = MemoryUtil.memAllocInt(1);
        IntBuffer heightBuffer = MemoryUtil.memAllocInt(1);
        glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
        
        int[] result = {widthBuffer.get(0), heightBuffer.get(0)};
        
        MemoryUtil.memFree(widthBuffer);
        MemoryUtil.memFree(heightBuffer);
        
        return result;
    }
    
    /**
     * Gets the current cursor position in screen coordinates.
     * 
     * @return Array containing [x, y] position
     */
    @Override
    public double[] getCursorPosition() {
        double[] xpos = new double[1];
        double[] ypos = new double[1];
        glfwGetCursorPos(window, xpos, ypos);
        return new double[]{xpos[0], ypos[0]};
    }
    
    /**
     * Makes the window's OpenGL context current and enables V-Sync.
     */
    public void makeContextCurrent() {
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Enable V-Sync
        
        // Create OpenGL capabilities
        GL.createCapabilities();
        GLDebug.enableIfRequested("GLFW-window");
    }
    
    /**
     * Shows the window to the user.
     */
    public void showWindow() {
        glfwShowWindow(window);
    }

    /**
     * Sets the title of the window.
     * @param title The new title for the window.
     */
    @Override
    public void setWindowTitle(String title) {
        glfwSetWindowTitle(window, title);
    }


    @Override
    public void cleanup() {
        // Free callbacks
        if (framebufferSizeCallback != null) {
            framebufferSizeCallback.free();
        }
        
        // Destroy window
        if (window != NULL) {
            glfwDestroyWindow(window);
        }
        
        // Terminate GLFW
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
    
}
