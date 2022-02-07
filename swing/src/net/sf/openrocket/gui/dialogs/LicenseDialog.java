package net.sf.openrocket.gui.dialogs;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

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
		panel.add(new JLabel(Icons.loadImageIcon("pix/icon/icon-about.png", "OpenRocket")), "top");
		
		panel.add(new StyledLabel("Software Licenses", 10), "ax 50%, wrap para");

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
		final String gplInternalHref = "<a href=\"" + jarUrl + "!/LICENSE.TXT\">here</a>";
		final String orLicense = "<strong>GNU GENERAL PUBLIC LICENSE</strong>" + "<br>" +
			"<br>" +
			"OpenRocket - A model rocket simulator<br>" + 
			"Copyright " + Chars.COPY + " 2007-" + copyrightYear + "Sampo Niskanen and others<br>" +
			"Project page: <a href=\"https://openrocket.info/\">https://openrocket.info/</a><br>" +
			"<br>" + 
			"This program is free software: you can redistribute it and/or modify it under the terms " +
			"of the GNU General Public License as published by the Free Software Foundation, either " +
			"version 3 of the License, or any later version.<br>" + 
			"<br>" + 
			"This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; " +
			"without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. " +
			"See the GNU General Public License " + gplInternalHref + " for more details.<br>" + 
			"<br>" + 
			"A copy of the GNU General Public License may be viewed " + gplInternalHref + ". " +
			"You may also obtain a copy of the License at:<br>" + 
			"<br>" + 
			"<a href=\"https://www.gnu.org/licenses/gpl-3.0.html\">https://www.gnu.org/licenses/gpl-3.0.html</a><br>" + 
			"<br>" + 
			"OpenRocket developers may be contacted electronically at:<br>" + 
			"<br>" + 
			"<a href=\"mailto:openrocket-devel@lists.sourceforge.net\">mailto:openrocket-devel@lists.sourceforge.net</a><br>" + 
			"<a href=\"https://github.com/openrocket\">https://github.com/openrocket</a><br>" +
			"<br>";
		
		/*****************************************************************************************************************************/
        /* APACHE:  components library                                                                                               */
		/*****************************************************************************************************************************/
		final String apacheInternalHref = "<a href=\"" + jarUrl + "!/datafiles/presets/LICENSE\">here</a>";
		final String componentsLicense = 
			"<strong>APACHE LICENSE</strong><br>" + 
			"<br>" + 
			"Enhanced components database for OpenRocket<br>" +
			"Copyright " + Chars.COPY + " 2015-" + copyrightYear + " David B. Cook<br>" +
			"Project page:  <a href=\"https://github.com/dbcook/openrocket-database\">https://github.com/dbcook/openrocket-database</a><br>" +
			"<br>" +
			"Licensed under the Apache License, Version 2.0 (the \"License\"); you may not use this work except in compliance with the License. " +
			" A copy of the Apache License may be viewed " + apacheInternalHref + ".  You may also obtain a copy of the License at:<br>" + 
			"<br>" +
			"<a href=\"http://www.apache.org/licenses/LICENSE-2.0\">http://www.apache.org/licenses/LICENSE-2.0</a><br><br>" +
			
			"Unless required by applicable law or agreed to in writing, software distributed under the License is distributed " +
			"on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License " +
			apacheInternalHref + " for the specific language governing permissions and limitations under the License.<br>" +
			"<br>";

		
		/*****************************************************************************************************************************/
        /* BITSTREAM VERA:  Deja Vu font                                                                                             */
		/*****************************************************************************************************************************/
		final String dejaLicenseUrl = jarUrl + "!/dejavu-font/LICENSE";
		final String fontLicense =
			"<strong>Bitstream Vera Font License</strong><br>" +
			"<br>" +
			"DejaVu Serif Font<br>" +
			"Fonts are Copyright " +  Chars.COPY + " 2003 by Bitstream, Inc. All Rights Reserved. Bitstream Vera is a trademark of Bitstream, Inc.<br>" +
			"DejaVu changes are in the public domain<br>" +
			"Glyphs imported from Arev Fonts Copyright " + Chars.COPY + " 2006 by Tavmjong Bah. All Rights Reserved.<br>" +
			"<br>" +
			"Licensed according to the Bitstream Vera Font License which may be found <a href=\"" + dejaLicenseUrl + "\">here</a>." +
			"<br>" +
			"THE FONT SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS " +
			"OR IMPLIED, INCLUDING BUT NOT LIMITED TO ANY WARRANTIES OF MERCHANTABILITY " +
			"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF COPYRIGHT, PATENT, " +
			"TRADEMARK, OR OTHER RIGHT. IN NO EVENT SHALL BITSTREAM OR THE GNOME " +
			"FOUNDATION BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, INCLUDING " +
			"ANY GENERAL, SPECIAL, INDIRECT, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, " +
			"WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF " +
			"THE USE OR INABILITY TO USE THE FONT SOFTWARE OR FROM OTHER DEALINGS IN THE " +
			"FONT SOFTWARE.<br>" +
			"<br>" +
			"See the <a href=\"" + dejaLicenseUrl + "\">License</a> for the specific language" +
			"governing permissions and limitations under the License.<br>" +
			"<br>";
		
		
		/*****************************************************************************************************************************/
        /* BSD 2-Clause:  commonmark-java library                                                                                    */
		/*****************************************************************************************************************************/
		final String commonmarkLicense =
			"<strong>BSD 2-Clause License</strong><br>" +
			"<br>" +
			"Commonmark-Java Library<br>" +
			"Copyright " + Chars.COPY + " 2015-2016 Atlassian Pty Ltd<br>" +
			"All rights reserved.<br>" +
			"<br>" +
			"Redistribution and use in source and binary forms, with or without" +
			"modification, are permitted provided that the following conditions are met:<br>" +
			"<ul>" +
			"<li>Redistributions of source code must retain the above copyright notice, this " +
			"list of conditions and the following disclaimer.<br></li>" +
			"<li>Redistributions in binary form must reproduce the above copyright notice, " +
			"this list of conditions and the following disclaimer in the documentation " +
			"and/or other materials provided with the distribution.</li></ul>" +
			"<br>" +
			"THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" " +
			"AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED " +
			"WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. " +
			"IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, " +
			"INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, " +
			"BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, " +
			"DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF " +
			"LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE " +
			"OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED " +
			"OF THE POSSIBILITY OF SUCH DAMAGE.<br>" +
			"<br>";
		
		/*****************************************************************************************************************************/
        /* End of license text                                                                                                       */
		/*****************************************************************************************************************************/

		DescriptionArea info = new DescriptionArea(20);
		info.setText(orLicense + componentsLicense + fontLicense + commonmarkLicense);
		panel.add(info, "newline, width 600lp, height 150lp, grow, spanx, wrap para");
		
		//Close button
		JButton close = new SelectColorButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				LicenseDialog.this.dispose();
			}
		});
		panel.add(close, "right");
		
		this.add(panel);
		this.setTitle("OpenRocket license");
		this.pack();
		this.setLocationRelativeTo(parent);
		
		GUIUtil.setDisposableDialogOptions(this, close);
	}
	
}
