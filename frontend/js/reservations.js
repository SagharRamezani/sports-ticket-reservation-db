// Phase 4 Frontend - Reservation, Payment, Cancellation and Report UI Logic

(function () {
    let currentTicket = null;
    let currentReservation = null;

    document.addEventListener("DOMContentLoaded", function () {
        bindSharedButtons();

        if (document.getElementById("ticketDetailBox")) {
            bindTicketDetailPage();
            loadTicketDetail();
        }

        if (document.getElementById("bookingsList")) {
            bindBookingsPage();
            loadBookings();
        }
    });

    function bindSharedButtons() {
        const logoutButton = document.getElementById("logoutButton");

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                ApiClient.clearToken();
                window.location.href = "login.html";
            });
        }
    }

    function bindTicketDetailPage() {
        const reserveButton = document.getElementById("reserveButton");
        const payButton = document.getElementById("payButton");
        const fillPaymentButton = document.getElementById("fillPaymentButton");
        const reportForm = document.getElementById("reportForm");

        if (reserveButton) {
            reserveButton.addEventListener("click", reserveTicket);
        }

        if (payButton) {
            payButton.addEventListener("click", function () {
                const reservationId = getValue("reservationIdInput");
                const amount = Number(getValue("paymentAmount") || 0);
                const method = getValue("paymentMethod") || "CARD";
                payReservation(reservationId, amount, method, "paymentResult", "detailMessage");
            });
        }

        if (fillPaymentButton) {
            fillPaymentButton.addEventListener("click", fillPaymentFromTicket);
        }

        if (reportForm) {
            reportForm.addEventListener("submit", async function (event) {
                event.preventDefault();
                await submitReport(reportForm);
            });
        }
    }

    function bindBookingsPage() {
        const loadButton = document.getElementById("loadBookingsButton");
        const mockButton = document.getElementById("loadMockBookingsButton");
        const manualPayButton = document.getElementById("manualPayButton");
        const manualPenaltyButton = document.getElementById("manualPenaltyButton");
        const manualCancelButton = document.getElementById("manualCancelButton");

        if (loadButton) {
            loadButton.addEventListener("click", loadBookings);
        }

        if (mockButton) {
            mockButton.addEventListener("click", function () {
                renderBookings(getMockBookings());
                ApiClient.showMessage("bookingsMessage", "Mock bookings loaded.", "success");
            });
        }

        if (manualPayButton) {
            manualPayButton.addEventListener("click", function () {
                payReservation(
                    getValue("manualReservationId"),
                    Number(getValue("manualPaymentAmount") || 0),
                    getValue("manualPaymentMethod") || "CARD",
                    "bookingActionResult",
                    "bookingsMessage"
                );
            });
        }

        if (manualPenaltyButton) {
            manualPenaltyButton.addEventListener("click", function () {
                checkCancellationPenalty(getValue("manualReservationId"));
            });
        }

        if (manualCancelButton) {
            manualCancelButton.addEventListener("click", function () {
                cancelReservation(getValue("manualReservationId"), getValue("manualCancelReason"));
            });
        }
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
            const reservationId = currentReservation.reservationId || currentReservation.reservation_id || 101;

            setValue("reservationIdInput", reservationId);
            setValue("reportReservationId", reservationId);
            setBox("reservationResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("detailMessage", "Reservation created successfully.", "success");
        } catch (error) {
            setBox("reservationResult", "Reservation failed: " + error.message);
            ApiClient.showMessage("detailMessage", "Reservation failed: " + error.message, "error");
        }
    }

    async function loadBookings() {
        const list = document.getElementById("bookingsList");

        if (list) {
            list.innerHTML = "<div class=\"alert\">Loading bookings...</div>";
        }

        try {
            const response = await ApiClient.get("/api/users/me/bookings", { useMockOnError: true });
            const bookings = normalizeBookings(response);
            renderBookings(bookings);
            ApiClient.showMessage("bookingsMessage", "Bookings loaded successfully.", "success");
        } catch (error) {
            renderBookings(getMockBookings());
            ApiClient.showMessage("bookingsMessage", "Booking API failed. Mock bookings are shown.", "error");
        }
    }

    async function payReservation(reservationId, amount, method, resultBoxId, messageBoxId) {
        if (!reservationId) {
            ApiClient.showMessage(messageBoxId, "Reservation ID is required.", "error");
            return;
        }

        const payload = {
            paymentMethod: method || "CARD",
            trackingCode: "TRX-" + Date.now(),
            amount: amount || 0
        };

        setBox(resultBoxId, "Sending payment request...");

        try {
            const response = await ApiClient.post(
                "/api/reservations/" + encodeURIComponent(reservationId) + "/pay",
                payload,
                { useMockOnError: true }
            );

            setBox(resultBoxId, JSON.stringify(response, null, 2));
            ApiClient.showMessage(messageBoxId, "Payment completed.", "success");
            if (document.getElementById("bookingsList")) {
                await loadBookings();
            }
        } catch (error) {
            setBox(resultBoxId, "Payment failed: " + error.message);
            ApiClient.showMessage(messageBoxId, "Payment failed: " + error.message, "error");
        }
    }

    async function checkCancellationPenalty(reservationId) {
        if (!reservationId) {
            ApiClient.showMessage("bookingsMessage", "Reservation ID is required.", "error");
            return;
        }

        setBox("bookingActionResult", "Checking cancellation penalty...");

        try {
            const response = await ApiClient.get(
                "/api/reservations/" + encodeURIComponent(reservationId) + "/cancellation-penalty",
                { useMockOnError: true }
            );

            setBox("bookingActionResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("bookingsMessage", "Penalty loaded.", "success");
        } catch (error) {
            setBox("bookingActionResult", "Penalty check failed: " + error.message);
            ApiClient.showMessage("bookingsMessage", "Penalty check failed.", "error");
        }
    }

    async function cancelReservation(reservationId, reason) {
        if (!reservationId) {
            ApiClient.showMessage("bookingsMessage", "Reservation ID is required.", "error");
            return;
        }

        setBox("bookingActionResult", "Cancelling reservation...");

        try {
            const response = await ApiClient.post(
                "/api/reservations/" + encodeURIComponent(reservationId) + "/cancel",
                { cancellationReason: reason || "Cancelled from frontend UI" },
                { useMockOnError: true }
            );

            setBox("bookingActionResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("bookingsMessage", "Reservation cancelled.", "success");
            await loadBookings();
        } catch (error) {
            setBox("bookingActionResult", "Cancellation failed: " + error.message);
            ApiClient.showMessage("bookingsMessage", "Cancellation failed.", "error");
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

    function renderBookings(bookings) {
        const container = document.getElementById("bookingsList");
        const title = document.getElementById("bookingsTitle");
        const meta = document.getElementById("bookingsMeta");

        if (!container) {
            return;
        }

        if (title) {
            title.textContent = bookings.length + " booking" + (bookings.length === 1 ? "" : "s") + " found";
        }

        if (meta) {
            meta.textContent = "Booking history loaded from API or mock fallback.";
        }

        if (!bookings.length) {
            container.innerHTML = "<div class=\"alert\">No bookings found.</div>";
            return;
        }

        container.innerHTML = bookings.map(renderBookingCard).join("");

        container.querySelectorAll("[data-pay-reservation]").forEach(function (button) {
            button.addEventListener("click", function () {
                setValue("manualReservationId", button.dataset.payReservation);
                setValue("manualPaymentAmount", button.dataset.amount || 0);
                payReservation(button.dataset.payReservation, Number(button.dataset.amount || 0), "CARD", "bookingActionResult", "bookingsMessage");
            });
        });

        container.querySelectorAll("[data-penalty-reservation]").forEach(function (button) {
            button.addEventListener("click", function () {
                setValue("manualReservationId", button.dataset.penaltyReservation);
                checkCancellationPenalty(button.dataset.penaltyReservation);
            });
        });

        container.querySelectorAll("[data-cancel-reservation]").forEach(function (button) {
            button.addEventListener("click", function () {
                setValue("manualReservationId", button.dataset.cancelReservation);
                cancelReservation(button.dataset.cancelReservation, "Cancelled from booking card");
            });
        });
    }

    function renderBookingCard(booking) {
        const reservationId = safe(booking.reservationId || booking.reservation_id);
        const status = safe(booking.reservationStatus || booking.reservation_status || "PENDING_PAYMENT");
        const ticket = booking.ticket || booking;
        const match = ticket.match || {};
        const amount = booking.amount || booking.totalPrice || booking.total_price || ticket.price || 0;
        const matchTitle = safe(match.matchTitle || booking.matchTitle || booking.match_title || "Match");
        const matchTime = ApiClient.formatDateTime(match.matchStartTime || booking.matchStartTime || booking.match_start_time);
        const seatNumber = safe(ticket.seatNumber || booking.seatNumber || booking.seat_number || "-");

        return `
            <article class="booking-card">
                <div class="inline-actions" style="justify-content: space-between;">
                    <span class="badge ${status === "CANCELLED" ? "danger" : "info"}">${escapeHtml(status)}</span>
                    <span class="price">${ApiClient.formatMoney(amount)}</span>
                </div>

                <h3>${escapeHtml(matchTitle)}</h3>

                <div class="meta-list">
                    <span><strong>Reservation ID:</strong> ${escapeHtml(reservationId)}</span>
                    <span><strong>Seat:</strong> ${escapeHtml(seatNumber)}</span>
                    <span><strong>Time:</strong> ${escapeHtml(matchTime)}</span>
                    <span><strong>Amount:</strong> ${escapeHtml(ApiClient.formatMoney(amount))}</span>
                </div>

                <div class="inline-actions">
                    <button class="btn btn-primary" data-pay-reservation="${escapeHtml(reservationId)}" data-amount="${escapeHtml(amount)}" type="button">Pay</button>
                    <button class="btn btn-outline" data-penalty-reservation="${escapeHtml(reservationId)}" type="button">Penalty</button>
                    <button class="btn btn-danger" data-cancel-reservation="${escapeHtml(reservationId)}" type="button">Cancel</button>
                </div>
            </article>
        `;
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

    function normalizeBookings(response) {
        if (!response) {
            return getMockBookings();
        }

        if (Array.isArray(response)) {
            return response;
        }

        if (Array.isArray(response.data)) {
            return response.data;
        }

        if (response.data && Array.isArray(response.data.bookings)) {
            return response.data.bookings;
        }

        if (Array.isArray(response.bookings)) {
            return response.bookings;
        }

        return getMockBookings();
    }

    function getMockBookings() {
        return [
            {
                reservationId: 101,
                reservationStatus: "PENDING_PAYMENT",
                amount: 1500000,
                matchTitle: "Persepolis vs Esteghlal",
                matchStartTime: "2026-07-30T18:00:00",
                seatNumber: "A-12"
            },
            {
                reservationId: 102,
                reservationStatus: "PAID",
                amount: 500000,
                matchTitle: "Paykan vs Saipa",
                matchStartTime: "2026-08-01T16:00:00",
                seatNumber: "B-20"
            }
        ];
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
