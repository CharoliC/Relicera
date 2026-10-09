package com.yukari.relicera.common.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.entity.forgeling.Forgeling;
import com.yukari.relicera.registry.ModBlocks;
import com.yukari.relicera.registry.ModEntityTypes;
import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonModEvents {
    private CommonModEvents() {
    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(
                    ModBlocks.EPHEMERAL_BLOOM.getId(), ModBlocks.POTTED_EPHEMERAL_BLOOM);
            ComposterBlock.COMPOSTABLES.put(ModItems.EPHEMERAL_BLOOM.get(), 0.99F);
        });
    }

    @SubscribeEvent
    public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
        event.put(ModEntityTypes.FORGELING.get(), Forgeling.createAttributes().build());
    }
}
