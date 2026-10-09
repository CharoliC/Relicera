package com.yukari.relicera.client.compat.jade;

import com.yukari.relicera.common.block.SculkFruitCropBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public final class ReliceraJadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(SculkFruitGrowthProvider.INSTANCE, SculkFruitCropBlock.class);
        registration.markAsClientFeature(SculkFruitGrowthProvider.INSTANCE.getUid());
    }
}
