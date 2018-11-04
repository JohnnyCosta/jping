package org.jping.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.report.Reporting;
import org.jping.utils.ShellUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class PingCheckStrategy implements NetworkCheckStrategy {

  private ExecutorService executor;
  private Long howLong;

  public PingCheckStrategy(ExecutorService executor, Long howLong) {
    this.executor = executor;
    this.howLong = howLong;
  }

  @Override
  public Future<CommandResult> check(String host, Reporting reporting) {
    log.info("ping check to host: {}", host);

    String[] args = new String[]{"ping", "-n", host};
    return executor.submit(() -> ShellUtils.executeCommand(executor, args, reporting, howLong, host, "icmp_ping"));
  }
}
