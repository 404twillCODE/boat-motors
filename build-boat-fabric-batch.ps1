param([string[]]$Versions=@('26.2','26.1.2','26.1.1','26.1','1.21.11'))
$ErrorActionPreference='Continue'
$bundledJava=Join-Path $PSScriptRoot 'jdk25/jdk-25.0.4.1+1'
if(Test-Path $bundledJava){$env:JAVA_HOME=$bundledJava}
if(!$env:JAVA_HOME){throw 'Set JAVA_HOME to a Java 25 JDK before building.'}
$env:GRADLE_USER_HOME=Join-Path $PSScriptRoot 'gradle-home'
$failed=$false
foreach($v in $Versions){
 $dir=Join-Path $PSScriptRoot "boat-ports/fabric-$v"
 $log=Join-Path $PSScriptRoot "boat-ports/fabric-$v.log"
 Push-Location $dir
 try{& .\gradlew.bat build --no-daemon *> $log;$exit=$LASTEXITCODE}finally{Pop-Location}
 "${v}: exit $exit"
 if($exit -ne 0){$failed=$true;Get-Content $log -Tail 32}
}
if($failed){throw 'One or more Fabric builds failed. See the individual build logs.'}
