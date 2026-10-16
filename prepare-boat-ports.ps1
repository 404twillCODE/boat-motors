param([string[]]$Versions=@('26.2','26.1.2','26.1.1','26.1','1.21.11','1.21.10','1.21.9','1.21.8','1.21.7','1.21.6','1.21.5','1.21.4','1.21.3','1.21.2','1.21.1'))
$ErrorActionPreference='Stop'
$base=Join-Path $PSScriptRoot 'boat-motors-v2'
$api=Get-Content (Join-Path $PSScriptRoot 'boat-api-versions.json')|ConvertFrom-Json
foreach($v in $Versions){
 $dest=Join-Path $PSScriptRoot "boat-ports/fabric-$v"
 New-Item -ItemType Directory -Force -Path "$dest/src"|Out-Null
 foreach($file in @('gradle','gradlew','gradlew.bat','settings.gradle','gradle.properties','LICENSE','.gitignore')){Copy-Item -LiteralPath (Join-Path $base $file) -Destination $dest -Recurse -Force}
 foreach($side in @('main','client')){Copy-Item -LiteralPath "$base/src/$side" -Destination "$dest/src" -Recurse -Force}
 $build=Get-Content "$base/build.gradle" -Raw
 $build=$build.Substring(0,$build.IndexOf('fabricApi {'))
 $build=$build.Replace("minecraft:26.3","minecraft:$v").Replace('fabric-26.3',"fabric-$v").Replace('0.162.0+26.3',($api|Where-Object minecraft -eq $v).api)
 if($v.StartsWith('1.')){
  $build=$build.Replace("'net.fabricmc.fabric-loom'","'net.fabricmc.fabric-loom-remap'").Replace("implementation 'net.fabricmc:","modImplementation 'net.fabricmc:").Replace("implementation 'net.fabricmc.fabric-api:","modImplementation 'net.fabricmc.fabric-api:")
  $build=$build.Replace("minecraft 'com.mojang:minecraft:$v'","minecraft 'com.mojang:minecraft:$v'`n    mappings loom.officialMojangMappings()")
  $build=$build.Replace('VERSION_25','VERSION_21').Replace('release = 25','release = 21')
 }
 $build|Set-Content "$dest/build.gradle" -Encoding utf8
 $mod=Get-Content "$dest/src/main/resources/fabric.mod.json"|ConvertFrom-Json
 $mod.depends.minecraft=$v
 $mod.depends.'fabric-api'='*'
 if($v.StartsWith('1.')){$mod.depends.java='>=21'}
 $mod|ConvertTo-Json -Depth 10|Set-Content "$dest/src/main/resources/fabric.mod.json" -Encoding utf8
 if($v.StartsWith('1.')){
  foreach($config in Get-ChildItem "$dest/src" -Recurse -Filter '*mixins.json'){
   (Get-Content $config.FullName -Raw).Replace('JAVA_25','JAVA_21')|Set-Content $config.FullName
  }
 }
 foreach($java in Get-ChildItem "$dest/src" -Recurse -Filter '*.java'){
  $s=Get-Content $java.FullName -Raw
  $s=$s.Replace('poses.rotateDegrees(Axis.YP, 180 - state.yRot);','poses.mulPose(Axis.YP.rotationDegrees(180 - state.yRot));')
  if($v.StartsWith('1.')){
   $s=$s.Replace('EntityRendererProvider.Context context, Identifier texture, CallbackInfo','EntityRendererProvider.Context context, CallbackInfo')
   $s=$s.Replace('InteractionHand hand, Vec3 location, CallbackInfoReturnable','InteractionHand hand, CallbackInfoReturnable')
   $s=$s.Replace('net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents','net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents').Replace('CreativeModeTabEvents.modifyOutputEvent','ItemGroupEvents.modifyEntriesEvent')
   $s=[regex]::Replace($s,'player.sendSystemMessage\((.*)\);','player.displayClientMessage($1,false);')
   $s=$s.Replace('net.minecraft.client.renderer.state.level.CameraRenderState','net.minecraft.client.renderer.state.CameraRenderState').Replace('net/minecraft/client/renderer/state/level/CameraRenderState','net/minecraft/client/renderer/state/CameraRenderState')
   $s=$s.Replace('GuiGraphicsExtractor','GuiGraphics').Replace('g.text(','g.drawString(')
   if($java.Name -eq 'DashboardMixin.java'){$s=$s.Replace('method="extractRenderState"','method="render"')}
  }
  if($v.StartsWith('1.') -and [version]$v -le [version]'1.21.5'){
   $s=$s.Replace('import net.minecraft.world.level.storage.ValueInput;','import net.minecraft.nbt.CompoundTag;').Replace('import net.minecraft.world.level.storage.ValueOutput;','').Replace('ValueInput input','CompoundTag input').Replace('ValueOutput output','CompoundTag output')
  }
  if($v.StartsWith('1.') -and [version]$v -le [version]'1.21.4'){
   $s=$s.Replace('input.getIntOr("boat_motors:tier", 0)','input.getInt("boat_motors:tier")').Replace('input.getIntOr("boat_motors:fuel",0)','input.getInt("boat_motors:fuel")').Replace('input.getDoubleOr("boat_motors:fraction",0)','input.getDouble("boat_motors:fraction")')
   $s=$s.Replace('stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("boat_motors:fuel",fallback)','boatmotors$readTag(stack,fallback)')
   if($java.Name -eq 'Fuel.java'){$s=$s.Replace('    public static int read(', '    private static int boatmotors$readTag(ItemStack stack,int fallback){var tag=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();return tag.contains("boat_motors:fuel",99)?tag.getInt("boat_motors:fuel"):fallback;}' + "`n" + '    public static int read(')}
  }
  if($v -eq '1.21.1'){
   $s=$s.Replace('AbstractBoat','Boat').Replace('spawnAtLocation(server, returned)','spawnAtLocation(returned)').Replace('spawnAtLocation(server,dropped)','spawnAtLocation(dropped)')
   $s=[regex]::Replace($s,'\.setId\((key|gasKey|emptyKey)\)','')
  }
  if($v.StartsWith('26.1') -or $v.StartsWith('1.')){
   $s=$s.Replace('net.minecraft.client.gui.Hud','net.minecraft.client.gui.Gui').Replace('@Mixin(Hud.class)','@Mixin(Gui.class)').Replace('    @org.spongepowered.asm.mixin.Shadow private boolean isHidden;','').Replace('mc.player==null||isHidden||','mc.player==null||mc.options.hideGui||')
  }
  if($v.StartsWith('1.') -and [version]$v -lt [version]'1.21.11'){
   $s=$s.Replace('InteractionHand hand, Vec3 location, CallbackInfoReturnable','InteractionHand hand, CallbackInfoReturnable')
   $s=$s.Replace('Identifier','ResourceLocation').Replace('net.minecraft.world.entity.vehicle.boat.','net.minecraft.world.entity.vehicle.').Replace('net/minecraft/world/entity/vehicle/boat/','net/minecraft/world/entity/vehicle/').Replace('net.minecraft.world.level.gamerules.GameRules','net.minecraft.world.level.GameRules').Replace('server.getGameRules().get(GameRules.ENTITY_DROPS)','server.getGameRules().getBoolean(GameRules.RULE_DOENTITYDROPS)')
  }
  if($v.StartsWith('1.') -and [version]$v -le [version]'1.21.8' -and $java.Name -eq 'BoatRendererMixin.java'){
   $s=$s.Replace('EntityRendererProvider.Context context, ResourceLocation texture, CallbackInfo','EntityRendererProvider.Context context, CallbackInfo')
   $s=$s.Replace('import net.minecraft.client.renderer.SubmitNodeCollector;','import net.minecraft.client.renderer.MultiBufferSource;').Replace('import net.minecraft.client.renderer.state.CameraRenderState;','')
   $s=[regex]::Replace($s,'@Inject\(method = "submit\([^\r\n]+','@Inject(method = "render(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))')
   $s=$s.Replace('SubmitNodeCollector collector, CameraRenderState camera','MultiBufferSource buffers, int light')
   $s=$s.Replace('.submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor)','.render(poses, buffers, light, OverlayTexture.NO_OVERLAY)')
  }
  $s|Set-Content -LiteralPath $java.FullName -Encoding utf8
 }
 if($v.StartsWith('1.') -and [version]$v -le [version]'1.21.3' -and [version]$v -ge [version]'1.21.2'){
  $client="$dest/src/client/java/com/beeto/boatmotors"
  Copy-Item "$PSScriptRoot/port-templates/legacy-state/MotorRenderState.java" $client -Force
  Copy-Item "$PSScriptRoot/port-templates/legacy-state/BoatRenderStateMixin.java" "$client/mixin/client" -Force
  Copy-Item "$PSScriptRoot/port-templates/legacy-state/BoatRendererMixin.java" "$client/mixin/client" -Force
 }
 if($v -eq '1.21.1'){
  $client="$dest/src/client/java/com/beeto/boatmotors"
  Remove-Item -LiteralPath "$client/MotorRenderState.java","$client/mixin/client/BoatRenderStateMixin.java" -ErrorAction SilentlyContinue
  Copy-Item "$PSScriptRoot/port-templates/legacy-boat/BoatRendererMixin.java" "$client/mixin/client" -Force
  $config="$dest/src/client/resources/boat_motors.client.mixins.json"
  (Get-Content $config -Raw).Replace('"BoatRenderStateMixin", ','')|Set-Content $config
  foreach($recipe in Get-ChildItem "$dest/src/main/resources/data/boat_motors/recipe" -Filter '*.json'){
   $r=Get-Content $recipe.FullName|ConvertFrom-Json
   if($r.type -eq 'minecraft:crafting_shaped'){foreach($p in @($r.key.PSObject.Properties)){$r.key.($p.Name)=@{item=$p.Value}}}
   else{$r.ingredients=@($r.ingredients|ForEach-Object {@{item=$_}})}
   $r|ConvertTo-Json -Depth 20|Set-Content $recipe.FullName
  }
 }
}
