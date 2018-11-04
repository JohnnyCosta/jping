package org.jping.strategy;

import org.jping.data.CommandResult;
import org.jping.data.HttpResult;
import org.jping.report.Reporting;
import org.jping.utils.HttpSend;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.jping.utils.ShellCommands.waitFutures;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class HttpCheckStrategyTest {

  @Mock
  HttpSend httpSend;

  @Mock
  Reporting reporting;

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Test
  public void givenHttp_whenHttpCheckStrategy_shouldAddMessage() throws IOException {
    // Pre-test
    String url = "url";
    HttpResult httpResult = HttpResult.builder()
      .code(400)
      .message("message")
      .build();
    ExecutorService executor = Executors.newFixedThreadPool(30);

    // Given
    given(httpSend.send(anyString(), eq("GET"), eq("text/plain"), eq("utf-8"))).willReturn(httpResult);

    // When
    HttpCheckStrategy httpCheckStrategy = new HttpCheckStrategy(executor, 1000L, 100, httpSend);
    Future<CommandResult> commandResultFuture = httpCheckStrategy.check("host", reporting);
    Future<?>[] futures = new Future[]{commandResultFuture};
    waitFutures.apply(futures);

    // Then
    verify(httpSend, atLeast(1)).send(anyString(), anyString(), anyString(), anyString());
    verify(reporting, atLeast(1)).addMessage(any());
    verify(reporting, times(0)).addError(any());

  }

  @Test
  public void givenHttpError_whenHttpCheckStrategy_shouldAddError() throws IOException {
    // Pre-test
    String url = "url";
    HttpResult httpResult = HttpResult.builder()
      .code(400)
      .message("message")
      .build();
    ExecutorService executor = Executors.newFixedThreadPool(30);

    // Given
    given(httpSend.send(anyString(), eq("GET"), eq("text/plain"), eq("utf-8"))).willThrow(new IOException());

    // When
    HttpCheckStrategy httpCheckStrategy = new HttpCheckStrategy(executor, 1000L, 100, httpSend);
    Future<CommandResult> commandResultFuture = httpCheckStrategy.check("host", reporting);
    Future<?>[] futures = new Future[]{commandResultFuture};
    waitFutures.apply(futures);

    // Then
    verify(httpSend, atLeast(1)).send(anyString(), anyString(), anyString(), anyString());
    verify(reporting, atLeast(1)).addError(any());
    verify(reporting, times(0)).addMessage(any());

  }
}
