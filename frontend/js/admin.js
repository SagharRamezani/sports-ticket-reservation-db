// Phase 4 Frontend - Admin / Support Report Dashboard

(function () {
    const mockReports = [
        {
            reportId: 1,
            reportTitle: "Payment was not confirmed",
            reportText: "User paid for a reservation but the booking is still pending.",
            reportStatus: "OPEN",
            reportCategory: "PAYMENT",
            reporterName: "Demo User",
            ticketId: 1,
            reservationId: 101,
            createdAt: "2026-07-27T12:00:00"
        },
        {
            reportId: 2,
            reportTitle: "Wrong seat information",
            reportText: "Seat number shown on the ticket detail page is different from booking history.",
            reportStatus: "IN_PROGRESS",
            reportCategory: "TICKET",
            reporterName: "Sara Ahmadi",
            ticketId: 2,
            reservationId: 102,
            createdAt: "2026-07-28T09:30:00"
        },
        {
            reportId: 3,
            reportTitle: "Cancellation penalty question",
            reportText: "User asks why the cancellation penalty is high.",
            reportStatus: "RESOLVED",
            reportCategory: "RESERVATION",
            reporterName: "Ali Karimi",
            ticketId: 3,
            reservationId: 103,
            createdAt: "2026-07-29T18:45:00"
        }
    ];

    document.addEventListener("DOMContentLoaded", function () {
        bindAdminEvents();
        loadReports();
    });

    function bindAdminEvents() {
        const loadButton = document.getElementById("loadReportsButton");
        const mockButton = document.getElementById("loadMockReportsButton");
        const updateForm = document.getElementById("updateReportForm");
        const resetButton = document.getElementById("resetUpdateFormButton");
        const logoutButton = document.getElementById("logoutButton");

        if (loadButton) {
            loadButton.addEventListener("click", loadReports);
        }

        if (mockButton) {
            mockButton.addEventListener("click", function () {
                renderReports(mockReports);
                ApiClient.showMessage("adminMessage", "Mock reports loaded.", "success");
            });
        }

        if (updateForm) {
            updateForm.addEventListener("submit", async function (event) {
                event.preventDefault();
                await updateReport(updateForm);
            });
        }

        if (resetButton) {
            resetButton.addEventListener("click", function () {
                if (updateForm) {
                    updateForm.reset();
                }
                setResult("adminActionResult", "No admin action yet.");
            });
        }

        if (logoutButton) {
            logoutButton.addEventListener("click", function () {
                ApiClient.clearToken();
                window.location.href = "login.html";
            });
        }
    }

    async function loadReports() {
        const list = document.getElementById("reportsList");

        if (list) {
            list.innerHTML = "<div class=\"alert\">Loading admin reports...</div>";
        }

        try {
            const response = await ApiClient.get("/api/admin/reports", { useMockOnError: true });
            const reports = normalizeReports(response);
            renderReports(reports);
            ApiClient.showMessage("adminMessage", "Reports loaded successfully.", "success");
        } catch (error) {
            renderReports(mockReports);
            ApiClient.showMessage("adminMessage", "Admin reports API failed. Mock reports are shown.", "error");
        }
    }

    async function updateReport(form) {
        const payload = readForm(form);
        const reportId = payload.reportId;

        if (!reportId) {
            ApiClient.showMessage("adminMessage", "Report ID is required.", "error");
            return;
        }

        delete payload.reportId;

        setResult("adminActionResult", "Updating report...");

        try {
            const response = await ApiClient.patch(
                "/api/admin/reports/" + encodeURIComponent(reportId),
                payload,
                { useMockOnError: true }
            );

            setResult("adminActionResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("adminMessage", "Report updated successfully.", "success");
            await loadReports();
        } catch (error) {
            setResult("adminActionResult", "Update failed: " + error.message);
            ApiClient.showMessage("adminMessage", "Report update failed: " + error.message, "error");
        }
    }

    function renderReports(reports) {
        const container = document.getElementById("reportsList");
        const title = document.getElementById("adminTitle");
        const meta = document.getElementById("adminMeta");

        if (!container) {
            return;
        }

        if (title) {
            title.textContent = reports.length + " report" + (reports.length === 1 ? "" : "s") + " found";
        }

        if (meta) {
            meta.textContent = "Reports loaded from backend API or mock fallback.";
        }

        if (!reports.length) {
            container.innerHTML = "<div class=\"alert\">No reports found.</div>";
            return;
        }

        container.innerHTML = reports.map(renderReportCard).join("");

        container.querySelectorAll("[data-fill-report]").forEach(function (button) {
            button.addEventListener("click", function () {
                const reportId = button.dataset.fillReport;
                const status = button.dataset.status || "IN_PROGRESS";
                fillUpdateForm(reportId, status);
            });
        });

        container.querySelectorAll("[data-resolve-report]").forEach(function (button) {
            button.addEventListener("click", async function () {
                const reportId = button.dataset.resolveReport;
                fillUpdateForm(reportId, "RESOLVED");
                await updateReportFromCard(reportId, "RESOLVED");
            });
        });
    }

    function renderReportCard(report) {
        const reportId = safe(report.reportId || report.report_id);
        const title = safe(report.reportTitle || report.report_title || report.title || "Report");
        const text = safe(report.reportText || report.report_text || report.description || "-");
        const status = safe(report.reportStatus || report.report_status || "OPEN");
        const category = safe(report.reportCategory || report.report_category || report.category || "-");
        const reporter = safe(report.reporterName || report.reporter_name || report.userName || report.user_name || "-");
        const ticketId = safe(report.ticketId || report.ticket_id || "-");
        const reservationId = safe(report.reservationId || report.reservation_id || "-");
        const createdAt = ApiClient.formatDateTime(report.createdAt || report.created_at);

        return `
            <article class="admin-card">
                <div class="inline-actions" style="justify-content: space-between;">
                    <span class="badge ${statusClass(status)}">${escapeHtml(status)}</span>
                    <span class="badge info">${escapeHtml(category)}</span>
                </div>

                <h3>${escapeHtml(title)}</h3>
                <p>${escapeHtml(text)}</p>

                <div class="meta-list">
                    <span><strong>Report ID:</strong> ${escapeHtml(reportId)}</span>
                    <span><strong>Reporter:</strong> ${escapeHtml(reporter)}</span>
                    <span><strong>Ticket ID:</strong> ${escapeHtml(ticketId)}</span>
                    <span><strong>Reservation ID:</strong> ${escapeHtml(reservationId)}</span>
                    <span><strong>Created:</strong> ${escapeHtml(createdAt)}</span>
                    <span><strong>Status:</strong> ${escapeHtml(status)}</span>
                </div>

                <div class="inline-actions">
                    <button class="btn btn-outline" data-fill-report="${escapeHtml(reportId)}" data-status="${escapeHtml(status)}" type="button">Edit</button>
                    <button class="btn btn-primary" data-resolve-report="${escapeHtml(reportId)}" type="button">Mark resolved</button>
                </div>
            </article>
        `;
    }

    async function updateReportFromCard(reportId, status) {
        setResult("adminActionResult", "Updating report " + reportId + "...");

        try {
            const response = await ApiClient.patch(
                "/api/admin/reports/" + encodeURIComponent(reportId),
                {
                    reportStatus: status,
                    adminNote: "Updated from admin dashboard"
                },
                { useMockOnError: true }
            );

            setResult("adminActionResult", JSON.stringify(response, null, 2));
            ApiClient.showMessage("adminMessage", "Report " + reportId + " updated.", "success");
        } catch (error) {
            setResult("adminActionResult", "Update failed: " + error.message);
            ApiClient.showMessage("adminMessage", "Report update failed.", "error");
        }
    }

    function fillUpdateForm(reportId, status) {
        setValue("reportId", reportId);
        setValue("reportStatus", status || "IN_PROGRESS");
        setValue("adminNote", "Checked by support admin");
        setValue("assignedTo", "support-admin");
    }

    function normalizeReports(response) {
        if (!response) {
            return mockReports;
        }

        if (Array.isArray(response)) {
            return response;
        }

        if (Array.isArray(response.data)) {
            return response.data;
        }

        if (response.data && Array.isArray(response.data.reports)) {
            return response.data.reports;
        }

        if (Array.isArray(response.reports)) {
            return response.reports;
        }

        return mockReports;
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

    function setValue(id, value) {
        const element = document.getElementById(id);

        if (element && value !== null && value !== undefined) {
            element.value = value;
        }
    }

    function setResult(id, value) {
        const element = document.getElementById(id);

        if (element) {
            element.textContent = value;
        }
    }

    function statusClass(status) {
        const normalized = String(status || "").toUpperCase();

        if (normalized === "RESOLVED") {
            return "info";
        }

        if (normalized === "REJECTED") {
            return "danger";
        }

        if (normalized === "IN_PROGRESS") {
            return "warning";
        }

        return "";
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
