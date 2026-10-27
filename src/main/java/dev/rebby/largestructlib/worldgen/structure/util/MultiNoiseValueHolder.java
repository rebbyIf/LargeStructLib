package dev.rebby.largestructlib.worldgen.structure.util;

import dev.rebby.largestructlib.LargeStructureLib;
import dev.rebby.largestructlib.worldgen.density_function.DensityFunctionWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.event.server.ServerLifecycleEvent;

import java.util.HashMap;
import java.util.Map;

public class MultiNoiseValueHolder {

    private static MultiNoiseValueHolder instance = null;

    public static MultiNoiseValueHolder getInstance(){
        if (instance == null) {
            instance = new MultiNoiseValueHolder();
        }
        for (NoiseValueHolder noise : instance.noises.values()) {
            if (noise.values.size() > 1000000){
                noise.values = new HashMap<>();
            }
        }
        return instance;
    }

    private final Map<String, NoiseValueHolder> noises;

    private MultiNoiseValueHolder(){
        noises = new HashMap<>();
    }

    public void instantiate(Holder<DensityFunction> noise,
                                       Structure.GenerationContext context,
                                       NoiseGeneratorSettings settings){
        String key = noise.getRegisteredName();
        if (noises.get(key) == null){
            noises.put(key, new NoiseValueHolder(noise.value().mapAll(new DensityFunctionWrapper(context, settings))));
        }
    }

    public double getValue(Holder<DensityFunction> key, BlockPos pos){
        NoiseValueHolder noise = noises.get(key.getRegisteredName());
        if (noise == null){
            return 0;
        }
        return noise.getValue(pos);
    }

    @EventBusSubscriber(modid = LargeStructureLib.MODID)
    public static class EventHandler{
        @SubscribeEvent
        private static void handleWorldLoad(ServerAboutToStartEvent event){
            instance = new MultiNoiseValueHolder();
            LargeStructureLib.LOGGER.info("Reloading Processor");

        }
    }

    private static class NoiseValueHolder {
        private final DensityFunction noise;
        private Map<BlockPos, Double> values;

        private NoiseValueHolder(DensityFunction noise){
            this.noise = noise;
            this.values = new HashMap<>();
        }

        private double getValue(BlockPos key){
            Double value = values.get(key);
            if (value == null){
                value = noise.compute(new DensityFunction.SinglePointContext(key.getX(), key.getY(), key.getZ()));
                values.put(key, value);
            }
            return value;
        }
    }
}
