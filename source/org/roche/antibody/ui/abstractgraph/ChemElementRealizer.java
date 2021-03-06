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
package org.roche.antibody.ui.abstractgraph;

import java.awt.Color;
import java.awt.Graphics2D;

import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.services.AbConst;

import y.view.ShapeNodeRealizer;

/**
 * {@code ChemElementRealizer}
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Stefan Zilch:</b> stefan_dieter DOT zilch AT contractors DOT roche DOT com
 * 
 * @version $Id$
 */
public class ChemElementRealizer extends ShapeNodeRealizer implements AbstractGraphElementInitializer {

  private static final int DEFAULT_HEIGHT = 20;

  private static final int DEFAULT_WIDTH = 80;

  boolean initialized = false;

  public ChemElementRealizer() {
    super();
    initFromMap();
  }

  public void initFromMap() {

    try {

      setShapeType(OCTAGON);
      setFillColor(Color.YELLOW);
      setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

      ChemElement mol = (ChemElement) getNode().getGraph()
          .getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY)
          .get(getNode());
      if (mol != null) {
        setLabelText(mol.getName());
        initialized = true;
      }

    } catch (NullPointerException e) {
      // Not yet ready for initialization
    }
  }

  @Override
  protected void paintNode(Graphics2D graph) {
    if (!initialized) {
      initFromMap();
    }
    super.paintNode(graph);
  }
}
