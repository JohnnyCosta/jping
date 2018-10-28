package org.jping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Report {
  private String host;
  private String type;
  private String line;
  private LocalDateTime dateTime;

  @Override
  public String toString() {
    return "{\'Report\':{" +
      "\'host\':'" + host + '\'' +
      ", \'type\':'" + type + '\'' +
      ", \'line\':'" + line + '\'' +
      ", \'dateTime\':\'" + dateTime +
      "\'}}";
  }
}
