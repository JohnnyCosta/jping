package org.jping;

import org.jping.data.CommandResult;

import java.util.concurrent.Future;

public interface NetworkCheckStrategy {
  Future<CommandResult> check(String host, Reporting reporting);
}
