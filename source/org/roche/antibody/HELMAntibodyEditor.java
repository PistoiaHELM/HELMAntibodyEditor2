/*******************************************************************************
 * Copyright C 2016, Roche pREDi (Roche Innovation Center Munich)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package org.roche.antibody;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.roche.antibody.services.CommandLineParameters;
import org.roche.antibody.services.ConfigFileService;
import org.roche.antibody.services.ConfigLoaderAuthentication;
import org.roche.antibody.services.DomainDetectionSettingsService;
import org.roche.antibody.services.PreferencesService;
import org.roche.antibody.ui.resources.ResourceProvider;

/**
 * 
 * {@code HELMAntibodyEditor} contains main routine that initializes antibody editor.
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * 
 * @version $Id$
 */
public class HELMAntibodyEditor extends JFrame {

  /** default */
  private static final long serialVersionUID = 1L;

  /** The Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(HELMAntibodyEditor.class
      .toString());

  public HELMAntibodyEditor() throws Exception {

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setPreferredSize(new Dimension(1024, 768));

    AntibodyEditorGUI antibodyEditor = new AntibodyEditorGUI(this);
    getContentPane().add(antibodyEditor.getContentPanel());

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        dispose();
        System.exit(0);
      }
    });

    URL url = ResourceProvider.getInstance().get("antibody_32.png");
    Toolkit kit = Toolkit.getDefaultToolkit();
    ImageIcon img = new ImageIcon(kit.createImage(url));
    this.setIconImage(img.getImage());

    pack();
    setExtendedState(MAXIMIZED_BOTH);

    ConfigLoaderAuthentication authClass = ConfigFileService.getInstance()
        .getAuthorizationClass();
    if (authClass.isPasswordNeeded()) {

      FlowLayout lo = new FlowLayout();
      JPanel panel = new JPanel(lo);

      JLabel labelName = new JLabel("Username");
      JTextField txtName = new JTextField(10);
      panel.add(labelName);
      panel.add(txtName);
      JLabel labelPass = new JLabel("Password");
      JPasswordField pass = new JPasswordField(10);
      panel.add(labelPass);
      panel.add(pass);
      
      String[] options = new String[] {"OK", "Cancel"};
      int option = JOptionPane.showOptionDialog(
          antibodyEditor.getContentPanel(), panel, "Please authorize",
          JOptionPane.NO_OPTION, JOptionPane.PLAIN_MESSAGE, null,
          options, txtName);//options[0]);
      if (option == JOptionPane.OK_OPTION) {
        char[] password = pass.getPassword();
        authClass.setUsernamePassword(txtName.getText(), new String(
            password));
        String error = authClass.checkLogon();
        if (!error.isEmpty()) {
          JOptionPane.showMessageDialog(antibodyEditor.getContentPanel(),
              error, "Not authorized",
              JOptionPane.ERROR_MESSAGE);
          dispose();
        }
      } else {
        JOptionPane.showMessageDialog(antibodyEditor.getContentPanel(),
            "Password-based authentication is configured. Use is forbidden without proper username and password.", "Unauthorized",
            JOptionPane.ERROR_MESSAGE);
        dispose();
      }
    }

    setTitle(generateApplicationTitle(authClass.getLogonUser()));

    DomainDetectionSettingsService.getInstance().reloadSettings();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String args[]) {
    try {
      CommandLineParameters.getInstance().setCommandLineParameters(args);

      HELMAntibodyEditor mEditor = new HELMAntibodyEditor();
      mEditor.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public static String generateApplicationTitle(String logonUser) {
    String environmentAddition = PreferencesService.getEnvironmentName();
    if (!environmentAddition.isEmpty()) {
      environmentAddition = " - " + environmentAddition;
    }

    String userAddition = "";
    if (logonUser != null && !logonUser.isEmpty()) {
      userAddition = " (" + logonUser + ")";
    }

    return "HELMAntibodyEditor v2.0" + environmentAddition + userAddition;
  }

}
