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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

import org.roche.antibody.services.UIService;
import org.roche.antibody.ui.actions.menu.AbstractEditorAction;
import org.roche.antibody.ui.actions.menu.AntibodyDomainsLibraryEditorAction;
import org.roche.antibody.ui.actions.menu.AntibodyLoadXmlAction;
import org.roche.antibody.ui.actions.menu.AntibodySaveXmlAction;
import org.roche.antibody.ui.actions.menu.EditorHelpAction;
import org.roche.antibody.ui.actions.menu.EditorLegendAction;
import org.roche.antibody.ui.actions.menu.EditorSettingsAction;
import org.roche.antibody.ui.actions.menu.ExitEditorAction;
import org.roche.antibody.ui.actions.menu.LoadSequenceAction;
import org.roche.antibody.ui.actions.menu.ShowAboutAction;
import org.roche.antibody.ui.resources.ResourceProvider;
import org.roche.antibody.ui.toolbar.buttons.ToolbarLoadSequenceButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quattroresearch.antibody.plugin.PluginLoader;

/**
 * 
 * {@code AntibodyEditorGUI} encapsulates antibody editor GUI elements.
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * 
 * @version $Id$
 */
public class AntibodyEditorGUI {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(AntibodyEditorGUI.class);

  private JFrame parentFrame;

  private JPanel contentPanel;

  private JDialog helmEditorDialog;

  private SplashScreen splashScreen;

  /**
   * Constructor that creates the antibody editor GUI.
   * 
   * @param parentFrame
   */
  protected AntibodyEditorGUI(JFrame parentFrame) {
    this.parentFrame = parentFrame;

    splashScreenInit();
    splashScreen.setProgress("Initializing GUI", 0);

    // create editor
    initLnF();

    parentFrame.setJMenuBar(createMenuBar());

    JPanel upperPanel = new JPanel();
    upperPanel.add(new JLabel("upper panel (curr. unused)"));

    JPanel antibodyPanel = new JPanel();
    antibodyPanel.setLayout(new BorderLayout());
    antibodyPanel.setPreferredSize(new Dimension(100, 10));
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setPreferredSize(new Dimension(100, 50));

    JPanel antibodyView = new JPanel();
    antibodyView.setLayout(new BorderLayout());
    antibodyView.setBackground(Color.WHITE);

    tabbedPane.add("Antibody View", antibodyView);
    tabbedPane.setUI(new BasicTabbedPaneUI() {
      @Override
      protected int calculateTabAreaHeight(int tab_placement, int run_count, int max_tab_height) {
        return 0;
      }
    });
    antibodyPanel.add(tabbedPane, BorderLayout.CENTER);

    // JSplitPane viewSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperPanel,
    // antibodyPanel);
    UIService.getInstance().setAntibodyPanel(parentFrame, antibodyView);

    // viewSplitPane.setDividerLocation(50);
    // viewSplitPane.setOneTouchExpandable(false);

    // create buttons
    JPanel buttonPanel = createButtonPanel();
    UIService.getInstance().setToolBar(createToolBar());

    this.contentPanel = new JPanel();
    this.contentPanel.setLayout(new BorderLayout());
    // getContentPane().add(BorderLayout.CENTER, editor.getContentComponent());
    // this.contentPanel.add(viewSplitPane, BorderLayout.CENTER);
    this.contentPanel.add(UIService.getInstance().getToolBar(), BorderLayout.NORTH);
    this.contentPanel.add(antibodyPanel, BorderLayout.CENTER);
    this.contentPanel.add(buttonPanel, BorderLayout.SOUTH);

    splashScreen.setProgress("Loading Menu Plugins", 33);
    PluginLoader.getInstance().loadMenuPlugins();
    splashScreen.setProgress("Loading Toolbar Plugins", 66);
    PluginLoader.getInstance().loadToolbarPlugins();
    splashScreen.setProgress(100);
    splashScreen.setScreenVisible(false);
    splashScreen = null;

  }

  private void splashScreenInit() {
    URL url = ResourceProvider.getInstance().get("splashscreen.png");
    Toolkit kit = Toolkit.getDefaultToolkit();
    ImageIcon img = new ImageIcon(kit.createImage(url));
    splashScreen = new SplashScreen(img);
    splashScreen.setLocationRelativeTo(null);
    splashScreen.setProgressMax(100);
    splashScreen.setScreenVisible(true);
  }

  /**
   * Returns the editors main panel.
   * 
   * @return
   */
  protected JPanel getContentPanel() {
    return this.contentPanel;
  }

  /**
   * Initializes to a "nice" look and feel.
   */
  public static void initLnF() {
    ResourceBundle resourceBundle = ResourceBundle.getBundle("org.helm.editor.editor.resource.GUIBase");
    String lnf = resourceBundle.getString("Application.lookAndFeel");
    if (lnf != null && lnf.equalsIgnoreCase("System")) {
      try {
        if (!UIManager.getSystemLookAndFeelClassName().equals(
            "com.sun.java.swing.plaf.motif.MotifLookAndFeel")
            && !UIManager.getSystemLookAndFeelClassName().equals(
                "com.sun.java.swing.plaf.gtk.GTKLookAndFeel")
            && !UIManager.getSystemLookAndFeelClassName().equals(
                UIManager.getLookAndFeel().getClass().getName())) {
          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    LoadSequenceAction loadSequenceAction = new LoadSequenceAction(parentFrame);
    fileMenu.add(loadSequenceAction);
    UIService.getInstance().setLoadSequenceAction(loadSequenceAction);
    JMenuItem openItem = fileMenu.add(new AntibodyLoadXmlAction(parentFrame));
    openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
    JMenuItem saveItem = fileMenu.add(new AntibodySaveXmlAction(parentFrame));
    saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
    fileMenu.addSeparator();
    fileMenu.add(new ExitEditorAction(parentFrame));

    JMenu settingsMenu = new JMenu("Settings");
    settingsMenu.add(new AntibodyDomainsLibraryEditorAction(parentFrame));
    settingsMenu.add(new EditorSettingsAction(parentFrame));

    JMenu helpMenu = new JMenu("Help");
    AbstractAction about = new ShowAboutAction();
    about.putValue(Action.SMALL_ICON, new AbstractEditorAction(parentFrame, "About").getImageIcon("help.png"));
    helpMenu.add(about);
    helpMenu.add(new EditorHelpAction(parentFrame));
    helpMenu.add(new EditorLegendAction(parentFrame));

    menuBar.add(fileMenu);
    menuBar.add(settingsMenu);
    menuBar.add(helpMenu);

    return menuBar;
  }

  protected JToolBar createToolBar() {
    JToolBar toolBar = new JToolBar();

    toolBar.add(new ToolbarLoadSequenceButton(parentFrame));

    return toolBar;
  }

  /**
   * create a panel to hold the setNotation and getNotation Buttons
   *
   * @return JPanel
   */
  private JPanel createButtonPanel() {
// JButton btnOpenEditor = new JButton("open editor");
// btnOpenEditor.addActionListener(new ActionListener() {
// public void actionPerformed(ActionEvent e) {
// String editorResult = HELMEditorAccessService.openEditor(null);
// }
// });
//
    JPanel buttonPanel = new JPanel();
// buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
// buttonPanel.add(Box.createHorizontalGlue());
// buttonPanel.add(btnOpenEditor);
// buttonPanel.add(Box.createHorizontalStrut(5));
    return buttonPanel;
  }

}
