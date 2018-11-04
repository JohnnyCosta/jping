package org.jping.utils;

import lombok.extern.slf4j.Slf4j;
import org.jping.data.HttpResult;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

@Slf4j
public class HttpSend {

  private int timeout;

  public HttpSend(int timeout) {
    this.timeout = timeout;
  }

  public HttpResult send(String url, String method, String type, String encoding) throws IOException {
    return send(null,url,method,type,encoding);
  }

  public HttpResult send(String message, String url, String method, String type, String encoding) throws IOException {
    URL postUrl = new URL(url);
    HttpURLConnection connection = (HttpURLConnection) postUrl.openConnection();
    connection.setDoOutput(true);
    connection.setInstanceFollowRedirects(false);
    connection.setRequestMethod(method);
    connection.setRequestProperty("Content-Type", type);
    connection.setRequestProperty("charset", encoding);
    connection.setConnectTimeout(timeout);

    if (Objects.nonNull(message)){
      OutputStream os = connection.getOutputStream();
      BufferedWriter writer = new BufferedWriter(
        new OutputStreamWriter(os, encoding));
      writer.write(message);
    }

    connection.connect();

    return HttpResult.builder()
      .code(connection.getResponseCode())
      .message(connection.getResponseMessage())
      .build();
  }
}
