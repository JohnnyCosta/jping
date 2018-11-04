package org.jping.strategy;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.data.HttpResult;
import org.jping.data.Report;
import org.jping.report.Reporting;
import org.jping.utils.HttpSend;
import org.jping.utils.TimingUtils;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@Slf4j
public class HttpCheckStrategy implements NetworkCheckStrategy {

  private ExecutorService executor;
  private Long howLong;
  private int timeout;
  private long wait;
  private HttpSend httpSend;

  public HttpCheckStrategy(ExecutorService executor, Long howLong, long wait, HttpSend httpSend) {
    this.executor = executor;
    this.howLong = howLong;
    this.wait = wait;
    this.httpSend = httpSend;
  }

  @Override
  public Future<CommandResult> check(String host, Reporting reporting) {
    log.info("http check to host: {}", host);
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

  private void checkHost(String host, Reporting reporting) throws InterruptedException {
    Instant start = Instant.now();
    Instant now = Instant.now();
    boolean passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    while (!passedMax) {
      sendRequest(host, reporting);
      Thread.sleep(wait);
      now = Instant.now();
      passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    }
  }

  private void sendRequest(String host, Reporting reporting) {
    Instant start = null;
    Instant end;
    long responseTime;

    try {
      start = Instant.now();
      HttpResult httpResult = httpSend.send("http://" + host, "GET", "text/plain", "utf-8");
      end = Instant.now();
      responseTime = Duration.between(start, end).toMillis();

      reporting.addMessage(Report.builder()
        .type("tcp_ping")
        .line(String.format("http code=%s time=%d ms", httpResult.getCode(), responseTime))
        .host(host)
        .dateTime(LocalDateTime.now())
        .build());
    } catch (IOException e) {
      if (Objects.nonNull(start)) {
        end = Instant.now();
        responseTime = Duration.between(start, end).toMillis();
      } else {
        responseTime = 0;
      }
      reporting.addError(Report.builder()
        .type("http")
        .line(String.format("error='%s' time=%d ms", e.getLocalizedMessage(), responseTime))
        .host(host)
        .dateTime(LocalDateTime.now())
        .build());
    }
  }
}
