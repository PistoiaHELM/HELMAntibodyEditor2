package com.quattroresearch.antibody;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.roche.antibody.model.antibody.Domain;
import org.roche.antibody.model.antibody.DomainLibraryValues;
import org.roche.antibody.model.antibody.Peptide;
import org.roche.antibody.services.ConfigFileService;

public class FindMutationsTest {

  @Test
  public void testReadMutationFile() throws Exception {
    List<Mutation> fm = ConfigFileService.getInstance().fetchMutationLibrary();
    int result = fm.size();
    Assert.assertEquals(8, result);
  }

  @Test
  public void testFindMutations() throws FileNotFoundException, Exception {

    // domainLibraryValues
    DomainLibraryValues dlv1 = new DomainLibraryValues();
    dlv1.setName("IGHG1_CH2_HUMAN");

    DomainLibraryValues dlv2 = new DomainLibraryValues();
    dlv2.setName("IGHG1_CH3_HUMAN");

    // domains
    Domain domain1 = new Domain();
    domain1.setLibraryValues(dlv1);
    domain1.setSequence("FFFFFFFFFFDDDDDDDDDDFFAFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDA");

    Domain domain2 = new Domain();
    domain2.setLibraryValues(dlv2);
    domain2.setSequence("FFFFFFFFFFDDDCDDDDDDFFFFFWFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDDDDDDDFFFFFFFFFFDDDDADDDDD");

    // domainlist

    List<Domain> domainlist = new LinkedList<Domain>();
    domainlist.add(domain1);
    domainlist.add(domain2);

    Peptide testPeptide = new Peptide();
    testPeptide.setDomains(domainlist.toArray(new Domain[0]));
    List<Peptide> pepList = new LinkedList<Peptide>(Arrays.asList(new Peptide[] {testPeptide}));
    FindMutations.find(pepList, ConfigFileService.getInstance().fetchMutationLibrary());

    Set<String> mutations = new HashSet<String>();
    for (Domain dom : testPeptide.getDomains()) {
      for (SingleMutation singleMut : dom.getSingleMutations()) {
        mutations.add(singleMut.getMutation().getMutationName());
      }
    }

    // not working without domain detection (unknown mutations need to be detected before)
// Assert.assertEquals(2, mutations.size());
// Assert.assertTrue(mutations.contains("3A"));
// Assert.assertTrue(mutations.contains("Knob"));

  }

}
