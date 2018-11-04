package org.jping.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.report.Reporting;
import org.jping.utils.ShellUtils;
import org.jping.utils.TimingUtils;

import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class TraceCheckStrategy implements NetworkCheckStrategy {

  private ExecutorService executor;
  private Long howLong;
  private long timeout;
  private long wait;

  public TraceCheckStrategy(ExecutorService executor, Long howLong, long timeout, long wait) {
    this.executor = executor;
    this.howLong = howLong;
    this.timeout = timeout;
    this.wait = wait;
  }

  @Override
  public Future<CommandResult> check(String host, Reporting reporting) {
    log.info("trace check to host: {}", host);
    return executor.submit(() -> {
      try {
        checkHost(host, reporting);
        return CommandResult.builder()
          .success(true)
          .build();
      } catch (Exception e) {
        return CommandResult.builder()
          .success(false)
          .result(e.getLocalizedMessage())
          .build();
      }
    });
  }

  private void checkHost (String host, Reporting reporting) throws InterruptedException {
    Instant start = Instant.now();
    Instant now = Instant.now();
    boolean passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    while (!passedMax) {
      String[] args = new String[]{"traceroute", host};
      ShellUtils.executeCommand(executor, args, reporting, timeout, host, "trace", true);
      Thread.sleep(wait);
      now = Instant.now();
      passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    }
  }
}
