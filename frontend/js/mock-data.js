// Phase 4 Frontend - Mock data for demo when backend is unavailable.

(function () {
    const tickets = [
        {
            ticketId: 1,
            ticketStatus: "AVAILABLE",
            seatNumber: "A-12",
            price: 1500000,
            category: {
                categoryId: 1,
                categoryName: "VIP"
            },
            match: {
                matchId: 1,
                matchTitle: "Persepolis vs Esteghlal",
                matchStartTime: "2026-07-30T18:00:00",
                matchStatus: "SCHEDULED"
            },
            sport: {
                sportId: 1,
                sportName: "Football"
            },
            venue: {
                venueId: 1,
                venueName: "Azadi Stadium",
                cityName: "Tehran"
            },
            teams: {
                homeTeamName: "Persepolis",
                awayTeamName: "Esteghlal"
            }
        },
        {
            ticketId: 2,
            ticketStatus: "AVAILABLE",
            seatNumber: "B-20",
            price: 500000,
            category: {
                categoryId: 2,
                categoryName: "NORMAL"
            },
            match: {
                matchId: 2,
                matchTitle: "Paykan vs Saipa",
                matchStartTime: "2026-08-01T16:00:00",
                matchStatus: "SCHEDULED"
            },
            sport: {
                sportId: 2,
                sportName: "Volleyball"
            },
            venue: {
                venueId: 2,
                venueName: "Azadi Volleyball Hall",
                cityName: "Tehran"
            },
            teams: {
                homeTeamName: "Paykan",
                awayTeamName: "Saipa"
            }
        },
        {
            ticketId: 3,
            ticketStatus: "AVAILABLE",
            seatNumber: "C-07",
            price: 800000,
            category: {
                categoryId: 3,
                categoryName: "SPECIAL"
            },
            match: {
                matchId: 3,
                matchTitle: "Mahram vs Zob Ahan",
                matchStartTime: "2026-08-04T19:30:00",
                matchStatus: "SCHEDULED"
            },
            sport: {
                sportId: 3,
                sportName: "Basketball"
            },
            venue: {
                venueId: 3,
                venueName: "Enghelab Hall",
                cityName: "Tehran"
            },
            teams: {
                homeTeamName: "Mahram",
                awayTeamName: "Zob Ahan"
            }
        },
        {
            ticketId: 4,
            ticketStatus: "AVAILABLE",
            seatNumber: "D-15",
            price: 350000,
            category: {
                categoryId: 4,
                categoryName: "ECONOMY"
            },
            match: {
                matchId: 4,
                matchTitle: "Sepahan vs Tractor",
                matchStartTime: "2026-08-06T20:00:00",
                matchStatus: "SCHEDULED"
            },
            sport: {
                sportId: 1,
                sportName: "Football"
            },
            venue: {
                venueId: 4,
                venueName: "Naghsh-e Jahan Stadium",
                cityName: "Isfahan"
            },
            teams: {
                homeTeamName: "Sepahan",
                awayTeamName: "Tractor"
            }
        }
    ];

    function resolve(path, method, body) {
        const normalizedMethod = (method || "GET").toUpperCase();

        if (normalizedMethod === "GET" && path.startsWith("/api/tickets/search")) {
            return {
                success: true,
                searchEngine: "mock",
                fallbackUsed: true,
                filters: {},
                data: tickets,
                count: tickets.length
            };
        }

        if (normalizedMethod === "GET" && path.startsWith("/api/tickets/")) {
            const ticketId = Number(path.replace("/api/tickets/", "").split("/")[0]);
            const ticket = tickets.find(function (item) {
                return Number(item.ticketId) === ticketId;
            });

            return {
                success: true,
                data: ticket || tickets[0]
            };
        }

        if (normalizedMethod === "POST" && path.includes("/reserve")) {
            return {
                success: true,
                message: "Mock reservation created successfully.",
                data: {
                    reservationId: 101,
                    reservationStatus: "PENDING_PAYMENT"
                }
            };
        }

        return {
            success: true,
            message: "Mock response",
            data: body || {}
        };
    }

    function getTickets() {
        return tickets.slice();
    }

    window.MockData = {
        resolve,
        getTickets
    };
})();
