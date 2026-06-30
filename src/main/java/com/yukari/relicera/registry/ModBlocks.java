package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.block.RelicRepairTableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ReliceraMod.MOD_ID);

    public static final RegistryObject<Block> RELIC_REPAIR_TABLE = BLOCKS.register("relic_repair_table", () ->
            new RelicRepairTableBlock(BlockBehaviour.Properties.copy(Blocks.SMITHING_TABLE)
                    .strength(3.5F, 6.0F)
                    .sound(SoundType.DEEPSLATE)
                    .noOcclusion()));

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
