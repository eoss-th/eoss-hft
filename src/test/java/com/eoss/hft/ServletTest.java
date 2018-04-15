package com.eoss.hft;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.eoss.hft.servlet.TriangularArbitrageServlet;

public class ServletTest {

  @Test
  public void test() throws IOException {
    MockHttpServletResponse response = new MockHttpServletResponse();
    new TriangularArbitrageServlet().doGet(null, response);
    Assert.assertEquals("text/plain", response.getContentType());
    Assert.assertEquals("UTF-8", response.getCharacterEncoding());
    Assert.assertEquals("Hello App Engine!\r\n", response.getWriterContent().toString());
  }
}
