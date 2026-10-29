package dev.rebby.largestructlib.worldgen.feature.configuration;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.rebby.largestructlib.worldgen.density_function.DensityFunctionWrapper;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DensitySamplerConfiguration(Holder<DensityFunction> functionHolder, List<Sample> samples, boolean replace) implements FeatureConfiguration {

    public static final Codec<DensitySamplerConfiguration> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    DensityFunction.CODEC.fieldOf("function").forGetter(DensitySamplerConfiguration::functionHolder),
                    Sample.CODEC.listOf().fieldOf("samples").forGetter(DensitySamplerConfiguration::samples),
                    Codec.BOOL.optionalFieldOf("replace", false).forGetter(DensitySamplerConfiguration::replace)
            ).apply(instance, DensitySamplerConfiguration::new));

    public record Sample(BlockStateProvider stateProvider, double min, double max) {

        public static final Codec<Sample> CODEC = RecordCodecBuilder.create(
                instance -> instance.group(
                        BlockStateProvider.CODEC.fieldOf("state_provider").forGetter(Sample::stateProvider),
                        Codec.DOUBLE.fieldOf("minimum").forGetter(Sample::min),
                        Codec.DOUBLE.fieldOf("maximum").forGetter(Sample::max)

                ).apply(instance, Sample::new)
        );
    }

}
