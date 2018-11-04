package org.jping.strategy;

import org.jping.data.CommandResult;
import org.jping.report.Reporting;

import java.util.concurrent.Future;

public interface NetworkCheckStrategy {
  Future<CommandResult> check(String host, Reporting reporting);
}
