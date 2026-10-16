package com.beeto.boatmotors.testmixin;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(AbstractBoat.class)
public interface BoatControlInvoker {
    @Invoker("controlBoat") void boatmotors$invokeControl();
}
