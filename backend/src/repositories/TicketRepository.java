package repositories;

import db.Database;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TicketRepository {
    public List<TicketResult> findAvailableTickets() {
        String sql = """
                SELECT
                    t.ticket_id,
                    t.ticket_status,
                    t.seat_number,
                    t.price,
                    tc.ticket_category_id,
                    tc.category_name,
                    m.match_id,
                    COALESCE(m.match_title, CONCAT(COALESCE(home_team.team_name, 'Team A'), ' vs ', COALESCE(away_team.team_name, 'Team B'))) AS match_title,
                    m.match_start_time,
                    m.match_status,
                    s.sport_id,
                    s.sport_name,
                    v.venue_id,
                    v.venue_name,
                    c.city_name,
                    home_team.team_name AS home_team_name,
                    away_team.team_name AS away_team_name
                FROM tickets t
                JOIN ticket_categories tc ON t.ticket_category_id = tc.ticket_category_id
                JOIN matches m ON tc.match_id = m.match_id
                LEFT JOIN sports s ON m.sport_id = s.sport_id
                LEFT JOIN venues v ON m.venue_id = v.venue_id
                LEFT JOIN cities c ON v.city_id = c.city_id
                LEFT JOIN teams home_team ON m.home_team_id = home_team.team_id
                LEFT JOIN teams away_team ON m.away_team_id = away_team.team_id
                WHERE t.ticket_status = 'AVAILABLE'
                ORDER BY m.match_start_time ASC, t.ticket_id ASC
                LIMIT 100
                """;

        List<TicketResult> tickets = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                tickets.add(mapTicket(resultSet));
            }

            return tickets;
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load ticket list", ex);
        }
    }

    public TicketResult findTicketDetailById(long ticketId) {
        String sql = """
                SELECT
                    t.ticket_id,
                    t.ticket_status,
                    t.seat_number,
                    t.price,
                    tc.ticket_category_id,
                    tc.category_name,
                    m.match_id,
                    COALESCE(m.match_title, CONCAT(COALESCE(home_team.team_name, 'Team A'), ' vs ', COALESCE(away_team.team_name, 'Team B'))) AS match_title,
                    m.match_start_time,
                    m.match_status,
                    s.sport_id,
                    s.sport_name,
                    v.venue_id,
                    v.venue_name,
                    c.city_name,
                    home_team.team_name AS home_team_name,
                    away_team.team_name AS away_team_name
                FROM tickets t
                JOIN ticket_categories tc ON t.ticket_category_id = tc.ticket_category_id
                JOIN matches m ON tc.match_id = m.match_id
                LEFT JOIN sports s ON m.sport_id = s.sport_id
                LEFT JOIN venues v ON m.venue_id = v.venue_id
                LEFT JOIN cities c ON v.city_id = c.city_id
                LEFT JOIN teams home_team ON m.home_team_id = home_team.team_id
                LEFT JOIN teams away_team ON m.away_team_id = away_team.team_id
                WHERE t.ticket_id = ?
                LIMIT 1
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, ticketId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Ticket not found");
                }

                return mapTicket(resultSet);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Could not load ticket detail", ex);
        }
    }

    private TicketResult mapTicket(ResultSet resultSet) throws SQLException {
        long categoryIdValue = resultSet.getLong("ticket_category_id");
        Long categoryId = resultSet.wasNull() ? null : categoryIdValue;

        long sportIdValue = resultSet.getLong("sport_id");
        Long sportId = resultSet.wasNull() ? null : sportIdValue;

        long venueIdValue = resultSet.getLong("venue_id");
        Long venueId = resultSet.wasNull() ? null : venueIdValue;

        return new TicketResult(
                resultSet.getLong("ticket_id"),
                resultSet.getString("ticket_status"),
                resultSet.getString("seat_number"),
                resultSet.getBigDecimal("price"),
                categoryId,
                resultSet.getString("category_name"),
                resultSet.getLong("match_id"),
                resultSet.getString("match_title"),
                timestampToString(resultSet, "match_start_time"),
                resultSet.getString("match_status"),
                sportId,
                resultSet.getString("sport_name"),
                venueId,
                resultSet.getString("venue_name"),
                resultSet.getString("city_name"),
                resultSet.getString("home_team_name"),
                resultSet.getString("away_team_name")
        );
    }

    private String timestampToString(ResultSet resultSet, String columnName) throws SQLException {
        if (resultSet.getTimestamp(columnName) == null) {
            return "";
        }

        return resultSet.getTimestamp(columnName).toLocalDateTime().toString();
    }

    public record TicketResult(
            long ticketId,
            String ticketStatus,
            String seatNumber,
            BigDecimal price,
            Long categoryId,
            String categoryName,
            long matchId,
            String matchTitle,
            String matchStartTime,
            String matchStatus,
            Long sportId,
            String sportName,
            Long venueId,
            String venueName,
            String cityName,
            String homeTeamName,
            String awayTeamName
    ) {
    }
}
