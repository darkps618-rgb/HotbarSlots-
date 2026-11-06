package com.example.hotbarprofiles;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HotbarProfilesClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("hotbarprofiles");

    private static KeyMapping openKey, nextKey, prevKey;

    @Override
    public void onInitializeClient() {
        ProfileManager.load();

        KeyMapping.Category category =
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath("hotbarprofiles", "main"));

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hotbarprofiles.open", InputConstants.Type.KEYBOARD, InputConstants.KEY_H, category));
        nextKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hotbarprofiles.next", InputConstants.Type.KEYBOARD, InputConstants.KEY_RBRACKET, category));
        prevKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hotbarprofiles.prev", InputConstants.Type.KEYBOARD, InputConstants.KEY_LBRACKET, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;
            while (openKey.consumeClick()) {
                if (client.gui.screen() == null) client.setScreenAndShow(new ProfileScreen());
            }
            while (nextKey.consumeClick()) {
                if (client.gui.screen() == null) ProfileManager.cycle(1, client);
            }
            while (prevKey.consumeClick()) {
                if (client.gui.screen() == null) ProfileManager.cycle(-1, client);
            }
        });
    }
}
