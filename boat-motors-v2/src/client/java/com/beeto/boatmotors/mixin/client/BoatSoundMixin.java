package com.beeto.boatmotors.mixin.client;

import com.beeto.boatmotors.MotorSound;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public class BoatSoundMixin {
    @Unique private MotorSound boatmotors$sound;
    @Unique private int boatmotors$soundRetry;
    @Inject(method="tick",at=@At("TAIL"))
    private void boatmotors$soundTick(CallbackInfo ci) {
        var boat=(AbstractBoat)(Object)this;
        if(!boat.level().isClientSide())return;
        if(boatmotors$soundRetry>0){boatmotors$soundRetry--;return;}
        var manager=Minecraft.getInstance().getSoundManager();
        if(MotorSound.running(boat) && (boatmotors$sound==null || boatmotors$sound.isStopped() || !manager.isActive(boatmotors$sound))){
            boatmotors$sound=new MotorSound(boat);
            manager.play(boatmotors$sound);
            boatmotors$soundRetry=20;
        }
    }
}
