package com.beeto.boatmotors;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

/** A positional engine loop; owned by the client boat, with one voice per motor. */
public final class MotorSound extends AbstractTickableSoundInstance {
    private final AbstractBoat boat;
    private final int tier;
    private float rpm;
    public MotorSound(AbstractBoat boat) {
        super(SoundEvent.createVariableRangeEvent(BoatMotors.id("engine_"+((MotorBoat)boat).boatmotors$getTier())), SoundSource.NEUTRAL, RandomSource.create());
        this.boat=boat;
        tier=((MotorBoat)boat).boatmotors$getTier();
        looping=true;
        delay=0;
        volume=0.22F;
        pitch=0.75F;
        x=boat.getX(); y=boat.getY(); z=boat.getZ();
    }
    public static boolean running(AbstractBoat boat) {
        var motor=(MotorBoat)boat;
        return !boat.isRemoved() && motor.boatmotors$getTier()>0 && motor.boatmotors$getFuel()>0
            && boat.isVehicle() && boat.isInWater() && !boat.isUnderWater();
    }
    @Override public void tick() {
        if(!running(boat) || ((MotorBoat)boat).boatmotors$getTier()!=tier){stop();return;}
        x=boat.getX();y=boat.getY();z=boat.getZ();
        float speed=(float)Math.min(1,boat.getDeltaMovement().horizontalDistance()/BoatMotors.SPEEDS[tier]);
        boolean throttling=boat.getPaddleState(0)||boat.getPaddleState(1);
        float target=throttling?Math.max(0.22F,speed):speed*0.35F;
        rpm+=(target-rpm)*0.14F;
        pitch=0.72F+rpm*1.0F;
        volume=0.22F+rpm*0.48F;
    }
}
