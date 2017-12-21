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

import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.services.AbConst;

import y.base.DataProvider;
import y.view.GenericEdgeRealizer;
import y.view.LineType;

/**
 * Extended realizer class representing the edges connecting all domains within antibody chain
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author raharjap
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * 
 * @version $Id$
 */
public class CysBridgeEdgeRealizer extends GenericEdgeRealizer implements AbstractGraphElementInitializer {

  /**
   * Constructor
   */
  public CysBridgeEdgeRealizer() {
    initFromMap();
  }

  @Override
  public void paint(Graphics2D gfx) {
    initFromMap();
    super.paint(gfx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initFromMap() {
    try {
      setLineType(LineType.LINE_1);
      setLineColor(Color.RED);

      DataProvider cysBridgeMap = getEdge().getGraph().getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
      CysteinConnection bridge = (CysteinConnection) cysBridgeMap.get(getEdge());
      super.setLabelText("Cys:Cys : " + bridge.getSourcePosition() + ":" + bridge.getTargetPosition());
      getLabel().setFontSize(8);

    } catch (Exception e) {
      // Not yet ready for initialization
    }
  }
}
