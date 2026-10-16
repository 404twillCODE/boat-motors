package com.beeto.boatmotors;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BoatMotors {
    public static final String[] HORSEPOWER = { "", "9.9", "20", "25", "50", "100" };
    // Approximate steady-water top speeds, in blocks per tick.
    public static final double[] SPEEDS = { 0.4, 0.65, 0.90, 1.05, 1.40, 1.85 };
    public static final String[] IDS = { "", "motor_9_9hp", "motor_20hp", "motor_25hp", "motor_50hp", "motor_100hp" };
    public static final Item[] MOTORS = new Item[6];
    public static Item GAS_CAN, EMPTY_CAN;
    public static Identifier id(String path) { return Identifier.fromNamespaceAndPath("boat_motors", path); }
    public static void registerItems(java.util.function.BiConsumer<ResourceKey<Item>,Item> register) {
        for (int tier = 1; tier <= 5; tier++) {
            ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id(IDS[tier]));
            MOTORS[tier] = new Item(new Item.Properties().setId(key).stacksTo(1));
            register.accept(key,MOTORS[tier]);
        }
        var gasKey=ResourceKey.create(Registries.ITEM,id("gas_can"));
        GAS_CAN=new Item(new Item.Properties().setId(gasKey).stacksTo(1));register.accept(gasKey,GAS_CAN);
        var emptyKey=ResourceKey.create(Registries.ITEM,id("empty_gas_can"));
        EMPTY_CAN=new Item(new Item.Properties().setId(emptyKey).stacksTo(16));register.accept(emptyKey,EMPTY_CAN);
    }
    public static int tierOf(ItemStack stack) {
        for (int i = 1; i <= 5; i++) if (stack.is(MOTORS[i])) return i;
        return 0;
    }
    public static int validTier(int tier) { return tier >= 1 && tier <= 5 ? tier : 0; }
}
