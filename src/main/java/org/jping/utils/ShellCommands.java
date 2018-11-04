package org.jping.utils;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.data.Report;
import org.jping.report.Reporting;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class ShellCommands {

  private ExecutorService executor;

  public ShellCommands(ExecutorService executor) {
    this.executor = executor;
  }

  public CommandResult executeCommand(String[] args, Reporting reporting, Long howLong,
                                             String host, String type) {
    return executeCommand(args, reporting, howLong, host, type, false);
  }

  public CommandResult executeCommand(String[] args, Reporting reporting, Long howLong,
                                             String host, String type, boolean redirectErrorStream) {
    log.info("Sending command: {}", Arrays.asList(args));
    try {
      sendCommandToShell(executor, args, reporting, howLong, host, type, redirectErrorStream);
      return CommandResult.builder()
        .success(true)
        .build();
    } catch (Exception e) {
      return CommandResult.builder()
        .success(false)
        .result(e.getLocalizedMessage())
        .build();
    }
  }

  private static void sendCommandToShell(ExecutorService executor, String[] args, Reporting reporting,
                                         Long howLong, String host, String type, boolean redirectErrorStream)
    throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(args);
    builder.redirectErrorStream(redirectErrorStream);
    Process process = builder.start();
    Instant start = Instant.now();
    Future<?> futureExec = processInputStream(executor, process, reporting, host, type);
    Future<?> futureErrorExec = processErrorStream(executor, process, reporting, host, type);
    Instant now = Instant.now();

    boolean passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    while (process.isAlive() && !passedMax) {
      Thread.sleep(300);
      now = Instant.now();
      passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    }

    if (process.isAlive() && passedMax) {
      log.info("force stop of process: {}", Arrays.asList(args).toString());
      futureExec.cancel(true);
      futureErrorExec.cancel(true);
      process.destroy();
    } else {
      Future<?>[] futures = new Future[]{futureExec, futureErrorExec};
      waitFutures.apply(futures);
    }
  }


  public static ThrowingFunction<Future<?>[], Optional<Void>> waitFutures = ShellCommands::waitFutureThrowing;


  private static Optional<Void> waitFutureThrowing(Future<?>[] futures) throws InterruptedException {
    while (notDone(futures)) {
      Thread.sleep(1000);
    }
    return Optional.empty();
  }

  private static boolean notDone(Future<?>[] futures) {
    for (Future<?> future : futures) {
      if (!future.isDone()) {
        return true;
      }
    }
    return false;
  }

  private static Future<?> processErrorStream(ExecutorService executor, Process process, Reporting reporting,
                                              String host, String type) {
    return executor.submit(() -> {
      Scanner errorScanner = new Scanner(process.getErrorStream());
      while (errorScanner.hasNext()) {
        String line = errorScanner.nextLine();
        reporting.addError(Report.builder()
          .dateTime(LocalDateTime.now())
          .host(host)
          .line(line)
          .type(type)
          .build());
      }
    });
  }

  private static Future<?> processInputStream(ExecutorService executor, Process process, Reporting reporting,
                                              String host, String type) {
    return executor.submit(() -> {
      Scanner scanner = new Scanner(process.getInputStream());
      while (scanner.hasNext()) {
        String line = scanner.nextLine();
        reporting.addMessage(Report.builder()
          .dateTime(LocalDateTime.now())
          .host(host)
          .line(line)
          .type(type)
          .build());
      }
    });
  }
}
