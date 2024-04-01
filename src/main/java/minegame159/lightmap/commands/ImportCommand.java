package minegame159.lightmap.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import minegame159.lightmap.LightMap;
import minegame159.lightmap.importer.WorldImporter;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.io.File;

import static net.minecraft.server.command.CommandManager.literal;

public class ImportCommand {
    public static LiteralArgumentBuilder<ServerCommandSource> register(LiteralArgumentBuilder<ServerCommandSource> root) {
        return root.then(literal("import").executes(context -> {
            LightMap.getInstance().importWorld();

            return 1;
        }));
    }
}
