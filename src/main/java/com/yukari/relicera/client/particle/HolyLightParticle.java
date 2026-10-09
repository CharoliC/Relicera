package com.yukari.relicera.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class HolyLightParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    private HolyLightParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = 0.0D;
        this.yd = 0.0D;
        this.zd = 0.0D;
        this.lifetime = 16;
        this.quadSize = 1.25F;
        this.hasPhysics = false;
        updateSprite();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.removed) {
            updateSprite();
            if (this.age >= this.lifetime - 2) {
                this.alpha = (this.lifetime - this.age) / 2.0F;
            }
        }
    }

    private void updateSprite() {
        int frame = this.age < 2 || this.age >= 14 ? 3
                : this.age < 4 || this.age >= 12 ? 2
                : this.age < 6 || this.age >= 10 ? 1 : 0;
        this.setSprite(this.sprites.get(frame, 3));
    }

    @Override
    public int getLightColor(float partialTick) {
        return 0xF000F0;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new HolyLightParticle(level, x, y, z, this.sprites);
        }
    }
}
