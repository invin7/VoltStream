package grid;

import java.io.IOException;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.Path;

import grid.web.SimulationService;
import grid.web.WebServer;

public class Main {
    private static final int DEFAULT_PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web");

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(WEB_ROOT)) {
            System.err.println("Could not find the web folder. Run VoltStream from the project root.");
            System.exit(1);
        }

        int port = DEFAULT_PORT;

        try {
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            WebServer server = new WebServer(port, WEB_ROOT, new SimulationService());
            server.start();

            System.out.printf("VoltStream is running at http://localhost:%d%n", server.getPort());
            System.out.println("Press Ctrl+C to stop.");
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid port: " + args[0]);
            System.exit(1);
        } catch (BindException e) {
            System.err.printf("Port %d is already in use. Pass another one: java -cp out grid.Main 9090%n", port);
            System.exit(1);
        }
    }
}
