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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.helm.notation.model.Attachment;
import org.helm.notation.model.Monomer;
import org.helm.notation.tools.StructureParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import chemaxon.formats.MolImporter;
import chemaxon.marvin.plugin.PluginException;
import chemaxon.struc.MolAtom;
import chemaxon.struc.MolBond;
import chemaxon.struc.Molecule;

/**
 * 
 * {@code MonomerUtils} Contains methods needed for monomer registration.
 * 
 * @author <b>Jutta Fichtner:</b> fichtner AT quattro-research DOT com
 * @version $Id$
 */
public class MonomerUtils {

	/** The Logger for this class */
	private static final Logger LOG = LoggerFactory
			.getLogger(MonomerUtils.class);

	private static Map<String, Integer> molAtomMap = new HashMap<String, Integer>() {

		private static final long serialVersionUID = 1L;

		{
			put("H", 1);
			put("He", 2);
			put("Li", 3);
			put("Be", 4);
			put("B", 5);
			put("C", 6);
			put("N", 7);
			put("O", 8);
			put("F", 9);
			put("Ne", 10);
			put("Na", 11);
			put("Mg", 12);
			put("Al", 13);
			put("Si", 14);
			put("P", 15);
			put("S", 16);
			put("Cl", 17);
			put("Ar", 18);
		}
	};

	private static final Pattern ELEMENT_SYMBOL_PATTERN = Pattern
			.compile("[A-Z]{1}[a-z]?");

	private static final Pattern R_GROUP_PATTERN = Pattern
			.compile("R([0-9]{1})");

	private static final String R_GROUP = "R#";

	private static final String RGP_ATTACHMENT = "M  RGP";

	private static final String SUPER_ATOM_ATTACHMENT = "M  STY";

	/**
	 * Takes the given {@code Monomer} and converts it into a {@code Molecule}
	 * to retrieve the molfile for this monomer without r-groups and retrieves
	 * the {@code Monomer} with the new molfile. The r-groups will be replaced
	 * with the leaving groups (e.g. OH, H, ...).
	 * 
	 * @param m
	 *            the monomer
	 * @return monomer with molfile without r-groups
	 * @throws Exception
	 */
	public static Monomer removeRGroupsfromMolfile(Monomer m) throws Exception {
		String molfile = m.getMolfile();

		Molecule molecule = null;
		InputStream is = null;
		try {
			is = new ByteArrayInputStream(molfile.getBytes());
			MolImporter importer = new MolImporter(is);
			molecule = importer.read();
			molecule.clean(1, "clean2d:full"); // required as SMILES has no
												// coordinates
			molecule.dearomatize(); // required as Accelrys Direct does not like
									// aromatic bonds
		} finally {
			is.close();
		}

		// save all bonds
		MolBond[] bonds = molecule.getBondArray();

		Map<Integer, String> leavingGroups = extractLeavingGroups(m);

		for (int i = 0; i < molecule.getNodeCount(); i++) {
			LOG.debug(((MolAtom) molecule.getNode(i)).getSymbol());
			if (((MolAtom) molecule.getNode(i)).getSymbol().equalsIgnoreCase(
					R_GROUP)) {
				LOG.debug(((MolAtom) molecule.getNode(i)).getSymbol() + "");
				MolAtom oldMolAtom = (MolAtom) molecule.getNode(i);
				LOG.debug("R-Group: " + oldMolAtom.getRgroup());
				MolAtom newMolAtom = new MolAtom(molAtomMap.get(leavingGroups
						.get(oldMolAtom.getRgroup())), oldMolAtom.getX(),
						oldMolAtom.getY(), oldMolAtom.getZ());

				// Adapt bonds array to newMolAtom
				for (int j = 0; j < bonds.length; j++) {
					if (bonds[j].getAtom1().equals(oldMolAtom)) {
						bonds[j] = new MolBond(newMolAtom, bonds[j].getAtom2(),
								bonds[j].getFlags());
					}
					if (bonds[j].getAtom2().equals(oldMolAtom)) {
						bonds[j] = new MolBond(bonds[j].getAtom1(), newMolAtom,
								bonds[j].getFlags());
					}
				}
				molecule.setNode(i, newMolAtom);
				LOG.debug(((MolAtom) molecule.getNode(i)).getSymbol() + "");
			}
		}
		// remove all old bonds and add the new ones
		molecule.removeAllEdges();
		for (int i = 0; i < bonds.length; i++) {
			molecule.add(bonds[i]);
		}

		m.setMolfile(new String(molecule.toBinFormat("mol")));
		LOG.debug(m.getMolfile());
		return m;
	}

	/**
	 * Returns a map containing the r-groups with their mol formula.
	 * 
	 * @param m
	 *            the monomer
	 * @return {@code Map<Integer, String>} key is the r_group and value the
	 *         r-group mol formula
	 * @throws IOException
	 * @throws PluginException
	 */
	public static Map<Integer, String> extractLeavingGroups(Monomer m)
			throws Exception {
		Map<Integer, String> leavingGroups = new HashMap<Integer, String>();
		if (m.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT) != null
				&& m.getAttachment(Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT)
						.getCapGroupName() != null) {
			leavingGroups.put(
					1,
					m.getAttachment(
							Attachment.BACKBONE_MONOMER_LEFT_ATTACHEMENT)
							.getCapGroupName());
		}
		if (m.getAttachment(Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT) != null
				&& m.getAttachment(
						Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT)
						.getCapGroupName() != null) {
			leavingGroups.put(
					2,
					m.getAttachment(
							Attachment.BACKBONE_MONOMER_RIGHT_ATTACHEMENT)
							.getCapGroupName());
		}
		if (m.getAttachment(Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT) != null
				&& m.getAttachment(
						Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)
						.getCapGroupName() != null) {
			leavingGroups.put(
					3,
					m.getAttachment(
							Attachment.BACKBONE_MONOMER_BRANCH_ATTACHEMENT)
							.getCapGroupName());
		}

