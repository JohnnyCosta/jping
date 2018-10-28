package org.jping.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class FileUtils {

  private FileUtils() {
  }

  public static ThrowingFunction<String, Properties> readProperties = FileUtils::readPropertiesFromFile;

  private static Properties readPropertiesFromFile(String fileName) throws IOException {
    InputStream is = FileUtils.class.getResourceAsStream(fileName);
    if (Objects.isNull(is)) {
      throw new IOException("Something is wrong with the file");
    }
    Properties properties = new Properties();
    properties.load(is);
    return properties;
  }


  public static List<String> listFromPrefix(Properties properties, String prefix) {
    List<String> collect = properties.entrySet()
      .stream()
      .flatMap(objectEntry -> {
        if (objectEntry.getKey().toString().startsWith(prefix)) {
          return Stream.of(objectEntry.getValue().toString());
        } else {
          return null;
        }
      })
      .collect(Collectors.toList());
    return collect;
  }
}
