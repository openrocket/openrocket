package info.openrocket.swing.gui.theme;

import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.JRootPane;
import javax.swing.RootPaneContainer;
import java.awt.Window;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Tests runtime theme updates that are not handled by Swing's component-tree refresh.
 */
class UIThemeTest extends BaseTestCase {

	@Test
	void appliesThemeToEveryRootPaneWindow() {
		UITheme.Theme theme = mock(UITheme.Theme.class);
		JRootPane rootPane = new JRootPane();
		Window rootPaneWindow = mock(Window.class, withSettings().extraInterfaces(RootPaneContainer.class));
		RootPaneContainer rootPaneContainer = (RootPaneContainer) rootPaneWindow;
		when(rootPaneContainer.getRootPane()).thenReturn(rootPane);

		Window ordinaryWindow = mock(Window.class);
		UITheme.applyThemeToRootPanes(theme, new Window[] { ordinaryWindow, rootPaneWindow });

		verify(theme).applyThemeToRootPane(rootPane);
		verifyNoMoreInteractions(theme);
	}
}
