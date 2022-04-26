package dev.cjhowe;

@FunctionalInterface
public interface HttpServerRequestCallback {
  public void handleRequest(HttpServerRequest request, HttpServerResponse response);
}
