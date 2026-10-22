[CmdletBinding()]
param(
    [ValidateSet('Build','Start','Stop')][string]$Action = 'Start',
    [ValidateSet('All','Account','Seat','Payment','Booking')][string]$Service = 'All',
    [string]$JavaHome = $env:JAVA_HOME,
    [switch]$ValidateOnly
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$backendRoot = $PSScriptRoot
$stackRoot = Split-Path -Parent $backendRoot
$projectRoot = Split-Path -Parent $stackRoot
$stateFile = Join-Path $backendRoot 'run/services.json'
$modules = @(
    @{Name='account';Port=8091},
    @{Name='seat';Port=8092},
    @{Name='payment';Port=8093},
    @{Name='booking';Port=8082}
)
$selectedModules = @($modules | Where-Object { $Service -eq 'All' -or $_.Name -eq $Service })
function Save-State($Entries) {
    if (@($Entries).Count -eq 0) {
        if (Test-Path -LiteralPath $stateFile) { Remove-Item -LiteralPath $stateFile }
    } else {
        ConvertTo-Json -InputObject @($Entries) | Set-Content -LiteralPath $stateFile -Encoding UTF8
    }
}
function Read-Settings([string]$Path) {
    $values = @{}
    foreach ($line in [IO.File]::ReadAllLines($Path)) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith('#')) { continue }
        if ($trimmed -notmatch '^([A-Za-z_][A-Za-z0-9_]*)=(.*)$') { throw "Invalid settings line in $Path" }
        $settingName = $Matches[1]; $settingValue = $Matches[2]
        if ($settingValue.Length -ge 2 -and (($settingValue.StartsWith('"') -and $settingValue.EndsWith('"')) -or ($settingValue.StartsWith("'") -and $settingValue.EndsWith("'")))) {
            $settingValue = $settingValue.Substring(1,$settingValue.Length-2)
        }
        $values[$settingName] = $settingValue
    }
    return $values
}
function Get-LocalServiceSettings {
    $settingsByService = @{}
    foreach ($module in $selectedModules) {
        $relativePath = "orchestration/backend/$($module.Name)-service/.env"
        $configurationFile = Join-Path $backendRoot "$($module.Name)-service/.env"
        if (-not (Test-Path -LiteralPath $configurationFile -PathType Leaf)) { throw "Missing service configuration: $relativePath" }
        $configuration = Read-Settings $configurationFile
        # Each independent service owns its configuration and database password.
        $settings = @{}
        $allowed = @('DB_URL','DB_PASSWORD','INTERNAL_API_KEY','SERVICE_BIND_ADDRESS','SERVER_PORT','SPRING_PROFILES_ACTIVE')
        if ($module.Name -eq 'booking') { $allowed += @('ACCOUNT_SERVICE_URL','SEAT_SERVICE_URL','PAYMENT_SERVICE_URL','FRONTEND_ORIGINS','SESSION_COOKIE_SECURE','HTTP_CONNECT_TIMEOUT_MS','HTTP_READ_TIMEOUT_MS','STREAM_READ_TIMEOUT_MS') }
        $settings.SERVER_PORT = [string]$module.Port
        $settings.SPRING_PROFILES_ACTIVE = 'local'
        foreach ($name in $configuration.Keys) {
            if ($name -notin $allowed) { throw "Unsupported setting $name in $relativePath" }
            $settings[$name] = $configuration[$name]
        }
        foreach ($name in @('DB_URL','DB_PASSWORD')) {
            if (-not $settings.ContainsKey($name) -or [string]::IsNullOrWhiteSpace($settings[$name]) -or $settings[$name].StartsWith('CHANGE_ME')) { throw "Set $name in $relativePath" }
        }
        if (-not $settings.ContainsKey('INTERNAL_API_KEY') -or $settings.INTERNAL_API_KEY.Trim().Length -lt 24 -or $settings.INTERNAL_API_KEY.StartsWith('CHANGE_ME')) { throw "Set INTERNAL_API_KEY (at least 24 characters) in $relativePath" }
        $portNumber = 0
        if (-not [int]::TryParse($settings.SERVER_PORT, [ref]$portNumber) -or $portNumber -lt 1 -or $portNumber -gt 65535) { throw "Invalid SERVER_PORT in $relativePath" }
        $module.Port = $portNumber
        $settingsByService[$module.Name] = $settings
    }
    if (@($selectedModules.Port | Select-Object -Unique).Count -ne $selectedModules.Count) { throw 'Selected service ports must be distinct.' }
    return $settingsByService
}
function Get-OwnedProcess($Entry) {
    if ($Entry.name -notin @('account','seat','payment','booking')) { throw 'Unknown service in state file.' }
    $expectedJar = Join-Path $backendRoot "$($Entry.name)-service/target/$($Entry.name)-service-1.0.0.jar"
    if ([IO.Path]::GetFullPath($Entry.jar) -ne [IO.Path]::GetFullPath($expectedJar)) { throw 'Unexpected JAR in state file.' }
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$([int]$Entry.processId)" -ErrorAction SilentlyContinue
    if ($process -and ($process.Name -ne 'java.exe' -or -not $process.CommandLine -or $process.CommandLine.IndexOf($expectedJar,[StringComparison]::OrdinalIgnoreCase) -lt 0)) {
        throw 'PID belongs to another process; refusing to stop or replace it.'
    }
    return $process
}
function Build-Backend {
    $originalJava = $env:JAVA_HOME
    try {
        $env:JAVA_HOME = $JavaHome
        foreach ($module in $selectedModules) {
            $serviceRoot = Join-Path $backendRoot "$($module.Name)-service"
            Push-Location $serviceRoot
            try {
                Write-Host "BUILD: $($module.Name) as an independent Maven project"
                & (Join-Path $serviceRoot 'mvnw.cmd') -B -ntp '-Dmaven.test.skip=true' clean package
                if ($LASTEXITCODE -ne 0) { throw "Maven build failed: $($module.Name)." }
            } finally { Pop-Location }
        }
    } finally { $env:JAVA_HOME = $originalJava }
}
try {
    if ($Action -eq 'Stop') {
        if (-not (Test-Path -LiteralPath $stateFile)) { Write-Host 'No service state file; nothing to stop.'; exit 0 }
        $entries = Get-Content -LiteralPath $stateFile -Raw -Encoding UTF8 | ConvertFrom-Json
        $stopEntries = @($entries | Where-Object { $_.name -in $selectedModules.Name })
        $remainingEntries = @($entries | Where-Object { $_.name -notin $selectedModules.Name })
        [array]::Reverse($stopEntries)
        foreach ($entry in $stopEntries) {
            $owned = Get-OwnedProcess $entry
            if ($owned -and -not $ValidateOnly) { Stop-Process -Id ([int]$entry.processId) -ErrorAction Stop; Write-Host "Stopped $($entry.name)." }
        }
        if (-not $ValidateOnly) { Save-State $remainingEntries }
        exit 0
    }
    if (-not $JavaHome -or -not (Test-Path -LiteralPath (Join-Path $JavaHome 'bin/javac.exe'))) { throw 'Set JAVA_HOME or pass -JavaHome with a JDK 17 directory.' }
    $javaExecutable = Join-Path $JavaHome 'bin/java.exe'
    $versionInfo = New-Object System.Diagnostics.ProcessStartInfo
    $versionInfo.FileName = $javaExecutable; $versionInfo.Arguments = '-version'
    $versionInfo.UseShellExecute = $false; $versionInfo.CreateNoWindow = $true; $versionInfo.RedirectStandardError = $true
    $versionProcess = [Diagnostics.Process]::Start($versionInfo)
    $version = $versionProcess.StandardError.ReadToEnd(); $versionProcess.WaitForExit()
    if ($version -notmatch '(?:openjdk|java) version "17\.') { throw 'These services require JDK 17. Java 8 cannot run Spring Boot 4.' }
    if ($Action -eq 'Start') { $settingsByService = Get-LocalServiceSettings }
    if ($ValidateOnly) {
        foreach ($module in $selectedModules) {
            $serviceRoot = Join-Path $backendRoot "$($module.Name)-service"
            foreach ($file in @('pom.xml','mvnw','mvnw.cmd','.mvn/wrapper/maven-wrapper.properties','src/main/resources/application.yaml')) {
                if (-not (Test-Path -LiteralPath (Join-Path $serviceRoot $file))) { throw "Missing $file in $($module.Name)." }
            }
            [xml]$pom = Get-Content -LiteralPath (Join-Path $serviceRoot 'pom.xml') -Raw -Encoding UTF8
            if ($pom.project.parent.groupId -ne 'org.springframework.boot' -or $pom.project.parent.artifactId -ne 'spring-boot-starter-parent' -or $pom.project.parent.relativePath -ne '') { throw 'Service POM must inherit only the official Spring Boot parent with an empty relativePath.' }
            foreach ($dependency in $pom.project.dependencies.dependency) { if ($dependency.groupId -eq 'com.td.cinema') { throw 'Service cannot depend on another project service/library.' } }
        }
        Write-Host "CONFIG OK: $($selectedModules.Count) independent REST project(s), JDK 17, separate DB roles; no service or SQL was started."
        exit 0
    }
    $entries = @()
    if (Test-Path -LiteralPath $stateFile) {
        $entries = Get-Content -LiteralPath $stateFile -Raw -Encoding UTF8 | ConvertFrom-Json
        $activeEntries = @()
        foreach ($entry in $entries) {
            if (Get-OwnedProcess $entry) {
                if ($entry.name -in $selectedModules.Name) { throw "$($entry.name) is already running. Stop that service before rebuilding or starting it." }
                $activeEntries += $entry
            }
        }
        $entries = $activeEntries
    }
    Build-Backend
    if ($Action -eq 'Build') { Write-Host "SUCCESS: $($selectedModules.Count) independent executable JAR(s) built."; exit 0 }
    $listeningPorts = @([Net.NetworkInformation.IPGlobalProperties]::GetIPGlobalProperties().GetActiveTcpListeners() | Select-Object -ExpandProperty Port)
    foreach ($module in $selectedModules) { if ($module.Port -in $listeningPorts) { throw "Port $($module.Port) is already in use. Stop the existing application before starting this service." } }
    $runDirectory = Join-Path $backendRoot 'run'; New-Item -ItemType Directory -Path $runDirectory -Force | Out-Null
    $logDirectory = Join-Path $projectRoot 'logs/orchestration'; New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
    foreach ($module in $selectedModules) {
        $settings = $settingsByService[$module.Name]
        $jar = Join-Path $backendRoot "$($module.Name)-service/target/$($module.Name)-service-1.0.0.jar"
        $log = Join-Path $logDirectory "$($module.Name).log"
        $startInfo = New-Object System.Diagnostics.ProcessStartInfo
        $startInfo.FileName = $javaExecutable
        $startInfo.Arguments = '-Dlogging.file.name="' + $log + '" -jar "' + $jar + '"'
        $startInfo.WorkingDirectory = Join-Path $backendRoot "$($module.Name)-service"
        $startInfo.UseShellExecute = $false
        $startInfo.CreateNoWindow = $true
        # Remove inherited database administrator/other-service credentials before adding this service's password.
        foreach ($name in @('POSTGRES_PASSWORD','ACCOUNT_DB_PASSWORD','BOOKING_DB_PASSWORD','SEAT_DB_PASSWORD','PAYMENT_DB_PASSWORD','RABBITMQ_DEFAULT_PASS','DB_URL','DB_PASSWORD','SERVER_PORT','ACCOUNT_SERVICE_URL','SEAT_SERVICE_URL','PAYMENT_SERVICE_URL','FRONTEND_ORIGINS','INTERNAL_API_KEY','SERVICE_BIND_ADDRESS','SPRING_PROFILES_ACTIVE','SESSION_COOKIE_SECURE','HTTP_CONNECT_TIMEOUT_MS','HTTP_READ_TIMEOUT_MS','STREAM_READ_TIMEOUT_MS')) { $startInfo.EnvironmentVariables.Remove($name) }
        foreach ($name in $settings.Keys) { $startInfo.EnvironmentVariables[$name] = $settings[$name] }
        $process = [Diagnostics.Process]::Start($startInfo)
        $entries += [pscustomobject]@{ name=$module.Name; processId=$process.Id; jar=$jar }
        Save-State $entries
        $ready = $false
        for ($attempt = 0; $attempt -lt 40; $attempt++) {
            if ($process.HasExited) { throw "$($module.Name) exited. Read $log; stop already-started services with dung-services.bat." }
            try { $health = Invoke-RestMethod -Uri "http://127.0.0.1:$($module.Port)/internal/health" -Headers @{'X-Internal-Key'=$settings.INTERNAL_API_KEY} -TimeoutSec 2; if ($health.status -eq 'UP') { $ready=$true; break } } catch { }
            Start-Sleep -Milliseconds 500
        }
        if (-not $ready) { throw "$($module.Name) did not become ready. Read $log; use dung-services.bat before restarting." }
        Write-Host "READY: $($module.Name), port $($module.Port)"
    }
    Write-Host "SUCCESS: $($selectedModules.Count) selected REST service(s) ready; no database or RabbitMQ was started."
} catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    exit 1
}
