# Phase 4 UI Theme System

This document explains the final light/dark/auto theme polish added for the phase 4 demo.

## Files

```text
frontend/css/theme.css
frontend/js/theme.js
tools/apply_phase4_theme.ps1
```

## Modes

The UI supports three modes:

```text
Light
Dark
Auto
```

## Auto Mode

Auto mode is synchronized with the user's local device time.

```text
07:00 - 18:59 -> Light theme
19:00 - 06:59 -> Dark theme
```

The selected mode is saved in localStorage:

```text
sports_ticket_theme_mode
```

## Visual Polish

The theme CSS improves:

```text
Dark-mode contrast
Hero section readability
Card contrast
Muted text readability
Button contrast
Debug/API response readability
Navbar theme control styling
```

## Notes

This feature only changes frontend presentation.

It does not change:

```text
Backend Java code
PostgreSQL schema
Elasticsearch logic
API contracts
```
