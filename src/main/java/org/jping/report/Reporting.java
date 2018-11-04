package org.jping.report;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.Report;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class Reporting {

  private String url;
  private int timeout;

  private volatile List<Report> messages = Collections.synchronizedList(new ArrayList<>());

  private volatile List<Report> errors = Collections.synchronizedList(new ArrayList<>());

  private volatile Map<String, Report> latest = Collections.synchronizedMap(new HashMap<>());

  public Reporting(String url, int timeout) {
    this.url = url;
    this.timeout = timeout;
  }

  public synchronized void addMessage(Report report) {
    log.debug("add message: {}", report);
    latest.put(report.getType() + "-" + report.getHost(), report);
    messages.add(report);
  }

  public synchronized void addError(Report report) {
    log.debug("add error: {}", report);
    latest.put(report.getType() + "-" + report.getHost(), report);
    errors.add(report);

    postString(mapToJson(latest, report.getHost()));
  }

  public void report() {
    log.info("Messages: ");
    messages
      .forEach(msg -> {
        log.info("{}", msg);
      });

    log.info("Errors: ");
    errors
      .forEach(error -> {
        log.error("{}", error);
      });

    log.info("Latests: {}", mapToJson(latest));

    postString(mapToJson(latest, "google.com"));

  }

  private void postString(String value) {
    log.debug("Sending report: {}", value);
    try {
      URL postUrl = new URL(url);
      HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
      connection.setDoOutput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "text/plain");
      connection.setRequestProperty("charset", "utf-8");
      connection.setConnectTimeout(timeout);

      connection.connect();
      int responseCode = connection.getResponseCode();

      log.info(String.format("report post code=%s", responseCode));
    } catch (Exception e) {
      log.error("Error to post report", e);
    }
  }

  private String mapToJson(Map<String, Report> map, String host) {

    Map<String, Set<Report>> collect = map.entrySet()
      .stream()
      .filter(entry -> host.equals(entry.getValue().getHost()))
      .map(Map.Entry::getValue)
      .collect(groupingBy(Report::getHost, toSet()));

    return "{'host':'" + host + "'," + collect.get(host).stream()
      .map(report -> "'" + report.getType() + "':'" + report.getLine() + "'")
      .collect(Collectors.joining(",")) + "}";

  }

  private String mapToJson(Map<String, Report> map) {
    return "[" + map.entrySet()
      .stream()
      .map(Map.Entry::getValue)
      .collect(groupingBy(Report::getHost, toSet()))
      .entrySet()
      .stream()
      .map(entry -> "{'host':'" + entry.getKey() + "'," + entry.getValue()
        .stream()
        .map(report -> "'" + report.getType() + "':'" + report.getLine() + "'")
        .collect(Collectors.joining(",")) + "}")
      .collect(Collectors.joining(","))
      + "]";
  }
}
