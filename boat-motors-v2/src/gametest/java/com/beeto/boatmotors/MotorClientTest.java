package com.beeto.boatmotors;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

public class MotorClientTest implements FabricClientGameTest {
    @Override public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            world.getServer().runCommand("time set noon");
            world.getServer().runCommand("weather clear");
            world.getServer().runCommand("gamemode spectator @p");
            world.getServer().runCommand("fill -16 62 -16 16 63 64 minecraft:water");
            world.getServer().runCommand("tp @p 0 68 -10 0 20");
            for(int tier=1;tier<=5;tier++)world.getServer().runCommand("summon minecraft:oak_boat "+((tier-3)*3.5)+" 63.5 0 {\"boat_motors:tier\":"+tier+",\"boat_motors:fuel\":10000,NoGravity:1b}");
            context.waitTicks(60);
            context.runOnClient(mc -> { if (mc.level == null) throw new AssertionError("Client world missing"); });
            context.takeScreenshot("five-motor-sizes");
            world.getServer().runCommand("gamemode survival @p");
            world.getServer().runCommand("tp @p 7 65 0 0 0");
            world.getServer().runCommand("execute at @p run ride @p mount @e[type=minecraft:oak_boat,sort=nearest,limit=1]");
            context.waitTicks(10);
            context.getInput().holdKeyFor(o->o.keyUp,40);
            context.takeScreenshot("speedometer-gas-mpg");
            world.getServer().runOnServer(server->{
                var p=server.getPlayerList().getPlayers().getFirst();
                if(!(p.getVehicle() instanceof MotorBoat boat)||boat.boatmotors$getFuel()>=10000||boat.boatmotors$getFuel()<=0)throw new AssertionError("Driving did not consume synchronized motor fuel");
            });
        }
    }
}
