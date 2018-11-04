package org.jping.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.report.Reporting;
import org.jping.utils.ShellCommands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class PingCheckStrategy implements NetworkCheckStrategy {
  private ExecutorService executor;
  private Long howLong;
  private ShellCommands shellCommands;

  public PingCheckStrategy(ExecutorService executor,Long howLong, ShellCommands shellCommands) {
    this.executor = executor;
    this.howLong = howLong;
    this.shellCommands = shellCommands;
  }

  @Override
  public Future<CommandResult> check(String host, Reporting reporting) {
    log.info("ping check to host: {}", host);

    String[] args = new String[]{"ping", "-n", host};
    return executor.submit(() -> shellCommands.executeCommand(args, reporting, howLong, host, "icmp_ping"));
  }
}
