package org.jping;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.report.Reporting;
import org.jping.strategy.HttpCheckStrategy;
import org.jping.strategy.NetworkCheckStrategy;
import org.jping.strategy.PingCheckStrategy;
import org.jping.strategy.TraceCheckStrategy;
import org.jping.utils.HttpSend;
import org.jping.utils.ShellCommands;
import org.jping.utils.ThrowingFunction;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.jping.utils.FileUtils.listFromPrefix;
import static org.jping.utils.FileUtils.readProperties;
import static org.jping.utils.ShellCommands.waitFutures;


@Slf4j
public class App {

  private static volatile Reporting reporting;

  public static void main(String[] args) {
    Properties properties = readProperties.apply("/application.properties");

    List<String> addresses = listFromPrefix(properties, "app.host.");
    log.info("{}", addresses);

    HttpSend httpSend = new HttpSend(new Integer(properties.getProperty("reporting.timeout")));

    reporting = new Reporting(properties.getProperty("reporting.url"), httpSend);

    ExecutorService executor = Executors.newFixedThreadPool(30);

    ShellCommands shellCommands = new ShellCommands(executor);

    addresses.parallelStream()
      .forEach(addr -> {
        NetworkCheckStrategy pingCheck = new PingCheckStrategy(executor, 5000L, shellCommands);
        Future<CommandResult> futurePingCheck = pingCheck.check(addr, reporting);

        NetworkCheckStrategy httpCheck = new HttpCheckStrategy(executor, 5000L, 1000, httpSend);
        Future<CommandResult> futureHttpCheck = httpCheck.check(addr, reporting);

        NetworkCheckStrategy traceCheck = new TraceCheckStrategy(executor, 5000L, shellCommands);
        Future<CommandResult> futureTraceCheck = traceCheck.check(addr, reporting);

        Future<?>[] futures = new Future[]{futurePingCheck, futureHttpCheck, futureTraceCheck};
        waitFutures.apply(futures);

        log.debug("ping check result: {}", futureGet.apply(futurePingCheck));
        log.debug("http check result: {}", futureGet.apply(futureHttpCheck));
        log.debug("trace check result: {}", futureGet.apply(futureTraceCheck));
      });

    reporting.report();

    executor.shutdown();
  }

  private static ThrowingFunction<Future<CommandResult>, CommandResult> futureGet = App::futureGetThrowing;

  private static CommandResult futureGetThrowing(Future<CommandResult> future) throws ExecutionException, InterruptedException {
    return future.get();
  }
}
