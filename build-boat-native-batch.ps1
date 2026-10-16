param([string]$Loader='neoforge',[string[]]$Versions=@('26.2','26.1.2','26.1.1'))
$ErrorActionPreference='Continue'
$bundledJava=Join-Path $PSScriptRoot 'jdk25/jdk-25.0.4.1+1'
if(Test-Path $bundledJava){$env:JAVA_HOME=$bundledJava}
if(!$env:JAVA_HOME){throw 'Set JAVA_HOME to a Java 25 JDK before building.'}
$env:GRADLE_USER_HOME=Join-Path $PSScriptRoot 'gradle-home'
$matrix=Get-Content "$PSScriptRoot/boat-compatibility-research.json"|ConvertFrom-Json
$failed=$false
foreach($v in $Versions){
 $lv=($matrix|Where-Object minecraft -eq $v).$Loader
 if(!$lv){"${Loader} ${v}: no official loader release";continue}
 & "$PSScriptRoot/prepare-modern-loader-ports.ps1" -Loader $Loader -Minecraft $v -LoaderVersion $lv
 $dir=Join-Path $PSScriptRoot "boat-ports/$Loader-$v"
 Add-Content "$dir/gradle.properties" "`norg.gradle.workers.max=2`norg.gradle.parallel=false"
 Push-Location $dir
 try{& .\gradlew.bat build --console plain --no-daemon *> "$PSScriptRoot/boat-ports/$Loader-$v.log";$exit=$LASTEXITCODE}finally{Pop-Location}
 "${Loader} ${v}: exit $exit"
 if($exit -ne 0){$failed=$true;Get-Content "$PSScriptRoot/boat-ports/$Loader-$v.log" -Tail 35}
}
if($failed){throw 'One or more native builds failed. See the individual build logs.'}
