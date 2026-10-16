package com.beeto.boatmotors.mixin.client;
import com.beeto.boatmotors.BoatMotors;
import com.beeto.boatmotors.MotorBoat;
import com.beeto.boatmotors.MotorRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.AbstractBoatRenderer;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.AbstractBoat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(AbstractBoatRenderer.class)
public class BoatRendererMixin {
    @Inject(method="extractRenderState(Lnet/minecraft/world/entity/vehicle/AbstractBoat;Lnet/minecraft/client/renderer/entity/state/BoatRenderState;F)V",at=@At("TAIL"))
    private void boatmotors$extract(AbstractBoat boat,BoatRenderState state,float partial,CallbackInfo ci){
        int tier=((MotorBoat)boat).boatmotors$getTier();
        ((MotorRenderState)state).boatmotors$setItem(tier==0?ItemStack.EMPTY:new ItemStack(BoatMotors.MOTORS[tier]));
    }
    @Inject(method="render(Lnet/minecraft/client/renderer/entity/state/BoatRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void boatmotors$render(BoatRenderState state,PoseStack poses,MultiBufferSource buffers,int light,CallbackInfo ci){
        poses.pushPose();poses.mulPose(Axis.YP.rotationDegrees(180-state.yRot));poses.translate(0,.24,1);
        Minecraft.getInstance().getItemRenderer().renderStatic(((MotorRenderState)state).boatmotors$item(),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,poses,buffers,Minecraft.getInstance().level,0);
        poses.popPose();
    }
}
