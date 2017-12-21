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

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.roche.antibody.model.antibody.Antibody;
import org.roche.antibody.model.antibody.ChemElement;
import org.roche.antibody.model.antibody.Connection;
import org.roche.antibody.model.antibody.CysteinConnection;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.DomainType;
import org.roche.antibody.model.antibody.GeneralConnection;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.model.antibody.RNA;
import org.roche.antibody.model.antibody.Sequence;
import org.roche.antibody.services.antibody.PeptideChainSorter;
import org.roche.antibody.ui.abstractgraph.AbstractGraphElementInitializer;
import org.roche.antibody.ui.abstractgraph.ChemElementRealizer;
import org.roche.antibody.ui.abstractgraph.CysBridgeEdgeRealizer;
import org.roche.antibody.ui.abstractgraph.DomainEdgeRealizer;
import org.roche.antibody.ui.abstractgraph.DomainNodeRealizer;
import org.roche.antibody.ui.abstractgraph.GeneralConnectionEdgeRealizer;
import org.roche.antibody.ui.abstractgraph.RNARealizer;
import org.roche.antibody.ui.abstractgraph.view.DomainAnnotationAction;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import y.base.DataMap;
import y.base.DataProvider;
import y.base.Edge;
import y.base.EdgeMap;
import y.base.GraphEvent;
import y.base.Node;
import y.base.NodeMap;
import y.layout.PortConstraintKeys;
import y.layout.hierarchic.GivenLayersLayerer;
import y.layout.hierarchic.IncrementalHierarchicLayouter;
import y.layout.hierarchic.incremental.IntValueHolderAdapter;
import y.util.Maps;
import y.view.EdgeRealizer;
import y.view.Graph2D;
import y.view.LineType;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.hierarchy.DefaultHierarchyGraphFactory;
import y.view.hierarchy.GenericGroupNodeRealizer;
import y.view.hierarchy.HierarchyManager;

