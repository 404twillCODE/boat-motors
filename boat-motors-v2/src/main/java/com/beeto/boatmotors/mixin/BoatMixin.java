package com.beeto.boatmotors.mixin;

import com.beeto.boatmotors.BoatMotors;
import com.beeto.boatmotors.MotorBoat;
import com.beeto.boatmotors.Fuel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBoat.class)
public abstract class BoatMixin extends Entity implements MotorBoat {
    @Unique private static final EntityDataAccessor<Integer> BOATMOTORS_TIER =
        SynchedEntityData.defineId(AbstractBoat.class, EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Integer> BOATMOTORS_FUEL = SynchedEntityData.defineId(AbstractBoat.class,EntityDataSerializers.INT);
    @Unique private double boatmotors$fraction,boatmotors$lastX,boatmotors$lastZ;
    @Unique private boolean boatmotors$hasPosition;
    @Shadow private boolean inputUp;
    @Shadow private boolean inputDown;
    protected BoatMixin(EntityType<?> type, Level level) { super(type, level); }

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void boatmotors$define(SynchedEntityData.Builder builder, CallbackInfo ci) { builder.define(BOATMOTORS_TIER, 0); builder.define(BOATMOTORS_FUEL,0); }
    @Override public int boatmotors$getTier() { return entityData.get(BOATMOTORS_TIER); }
    @Override public void boatmotors$setTier(int tier) { entityData.set(BOATMOTORS_TIER, BoatMotors.validTier(tier)); }
    @Override public int boatmotors$getFuel(){return entityData.get(BOATMOTORS_FUEL);}
    @Override public void boatmotors$setFuel(int amount){entityData.set(BOATMOTORS_FUEL,Math.clamp(amount,0,Fuel.capacity(boatmotors$getTier())));}
    @Override public void boatmotors$burnFuel(double distance,double speed){
        int tier=boatmotors$getTier();if(tier==0||boatmotors$getFuel()==0||distance<=0)return;
        boatmotors$fraction+=distance*Fuel.UNITS/(Fuel.mpg(tier,speed)*1609.344);
        int used=(int)boatmotors$fraction;
        if(used>0){boatmotors$setFuel(boatmotors$getFuel()-used);boatmotors$fraction-=used;}
    }
    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void boatmotors$save(ValueOutput output, CallbackInfo ci) { output.putInt("boat_motors:tier", boatmotors$getTier());output.putInt("boat_motors:fuel",boatmotors$getFuel());output.putDouble("boat_motors:fraction",boatmotors$fraction); }
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void boatmotors$load(ValueInput input, CallbackInfo ci) { boatmotors$setTier(input.getIntOr("boat_motors:tier", 0));boatmotors$setFuel(input.getIntOr("boat_motors:fuel",0));boatmotors$fraction=input.getDoubleOr("boat_motors:fraction",0); }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void boatmotors$interact(Player player, InteractionHand hand, Vec3 location, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.isSpectator()) return;
        ItemStack held = player.getItemInHand(hand);
        int tier = BoatMotors.tierOf(held);
        int previous = boatmotors$getTier();
        if(held.is(BoatMotors.GAS_CAN) && previous>0){
            if(level() instanceof ServerLevel server){
                int supplied=Math.min(Fuel.capacity(previous)-boatmotors$getFuel(),Fuel.read(held,Fuel.UNITS));
                if(supplied>0){boatmotors$setFuel(boatmotors$getFuel()+supplied);
                    if(!player.getAbilities().instabuild){int left=Fuel.read(held,Fuel.UNITS)-supplied;if(left==0)player.setItemInHand(hand,new ItemStack(BoatMotors.EMPTY_CAN));else Fuel.with(held,left);}
                    server.playSound(null,getX(),getY(),getZ(),SoundEvents.BOTTLE_FILL,SoundSource.PLAYERS,0.7F,0.7F);
                }
                player.sendSystemMessage(Component.literal(String.format(java.util.Locale.ROOT,"Gas: %.2f / %.2f gal",boatmotors$getFuel()/(double)Fuel.UNITS,Fuel.TANKS[previous])));
            }
            cir.setReturnValue(InteractionResult.SUCCESS);return;
        }
        boolean remove = player.isSecondaryUseActive() && held.is(Items.SHEARS) && previous > 0;
        if (tier == 0 && !remove) return;
        if (level() instanceof ServerLevel server) {
            if (tier != previous || remove) {
                int oldFuel=boatmotors$getFuel(),newFuel=tier>0?Fuel.read(held,0):0;
                if (tier > 0 && !player.getAbilities().instabuild) held.shrink(1);
                boatmotors$setTier(remove ? 0 : tier);
                boatmotors$setFuel(newFuel);boatmotors$fraction=0;
                if (previous > 0) {
                    ItemStack returned = Fuel.with(new ItemStack(BoatMotors.MOTORS[previous]),oldFuel);
                    if (!player.getInventory().add(returned)) spawnAtLocation(server, returned);
                }
                server.playSound(null, getX(), getY(), getZ(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.PLAYERS, 0.7F, 1.2F);
            }
            player.sendSystemMessage(Component.translatable(remove ? "message.boat_motors.removed" : "message.boat_motors.installed", BoatMotors.HORSEPOWER[tier]));
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
    @Inject(method = "controlBoat", at = @At("TAIL"))
    private void boatmotors$drive(CallbackInfo ci) { boatmotors$applyThrottle(inputUp, inputDown); }

    @Override public void boatmotors$applyThrottle(boolean forward, boolean reverse) {
        int tier = boatmotors$getTier();
        if (tier == 0 || boatmotors$getFuel()<=0 || !isInWater() || isUnderWater() || !isVehicle()) return;
        Vec3 v = getDeltaMovement();
        double yaw = Math.toRadians(getYRot());
        double dx = -Math.sin(yaw), dz = Math.cos(yaw);
        if (forward && !reverse) {
            // Vanilla already adds 0.04. Add the remaining motor thrust after vanilla drag.
            double thrust = BoatMotors.SPEEDS[tier] * 0.1 - 0.04;
            double x = v.x + dx * thrust, z = v.z + dz * thrust;
            double speed = Math.sqrt(x * x + z * z);
            // Steering grip stops high-power boats sliding sideways through turns.
            x = x * 0.88 + dx * speed * 0.12;
            z = z * 0.88 + dz * speed * 0.12;
            double capped = Math.sqrt(x * x + z * z);
            double scale = capped > BoatMotors.SPEEDS[tier] ? BoatMotors.SPEEDS[tier] / capped : 1;
            setDeltaMovement(x * scale, v.y, z * scale);
        } else if (reverse) {
            double along = v.x * dx + v.z * dz;
            if (along > 0.05) setDeltaMovement(v.x * 0.72, v.y, v.z * 0.72);
            else setDeltaMovement(v.x - dx * 0.02, v.y, v.z - dz * 0.02);
        }
    }
    @Inject(method="tick",at=@At("HEAD"))
    private void boatmotors$consume(CallbackInfo ci){
        if(level().isClientSide())return;
        double distance=Math.hypot(getX()-boatmotors$lastX,getZ()-boatmotors$lastZ);
        var boat=(AbstractBoat)(Object)this;
        if(boatmotors$hasPosition && distance<10 && isInWater() && isVehicle() && (boat.getPaddleState(0)||boat.getPaddleState(1)))boatmotors$burnFuel(distance,distance);
        boatmotors$lastX=getX();boatmotors$lastZ=getZ();boatmotors$hasPosition=true;
    }
    @Inject(method = "tick", at = @At("TAIL"))
    private void boatmotors$wake(CallbackInfo ci) {
        if (boatmotors$getTier() > 0 && isInWater() && getDeltaMovement().horizontalDistanceSqr() > 0.12 && tickCount % 2 == 0) {
            if (level().isClientSide()) {
                double yaw = Math.toRadians(getYRot());
                level().addParticle(ParticleTypes.SPLASH, getX() + Math.sin(yaw), getY() + 0.1, getZ() - Math.cos(yaw), 0, 0.04, 0);
            }
        }
    }
    @Inject(method = "remove", at = @At("HEAD"))
    private void boatmotors$drop(Entity.RemovalReason reason, CallbackInfo ci) {
        if (reason.shouldDestroy() && level() instanceof ServerLevel server && boatmotors$getTier() > 0) {
            int tier = boatmotors$getTier();
            ItemStack dropped=Fuel.with(new ItemStack(BoatMotors.MOTORS[tier]),boatmotors$getFuel());
            boatmotors$setTier(0);
            if (server.getGameRules().get(GameRules.ENTITY_DROPS)) spawnAtLocation(server,dropped);
        }
    }
}
