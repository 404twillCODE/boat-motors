package com.beeto.boatmotors.mixin.client;
import com.beeto.boatmotors.BoatMotors;
import com.beeto.boatmotors.MotorBoat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(BoatRenderer.class)
public class BoatRendererMixin {
    @Inject(method="render(Lnet/minecraft/world/entity/vehicle/Boat;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",at=@At("HEAD"))
    private void boatmotors$render(Boat boat,float yaw,float partial,PoseStack poses,MultiBufferSource buffers,int light,CallbackInfo ci){
        int tier=((MotorBoat)boat).boatmotors$getTier();if(tier==0)return;
        poses.pushPose();poses.mulPose(Axis.YP.rotationDegrees(180-yaw));poses.translate(0,.24,1);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(BoatMotors.MOTORS[tier]),ItemDisplayContext.NONE,light,OverlayTexture.NO_OVERLAY,poses,buffers,boat.level(),boat.getId());
        poses.popPose();
    }
}
