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
import java.text.DecimalFormat;

import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.model.antibody.StatisticalConnection;
import org.roche.antibody.services.AbConst;

import y.base.DataProvider;
import y.view.EdgeRealizer;
import y.view.GenericEdgeRealizer;
import y.view.LineType;

/**
 * Extended realizer class representing the edges connecting all domains within
 * antibody chain (non-cystein connection)
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com,
 *         Roche Pharma Research and Early Development - Informatics, Roche
 *         Innovation Center Munich
 * @author raharjap
 * 
 * @version $Id: GeneralConnectionEdgeRealizer.java 15417 2015-03-24 10:18:30Z
 *          schirmb $
 */
public class GeneralConnectionEdgeRealizer extends GenericEdgeRealizer
		implements AbstractGraphElementInitializer {

	/**
	 * Constructor
	 */
	public GeneralConnectionEdgeRealizer() {
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
			DataProvider cysBridgeMap = getEdge().getGraph().getDataProvider(
					AbConst.EDGE_TO_CONNECTION_KEY);

			if (cysBridgeMap.get(getEdge()) instanceof StatisticalConnection) {
				StatisticalConnection bridge = (StatisticalConnection) cysBridgeMap
						.get(getEdge());

				String aminoAcid;
				// ML: connection could go in both directions
				if (bridge.getSource() instanceof Peptide) {
          aminoAcid = "" + bridge.getSource().getSequence()
              .charAt(bridge.getSourcePosition() - 1) + "";
        } else if (bridge.getTarget() instanceof Peptide) {
          aminoAcid = "" + bridge.getTarget().getSequence().charAt(bridge.getTargetPosition() - 1) + "";
				} else {
          aminoAcid = "";
				}

        super.setLabelText(aminoAcid
						+ ":"
						+ bridge.getSourceRest()
						+ "-"
            + bridge.getTargetPosition() + ":" + bridge.getTargetRest()
						+ "("
						+ new DecimalFormat("0.00").format(bridge
								.getEquivalents()) + ")");
				getLabel().setFontSize(8);
				setupEdgeRealizerForStatisticalBonds(this);
			} else {
				GeneralConnection bridge = (GeneralConnection) cysBridgeMap
						.get(getEdge());

        char aminoAcidSource;
        char aminoAcidTarget;
        String sourceInfo = "";
        String targetInfo = "";
        if (bridge.getSource() instanceof Peptide) {
          aminoAcidSource = bridge.getSource().getSequence()
              .charAt(bridge.getSourcePosition() - 1);
          sourceInfo = "(" + aminoAcidSource + ")";
        }
        if (bridge.getTarget() instanceof Peptide) {
          aminoAcidTarget = bridge.getTarget().getSequence()
              .charAt(bridge.getTargetPosition() - 1);
          targetInfo = "(" + aminoAcidTarget + ")";
        }


        super.setLabelText(bridge.getSourcePosition() + sourceInfo + ":"
            + bridge.getSourceRest() + "-"
            + bridge.getTargetPosition() + targetInfo + ":"
						+ bridge.getTargetRest());
				getLabel().setFontSize(8);
				setLineColor(Color.BLUE);
				setLineType(LineType.LINE_2);
			}
		} catch (Exception e) {
			// Not yet ready for initialization
		}
	}

  public static void setupEdgeRealizerForStatisticalBonds(EdgeRealizer edgeRealizer) {
    // DOTTED lines are currently not visible in SVGs within Chrome (bug...)
	  // ML: 2015-09-23 There's no chrome inside the HELMEditor-> changed back
    edgeRealizer.setLineType(LineType.LINE_6);
    edgeRealizer.setLineColor(Color.GREEN);
  }
}

