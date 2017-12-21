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

package org.roche.antibody.ui.actions.menu;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.roche.antibody.ui.components.LegendDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@code EditorLegendAction} opens the legend dialog, containing information on how an antibody is drawn.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class EditorLegendAction extends AbstractEditorAction {

  private static final long serialVersionUID = 1L;

  public static final String NAME = "Legend";

  public static final String SHORT_DESCRIPTION = "Shows the legend.";

  public static final String IMAGE_PATH = "help.png";

  /** The Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(EditorLegendAction.class);

  public EditorLegendAction(JFrame parentFrame) {
    super(parentFrame, NAME);

    ImageIcon icon = getImageIcon(IMAGE_PATH);
    if (icon != null) {
      this.putValue(Action.SMALL_ICON, icon);
    }
    this.putValue(Action.SHORT_DESCRIPTION, SHORT_DESCRIPTION);
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    LegendDialog dialog = null;

    try {

      dialog = new LegendDialog(super.getParentFrame(), false);
      dialog.setLocationRelativeTo(super.getParentFrame());
      dialog.setVisible(true);
    } catch (NullPointerException ex) {
      ex.printStackTrace();
      // no dialog on error --> error handling inside
    }

  }
}
