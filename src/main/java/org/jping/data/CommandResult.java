package org.jping.data;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResult {
  private boolean success;
  private String result;

  @Override
  public String toString() {
    return "'CommandResult':{" +
      "'success':" + success +
      ", 'result':'" + result + '\'' +
      '}';
  }
}
