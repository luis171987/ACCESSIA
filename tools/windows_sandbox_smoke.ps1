param([Parameter(Mandatory=$true)][string]$Executable, [string]$OutputDirectory = 'sandbox-evidence')
$ErrorActionPreference = 'Stop'
New-Item -ItemType Directory -Force $OutputDirectory | Out-Null
Add-Type -AssemblyName UIAutomationClient, UIAutomationTypes, System.Drawing
$appProcess = Start-Process -FilePath (Resolve-Path $Executable) -PassThru
try {
    $deadline = [DateTime]::UtcNow.AddSeconds(60)
    do {
        Start-Sleep -Milliseconds 500
        $appProcess.Refresh()
        if ($appProcess.HasExited) { throw "ACCESSIA exited before opening a window (code $($appProcess.ExitCode))." }
    } while ($appProcess.MainWindowHandle -eq 0 -and [DateTime]::UtcNow -lt $deadline)
    if ($appProcess.MainWindowHandle -eq 0) { throw 'No Windows application window appeared.' }
    $window = [System.Windows.Automation.AutomationElement]::FromHandle($appProcess.MainWindowHandle)
    $statusCondition = [System.Windows.Automation.PropertyCondition]::new([System.Windows.Automation.AutomationElement]::AutomationIdProperty, 'AccessiaStatus')
    $addressCondition = [System.Windows.Automation.PropertyCondition]::new([System.Windows.Automation.AutomationElement]::NameProperty, 'Dirección o búsqueda')
    $readyDeadline = [DateTime]::UtcNow.AddSeconds(90)
    $description = ''
    $browserPrepared = $false
    do {
        $statusElement = $window.FindFirst([System.Windows.Automation.TreeScope]::Descendants, $statusCondition)
        if ($null -ne $statusElement) {
            $description = $statusElement.Current.Name
        }
        if ($description -like 'Estado: No se pudo preparar WebView2*') { throw $description }
        $addressElement = $window.FindFirst([System.Windows.Automation.TreeScope]::Descendants, $addressCondition)
        $browserPrepared = $null -ne $addressElement -and $addressElement.Current.IsEnabled
        if ($browserPrepared) { break }
        Start-Sleep -Milliseconds 500
        $appProcess.Refresh()
        if ($appProcess.HasExited) { throw 'ACCESSIA closed before WebView2 initialized.' }
    } while ([DateTime]::UtcNow -lt $readyDeadline)
    if (!$browserPrepared) { throw "The native window appeared, but WebView2 readiness was not verified. Last native status: $description" }
    $condition = [System.Windows.Automation.PropertyCondition]::new([System.Windows.Automation.AutomationElement]::NameProperty, 'Asistente local')
    $button = $window.FindFirst([System.Windows.Automation.TreeScope]::Descendants, $condition)
    if ($null -eq $button) { throw 'Assistant configuration control is absent from the real Windows UI.' }
    $rectangle = $window.Current.BoundingRectangle
    $bitmap = [System.Drawing.Bitmap]::new([int]$rectangle.Width, [int]$rectangle.Height)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    try {
        $graphics.CopyFromScreen([int]$rectangle.X, [int]$rectangle.Y, 0, 0, $bitmap.Size)
        $bitmap.Save((Join-Path (Resolve-Path $OutputDirectory) 'windows-start.png'), [System.Drawing.Imaging.ImageFormat]::Png)
    } finally { $graphics.Dispose(); $bitmap.Dispose() }
    @{ commit = $env:GITHUB_SHA; head_commit = $env:ACCESSIA_HEAD_SHA; platform = 'Windows GitHub runner VM'; physical_device = $false; app_window = $window.Current.Name; status = $description; assertions = @('process stayed alive', 'native window created', 'WebView2 initialized', 'assistant control accessible'); limitations = @('no physical microphone', 'no local model inference in this smoke test', 'no Honor validation') } | ConvertTo-Json -Depth 3 | Set-Content (Join-Path $OutputDirectory 'provenance.json')
} finally { if (!$appProcess.HasExited) { $appProcess.Kill(); $appProcess.WaitForExit() } }
