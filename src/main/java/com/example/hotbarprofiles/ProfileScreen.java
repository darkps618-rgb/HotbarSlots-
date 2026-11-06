package com.example.hotbarprofiles;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ProfileScreen extends Screen {
    private static final int ROW_H = 24;
    private static final int TOP = 40;

    private EditBox nameField;

    public ProfileScreen() {
        super(Component.literal("Hotbar Profiles"));
    }

    private int left() { return width / 2 - 190; }

    @Override
    protected void init() {
        List<ProfileManager.Profile> profiles = ProfileManager.profiles();
        int left = left();

        for (int i = 0; i < profiles.size(); i++) {
            final int idx = i;
            int y = TOP + i * ROW_H;

            addRenderableWidget(Button.builder(Component.literal("Load"), b -> {
                ProfileManager.apply(idx, minecraft);
                onClose();
            }).bounds(left + 252, y, 44, 20).build());

            addRenderableWidget(Button.builder(Component.literal("Save"), b -> {
                ProfileManager.capture(ProfileManager.profiles().get(idx), minecraft.player);
                rebuildWidgets();
            }).bounds(left + 298, y, 44, 20).build());

            addRenderableWidget(Button.builder(Component.literal("X"), b -> {
                ProfileManager.remove(idx);
                rebuildWidgets();
            }).bounds(left + 344, y, 24, 20).build());
        }

        int by = TOP + ProfileManager.MAX_PROFILES * ROW_H + 8;
        nameField = new EditBox(font, left, by, 220, 20, Component.literal("Profile name"));
        nameField.setMaxLength(16);
        nameField.setHint(Component.literal("New profile name..."));
        addRenderableWidget(nameField);

        addRenderableWidget(Button.builder(Component.literal("Save current hotbar as new"), b -> {
            if (ProfileManager.add(nameField.getValue(), minecraft.player)) rebuildWidgets();
        }).bounds(left + 226, by, 154, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        g.centeredText(font, title, width / 2, 14, 0xFFFFFFFF);

        List<ProfileManager.Profile> profiles = ProfileManager.profiles();
        if (profiles.isEmpty()) {
            g.centeredText(font,
                    Component.literal("No profiles yet. Arrange your hotbar, name it below, and save."),
                    width / 2, TOP + 6, 0xFFAAAAAA);
        }

        int left = left();
        for (int i = 0; i < profiles.size(); i++) {
            ProfileManager.Profile p = profiles.get(i);
            int y = TOP + i * ROW_H;
            int color = (i == ProfileManager.active()) ? 0xFFFFAA00 : 0xFFFFFFFF;
            g.text(font, p.name, left, y + 6, color);

            for (int s = 0; s < 9; s++) {
                int x = left + 85 + s * 18;
                g.fill(x, y + 1, x + 17, y + 18, 0x66000000);
                Item item = ProfileManager.itemOf(p.slots[s]);
                if (item != null) g.item(new ItemStack(item), x, y + 1);
            }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
