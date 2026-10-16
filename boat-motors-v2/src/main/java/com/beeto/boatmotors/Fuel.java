package com.beeto.boatmotors;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
public final class Fuel {
    public static final int UNITS=10000;
    public static final double[] TANKS={0,2.5,4,5,8,12};
    public static final double[] MPG={0,6,5,4,3,2};
    public static int capacity(int tier){return (int)(TANKS[BoatMotors.validTier(tier)]*UNITS);}
    public static int read(ItemStack stack,int fallback){return Math.max(0,stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getIntOr("boat_motors:fuel",fallback));}
    public static ItemStack with(ItemStack stack,int amount){CustomData.update(DataComponents.CUSTOM_DATA,stack,t->t.putInt("boat_motors:fuel",Math.max(0,amount)));return stack;}
    public static double mpg(int tier,double speed){return MPG[tier]*(0.45+0.55*Math.min(1,Math.abs(speed)/BoatMotors.SPEEDS[tier]));}
}
