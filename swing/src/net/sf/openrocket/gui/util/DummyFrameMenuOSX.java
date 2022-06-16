package net.sf.openrocket.gui.util;

import net.sf.openrocket.gui.dialogs.AboutDialog;
import net.sf.openrocket.gui.dialogs.LicenseDialog;
import net.sf.openrocket.gui.help.tours.GuidedTourSelectionDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.main.ExampleDesignFileAction;
import net.sf.openrocket.gui.main.MRUDesignFileAction;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.logging.Markers;
import net.sf.openrocket.startup.Application;
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
        //// File-handling related tasks
        menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.desc"));
        menubar.add(menu);

        //// New
        item = new JMenuItem(trans.get("main.menu.file.new"), KeyEvent.VK_N);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, BasicFrame.SHORTCUT_KEY));
        item.setMnemonic(KeyEvent.VK_N);
        //// Create a new rocket design
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.new.desc"));
        item.setIcon(Icons.FILE_NEW);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DummyFrameMenuOSX.removeDummyDialog();
                log.info(Markers.USER_MARKER, "New... selected");
                BasicFrame.newAction();
            }
        });
        menu.add(item);

        //// Open...
        item = new JMenuItem(trans.get("main.menu.file.open"), KeyEvent.VK_O);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, BasicFrame.SHORTCUT_KEY));
        //// Open a rocket design
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.open.desc"));
        item.setIcon(Icons.FILE_OPEN);
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DummyFrameMenuOSX.removeDummyDialog();
                log.info(Markers.USER_MARKER, "Open... selected");
                BasicFrame.openAction(DummyFrameMenuOSX.this);
            }
        });
        menu.add(item);

        //// Open Recent...
        item = new MRUDesignFileAction(trans.get("main.menu.file.openRecent"), this);
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openRecent.desc"));
        item.setIcon(Icons.FILE_OPEN);
        menu.add(item);

        //// Open example...
        item = new ExampleDesignFileAction(trans.get("main.menu.file.openExample"), null);
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.file.openExample.desc"));
        item.setIcon(Icons.FILE_OPEN_EXAMPLE);
        menu.add(item);

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
        menu = new JMenu(trans.get("main.menu.help"));
        menu.setMnemonic(KeyEvent.VK_H);
        menu.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.desc"));
        menubar.add(menu);

        ////	Guided tours
        item = new JMenuItem(trans.get("main.menu.help.tours"), KeyEvent.VK_L);
        item.setIcon(Icons.HELP_TOURS);
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.tours.desc"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info(Markers.USER_MARKER, "Guided tours selected");
                GuidedTourSelectionDialog.showDialog(DummyFrameMenuOSX.this);
            }
        });
        menu.add(item);

        menu.addSeparator();

        ////	License
        item = new JMenuItem(trans.get("main.menu.help.license"), KeyEvent.VK_L);
        item.setIcon(Icons.HELP_LICENSE);
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.license.desc"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info(Markers.USER_MARKER, "License selected");
                new LicenseDialog(DummyFrameMenuOSX.this).setVisible(true);
            }
        });
        menu.add(item);

        ////	About
        item = new JMenuItem(trans.get("main.menu.help.about"), KeyEvent.VK_A);
        item.setIcon(Icons.HELP_ABOUT);
        item.getAccessibleContext().setAccessibleDescription(trans.get("main.menu.help.about.desc"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info(Markers.USER_MARKER, "About selected");
                new AboutDialog(DummyFrameMenuOSX.this).setVisible(true);
            }
        });
        menu.add(item);


        this.setJMenuBar(menubar);
    }
}
