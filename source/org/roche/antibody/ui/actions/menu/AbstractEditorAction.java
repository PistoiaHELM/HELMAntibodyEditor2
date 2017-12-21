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
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

import org.roche.antibody.ui.resources.ResourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quattroresearch.antibody.plugin.IEditorPlugin;

/**
 * 
 * {@code AbstractEditorAction} implements an {@code AbstractAction} used for all kinds of actions inside the antibody
 * editor. Implements {@code IEditorPlugin} to be used as adapter for plugin classes.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class AbstractEditorAction extends AbstractAction implements IEditorPlugin {

  /** default */
  private static final long serialVersionUID = 1L;

  private static Logger LOG = LoggerFactory
      .getLogger(AbstractEditorAction.class);

  private JFrame parentFrame;

  private String menuName = "Unknown Plugins";

  public AbstractEditorAction(JFrame parentFrame, String actionName) {
    super(actionName);

    this.parentFrame = parentFrame;
  }

  protected JFrame getParentFrame() {
    return parentFrame;
  }

  public ImageIcon getImageIcon(String imagePath) {
    URL imageURL = ResourceProvider.getInstance().get(imagePath);
    if (imageURL != null) {
      return new ImageIcon(imageURL);
    }
    return null;
  }

  public void setMenuName(String menuName) {
    this.menuName = menuName;
  }

  public String getMenuName() {
    return menuName;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // TODO Auto-generated method stub

  }

  /**
   * Override this method to achieve custom loading actions in your plugin. {@inheritDoc}
   */
  @Override
  public void onInit() {
    LOG.info("Plugin " + this.getClass().getSimpleName() + " initialized.");
  }

  /**
   * Should return the index, where to insert a menu entry. Recommended is returning -1, which means appending to the
   * end of the menu, but may not be sufficient in all cases. Override this function in that case.
   * 
   * @return index position in the menu, or -1 for the end of the list.
   */
  public int getMenuEntryIndex() {
    return -1;
  }

}
