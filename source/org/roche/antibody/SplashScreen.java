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
import java.awt.FlowLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JWindow;
import javax.swing.SwingUtilities;

/**
 * 
 * {@code SplashScreen}
 * 
 * Free splashscreen implementation from Alvin Alexander.
 * http://alvinalexander.com/java/java-splash-screen-with-progress-bar-1
 * 
 * @version $Id$
 */
public class SplashScreen extends JWindow {
  private static final long serialVersionUID = 1L;

  BorderLayout borderLayout1 = new BorderLayout();

  JLabel imageLabel = new JLabel();

  JPanel southPanel = new JPanel();

  FlowLayout southPanelFlowLayout = new FlowLayout();

  JProgressBar progressBar = new JProgressBar();

  ImageIcon imageIcon;

  public SplashScreen(ImageIcon imageIcon) {
    this.imageIcon = imageIcon;
    try {
      jbInit();
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  // note - this class created with JBuilder
  void jbInit() throws Exception {
    imageLabel.setIcon(imageIcon);
    this.getContentPane().setLayout(borderLayout1);
    southPanel.setLayout(southPanelFlowLayout);
    southPanel.setBackground(Color.BLACK);
    this.getContentPane().add(imageLabel, BorderLayout.CENTER);
    this.getContentPane().add(southPanel, BorderLayout.SOUTH);
    southPanel.add(progressBar, null);
    this.pack();
  }

  public void setProgressMax(int maxProgress)
  {
    progressBar.setMaximum(maxProgress);
  }

  public void setProgress(int progress)
  {
    final int theProgress = progress;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        progressBar.setValue(theProgress);
      }
    });
  }

  public void setProgress(String message, int progress)
  {
    final int theProgress = progress;
    final String theMessage = message;
    setProgress(progress);
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        progressBar.setValue(theProgress);
        setMessage(theMessage);
      }
    });
  }

  public void setScreenVisible(boolean b)
  {
    final boolean boo = b;
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        setVisible(boo);
      }
    });
  }

  private void setMessage(String message)
  {
    if (message == null)
    {
      message = "";
      progressBar.setStringPainted(false);
    }
    else
    {
      progressBar.setStringPainted(true);
    }
    progressBar.setString(message);
  }

}

