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
        registerPhase3Routes(router);

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

    private static void registerPhase3Routes(Router router) {
        AuthController authController = new AuthController();
        UserController userController = new UserController();
        TicketController ticketController = new TicketController();
        ReservationController reservationController = new ReservationController();
        PaymentController paymentController = new PaymentController();
        ReportController reportController = new ReportController();
        AdminController adminController = new AdminController();

        router.post("/api/auth/signup", authController::signup);
        router.post("/api/auth/request-otp", authController::requestOtp);
        router.post("/api/auth/verify-otp", authController::verifyOtp);
        router.post("/api/auth/login", authController::login);

        router.get("/api/users/me", userController::getMyProfile);
        router.patch("/api/users/me", userController::updateMyProfile);
        router.get("/api/users/me/bookings", userController::getMyBookings);

        router.get("/api/cities", ticketController::listCities);
        router.get("/api/venues", ticketController::listVenues);
        router.get("/api/tickets", ticketController::listTickets);
        router.get("/api/tickets/search", ticketController::searchTickets);
        router.get("/api/tickets/{id}", ticketController::getTicketDetail);

        router.post("/api/tickets/{id}/reserve", reservationController::reserveTicket);
        router.post("/api/reservations/{id}/pay", paymentController::payReservation);
        router.get("/api/reservations/{id}/cancellation-penalty", reservationController::getCancellationPenalty);
        router.post("/api/reservations/{id}/cancel", reservationController::cancelReservation);

        router.post("/api/reports", reportController::createReport);
        router.get("/api/reports/me", reportController::getMyReports);

        router.get("/api/admin/reports", adminController::getReports);
        router.patch("/api/admin/reports/{id}", adminController::updateReport);
        router.get("/api/admin/reservations/suspicious", adminController::getSuspiciousReservations);
        router.patch("/api/admin/reservations/{id}", adminController::updateReservationStatus);
    }
}