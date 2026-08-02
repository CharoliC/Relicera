package com.yukari.relicera.mixin;

import com.yukari.relicera.common.curio.TreasureHuntersGlovesEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RandomizableContainerBlockEntity.class)
public abstract class RandomizableContainerBlockEntityMixin {
    @Shadow
    protected ResourceLocation lootTable;

    @Unique
    private ResourceLocation relicera$generatedLootTable;

    @Unique
    private int relicera$lootTableUnpackDepth;

    @Inject(method = "unpackLootTable", at = @At("HEAD"))
    private void relicera$captureTreasureHuntersGlovesLootTable(Player player, CallbackInfo ci) {
        if (relicera$lootTableUnpackDepth == 0) {
            relicera$generatedLootTable = this.lootTable;
        }
        relicera$lootTableUnpackDepth++;
    }

    @Inject(method = "unpackLootTable", at = @At("RETURN"))
    private void relicera$recordTreasureHuntersGlovesLootTable(Player player, CallbackInfo ci) {
        relicera$lootTableUnpackDepth--;
        if (relicera$lootTableUnpackDepth == 0) {
            ResourceLocation generatedLootTable = relicera$generatedLootTable;
            relicera$generatedLootTable = null;
            if (generatedLootTable == null) {
                return;
            }

            TreasureHuntersGlovesEffects.onLootTableGenerated(
                    (RandomizableContainerBlockEntity) (Object) this,
                    player,
                    generatedLootTable
            );
        }
    }
}
