package info.openrocket.swing.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.BuildProperties;
import info.openrocket.core.util.Chars;

import net.miginfocom.swing.MigLayout;
import info.openrocket.swing.gui.components.DescriptionArea;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;

public class LicenseDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();

	public LicenseDialog(JFrame parent) {
		super(parent, true);
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		// OpenRocket logo
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-128.png", "OpenRocket")), "top");
		
		panel.add(new StyledLabel("Software Licenses", 10), "ax 50%, pushx, wrap para");

		final String jarUrl = "jar:" + getClass().getProtectionDomain().getCodeSource().getLocation().toString();
		final String copyrightYear = BuildProperties.getCopyrightYear();

		/*****************************************************************************************************************************/
		/*                                                                                                                           */
		/* LICENSE TEXT:  each of the licenses we're using is described here.  At the end, they are all concatenated for insertion   */
		/* in the description window                                                                                                 */
		/*                                                                                                                           */
		/*****************************************************************************************************************************/
		
		/*****************************************************************************************************************************/
        /* GPL:  overall project                                                                                                     */
		/*****************************************************************************************************************************/
		final String orLicense = "<strong>GNU GENERAL PUBLIC LICENSE</strong>" + "<br>" +
			"<br>" +
			"OpenRocket - A model rocket simulator<br>" + 
			"Copyright " + Chars.COPY + " 2007-" + copyrightYear + " Sampo Niskanen and others<br>" +
			"Project page: <a href=\"https://openrocket.info/\">https://openrocket.info/</a><br>" +
			"<br>" + 
			"This program is free software: you can redistribute it and/or modify it under the terms of the " +
			"GNU General Public License as published by the Free Software Foundation, either version 3 " +
			"of the License, or any later version. " +
			"The license may be viewed " +
			"<a href=\"" + jarUrl + "!/LICENSE.TXT\">here</a>.<br>" +
			"<br>" +
			"This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; " +
			"without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. " +
			"See the GNU General Public License for more details.<br>" + 
			"<br>" +
			"You should have received a copy of the GNU General Public License along with this program.  If not, you may obtain a copy at " +
			"<a href=\"https://www.gnu.org/licenses/gpl-3.0.html\">https://www.gnu.org/licenses/gpl-3.0.html</a><br>" + 
			"<br>" + 
			"OpenRocket developers may be contacted electronically at:<br>" + 
			"<a href=\"mailto:openrocket-devel@lists.sourceforge.net\">mailto:openrocket-devel@lists.sourceforge.net</a><br>" +
			"<a href=\"https://discord.gg/qD2G5v2FAw\">https://discord.gg/qD2G5v2FAw</a><br>" +
			"<a href=\"https://github.com/openrocket\">https://github.com/openrocket</a><br>" +
			"<br>";
		
		/*****************************************************************************************************************************/
        /* APACHE:  components library                                                                                               */
		/*****************************************************************************************************************************/
		final String componentsLicense = 
			"<strong>APACHE LICENSE</strong><br>" + 
			"<br>" + 
			"OpenRocket features the enhanced components database created by David B. Cook<br>" +
			"Copyright " + Chars.COPY + " 2015-" + copyrightYear + " David B. Cook<br>" +
			"Project page:  <a href=\"https://github.com/dbcook/openrocket-database\">https://github.com/dbcook/openrocket-database</a><br>" +
			"<br>" +
			"Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this work except in compliance with the License. " +
			"You may view the License " +
			"<a href=\"" + jarUrl + "!/datafiles/components/database/LICENSE\">here</a>.<br>" +
			"You may also obtain a copy of the License at " +
			"<a href=\"https://www.apache.org/licenses/LICENSE-2.0\">https://www.apache.org/licenses/LICENSE-2.0</a><br>" +
			"<br>";

		/*****************************************************************************************************************************/
        /* BITSTREAM VERA:  Deja Vu font                                                                                             */
		/*****************************************************************************************************************************/
		final String fontLicense =
			"<strong>BITSTREAM VERA FONT LICENSE</strong><br>" +
			"<br>" +
			"OpenRocket makes use of the DejaVu Serif Font<br>" +
			"Fonts are Copyright " +  Chars.COPY + " 2003 by Bitstream, Inc. All Rights Reserved. " +
			"Bitstream Vera is a trademark of Bitstream, Inc.<br>" +
			"DejaVu changes are in the public domain<br>" +
			"Glyphs imported from Arev Fonts Copyright " + Chars.COPY + " 2006 by Tavmjong Bah. All Rights Reserved.<br>" +
			"Project page: <a href=\"https://github.com/dejavu-fonts/dejavu-fonts/\">https://github.com/dejavu-fonts/dejavu-fonts/</a><br>" +
			"<br>" +
			"Licensed according to the Bitstream Vera Font License which may be found " +
			"<a href=\"" + jarUrl + "!/dejavu-font/LICENSE\">here</a>." +
			"<br>" +
			"You may also obtain a copy of the License at " +
			"<a href=\"https://github.com/dejavu-fonts/dejavu-fonts/blob/master/LICENSE\">https://github.com/dejavu-fonts/dejavu-fonts/blob/master/LICENSE</a><br>" +
			"<br>";
				
		/*****************************************************************************************************************************/
        /* BSD 2-Clause:  commonmark-java library                                                                                    */
		/*****************************************************************************************************************************/
		final String commonmarkLicense =
			"<strong>BSD 2-Clause License</strong><br>" +
		    "<br>" +
			"OpenRocket makes use of the Commonmark-Java Library<br>" +
			"Copyright " + Chars.COPY + " 2015-2016 Atlassian Pty Ltd. All rights reserved.<br>" +
			"Project page: <a href=\"https://github.com/commonmark/commonmark-java/\">https://github.com/commonmark/commonmark-java/</a><br>" +
			"<br>" +
			"You may obtain a copy of the License at <a href=\"https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt\">https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt</a>." +
			"<br>";

		/*****************************************************************************************************************************/
		/* BSD 3-Clause:  LWJGL                                                                                                       */
		/*****************************************************************************************************************************/
		final String lwjglLicense =
			"<strong>BSD 3-Clause License</strong><br>" +
			"<br>" +
			"OpenRocket makes use of LWJGL (Lightweight Java Game Library)<br>" +
			"Copyright " + Chars.COPY + " LWJGL. All rights reserved.<br>" +
			"Project page: <a href=\"https://www.lwjgl.org/\">https://www.lwjgl.org/</a><br>" +
			"Source code: <a href=\"https://github.com/LWJGL/lwjgl3\">https://github.com/LWJGL/lwjgl3</a><br>" +
			"<br>" +
			"You may obtain a copy of the License at <a href=\"https://www.lwjgl.org/license\">https://www.lwjgl.org/license</a>." +
			"<br>";

		/*****************************************************************************************************************************/
		/* MIT:  lwjgl3-awt                                                                                                           */
		/*****************************************************************************************************************************/
		final String lwjgl3AwtLicense =
			"<strong>MIT License</strong><br>" +
			"<br>" +
			"OpenRocket makes use of LWJGLX/lwjgl3-awt<br>" +
			"Copyright " + Chars.COPY + " 2015 Kai Burjack<br>" +
			"Project page: <a href=\"https://github.com/LWJGLX/lwjgl3-awt\">https://github.com/LWJGLX/lwjgl3-awt</a><br>" +
			"<br>" +
			"You may obtain a copy of the License at <a href=\"http://www.opensource.org/licenses/mit-license.php\">http://www.opensource.org/licenses/mit-license.php</a>." +
			"<br>";
		
		/*****************************************************************************************************************************/
        /* End of license text                                                                                                       */
		/*****************************************************************************************************************************/

		DescriptionArea info = new DescriptionArea(20);
		info.setTextFont(UIManager.getFont("Label.font"));
		info.setText(orLicense + componentsLicense + fontLicense + commonmarkLicense + lwjglLicense + lwjgl3AwtLicense);
		panel.add(info, "newline, width 700lp, height 250lp, pushy, grow, spanx, wrap para");
		
		//Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.setTitle("OpenRocket license");
		this.pack();
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
}
