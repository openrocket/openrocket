package info.openrocket.core.motor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import java.util.ResourceBundle;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import info.openrocket.core.l10n.ResourceBundleTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import org.junit.jupiter.api.Test;

public class MotorTypeLocalizationTest {
	@Test
	public void chineseDisplayKeepsMotorTypeIdentifiers() {
		Injector originalInjector = Application.getInjector();
		try {
			Application.setInjector(Guice.createInjector(new AbstractModule() {
				@Override
				protected void configure() {
					bind(Translator.class).toInstance(new ResourceBundleTranslator("l10n.messages", Locale.SIMPLIFIED_CHINESE));
				}
			}));

			assertEquals("\u4e00\u6b21\u6027", Motor.Type.SINGLE.getName());
			assertEquals("\u4e00\u6b21\u6027\u56fa\u4f53\u63a8\u8fdb\u5242\u53d1\u52a8\u673a", Motor.Type.SINGLE.getDescription());
			assertEquals("\u53ef\u91cd\u65b0\u88c5\u586b", Motor.Type.RELOAD.getName());
			assertEquals("Reloadable", Motor.Type.RELOAD.toString());
			assertEquals("SINGLE", Motor.Type.SINGLE.name());
			assertEquals("RELOAD", Motor.Type.RELOAD.name());
			assertEquals("HYBRID", Motor.Type.HYBRID.name());
			assertEquals("UNKNOWN", Motor.Type.UNKNOWN.name());
		} finally {
			Application.setInjector(originalInjector);
			ResourceBundle.clearCache();
		}
	}
}
