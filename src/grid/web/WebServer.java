package grid.web;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import grid.exceptions.BayDockingException;
import grid.exceptions.InvalidVehicleConfigurationException;
import grid.exceptions.NoAvailableBayException;

/** Serves the dashboard from the web folder and exposes the simulation as a small JSON API. */
public class WebServer {

    private static final Map<String, String> CONTENT_TYPES = Map.of(
            ".html", "text/html; charset=utf-8",
            ".css", "text/css; charset=utf-8",
            ".js", "text/javascript; charset=utf-8");

    private final HttpServer server;
    private final SimulationService simulation;
    private final Path webRoot;

    public WebServer(int port, Path webRoot, SimulationService simulation) throws IOException {
        this.simulation = simulation;
        this.webRoot = webRoot.toAbsolutePath().normalize();

        // Bound to loopback so the simulator is only reachable from this machine.
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.createContext("/api/", this::handleApi);
        server.createContext("/", this::handleStatic);

        // The controller is not thread-safe, so every request runs on this one thread.
        server.setExecutor(Executors.newSingleThreadExecutor());
    }

    public void start() {
        server.start();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    // --- API ---

    private void handleApi(HttpExchange exchange) throws IOException {
        String route = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();

        try {
            switch (route) {
                case "GET /api/state" -> { }
                case "POST /api/vehicles" -> {
                    Map<String, String> form = readForm(exchange);
                    simulation.addVehicle(form.get("type"), form.get("plate"), form.get("charge"));
                }
                case "POST /api/step" -> simulation.step();
                case "POST /api/reset" -> simulation.reset();
                default -> {
                    sendError(exchange, 404, "Unknown API route.");
                    return;
                }
            }
            sendJson(exchange, 200, simulation.snapshot());
        } catch (InvalidVehicleConfigurationException e) {
            sendError(exchange, 400, e.getMessage());
        } catch (NoAvailableBayException | BayDockingException e) {
            sendError(exchange, 409, e.getMessage());
        } catch (IllegalArgumentException e) {
            sendError(exchange, 400, "Malformed request.");
        } catch (RuntimeException e) {
            e.printStackTrace();
            sendError(exchange, 500, "The simulator hit an unexpected error.");
        }
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> form = new HashMap<>();

        for (String pair : body.split("&")) {
            int separator = pair.indexOf('=');
            if (separator > 0) {
                String key = URLDecoder.decode(pair.substring(0, separator), StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair.substring(separator + 1), StandardCharsets.UTF_8);
                form.put(key, value);
            }
        }

        return form;
    }

    // --- Static files ---

    private void handleStatic(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method not allowed.");
            return;
        }

        String requestPath = exchange.getRequestURI().getPath();
        String relativePath = requestPath.equals("/") ? "index.html" : requestPath.substring(1);

        Path file;
        try {
            file = webRoot.resolve(relativePath).normalize();
        } catch (InvalidPathException e) {
            sendError(exchange, 404, "File not found.");
            return;
        }

        String contentType = contentTypeOf(file);
        if (contentType == null || !file.startsWith(webRoot) || !Files.isRegularFile(file)) {
            sendError(exchange, 404, "File not found.");
            return;
        }

        send(exchange, 200, contentType, Files.readAllBytes(file));
    }

    private String contentTypeOf(Path file) {
        String name = file.getFileName() == null ? "" : file.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? null : CONTENT_TYPES.get(name.substring(dot));
    }

    // --- Responses ---

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        sendJson(exchange, status, Map.of("error", message));
    }

    private void sendJson(HttpExchange exchange, int status, Object body) throws IOException {
        send(exchange, status, "application/json; charset=utf-8", Json.write(body).getBytes(StandardCharsets.UTF_8));
    }

    private void send(HttpExchange exchange, int status, String contentType, byte[] body) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
