package org.jping.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class FileUtilsTest {

  @Test (expected = IOException.class)
  public void openInvalidFileThrows () throws IOException {

    FileUtils.readPropertiesFromFile("invalid");

    Assert.fail();
  }

  @Test (expected = RuntimeException.class)
  public void openInvalidFile () {

    FileUtils.readProperties.apply("invalid");

    Assert.fail();
  }

  @Test
  public void givenProperties_checkValidListOfPrefixes () {
    Properties properties = FileUtils.readProperties.apply("/application.properties");

    List<String> invalidList = FileUtils.listFromPrefix(properties, "invalid");
    Assert.assertEquals(0,invalidList.size() );

    List<String> validList = FileUtils.listFromPrefix(properties, "app.host.");
    Assert.assertEquals(3,validList.size() );
    Assert.assertTrue(validList.contains("google.com"));
    Assert.assertTrue(validList.contains("jasmin.com"));
    Assert.assertTrue(validList.contains("oranum.com"));

  }
}
