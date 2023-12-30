package org.hw_http;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final List<String> validPaths;
    private final ExecutorService threadPool;

    public Server(List<String> validPaths) {
        this.validPaths = validPaths;
        this.threadPool = Executors.newFixedThreadPool(64);
    }

    public void start(int port) {
        try (final var serverSocket = new ServerSocket(port)) {
            while (true) {
                handleConnection(serverSocket.accept());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleConnection(Socket socket) {
        threadPool.execute(() -> {
            try (
                    final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    final var out = new BufferedOutputStream(socket.getOutputStream());
            ) {
                final var requestLine = in.readLine();
                final var parts = requestLine.split(" ");

                if (parts.length != 3) {
                    return;
                }

                final var path = parts[1];
                if (!isValidPath(path)) {
                    sendNotFoundResponse(out);
                    return;
                }

                final var queryParams = Request.parseQueryParams(path);
                final var request = new Request(path, queryParams);
//                System.out.println(path);
//                System.out.println(queryParams);

                if (path.equals("/classic.html")) {
                    sendClassicHtmlResponse(out, Path.of(".", "public", path), request);
                } else {
                    sendFileResponse(out, Path.of(".", "public", path), Files.probeContentType(Path.of(".", "public", path)), request);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isValidPath(String path) {
        return validPaths.contains(Request.extractPathWithoutQuery(path));
    }

    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        out.write((
                "HTTP/1.1 404 Not Found\r\n" +
                        "Content-Length: 0\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.flush();
    }

    private void sendClassicHtmlResponse(BufferedOutputStream out, Path filePath, Request request) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: " + content.length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        out.write(content);
        out.flush();
    }

    private void sendFileResponse(BufferedOutputStream out, Path filePath, String mimeType, Request request) throws IOException {
        final var length = Files.size(filePath);
        out.write((
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + mimeType + "\r\n" +
                        "Content-Length: " + length + "\r\n" +
                        "Connection: close\r\n" +
                        "\r\n"
        ).getBytes());
        Files.copy(filePath, out);
        out.flush();
    }
}
