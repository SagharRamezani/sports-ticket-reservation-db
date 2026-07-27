// Phase 4 Frontend - Ticket Search UI Logic

(function () {
    document.addEventListener("DOMContentLoaded", function () {
        bindSearchForm();
        bindButtons();
        runInitialSearch();
    });

    function bindSearchForm() {
        const form = document.getElementById("ticketSearchForm");

        if (!form) {
            return;
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            await searchTickets();
        });
    }

    function bindButtons() {
        const resetButton = document.getElementById("resetSearchButton");
        const mockButton = document.getElementById("loadMockTicketsButton");
        const logoutButton = document.getElementById("logoutButton");

        if (resetButton) {
            resetButton.addEventListener("click", async function () {
                const form = document.getElementById("ticketSearchForm");
                if (form) {
                    form.reset();
                }

                await searchTickets();
            });
        }

        if (mockButton) {
            mockButton.addEventListener("click", function () {
                renderTickets(window.MockData ? window.MockData.getTickets() : [], {
                    searchEngine: "mock",
                    fallbackUsed: true,
                    count: window.MockData ? window.MockData.getTickets().length : 0
                });

                ApiClient.showMessage("searchMessage", "Mock tickets loaded for frontend demo.", "success");
            });
        }

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                ApiClient.clearToken();
                window.location.href = "login.html";
            });
        }
    }

    async function runInitialSearch() {
        const params = ApiClient.readSearchParams();

        setValue("sport", params.get("sport"));
        setValue("city", params.get("city"));
        setValue("venue", params.get("venue"));
        setValue("team", params.get("team"));
        setValue("category", params.get("category"));
        setValue("min_price", params.get("min_price"));
        setValue("max_price", params.get("max_price"));
        setValue("match_date", params.get("match_date"));

        await searchTickets();
    }

    async function searchTickets() {
        ApiClient.hideMessage("searchMessage");

        const form = document.getElementById("ticketSearchForm");
        const filters = readFilters(form);
        const query = ApiClient.buildQuery(filters);
        const path = "/api/tickets/search" + query;

        setLoading(true);

        try {
            const response = await ApiClient.get(path, { useMockOnError: true });
            const tickets = normalizeTickets(response);

            renderTickets(tickets, {
                searchEngine: response.searchEngine || "api",
                fallbackUsed: Boolean(response.fallbackUsed),
                count: response.count !== undefined ? response.count : tickets.length
            });

            ApiClient.showMessage("searchMessage", "Search completed successfully.", "success");
        } catch (error) {
            ApiClient.showMessage("searchMessage", "Search failed: " + error.message, "error");
            renderTickets([], {
                searchEngine: "error",
                fallbackUsed: false,
                count: 0
            });
        } finally {
            setLoading(false);
        }
    }

    function readFilters(form) {
        const formData = new FormData(form);
        const filters = {};

        formData.forEach(function (value, key) {
            const cleanValue = String(value).trim();

            if (cleanValue !== "") {
                filters[key] = cleanValue;
            }
        });

        return filters;
    }

    function normalizeTickets(response) {
        if (!response) {
            return [];
        }

        if (Array.isArray(response)) {
            return response;
        }

        if (Array.isArray(response.data)) {
            return response.data;
        }

        if (response.data && Array.isArray(response.data.tickets)) {
            return response.data.tickets;
        }

        if (response.tickets && Array.isArray(response.tickets)) {
            return response.tickets;
        }

        return [];
    }

    function renderTickets(tickets, meta) {
        const container = document.getElementById("ticketResults");
        const resultTitle = document.getElementById("resultTitle");
        const resultMeta = document.getElementById("resultMeta");
        const searchSource = document.getElementById("searchSource");

        if (!container) {
            return;
        }

        const count = tickets.length;

        if (resultTitle) {
            resultTitle.textContent = count + " ticket" + (count === 1 ? "" : "s") + " found";
        }

        if (resultMeta) {
            const engine = meta.searchEngine || "unknown";
            const fallback = meta.fallbackUsed ? "Fallback enabled" : "Direct result";
            resultMeta.textContent = "Search engine: " + engine + " - " + fallback;
        }

        if (searchSource) {
            searchSource.textContent = (meta.searchEngine || "unknown") + (meta.fallbackUsed ? " fallback" : "");
        }

        if (!tickets.length) {
            container.innerHTML = "<div class=\"alert\">No tickets found. Try changing the filters.</div>";
            return;
        }

        container.innerHTML = tickets.map(renderTicketCard).join("");
    }

    function renderTicketCard(ticket) {
        const ticketId = safe(ticket.ticketId || ticket.ticket_id);
        const match = ticket.match || {};
        const sport = ticket.sport || {};
        const venue = ticket.venue || {};
        const teams = ticket.teams || {};
        const category = ticket.category || {};

        const matchTitle = safe(match.matchTitle || ticket.matchTitle || ticket.match_title || "Match");
        const sportName = safe(sport.sportName || ticket.sportName || ticket.sport || "-");
        const venueName = safe(venue.venueName || ticket.venueName || ticket.venue || "-");
        const cityName = safe(venue.cityName || ticket.cityName || ticket.city || "-");
        const categoryName = safe(category.categoryName || ticket.categoryName || ticket.category || "-");
        const homeTeam = safe(teams.homeTeamName || ticket.homeTeamName || ticket.home_team || "-");
        const awayTeam = safe(teams.awayTeamName || ticket.awayTeamName || ticket.away_team || "-");
        const status = safe(ticket.ticketStatus || ticket.ticket_status || "AVAILABLE");
        const seatNumber = safe(ticket.seatNumber || ticket.seat_number || "-");
        const price = ApiClient.formatMoney(ticket.price);
        const matchTime = ApiClient.formatDateTime(match.matchStartTime || ticket.matchStartTime || ticket.match_time);

        return `
            <article class="ticket-card">
                <div class="inline-actions" style="justify-content: space-between;">
                    <span class="badge info">${escapeHtml(status)}</span>
                    <span class="price">${price}</span>
                </div>

                <h3>${escapeHtml(matchTitle)}</h3>

                <div class="meta-list">
                    <span><strong>Sport:</strong> ${escapeHtml(sportName)}</span>
                    <span><strong>City:</strong> ${escapeHtml(cityName)}</span>
                    <span><strong>Venue:</strong> ${escapeHtml(venueName)}</span>
                    <span><strong>Time:</strong> ${escapeHtml(matchTime)}</span>
                    <span><strong>Teams:</strong> ${escapeHtml(homeTeam)} vs ${escapeHtml(awayTeam)}</span>
                    <span><strong>Category:</strong> ${escapeHtml(categoryName)}</span>
                    <span><strong>Seat:</strong> ${escapeHtml(seatNumber)}</span>
                    <span><strong>Ticket ID:</strong> ${escapeHtml(ticketId)}</span>
                </div>

                <div class="inline-actions">
                    <a class="btn btn-primary" href="ticket-detail.html?id=${encodeURIComponent(ticketId)}">View details</a>
                    <a class="btn btn-outline" href="ticket-detail.html?id=${encodeURIComponent(ticketId)}#reserve">Reserve</a>
                </div>
            </article>
        `;
    }

    function setLoading(isLoading) {
        const container = document.getElementById("ticketResults");

        if (isLoading && container) {
            container.innerHTML = "<div class=\"alert\">Loading tickets...</div>";
        }
    }

    function setValue(id, value) {
        const element = document.getElementById(id);

        if (element && value) {
            element.value = value;
        }
    }

    function safe(value) {
        if (value === null || value === undefined || value === "") {
            return "-";
        }

        return String(value);
    }

    function escapeHtml(value) {
        return String(value)
            .replaceAll("&", "&amp;")
            .replaceAll("<", "&lt;")
            .replaceAll(">", "&gt;")
            .replaceAll("\"", "&quot;")
            .replaceAll("'", "&#039;");
    }
})();
