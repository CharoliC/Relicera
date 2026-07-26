package com.yukari.relicera.client.event;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.client.renderer.LuminasMoonRenderer;
import com.yukari.relicera.common.curio.LuminasCelestialLensEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReliceraMod.MOD_ID, value = Dist.CLIENT)
public final class LuminasCelestialLensClientEvents {
    private LuminasCelestialLensClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (shouldSkipLocalFirstPerson(entity) || !LuminasCelestialLensEffects.isEquipped(entity)) {
            return;
        }

        LuminasMoonRenderer.render(entity, event.getPoseStack(), event.getMultiBufferSource(), event.getPartialTick());
    }

    private static boolean shouldSkipLocalFirstPerson(LivingEntity entity) {
        Minecraft minecraft = Minecraft.getInstance();
        return entity == minecraft.player && minecraft.options.getCameraType() == CameraType.FIRST_PERSON;
    }
}
