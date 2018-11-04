package org.jping.report;

import org.jping.data.HttpResult;
import org.jping.data.Report;
import org.jping.data.ReportResult;
import org.jping.utils.HttpSend;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ReportingTest {

  @Mock
  HttpSend httpSend;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void givenHttpSend_whenAddError_thenReportHasError() throws IOException {
    // Pre-test
    String url = "url";
    HttpResult httpResult = HttpResult.builder()
      .code(400)
      .message("message")
      .build();

    // Given
    given(httpSend.send(any(), eq(url), eq("POST"), eq("application/json"), eq("utf-8"))).willReturn(httpResult);

    // When
    Reporting reporting = new Reporting("url", httpSend);
    reporting.addError(
      Report.builder()
        .host("host")
        .line("line")
        .type("type")
        .build()
    );
    ReportResult reportResult = reporting.report();

    // Then
    assertThat(reportResult.getErrors().size(), is(1));
    assertThat(reportResult.getLatest().entrySet().size(), is(1));
    assertThat(reportResult.getMessages().size(), is(0));
    verify(httpSend, times(1)).send(any(),any(),any(),any(),any());
  }

  @Test
  public void givenHttpSend_whenAddMessage_thenReportHasMessage() throws IOException {
    // Pre-test
    String url = "url";
    HttpResult httpResult = HttpResult.builder()
      .code(400)
      .message("message")
      .build();

    // When
    Reporting reporting = new Reporting("url", httpSend);
    reporting.addMessage(
      Report.builder()
        .host("host")
        .line("line")
        .type("type")
        .build()
    );
    ReportResult reportResult = reporting.report();

    // Then
    assertThat(reportResult.getErrors().size(), is(0));
    assertThat(reportResult.getLatest().entrySet().size(), is(1));
    assertThat(reportResult.getMessages().size(), is(1));
    verify(httpSend, times(0)).send(any(),any(),any(),any(),any());
  }
}
