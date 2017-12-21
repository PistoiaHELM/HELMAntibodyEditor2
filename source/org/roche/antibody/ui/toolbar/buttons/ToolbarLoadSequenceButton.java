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
package org.roche.antibody.ui.toolbar.buttons;

import javax.swing.JFrame;

import org.roche.antibody.services.UIService;
import org.roche.antibody.ui.actions.menu.LoadSequenceAction;

/**
 * {@code MenuLoadSequenceAction} triggers the {@code LoadSequenceAction} from the toolbar.
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Stefan Zilch:</b> stefan_dieter DOT zilch AT contractors DOT roche DOT com
 * 
 * @version $Id: ToolbarLoadSequenceButton.java 15173 2015-03-05 16:18:13Z schirmb $
 */
public class ToolbarLoadSequenceButton extends ToolBarToggleButton {

  /** */
  private static final long serialVersionUID = 8270310476746408309L;

  public static final String IMAGE_PATH = "dna-add_big.png";

  public ToolbarLoadSequenceButton(JFrame parentFrame) {
    // super(new LoadSequenceAction(parentFrame));
    super(UIService.getInstance().getLoadSequenceAction());

    super.setIcon(getEditorAction().getImageIcon(IMAGE_PATH));
    setToolTipText(LoadSequenceAction.SHORT_DESCRIPTION);
    setText(LoadSequenceAction.NAME);

  }

}