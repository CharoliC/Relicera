package com.yukari.relicera.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;

public final class FlyParticle extends TextureSheetParticle {
    private static final int FLAP_INTERVAL = 2;

    private final SpriteSet sprites;

    private FlyParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            SpriteSet sprites
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.friction = 0.94F;
        this.lifetime = 28 + this.random.nextInt(17);
        this.quadSize *= 0.92F;
        this.hasPhysics = false;
        updateSprite();
    }

    @Override
    public void tick() {
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.xd += (this.random.nextDouble() - 0.5D) * 0.016D;
        this.yd += (this.random.nextDouble() - 0.48D) * 0.010D;
        this.zd += (this.random.nextDouble() - 0.5D) * 0.016D;
        this.xd = clampVelocity(this.xd, 0.045D);
        this.yd = clampVelocity(this.yd, 0.025D);
        this.zd = clampVelocity(this.zd, 0.045D);
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        if (this.age > this.lifetime - 8) {
            this.alpha = Math.max(0.0F, (this.lifetime - this.age) / 8.0F);
        }
        updateSprite();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    private void updateSprite() {
        this.setSprite(this.sprites.get((this.age / FLAP_INTERVAL) % 2, 1));
    }

    private static double clampVelocity(double velocity, double limit) {
        return Math.max(-limit, Math.min(limit, velocity));
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            return new FlyParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
