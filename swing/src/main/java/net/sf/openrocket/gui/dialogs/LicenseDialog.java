package net.sf.openrocket.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.util.BuildProperties;
import net.sf.openrocket.util.Chars;

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
			"You should have received a copy of the GNU Public License along with this program.  If not, you may obtain a copy at " +
			"<a href=\"https://www.gnu.org/licenses/gpl-3.0.html\">https://www.gnu.org/licenses/gpl-3.0.html</a><br>" + 
			"<br>" + 
			"OpenRocket developers may be contacted electronically at:<br>" + 
			"<a href=\"mailto:openrocket-devel@lists.sourceforge.net\">mailto:openrocket-devel@lists.sourceforge.net</a><br>" +
			"<a href=\"https://openrocket.slack.com\">https://openrocket.slack.com</a><br>" +
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
			"<a href=\"" + jarUrl + "!/datafiles/presets/LICENSE\">here</a>.<br>" +
			"You may also obtain a copy of the License at " + 
			"<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a><br>" +
			"<br>" +
			"OpenRocket uses the Work or Derivative Works of Ant, a product which includes software developed by the Apache " +
			"Software Foundation<br>" +
			"<br>" +
			"Ant also includes software developed by:" +
			"<ul style=\"margin-top: 0;\">" +
			"<li>the W3C Consortium (<a href=\"http://www.w3c.org\">http://www.w3c.org</a>)</li>" +
			"<li>the SAX project (<a href=\"http://www.saxproject.org\">http://www.saxproject.org</a></li>" +
			"</ul>" +
			"The names \"Ant\" and \"Apache Software Foundation\" must not be used to endorse or " +
			"promote products derived from this software without prior written permission.  For written permission, "+
			"please contact <a href=\"mailto:apache@apache.org\">apache@apache.org</a>.<br>" +
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
			"Project page: <a href=\"https://github.com/dejavu-fonts/dejavu-fonts/\">https://github.com/dejavu-fonts/dejavu-fonts/<a/><br>" +
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
			"Project page: <a href=\"https://github.com/commonmark/commonmark-java/\">https://github.com/commonmark/commonmark-java/<a/><br>" +
			"<br>" +
			"You may obtain a copy of the License at <a href=\"https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt\">https://github.com/commonmark/commonmark-java/blob/main/LICENSE.txt</a>." +
			"<br>";
		
		/*****************************************************************************************************************************/
        /* End of license text                                                                                                       */
		/*****************************************************************************************************************************/

		DescriptionArea info = new DescriptionArea(20);
		info.setTextFont(UIManager.getFont("Label.font"));
		info.setText(orLicense + componentsLicense + fontLicense + commonmarkLicense);
		panel.add(info, "newline, width 700lp, height 250lp, pushy, grow, spanx, wrap para");
		
		//Close button
		JButton close = new SelectColorButton(trans.get("dlg.but.close"));
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
