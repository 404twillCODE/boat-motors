package com.beeto.boatmotors;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
/** Native tracked attachments avoid allocating data IDs on vanilla entities. */
public final class MotorAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER=DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,"boat_motors");
    public static final Supplier<AttachmentType<Integer>> TIER=REGISTER.register("motor_tier",()->AttachmentType.builder(()->0).sync(ByteBufCodecs.VAR_INT).build());
    public static final Supplier<AttachmentType<Integer>> FUEL=REGISTER.register("motor_fuel",()->AttachmentType.builder(()->0).sync(ByteBufCodecs.VAR_INT).build());
}
