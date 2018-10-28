package org.jping;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.Report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toSet;

@Slf4j
public class Reporting {

  private volatile List<Report> messages = Collections.synchronizedList(new ArrayList<>());

  private volatile List<Report> errors = Collections.synchronizedList(new ArrayList<>());

  private volatile Map<String, Report> latest = Collections.synchronizedMap(new HashMap<>());

  public synchronized void addMessage(Report report) {
    log.debug("add message: {}", report);
    latest.put(report.getType() + "-" + report.getHost(), report);
    messages.add(report);
  }

  public synchronized void addError(Report report) {
    log.debug("add error: {}", report);
    latest.put(report.getType() + "-" + report.getHost(), report);
    errors.add(report);
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

    log.info("Latests: {}", mapToString(latest));

  }

  private static String mapToString(Map<String, Report> map) {
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
