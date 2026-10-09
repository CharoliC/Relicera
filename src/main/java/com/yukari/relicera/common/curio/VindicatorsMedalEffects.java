package com.yukari.relicera.common.curio;

import com.yukari.relicera.registry.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import top.theillusivec4.curios.api.CuriosApi;

public final class VindicatorsMedalEffects {
    private VindicatorsMedalEffects() {
    }

    public static boolean isEquipped(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .resolve()
                .map(handler -> handler.isEquipped(ModItems.VINDICATORS_MEDAL.get()))
                .orElse(false);
    }

    static void dropRandomVillagerTrade(LivingEntity target) {
        if (!(target instanceof Villager villager)) {
            return;
        }

        VillagerProfession profession = villager.getVillagerData().getProfession();
        if (profession == VillagerProfession.NONE || profession == VillagerProfession.NITWIT) {
            return;
        }

        MerchantOffers offers = villager.getOffers();
        if (offers.isEmpty()) {
            return;
        }

        ItemStack result = offers.get(villager.getRandom().nextInt(offers.size())).assemble();
        if (!result.isEmpty()) {
            villager.spawnAtLocation(result);
        }
    }
}
