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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import org.roche.antibody.ui.actions.menu.AbstractEditorAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quattroresearch.antibody.plugin.IEditorPlugin;

/**
 * 
 * {@code ToolBarToggleButton} describes a {@code JToggleButton} used by plugin system.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class ToolBarToggleButton extends JToggleButton implements IEditorPlugin {

  /** default */
  private static final long serialVersionUID = 1L;

  private static Logger LOG = LoggerFactory
      .getLogger(ToolBarToggleButton.class);

  private AbstractEditorAction action;

  public ToolBarToggleButton(AbstractEditorAction action) {
    this.action = action;

    setFocusable(false);
    setVerticalTextPosition(SwingConstants.BOTTOM);
    setHorizontalTextPosition(SwingConstants.CENTER);

    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        getEditorAction().actionPerformed(e);
        setSelected(false);
      }
    });
  }

  protected AbstractEditorAction getEditorAction() {
    return action;
  }

  @Override
  public void onInit() {
    LOG.info("Plugin " + this.getClass().getSimpleName() + " initialized.");
  }

}
