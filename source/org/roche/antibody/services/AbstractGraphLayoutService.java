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
package org.roche.antibody.services;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.DomainType;
import org.roche.antibody.ui.abstractgraph.CysBridgeEdgeRealizer;
import org.roche.antibody.ui.abstractgraph.DomainEdgeRealizer;

import y.base.Edge;
import y.base.EdgeCursor;
import y.base.Node;
import y.base.NodeMap;
import y.layout.CanonicMultiStageLayouter;
import y.layout.hierarchic.GivenLayersLayerer;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.RoutingStyle;
import y.layout.router.OrthogonalEdgeRouter;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.Graph2DLayoutExecutor;
import y.view.hierarchy.HierarchyManager;

/**
 * {@code AbstractGraphLayoutService} contains all methods, related to the graphical layout of an Abstract Antibody
 * Graph (derived from {@link AbstractGraphService#getGraph(org.roche.antibody.model.antibody.Antibody)}).
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Clemens Wrzodek:</b> clemens DOT wrzodek AT roche DOT com
 * @author <b>Sabrina Hecht:</b> hecht AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class AbstractGraphLayoutService {

  /** The Logger for this class */
  private static final Logger LOG = Logger.getLogger(AbstractGraphLayoutService.class);

  private static final int CORE_HINGE_LAYER = 0;

  private static final int UP_HINGE_LAYER = 0;

  private static final int VARIABLE_LAYER = 0;

  public static IncrementalHierarchicLayouter createLayouter() {
    // create and configure the layout algorithm
    IncrementalHierarchicLayouter hierarchicLayouter = new IncrementalHierarchicLayouter();
    hierarchicLayouter.setFixedElementsLayerer(new GivenLayersLayerer());
    hierarchicLayouter.setComponentLayouterEnabled(false);
    hierarchicLayouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
    hierarchicLayouter.getEdgeLayoutDescriptor().setRoutingStyle(new RoutingStyle(RoutingStyle.EDGE_STYLE_ORTHOGONAL));
    /* Grouping */
    hierarchicLayouter.setGroupNodeHidingEnabled(true);
    hierarchicLayouter.setRecursiveGroupLayeringEnabled(false);
    return hierarchicLayouter;
  }

  /**
   * Idea: we find the hinge in the chain and calculate an offset to layer 0; afterwards we find the layer indexes with
   * the constraint that each hinge is in layer 0. so domains before the hinge are < 0 and domains after the hinge are >
   * 0
   * 
   * @param domainMap
   * @param sequenceNodes
   * @param layerIdMap
   */
  public static void bringHingeToHorizontalLine(NodeMap domainMap, List<Node> sequenceNodes,
      NodeMap layerIdMap) {
    int offset = 0;
    boolean found = false;
    int firstAlignedNode = 0;
    /* Save all variable regions, upHinges, coreHinges */
    List<Integer> variableRegions = new ArrayList<Integer>();
    List<Integer> upHinges = new ArrayList<Integer>();
    List<Integer> coreHinges = new ArrayList<Integer>();
    for (int i = 0; i < sequenceNodes.size(); i++) {
      Domain domain = ((Domain) domainMap.get(sequenceNodes.get(i)));
      if (((Domain) domainMap.get(sequenceNodes.get(i))).isHinge()) {
        offset = i;
        coreHinges.add(i);
      } else if (((Domain) domainMap.get(sequenceNodes.get(i))).getDomainType().equals(DomainType.UP_HINGE)) {
        offset = i;
        upHinges.add(i);
      } else if (((Domain) domainMap.get(sequenceNodes.get(i))).getDomainType().equals(DomainType.VARIABLE)) {
        offset = i;
        variableRegions.add(i);
      }
    }

    /* Rule 1: align first coreHinge together on layerID 0 */
    if (coreHinges.size() > 0) {
      alignOnSameLevel(coreHinges.get(0), sequenceNodes.size(), sequenceNodes, layerIdMap, CORE_HINGE_LAYER);
      firstAlignedNode = coreHinges.get(0);
    }

    /* Rule 2: align first upHinge together on layerId 0 */
    else if (upHinges.size() > 0) {
      int lastElement = sequenceNodes.size();
      if (coreHinges.size() > 0) {
        lastElement = coreHinges.get(0);
      }
      alignOnSameLevel(upHinges.get(0), lastElement, sequenceNodes, layerIdMap, UP_HINGE_LAYER);
      firstAlignedNode = upHinges.get(0);
    }

    /* Rule 3: align first variable Region together on layerID 0 */
    else if (variableRegions.size() > 0) {
      int lastElement = sequenceNodes.size();
      if (upHinges.size() > 0) {
        lastElement = upHinges.get(0);
      } else if (coreHinges.size() > 0) {
        lastElement = coreHinges.get(0);
      }
      alignOnSameLevel(variableRegions.get(0), lastElement, sequenceNodes, layerIdMap, VARIABLE_LAYER);
      firstAlignedNode = variableRegions.get(0);
    }

    /* Rule 4: align rest */
    if (firstAlignedNode != 0) {
      alignOnSameLevel(0, firstAlignedNode, sequenceNodes, layerIdMap, UP_HINGE_LAYER - firstAlignedNode);
    }

    /* no core hinges and no upHinges are there */
    if (coreHinges.size() == 0 & upHinges.size() == 0 & variableRegions.size() == 0) {
      alignOnSameLevel(0, sequenceNodes.size(), sequenceNodes, layerIdMap, 0);
    }

  }

  /**
   * align the first element on layer, the other elements directly after it
   * 
   * @param firstElement node to be on layer
   * @param lastElement last node to set
   * @param sequenceNodes nodes
   * @param layerIdMap
   * @param layer layer to set
   */
  private static void alignOnSameLevel(int firstElement, int lastElement, List<Node> sequenceNodes,
      NodeMap layerIdMap, int layer) {
    int j = 0;
    for (int i = firstElement; i < lastElement; i++) {
      int layerIndex = layer + j;
      LOG.trace("" + sequenceNodes.get(i) + ": " + layerIndex);
      layerIdMap.setInt(sequenceNodes.get(i), layerIndex);
      j++;
    }
  }

  /**
   * Layouts the ANTIBODY graph.
   * 
   * @param abstractGraph
   * @param abstractGraphLayouter
   */
  public static void layout(Graph2D abstractGraph) {
    layout(abstractGraph, null);
  }

  /**
   * Layouts the ANTIBODY graph.
   * 
   * @param abstractGraph
   * @param abstractGraphLayouter instance of {@link AbstractGraphService#createLayouter()} or {@code null} to create a
   *          new instance.
   */
  public static void layout(Graph2D abstractGraph, CanonicMultiStageLayouter abstractGraphLayouter) {
    if (abstractGraphLayouter == null) {
      abstractGraphLayouter = createLayouter();
    }
    final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
    // SH/ML 2016-04-19: Removing the last node would lead to a layouting error, because of empty group
    removeEmptyGroups(abstractGraph);
    layoutExecutor.doLayout(abstractGraph, createOrthogonalEdgeRouter(abstractGraphLayouter));
    layoutExecutor.doLayout(abstractGraph, abstractGraphLayouter);

  }

  private static void removeEmptyGroups(Graph2D graph) {
    HierarchyManager hm = graph.getHierarchyManager();
    Node[] nodes = graph.getNodeArray();

    for (Node node : nodes) {
      if (hm.isGroupNode(node)) {
        if (hm.getChildren(node).size() == 0) {
          graph.removeNode(node);

        }
      }
    }

  }

  public static OrthogonalEdgeRouter createOrthogonalEdgeRouter(CanonicMultiStageLayouter abstractGraphLayouter) {
    OrthogonalEdgeRouter edgerouter = new OrthogonalEdgeRouter(abstractGraphLayouter);
    edgerouter.setLocalCrossingMinimizationEnabled(true);
    return edgerouter;
  }

  public static void updateLayoutHints(Graph2D graph) {
    if (graph == null) {
      return;
    }

    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap layers = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
    NodeMap nodeToSequence = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap swimlaneMap = (NodeMap) graph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);

    // Inspect all nodes with missing layer information
    for (Node n : graph.getNodeArray()) {
      // graph.setLabelText(n, String.valueOf(layers.get(n))); // Can be used for debugging the layers

      Integer layerId = layers.get(n) == null ? null : layers.getInt(n);

      if (!(nodeToSequence instanceof Domain))
        continue;
      // Domain d = (Domain) (nodeToSequence.get(n) == null ? null : nodeToSequence.get(n));
      Domain d = (Domain) nodeToSequence.get(n);

      if (layerId == null || (layerId == 0 && !d.isHinge())) {
        if (d.isHinge()) {
          layers.setInt(n, 0);
          continue;
        }

        // This node needs to get a layer info => Inspect adjacent nodes
        Collection<Integer> adjacentLayers = getLevelOfAdjacentNodes(n, graph);
        // Now we need to stick our node either on top of the adjavent (=adjacentLayer-1) or below (=adjacentLayer+1).
        // we actually need to inspect the adjacent nodes of the adjecent node to decide which direction to go
        // however, we aproximate since the top layer always has layer id "1". This fails only if top layer gets
        // deleted.
        if (adjacentLayers == null || adjacentLayers.size() < 1) {
          continue; // Can't do antything
        }
        int adjacentLayer = (int) max(adjacentLayers);

        // Based on value of adjacent nodes, assign value to this node
        layers.setInt(n, adjacentLayer > 1 ? adjacentLayer + 1 : adjacentLayer - 1);
        swimlaneMap.set(n, getAdjacentSwimlane(n, graph)); // Place in same column as adjacent nodes

        LOG.debug(String.format("Fixed missing adjacent layer for node '%s' ('%s'). Assigned: %s. Swimlane: %s.",
            graph.getLabelText(n).replace("\n", " | "), d.getName(), layers.getInt(n), swimlaneMap.get(n)));
      }
    }

  }

  /**
   * @param adjacentLayers
   * @return 0 if given collection is empty (or null), else, the minimum element.
   */
  private static int max(Collection<Integer> adjacentLayers) {
    Integer max = Integer.MIN_VALUE;

    if (adjacentLayers == null || adjacentLayers.size() < 1) {
      return 0;
    }

    for (Integer i : adjacentLayers) {
      if (i != null && i > max) {
        max = i;
      }
    }

    return max;
  }

  /**
   * @param n
   * @return
   */
  private static Collection<Integer> getLevelOfAdjacentNodes(Node n, Graph2D abstractGraph) {
    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap layers = (NodeMap) abstractGraph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);

    // Inspect mapValues in layers of adjacent nodes
    EdgeCursor ec = n.edges();
    Set<Integer> adjacentLayer = new HashSet<Integer>();
    Set<Integer> adjacentLayerCys2ndPrio = new HashSet<Integer>();
    while (ec.ok()) {
      Edge e = (Edge) ec.current();
      Node connectedNode = e.opposite(n);
      EdgeRealizer r = abstractGraph.getRealizer(e);
      if (r instanceof DomainEdgeRealizer) {
        if (layers.get(connectedNode) != null) {
          adjacentLayer.add(layers.getInt(connectedNode));
        }

      } else if (!(r instanceof CysBridgeEdgeRealizer)) { // Don't consider Cystein Brdidges!
        // Most likely GeneralConnectionEdgeRealizer
        if (layers.get(connectedNode) != null) {
          adjacentLayerCys2ndPrio.add(layers.getInt(connectedNode));
        }
      }
      ec.next();
    }

    // Consider only backbone edges if available. Else, consider non-cys.
    return (adjacentLayer.size() > 0) ? adjacentLayer : adjacentLayerCys2ndPrio;
  }

  /**
   * @param n
   * @return
   */
  private static Object getAdjacentSwimlane(Node n, Graph2D abstractGraph) {
    // Get the vertical layers (y-coordinate) in which nodes are arranged
    NodeMap swimlaneMap =
        (NodeMap) abstractGraph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);

    // This node needs to get a layer info => Inspect adjacent nodes
    EdgeCursor ec = n.edges();
    Object adjacentSwimlandID = null;
    Object adjacentSwimlandID2ndPrio = null;
    while (ec.ok()) {
      Edge e = (Edge) ec.current();
      Node connectedNode = e.opposite(n);
      EdgeRealizer r = abstractGraph.getRealizer(e);
      if (r instanceof DomainEdgeRealizer) {
        adjacentSwimlandID = swimlaneMap.get(connectedNode);

      } else if (!(r instanceof CysBridgeEdgeRealizer)) { // Don't consider Cystein Brdidges!
        // Most likely GeneralConnectionEdgeRealizer
        adjacentSwimlandID2ndPrio = swimlaneMap.get(connectedNode);
      }
      ec.next();
    }

    return adjacentSwimlandID != null ? adjacentSwimlandID : adjacentSwimlandID2ndPrio;
  }

}
