/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.internal.stream.bytes;

import org.mule.runtime.api.stream.bytes.CursorStream;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * A {@link CursorStream} which pulls its data from a {@link TraversableBuffer}
 *
 * @see TraversableBuffer
 * @since 1.0
 */
public final class BufferedCursorStream extends CursorStream {

  private final TraversableBuffer buffer;
  private final Consumer<CursorStream> closeCallback;
  private boolean closed = false;

  private long position = 0;

  /**
   * Creates a new instance
   * @param buffer the buffer which provides data
   * @param closeCallback A callback to be invoked when the {@link #close()} method is called
   */
  public BufferedCursorStream(TraversableBuffer buffer, Consumer<CursorStream> closeCallback) {
    this.buffer = buffer;
    this.closeCallback = closeCallback;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getPosition() {
    return position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void seek(long position) throws IOException {
    assertNotClosed();
    this.position = position;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read() throws IOException {
    assertNotClosed();

    byte[] data = new byte[1];
    int read = buffer.get(data, position++, 1, 0);
    return read == -1 ? -1 : unsigned((int) data[0]);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    assertNotClosed();

    int read = buffer.get(b, position, len, off);
    if (read > 0) {
      position += read;
    }
    return read;
  }

  /**
   * Closes this stream and invokes the closing callback received in the constructor.
   */
  @Override
  public void close() throws IOException {
    if (!closed) {
      closed = true;
      closeCallback.accept(this);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isClosed() {
    return closed;
  }

  private int unsigned(int value) {
    return value & 0xff;
  }

  private void assertNotClosed() {
    if (closed) {
      throw new IllegalStateException("Stream is closed");
    }
  }
}
