param([string]$Loader='neoforge',[string]$Minecraft='26.3',[string]$LoaderVersion='26.3.0.84')
$ErrorActionPreference='Stop'
$base=Join-Path $PSScriptRoot 'boat-motors-v2'
$source=$base
if($Minecraft -ne '26.3'){$source=Join-Path $PSScriptRoot "boat-ports/fabric-$Minecraft"}
$dest=Join-Path $PSScriptRoot "boat-ports/$Loader-$Minecraft"
New-Item -ItemType Directory -Force -Path "$dest/src/main/java/com/beeto/boatmotors","$dest/src/main/resources/META-INF"|Out-Null
foreach($f in @('gradle','gradlew','gradlew.bat','gradle.properties','LICENSE','.gitignore')){Copy-Item -LiteralPath "$base/$f" -Destination $dest -Recurse -Force}
Copy-Item -Path "$source/src/main/java/*" -Destination "$dest/src/main/java" -Recurse -Force
Copy-Item -Path "$source/src/client/java/*" -Destination "$dest/src/main/java" -Recurse -Force
Copy-Item -Path "$source/src/main/resources/*" -Destination "$dest/src/main/resources" -Recurse -Force
Copy-Item -Path "$source/src/client/resources/*" -Destination "$dest/src/main/resources" -Recurse -Force
$formats=(Get-Content "$PSScriptRoot/boat-pack-versions.json"|ConvertFrom-Json).$Minecraft
if($formats.PSObject.Properties.Name -contains 'data_major'){
 $pack=@{description='Boat Motors resources and recipes';min_format=@($formats.resource_major,$formats.resource_minor);max_format=@($formats.data_major,$formats.data_minor)}
}else{$pack=@{description='Boat Motors resources and recipes';pack_format=$formats.resource;supported_formats=@{min_inclusive=$formats.resource;max_inclusive=$formats.data}}}
@{pack=$pack}|ConvertTo-Json -Depth 10|Set-Content "$dest/src/main/resources/pack.mcmeta"
foreach($config in Get-ChildItem "$dest/src/main/resources" -Filter '*mixins.json'){
 $json=Get-Content $config.FullName|ConvertFrom-Json
 $json|Add-Member -NotePropertyName minVersion -NotePropertyValue '0.8' -Force
 $json|ConvertTo-Json -Depth 10|Set-Content $config.FullName
}
# Exclude Fabric bootstrap/metadata in the native loader projects.
if(Test-Path "$dest/src/main/java/com/beeto/boatmotors/BoatMotorsFabric.java"){Remove-Item -LiteralPath "$dest/src/main/java/com/beeto/boatmotors/BoatMotorsFabric.java"}
if(Test-Path "$dest/src/main/resources/fabric.mod.json"){Remove-Item -LiteralPath "$dest/src/main/resources/fabric.mod.json"}
"pluginManagement { repositories { gradlePluginPortal(); maven { url='https://maven.neoforged.net/releases' }; maven { url='https://maven.minecraftforge.net' } } }; rootProject.name='boat-motors-$Loader-$Minecraft'"|Set-Content "$dest/settings.gradle"
$forge=$Loader -eq 'forge'
$prefix=if($forge){'net.minecraftforge'}else{'net.neoforged'}
$busSetup=if($forge){'var group=context.getModBusGroup(); RegisterEvent.getBus(group).addListener(this::items); BuildCreativeModeTabContentsEvent.BUS.addListener(this::creative);'}else{'bus.addListener(this::items); bus.addListener(this::creative);'}
if($forge -and $Minecraft.StartsWith('1.') -and [version]$Minecraft -le [version]'1.21.5'){$busSetup='var bus=context.getModEventBus(); bus.addListener(this::items); bus.addListener(this::creative);'}
if($forge -and $Minecraft.StartsWith('1.') -and [version]$Minecraft -ge [version]'1.21.6' -and [version]$Minecraft -le [version]'1.21.8'){$busSetup='var group=context.getModBusGroup(); RegisterEvent.getBus(group).addListener(this::items); BuildCreativeModeTabContentsEvent.getBus(group).addListener(this::creative);'}
$ctor=if($forge){'net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context'}else{'net.neoforged.bus.api.IEventBus bus'}
$eventPrefix=if($forge){'net.minecraftforge'}else{'net.neoforged.neoforge'}
@"
package com.beeto.boatmotors;
import $prefix.fml.common.Mod;
import $eventPrefix.registries.RegisterEvent;
import $eventPrefix.event.BuildCreativeModeTabContentsEvent;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTabs;
@Mod("boat_motors")
public class BoatMotorsNative {
 public BoatMotorsNative($ctor){$busSetup}
 private void items(RegisterEvent event){
  if(event.getRegistryKey().equals(Registries.ITEM))BoatMotors.registerItems((key,item)->event.register(Registries.ITEM,h->h.register(key.identifier(),item)));
 }
 private void creative(BuildCreativeModeTabContentsEvent event){
  if(event.getTabKey().equals(CreativeModeTabs.TOOLS_AND_UTILITIES)){
   for(int i=1;i<=5;i++)event.accept(BoatMotors.MOTORS[i]);event.accept(BoatMotors.GAS_CAN);event.accept(BoatMotors.EMPTY_CAN);
  }
 }
}
"@|Set-Content "$dest/src/main/java/com/beeto/boatmotors/BoatMotorsNative.java"
$metadata=if($forge){'mods.toml'}else{'neoforge.mods.toml'}
@"
modLoader="javafml"
loaderVersion="[1,)"
license="MIT"
[[mods]]
modId="boat_motors"
version="2.0.0"
displayName="Boat Motors"
logoFile="assets/boat_motors/icon.png"
authors="beeto"
description='''Five detailed outboards, gasoline, direct recipes and an MPH / gas / MPG dashboard.'''
[[dependencies.boat_motors]]
modId="minecraft"
type="required"
mandatory=true
versionRange="[$Minecraft]"
ordering="NONE"
side="BOTH"
[[mixins]]
config="boat_motors.mixins.json"
[[mixins]]
config="boat_motors.client.mixins.json"
"@|Set-Content "$dest/src/main/resources/META-INF/$metadata"
if($forge){
 "Manifest-Version: 1.0`nMixinConfigs: boat_motors.mixins.json,`n boat_motors.client.mixins.json`n`n"|Set-Content "$dest/src/main/resources/META-INF/MANIFEST.MF"
 foreach($config in Get-ChildItem "$dest/src/main/resources" -Filter '*mixins.json'){(Get-Content $config.FullName -Raw).Replace('JAVA_25','JAVA_21')|Set-Content $config.FullName}
}
$common=@"
version='2.0.0'
group='com.beeto'
base { archivesName='boat-motors-$Loader-$Minecraft' }
java { toolchain.languageVersion=JavaLanguageVersion.of(25); withSourcesJar() }
tasks.withType(JavaCompile).configureEach { options.encoding='UTF-8' }
jar { from('LICENSE'); manifest { attributes('MixinConfigs':'boat_motors.mixins.json,boat_motors.client.mixins.json') } }
"@
if($forge){
@"
plugins { id 'java'; id 'net.minecraftforge.gradle' version '7.0.17' }
$common
minecraft { runs { register('server') { args '--nogui' } } }
repositories { minecraft.mavenizer(it); maven fg.forgeMaven; maven fg.minecraftLibsMaven; mavenCentral() }
dependencies { implementation minecraft.dependency('net.minecraftforge:forge:$Minecraft-$LoaderVersion') }
// Keep development mod metadata and classes in the same scan root.
sourceSets.main.output.resourcesDir=layout.buildDirectory.dir('classes/java/main').get().asFile
tasks.named('processResources') { destinationDir=sourceSets.main.output.resourcesDir }
tasks.named('jar') { duplicatesStrategy=DuplicatesStrategy.EXCLUDE }
tasks.withType(JavaCompile).configureEach { options.release=21 }
"@|Set-Content "$dest/build.gradle"
 $w=Get-Content "$dest/gradle/wrapper/gradle-wrapper.properties" -Raw;$w.Replace('gradle-9.6.0','gradle-9.7.1')|Set-Content "$dest/gradle/wrapper/gradle-wrapper.properties"
 if($Minecraft.StartsWith('26.')){Add-Content "$dest/build.gradle" "configurations.compileClasspath.attributes.attribute(org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,25)`nconfigurations.runtimeClasspath.attributes.attribute(org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE,25)"}
}else{
@"
plugins { id 'java-library'; id 'net.neoforged.moddev' version '2.0.148' }
$common
neoForge { version='$LoaderVersion'; mods { boat_motors { sourceSet sourceSets.main } }; runs { server { server(); programArgument '--nogui' } } }
"@|Set-Content "$dest/build.gradle"
}
if($Minecraft -ne '26.3'){
 foreach($java in Get-ChildItem "$dest/src/main/java" -Recurse -Filter '*.java'){
  (Get-Content $java.FullName -Raw).Replace('poses.rotateDegrees(Axis.YP, 180 - state.yRot);','poses.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));')|Set-Content $java.FullName
 }
}
if($Minecraft.StartsWith('1.')){
 $b=Get-Content "$dest/build.gradle" -Raw
 $b=$b.Replace('JavaLanguageVersion.of(25)','JavaLanguageVersion.of(21)')
 if($forge){$b=$b.Replace('minecraft { runs',"minecraft { mappings channel: 'official', version: '$Minecraft'; runs")}
 $b|Set-Content "$dest/build.gradle"
 if([version]$Minecraft -lt [version]'1.21.11'){
  $file="$dest/src/main/java/com/beeto/boatmotors/BoatMotorsNative.java"
  (Get-Content $file -Raw).Replace('key.identifier()','key.location()')|Set-Content $file
 }
}
if(!$forge -and ($Minecraft.StartsWith('26.') -or [version]$Minecraft -ge [version]'1.21.7')){
 Copy-Item "$PSScriptRoot/port-templates/MotorAttachments.java" "$dest/src/main/java/com/beeto/boatmotors" -Force
 $mixin="$dest/src/main/java/com/beeto/boatmotors/mixin/BoatMixin.java"
 $s=Get-Content $mixin -Raw
 $s=$s.Replace('import com.beeto.boatmotors.Fuel;','import com.beeto.boatmotors.Fuel;'+"`n"+'import com.beeto.boatmotors.MotorAttachments;')
 $s=[regex]::Replace($s,'(?s)    @Unique private static final EntityDataAccessor<Integer> BOATMOTORS_TIER =.*?(?=    @Unique private double)','')
 $s=[regex]::Replace($s,'(?s)    @Inject\(method = "defineSynchedData".*?builder.define\(BOATMOTORS_FUEL,0\); \}\r?\n','')
 $s=$s.Replace('entityData.get(BOATMOTORS_TIER)','getData(MotorAttachments.TIER.get())').Replace('entityData.get(BOATMOTORS_FUEL)','getData(MotorAttachments.FUEL.get())').Replace('entityData.set(BOATMOTORS_TIER,','setData(MotorAttachments.TIER.get(),').Replace('entityData.set(BOATMOTORS_FUEL,','setData(MotorAttachments.FUEL.get(),')
 $s|Set-Content $mixin
 $bootstrap="$dest/src/main/java/com/beeto/boatmotors/BoatMotorsNative.java"
 (Get-Content $bootstrap -Raw).Replace('{bus.addListener(this::items);','{MotorAttachments.REGISTER.register(bus);bus.addListener(this::items);')|Set-Content $bootstrap
}
