$ErrorActionPreference = "Stop"

$pages = @(
  "frontend/index.html",
  "frontend/login.html",
  "frontend/tickets.html",
  "frontend/ticket-detail.html",
  "frontend/profile.html",
  "frontend/bookings.html",
  "frontend/admin.html"
)

$cssLink = '  <link rel="stylesheet" href="css/ui-polish.css">'
$scriptTag = '  <script src="js/ui-polish.js"></script>'

foreach ($page in $pages) {
  if (!(Test-Path $page)) {
    Write-Host "Skipping missing page: $page"
    continue
  }

  $content = Get-Content $page -Raw

  if ($content -notmatch [regex]::Escape('css/ui-polish.css')) {
    if ($content -match '</head>') {
      $content = $content -replace '</head>', "$cssLink`r`n</head>"
    } else {
      Write-Host "No </head> found in $page"
    }
  }

  if ($content -notmatch [regex]::Escape('js/ui-polish.js')) {
    if ($content -match '</body>') {
      $content = $content -replace '</body>', "$scriptTag`r`n</body>"
    } else {
      Write-Host "No </body> found in $page"
    }
  }

  Set-Content -Path $page -Value $content -Encoding UTF8
  Write-Host "Updated $page"
}
