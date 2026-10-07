param(
  [string]$MavenBin = "C:\Program Files\apache-maven-3.10.0-bin\apache-maven-3.10.0\bin",
  [string]$JavaHome = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
)
$ErrorActionPreference = 'Stop'
$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$MavenBin;$env:Path"
if (Test-Path "$PSScriptRoot\..\.env") {
  Get-Content "$PSScriptRoot\..\.env" | ForEach-Object {
    if ($_ -match '^\s*([^#=]+?)\s*=\s*(.*?)\s*$') {
      [System.Environment]::SetEnvironmentVariable($matches[1], $matches[2], 'Process')
    }
  }
}
Set-Location "$PSScriptRoot\..\backend"
mvn spring-boot:run
