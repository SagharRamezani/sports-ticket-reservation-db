(function () {
  "use strict";

  var STORAGE_KEY = "sports_ticket_theme_mode";
  var DEFAULT_MODE = "auto";

  function getHour() {
    return new Date().getHours();
  }

  function getLocalTimeText() {
    return new Date().toLocaleTimeString([], {
      hour: "2-digit",
      minute: "2-digit"
    });
  }

  function getAutoTheme() {
    var hour = getHour();
    return hour >= 7 && hour < 19 ? "light" : "dark";
  }

  function getSavedMode() {
    try {
      return localStorage.getItem(STORAGE_KEY) || DEFAULT_MODE;
    } catch (error) {
      return DEFAULT_MODE;
    }
  }

  function saveMode(mode) {
    try {
      localStorage.setItem(STORAGE_KEY, mode);
    } catch (error) {
      // Ignore localStorage failures.
    }
  }

  function resolveTheme(mode) {
    if (mode === "light" || mode === "dark") {
      return mode;
    }
    return getAutoTheme();
  }

  function applyTheme(mode, silent) {
    var resolved = resolveTheme(mode);
    document.documentElement.setAttribute("data-theme", resolved);
    document.documentElement.setAttribute("data-theme-mode", mode);

    var meta = document.querySelector('meta[name="theme-color"]');
    if (!meta) {
      meta = document.createElement("meta");
      meta.setAttribute("name", "theme-color");
      document.head.appendChild(meta);
    }
    meta.setAttribute("content", resolved === "dark" ? "#0b1120" : "#f3f6fb");

    updateControl(mode, resolved);

    if (!silent) {
      showToast("Theme set to " + modeLabel(mode, resolved));
    }
  }

  function modeLabel(mode, resolved) {
    if (mode === "auto") {
      return "Auto (" + resolved + " by local time)";
    }
    return mode.charAt(0).toUpperCase() + mode.slice(1);
  }

  function findHeaderTarget() {
    return (
      document.querySelector("header nav") ||
      document.querySelector("nav") ||
      document.querySelector("header") ||
      document.body
    );
  }

  function buildControl() {
    if (document.querySelector(".theme-control")) {
      return;
    }

    var target = findHeaderTarget();

    var wrapper = document.createElement("label");
    wrapper.className = "theme-control";
    wrapper.setAttribute("title", "Theme can be light, dark, or synchronized with local time");

    var icon = document.createElement("span");
    icon.className = "theme-icon";
    icon.textContent = "◐";

    var select = document.createElement("select");
    select.setAttribute("aria-label", "Theme mode");

    [
      ["auto", "Auto"],
      ["light", "Light"],
      ["dark", "Dark"]
    ].forEach(function (item) {
      var option = document.createElement("option");
      option.value = item[0];
      option.textContent = item[1];
      select.appendChild(option);
    });

    var clock = document.createElement("span");
    clock.className = "theme-clock";

    wrapper.appendChild(icon);
    wrapper.appendChild(select);
    wrapper.appendChild(clock);

    target.appendChild(wrapper);

    select.addEventListener("change", function () {
      var mode = select.value;
      saveMode(mode);
      applyTheme(mode, false);
    });
  }

  function updateControl(mode, resolved) {
    var control = document.querySelector(".theme-control");
    if (!control) return;

    var select = control.querySelector("select");
    var icon = control.querySelector(".theme-icon");
    var clock = control.querySelector(".theme-clock");

    if (select) {
      select.value = mode;
    }

    if (icon) {
      icon.textContent = resolved === "dark" ? "☾" : "☀";
    }

    if (clock) {
      clock.textContent = mode === "auto" ? getLocalTimeText() : "";
    }
  }

  function showToast(message) {
    var toast = document.querySelector(".theme-toast");

    if (!toast) {
      toast = document.createElement("div");
      toast.className = "theme-toast";
      document.body.appendChild(toast);
    }

    toast.textContent = message;
    toast.classList.add("is-visible");

    window.clearTimeout(showToast.timer);
    showToast.timer = window.setTimeout(function () {
      toast.classList.remove("is-visible");
    }, 2200);
  }

  function startAutoSync() {
    window.setInterval(function () {
      var mode = getSavedMode();

      if (mode === "auto") {
        applyTheme("auto", true);
      }
    }, 60000);
  }

  document.addEventListener("DOMContentLoaded", function () {
    buildControl();
    applyTheme(getSavedMode(), true);
    startAutoSync();
  });

  window.Phase4Theme = {
    apply: function (mode) {
      saveMode(mode);
      applyTheme(mode, false);
    },
    getMode: getSavedMode,
    getAutoTheme: getAutoTheme
  };
})();
