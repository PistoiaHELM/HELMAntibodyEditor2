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

import java.awt.Graphics2D;

import y.view.GenericEdgeRealizer;
import y.view.LineType;

/**
 * Extended realizer class representing the edges connecting all domains within antibody chain (non-cystein connection)
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author Stefan Zilch - BridgingIT GmbH
 * @version $Id$
 */
public class DomainEdgeRealizer extends GenericEdgeRealizer implements AbstractGraphElementInitializer {

  public DomainEdgeRealizer() {
    super();
    initFromMap();
  }

  @Override
  public void paint(Graphics2D gfx) {
    super.paint(gfx);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void initFromMap() {
    this.setLineType(LineType.LINE_2);
  }

}
