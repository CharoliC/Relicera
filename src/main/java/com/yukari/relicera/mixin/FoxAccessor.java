package com.yukari.relicera.mixin;

import java.util.UUID;
import net.minecraft.world.entity.animal.Fox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Fox.class)
public interface FoxAccessor {
    @Invoker("addTrustedUUID")
    void relicera$addTrustedUUID(UUID uuid);

    @Invoker("trusts")
    boolean relicera$trusts(UUID uuid);
}
