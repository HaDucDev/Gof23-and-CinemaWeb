[CmdletBinding()]
param(
    [string]$StartDate = '',
    [switch]$ValidateOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$stackRoot = Split-Path -Parent $PSScriptRoot
$composeFile = Join-Path $stackRoot 'docker-compose.yml'
$stackName = 'Orchestration'
$databaseName = 'cinema_orchestration'
$hostPort = 50102
$projectName = 'cinema-orch'
$transcriptStarted = $false

function Assert-DateFormat {
    param([string]$Value)
    $parsedDate = [datetime]::MinValue
    if (-not [datetime]::TryParseExact($Value, 'yyyy-MM-dd',
        [Globalization.CultureInfo]::InvariantCulture, [Globalization.DateTimeStyles]::None, [ref]$parsedDate)) {
        throw 'StartDate must be a valid date in YYYY-MM-DD format.'
    }
}

function Invoke-Compose {
    param([string[]]$CommandArguments)
    & $script:dockerExecutable @script:composeArguments @CommandArguments
    if ($LASTEXITCODE -ne 0) { throw "Docker Compose command failed for $stackName." }
}

function Invoke-Psql {
    param([string[]]$PsqlArguments, [switch]$Capture)
    $arguments = @($script:composeArguments) + @('exec', '-T', 'postgres', 'psql', '-X',
        '--username', $script:adminUser, '--dbname', $databaseName, '--set=ON_ERROR_STOP=1') + $PsqlArguments
    if ($Capture) {
        $result = (& $script:dockerExecutable @arguments | Out-String)
        if ($LASTEXITCODE -ne 0) { throw "PostgreSQL query failed for $stackName." }
        return $result.Trim()
    }
    & $script:dockerExecutable @arguments
    if ($LASTEXITCODE -ne 0) { throw "SQL initialization failed for $stackName." }
}

try {
    $script:dockerExecutable = (Get-Command docker -ErrorAction Stop).Source
    $composeVersion = (& $script:dockerExecutable compose version --short | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $composeVersion -notmatch '(\d+\.\d+\.\d+)') { throw 'Docker Compose v2 is required.' }
    if ([version]$Matches[1] -lt [version]'2.20.0') { throw 'Docker Compose 2.20 or newer is required.' }
    if ($StartDate) { Assert-DateFormat $StartDate }
    foreach ($required in @($composeFile) + @('00-roles.sql', '01-schema.sql', '02-seed.sql', '03-verify.sql', 'reset-data.sql' |
        ForEach-Object { Join-Path $PSScriptRoot $_ })) {
        if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Missing file: $required" }
    }
    $script:composeArguments = @('compose', '--project-name', $projectName, '--file', $composeFile)
    $json = (& $script:dockerExecutable @script:composeArguments config --format json | Out-String)
    if ($LASTEXITCODE -ne 0) { throw "Invalid Compose configuration: $stackName" }
    $configuration = $json | ConvertFrom-Json
    $serviceNames = @($configuration.services.PSObject.Properties.Name)
    if ($serviceNames.Count -ne 1 -or 'postgres' -notin $serviceNames) { throw 'This initializer supports PostgreSQL only.' }
    $pg = $configuration.services.postgres
    if ($pg.environment.POSTGRES_DB -ne $databaseName) { throw "Expected database $databaseName." }
    if ($pg.ports.Count -ne 1 -or [int]$pg.ports[0].published -ne $hostPort -or [int]$pg.ports[0].target -ne 5432) {
        throw "Expected PostgreSQL host port $hostPort mapped to container port 5432."
    }
    if ($pg.image -notmatch ':[^/:]+$' -or $pg.image -match ':latest$') { throw 'Use a pinned PostgreSQL image tag.' }
    foreach ($setting in @('POSTGRES_USER', 'POSTGRES_PASSWORD', 'ACCOUNT_DB_PASSWORD', 'BOOKING_DB_PASSWORD', 'SEAT_DB_PASSWORD', 'PAYMENT_DB_PASSWORD')) {
        if ([string]::IsNullOrWhiteSpace([string]$pg.environment.$setting)) { throw "Missing setting: $setting" }
    }
    $script:adminUser = [string]$pg.environment.POSTGRES_USER
    Write-Host "CONFIG OK: $stackName only, database $databaseName, host port $hostPort"
    if ($ValidateOnly) {
        Write-Host 'Validation only: no container, database, configuration, log or volume was changed.'
        exit 0
    }
    $serverVersion = (& $script:dockerExecutable info --format '{{.ServerVersion}}' | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $serverVersion) { throw 'Start Docker Desktop with Linux containers, then retry.' }
    $logDirectory = Join-Path $stackRoot 'logs'
    New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
    $stamp = [datetime]::UtcNow.ToString('yyyyMMdd-HHmmss') + 'Z'
    Start-Transcript -LiteralPath (Join-Path $logDirectory "initialize-data-$stamp.log") | Out-Null
    $transcriptStarted = $true
    Invoke-Compose @('up', '-d', '--wait', '--wait-timeout', '180', '--no-deps', 'postgres')
    $olderLayout = Invoke-Psql @('--tuples-only', '--no-align', '--command',
        "SELECT to_regclass('booking.bookings') IS NOT NULL AND (to_regclass('booking.schema_version') IS NOT NULL OR NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='seat' AND table_name='seat_holds' AND column_name='seat_count') OR NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_schema='booking' AND table_name='bookings' AND column_name='saga_lease_token'));") -Capture
    if ($olderLayout -eq 't') { throw 'An older demo layout exists. Use a fresh database; no automatic upgrade/reset was performed.' }
    foreach ($sqlFile in @('00-roles.sql', '01-schema.sql')) {
        Write-Host "SQL: $stackName/$sqlFile"
        Invoke-Psql @('--file', "/opt/cinema/database/$sqlFile")
    }
    $savedDate = Invoke-Psql @('--tuples-only', '--no-align', '--command',
        "SELECT nullif(current_setting('cinema.seed_start_date', true), '');") -Capture
    $selectedDate = $StartDate
    if (-not $selectedDate) { $selectedDate = [string]$pg.environment.SEED_START_DATE }
    if (-not $selectedDate) { $selectedDate = $savedDate }
    if (-not $selectedDate) {
        $nextDemoDay = [datetime]::UtcNow.AddHours(7).Date.AddDays(1)
        $firstDemoDay = [datetime]::new(2026, 11, 1)
        $selectedDate = if ($nextDemoDay -gt $firstDemoDay) { $nextDemoDay.ToString('yyyy-MM-dd') } else { '2026-11-01' }
    }
    Assert-DateFormat $selectedDate
    if ($savedDate -and $savedDate -ne $selectedDate) {
        throw "This database was seeded for $savedDate. Its existing schedule will not be replaced."
    }
    Write-Host "SEED DATE: $selectedDate (Asia/Ho_Chi_Minh); applies to $stackName only."
    Invoke-Psql @("--set=seed_start_date=$selectedDate", '--file', '/opt/cinema/database/02-seed.sql')
    Invoke-Psql @('--file', '/opt/cinema/database/03-verify.sql')
    Write-Host "SUCCESS: $stackName PostgreSQL and SQL fixtures are ready."
    Write-Host 'No other stack, RabbitMQ, backend or frontend was started. Existing data was preserved.'
    exit 0
}
catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    Write-Host 'No automatic reset or volume deletion was performed.'
    exit 1
}
finally {
    if ($transcriptStarted) { Stop-Transcript | Out-Null }
}
