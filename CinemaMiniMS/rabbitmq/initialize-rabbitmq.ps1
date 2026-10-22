[CmdletBinding()]
param([switch]$ValidateOnly)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$brokerDirectory = $PSScriptRoot
$localEnv = Join-Path $brokerDirectory '.env'
$exampleEnv = Join-Path $brokerDirectory '.env.example'
$composeFile = Join-Path $brokerDirectory 'docker-compose.yml'
$transcriptStarted = $false

function Get-BrokerConfiguration {
    $envFile = if (Test-Path -LiteralPath $localEnv -PathType Leaf) { $localEnv } else { $exampleEnv }
    foreach ($required in @($composeFile, $envFile)) {
        if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Missing file: $required" }
    }
    $composeArguments = @('compose', '--project-name', 'cinema-rabbitmq', '--env-file', $envFile, '--file', $composeFile)
    $json = (& $script:dockerExecutable @composeArguments config --format json | Out-String)
    if ($LASTEXITCODE -ne 0) { throw 'Invalid RabbitMQ Compose configuration.' }
    $configuration = $json | ConvertFrom-Json
    $serviceNames = @($configuration.services.PSObject.Properties.Name)
    if ($serviceNames.Count -ne 1 -or 'rabbitmq' -notin $serviceNames) {
        throw 'This Compose project must contain RabbitMQ only.'
    }
    $broker = $configuration.services.rabbitmq
    if ($broker.image -notmatch ':[^/:]+$' -or $broker.image -match ':latest$') {
        throw 'Use a pinned RabbitMQ image tag.'
    }
    foreach ($key in @('RABBITMQ_DEFAULT_USER', 'RABBITMQ_DEFAULT_PASS', 'RABBITMQ_DEFAULT_VHOST')) {
        if ([string]::IsNullOrWhiteSpace([string]$broker.environment.$key)) { throw "Missing setting: $key" }
    }
    $amqpPorts = @($broker.ports | Where-Object { [int]$_.target -eq 5672 })
    $managementPorts = @($broker.ports | Where-Object { [int]$_.target -eq 15672 })
    if ($broker.ports.Count -ne 2 -or $amqpPorts.Count -ne 1 -or $managementPorts.Count -ne 1) {
        throw 'RabbitMQ requires one AMQP port and one Management port mapping.'
    }
    foreach ($port in $broker.ports) {
        if ($port.host_ip -ne '127.0.0.1' -or [int]$port.published -lt 1 -or [int]$port.published -gt 65535) {
            throw 'RabbitMQ ports must bind to localhost with valid port numbers.'
        }
    }
    if ($amqpPorts[0].published -eq $managementPorts[0].published) { throw 'RabbitMQ host ports must differ.' }
    return [pscustomobject]@{
        ComposeArguments = $composeArguments
        AmqpPort = [int]$amqpPorts[0].published
        ManagementPort = [int]$managementPorts[0].published
    }
}

try {
    $script:dockerExecutable = (Get-Command docker -ErrorAction Stop).Source
    $composeVersion = (& $script:dockerExecutable compose version --short | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $composeVersion -notmatch '(\d+\.\d+\.\d+)') { throw 'Docker Compose v2 is required.' }
    if ([version]$Matches[1] -lt [version]'2.20.0') { throw 'Docker Compose 2.20 or newer is required.' }
    $broker = Get-BrokerConfiguration
    Write-Host "CONFIG OK: RabbitMQ only, AMQP $($broker.AmqpPort), Management $($broker.ManagementPort)"
    if ($ValidateOnly) {
        Write-Host 'Validation only: no container, .env file or volume was created or changed.'
        exit 0
    }
    $serverVersion = (& $script:dockerExecutable info --format '{{.ServerVersion}}' | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $serverVersion) { throw 'Start Docker Desktop with Linux containers, then retry.' }
    $logDirectory = Join-Path $brokerDirectory 'logs'
    New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null
    $stamp = [datetime]::UtcNow.ToString('yyyyMMdd-HHmmss') + 'Z'
    Start-Transcript -LiteralPath (Join-Path $logDirectory "initialize-rabbitmq-$stamp.log") | Out-Null
    $transcriptStarted = $true
    if (-not (Test-Path -LiteralPath $localEnv)) {
        Copy-Item -LiteralPath $exampleEnv -Destination $localEnv
        Write-Host 'Created rabbitmq/.env from .env.example.'
    }
    $broker = Get-BrokerConfiguration
    Write-Host 'START: RabbitMQ only; waiting for health check...'
    $cliArguments = @($broker.ComposeArguments) + @('up', '-d', '--wait', '--wait-timeout', '180', '--no-deps', 'rabbitmq')
    & $script:dockerExecutable @cliArguments
    if ($LASTEXITCODE -ne 0) { throw 'RabbitMQ startup failed. Check Docker logs and port conflicts.' }
    Write-Host 'SUCCESS: RabbitMQ is ready.'
    Write-Host "AMQP: 127.0.0.1:$($broker.AmqpPort)"
    Write-Host "Management: http://localhost:$($broker.ManagementPort)"
    Write-Host 'Credentials and virtual host: rabbitmq/.env. Passwords are not printed.'
    Write-Host 'PostgreSQL, SQL, application services and frontend were not started. Future services declare the Saga queues.'
    exit 0
}
catch {
    Write-Host ("ERROR: " + $_.Exception.Message) -ForegroundColor Red
    Write-Host 'No automatic reset or volume deletion was performed. Fix the issue and run again.'
    exit 1
}
finally {
    if ($transcriptStarted) { Stop-Transcript | Out-Null }
}
