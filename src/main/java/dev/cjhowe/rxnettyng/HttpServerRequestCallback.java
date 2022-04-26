package dev.cjhowe.rxnettyng;

/** An interface for receiving HTTP requests and sending HTTP responses to those requests. */
@FunctionalInterface
public interface HttpServerRequestCallback {
  /**
   * Handles an HTTP request by sending a response.
   *
   * @param request The HTTP request received by the HttpServer
   * @param response The in-progress HTTP response, used to send a response for the request
   */
  public void handleRequest(HttpServerRequest request, HttpServerResponse response);
}
