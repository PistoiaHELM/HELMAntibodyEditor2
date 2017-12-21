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

package org.roche.antibody.ui.components;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * 
 * {@code LegendDialog} loads and shows the legend which explains how an antibody is drawn.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class LegendDialog extends javax.swing.JDialog {

  private static final long serialVersionUID = 1L;

  private Frame parent;

  /**
   * Creates new form LegendDialog
   */
  public LegendDialog(java.awt.Frame parent, boolean modal) {
    super(parent, modal);
    initComponents();

  }

  private void initComponents() {

    ImageIcon image = null;
    try {
      image = new ImageIcon(ImageIO.read(new File("resources\\RAbE_legend.jpg")));
    } catch (IOException e) {
      e.printStackTrace();
    }
    picture = new javax.swing.JLabel(image);
    picture.setText("");
    setTitle("Legend");
    setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
    getContentPane().add(picture);
    setResizable(false);

    pack();
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    setLocationRelativeTo(parent);
    setLocation(dimension.width - this.getWidth(), dimension.height
        - this.getHeight());
  }

  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLabel picture;
  // End of variables declaration//GEN-END:variables
}
