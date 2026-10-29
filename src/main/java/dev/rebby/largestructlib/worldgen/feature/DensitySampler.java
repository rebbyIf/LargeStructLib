package dev.rebby.largestructlib.worldgen.feature;

import com.mojang.serialization.Codec;
import dev.rebby.largestructlib.mixin.worldgen.RandomStateAccessor;
import dev.rebby.largestructlib.worldgen.density_function.DensityFunctionWrapper;
import dev.rebby.largestructlib.worldgen.feature.configuration.DensitySamplerConfiguration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class DensitySampler extends Feature<DensitySamplerConfiguration> {

    public DensityFunction function;

    public DensitySampler(Codec<DensitySamplerConfiguration> codec) {
        super(codec);
        function = null;
    }

    @Override
    public boolean place(@NotNull FeaturePlaceContext<DensitySamplerConfiguration> context) {
        ChunkGenerator generator = context.chunkGenerator();
        WorldGenLevel level = context.level();
        RegistryAccess registryaccess = level.registryAccess();
        NoiseGeneratorSettings noiseGeneratorSettings = NoiseGeneratorSettings.dummy();

        if (generator instanceof NoiseBasedChunkGenerator noisebasedchunkgenerator){
            noiseGeneratorSettings = noisebasedchunkgenerator.generatorSettings().value();
        }

        if (function == null) {
            RandomState randomState = RandomState.create(noiseGeneratorSettings, registryaccess.lookupOrThrow(Registries.NOISE), context.level().getSeed());
            function = context.config().functionHolder().value().mapAll(new DensityFunctionWrapper(
                    context.level().getSeed(),
                    noiseGeneratorSettings.useLegacyRandomSource(),
                    randomState,
                    ((RandomStateAccessor)(Object)randomState).getRandom()
            ));
        }

        BlockPos min = new BlockPos((context.origin().getX() >> 4) << 4,
                generator.getMinY(),
                (context.origin().getZ() >> 4) << 4);
        BlockPos max = new BlockPos(((context.origin().getX() >> 4) << 4) + 16,
                level.getMaxBuildHeight(),
                ((context.origin().getZ() >> 4) << 4) + 16);

        boolean placed = false;

        for (int x = min.getX(); x < max.getX(); x++) {
            for (int y = min.getY(); y < max.getY(); y++) {
                for (int z = min.getZ(); z < max.getZ(); z++) {
                    for (DensitySamplerConfiguration.Sample sample : context.config().samples()) {
                        double d = function.compute(new DensityFunction.SinglePointContext(x,y,z));
                        BlockPos pos = new BlockPos(x,y,z);
                        boolean flag = level.getBlockState(pos).isAir() || context.config().replace();
                        if (d >= sample.min() && d < sample.max() && flag) {
                            placed = true;
                            level.setBlock(pos, sample.stateProvider().getState(context.random(), pos), 2);
                            break;
                        }

                    }
                }
            }
        }

        return placed;
    }
}