		for (Integer i : leavingGroups.keySet()) {
			LOG.debug(i + " : " + leavingGroups.get(i));
			if (leavingGroups.get(i).equalsIgnoreCase("HO")
					|| leavingGroups.get(i).equalsIgnoreCase("OH")) {
				leavingGroups.put(i, "O");
				LOG.debug(i + " : " + leavingGroups.get(i));
			} else if (!(ELEMENT_SYMBOL_PATTERN.matcher(leavingGroups.get(i))
					.matches())) {
				throw new Exception(
						"The following leaving group "
								+ leavingGroups.get(i)
								+ " can not yet be resolved to a element symbol. Please contact your administrator");
			}
		}

		return leavingGroups;
	}

	/**
	 * Create an instance of {@code Molecule} from a chemical notation.
	 * 
	 * @param chemNotation
	 *            Molfile or SMILES string.
	 * @return the {@code Molecule} instance.
	 * @throws Exception 
	 */
	public static Molecule createMoleculeFromChemicalNotation(
			String chemNotation) throws Exception {
		Molecule result = null;
		InputStream is = null;

		try {
			LOG.error("Is base64 string :"
					+ org.apache.commons.codec.binary.Base64
							.isBase64(chemNotation));
			if (org.apache.commons.codec.binary.Base64.isBase64(chemNotation)) {
				chemNotation = Base64.decode(chemNotation);
			}
			is = new ByteArrayInputStream(chemNotation.getBytes());



			MolImporter importer = new MolImporter(is);
			result = importer.read();
//			result.clean(1, "clean2d:full"); // required as SMILES has no
//												// coordinates
			result.dearomatize(); // required as Accelrys Direct does not like
									// aromatic bonds
		} catch (Exception e) {
			LOG.error(e.getMessage(), e); 
			throw e;
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				is = null;
			}
		}
		return result;
	}

	/**
	 * Use MarvinBeans to create a bond between two given molecules and the
	 * specified R-Groups.
	 * 
	 * @param reactant1
	 *            the first reactant as Molfile or extended SMILES.
	 * @param reactant2
	 *            the second reactant as Molfile or extended SMILES.
	 * @param rGroup1
	 *            the R-Group in the first reactant.
	 * @param rGroup2
	 *            the R-Group in the second reactant.
	 * @return the reaction product as extended SMILES or the empty
	 *         {@code String} if an error occurred.
	 * @throws Exception 
	 */
	public static String createBondBetween(String reactant1, String reactant2,
			String rGroup1, String rGroup2) throws Exception {
		String product = "";

		Molecule rct1 = createMoleculeFromChemicalNotation(reactant1);
		Molecule rct2 = createMoleculeFromChemicalNotation(reactant2);
		try {
			List<String> rGroups1 = StructureParser
					.getRGroupsFromExtendedSmiles(StructureParser
							.getUniqueExtendedSMILES(rct1));
			List<String> rGroups2 = StructureParser
					.getRGroupsFromExtendedSmiles(StructureParser
							.getUniqueExtendedSMILES(rct2));

			MolAtom atom1 = StructureParser.getRgroupAtom(rct1,
					rGroups1.indexOf(rGroup1));
			MolAtom atom2 = StructureParser.getRgroupAtom(rct2,
					rGroups2.indexOf(rGroup2));

			StructureParser.merge(rct1, atom1, rct2, atom2);

			product = StructureParser.getUniqueExtendedSMILES(rct1);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		return product;
	}

	/**
	 * Check a molfile for R groups.
	 * 
	 * @param molfile
	 *            the molfile to check.
	 * @return {@code true} if the molfile has R groups, {@code false}
	 *         otherwise.
	 * @throws IOException
	 */
	public static boolean hasRGroup(String molfile) throws IOException {
		boolean hasRGroup = false;
		boolean hasRGPAttachment = false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(molfile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains(R_GROUP)) {
					hasRGroup = true;
				}
				if (line.startsWith(RGP_ATTACHMENT)) {
					hasRGPAttachment = true;
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return hasRGroup && hasRGPAttachment;
	}

	/**
	 * Counts the R groups contained in a molfile
	 * 
	 * @param molfile
	 *            the molfile to check.
	 * @return the number of R groups contained in a molfile
	 * @throws IOException
	 */
	public static int countRGroup(String molfile) throws IOException {
		int rGroupCount = 0;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(molfile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.contains(R_GROUP)) {
					rGroupCount++;
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return rGroupCount;
	}

	/**
	 * Check a molfile for super atoms
	 * 
	 * @param molfile
	 *            the molfile to check.
	 * @return {@code true} if the molfile has super atoms, {@code false}
	 *         otherwise.
	 * @throws IOException
	 */
	public static boolean hasSuperAtoms(String molfile) throws IOException {
		boolean hasSuperAtoms = false;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new StringReader(molfile));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (line.startsWith(SUPER_ATOM_ATTACHMENT)) {
					hasSuperAtoms = true;
				}
			}
		} finally {
			if (reader != null) {
				reader.close();
			}
		}
		return hasSuperAtoms;
	}

}
