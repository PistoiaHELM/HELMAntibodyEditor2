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
package org.roche.antibody.services.graphsynchronizer;

import java.util.Map;

import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.DomainService;
import org.roche.antibody.services.helmnotation.model.HELMConnection;
import org.roche.antibody.services.helmnotation.model.HELMElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@code PeptideToMoleculeConnectionBuilder}
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Stefan Zilch:</b> stefan_dieter DOT zilch AT contractors DOT roche DOT com
 * @version $Id$
 */
class GeneralConnectionBuilder extends ConnectionBuilder<GeneralConnection> {

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(GeneralConnectionBuilder.class);

  @Override
  public GeneralConnection build(HELMConnection helmConnection, Map<HELMElement, Sequence> sequenceMap) {
    
    int sPos = helmConnection.getSourcePosition();
    int tPos = helmConnection.getTargetPosition();
    Sequence source = sequenceMap.get(helmConnection.getSource());
    Sequence target = sequenceMap.get(helmConnection.getTarget());

    if (source instanceof Domain) {
      Domain dom = (Domain)source;
      sPos = DomainService.getInstance().transformDomainPositionToPeptidePosition(dom, helmConnection.getSourcePosition());
      source = dom.getPeptide();
    }
    
    if (target instanceof Domain) {
      Domain dom = (Domain) target;
      tPos = DomainService.getInstance().transformDomainPositionToPeptidePosition(dom, helmConnection.getTargetPosition());
      target = dom.getPeptide();
    }
    
    GeneralConnection newConn =
        new GeneralConnection(source, target, sPos, tPos, helmConnection.getSourceRest(),
            helmConnection.getTargetRest());
    LOG.debug("New GeneralConnection created: {}", newConn);
    return newConn;
  }

}
