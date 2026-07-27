// Phase 4 Frontend - Ticket Detail, Reservation, Payment and Report UI Logic

(function () {
    let currentTicket = null;
    let currentReservation = null;

    document.addEventListener("DOMContentLoaded", function () {
        bindButtons();
        bindReportForm();
        loadTicketDetail();
    });

    function bindButtons() {
        const reserveButton = document.getElementById("reserveButton");
        const payButton = document.getElementById("payButton");
        const fillPaymentButton = document.getElementById("fillPaymentButton");
        const logoutButton = document.getElementById("logoutButton");

        if (reserveButton) {
            reserveButton.addEventListener("click", reserveTicket);
        }

        if (payButton) {
            payButton.addEventListener("click", payReservation);
        }

        if (fillPaymentButton) {
            fillPaymentButton.addEventListener("click", fillPaymentFromTicket);
        }

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                ApiClient.clearToken();
                window.location.href = "login.html";
            });
        }
    }

    function bindReportForm() {
        const form = document.getElementById("reportForm");

        if (!form) {
            return;
        }

        form.addEventListener("submit", async function (event) {
            event.preventDefault();
            await submitReport(form);
        });
    }

    async function loadTicketDetail() {
        const ticketId = readTicketId();

        if (!ticketId) {
            renderDetailError("Ticket id is missing. Open this page from the tickets list.");
            return;
        }

        setValue("reportTicketId", ticketId);

        try {
            const response = await ApiClient.get("/api/tickets/" + encodeURIComponent(ticketId), {
                useMockOnError: true
            });

            currentTicket = normalizeTicket(response);
            renderTicketDetail(currentTicket);
            fillPaymentFromTicket();
        } catch (error) {
            renderDetailError("Could not load ticket detail: " + error.message);
        }
    }

    async function reserveTicket() {
        const ticketId = readTicketId();

        if (!ticketId) {
            ApiClient.showMessage("detailMessage", "Ticket id is missing.", "error");
            return;
        }

        const note = getValue("reservationNote");
        const payload = note ? { note } : {};

        setBox("reservationResult", "Creating reservation...");

        try {
            const response = await ApiClient.post(
                "/api/tickets/" + encodeURIComponent(ticketId) + "/reserve",
                payload,
                { useMockOnError: true }
            );

            currentReservation = normalizeReservation(response);
            const reservationId = currentReservation.reservationId || currentReservation.reservation_id;

            if (reservationId) {
                setValue("reservationIdInput", reservationId);
                setValue("reportReservationId", reservationId);
            }

            setBox("reservationResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("detailMessage", "Reservation created successfully.", "success");
        } catch (error) {
            setBox("reservationResult", "Reservation failed: " + error.message);
            ApiClient.showMessage("detailMessage", "Reservation failed: " + error.message, "error");
        }
    }

    async function payReservation() {
        const reservationId = getValue("reservationIdInput");

        if (!reservationId) {
            ApiClient.showMessage("detailMessage", "Reservation ID is required for payment.", "error");
            return;
        }

        const payload = {
            paymentMethod: getValue("paymentMethod") || "CARD",
            trackingCode: getValue("paymentTrackingCode") || ("TRX-" + Date.now()),
            amount: Number(getValue("paymentAmount") || 0)
        };

        setBox("paymentResult", "Sending payment request...");

        try {
            const response = await ApiClient.post(
                "/api/reservations/" + encodeURIComponent(reservationId) + "/pay",
                payload,
                { useMockOnError: true }
            );

            setBox("paymentResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("detailMessage", "Payment request completed.", "success");
        } catch (error) {
            setBox("paymentResult", "Payment failed: " + error.message);
            ApiClient.showMessage("detailMessage", "Payment failed: " + error.message, "error");
        }
    }

    async function submitReport(form) {
        const payload = readForm(form);

        if (!payload.ticketId) {
            payload.ticketId = readTicketId();
        }

        setBox("reportResult", "Submitting report...");

        try {
            const response = await ApiClient.post("/api/reports", payload, { useMockOnError: true });
            setBox("reportResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("detailMessage", "Report submitted successfully.", "success");
        } catch (error) {
            setBox("reportResult", "Report failed: " + error.message);
            ApiClient.showMessage("detailMessage", "Report failed: " + error.message, "error");
        }
    }

    function renderTicketDetail(ticket) {
        const box = document.getElementById("ticketDetailBox");

        if (!box) {
            return;
        }

        const match = ticket.match || {};
        const sport = ticket.sport || {};
        const venue = ticket.venue || {};
        const teams = ticket.teams || {};
        const category = ticket.category || {};

        const ticketId = safe(ticket.ticketId || ticket.ticket_id);
        const matchTitle = safe(match.matchTitle || ticket.matchTitle || ticket.match_title || "Match");
        const sportName = safe(sport.sportName || ticket.sportName || ticket.sport);
        const venueName = safe(venue.venueName || ticket.venueName || ticket.venue);
        const cityName = safe(venue.cityName || ticket.cityName || ticket.city);
        const categoryName = safe(category.categoryName || ticket.categoryName || ticket.category);
        const homeTeam = safe(teams.homeTeamName || ticket.homeTeamName || ticket.home_team);
        const awayTeam = safe(teams.awayTeamName || ticket.awayTeamName || ticket.away_team);
        const status = safe(ticket.ticketStatus || ticket.ticket_status || "AVAILABLE");
        const seatNumber = safe(ticket.seatNumber || ticket.seat_number);
        const price = ApiClient.formatMoney(ticket.price);
        const matchTime = ApiClient.formatDateTime(match.matchStartTime || ticket.matchStartTime || ticket.match_time);

        const pageTitle = document.getElementById("pageTitle");
        if (pageTitle) {
            pageTitle.textContent = matchTitle;
        }

        box.innerHTML = `
            <article class="ticket-card" style="box-shadow: none;">
                <div class="inline-actions" style="justify-content: space-between;">
                    <span class="badge info">${escapeHtml(status)}</span>
                    <span class="price">${price}</span>
                </div>

                <h2>${escapeHtml(matchTitle)}</h2>

                <div class="meta-list">
                    <span><strong>Ticket ID:</strong> ${escapeHtml(ticketId)}</span>
                    <span><strong>Seat:</strong> ${escapeHtml(seatNumber)}</span>
                    <span><strong>Sport:</strong> ${escapeHtml(sportName)}</span>
                    <span><strong>Category:</strong> ${escapeHtml(categoryName)}</span>
                    <span><strong>Venue:</strong> ${escapeHtml(venueName)}</span>
                    <span><strong>City:</strong> ${escapeHtml(cityName)}</span>
                    <span><strong>Teams:</strong> ${escapeHtml(homeTeam)} vs ${escapeHtml(awayTeam)}</span>
                    <span><strong>Time:</strong> ${escapeHtml(matchTime)}</span>
                </div>
            </article>
        `;
    }

    function renderDetailError(message) {
        const box = document.getElementById("ticketDetailBox");

        if (box) {
            box.innerHTML = "<div class=\"alert error\">" + escapeHtml(message) + "</div>";
        }
    }

    function fillPaymentFromTicket() {
        if (!currentTicket) {
            return;
        }

        setValue("paymentMethod", "CARD");
        setValue("paymentTrackingCode", "TRX-" + Date.now());

        if (currentTicket.price !== undefined && currentTicket.price !== null) {
            setValue("paymentAmount", currentTicket.price);
        }
    }

    function normalizeTicket(response) {
        if (!response) {
            return {};
        }

        if (response.data && !Array.isArray(response.data)) {
            return response.data;
        }

        return response;
    }

    function normalizeReservation(response) {
        if (!response) {
            return {};
        }

        if (response.data && !Array.isArray(response.data)) {
            return response.data;
        }

        return response;
    }

    function readTicketId() {
        const params = ApiClient.readSearchParams();
        return params.get("id") || params.get("ticketId") || "";
    }

    function readForm(form) {
        const formData = new FormData(form);
        const payload = {};

        formData.forEach(function (value, key) {
            const cleanValue = String(value).trim();

            if (cleanValue !== "") {
                payload[key] = cleanValue;
            }
        });

        return payload;
    }

    function getValue(id) {
        const element = document.getElementById(id);
        return element ? String(element.value).trim() : "";
    }

    function setValue(id, value) {
        const element = document.getElementById(id);

        if (element && value !== null && value !== undefined) {
            element.value = value;
        }
    }

    function setBox(id, text) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = text;
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
