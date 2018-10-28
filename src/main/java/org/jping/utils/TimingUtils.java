package org.jping.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public class TimingUtils {

  private TimingUtils (){}

  public static boolean passedMaxTime(Long howLong, Instant start, Instant now) {
    boolean passedTime = false;
    if (Objects.nonNull(howLong)) {
      Long timeElapsed = Duration.between(start, now).toMillis();
      if (timeElapsed > howLong) {
        passedTime = true;
      }
    }
    return passedTime;
  }
}
