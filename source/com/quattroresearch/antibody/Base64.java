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

package com.quattroresearch.antibody;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base64 
 * @author <b>Marco Erdmann:</b> erdmann AT quattro-research DOT com, quattro research GmbH
 * @version $Id$
 */
public class Base64 {

  /** The Logger for this class */
  @SuppressWarnings("unused")
  private static final Logger LOG = LoggerFactory.getLogger(Base64.class);

  /**
   * Encodes an input {@code String} to Base64 with Apache Commons-Codec.
   * 
   * @param input the {@code String} to encode, {@code null} is treated as empty.
   * @return the encoded {@code String}.
   */
  public static String encode(String input) {
    if (StringUtils.isEmpty(input)) {
      return "";
    }

    return new String(org.apache.commons.codec.binary.Base64.encodeBase64(input.getBytes()));
  }

  /**
   * Decodes an input {@code String} from Base64 with Apache Commons-Codec.
   * 
   * @param input the {@code String} to decode, {@code null} is treated as empty.
   * @return the decoded {@code String}.
   */
  public static String decode(String input) {
    if (StringUtils.isEmpty(input)) {
      return "";
    }

    return new String(org.apache.commons.codec.binary.Base64.decodeBase64(input.getBytes()));
  }
}
