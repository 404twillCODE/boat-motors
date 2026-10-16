package com.beeto.boatmotors.mixin.client;

import com.beeto.boatmotors.BoatMotors;
import com.beeto.boatmotors.MotorBoat;
import com.beeto.boatmotors.MotorRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoatRenderer.class)
public class BoatRendererMixin {
    @Unique private ItemModelResolver boatmotors$resolver;
    @Inject(method = "<init>", at = @At("TAIL"))
    private void boatmotors$init(EntityRendererProvider.Context context, Identifier texture, CallbackInfo ci) { boatmotors$resolver = context.getItemModelResolver(); }
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/vehicle/boat/AbstractBoat;Lnet/minecraft/client/renderer/entity/state/BoatRenderState;F)V", at = @At("TAIL"))
    private void boatmotors$extract(AbstractBoat boat, BoatRenderState state, float partialTicks, CallbackInfo ci) {
        int tier = ((MotorBoat)boat).boatmotors$getTier();
        var item = ((MotorRenderState)state).boatmotors$item();
        if (tier == 0) item.clear();
        else boatmotors$resolver.updateForNonLiving(item, new ItemStack(BoatMotors.MOTORS[tier]), ItemDisplayContext.NONE, boat);
    }
    @Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At("HEAD"))
    private void boatmotors$render(BoatRenderState state, PoseStack poses, SubmitNodeCollector collector, CameraRenderState camera, CallbackInfo ci) {
        poses.pushPose();
        poses.rotateDegrees(Axis.YP, 180 - state.yRot);
        poses.translate(0, 0.24, 1.0);
        ((MotorRenderState)state).boatmotors$item().submit(poses, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poses.popPose();
    }
}
