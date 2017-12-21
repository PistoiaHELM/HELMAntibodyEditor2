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
package org.roche.antibody.services;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import org.roche.antibody.ui.actions.menu.LoadSequenceAction;
import org.roche.antibody.ui.components.AntibodyEditorPane;

import com.quattroresearch.antibody.AntibodyFindDialog;

/**
 * {@code UIService}
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @author <b>Stefan Zilch:</b> stefan_dieter DOT zilch AT contractors DOT roche DOT com
 * @version $Id$
 */
public class UIService {

  /** static Singleton instance */
  private static UIService instance;

  /** The editors parentFrame. */
  private JFrame parentFrame;

  /** The bottom panel, where the antibody pane is shown */
  private JPanel antibodyPanel;

  private JToolBar toolBar;

  private AntibodyFindDialog antibodyFindDialog;

  private LoadSequenceAction loadSequenceAction;

  /** Private constructor for singleton */
  private UIService() {
  }

  /** Static getter method for retrieving the singleton instance */
  public synchronized static UIService getInstance() {
    if (instance == null) {
      instance = new UIService();
    }
    return instance;
  }

  public void setLoadSequenceAction(LoadSequenceAction action) {
    this.loadSequenceAction = action;
  }

  public LoadSequenceAction getLoadSequenceAction() {
    return this.loadSequenceAction;
  }

  public JFrame getMainFrame() {
    return parentFrame;
  }

  public JPanel getGlassPane() {
    return (JPanel) parentFrame.getGlassPane();
  }

  public AntibodyFindDialog getAntibodyFindDialog() {
    return antibodyFindDialog;
  }

  public void setAntibodyFindDialog(AntibodyFindDialog antibodyFindDialog) {
    this.antibodyFindDialog = antibodyFindDialog;
  }

  public void setAntibodyPanel(JFrame parentFrame, JPanel antibodyPanel) {
    this.parentFrame = parentFrame;
    this.antibodyPanel = antibodyPanel;
  }

  public void setToolBar(JToolBar toolBar) {
    this.toolBar = toolBar;
  }

  public JToolBar getToolBar() {
    return toolBar;
  }

  public AntibodyEditorPane addAntibodyViewEditor(
      AntibodyFindDialog antibodyFindDialog) {
    UIService.getInstance().setAntibodyFindDialog(antibodyFindDialog);
    AntibodyEditorPane viewDialog = new AntibodyEditorPane(parentFrame);
    this.antibodyPanel.removeAll();
    this.antibodyPanel.add(viewDialog);

    this.antibodyPanel.revalidate();
    this.antibodyPanel.repaint();

    return viewDialog;
  }

  /**
   * We add or replace the Antibody View Editor in the tab pane of the main editor window
   *
   * @param editor - main window
   * @param findDialog - can be null, if user loads Antibody via XML functionality.
   * @return created {@link AntibodyEditorPane}
   */
  public AntibodyEditorPane addAntibodyViewEditor() {
    return addAntibodyViewEditor(null);
  }

}
