package org.jping.utils;

import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class FileUtilsTest {

  @Test (expected = IOException.class)
  public void openInvalidFileThrows () throws IOException {

    FileUtils.readPropertiesFromFile("invalid");

    fail();
  }

  @Test (expected = RuntimeException.class)
  public void openInvalidFile () {

    FileUtils.readProperties.apply("invalid");

    fail();
  }

  @Test
  public void givenProperties_checkValidListOfPrefixes () {
    Properties properties = FileUtils.readProperties.apply("/application.properties");

    List<String> invalidList = FileUtils.listFromPrefix(properties, "invalid");
    assertEquals(0,invalidList.size() );

    List<String> validList = FileUtils.listFromPrefix(properties, "app.host.");
    assertEquals(3,validList.size() );
    assertTrue(validList.contains("google.com"));
    assertTrue(validList.contains("jasmin.com"));
    assertTrue(validList.contains("oranum.com"));

  }
}
