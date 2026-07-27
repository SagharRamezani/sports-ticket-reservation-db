import com.sun.net.httpserver.HttpServer;
import config.AppConfig;
import controllers.*;
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
        registerAuthRoutes(router);
        registerUserRoutes(router);
        registerTicketRoutes(router);
        registerReservationAndPaymentRoutes(router);
        registerReportAndAdminRoutes(router);

        server.createContext("/", router);
        server.setExecutor(null);
        server.start();

        System.out.println("Sports Ticket Reservation API is running on http://localhost:" + port);
        System.out.println("Health check: http://localhost:" + port + "/api/health");
        System.out.println("Frontend default URL: http://localhost:5500");
        System.out.println("Elasticsearch URL: " + AppConfig.getElasticUrl());
        System.out.println("Elasticsearch ticket index: " + AppConfig.getElasticTicketIndex());
    }

    private static void registerBaseRoutes(Router router) {
        router.get("/api/health", exchange -> JsonResponse.ok(exchange,
                "{"
                        + "\"status\":\"UP\","
                        + "\"service\":\"sports-ticket-reservation-api\","
                        + "\"phase\":\"phase-4-ui-elastic\","
                        + "\"frontend\":\"http://localhost:5500\","
                        + "\"database\":\"postgresql\","
                        + "\"search\":\"elasticsearch-with-sql-fallback\""
                        + "}"
        ));
    }

    private static void registerAuthRoutes(Router router) {
        AuthController authController = new AuthController();

        router.post("/api/auth/signup", authController::signup);
        router.post("/api/auth/request-otp", authController::requestOtp);
        router.post("/api/auth/verify-otp", authController::verifyOtp);
        router.post("/api/auth/login", authController::login);
    }

    private static void registerUserRoutes(Router router) {
        UserController userController = new UserController();

        router.get("/api/users/me", userController::getMyProfile);
        router.patch("/api/users/me", userController::updateMyProfile);
        router.get("/api/users/me/bookings", userController::getMyBookings);
    }

    private static void registerTicketRoutes(Router router) {
        TicketController ticketController = new TicketController();

        router.get("/api/cities", ticketController::listCities);
        router.get("/api/venues", ticketController::listVenues);
        router.get("/api/tickets", ticketController::listTickets);
        router.get("/api/tickets/search", ticketController::searchTickets);
        router.get("/api/tickets/{id}", ticketController::getTicketDetail);
    }

    private static void registerReservationAndPaymentRoutes(Router router) {
        ReservationController reservationController = new ReservationController();
        PaymentController paymentController = new PaymentController();

        router.post("/api/tickets/{id}/reserve", reservationController::reserveTicket);
        router.get("/api/reservations/{id}/cancellation-penalty", reservationController::getCancellationPenalty);
        router.post("/api/reservations/{id}/cancel", reservationController::cancelReservation);
        router.post("/api/reservations/{id}/pay", paymentController::payReservation);
    }

    private static void registerReportAndAdminRoutes(Router router) {
        ReportController reportController = new ReportController();
        AdminController adminController = new AdminController();

        router.post("/api/reports", reportController::createReport);
        router.get("/api/reports/me", reportController::getMyReports);

        router.get("/api/admin/reports", adminController::getReports);
        router.patch("/api/admin/reports/{id}", adminController::updateReport);
        router.get("/api/admin/reservations/suspicious", adminController::getSuspiciousReservations);
        router.patch("/api/admin/reservations/{id}", adminController::updateReservationStatus);
    }
}
