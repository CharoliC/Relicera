package com.yukari.relicera.common.entity.forgeling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

final class ForgelingSmithingGoal extends Goal {
    private static final double WORK_POSITION_OUTWARD_OFFSET = 0.12D;
    private static final double WORK_ARRIVAL_DISTANCE_SQR = 0.0225D;
    private static final double STRIKE_CENTER_TOLERANCE = 0.28D;
    private static final int RECIPE_CHECK_INTERVAL_TICKS = 10;
    private static final int HEART_PAUSE_TICKS = 24;
    private static final int HAMMERING_PAUSE_TICKS = 8;
    private static final int COMPLETION_PAUSE_TICKS = 28;
    private static final int RETURN_PAUSE_TICKS = 8;
    private static final int WORK_SETTLE_TICKS = 3;
    private static final float RETURN_TURN_TOLERANCE = 8.0F;

    private final Forgeling forgeling;
    @Nullable
    private ForgelingWorksiteManager.Binding binding;
    @Nullable
    private ForgelingSmithingTask task;
    private SmithingStep step = SmithingStep.PAUSING_BEFORE_RETURN;
    private int stepTicks;
    private int nextSearchTick;
    private long nextRecipeCheckTime;

    ForgelingSmithingGoal(Forgeling forgeling) {
        this.forgeling = forgeling;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (!(this.forgeling.level() instanceof ServerLevel)) {
            return false;
        }

        if (this.forgeling.getBoundTablePos() == null && --this.nextSearchTick > 0) {
            return false;
        }

        this.nextSearchTick = this.adjustedTickDelay(30 + this.forgeling.getRandom().nextInt(21));
        this.binding = ForgelingWorksiteManager.ensureBinding(this.forgeling).orElse(null);
        return this.binding != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.binding != null && ForgelingWorksiteManager.validateBinding(this.forgeling, this.binding);
    }

    @Override
    public void start() {
        beginReturn(true);
    }

