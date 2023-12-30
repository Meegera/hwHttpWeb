package org.hw_http;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        final var validPaths = List.of("/index.html", "/spring.svg", "/spring.png", "/resources.html", "/styles.css", "/app.js", "/links.html", "/forms.html", "/classic.html", "/events.html", "/events.js");

        Server server = new Server(validPaths);
        server.start(9999);
    }
}
