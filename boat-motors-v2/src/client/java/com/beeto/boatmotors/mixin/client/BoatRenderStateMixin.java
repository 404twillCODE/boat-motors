package com.beeto.boatmotors.mixin.client;
import com.beeto.boatmotors.MotorRenderState;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(BoatRenderState.class)
public class BoatRenderStateMixin implements MotorRenderState {
    @Unique private final ItemStackRenderState boatmotors$motor = new ItemStackRenderState();
    @Override public ItemStackRenderState boatmotors$item() { return boatmotors$motor; }
}
