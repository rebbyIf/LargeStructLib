package dev.rebby.largestructlib.worldgen.density_function;

import dev.rebby.largestructlib.mixin.worldgen.RandomStateAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.synth.BlendedNoise;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Copied from Minecraft's Source Code & ApolloUnknownDev's Lithostiched!
 */
public class DensityFunctionWrapper implements DensityFunction.Visitor {
    private final Map<DensityFunction, DensityFunction> wrapped = new ConcurrentHashMap<>();
    private final boolean useLegacySource;
    private final long seed;
    final RandomState randomState;
    final PositionalRandomFactory random;

    public DensityFunctionWrapper(Structure.GenerationContext context, NoiseGeneratorSettings settings){
        this(context.seed(), settings.useLegacyRandomSource(), context.randomState(), ((RandomStateAccessor)(Object)context.randomState()).getRandom());
    }

    public DensityFunctionWrapper(long seed, boolean useLegacySource, RandomState randomState, PositionalRandomFactory random) {
        this.seed = seed;
        this.useLegacySource = useLegacySource;
        this.randomState = randomState;
        this.random = random;
    }


    private RandomSource newLegacyInstance(long seed) {
        return new LegacyRandomSource(this.seed + seed);
    }

    @SuppressWarnings("deprecation")
    public DensityFunction.@NotNull NoiseHolder visitNoise(DensityFunction.NoiseHolder noiseHolder) {
        Holder<NormalNoise.NoiseParameters> holder = noiseHolder.noiseData();
        if (this.useLegacySource) {
            if (holder.is(Noises.TEMPERATURE)) {
                NormalNoise normalnoise3 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(0L), new NormalNoise.NoiseParameters(-7, (double)1.0F, new double[]{(double)1.0F}));
                return new DensityFunction.NoiseHolder(holder, normalnoise3);
            }

            if (holder.is(Noises.VEGETATION)) {
                NormalNoise normalnoise2 = NormalNoise.createLegacyNetherBiome(this.newLegacyInstance(1L), new NormalNoise.NoiseParameters(-7, (double)1.0F, new double[]{(double)1.0F}));
                return new DensityFunction.NoiseHolder(holder, normalnoise2);
            }

            if (holder.is(Noises.SHIFT)) {
                NormalNoise normalnoise1 = NormalNoise.create(this.random.fromHashOf(Noises.SHIFT.location()), new NormalNoise.NoiseParameters(0, (double)0.0F, new double[0]));
                return new DensityFunction.NoiseHolder(holder, normalnoise1);
            }
        }

        NormalNoise normalnoise = this.randomState.getOrCreateNoise(holder.unwrapKey().orElseThrow());
        return new DensityFunction.NoiseHolder(holder, normalnoise);
    }

    private DensityFunction wrapNew(DensityFunction densityFunction) {
        if (densityFunction instanceof BlendedNoise blendednoise) {
            RandomSource randomsource = this.useLegacySource ? this.newLegacyInstance(0L) : this.random.fromHashOf(ResourceLocation.withDefaultNamespace("terrain"));
            return blendednoise.withNewRandom(randomsource);
        } else {
            return densityFunction instanceof DensityFunctions.EndIslandDensityFunction ? new DensityFunctions.EndIslandDensityFunction(this.seed) : densityFunction;
        }
    }

    public @NotNull DensityFunction apply(@NotNull DensityFunction densityFunction) {
        return this.wrapped.computeIfAbsent(densityFunction, this::wrapNew);
    }
}
