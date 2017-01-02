/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.api.stream.bytes;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.mule.runtime.internal.stream.bytes.FileStoreTraversableBuffer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class FileStoreTraversableBufferTestCase extends AbstractByteStreamingTestCase {

  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        {"With Prefetched data", true},
        {"Without Prefetched Data", false}
    });
  }

  private final int bufferSize = KB_256;

  private FileStoreTraversableBuffer buffer;
  private byte[] prefetchedData = null;

  public FileStoreTraversableBufferTestCase(String name, boolean prefetchData) {
    super(MB_2);
    InputStream stream;

    if (prefetchData) {
      prefetchedData = data.substring(0, bufferSize).getBytes();
      assertThat(prefetchedData.length, is(bufferSize));
      // simulate stream advanced due to prefetching
      stream = new ByteArrayInputStream(data.substring(bufferSize).getBytes());
    } else {
      stream = new ByteArrayInputStream(data.getBytes());
    }

    buffer = new FileStoreTraversableBuffer(stream, prefetchedData, bufferSize);
  }

  @Test
  public void getSliceOfCurrentBufferSegment() throws Exception {
    final int position = bufferSize / 4;
    int len = (bufferSize / 2) - position;
    byte[] dest = new byte[len];

    assertThat(buffer.get(dest, position, len, 0), is(len));
    assertThat(toString(dest), equalTo(data.substring(position, position + len)));
  }

  @Test
  public void getSliceWhichStartsInCurrentSegmentButEndsInTheNext() throws Exception {
    final int position = bufferSize - 10;
    final int len = bufferSize / 2;
    byte[] dest = new byte[len];

    assertThat(buffer.get(dest, position, len, 0), is(len));
    assertThat(toString(dest), equalTo(data.substring(position, position + len)));
  }
}
