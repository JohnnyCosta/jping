package org.jping;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.CommandResult;
import org.jping.data.Report;
import org.jping.utils.TimingUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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

  public HttpCheckStrategy(ExecutorService executor, Long howLong, int timeout, long wait) {
    this.executor = executor;
    this.howLong = howLong;
    this.timeout = timeout;
    this.wait = wait;
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

  private void checkHost (String host, Reporting reporting) throws InterruptedException {
    Instant start = Instant.now();
    Instant now = Instant.now();
    boolean passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    while (!passedMax) {
      sendRequest(host, timeout, reporting);
      Thread.sleep(wait);
      now = Instant.now();
      passedMax = TimingUtils.passedMaxTime(howLong, start, now);
    }
  }

  private void sendRequest(String host, int timeout, Reporting reporting) {
    URL url;
    HttpURLConnection connection;
    Instant start = null;
    Instant end;

    int responseCode;
//    String responseBody;
    long responseTime;
    try {
      url = new URL("http://" + host);
      connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("GET");
      connection.setRequestProperty("Content-Type", "text/plain");
      connection.setRequestProperty("charset", "utf-8");
      connection.setConnectTimeout(timeout);

      start = Instant.now();
      connection.connect();
      end = Instant.now();

      responseCode = connection.getResponseCode();
//      responseBody = connection.getResponseMessage();
      responseTime = Duration.between(start, end).toMillis();

      reporting.addMessage(Report.builder()
        .type("http")
        .line(String.format("http code=%s time=%d ms", responseCode, responseTime))
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
      reporting.addMessage(Report.builder()
        .type("http")
        .line(String.format("error='%s' time=%d ms", e.getLocalizedMessage(), responseTime))
        .host(host)
        .dateTime(LocalDateTime.now())
        .build());
    }
  }
}
