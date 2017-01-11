/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.runtime.api.stream.bytes.demo.sw;

import org.mule.runtime.api.stream.bytes.CursorStream;
import org.mule.runtime.api.stream.bytes.TraversableStream;

import java.io.InputStream;

import org.apache.commons.net.telnet.TelnetClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class StarWarsDemo {

  private static final int BUFFER_SIZE = 1024 * 3;
  private TelnetClient telnetClient = new TelnetClient();

  private Printer nullPrinter = read -> {
  };

  private Printer consolePrinter = c -> System.out.print(c);

  @Before
  public void before() throws Exception {
    telnetClient.connect("towel.blinkenlights.nl", 23);
  }

  @After
  public void after() throws Exception {
    if (telnetClient.isConnected()) {
      telnetClient.disconnect();
    }
  }

  @Test
  public void aNewHope() throws Exception {
    InputStream in = telnetClient.getInputStream();
    new MovieShowing(in, consolePrinter).showMovie();
  }

  @Test
  public void inYourFaceAmericanAirlines() throws Exception {
    TraversableStream in = new TraversableStream(telnetClient.getInputStream(), BUFFER_SIZE);
    MovieShowing lastShowing = null;

    for (;;) {
      CursorStream cursor = in.openCursor();
      MovieShowing movie = new MovieShowing(cursor, consolePrinter);
      if (lastShowing != null) {
        lastShowing.setPrinter(nullPrinter);
        //cursor.seek(BUFFER_SIZE * 14);
      }

      lastShowing = movie;

      new Thread(movie::showMovie).start();
      Thread.sleep(60000);
    }
  }


  private class MovieShowing {

    private final InputStream inputStream;
    private Printer printer;

    public MovieShowing(InputStream inputStream, Printer printer) {
      this.inputStream = inputStream;
      this.printer = printer;
    }

    private void showMovie() {
      try {
        int read;
        do {
          byte[] buffer = new byte[BUFFER_SIZE];
          read = inputStream.read(buffer);
          for (byte b : buffer) {
            printer.print((char) b);
          }
          //read = inputStream.read();
          //printer.print((char) read);
        } while (read != -1);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    public void setPrinter(Printer printer) {
      this.printer = printer;
    }
  }


  @FunctionalInterface
  private interface Printer {

    void print(char c);
  }
}
