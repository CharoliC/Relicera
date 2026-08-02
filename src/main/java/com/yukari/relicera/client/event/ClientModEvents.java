package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.renderer.LuminasMoonRenderer;
import com.yukari.relicera.client.particle.ElectricSparkParticle;
import com.yukari.relicera.client.particle.GoldHeartParticle;
import com.yukari.relicera.client.renderer.TempestSprintHorseLayer;
import com.yukari.relicera.client.screen.FourfoldSherdPendantScreen;
import com.yukari.relicera.client.renderer.RelicRepairTableRenderer;
import com.yukari.relicera.client.screen.RelicRepairTableScreen;
import com.yukari.relicera.client.tooltip.ClientIluthiasChaliceTooltip;
import com.yukari.relicera.common.curio.CovenantTabletEffects;
import com.yukari.relicera.common.tooltip.IluthiasChaliceTooltip;
import com.yukari.relicera.registry.ModBlockEntities;
import com.yukari.relicera.registry.ModItems;
import com.yukari.relicera.registry.ModMenuTypes;
import com.yukari.relicera.registry.ModParticleTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.HorseRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.RELIC_REPAIR_TABLE.get(), RelicRepairTableScreen::new);
            MenuScreens.register(ModMenuTypes.FOURFOLD_SHERD_PENDANT.get(), FourfoldSherdPendantScreen::new);
            ItemProperties.register(ModItems.PASTORAL_MELODY.get(), ResourceLocation.fromNamespaceAndPath("minecraft", "tooting"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ItemProperties.register(ModItems.COVENANT_TABLET.get(), ResourceLocation.fromNamespaceAndPath(ReliceraMod.MOD_ID, "completed"),
                    (stack, level, entity, seed) -> CovenantTabletEffects.isFullyUnlocked(stack) ? 1.0F : 0.0F);
        });
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RELIC_REPAIR_TABLE.get(), RelicRepairTableRenderer::new);
    }

    @SubscribeEvent
    public static void addEntityLayers(EntityRenderersEvent.AddLayers event) {
        if (event.getRenderer(EntityType.HORSE) instanceof HorseRenderer renderer) {
            renderer.addLayer(new TempestSprintHorseLayer(renderer, event.getEntityModels()));
        }
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(LuminasMoonRenderer.MOON_MODEL);
    }

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(IluthiasChaliceTooltip.class, ClientIluthiasChaliceTooltip::new);
    }

    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.ELECTRIC_SPARK.get(), ElectricSparkParticle.Provider::new);
        event.registerSpriteSet(ModParticleTypes.GOLDHEART_0.get(), sprites -> new GoldHeartParticle.Provider(sprites, 1.1F));
        event.registerSpriteSet(ModParticleTypes.GOLDHEART_1.get(), sprites -> new GoldHeartParticle.Provider(sprites, 1.35F));
        event.registerSpriteSet(ModParticleTypes.GOLDHEART_2.get(), sprites -> new GoldHeartParticle.Provider(sprites, 1.65F));
    }
}
