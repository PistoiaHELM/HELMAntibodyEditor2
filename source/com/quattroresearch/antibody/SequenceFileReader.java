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

import java.awt.HeadlessException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.roche.antibody.ui.filechooser.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * {@code SequenceFileReader} is the default implementation for {@code ISequenceFileReader}, which reads in protein
 * FASTA and GP files.
 * 
 * @author <b>Marco Lanig:</b> lanig AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class SequenceFileReader implements ISequenceFileReader {

  /** The Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory
      .getLogger(SequenceFileReader.class);

  private JFrame parentFrame;

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String>[] read(JFrame parentFrame, TreeMap<String, String> files)
      throws IllegalArgumentException, HeadlessException, IOException {
    this.parentFrame = parentFrame;

    List<String>[] result = new List[2];
    result[0] = new ArrayList<String>();
    result[1] = new ArrayList<String>();
    List<String>[] innerResult;
    for (Map.Entry<String, String> file : files.entrySet()) {
      if (FileUtils.isFastaFile(file.getValue())) {
        innerResult = readFastaFileContent(file.getValue());
      } else if (FileUtils.isVNTProteinFile(file.getValue())) {
        innerResult = readGPFileContent(file.getKey(), file.getValue());
      } else {
        String exceptionText =
            "The content of the file is not a fasta nor a VNT protein format. Please use only these file formats.";
        JOptionPane.showMessageDialog(parentFrame, exceptionText);
        throw new IllegalArgumentException(exceptionText);
      }
      /* add each file result to the output result */
      result[0].addAll(innerResult[0]);
      result[1].addAll(innerResult[1]);
    }
    return result;

  }

  /**
   * Parses Fasta content. Sequence can be upper- or lower-case letters, name is everything from ">".
   * 
   * @param file .fa-File to parse
   * @return (List of chainnames, List of chainsequences)
   */
  private List<String>[] readFastaFileContent(String fileAsString) {
    List<String> foundNames = new ArrayList<String>();
    List<String> foundChains = new ArrayList<String>();
    int current = -1;
    BufferedReader br = null;

    try {
      br = new BufferedReader(new StringReader(fileAsString));
      String line;
      Pattern p = Pattern.compile("\\d+");
      Matcher matcher;
      while ((line = br.readLine()) != null) {
        String lineClean = line.trim();
        if (!lineClean.isEmpty()) {
          if (lineClean.startsWith(">")) {
            foundNames.add(lineClean.split(">")[1]);
            foundChains.add("");
            current++;
          } else {
            matcher = p.matcher(lineClean);
            if (matcher.find()) {
              throw new Exception(
                  "Could not read fasta. Sequence contains number (Line " + (current + 1) + ")");
            }
            foundChains.set(current, foundChains.get(current)
                + lineClean);
          }
        }
      }
    } catch (IOException e) {
      JOptionPane.showMessageDialog(parentFrame,
          "Could not read fasta. (Line " + (current + 1) + ")");
      e.printStackTrace();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(parentFrame, e.getMessage());
      e.printStackTrace();
    } finally {

      if (br != null) {
        try {
          br.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }

    List<String>[] result = new List[2];
    result[0] = foundNames;
    result[1] = foundChains;

    return result;
  }

  /**
   * Parses .gp Files <p> Parses all chains found in a gp-formatted file (usually one). Sequence can be upper- or
   * lower-case letters, chainname is the filename.
   * 
   * @param file .gp-File to parse
   * @return (List of chainnames, List of chainsequences)
   */
  private List<String>[] readGPFileContent(String fileName, String fileContent) {
    List<String> foundNames = new ArrayList<String>();
    List<String> foundChains = new ArrayList<String>();
    try {
      BufferedReader br = new BufferedReader(
          new StringReader(fileContent));
      String strLine;
      StringBuilder buildChain = new StringBuilder();
      Pattern startofSequence = Pattern.compile("^ORIGIN.*");
      Pattern dataline = Pattern
          .compile("^(\\s*[A-Z]{2,}\\s{3,})|(//).*");
      Pattern chain = Pattern.compile("^\\s*\\d+\\s+([A-Za-z ]+)$");
      boolean readSequence = false;
      Matcher matcher;

      // extract all Chains from file
      while ((strLine = br.readLine()) != null) {
        matcher = startofSequence.matcher(strLine);
        if (matcher.matches()) {
          readSequence = true;
        }

        if (readSequence) {
          matcher = chain.matcher(strLine.replaceAll("\\*", ""));
          if (matcher.matches()) {
            buildChain.append(matcher.group(1).replaceAll(" ", ""));
          }

          matcher = dataline.matcher(strLine);
          if (matcher.matches()) {
            foundChains.add(buildChain.toString().toUpperCase());
            buildChain = new StringBuilder();
            readSequence = false;
          }
        }
      }

      if (foundChains.size() == 1) {
        foundNames.add(fileName);
      } else {
        for (int i = 0; i < foundChains.size(); i++) {
          foundNames.add(fileName + String.valueOf(i));
        }
      }

    } catch (IOException x) {
      System.err.format("IOException: %s%n", x);
    }

    List<String>[] result = new List[2];
    result[0] = foundNames;
    result[1] = foundChains;

    return result;
  }

}
