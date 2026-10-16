package com.beeto.boatmotors;
import com.beeto.boatmotors.testmixin.BoatControlInvoker;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.Vec3;

public class MotorTests {
    private void check(GameTestHelper h, boolean value, String text) { h.assertTrue(value, Component.literal(text)); }
    private Player player(GameTestHelper h) {
        Player p = h.makeMockPlayer(GameType.SURVIVAL);
        GameType.SURVIVAL.updatePlayerAbilities(p.getAbilities());
        return p;
    }
    private void attach(AbstractBoat boat, Player p, int tier) {
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BoatMotors.MOTORS[tier]));
        boat.interact(p, InteractionHand.MAIN_HAND, Vec3.ZERO);
    }
    @GameTest public void attachAndUpgrade(GameTestHelper h) {
        AbstractBoat boat = h.spawn(EntityTypes.OAK_BOAT, new BlockPos(1, 2, 1));
        Player p = player(h);
        attach(boat, p, 1);
        check(h, ((MotorBoat)boat).boatmotors$getTier() == 1, "9.9 HP must attach");
        check(h, p.getMainHandItem().isEmpty(), "Installing a motor consumes its item");
        attach(boat, p, 5);
        check(h, ((MotorBoat)boat).boatmotors$getTier() == 5, "100 HP must replace the old motor");
        check(h, p.getInventory().countItem(BoatMotors.MOTORS[1]) == 1, "Upgrade must return exactly one old motor");
        h.succeed();
    }
    @GameTest public void raftAndChestBoatSupport(GameTestHelper h) {
        AbstractBoat raft = h.spawn(EntityTypes.BAMBOO_RAFT, new BlockPos(1, 2, 1));
        AbstractBoat chest = h.spawn(EntityTypes.OAK_CHEST_BOAT, new BlockPos(3, 2, 1));
        Player p = player(h);
        attach(raft, p, 2); attach(chest, p, 3);
        check(h, ((MotorBoat)raft).boatmotors$getTier() == 2, "Rafts must support motors");
        check(h, ((MotorBoat)chest).boatmotors$getTier() == 3, "Chest boats must support motors");
        h.succeed();
    }
    @GameTest public void attachmentSurvivesSaveAndLoad(GameTestHelper h) {
        AbstractBoat boat = h.spawn(EntityTypes.OAK_BOAT, new BlockPos(1, 2, 1));
        ((MotorBoat)boat).boatmotors$setTier(4);
        ((MotorBoat)boat).boatmotors$setFuel(4321);
        var saved = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, h.getLevel().registryAccess());
        boat.saveWithoutId(saved);
        ((MotorBoat)boat).boatmotors$setTier(0);
        boat.load(TagValueInput.create(ProblemReporter.DISCARDING, h.getLevel().registryAccess(), saved.buildResult()));
        check(h, ((MotorBoat)boat).boatmotors$getTier() == 4, "Saved boat must retain 50 HP motor");
        check(h, ((MotorBoat)boat).boatmotors$getFuel() == 4321, "Saved boat must retain its gasoline");
        h.succeed();
    }
    @GameTest public void eachHorsepowerIsFasterWithGas(GameTestHelper h) {
        for(int x=0;x<5;x++) for(int z=0;z<5;z++) h.setBlock(x,1,z,Blocks.WATER);
        AbstractBoat boat = h.spawn(EntityTypes.OAK_BOAT, new Vec3(2, 1.5, 2));
        boat.tick();
        Player p = player(h);
        p.startRiding(boat);
        boat.setInput(false, false, true, false);
        check(h, boat.isInWater(), "Speed test boat must be floating in water");
        double previous = 0;
        for(int tier=1;tier<=5;tier++) {
            ((MotorBoat)boat).boatmotors$setTier(tier);
            ((MotorBoat)boat).boatmotors$setFuel(Fuel.capacity(tier));
            boat.setDeltaMovement(Vec3.ZERO);
            boat.setYRot(0);
            for(int tick=0;tick<160;tick++) {
                boat.setDeltaMovement(boat.getDeltaMovement().multiply(0.9,0,0.9));
                ((BoatControlInvoker)boat).boatmotors$invokeControl();
            }
            double speed = boat.getDeltaMovement().horizontalDistance();
            check(h, speed > previous + 0.1, "Each horsepower must increase cruising speed");
            check(h, Math.abs(speed - BoatMotors.SPEEDS[tier]) < 0.02, "Motor must approach its designed top speed");
            previous = speed;
        }
        check(h, p.getInventory().isEmpty(), "Fuel is stored in the motor, not carried as an inventory requirement");
        h.succeed();
    }
    @GameTest public void motorDoesNotBoostOnLand(GameTestHelper h) {
        AbstractBoat boat = h.spawn(EntityTypes.OAK_BOAT, new BlockPos(1, 2, 1));
        player(h).startRiding(boat);
        ((MotorBoat)boat).boatmotors$setTier(5);
        ((MotorBoat)boat).boatmotors$setFuel(10000);
        boat.setDeltaMovement(Vec3.ZERO);
        ((MotorBoat)boat).boatmotors$applyThrottle(true, false);
        check(h, boat.getDeltaMovement().equals(Vec3.ZERO), "Motor thrust must require water");
        h.succeed();
    }
    @GameTest public void removalReturnsMotor(GameTestHelper h) {
        AbstractBoat boat = h.spawn(EntityTypes.OAK_BOAT, new BlockPos(1, 2, 1));
        Player p = player(h);
        attach(boat, p, 4);
        ((MotorBoat)boat).boatmotors$setFuel(7654);
        p.setShiftKeyDown(true);
        p.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHEARS));
        boat.interact(p, InteractionHand.MAIN_HAND, Vec3.ZERO);
        check(h, ((MotorBoat)boat).boatmotors$getTier() == 0, "Shears must detach the motor");
        check(h, p.getInventory().countItem(BoatMotors.MOTORS[4]) == 1, "Detaching must return one motor");
        var returned=p.getInventory().getItem(1);
        for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(BoatMotors.MOTORS[4]))returned=p.getInventory().getItem(i);
        check(h,Fuel.read(returned,0)==7654,"Detached motor must preserve gasoline");
        h.succeed();
    }
    @GameTest public void gasCanRefillsAndReturnsEmpty(GameTestHelper h){
        AbstractBoat boat=h.spawn(EntityTypes.OAK_BOAT,new BlockPos(1,2,1));var p=player(h);attach(boat,p,1);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BoatMotors.GAS_CAN));
        boat.interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO);
        check(h,((MotorBoat)boat).boatmotors$getFuel()==Fuel.UNITS,"Gas can must add one gallon");
        check(h,p.getMainHandItem().is(BoatMotors.EMPTY_CAN),"Spent gas can must become empty");
        ((MotorBoat)boat).boatmotors$burnFuel(1609.344,BoatMotors.SPEEDS[1]);
        check(h,Math.abs(((MotorBoat)boat).boatmotors$getFuel()-(10000-10000/6))<=1,"Fuel use must match 6 MPG at cruising speed");
        h.succeed();
    }
    @GameTest public void partialFillDoesNotWasteGas(GameTestHelper h){
        AbstractBoat boat=h.spawn(EntityTypes.OAK_BOAT,new BlockPos(1,2,1));var p=player(h);attach(boat,p,1);
        ((MotorBoat)boat).boatmotors$setFuel(Fuel.capacity(1)-1234);
        p.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(BoatMotors.GAS_CAN));boat.interact(p,InteractionHand.MAIN_HAND,Vec3.ZERO);
        check(h,((MotorBoat)boat).boatmotors$getFuel()==Fuel.capacity(1),"Partial fill must reach capacity");
        check(h,Fuel.read(p.getMainHandItem(),0)==8766,"Unused gas must stay in the can");h.succeed();
    }
    @GameTest public void emptyTankCannotBoost(GameTestHelper h){
        for(int x=0;x<5;x++)for(int z=0;z<5;z++)h.setBlock(x,1,z,Blocks.WATER);
        AbstractBoat boat=h.spawn(EntityTypes.OAK_BOAT,new Vec3(2,1.5,2));boat.tick();player(h).startRiding(boat);
        ((MotorBoat)boat).boatmotors$setTier(5);boat.setDeltaMovement(Vec3.ZERO);((MotorBoat)boat).boatmotors$applyThrottle(true,false);
        check(h,boat.getDeltaMovement().equals(Vec3.ZERO),"Empty motor must not add thrust");h.succeed();
    }
    @GameTest public void realServerMovementBurnsFuel(GameTestHelper h){
        for(int x=0;x<5;x++)for(int z=0;z<5;z++)h.setBlock(x,1,z,Blocks.WATER);
        AbstractBoat boat=h.spawn(EntityTypes.OAK_BOAT,new Vec3(2,1.5,2));boat.tick();player(h).startRiding(boat);
        var motor=(MotorBoat)boat;motor.boatmotors$setTier(5);motor.boatmotors$setFuel(10000);boat.setPaddleState(true,true);boat.tick();
        for(int i=0;i<20;i++){boat.setPaddleState(true,true);boat.setPos(boat.getX()+0.02,boat.getY(),boat.getZ());boat.tick();}
        check(h,motor.boatmotors$getFuel()<10000,"Server movement and paddle packets must consume motor fuel");h.succeed();
    }
}
