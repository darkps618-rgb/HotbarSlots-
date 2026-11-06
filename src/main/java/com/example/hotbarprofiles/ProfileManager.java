package com.example.hotbarprofiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class ProfileManager {
    public static final int MAX_PROFILES = 8;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("hotbarprofiles.json");

    public static class Profile {
        public String name;
        public String[] slots = new String[9]; // item ids, null = empty
        public Profile() {}
        public Profile(String name) { this.name = name; }
    }

    private static class Store {
        List<Profile> profiles = new ArrayList<>();
        int active = -1;
    }

    private static Store store = new Store();

    private ProfileManager() {}

    // ---------- persistence ----------

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                Store s = GSON.fromJson(Files.readString(FILE), Store.class);
                if (s != null && s.profiles != null) store = s;
            }
        } catch (Exception e) {
            HotbarProfilesClient.LOGGER.error("Could not read hotbar profiles", e);
        }
    }

    public static void save() {
        try {
            Files.writeString(FILE, GSON.toJson(store));
        } catch (IOException e) {
            HotbarProfilesClient.LOGGER.error("Could not save hotbar profiles", e);
        }
    }

    // ---------- profile list ----------

    public static List<Profile> profiles() { return store.profiles; }
    public static int active() { return store.active; }

    public static boolean add(String name, LocalPlayer player) {
        if (store.profiles.size() >= MAX_PROFILES || name.isBlank()) return false;
        Profile p = new Profile(name.trim());
        capture(p, player);
        store.profiles.add(p);
        store.active = store.profiles.size() - 1;
        save();
        return true;
    }

    public static void remove(int index) {
        if (index < 0 || index >= store.profiles.size()) return;
        store.profiles.remove(index);
        if (store.active == index) store.active = -1;
        else if (store.active > index) store.active--;
        save();
    }

    public static void capture(Profile p, LocalPlayer player) {
        for (int i = 0; i < 9; i++) {
            ItemStack st = player.getInventory().getItem(i);
            p.slots[i] = st.isEmpty() ? null : BuiltInRegistries.ITEM.getKey(st.getItem()).toString();
        }
        save();
    }

    public static Item itemOf(String id) {
        if (id == null) return null;
        Identifier ident = Identifier.tryParse(id);
        if (ident == null || !BuiltInRegistries.ITEM.containsKey(ident)) return null;
        return BuiltInRegistries.ITEM.getValue(ident);
    }

    // ---------- applying a profile ----------

    public static void cycle(int dir, Minecraft client) {
        int n = store.profiles.size();
        if (n == 0 || client.player == null) return;
        int next = store.active < 0 ? (dir > 0 ? 0 : n - 1) : Math.floorMod(store.active + dir, n);
        apply(next, client);
    }

    /**
     * Rearranges the player's inventory so the hotbar matches the profile.
     * Uses vanilla "number key swap" clicks, so it works on servers too.
     * Matches by item type; empty profile slots are left untouched.
     */
    public static void apply(int index, Minecraft client) {
        LocalPlayer player = client.player;
        MultiPlayerGameMode gm = client.gameMode;
        if (player == null || gm == null || index < 0 || index >= store.profiles.size()) return;

        Profile profile = store.profiles.get(index);
        Inventory inv = player.getInventory();
        Item[] want = new Item[9];
        boolean[] ok = new boolean[9];

        for (int s = 0; s < 9; s++) {
            want[s] = itemOf(profile.slots[s]);
            ok[s] = want[s] == null || inv.getItem(s).is(want[s]);
        }

        int missing = 0;
        for (int s = 0; s < 9; s++) {
            if (ok[s]) continue;

            int source = -1;
            for (int i = 9; i < 36 && source < 0; i++) {          // main inventory first
                if (inv.getItem(i).is(want[s])) source = i;
            }
            for (int j = 0; j < 9 && source < 0; j++) {            // then misplaced hotbar items
                if (j != s && !ok[j] && inv.getItem(j).is(want[s])) source = j;
            }

            if (source < 0) { missing++; continue; }

            int menuSlot = source < 9 ? 36 + source : source;      // inventory menu slot ids
            gm.handleContainerInput(player.inventoryMenu.containerId, menuSlot, s, ContainerInput.SWAP, player);
            ok[s] = true;
        }

        store.active = index;
        save();
        String msg = "Hotbar profile: " + profile.name + (missing > 0 ? " (" + missing + " item(s) missing)" : "");
        player.sendOverlayMessage(Component.literal(msg));
    }
            }
