package services;

import http.JsonResponse;
import repositories.TicketRepository;
import repositories.TicketRepository.CityResult;
import repositories.TicketRepository.TicketResult;
import repositories.TicketRepository.VenueResult;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class TicketService {
    private final TicketRepository ticketRepository;

    public TicketService() {
        this.ticketRepository = new TicketRepository();
    }

    public String listTickets() {
        List<TicketResult> tickets = ticketRepository.findAvailableTickets();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < tickets.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildTicketJson(tickets.get(i)));
        }

        json.append("],\"count\":").append(tickets.size()).append("}");
        return json.toString();
    }

    public String getTicketDetail(long ticketId) {
        if (ticketId <= 0) {
            throw new IllegalArgumentException("Invalid ticketId");
        }

        TicketResult ticket = ticketRepository.findTicketDetailById(ticketId);

        return "{"
                + "\"success\":true,"
                + "\"data\":" + buildTicketJson(ticket)
                + "}";
    }

    public String listCities() {
        List<CityResult> cities = ticketRepository.findCities();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < cities.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildCityJson(cities.get(i)));
        }

        json.append("],\"count\":").append(cities.size()).append("}");
        return json.toString();
    }

    public String listVenues() {
        List<VenueResult> venues = ticketRepository.findVenues();

        StringBuilder json = new StringBuilder();
        json.append("{\"success\":true,\"data\":[");

        for (int i = 0; i < venues.size(); i++) {
            if (i > 0) {
                json.append(",");
            }

            json.append(buildVenueJson(venues.get(i)));
        }

        json.append("],\"count\":").append(venues.size()).append("}");
        return json.toString();
    }

    private String buildTicketJson(TicketResult ticket) {
        return "{"
                + "\"ticketId\":" + ticket.ticketId() + ","
                + "\"ticketStatus\":\"" + JsonResponse.escape(ticket.ticketStatus()) + "\","
                + "\"seatNumber\":\"" + JsonResponse.escape(ticket.seatNumber()) + "\","
                + "\"price\":" + money(ticket.price()) + ","
                + "\"category\":{"
                + "\"categoryId\":" + nullableLong(ticket.categoryId()) + ","
                + "\"categoryName\":\"" + JsonResponse.escape(ticket.categoryName()) + "\""
                + "},"
                + "\"match\":{"
                + "\"matchId\":" + ticket.matchId() + ","
                + "\"matchTitle\":\"" + JsonResponse.escape(ticket.matchTitle()) + "\","
                + "\"matchStartTime\":\"" + JsonResponse.escape(ticket.matchStartTime()) + "\","
                + "\"matchStatus\":\"" + JsonResponse.escape(ticket.matchStatus()) + "\""
                + "},"
                + "\"sport\":{"
                + "\"sportId\":" + nullableLong(ticket.sportId()) + ","
                + "\"sportName\":\"" + JsonResponse.escape(ticket.sportName()) + "\""
                + "},"
                + "\"venue\":{"
                + "\"venueId\":" + nullableLong(ticket.venueId()) + ","
                + "\"venueName\":\"" + JsonResponse.escape(ticket.venueName()) + "\","
                + "\"cityName\":\"" + JsonResponse.escape(ticket.cityName()) + "\""
                + "},"
                + "\"teams\":{"
                + "\"homeTeamName\":\"" + JsonResponse.escape(ticket.homeTeamName()) + "\","
                + "\"awayTeamName\":\"" + JsonResponse.escape(ticket.awayTeamName()) + "\""
                + "}"
                + "}";
    }

    private String buildCityJson(CityResult city) {
        return "{"
                + "\"cityId\":" + city.cityId() + ","
                + "\"cityName\":\"" + JsonResponse.escape(city.cityName()) + "\","
                + "\"province\":\"" + JsonResponse.escape(city.province()) + "\","
                + "\"activeVenueCount\":" + city.activeVenueCount()
                + "}";
    }

    private String buildVenueJson(VenueResult venue) {
        return "{"
                + "\"venueId\":" + venue.venueId() + ","
                + "\"venueName\":\"" + JsonResponse.escape(venue.venueName()) + "\","
                + "\"address\":\"" + JsonResponse.escape(venue.address()) + "\","
                + "\"capacity\":" + venue.capacity() + ","
                + "\"city\":{"
                + "\"cityId\":" + nullableLong(venue.cityId()) + ","
                + "\"cityName\":\"" + JsonResponse.escape(venue.cityName()) + "\""
                + "}"
                + "}";
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String nullableLong(Long value) {
        return value == null ? "null" : String.valueOf(value);
    }
}
