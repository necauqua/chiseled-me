package dev.necauqua.mods.cm.mixin.compat;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModClassLoader;
import net.minecraftforge.fml.common.ModContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.MixinProcessor;
import org.spongepowered.asm.mixin.transformer.Proxy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(value = Loader.class, remap = false)
public final class ClassloadingHackMixin {

    // yeah almost completely stolen from VanillaFix as the only way
    // to mixin into other mods in 1.12 that I know of

    @Inject(method = "loadMods", at = @At(value = "INVOKE", ordinal = 1, target = "Lnet/minecraftforge/fml/common/LoadController;transition(Lnet/minecraftforge/fml/common/LoaderState;Z)V"))
    private void theGrossestHackFromVanillaFixThatMumfreyDoesNotApprove(List<String> injectedModContainers, CallbackInfo ci) {

        // yeah technically if both vanillafix and this are present then this completely runs for the second time

        Set<URL> alreadyThere = new HashSet<>(Arrays.asList(modClassLoader.getURLs()));
        for (ModContainer mod : mods) {
            try {
                if (!alreadyThere.contains(mod.getSource().toURI().toURL())) {
                    modClassLoader.addFile(mod.getSource());
                }
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        Mixins.addConfiguration("chiseled-me.compat.mixins.json");

        try {
            Field transformerField = Proxy.class.getDeclaredField("transformer");
            transformerField.setAccessible(true);
            Object transformer = transformerField.get(null);

            Field processorField = transformer.getClass().getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);

            Method selectConfigsMethod = MixinProcessor.class.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            Method prepareConfigsMethod = MixinProcessor.class.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);
            prepareConfigsMethod.setAccessible(true);

            MixinEnvironment environment = MixinEnvironment.getCurrentEnvironment();
            selectConfigsMethod.invoke(processor, environment);
            prepareConfigsMethod.invoke(processor, environment);

        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Shadow
    private ModClassLoader modClassLoader;

    @Shadow
    private List<ModContainer> mods;
}
