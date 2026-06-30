package com.yukari.relicera.registry;

import com.yukari.relicera.ReliceraMod;
import com.yukari.relicera.common.menu.FourfoldSherdPendantMenu;
import com.yukari.relicera.common.menu.RelicRepairTableMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, ReliceraMod.MOD_ID);

    public static final RegistryObject<MenuType<RelicRepairTableMenu>> RELIC_REPAIR_TABLE =
            MENU_TYPES.register("relic_repair_table", () -> IForgeMenuType.create(RelicRepairTableMenu::new));

    public static final RegistryObject<MenuType<FourfoldSherdPendantMenu>> FOURFOLD_SHERD_PENDANT =
            MENU_TYPES.register("fourfold_sherd_pendant", () -> IForgeMenuType.create(FourfoldSherdPendantMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
