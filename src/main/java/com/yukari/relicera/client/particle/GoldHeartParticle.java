package com.yukari.relicera.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public class GoldHeartParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private GoldHeartParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites, float scale) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed + 0.025D;
        this.zd = zSpeed;
        this.gravity = -0.006F;
        this.friction = 0.9F;
        this.lifetime = 38 + this.random.nextInt(12);
        this.alpha = 0.95F;
        this.quadSize *= scale;
        this.hasPhysics = false;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            float progress = (float) this.age / (float) this.lifetime;
            if (progress < 0.65F) {
                this.alpha = 0.95F;
            } else {
                this.alpha = 0.95F * (1.0F - (progress - 0.65F) / 0.35F);
            }
            this.setSpriteFromAge(this.sprites);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;
        private final float scale;

        public Provider(SpriteSet sprites, float scale) {
            this.sprites = sprites;
            this.scale = scale;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new GoldHeartParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites, this.scale);
        }
    }
}
