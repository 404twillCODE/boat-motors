package com.beeto.boatmotors.mixin.client;
import com.beeto.boatmotors.BoatMotors;
import com.beeto.boatmotors.Fuel;
import com.beeto.boatmotors.MotorBoat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Hud.class)
public class DashboardMixin {
    @org.spongepowered.asm.mixin.Shadow private boolean isHidden;
    @Inject(method="extractRenderState",at=@At("TAIL"))
    private void boatmotors$dashboard(GuiGraphicsExtractor g,DeltaTracker delta,CallbackInfo ci){
        var mc=Minecraft.getInstance();
        if(mc.player==null||isHidden||!(mc.player.getVehicle() instanceof MotorBoat boat)||boat.boatmotors$getTier()==0)return;
        int tier=boat.boatmotors$getTier(),fuel=boat.boatmotors$getFuel();
        double speed=mc.player.getVehicle().getDeltaMovement().horizontalDistance();
        int w=mc.getWindow().getGuiScaledWidth(),h=mc.getWindow().getGuiScaledHeight(),x=w/2-105,y=h-102;
        // Raised silver rim and navy instrument face match the illustrated dashboard.
        g.fill(x-2,y-2,x+212,y+45,0xE508101C);
        g.fill(x-1,y-1,x+211,y+44,0xFFC3CFDE);
        g.fill(x,y,x+210,y+43,0xF01A2E4F);
        g.fill(x+1,y+1,x+209,y+2,0xFF5C769D);
        g.fill(x+60,y+6,x+61,y+37,0xFF344966);
        g.fill(x+151,y+6,x+152,y+37,0xFF344966);
        boatmotors$center(g,mc,"MPH",x+30,y+6,0xFFF0F5FF);
        boatmotors$center(g,mc,"GAS",x+106,y+6,0xFFF0F5FF);
        boatmotors$center(g,mc,"MPG",x+181,y+6,0xFFF0F5FF);
        boatmotors$center(g,mc,String.format(java.util.Locale.ROOT,"%.0f",speed*20*2.236936),x+30,y+20,0xFFFFFFFF);
        boatmotors$center(g,mc,fuel==0?"--":String.format(java.util.Locale.ROOT,"%.1f",Fuel.mpg(tier,speed)),x+181,y+20,0xFFFFFFFF);
        double ratio=fuel/(double)Fuel.capacity(tier);
        int lit=(int)Math.ceil(ratio*10);
        for(int segment=0;segment<10;segment++){
            int sx=x+67+segment*8;
            g.fill(sx,y+19,sx+7,y+31,0xFF091323);
            int color=segment<lit?(ratio>0.2?0xFF33EA37:ratio>0.1?0xFFFFCB3B:0xFFFF4B40):0xFF34495C;
            g.fill(sx+1,y+20,sx+6,y+30,color);
            if(segment<lit)g.fill(sx+1,y+20,sx+6,y+21,0xFFB4F49C);
        }
        boatmotors$center(g,mc,BoatMotors.HORSEPOWER[tier]+" HP",x+30,y+32,0xFFB8C9DE);
        boatmotors$center(g,mc,fuel==0?"EMPTY":String.format(java.util.Locale.ROOT,"%.1f/%.1f gal",fuel/(double)Fuel.UNITS,Fuel.TANKS[tier]),x+106,y+33,fuel==0?0xFFFF7767:0xFFCAD8E9);
    }
    private static void boatmotors$center(GuiGraphicsExtractor g,Minecraft mc,String text,int center,int y,int color){g.text(mc.font,text,center-mc.font.width(text)/2,y,color);}
}