/**
 * Contains servive methods to modify an abstract antibody graph (derived from {@link #getGraph(Antibody)}). Does and
 * should NOT contain methods, related to the layout of an abract antibody. See {@link AbstractGraphLayoutService} for
 * these purposes.
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com, Roche Pharma Research and Early
 *         Development - Informatics, Roche Innovation Center Munich
 * @author <b>Stefan Zilch:</b> stefan_dieter DOT zilch AT contractors DOT roche DOT com
 * @author <b>Clemens Wrzodek:</b> clemens DOT wrzodek AT roche DOT com
 * @author <b>Sabrina Hecht:</b> hecht AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class AbstractGraphService {

  private static AbstractGraphService instance = null;

  private static final int LEFT = 1;

  /** The Logger for this class */
  private static final Logger LOG = LoggerFactory.getLogger(AbstractGraphService.class);

  private static final int RIGHT = 2;

  private static final int SECOND_DOMAIN = 1;

  public synchronized static AbstractGraphService getInstance() {
    if (instance == null) {
      instance = new AbstractGraphService();
    }
    return instance;
  }

  public AbstractGraphService() {
    // Actually not required, since all methods are static and require the graph as input.
  }

  /**
   * Remove a {@link Connection} from the graph
   * 
   * @param graph
   * @param con
   */
  public static void removeConnection(Graph2D graph, Connection con) {
    Edge edgeToRemove = findeEdgeByConnection(graph, con);
    if (edgeToRemove != null) {
      graph.removeEdge(edgeToRemove);
    }
  }

  /**
   * Remove a node from the graph.
   * 
   * @param graph
   * @param seq
   */
  public static void removeSequence(Graph2D graph, Sequence seq) {
    Node node = findNodeBySequence(graph, seq);
    if (node != null) {
      graph.removeNode(node);
    }
  }

  /**
   * Add a connection to the graph
   * 
   * @param graph
   * @param connection
   */
  public static void addConnection(Graph2D graph, Connection connection) {
    Node source = null;
    Node target = null;
    EdgeRealizer realizer = null;
    if (connection instanceof CysteinConnection) {
      CysteinConnection cysCon = (CysteinConnection) connection;
      source = findNodeBySequence(graph, cysCon.getSource().getDomain(cysCon.getSourcePosition()));
      target = findNodeBySequence(graph, cysCon.getTarget().getDomain(cysCon.getTargetPosition()));
      realizer = new CysBridgeEdgeRealizer();

    } else if (connection instanceof GeneralConnection) {
      GeneralConnection genConn = (GeneralConnection) connection;
      Sequence realSource = genConn.getSource();
      Sequence realTarget = genConn.getTarget();
      if (realSource instanceof Peptide) {
        realSource = ((Peptide) realSource).getDomain(genConn.getSourcePosition());
      }
      if (realTarget instanceof Peptide) {
        realTarget = ((Peptide) realTarget).getDomain(genConn.getTargetPosition());
      }

      source = findNodeBySequence(graph, realSource);
      target = findNodeBySequence(graph, realTarget);
      realizer = new GeneralConnectionEdgeRealizer();
    }

    if (source != null && target != null) {
      Edge newEdge = graph.createEdge(source, target, realizer);
      EdgeMap edgeMap = (EdgeMap) graph.getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
      edgeMap.set(newEdge, connection);

     // if (connection instanceof GeneralConnection) {
     //   EdgeMap criticalPathMap = (EdgeMap) graph.getDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);
     //   Double criticalWeight = criticalPathMap.getDouble(source);
     //   criticalPathMap.setDouble(newEdge, criticalWeight);
       
     //   String restPeptide = connection.getSourceRest();
     //   NodeMap layerIdMap = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
     //   if (restPeptide.equals(HELM.R1)) {
     //       layerIdMap.setInt(target, layerIdMap.getInt(source) - 1);
     //     }
     //     else if (restPeptide.equals(HELM.R2)) {
     //       layerIdMap.setInt(target, layerIdMap.getInt(source) + 1);
     //     }
     //     else {
     //       layerIdMap.setInt(target, layerIdMap.getInt(source));
     //     }

      // }
    }
    else {
      LOG.debug("Source or target is null");
    }
  }

  /**
   * @param graph
   * @param sequence could be a {@link ChemElement} or a @link {@link Domain}
   */
  public static Node addSequence(Graph2D graph, Sequence sequence) {
    AntibodyEditorPane abePane = AntibodyEditorAccess.getInstance().getAntibodyEditorPane();
    if (abePane != null) {
      abePane.updatePane(null);
    }

    Node newNode = null;
    if (sequence instanceof ChemElement) {
      newNode = addChemElement(graph, (ChemElement) sequence);
    } else if (sequence instanceof Domain) {
      newNode = addDomain(graph, (Domain) sequence);
    } else if (sequence instanceof RNA) {
      newNode = addRNAElement(graph, (RNA) sequence);
    } else {
      LOG.warn("Cannot add unknown sequence type.");
    }

    // graph.updateViews(); // Connections are eventually missing. This will mis-arrange the graph here.
    return newNode;
  }

  /**
   * Calls {@link #getGraph(Antibody)} and subsequently {@link AbstractGraphLayoutService#layout(Graph2D)}.
   * 
   * @param ab our {@link Antibody} data structure
   * @return layouted {@link Graph2D} view of the antibody {@code ab}.
   */
  public static Graph2D getLayoutedGraph(Antibody ab) {
    // Sort heavy/light chains (LHHL instead of HHLL)
    ab = new PeptideChainSorter().remodel(ab);

    // Create "Nodes and edges" + Shape information
    Graph2D graph = getGraph(ab);

    // Setup some layout hints
    AbstractGraphLayoutService.updateLayoutHints(graph);

    // Actual layout of the graph
    AbstractGraphLayoutService.layout(graph);

    return graph;
  }

  /**
   * Builds the basic Abstract Graph of an antibody. Does just setup the graph! No layout of nodes or other required
   * styling actions are performed here. <p> Use {@link AbstractGraphLayoutService#layout(Graph2D)} to layout the
   * returned graph (if desired). Or {@link #getLayoutedGraph(Antibody)} to do both in one run.
   * 
   * @param ab our {@link Antibody} data structure
   * @return basic yFiles {@link Graph2D} with unlayouted nodes and edges.
   */
  public static Graph2D getGraph(Antibody ab) {
    Graph2D graph = new Graph2D();
    graph.getDefaultEdgeRealizer().setLineColor(Color.RED);
    NodeMap sequenceMap = graph.createNodeMap();
    EdgeMap edge2CriticalValue = Maps.createHashedEdgeMap();
    EdgeMap connectionMap = graph.createEdgeMap();
    NodeMap layerIdMap = graph.createNodeMap();
    NodeMap sequenceIdMap = graph.createNodeMap();
    NodeMap swimLaneIdMap = graph.createNodeMap();
    EdgeMap sourcePortMap = graph.createEdgeMap();
    EdgeMap targetPortMap = graph.createEdgeMap();
    DataMap hintMap = Maps.createHashedDataMap();
    NodeMap deletableNodeMap = graph.createNodeMap();

    // register them with the graph
    graph.addDataProvider(PortConstraintKeys.SOURCE_PORT_CONSTRAINT_KEY, sourcePortMap);
    graph.addDataProvider(PortConstraintKeys.TARGET_PORT_CONSTRAINT_KEY, targetPortMap);
    graph.addDataProvider(GivenLayersLayerer.LAYER_ID_KEY, layerIdMap);
    graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, hintMap);
    graph.addDataProvider(IncrementalHierarchicLayouter.SEQUENCE_VALUE_HOLDER_DPKEY, sequenceIdMap);
    graph.addDataProvider(IncrementalHierarchicLayouter.LAYER_VALUE_HOLDER_DPKEY, new IntValueHolderAdapter(layerIdMap));
    graph.addDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY, edge2CriticalValue);
    graph.addDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY, swimLaneIdMap); // all Nodes with

    // same label come in same lane
    graph.addDataProvider(AbConst.EDGE_TO_CONNECTION_KEY, connectionMap);
    graph.addDataProvider(AbConst.NODE_DELETABLE_KEY, deletableNodeMap);
    graph.addDataProvider(AbConst.NODE_TO_SEQUENCE_KEY, sequenceMap);

    /* Grouping */
    graph.setHierarchyManager(createHierarchyManager(graph));

    HashMap<Sequence, Node> sequenceToNode = new HashMap<Sequence, Node>();
    initPeptideSequences(ab, sequenceToNode, graph);
    initAdditionalSequences(ab, graph);
    initConnections(graph, ab, sequenceToNode, connectionMap);
    alignNonDomainSequences(graph, ab, layerIdMap);

    // Perform an initial setup of graph objects
    updateNodeAndEdgeStyles(graph);


    LOG.debug(String.format("NEW GRAPH OBJECT CREATED (Nodes: %s, Edges: %s).", graph.getNodeArray().length, graph.getEdgeArray().length));
    return graph;
  }

  /**
   * Builds the HierarchyManager of this graph. The HierarchyManager is there to create a group node for each peptide
   * Here you can define the shape of the group label, the color ect...
   * 
   * @param graph
   * @return HierarchyManager
   */
  private static HierarchyManager createHierarchyManager(Graph2D graph) {
    HierarchyManager hierarchyManager = new HierarchyManager(graph);
    DefaultHierarchyGraphFactory factory = (DefaultHierarchyGraphFactory) hierarchyManager.getGraphFactory();
    GenericGroupNodeRealizer gnr = new GenericGroupNodeRealizer();
    gnr.setFillColor(new Color(255, 255, 255));
    gnr.setLineType(LineType.DOTTED_1);
    gnr.setLineColor(new Color(255, 255, 255));

    // These are the labels above each chain/sequence (A-Z or Chain name)
    gnr.getLabel().setTextColor(Color.GRAY);
    gnr.getLabel().setFontSize(10);
    gnr.getLabel().setFontStyle(Font.BOLD);
    gnr.getLabel().setLineColor(Color.GRAY);
    gnr.getLabel().setModel(NodeLabel.SIDES);
    gnr.getLabel().setPosition(NodeLabel.N);
    gnr.getLabel().setAlignment(NodeLabel.ALIGN_CENTER);
    gnr.getLabel().setAutoSizePolicy(NodeLabel.AUTOSIZE_NODE_WIDTH); // <- Span the box full width
    gnr.getLabel().setHorizontalTextPosition(NodeLabel.CENTER_TEXT_POSITION);

    gnr.setVisible(true);

    factory.setProxyNodeRealizerEnabled(true);

    factory.setDefaultGroupNodeRealizer(gnr.createCopy());

    return hierarchyManager;
  }


  /**
   * Vertically aligns non domain sequences (chem/rna) according to connected domain nodes. It searches in a fixed depth
   * and considers the first hit. The sequence is then aligned on layer-1. You can also add the list of sequences
   * yourself, to align only selected ones.
   * 
   * @param graph
   * @param ab
   * @param layerIdMap
   * @param sequencesToAlign or null, when all.
   */
  public static void alignNonDomainSequences(Graph2D graph, Antibody ab, NodeMap layerIdMap) {
    List<Sequence> sequences = new LinkedList<Sequence>();

    sequences.addAll(ab.getChemElements());
    sequences.addAll(ab.getRnaElements());

    List<NodeAlignmentRecord> additionalSequencesToAlign = new LinkedList<NodeAlignmentRecord>();
    for (Sequence seq : sequences) {
      Node nodetoAlign = findNodeBySequence(graph, seq);
      if (nodetoAlign != null) {
        NodeAlignmentRecord foundDomainNode = findOneDirectlyConnectedDomainNode(graph, nodetoAlign, seq, 5);
        if (foundDomainNode != null) {
          additionalSequencesToAlign.add(foundDomainNode);

        }
      }
    }

    Collections.sort(additionalSequencesToAlign);
    for (NodeAlignmentRecord rec : additionalSequencesToAlign) {
      graph.moveToLast(rec.getNodeToAlign());
      // Node[] array = graph.getNodeArray();
      // Node source = rec.getNodeToAlign();
      // Node target = rec.getNearestDomainNode();
      // int position = 0;
      // for (int i = 0; i < array.length; i++) {
      // if (array[i] == target) {
      // position = i;
      // break;
      // }
      // }

      // graph.moveToFirst(array[array.length - 1]);
      // for (int j = position; j > 0; j--) {
      // graph.moveToFirst(array[j]);
      // }

      // for (int j = position; j < array.length - 1; j++) {
      // graph.moveToLast(array[j]);
      // }

      // graph.moveToFirst(rec.getNodeToAlign());


    }

    for (NodeAlignmentRecord rec : additionalSequencesToAlign) {
      // TODO other side
      if (rec.getNearestDomain().getDomainIndex() >= rec.getNearestDomain().getPeptide().getDomains().size() - 1) {
        layerIdMap.setInt(rec.getNodeToAlign(), layerIdMap.getInt(rec.getNearestDomainNode()) + rec.getDistance());
      } else {
        layerIdMap.setInt(rec.getNodeToAlign(), layerIdMap.getInt(rec.getNearestDomainNode()) - rec.getDistance());
      }
    }
  }

  /**
   * Searches a domain node that is connected to the given sequence, but only to the given depth. <br /><br />
   * 
   * e.g.:<br /> CHEM1-->RNA1-->CHEM2-->Domain1: Until a maximum depth of 3, we will find Domain1 as connected domain of
   * CHEM1
   * 
   * @param graph
   * @param nodeToAlign
   * @param sequenceToAlign
   * @param maxDepth
   * @param depth
   * @return
   */
  private static NodeAlignmentRecord findOneDirectlyConnectedDomainNode(Graph2D graph, Node nodeToAlign,
      Sequence sequenceToAlign, int maxDepth) {
    return findOneConnectedDomainNode(graph, nodeToAlign, sequenceToAlign, maxDepth, 1);
  }

  /**
   * 
   * This overload takes a currentDepth for recursion purposes.
   * 
   * @param graph
   * @param nodeToAlign
   * @param sequenceToAlign
   * @param maxDepth
   * @param currentDepth
   * @return
   */
  private static NodeAlignmentRecord findOneConnectedDomainNode(Graph2D graph, Node nodeToAlign,
      Sequence sequenceToAlign, int maxDepth,
      int currentDepth) {
    if (currentDepth <= maxDepth) {
      for (Connection conn : sequenceToAlign.getConnections()) {
        if (conn.getSource() instanceof Peptide && !conn.getSource().equals(sequenceToAlign)) {
          Domain dom = ((Peptide) conn.getSource()).getDomain(conn.getSourcePosition());
          return new NodeAlignmentRecord(nodeToAlign, findNodeBySequence(graph, dom), dom, currentDepth);

        } else if (conn.getTarget() instanceof Peptide && !conn.getTarget().equals(sequenceToAlign)) {
          Domain dom = ((Peptide) conn.getTarget()).getDomain(conn.getTargetPosition());
          return new NodeAlignmentRecord(nodeToAlign, findNodeBySequence(graph, dom), dom, currentDepth);
        }
      }

      // no direct domain neighbor found --> go deeper recursively
      for (Connection conn : sequenceToAlign.getConnections()) {
        if (conn.getSource().equals(sequenceToAlign)) {
          NodeAlignmentRecord nodeFoundDeeper =
              findOneConnectedDomainNode(graph, nodeToAlign, conn.getTarget(), maxDepth, currentDepth + 1);
          if (nodeFoundDeeper != null) {
            return nodeFoundDeeper;
          }
        } else if (conn.getTarget().equals(sequenceToAlign)) {
          NodeAlignmentRecord nodeFoundDeeper =
              findOneConnectedDomainNode(graph, nodeToAlign, conn.getSource(), maxDepth, currentDepth + 1);
          if (nodeFoundDeeper != null) {
            return nodeFoundDeeper;
          }
        }
      }

    }
    return null;
  }

  /**
   * Perform an initial setup of graph objects (colors, styles, shapes and lines).
   * 
   * @param graph
   */
  public static void updateNodeAndEdgeStyles(Graph2D graph) {
    // TODO: Move business logic for style of elements (shape, color, etc.) out of the realizers!!!
    for (Node n : graph.getNodeArray()) {
      NodeRealizer r = graph.getRealizer(n);
      if (r instanceof AbstractGraphElementInitializer) {
        ((AbstractGraphElementInitializer) r).initFromMap();
      }
    }
    for (Edge e : graph.getEdgeArray()) {
      EdgeRealizer r = graph.getRealizer(e);
      if (r instanceof AbstractGraphElementInitializer) {
        ((AbstractGraphElementInitializer) r).initFromMap();
      }
    }
  }

  /**
   * We delete all {@link CysteinConnection}, which are not an intraBridge. Attention: Each
   * {@link Graph2D#removeEdge(Edge)} fires an {@link GraphEvent} which is implemented in {@linkAntibodyEditorPane}. The
   * implementation removes the {@link CysteinConnection} from the {@link Antibody} model.
   * 
   * {@link Antibody}
   * 
   * @param graph
   */
  public static void resetCysteinBridges(Graph2D graph) {
    DataProvider edgeToBridge = graph.getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
    ConnectionService cs = ConnectionService.getInstance();
    Connection con;
    for (Edge curEdge : graph.getEdgeArray()) {
      con = (Connection) edgeToBridge.get(curEdge);
      if (edgeToBridge.get(curEdge) != null && con instanceof CysteinConnection) {
        CysteinConnection cysCon = (CysteinConnection) con;
        if (cs.isIntraDomainConnection(cysCon) && !cysCon.isHingeConnected()) {
          graph.removeEdge(curEdge);
        }
      }
    }
  }

  /**
   * If we add a new Domain, which was created on an existing peptide chain, we calculate the position (start or end of
   * chain) and add the domain. If the domain is also a new peptide, we only add it to the model.
   * 
   * @param graph
   * @param domain
   */
  private static Node addDomain(Graph2D graph, Domain domain) {
    if (domain == null) {
      return null;
    }
    NodeMap map = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap deleteable = (NodeMap) graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
    Node node = findNodeBySequence(graph, domain);
    if (node != null) {
      // Already exists
      return node;
    }

    // Create the actual new node
    node = graph.createNode(new DomainNodeRealizer(domain));
    graph.setLabelText(node, graph.getRealizer(node).getLabel().getText());
    setupCommonLabelConfiguration(graph.getRealizer(node).getLabel());
    LOG.debug("New Domain added to Graph: {}", domain.getName());
    map.set(node, domain);
    deleteable.setBool(node, true);

    // Add new domain to existing chain
    Node refNode = null;
    if (domain.getDomainIndex() == 0 && domain.getPeptide().getDomains().size() > 1) {
      EdgeMap criticalPathMap = (EdgeMap) graph.getDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);

      // Create path to node
      refNode = findNodeBySequence(graph, domain.getPeptide().getDomains().get(SECOND_DOMAIN));
      Double criticalPathValue = criticalPathMap.getDouble(refNode);
      ArrayList<Node> nodesToConnect = new ArrayList<Node>();
      nodesToConnect.add(node);
      nodesToConnect.add(refNode);
      // set layer to one before
      NodeMap layerIdMap = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
      layerIdMap.setInt(node, layerIdMap.getInt(refNode) - 1);
      createBackboneBridge(graph, nodesToConnect, criticalPathValue, criticalPathMap, domain.getPeptide());

    } else if (domain.getDomainIndex() == domain.getPeptide().getDomains().size() - 1
        && domain.getPeptide().getDomains().size() > 1) {
      EdgeMap criticalPathMap =
          (EdgeMap) graph.getDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);
      refNode = findNodeBySequence(graph, domain.getPeptide().getDomains().get(domain.getDomainIndex() - 1));
      Double criticalPathValue = criticalPathMap.getDouble(refNode);
      ArrayList<Node> nodesToConnect = new ArrayList<Node>();
      nodesToConnect.add(refNode);
      nodesToConnect.add(node);
      // set layer to one after
      NodeMap layerIdMap = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
      layerIdMap.setInt(node, layerIdMap.getInt(refNode) + 1);
      createBackboneBridge(graph, nodesToConnect, criticalPathValue, criticalPathMap, domain.getPeptide());
    }

    try {
      DomainAnnotationAction.annotateDomain(AntibodyEditorAccess.getInstance().getAntibodyEditorPane(), domain);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    return node;
  }

  /**
   * @param label
   */
  private static void setupCommonLabelConfiguration(NodeLabel label) {
    // Common settings for all nodes: center the labels.
    label.setModel(NodeLabel.INTERNAL);
    label.setPosition(NodeLabel.CENTER);
    label.setAlignment(NodeLabel.ALIGN_CENTER);
    label.setHorizontalTextPosition(NodeLabel.CENTER_TEXT_POSITION);
  }

  private static Node addRNAElement(Graph2D graph, RNA element) {
    Node node = graph.createNode(new RNARealizer());
    graph.setLabelText(node, graph.getRealizer(node).getLabel().getText());
    setupCommonLabelConfiguration(graph.getRealizer(node).getLabel());
    NodeMap map = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap deleteable = (NodeMap) graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
    map.set(node, element);
    deleteable.setBool(node, true);

    LOG.debug("New RNAElement added to Graph: {}", element);

    return node;
  }

  private static Node addChemElement(Graph2D graph, ChemElement molecule) {
    Node node = graph.createNode(new ChemElementRealizer());
    graph.setLabelText(node, graph.getRealizer(node).getLabel().getText());
    setupCommonLabelConfiguration(graph.getRealizer(node).getLabel());
    NodeMap map = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap deleteable = (NodeMap) graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
    map.set(node, molecule);
    deleteable.setBool(node, true);

    LOG.debug("New ChemElement added to Graph: {}", molecule);

    return node;
  }

  /**
   * This method aligns the light peptide sequences on the left side of the heavy peptide chains. Find the first
   * variable Domain on the heavy chain and align the first variable domain in the light chain in one level.
   * 
   * TRICK: We have to align the left and the right sides of an antibody. Hence you have to call this method twice.
   * First: call it with peptideList
   * 
   * Second: Reverse peptideList and call it again (Collections.reverse...)
   * 
   * Third: Reverse it again to get the original order.
   * 
   */
  private static void alignLightChains(List<Peptide> peptideList, HashMap<Sequence, Node> domainToNode,
      NodeMap layerIdMap, int direction, Antibody ab) {
    int leftHeavyPeptideIndex = 0; // index of heavy peptide from the left side
    int variableDomainLayerId = 0;
    int positionLightDomain = 0;
    boolean connected = false;
    if (direction == RIGHT) {
      Collections.reverse(peptideList);
    }

    for (int i = 0; i < peptideList.size(); i++) {
      if (peptideList.get(i).hasHinge()) {
        leftHeavyPeptideIndex = i;
        break;
      }
    }

    variableDomainLayerId = 0;
    LOG.debug("DIRECTION : " + direction);
    for (int peptideIndex = 0; peptideIndex < leftHeavyPeptideIndex; peptideIndex++) {
      LOG.debug("PEPTIDE: " + peptideList.get(peptideIndex).getName() + peptideIndex);
      /* align connected hinges together */
        for (Connection con : peptideList.get(peptideIndex).getConnections()) {
          if (con.getSource() != con.getTarget() && con.getSource() instanceof Peptide
              && con.getTarget() instanceof Peptide) {
            if (con.getSource() == peptideList.get(peptideIndex)) {
            if (peptideList.get(leftHeavyPeptideIndex) == con.getTarget()) {
              variableDomainLayerId =
                  layerIdMap.getInt(domainToNode.get(peptideList.get(leftHeavyPeptideIndex).getDomains().get(getPositionOfAlignedHeavyChain(peptideList.get(leftHeavyPeptideIndex), con.getTargetPosition()))));


              connected = true;
            }

              positionLightDomain =
                getPositionOfAlignedLightChain(peptideList.get(peptideIndex), con.getSourcePosition());
            } else {
            if (peptideList.get(leftHeavyPeptideIndex) == con.getSource()) {
              variableDomainLayerId =
                  layerIdMap.getInt(domainToNode.get(peptideList.get(leftHeavyPeptideIndex).getDomains().get(getPositionOfAlignedHeavyChain(peptideList.get(leftHeavyPeptideIndex), con.getSourcePosition()))));
              // layerIdMap.getInt(domainToNode.get(peptideList.get(leftHeavyPeptideIndex).getDomain(con.getSourcePosition())));
              connected = true;
            }

              positionLightDomain =
                getPositionOfAlignedLightChain(peptideList.get(peptideIndex), con.getTargetPosition());
            }
            
          if(connected){
            break;
          }
          }
        }


      /* only if they are connected to a heavy chain */
      if (connected) {
        Domain dom;
        dom = peptideList.get(peptideIndex).getDomains().get(positionLightDomain);
        Node node = domainToNode.get(dom);
        LOG.debug("DomainHeavyConnctionLayer " + variableDomainLayerId);
        int j = 0;
        for (int i = positionLightDomain; i >= 0; i--) {
          node = domainToNode.get(peptideList.get(peptideIndex).getDomains().get(i));
          int newLayerId = variableDomainLayerId - j - 1;
          layerIdMap.setInt(node, newLayerId);
          LOG.debug("" + newLayerId);
          j++;
        }

        j = 0;
        for (int i = positionLightDomain + 1; i < peptideList.get(peptideIndex).getDomains().size(); i++) {
          node = domainToNode.get(peptideList.get(peptideIndex).getDomains().get(i));
          int newLayerId = variableDomainLayerId + j;
          layerIdMap.setInt(node, newLayerId);
          LOG.debug("" + newLayerId);
          j++;
        }
      }
    }

    // back to original order
    if (direction == RIGHT) {
      Collections.reverse(peptideList);
    }
  }

  private static int getPositionOfAlignedHeavyChain(Peptide peptide, int connected) {
    /* search for the next upHinge or Hinge region to the connection */

    connected = peptide.getDomain(connected).getDomainIndex();
    for (int i = connected; i >= 0; i--) {
      if ((peptide.getDomains().get(i).isHinge() || peptide.getDomains().get(i).getDomainType().equals(DomainType.UP_HINGE))) {
        return i;
      }
    }

    for (int i = connected + 1; i < peptide.getDomains().size(); i++) {
      if ((peptide.getDomains().get(i).isHinge() || peptide.getDomains().get(i).getDomainType().equals(DomainType.UP_HINGE))) {
        return i;
      }
    }
    return connected;
  }

  private static int getPositionOfAlignedLightChain(Peptide peptide, int connected) {

    connected = peptide.getDomain(connected).getDomainIndex();
    for (int i = connected; i >= 0; i--) {
      if (!(peptide.getDomains().get(i).isHinge() || peptide.getDomains().get(i).getDomainType().equals(DomainType.UP_HINGE))) {
        return i;
      }
    }
    return connected;
  }

  /**
   * We add all domains (nodes) assigned to one sequence to a critical path map. this ensures that all domains are
   * painted in one vertical line per sequence.
   * 
   * @param graph
   * @param nodes
   * @param criticalWeight
   * @param criticalPathMap
   * @param peptide
   */
  private static void createBackboneBridge(Graph2D graph, List<Node> nodes, double criticalWeight,
      EdgeMap criticalPathMap, Peptide peptide) {
    // That method fails if nodes.size()==0!
    if (nodes == null || nodes.size() < 1) {
      return;
    }

    Node groupNode = createGroupNodeForSequence(graph, peptide);

    for (int i = 0; i < nodes.size() - 1; i++) {
      graph.getHierarchyManager().setParentNode(nodes.get(i), groupNode);
      Edge e = graph.createEdge(nodes.get(i), nodes.get(i + 1), new DomainEdgeRealizer());
      criticalPathMap.setDouble(e, criticalWeight);
    }
    graph.getHierarchyManager().setParentNode(nodes.get(nodes.size() -1), groupNode);

  }

  private static Node createGroupNodeForSequence(Graph2D graph, Peptide peptide) {
    String label = "";
    if (!peptide.getLabel().isEmpty()) {
      label = peptide.getLabel();
    }
    else if (peptide.getUid() != null) {
      label = peptide.getUid();
    } else if (!peptide.getName().isEmpty()) {
      label = peptide.getName();
    }
    
    Node outerGroup = graph.getHierarchyManager().createGroupNode(graph);
    NodeMap deletable = (NodeMap) graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
    deletable.setBool(outerGroup, true);
    

    if (label.length() > 20) {
      label = StringUtils.substring(label, 0, 20) + "(..)";
    }
    graph.setLabelText(outerGroup, label);
    return outerGroup;

  }

  /**
   * Searches the node representation in given graph that belongs to the given sequence.
   * 
   * @param graph
   * @param sequence
   * @return
   */
  public static Node findNodeBySequence(Graph2D graph, Sequence sequence) {
    DataMap nodeToSequence = (DataMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    for (Node node : graph.getNodeArray()) {
      Sequence foundSequence = (Sequence) nodeToSequence.get(node);
      if (foundSequence == sequence
          || (sequence instanceof Peptide && foundSequence instanceof Domain && ((Peptide) sequence).getDomainIndex((Domain) foundSequence) >= 0)) {
        return node;
      }
    }
    return null;
  }

  /**
   * Searches the edge representation in given graph that belongs to the gieven connection.
   * 
   * @param graph
   * @param con
   * @return
   */
  private static Edge findeEdgeByConnection(Graph2D graph, Connection con) {
    DataMap nodeToSequence = (DataMap) graph.getDataProvider(AbConst.EDGE_TO_CONNECTION_KEY);
    for (Edge edge : graph.getEdgeArray()) {
      Connection foundConnection = (Connection) nodeToSequence.get(edge);
      if (foundConnection == con) {
        return edge;
      }
    }
    return null;
  }

  /**
   * Here we add additional sequnces (rna, small molecules) to the graph
   * 
   * @param ab
   * @param graph
   */
  private static void initAdditionalSequences(Antibody ab, Graph2D graph) {
    for (Sequence seq : ab.getRnaElements()) {
      addSequence(graph, seq);
    }
    for (Sequence seq : ab.getChemElements()) {
      addSequence(graph, seq);
    }

  }

  /**
   * @param graph
   * @param ab
   * @param domainToNodeMap
   * @param connectionMap
   */
  private static void initConnections(Graph2D graph, Antibody ab, HashMap<Sequence, Node> domainToNodeMap,
      EdgeMap connectionMap) {
    for (Connection con : ab.getConnections()) {
      addConnection(graph, con);
    }

  }


  /**
   * This is the main block to build and align the peptide chains, which are loaded from external files.
   * 
   * @param ab
   * @param sequenceToNode
   * @param graph
   */
  private static void initPeptideSequences(Antibody ab, HashMap<Sequence, Node> sequenceToNode, Graph2D graph) {
    NodeMap nodeToSequence = (NodeMap) graph.getDataProvider(AbConst.NODE_TO_SEQUENCE_KEY);
    NodeMap layerIdMap = (NodeMap) graph.getDataProvider(GivenLayersLayerer.LAYER_ID_KEY);
// NodeMap swimlaneMap = (NodeMap) graph.getDataProvider(IncrementalHierarchicLayouter.SWIMLANE_DESCRIPTOR_DPKEY);
    EdgeMap edge2CriticalValue = (EdgeMap) graph.getDataProvider(IncrementalHierarchicLayouter.CRITICAL_EDGE_DPKEY);


    List<Peptide> sortedPeptides = sortPeptides(ab.getPeptides(), ab);
    for (Peptide peptide : sortedPeptides) {
      LOG.debug(peptide.getName());
    }

    ab.setPeptides(sortedPeptides);
    LOG.debug("Sort of peptides was successful");
    int criticalWeight = 1;
    List<Node> nodes = new ArrayList<Node>();
    for (Peptide peptide : ab.getPeptides()) {
      LOG.debug("PEPTIDE: " + peptide.getName());
      nodes.clear();

// SwimLaneDescriptor swimlaneUniqueIDforColumn = new SwimLaneDescriptor(peptide);
      for (Domain dom : peptide.getDomains()) {
        Node node = graph.createNode(new DomainNodeRealizer(dom));
        graph.setLabelText(node, graph.getRealizer(node).getLabel().getText());
        nodes.add(node);
        nodeToSequence.set(node, dom);
        sequenceToNode.put(dom, node);
        // ML 2016-02-04 all domains should be deletable
        NodeMap deleteable = (NodeMap) graph.getDataProvider(AbConst.NODE_DELETABLE_KEY);
        deleteable.setBool(node, true);
// swimlaneMap.set(node, swimlaneUniqueIDforColumn);
        // hintMap.set(node, hintsFactory.createSequenceIncrementallyHint(node));
      }

      AbstractGraphLayoutService.bringHingeToHorizontalLine(nodeToSequence, nodes, layerIdMap);
      createBackboneBridge(graph, nodes, criticalWeight++, edge2CriticalValue, peptide);
    }


    // shortenBackboneHeavyChains(layerIdMap, ab, sequenceToNode);
    alignLightChains(ab.getPeptides(), sequenceToNode, layerIdMap, LEFT, ab);
    alignLightChains(ab.getPeptides(), sequenceToNode, layerIdMap, RIGHT, ab);
  }

  private static List<Peptide> sortPeptides(List<Peptide> peptides, Antibody ab) {
    Map<Sequence, List<Sequence>> mapConnections = new HashMap<Sequence, List<Sequence>>();
    Map<Peptide, String> mapHeavyPeptides = new HashMap<Peptide, String>();
    Map<Peptide, String> mapAllPeptides = new HashMap<Peptide, String>();
    List<Peptide> lightChains = new ArrayList<Peptide>();
    /* init mapAllPeptides */

    for (Peptide single : peptides) {
      mapAllPeptides.put(single, "");
    }

    List<Peptide> sortedPeptides = new ArrayList<Peptide>();
    Map<Peptide, String> mapLightInserts = new HashMap<Peptide, String>();
    /*first read all Connections*/
    for(Connection c: ab.getAllConnections()){
      if (c.getSource() instanceof Peptide && c.getTarget() instanceof Peptide) {
      if(c.getSource() != c.getTarget()){
        if(!(mapConnections.containsKey(c.getSource()))){

        mapConnections.put(c.getSource(), new ArrayList<Sequence>());
      }
        if(!(mapConnections.containsKey(c.getTarget()))){
          mapConnections.put(c.getTarget(), new ArrayList<Sequence>());
        }
        mapConnections.get(c.getSource()).add(c.getTarget());
        mapConnections.get(c.getTarget()).add(c.getSource());
      }
    }
    }
    
      
    /*go over each Peptide -> sort */
    for (Peptide heavyPeptide : ab.getHeavyPeptides()) {
      mapHeavyPeptides.put(heavyPeptide, "");
    }

      /* find peptides */
      List<Peptide> heavy = ab.getHeavyPeptides();
    if (heavy.size() > 0) {
      Peptide actualHeavyChain = heavy.get(0);
      heavy.remove(0);
      Peptide nextHeavyChain = null;
    int counter = 0;
      while (actualHeavyChain != null) {
      counter++;
      lightChains = new ArrayList<Peptide>();
        mapHeavyPeptides.put(actualHeavyChain, "done");
      mapAllPeptides.remove(actualHeavyChain);
        LOG.debug("Heavy-Chain " + actualHeavyChain.getName());
        /* Get all inter connections */
        List<Sequence> interConnections = mapConnections.get(actualHeavyChain);
        if (interConnections != null) {
          LOG.trace("Size of connections: " + interConnections.size());
          for (Sequence single : interConnections) {
            LOG.trace(single.getUid());
            LOG.trace("Chain-Connection " + single.getName());
            if (mapHeavyPeptides.containsKey(single)) {
              if (mapHeavyPeptides.get(single) == ""){
                nextHeavyChain = (Peptide) single;
              mapHeavyPeptides.put((Peptide) single, "next");
              }

            } else {
              if( !mapLightInserts.containsKey(single)){
            mapLightInserts.put((Peptide) single, "done");
              mapAllPeptides.remove(single);
              lightChains.add((Peptide) single);
            }
          }
          }

        }
      LOG.trace("Add something to list " + actualHeavyChain.getName());
      if((counter % 2) == 1){
        sortedPeptides.addAll(lightChains);
        sortedPeptides.add(actualHeavyChain);
      }else{
        sortedPeptides.add(actualHeavyChain);
        sortedPeptides.addAll(lightChains);
      }

        if(nextHeavyChain != null){
          actualHeavyChain = nextHeavyChain;
        heavy.remove(actualHeavyChain);
          nextHeavyChain = null;
        }
        else {
          if (heavy.size() > 0) {
            actualHeavyChain = heavy.get(0);
            heavy.remove(0);
          }
          else {
            actualHeavyChain = null;
          }
        }
        
      }
  }



    LOG.debug("Size of heavyChains " + sortedPeptides.size());
    for (Peptide peptide : mapAllPeptides.keySet()) {
      sortedPeptides.add(peptide);
    }
    LOG.debug("Size of heavyChains " + sortedPeptides.size());
    return sortedPeptides;
  }

  /**
   * {@code NodeAlignmentRecord} contains data describing which node is connected with which peptide domain and how far
   * it is away. This class is needed to collect the data for aligning CHEM and RNA nodes with the domain nodes they are
   * connected to.
   * 
   * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com
   * @version $Id$
   */
  protected static class NodeAlignmentRecord implements Comparable<NodeAlignmentRecord> {
    private Node nearestDomainNode;

    private Domain nearestDomain;

    private Node nodeToAlign;

    private int distance;

    /**
     * Constructor using all parameters.
     * 
     * @param nodeToAlign the node that will be aligned
     * @param nearestDomainNode the node of the nearest peptide domain
     * @param nearestDomain the nearest peptide domain
     * @param distance the distance from nodeToAlign to nearestDomainNode
     */
    public NodeAlignmentRecord(Node nodeToAlign, Node nearestDomainNode, Domain nearestDomain, int distance) {
      this.nodeToAlign = nodeToAlign;
      this.nearestDomainNode = nearestDomainNode;
      this.nearestDomain = nearestDomain;
      this.distance = distance;
    }

    /**
     * Gets the node that will be aligned.
     * 
     * @return node
     */
    public Node getNodeToAlign() {
      return this.nodeToAlign;
    }

    /**
     * Gets the nearest domain node.
     * 
     * @return node
     */
    public Node getNearestDomainNode() {
      return this.nearestDomainNode;
    }

    /**
     * Gets the nearest peptide domain.
     * 
     * @return domain
     */
    public Domain getNearestDomain() {
      return this.nearestDomain;
    }

    /**
     * Gets the distance from nodeToAlign to nearestDomainNode.
     * 
     * @return
     */
    public int getDistance() {
      return this.distance;
    }

    /**
     * {@inheritDoc}
     * 
     * <br /> <br /> <b>This instance compares the {@code NodeAlignmentRecord} instances by the peptide index.</b>
     */
    @Override
    public int compareTo(NodeAlignmentRecord o) {
      Peptide thisPeptide = this.getNearestDomain().getPeptide();
      Peptide otherPeptide = o.getNearestDomain().getPeptide();
      if (thisPeptide == otherPeptide) {
        return 0;
        // compare by peptide index
      } else if (thisPeptide.getAntibody().getPeptides().indexOf(thisPeptide) < otherPeptide.getAntibody().getPeptides().indexOf(otherPeptide)) {
        return -1;
      } else
        return 1;
    }

  }

}
