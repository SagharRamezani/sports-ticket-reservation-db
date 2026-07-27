(function () {
  "use strict";

  function textOf(element) {
    return (element && element.textContent ? element.textContent : "").trim();
  }

  function lowerText(element) {
    return textOf(element).toLowerCase();
  }

  function closestPanel(element) {
    if (!element) return null;
    return element.closest(".card, .panel, .section-card, section, article, form, div");
  }

  function isJsonLike(text) {
    var trimmed = (text || "").trim();
    return (
      trimmed.startsWith("{") ||
      trimmed.startsWith("[") ||
      trimmed.includes('"success"') ||
      trimmed.includes('"apiBaseUrl"') ||
      trimmed.includes('"token"') ||
      trimmed.includes('"currentUser"')
    );
  }

  function wrapDebugBlocks() {
    var preBlocks = Array.from(document.querySelectorAll("pre"));

    preBlocks.forEach(function (pre) {
      if (pre.closest("details")) return;
      if (!isJsonLike(pre.textContent)) return;

      var details = document.createElement("details");
      details.className = "api-response-details";

      var summary = document.createElement("summary");
      summary.textContent = "View API response";

      pre.parentNode.insertBefore(details, pre);
      details.appendChild(summary);
      details.appendChild(pre);
    });
  }

  function polishAuthPage() {
    var pageText = lowerText(document.body);
    if (!pageText.includes("login") || !pageText.includes("signup") || !pageText.includes("otp")) {
      return;
    }

    var headings = Array.from(document.querySelectorAll("h2, h3"));
    var panels = {
      login: null,
      signup: null,
      otp: null
    };

    headings.forEach(function (heading) {
      var label = lowerText(heading);
      var panel = closestPanel(heading);

      if (!panel) return;

      if (label === "login" || label.includes("login")) {
        panels.login = panel;
      } else if (label.includes("signup") || label.includes("create account")) {
        panels.signup = panel;
      } else if (label.includes("otp")) {
        panels.otp = panel;
      }
    });

    if (!panels.login || !panels.signup || !panels.otp) {
      return;
    }

    Object.keys(panels).forEach(function (key) {
      panels[key].setAttribute("data-auth-panel", key);
    });

    var switcher = document.querySelector(".phase4-auth-switcher");
    if (!switcher) {
      switcher = document.createElement("div");
      switcher.className = "phase4-auth-switcher";

      var loginButton = document.createElement("button");
      loginButton.type = "button";
      loginButton.textContent = "Login";
      loginButton.setAttribute("data-auth-tab", "login");

      var signupButton = document.createElement("button");
      signupButton.type = "button";
      signupButton.textContent = "Signup";
      signupButton.setAttribute("data-auth-tab", "signup");

      var otpButton = document.createElement("button");
      otpButton.type = "button";
      otpButton.textContent = "OTP";
      otpButton.setAttribute("data-auth-tab", "otp");

      switcher.appendChild(loginButton);
      switcher.appendChild(signupButton);
      switcher.appendChild(otpButton);

      panels.login.parentNode.insertBefore(switcher, panels.login);
    }

    function showPanel(name) {
      Object.keys(panels).forEach(function (key) {
        if (!panels[key]) return;
        panels[key].classList.toggle("phase4-hidden", key !== name);
        panels[key].classList.toggle("phase4-active-panel", key === name);
      });

      Array.from(switcher.querySelectorAll("[data-auth-tab]")).forEach(function (button) {
        var active = button.getAttribute("data-auth-tab") === name;
        button.classList.toggle("active", active);
        button.setAttribute("aria-pressed", active ? "true" : "false");
      });
    }

    switcher.addEventListener("click", function (event) {
      var target = event.target.closest("[data-auth-tab]");
      if (!target) return;
      showPanel(target.getAttribute("data-auth-tab"));
    });

    showPanel("login");
  }

  function polishNavigation() {
    var token = "";
    try {
      token = localStorage.getItem("sports_ticket_auth_token") || "";
    } catch (error) {
      token = "";
    }

    var links = Array.from(document.querySelectorAll("a, button"));
    links.forEach(function (item) {
      var label = lowerText(item);

      if (label === "login" || label.includes("login")) {
        item.classList.toggle("phase4-hidden", Boolean(token));
      }

      if (label === "logout" || label.includes("logout")) {
        item.classList.toggle("phase4-hidden", !token);
      }
    });
  }

  function addCancelConfirmation() {
    document.addEventListener("click", function (event) {
      var button = event.target.closest("button, a");
      if (!button) return;

      var label = lowerText(button);
      var isCancelAction = label === "cancel" || label.includes("cancel reservation");

      if (!isCancelAction) return;

      var confirmed = window.confirm("Are you sure you want to cancel this reservation?");
      if (!confirmed) {
        event.preventDefault();
        event.stopPropagation();
      }
    }, true);
  }

  function polishAdminNotes() {
    var headings = Array.from(document.querySelectorAll("h2, h3"));
    headings.forEach(function (heading) {
      if (!lowerText(heading).includes("frontend-only admin page")) return;

      heading.textContent = "Integration notes";
      var panel = closestPanel(heading);
      if (!panel || panel.closest("details")) return;

      var details = document.createElement("details");
      details.className = "integration-notes";

      var summary = document.createElement("summary");
      summary.textContent = "View integration notes";

      panel.parentNode.insertBefore(details, panel);
      details.appendChild(summary);
      details.appendChild(panel);
    });
  }

  function polishSearchSourceText() {
    Array.from(document.querySelectorAll("*")).forEach(function (node) {
      if (!node.childNodes || node.childNodes.length !== 1) return;

      var current = textOf(node);
      if (current === "mock fallback") {
        node.textContent = "Mock fallback";
      }
    });
  }

  function autoHealthHint() {
    var buttons = Array.from(document.querySelectorAll("button"));
    var healthButton = buttons.find(function (button) {
      return lowerText(button).includes("check health");
    });

    if (!healthButton) return;

    healthButton.setAttribute("title", "Checks GET /api/health on the Java backend");
  }

  document.addEventListener("DOMContentLoaded", function () {
    document.body.classList.add("phase4-polished");

    wrapDebugBlocks();
    polishAuthPage();
    polishNavigation();
    addCancelConfirmation();
    polishAdminNotes();
    polishSearchSourceText();
    autoHealthHint();
  });
})();
