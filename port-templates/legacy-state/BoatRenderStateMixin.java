package com.beeto.boatmotors.mixin.client;
import com.beeto.boatmotors.MotorRenderState;
import net.minecraft.client.renderer.entity.state.BoatRenderState;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
@Mixin(BoatRenderState.class)
public class BoatRenderStateMixin implements MotorRenderState {
    @Unique private ItemStack boatmotors$motor=ItemStack.EMPTY;
    @Override public ItemStack boatmotors$item(){return boatmotors$motor;}
    @Override public void boatmotors$setItem(ItemStack stack){boatmotors$motor=stack;}
}
