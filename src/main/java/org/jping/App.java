package org.jping;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.utils.ThrowingFunction;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.jping.utils.FileUtils.listFromPrefix;
import static org.jping.utils.FileUtils.readProperties;
import static org.jping.utils.ShellUtils.waitFutures;


@Slf4j
public class App {

  private static volatile Reporting reporting = new Reporting();

  public static void main(String[] args) {
    Properties properties = readProperties.apply("/application.properties");

    List<String> addresses = listFromPrefix(properties, "app.host.");
    log.info("{}", addresses);

    ExecutorService executor = Executors.newFixedThreadPool(30);

    addresses.parallelStream()
      .forEach(addr -> {
        NetworkCheckStrategy pingCheck = new PingCheckStrategy(executor, 5000L);
        Future<CommandResult> futurePingCheck = pingCheck.check(addr, reporting);

        NetworkCheckStrategy httpCheck = new HttpCheckStrategy(executor, 5000L, 1000, 1000);
        Future<CommandResult> futureHttpCheck = httpCheck.check(addr, reporting);

        NetworkCheckStrategy traceCheck = new TraceCheckStrategy(executor, 5000L, 1000, 1000);
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
