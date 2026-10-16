package com.beeto.boatmotors;

public interface MotorBoat {
    int boatmotors$getTier();
    void boatmotors$setTier(int tier);
    void boatmotors$applyThrottle(boolean forward, boolean reverse);
    int boatmotors$getFuel();
    void boatmotors$setFuel(int amount);
    void boatmotors$burnFuel(double distance, double speed);
}
