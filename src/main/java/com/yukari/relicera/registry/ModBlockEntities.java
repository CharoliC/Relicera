package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.block.DreamcatcherBoxBlockEntity;
import com.yukari.relicera.common.block.RelicRepairTableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ReliceraMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<RelicRepairTableBlockEntity>> RELIC_REPAIR_TABLE =
            BLOCK_ENTITIES.register("relic_repair_table", () ->
                    BlockEntityType.Builder.of(RelicRepairTableBlockEntity::new, ModBlocks.RELIC_REPAIR_TABLE.get()).build(null));

    public static final RegistryObject<BlockEntityType<DreamcatcherBoxBlockEntity>> DREAMCATCHER_BOX =
            BLOCK_ENTITIES.register("dreamcatcher_box", () ->
                    BlockEntityType.Builder.of(DreamcatcherBoxBlockEntity::new, ModBlocks.DREAMCATCHER_BOX.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
