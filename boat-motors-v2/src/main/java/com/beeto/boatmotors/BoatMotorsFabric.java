package com.beeto.boatmotors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.CreativeModeTabs;
public class BoatMotorsFabric implements ModInitializer {
    @Override public void onInitialize() {
        BoatMotors.registerItems((key,item)->Registry.register(BuiltInRegistries.ITEM,key,item));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries->{
            for(int i=1;i<=5;i++)entries.accept(BoatMotors.MOTORS[i]);
            entries.accept(BoatMotors.GAS_CAN);entries.accept(BoatMotors.EMPTY_CAN);
        });
    }
}
