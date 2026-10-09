package com.yukari.relicera.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class ReliceraRenderTypes extends RenderType {
    private static final Function<ResourceLocation, RenderType> INVERTED_GLOW = Util.memoize(texture ->
            RenderType.create(
                    "relicera_inverted_glow",
                    DefaultVertexFormat.NEW_ENTITY,
                    VertexFormat.Mode.QUADS,
                    256,
                    false,
                    true,
                    CompositeState.builder()
                            .setTextureState(new TextureStateShard(texture, false, false))
                            .setShaderState(RENDERTYPE_EYES_SHADER)
                            .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                            .setCullState(CULL)
                            .setDepthTestState(LEQUAL_DEPTH_TEST)
                            .setWriteMaskState(COLOR_DEPTH_WRITE)
                            .createCompositeState(false)));

    private ReliceraRenderTypes(
            String name,
            VertexFormat format,
            VertexFormat.Mode mode,
            int bufferSize,
            boolean affectsCrumbling,
            boolean sortOnUpload,
            Runnable setupState,
            Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
        throw new UnsupportedOperationException();
    }

    public static RenderType invertedGlow(ResourceLocation texture) {
        return INVERTED_GLOW.apply(texture);
    }
}
