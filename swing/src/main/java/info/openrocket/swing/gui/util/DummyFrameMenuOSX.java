package info.openrocket.swing.gui.util;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.logging.Markers;
import info.openrocket.core.startup.Application;

import info.openrocket.swing.gui.main.BasicFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

/**
 * This dummy frame will be generated when all design windows are close on macOS.
 * It serves to maintain and customize the application menu bar when all the BasicFrame windows are closed.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class DummyFrameMenuOSX extends JFrame {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(BasicFrame.class);

    private static DummyFrameMenuOSX dialog;

    private DummyFrameMenuOSX() {
        createMenu();
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setVisible(true);   // this is needed to show the menu bar
    }

    public static DummyFrameMenuOSX createDummyDialog() {
        dialog = new DummyFrameMenuOSX();
        return dialog;
    }

    public static DummyFrameMenuOSX getDummyDialog() {
        return dialog;
    }

    public static void removeDummyDialog() {
        if (dialog != null) {
            dialog.dispose();
            dialog = null;
        }
    }

    private void createMenu() {
        JMenuBar menubar = new JMenuBar();
        JMenu menu;
        JMenuItem item;

        ////  File
        menu = new JMenu(trans.get("main.menu.file"));
        menu.setMnemonic(KeyEvent.VK_F);
        menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
        menubar.add(menu);

        BasicFrame.addFileCreateAndOpenMenuItems(menu, DummyFrameMenuOSX.this);

        menu.addSeparator();

        ////	Quit
        item = new JMenuItem(trans.get("main.menu.file.quit"), KeyEvent.VK_Q);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, BasicFrame.SHORTCUT_KEY));
        //// Quit the program
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.quit.desc"));
        item.setIcon(Icons.FILE_QUIT);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info(Markers.USER_MARKER, "Quit selected");
                BasicFrame.quitAction();
            }
        });
        menu.add(item);

        ////	Help
        BasicFrame.generateHelpMenu(menubar, this);

        this.setJMenuBar(menubar);
    }
}
