package org.jping.report;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.HttpResult;
import org.jping.data.Report;
import org.jping.data.ReportResult;
import org.jping.utils.HttpSend;

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
  private HttpSend httpSend;

  private volatile List<Report> messages = Collections.synchronizedList(new ArrayList<>());

  private volatile List<Report> errors = Collections.synchronizedList(new ArrayList<>());

  private volatile Map<String, Report> latest = Collections.synchronizedMap(new HashMap<>());

  public Reporting(String url, HttpSend httpSend) {
    this.url = url;
    this.httpSend = httpSend;
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

  public ReportResult report() {
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

   return ReportResult.builder()
      .errors(Collections.synchronizedList(errors))
      .messages(Collections.synchronizedList(messages))
      .latest(latest.entrySet().stream()
        .map(Map.Entry::getValue)
        .collect(groupingBy(Report::getHost, toSet())))
      .build();

  }

  private void postString(String value) {
    log.debug("Sending report: {}", value);
    try {
      HttpResult httpResult = httpSend.send(value, url, "POST", "application/json", "utf-8");
      log.info(String.format("report post code=%s", httpResult.getCode()));
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