    @Override
    public void stop() {
        this.task = null;
        this.forgeling.setWorkIdle(false);
        this.forgeling.stopSmithingMovement();
        if (this.binding != null && !ForgelingWorksiteManager.validateBinding(this.forgeling, this.binding)) {
            ForgelingWorksiteManager.releaseBinding(this.forgeling);
        }
        this.binding = null;
        setStep(SmithingStep.PAUSING_BEFORE_RETURN);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.binding == null || !(this.forgeling.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = tableCenter();
        Vec3 workPosition = workPosition();
        switch (this.step) {
            case WAITING_TO_LAND -> tickWaitingToLand();
            case PAUSING_BEFORE_RETURN -> tickReturnPause();
            case TURNING_TO_WORK -> tickTurningToWork(workPosition);
            case RETURNING_TO_WORK -> tickReturningToWork(workPosition);
            case SETTLING_AT_WORK -> tickSettlingAtWork(center);
            case IDLE -> tickIdle(level, center);
            case HEART_PAUSE -> tickHeartPause(center);
            case APPROACHING_TABLE -> tickApproachingTable(workPosition, center);
            case AIRBORNE_TO_TABLE -> tickAirborneToTable(center);
            case HAMMERING_PAUSE -> tickHammeringPause(center);
            case COMPLETION_PAUSE -> {
                this.forgeling.stopSmithingMovement();
                this.forgeling.holdSmithingDirection();
                if (++this.stepTicks >= COMPLETION_PAUSE_TICKS) {
                    beginReturn();
                }
            }
        }
    }

    private void tickWaitingToLand() {
        this.forgeling.setWorkIdle(false);
        this.forgeling.stopSmithingMovement();
        this.forgeling.holdSmithingDirection();
        if (this.forgeling.onGround()) {
            setStep(SmithingStep.PAUSING_BEFORE_RETURN);
        }
    }

    private void tickReturnPause() {
        this.forgeling.setWorkIdle(false);
        this.forgeling.stopSmithingMovement();
        this.forgeling.holdSmithingDirection();
        if (!this.forgeling.onGround()) {
            setStep(SmithingStep.WAITING_TO_LAND);
        } else if (++this.stepTicks >= RETURN_PAUSE_TICKS) {
            setStep(SmithingStep.TURNING_TO_WORK);
        }
    }

    private void tickTurningToWork(Vec3 workPosition) {
        this.forgeling.setWorkIdle(false);
        this.forgeling.stopSmithingMovement();
        if (!this.forgeling.onGround()) {
            this.forgeling.holdSmithingDirection();
            setStep(SmithingStep.WAITING_TO_LAND);
            return;
        }
        turnToward(workPosition);
        if (isFacing(workPosition)) {
            setStep(SmithingStep.RETURNING_TO_WORK);
        }
    }

    private void tickReturningToWork(Vec3 workPosition) {
        this.forgeling.setWorkIdle(false);
        if (!this.forgeling.onGround()) {
            this.forgeling.stopSmithingMovement();
            this.forgeling.holdSmithingDirection();
            correctFlightToward(workPosition);
        } else if (isSupportedByTable()) {
            if (isFacing(workPosition)) {
                launchReturnFromTable(workPosition);
            } else {
                setStep(SmithingStep.TURNING_TO_WORK);
            }
        } else if (isAtWorkPosition(workPosition)) {
            this.forgeling.holdSmithingDirection();
            setStep(SmithingStep.SETTLING_AT_WORK);
        } else if (!isFacing(workPosition)) {
            setStep(SmithingStep.TURNING_TO_WORK);
        } else {
            moveToward(workPosition, 1.0D);
        }
    }

    private void tickSettlingAtWork(Vec3 tableCenter) {
        this.forgeling.stopSmithingMovement();
        this.forgeling.holdSmithingDirection();
        if (++this.stepTicks >= WORK_SETTLE_TICKS) {
            enterIdle(tableCenter);
        }
    }

    private void tickIdle(ServerLevel level, Vec3 center) {
        this.forgeling.stopSmithingMovement();
        faceToward(center);
        this.forgeling.setWorkIdle(true);
        if (level.getGameTime() < this.nextRecipeCheckTime) {
            return;
        }

        this.nextRecipeCheckTime = level.getGameTime() + RECIPE_CHECK_INTERVAL_TICKS;
        this.task = ForgelingSmithingTask.tryCreate(level, this.binding.tablePos()).orElse(null);
        if (this.task != null) {
            level.sendParticles(
                    ParticleTypes.HEART,
                    this.forgeling.getX(), this.forgeling.getY() + 0.8D, this.forgeling.getZ(),
                    4, 0.25D, 0.2D, 0.25D, 0.01D
            );
            this.forgeling.setWorkIdle(false);
            setStep(SmithingStep.HEART_PAUSE);
        }
    }

    private void tickHeartPause(Vec3 center) {
        this.forgeling.stopSmithingMovement();
        faceToward(center);
        if (!taskIsValid()) {
            beginReturn();
        } else if (++this.stepTicks >= HEART_PAUSE_TICKS) {
            this.forgeling.launchSmithingJump(center);
            setStep(SmithingStep.AIRBORNE_TO_TABLE);
        } else if (this.stepTicks >= HEART_PAUSE_TICKS - 5) {
            this.forgeling.anticipateSmithingJump();
        }
    }

    private void tickApproachingTable(Vec3 workPosition, Vec3 center) {
        if (!taskIsValid()) {
            beginReturn();
        } else if (this.forgeling.onGround() && isValidStrikePosition(center)) {
            recordStrike();
        } else if (this.forgeling.onGround() && isSupportedByTable()) {
            this.forgeling.launchSmithingJump(center);
            setStep(SmithingStep.AIRBORNE_TO_TABLE);
        } else if (isAtWorkPosition(workPosition)) {
            this.forgeling.launchSmithingJump(center);
            setStep(SmithingStep.AIRBORNE_TO_TABLE);
        } else {
            moveToward(workPosition, 1.0D);
        }
    }

    private void tickAirborneToTable(Vec3 center) {
        if (!taskIsValid()) {
            beginReturn();
            return;
        }

        this.forgeling.stopSmithingMovement();
        correctFlightToward(center);
        if (++this.stepTicks <= 4 || !this.forgeling.onGround()) {
            return;
        }

        if (isValidStrikePosition(center)) {
            recordStrike();
        } else if (isSupportedByTable()) {
            this.forgeling.launchSmithingJump(center);
            this.stepTicks = 0;
        } else {
            setStep(SmithingStep.APPROACHING_TABLE);
        }
    }

    private void tickHammeringPause(Vec3 center) {
        this.forgeling.stopSmithingMovement();
        faceToward(center);
        if (!taskIsValid()) {
            beginReturn();
        } else if (!isValidStrikePosition(center)) {
            setStep(SmithingStep.APPROACHING_TABLE);
        } else if (++this.stepTicks >= HAMMERING_PAUSE_TICKS) {
            this.forgeling.launchSmithingJump(center);
            setStep(SmithingStep.AIRBORNE_TO_TABLE);
        }
    }

    private void recordStrike() {
        if (this.task == null || !(this.forgeling.level() instanceof ServerLevel level)) {
            beginReturn();
            return;
        }

        ForgelingSmithingTask.StrikeResult result = this.task.strike(level, this.binding.tablePos());
        if (result == ForgelingSmithingTask.StrikeResult.CONTINUE) {
            setStep(SmithingStep.HAMMERING_PAUSE);
        } else if (result == ForgelingSmithingTask.StrikeResult.COMPLETED) {
            this.task = null;
            this.forgeling.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 0));
            setStep(SmithingStep.COMPLETION_PAUSE);
        } else {
            beginReturn();
        }
    }

    private boolean taskIsValid() {
        return this.task != null
                && this.forgeling.level() instanceof ServerLevel level
                && this.task.isValid(level, this.binding.tablePos());
    }

    private void beginReturn() {
        beginReturn(false);
    }

    private void beginReturn(boolean pauseBeforeTurning) {
        this.task = null;
        this.forgeling.setWorkIdle(false);
        Vec3 workPosition = workPosition();
        this.forgeling.holdSmithingDirection();

        if (!this.forgeling.onGround()) {
            setStep(pauseBeforeTurning ? SmithingStep.WAITING_TO_LAND : SmithingStep.RETURNING_TO_WORK);
        } else if (isSupportedByTable()) {
            setStep(SmithingStep.TURNING_TO_WORK);
        } else if (isAtWorkPosition(workPosition)) {
            setStep(SmithingStep.SETTLING_AT_WORK);
        } else {
            setStep(pauseBeforeTurning ? SmithingStep.PAUSING_BEFORE_RETURN : SmithingStep.TURNING_TO_WORK);
        }
    }

    private void launchReturnFromTable(Vec3 workPosition) {
        this.forgeling.stopSmithingMovement();
        this.forgeling.holdSmithingDirection();
        this.forgeling.launchSmithingJump(workPosition);
        setStep(SmithingStep.RETURNING_TO_WORK);
    }

    private void enterIdle(Vec3 tableCenter) {
        this.forgeling.stopSmithingMovement();
        faceToward(tableCenter);
        this.forgeling.setWorkIdle(true);
        setStep(SmithingStep.IDLE);
    }

    private void setStep(SmithingStep step) {
        this.step = step;
        this.stepTicks = 0;
    }

    private Vec3 tableCenter() {
        return new Vec3(
                this.binding.tablePos().getX() + 0.5D,
                this.binding.tablePos().getY() + 1.0D,
                this.binding.tablePos().getZ() + 0.5D
        );
    }

    private Vec3 workPosition() {
        BlockPos tablePos = this.binding.tablePos();
        BlockPos workPos = this.binding.workPos();
        return Vec3.atBottomCenterOf(workPos).add(
                Integer.signum(workPos.getX() - tablePos.getX()) * WORK_POSITION_OUTWARD_OFFSET,
                0.0D,
                Integer.signum(workPos.getZ() - tablePos.getZ()) * WORK_POSITION_OUTWARD_OFFSET
        );
    }

    private boolean isSupportedByTable() {
        BlockPos tablePos = this.binding.tablePos();
        AABB bounds = this.forgeling.getBoundingBox();
        return this.forgeling.onGround()
                && Math.abs(this.forgeling.getY() - (tablePos.getY() + 1.0D)) <= 0.15D
                && bounds.maxX > tablePos.getX()
                && bounds.minX < tablePos.getX() + 1.0D
                && bounds.maxZ > tablePos.getZ()
                && bounds.minZ < tablePos.getZ() + 1.0D;
    }

    private boolean isValidStrikePosition(Vec3 center) {
        return isSupportedByTable()
                && Math.abs(this.forgeling.getX() - center.x) <= STRIKE_CENTER_TOLERANCE
                && Math.abs(this.forgeling.getZ() - center.z) <= STRIKE_CENTER_TOLERANCE;
    }

    private boolean isAtWorkPosition(Vec3 workPosition) {
        if (!this.forgeling.onGround() || horizontalDistanceSqr(workPosition) > WORK_ARRIVAL_DISTANCE_SQR) {
            return false;
        }

        BlockPos floorBelow = BlockPos.containing(
                this.forgeling.getX(),
                this.forgeling.getY() - 0.1D,
                this.forgeling.getZ()
        );
        return floorBelow.equals(this.binding.workPos().below()) && isClearOfTable();
    }

    private boolean isClearOfTable() {
        BlockPos tablePos = this.binding.tablePos();
        AABB bounds = this.forgeling.getBoundingBox();
        return bounds.maxX <= tablePos.getX()
                || bounds.minX >= tablePos.getX() + 1.0D
                || bounds.maxZ <= tablePos.getZ()
                || bounds.minZ >= tablePos.getZ() + 1.0D;
    }

    private void correctFlightToward(Vec3 target) {
        if (this.forgeling.onGround()) {
            return;
        }

        Vec3 movement = this.forgeling.getDeltaMovement();
        double desiredX = Mth.clamp((target.x - this.forgeling.getX()) * 0.18D, -0.34D, 0.34D);
        double desiredZ = Mth.clamp((target.z - this.forgeling.getZ()) * 0.18D, -0.34D, 0.34D);
        this.forgeling.setDeltaMovement(
                Mth.lerp(0.3D, movement.x, desiredX),
                movement.y,
                Mth.lerp(0.3D, movement.z, desiredZ)
        );
    }

    private double horizontalDistanceSqr(Vec3 target) {
        double x = target.x - this.forgeling.getX();
        double z = target.z - this.forgeling.getZ();
        return x * x + z * z;
    }

    private void moveToward(Vec3 target, double speed) {
        this.forgeling.setQuickSmithingDirection(yawToward(target));
        this.forgeling.setSmithingMovement(speed);
    }

    private void faceToward(Vec3 target) {
        this.forgeling.setSmithingDirection(yawToward(target));
    }

    private void turnToward(Vec3 target) {
        this.forgeling.setSlowSmithingDirection(yawToward(target));
    }

    private boolean isFacing(Vec3 target) {
        return Math.abs(Mth.wrapDegrees(yawToward(target) - this.forgeling.getYRot()))
                <= RETURN_TURN_TOLERANCE;
    }

    private float yawToward(Vec3 target) {
        double x = target.x - this.forgeling.getX();
        double z = target.z - this.forgeling.getZ();
        return (float) (Mth.atan2(z, x) * Mth.RAD_TO_DEG) - 90.0F;
    }

    private enum SmithingStep {
        WAITING_TO_LAND,
        PAUSING_BEFORE_RETURN,
        TURNING_TO_WORK,
        RETURNING_TO_WORK,
        SETTLING_AT_WORK,
        IDLE,
        HEART_PAUSE,
        APPROACHING_TABLE,
        AIRBORNE_TO_TABLE,
        HAMMERING_PAUSE,
        COMPLETION_PAUSE
    }
}
