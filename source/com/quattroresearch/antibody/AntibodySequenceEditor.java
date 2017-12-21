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
package com.quattroresearch.antibody;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.dnd.DropTarget;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.FilenameUtils;
import org.roche.antibody.services.UIService;
import org.roche.antibody.ui.components.AntibodyEditorAccess;
import org.roche.antibody.ui.components.AntibodyEditorPane;
import org.roche.antibody.ui.filechooser.FileUtils;
import org.roche.antibody.ui.resources.ResourceProvider;

import com.quattroresearch.antibody.plugin.PluginLoader;

/**
 * Content Panel of "Antibody Sequence Editor" <p> Displays arbitrary number of chains to be searched against DB.
 * Accepts drag&drop File-Input.
 * 
 * @author <b>Anne Mund</b>, quattro research GmbH
 */
public class AntibodySequenceEditor extends JPanel {

  /** Generated UID */
  private static final long serialVersionUID = -7323309152562400371L;

  private static final String HELP_TEXT = "Load files by OpenFileDialog or Drag&Drop: ";

  private static AntibodySequenceEditor _instance;

  // declaration of variables
  private List<String> chainNames;

  private List<String> chainSequences;

  private Map<Integer, List<Component>> chainComponents;

  private AntibodyDragDropListener dragDropListener;

  private ImageIcon deleteButtonIcon = new ImageIcon(ResourceProvider.getInstance().get("delete.png"));

  private AntibodySequenceEditor() {
    initDragDrop();
    initComponents();
  }

  public static AntibodySequenceEditor getInstance() {
    if (_instance == null) {
      _instance = new AntibodySequenceEditor();
    }
    return _instance;
  }

  private void initComponents() {
    this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    chainNames = new ArrayList<String>();
    chainSequences = new ArrayList<String>();
    chainComponents = new Hashtable<Integer, List<Component>>();
    placeChainField(-1, HELP_TEXT, "");

  }

  private void initDragDrop() {
    dragDropListener = new AntibodyDragDropListener(this);
    new DropTarget(this, dragDropListener);
  }

  public List<String> getChains() {
    return chainSequences;
  }

  public List<String> getNames() {
    return chainNames;
  }

  public void setChains(List<String> namelist, List<String> sequencelist) {
    for (int i = 0; i < namelist.size(); i++) {
      placeChainField(i, namelist.get(i), sequencelist.get(i));
    }
  }

  public void clearAll() {
    AntibodyEditorPane pane = AntibodyEditorAccess.getInstance().getAntibodyEditorPane();
    if (pane != null) {
      AntibodyEditorAccess.getInstance().getAntibodyEditorPane().setModel(null);
    }
    
    this.removeAll();
    initComponents();
  }

  /**
   * Adds name and sequence of chain to the content Panel. <p> Method is called for every new chain added, sequence is
   * put in a JTextArea inside a JScrollPane.
   * 
   * @param name Name of chain; for gp-Files filename
   * @param sequence Sequence of amino acids
   */
  public void placeChainField(final int sequenceNumber, String name, String sequence) {
    final JPanel thisPanel = this;
    if (chainNames.size() == 1 && chainNames.get(0).equals(HELP_TEXT)) {
      this.removeAll();
      chainNames = new ArrayList<String>();
      chainSequences = new ArrayList<String>();
      chainComponents = new Hashtable<Integer, List<Component>>();
    }

    // remember name + sequence
    chainNames.add(name);
    chainSequences.add(sequence);
    final int number = chainSequences.size() - 1;
    chainComponents.put(number, new ArrayList<Component>());

    JPanel titlePanel = new JPanel();
    titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));
    titlePanel.setAlignmentX(LEFT_ALIGNMENT);
    // display name
    JLabel label = new JLabel(name);
    titlePanel.add(label);
    // delete button
    if (number >= 0) {
      JButton delButton = new JButton(new AbstractAction() {
        private static final long serialVersionUID = 1L;

        @Override
        public void actionPerformed(ActionEvent e) {
          List<String> names = chainNames;
          List<String> sequences = chainSequences;
          names.remove(number);
          sequences.remove(number);
          thisPanel.removeAll();
          initComponents();

          for (int i = 0; i < names.size(); i++) {
            placeChainField(i, names.get(i), sequences.get(i));
          }
          thisPanel.validate();
        }
      });
      delButton.setIcon(deleteButtonIcon);
      delButton.setBorder(BorderFactory.createEmptyBorder());

      titlePanel.add(Box.createHorizontalGlue());
      titlePanel.add(delButton);
      titlePanel.add(Box.createRigidArea(new Dimension(20, 0)));
    }
    this.add(titlePanel);
    chainComponents.get(number).add(titlePanel);

    // sequence
    JTextArea area = new JTextArea(3, 30);
    area.setText(sequence);
    area.setEditable(false);
    area.setLineWrap(true);
    JScrollPane scroll = new JScrollPane(area);
    this.add(scroll);
    chainComponents.get(number).add(scroll);
    this.validate();

    // enable drag&drop in TextArea
    new DropTarget(area, dragDropListener);
  }

  /**
   * Takes files and gives them to the appropriate parser. Opens Message Dialog on wrong file extension.
   * 
   * @param file .txt, .gp or .fa File to parse
   * @return (List of chainnames, List of chainsequences)
   * @throws IllegalArgumentException thrown when file type is unknown
   * @throws IOException
   * @throws HeadlessException
   */
  public List<String>[] readFiles(List<File> files) throws IllegalArgumentException, HeadlessException, IOException {
    ISequenceFileReader readerPlugin = PluginLoader.getInstance().loadSequenceFileReaderPlugin();
    TreeMap<String, String> inputs = getFilesContent(files);
    if (readerPlugin != null) {
      return readerPlugin.read(UIService.getInstance().getMainFrame(), inputs);

    } else {
      return new SequenceFileReader().read(UIService.getInstance().getMainFrame(), inputs);
    }

  }

  private TreeMap<String, String> getFilesContent(List<File> files) throws IOException {
    TreeMap<String, String> inputs = new TreeMap<String, String>();
    for(File file: files){
      inputs.put(FilenameUtils.removeExtension(file.getName()), FileUtils.getFileContent(file));
    }

    return inputs;
  }
}
