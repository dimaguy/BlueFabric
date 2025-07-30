package me.dimaguy.bluefabric;

import com.flowpowered.math.vector.Vector2d;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapWorld;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.border.WorldBorder;

public class Bluefabric implements DedicatedServerModInitializer {
    public static MinecraftServer minecraftServer;
    private static final String MARKER_SET_ID = "worldborder";
    private static final String LABEL = "World border";
    private static final String DEFAULT_COLOR = "FF0000";
    private static final Color color = new Color(Integer.parseInt(DEFAULT_COLOR.toLowerCase(), 16), 1f);

    @Override
    public void onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTED.register((server) -> {;
            minecraftServer = server;
        });
        BlueMapAPI.onEnable(Bluefabric::init);
        BlueMapAPI.onDisable(Bluefabric::destroy);
    }

    public static void init(BlueMapAPI blueMapAPI) {
        for (var world : minecraftServer.getWorlds()) {
            var worldName = world.getRegistryKey().getValue().toString();
            final MarkerSet markerSet = MarkerSet.builder().label(LABEL).build();
            final WorldBorder worldBorder = world.getWorldBorder();
            final double centerX = worldBorder.getCenterX();
            final double centerZ = worldBorder.getCenterZ();
            final double radius = worldBorder.getSize() / 2d;
            final Vector2d pos1 = new Vector2d(centerX - radius, centerZ - radius);
            final Vector2d pos2 = new Vector2d(centerX + radius, centerZ + radius);
            final Shape border = Shape.createRect(pos1, pos2);
            final ShapeMarker marker = ShapeMarker.builder()
                    .label(LABEL)
                    .shape(border, world.getSeaLevel())
                    .lineColor(color)
                    .fillColor(new Color(0))
                    .lineWidth(3)
                    .depthTestEnabled(false)
                    .build();
            markerSet.getMarkers().put(worldName, marker);
            blueMapAPI.getWorld(worldName)
                    .map(BlueMapWorld::getMaps)
                    .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().put(MARKER_SET_ID, markerSet)));
        }
    }
    public static void destroy(BlueMapAPI blueMapAPI) {
        for (var world : minecraftServer.getWorlds()) {
            var worldName = world.getRegistryKey().getValue().toString();
            blueMapAPI.getWorld(worldName)
                    .map(BlueMapWorld::getMaps)
                    .ifPresent(maps -> maps.forEach(map -> map.getMarkerSets().remove(MARKER_SET_ID)));
        }
    }
}
