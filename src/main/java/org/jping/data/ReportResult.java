package org.jping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResult {

  private List<Report> messages;

  private List<Report> errors;

  private Map<String, Set<Report>> latest;

}
