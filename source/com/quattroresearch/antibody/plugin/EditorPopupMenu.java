/*******************************************************************************
 * Copyright C 2016, quattro research GmbH, Roche pREDi (Roche Innovation Center Munich)
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
package com.quattroresearch.antibody.plugin;

import javax.swing.JFrame;
import javax.swing.JMenu;

import org.roche.antibody.model.antibody.Domain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@code EditorPopupMenu} is a domain popup plugin class, used to inject menus into a domains context menu.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class EditorPopupMenu extends JMenu implements IEditorPlugin {

  /** */
  private static final long serialVersionUID = -8187599773865304632L;

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(EditorPopupMenu.class);

  private JFrame parentFrame;

  public EditorPopupMenu(JFrame parentFrame, String menuTitle) {
    super(menuTitle);
    this.parentFrame = parentFrame;
  }

  public JFrame getParentFrame() {
    return parentFrame;
  }

  /**
   * Please override loading routine in your plugin.
   * 
   * @param domain
   * @return
   */
  public JMenu load(Domain domain) {
    return null;
  }

  @Override
  public void onInit() {
    LOG.info("Plugin " + this.getClass().getSimpleName() + " initialized.");
  }

}
