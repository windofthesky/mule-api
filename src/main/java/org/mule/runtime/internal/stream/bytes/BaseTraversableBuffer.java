/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.internal.stream.bytes;

import static java.lang.Math.toIntExact;
import static java.lang.System.arraycopy;

/**
 * Base class for implementations of {@link TraversableBuffer}
 *
 * @since 1.0
 */
public abstract class BaseTraversableBuffer implements TraversableBuffer {

  protected final int bufferSize;
  private boolean closed = false;

  /**
   * Creates a new instance
   *
   * @param bufferSize the buffer size
   */
  public BaseTraversableBuffer(int bufferSize) {
    this.bufferSize = bufferSize;
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if the buffer is closed
   */
  @Override
  public final int get(byte[] destination, long position, int length, int destOffset) {
    if (closed) {
      throw new IllegalStateException("Buffer is closed");
    }

    return doGet(destination, position, length, destOffset);
  }

  /**
   * Template method for getting information out of the buffer
   *
   * @param destination the array in which information is to be written
   * @param position    the position from which to start pulling information
   * @param length      the amount of bytes to copy
   * @param destOffset  the offset on which to start writing in the destination array
   * @return the amount of bytes read of {@code -1} if none could be read.
   */
  protected abstract int doGet(byte[] destination, long position, int length, int destOffset);

  /**
   * Copies part of the {@code data} array into the {@code dest} array
   *
   * @param data        the data source
   * @param dataLength  the amount of data to be copied
   * @param position    the position in the data array from which to start copying
   * @param destination the array in which to write
   * @param destOffset  the position in the destination array from which to start writing
   */
  protected void copy(byte[] data, int dataLength, long position, byte[] destination, int destOffset) {
    arraycopy(data, toIntExact(position), destination, destOffset, dataLength);
  }

  /**
   * Closes the buffer, releasing held resources
   */
  public final void close() {
    closed = true;
    doClose();
  }

  /**
   * Template method for actually closing the buffer
   */
  protected abstract void doClose();
}
