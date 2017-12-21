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
package org.roche.antibody.ui.abstractgraph.view;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.roche.antibody.model.antibody.ChemElement;

import chemaxon.marvin.beans.MSketchPane;
import chemaxon.marvin.beans.MViewPane;

/**
 * 
 * {@code DomainAddDialog}: Dialog for showing a HELMNotation
 * 
 * @author <b>Stefan Klostermann:</b> Stefan DOT Klostermann AT roche DOT com,
 *         Roche Pharma Research and Early Development - Informatics, Roche
 *         Innovation Center Munich
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro
 *         research GmbH
 * 
 * @version $Id: ShowHELMDialog.java 15173 2015-03-05 16:18:13Z schirmb $
 */
public class ShowMoleculeDialog extends JDialog implements ActionListener {

	JTable domainTable;

	private JButton btnOK;

	private ChemElement chemDomain;

	private MSketchPane msketchPane;

	private MViewPane mviewPane;

	/** Generated UID */
	private static final long serialVersionUID = 6102405755459856212L;

	public ShowMoleculeDialog(JFrame owner, Dialog.ModalityType isModal,
			ChemElement chemDomain) {
		super(owner, isModal);

		this.chemDomain = chemDomain;

		setLocationRelativeTo(null);

		initComponents();

	}

	/**
	 * UI Initialization
	 * 
	 * @throws FileNotFoundException
	 */
	private void initComponents() {
		setResizable(true);
		setTitle("Molecule Viewer");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(400, 400));
		getContentPane().setLayout(
				new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(createViewerPanel());
		// getContentPane().add(buildButtonPanel());

		
		setLocationRelativeTo(null);
		pack();
	}

	private JPanel createViewerPanel() {
		msketchPane = new MSketchPane();
		mviewPane = new MViewPane();
		mviewPane.setEnabled(true);
		mviewPane.setDetachable(false);
		mviewPane.setM(0, chemDomain.getSmiles());

		msketchPane.setMol(mviewPane.getM(0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(mviewPane, BorderLayout.CENTER);

		return panel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnOK) {
			this.dispose();
		}

	}
}
