package net.sf.openrocket.gui.figure3d.photo;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import net.sf.openrocket.database.Databases;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.file.GeneralRocketLoader;
import net.sf.openrocket.file.RocketLoadException;
import net.sf.openrocket.gui.main.SwingExceptionHandler;
import net.sf.openrocket.gui.util.FileHelper;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.LoggingSystemSetup;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.GuiModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

@SuppressWarnings("serial")
public class PhotoFrame extends JFrame {
	private static final Logger log = LoggerFactory.getLogger(PhotoFrame.class);
	private final int SHORTCUT_KEY = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
	private final Translator trans = Application.getTranslator();

	private final PhotoPanel photoPanel;
	private final JDialog settings;

	public PhotoFrame(OpenRocketDocument document, Window parent) {
		this(false);
		setTitle("Photo - " + document.getRocket().getName());
		photoPanel.setDoc(document);
	}

	public PhotoFrame(boolean app) {
		setSize(1024, 768);
		this.setMinimumSize(new Dimension(160, 150));
		photoPanel = new PhotoPanel();
		setJMenuBar(getMenu(app));
		setContentPane(photoPanel);

		GUIUtil.rememberWindowSize(this);
		this.setLocationByPlatform(true);
		GUIUtil.setWindowIcons(this);

		settings = new JDialog(this, "Settings") {
			{
				setContentPane(new PhotoSettingsConfig(photoPanel.getSettings()));
				pack();
				this.setLocationByPlatform(true);
				setVisible(true);
			}
		};
	}

	private JMenuBar getMenu(final boolean showFileMenu) {
		JMenuBar menubar = new JMenuBar();
		JMenu menu;
		JMenuItem item;

		// // File
		if (showFileMenu) {
			menu = new JMenu(trans.get("main.menu.file"));
			menu.setMnemonic(KeyEvent.VK_F);
			// // File-handling related tasks
			menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
			menubar.add(menu);

			item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
			item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, SHORTCUT_KEY));
			// // Open a rocket design
			item.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.item.Openrocketdesign"));
			item.setIcon(Icons.FILE_OPEN);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					log.info(Markers.USER_MARKER, "Open... selected");

					JFileChooser chooser = new JFileChooser();

					chooser.addChoosableFileFilter(FileHelper.ALL_DESIGNS_FILTER);
					chooser.addChoosableFileFilter(FileHelper.OPENROCKET_DESIGN_FILTER);
					chooser.addChoosableFileFilter(FileHelper.ROCKSIM_DESIGN_FILTER);
					chooser.setFileFilter(FileHelper.ALL_DESIGNS_FILTER);

					chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());
					int option = chooser.showOpenDialog(PhotoFrame.this);
					if (option == JFileChooser.APPROVE_OPTION) {
						File file = chooser.getSelectedFile();
						log.debug("Opening File " + file.getAbsolutePath());
						GeneralRocketLoader grl = new GeneralRocketLoader(file);
						try {
							OpenRocketDocument doc = grl.load();
							photoPanel.setDoc(doc);
						} catch (RocketLoadException e1) {
							e1.printStackTrace();
						}
					}
				}
			});
			menu.add(item);
		}

		// // Edit
		menu = new JMenu(trans.get("main.menu.edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		// // Rocket editing
		menu.getAccessibleContext().setAccessibleDescription(trans.get("BasicFrame.menu.Rocketedt"));
		menubar.add(menu);

		Action action = new AbstractAction("Copy") {
			@Override
			public void actionPerformed(ActionEvent e) {
				photoPanel.doCopy();
			}
		};
		item = new JMenuItem(action);
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, SHORTCUT_KEY));
		item.setMnemonic(KeyEvent.VK_C);
		item.getAccessibleContext().setAccessibleDescription("Copy image to clipboard");
		menu.add(item);

		menu.add(new JMenuItem(new AbstractAction("Photo Settings") {
			@Override
			public void actionPerformed(ActionEvent e) {
				settings.setVisible(true);
			}
		}));

		// Window
		menu = new JMenu("Window");
		menubar.add(menu);
		JMenu sizeMenu = new JMenu("Size");
		menu.add(sizeMenu);

		sizeMenu.add(new JMenuItem(new SizeAction(320, 240, "QVGA")));
		sizeMenu.add(new JMenuItem(new SizeAction(640, 480, "VGA")));
		sizeMenu.add(new JMenuItem(new SizeAction(1024, 768, "XGA")));

		sizeMenu.addSeparator();

		sizeMenu.add(new JMenuItem(new SizeAction(240, 320, "QVGA Portrait")));
		sizeMenu.add(new JMenuItem(new SizeAction(480, 640, "VGA Portrait")));
		sizeMenu.add(new JMenuItem(new SizeAction(768, 1024, "XGA Portrait")));

		sizeMenu.addSeparator();

		sizeMenu.add(new JMenuItem(new SizeAction(854, 480, "420p")));
		sizeMenu.add(new JMenuItem(new SizeAction(1280, 720, "720p")));
		sizeMenu.add(new JMenuItem(new SizeAction(1920, 1080, "1080p")));

		return menubar;
	}

	private class SizeAction extends AbstractAction {
		private final int w, h;

		SizeAction(final int w, final int h, final String n) {
			super(w + " x " + h + " (" + n + ")");
			this.w = w;
			this.h = h;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			photoPanel.setPreferredSize(new Dimension(w, h));
			PhotoFrame.this.pack();
		}

	}

	public static void main(String args[]) throws Exception {

		LoggingSystemSetup.setupLoggingAppender();
		LoggingSystemSetup.addConsoleAppender();

		// Setup the uncaught exception handler
		log.info("Registering exception handler");
		SwingExceptionHandler exceptionHandler = new SwingExceptionHandler();
		Application.setExceptionHandler(exceptionHandler);
		exceptionHandler.registerExceptionHandler();

		// Load motors etc.
		log.info("Loading databases");

		GuiModule guiModule = new GuiModule();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(guiModule, pluginModule);
		Application.setInjector(injector);

		guiModule.startLoader();

		// Set the best available look-and-feel
		log.info("Setting best LAF");
		GUIUtil.setBestLAF();

		// Load defaults
		((SwingPreferences) Application.getPreferences()).loadDefaultUnits();

		Databases.fakeMethod();

		PhotoFrame pa = new PhotoFrame(true);
		pa.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pa.setTitle("OpenRocket - Photo Studio Alpha");
		pa.setVisible(true);
	}

}
