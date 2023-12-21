package com.nettakrim.fake_afk;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Data {
    private File data = null;

    public Data() {
        ServerLifecycleEvents.SERVER_STOPPED.register((server) -> save());
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> load());
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((server, resourceManager) -> load());
    }

    private void ResolveDataFile() {
        if (data != null) return;
        data = FabricLoader.getInstance().getConfigDir().resolve("fake_afk.txt").toFile();
    }

    private void save() {
        ResolveDataFile();
        try {
            String s = "";
            s += FakeAFK.instance.commands.savePermissions();
            s += FakeAFK.instance.connection.saveConfig();
            s += FakePlayerInfo.SavePlayerNames();
            FileWriter writer = new FileWriter(data);
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            FakeAFK.info("Failed to save data");
        }
    }

    private void load() {
        ResolveDataFile();
        try {
            data.createNewFile();
            PeekableScanner scanner = new PeekableScanner(new Scanner(data));
            FakeAFK.instance.commands.loadPermissions(scanner);
            FakeAFK.instance.connection .loadConfig(scanner);
            FakePlayerInfo.LoadPlayerNames(scanner);
            scanner.close();
        } catch (IOException e) {
            FakeAFK.info("Failed to load data");
        }
    }

}
