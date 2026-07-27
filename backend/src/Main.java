import com.sun.net.httpserver.HttpServer;
import config.AppConfig;
import http.JsonResponse;
import http.Router;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = AppConfig.getServerPort();

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        Router router = new Router();

        registerBaseRoutes(router);

        server.createContext("/", router);
        server.setExecutor(null);
        server.start();

        System.out.println("Sports Ticket Reservation API is running on http://localhost:" + port);
        System.out.println("Health check: http://localhost:" + port + "/api/health");
    }

    private static void registerBaseRoutes(Router router) {
        router.get("/api/health", exchange -> JsonResponse.ok(exchange,
                "{"
                        + "\"status\":\"UP\","
                        + "\"service\":\"sports-ticket-reservation-api\","
                        + "\"phase\":\"phase-3-java-backend\""
                        + "}"
        ));
    }
}
