package org.jping.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.report.Reporting;
import org.jping.utils.ShellCommands;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class TraceCheckStrategy implements NetworkCheckStrategy {
  private ExecutorService executor;
  private long timeout;
  private ShellCommands shellCommands;

  public TraceCheckStrategy(ExecutorService executor, long timeout, ShellCommands shellCommands) {
    this.executor = executor;
    this.timeout = timeout;
    this.shellCommands = shellCommands;
  }

  @Override
  public Future<CommandResult> check(String host, Reporting reporting) {

    String[] args = new String[]{"traceroute", host};
    return executor.submit(() -> shellCommands.executeCommand(args, reporting, timeout, host, "trace", true));

  }
}
