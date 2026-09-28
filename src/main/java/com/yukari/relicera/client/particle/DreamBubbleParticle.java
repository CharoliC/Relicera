package com.yukari.relicera.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class DreamBubbleParticle extends TextureSheetParticle {
    private static final int POP_DURATION_TICKS = 5;

    private final SpriteSet sprites;

    private DreamBubbleParticle(ClientLevel level, double x, double y, double z, SpriteSet sprites) {
        super(level, x, y, z);
        this.sprites = sprites;
        this.xd = (this.random.nextDouble() - 0.5D) * 0.014D;
        this.yd = 0.012D + this.random.nextDouble() * 0.014D;
        this.zd = (this.random.nextDouble() - 0.5D) * 0.014D;
        this.gravity = -0.0005F;
        this.friction = 0.92F;
        this.lifetime = 30 + this.random.nextInt(11);
        this.quadSize *= 1.0F + this.random.nextFloat() * 0.4F;
        this.hasPhysics = false;
        updateSprite();
    }

    @Override
    public void tick() {
        this.xd += (this.random.nextDouble() - 0.5D) * 0.002D;
        this.zd += (this.random.nextDouble() - 0.5D) * 0.002D;
        super.tick();
        if (!this.removed) {
            updateSprite();
            if (this.age >= this.lifetime - 3) {
                this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 3.0F);
            }
        }
    }

    private void updateSprite() {
        this.setSprite(this.sprites.get(this.age >= this.lifetime - POP_DURATION_TICKS ? 1 : 0, 1));
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
            return new DreamBubbleParticle(level, x, y, z, this.sprites);
        }
    }
}
