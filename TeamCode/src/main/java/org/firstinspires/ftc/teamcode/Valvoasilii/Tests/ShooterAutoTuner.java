//package org.firstinspires.ftc.teamcode.Valvoasilii.Tests;
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.util.ElapsedTime;
//
//import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
//
//import java.util.ArrayDeque;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * yes ts is vibecoded
// *
// * ShooterAutotuner
// *
// * Automatically determines kV, kS, and kP for the custom Shooter subsystem.
// *
// * The Shooter subsystem is NOT modified.
// *
// * Shooter.update() refreshes the velocity reading but also writes motor power.
// * Therefore, every update() call is immediately followed by shooter.power(...)
// * to restore the autotuner's commanded power.
// *
// * Control equation:
// *
// * power = kV * target
// *       + kP * (target - velocity)
// *       + kS
// */
//@Autonomous(name = "Shooter Autotuner Verified", group = "Tests")
//public class ShooterAutoTuner extends LinearOpMode {
//
//    // ================================================================
//    // CONFIG
//    // ================================================================
//
//    // Moving-average filter window used during Phase 1
//    private static final int FILTER_WINDOW = 8;
//
//    // ------------------------- Phase 1 -------------------------
//
//    private static final double RAMP_POWER_STEP = 0.02;
//    private static final double RAMP_MAX_POWER = 1.0;
//
//    private static final long RAMP_HOLD_MS = 350;
//    private static final long RAMP_SETTLE_CHECK_MS = 40;
//
//    private static final double RAMP_SETTLE_VEL_SLOPE = 4.0;
//    private static final int RAMP_SETTLE_STREAK = 4;
//
//    private static final double MIN_MOVING_VELOCITY = 10.0;
//
//    // ------------------------- Phase 2 -------------------------
//
//    private static final double KP_START = 0.0001;
//    private static final double KP_SEARCH_MULTIPLIER = 2.0;
//    private static final double KP_MAX = 0.05;
//
//    private static final int KP_BINARY_SEARCH_ITERS = 6;
//
//    private static final double KP_BACKOFF_FACTOR = 0.75;
//
//    private static final long STEP_TEST_DURATION_MS = 1200;
//    private static final long STEP_SETTLE_WAIT_MS = 1000;
//
//    private static final double STEP_TARGET_FRACTION = 0.7;
//
//    private static final int OSCILLATION_CROSSINGS_THRESHOLD = 3;
//
//    private static final double OVERSHOOT_FRACTION_THRESHOLD = 0.25;
//
//    private static final double NOISE_DEADBAND = 25.0;
//
//    // ================================================================
//    // STATE
//    // ================================================================
//
//    private Shooter shooter;
//
//    private final ArrayDeque<Double> filterWindow = new ArrayDeque<>();
//    private double filterSum = 0.0;
//
//    /*
//     * Power currently commanded by the autotuner.
//     *
//     * Shooter.update() will overwrite the motor power because your
//     * Shooter subsystem is disabled. We immediately restore this value
//     * after every update().
//     */
//    private double currentPower = 0.0;
//
//    // ================================================================
//    // OP MODE
//    // ================================================================
//
//    @Override
//    public void runOpMode() {
//
//        shooter = new Shooter(hardwareMap);
//
//        /*
//         * Leave your Shooter subsystem unchanged.
//         *
//         * Keeping it disabled means update() will:
//         *
//         *   1. Read bottom motor velocity into current
//         *   2. Set motor power to 0
//         *
//         * We immediately override that 0 with the autotuner's power.
//         */
//        shooter.disable();
//
//        telemetry.addLine("Shooter Autotuner Ready.");
//        telemetry.addLine("Custom Shooter subsystem loaded.");
//        telemetry.addLine("Ensure flywheel is clear to spin freely.");
//        telemetry.update();
//
//        waitForStart();
//
//        if (isStopRequested()) {
//            return;
//        }
//
//        resetFilter();
//
//        // ============================================================
//        // PHASE 1: kV / kS
//        // ============================================================
//
//        telemetry.clearAll();
//        telemetry.addLine("Phase 1: Ramp test (finding kV, kS)...");
//        telemetry.update();
//
//        List<double[]> samples = runRampTest();
//
//        setPower(0);
//
//        double[] fit = linearRegression(samples);
//
//        double kV = fit[0];
//        double kSRaw = fit[1];
//
//        double noiseFloor = estimateNoiseFloor();
//
//        double kS =
//                (Math.abs(kSRaw) < noiseFloor)
//                        ? 0.0
//                        : Math.max(0.0, kSRaw);
//
//        double maxObservedVelocity = 0.0;
//
//        for (double[] sample : samples) {
//            maxObservedVelocity =
//                    Math.max(maxObservedVelocity, sample[1]);
//        }
//
//        telemetry.clearAll();
//
//        telemetry.addLine("Phase 1 complete.");
//
//        telemetry.addData(
//                "kV",
//                "%.8f",
//                kV
//        );
//
//        telemetry.addData(
//                "kS (raw fit)",
//                "%.6f",
//                kSRaw
//        );
//
//        telemetry.addData(
//                "kS (used)",
//                "%.6f",
//                kS
//        );
//
//        telemetry.addData(
//                "Max velocity (ticks/s)",
//                "%.1f",
//                maxObservedVelocity
//        );
//
//        telemetry.update();
//
//        sleep(1500);
//
//        if (!opModeIsActive()) {
//            return;
//        }
//
//        // ============================================================
//        // PHASE 2: kP
//        // ============================================================
//
//        telemetry.clearAll();
//
//        telemetry.addLine(
//                "Phase 2: Step-response search (finding kP)..."
//        );
//
//        telemetry.update();
//
//        double targetVelocity =
//                maxObservedVelocity * STEP_TARGET_FRACTION;
//
//        double criticalKp =
//                findCriticalKp(
//                        kV,
//                        kS,
//                        targetVelocity
//                );
//
//        double finalKp =
//                criticalKp * KP_BACKOFF_FACTOR;
//
//        setPower(0);
//
//        // ============================================================
//        // RESULTS
//        // ============================================================
//
//        telemetry.clearAll();
//
//        telemetry.addLine("=== Autotune Results ===");
//
//        telemetry.addData(
//                "kV",
//                "%.8f",
//                kV
//        );
//
//        telemetry.addData(
//                "kS",
//                "%.6f",
//                kS
//        );
//
//        telemetry.addData(
//                "kP (critical)",
//                "%.8f",
//                criticalKp
//        );
//
//        telemetry.addData(
//                "kP (final)",
//                "%.8f",
//                finalKp
//        );
//
//        telemetry.addData(
//                "Test Target Velocity",
//                "%.1f ticks/s",
//                targetVelocity
//        );
//
//        telemetry.addLine("---------------------------------");
//        telemetry.addLine("Control Equation:");
//        telemetry.addLine(
//                "power = kV*target + kP*(target - velocity) + kS"
//        );
//
//        telemetry.update();
//
//        // Hold results on screen
//        while (opModeIsActive()) {
//            telemetry.update();
//        }
//    }
//
//    // ================================================================
//    // SHOOTER CONTROL
//    // ================================================================
//
//    /**
//     * Calls Shooter.update() so that Shooter.current gets refreshed.
//     *
//     * Since Shooter is disabled, update() also calls power(0).
//     * We immediately restore currentPower afterward.
//     */
//    private void updateShooter() {
//
//        shooter.update();
//
//        // Override the power(0) from Shooter.update()
//        shooter.power(currentPower);
//    }
//
//    /**
//     * Set the power used by the autotuner.
//     */
//    private void setPower(double power) {
//
//        currentPower =
//                Math.max(
//                        0.0,
//                        Math.min(
//                                1.0,
//                                power
//                        )
//                );
//
//        shooter.power(currentPower);
//    }
//
//    /**
//     * Refresh the velocity reading and return it.
//     */
//    private double getVelocity() {
//
//        updateShooter();
//
//        return shooter.velocity();
//    }
//
//    // ================================================================
//    // PHASE 1: RAMP TEST
//    // ================================================================
//
//    private List<double[]> runRampTest() {
//
//        List<double[]> samples =
//                new ArrayList<>();
//
//        double power = 0.0;
//
//        while (
//                opModeIsActive()
//                        && power <= RAMP_MAX_POWER + 1e-9
//        ) {
//
//            setPower(power);
//
//            double settledVelocity =
//                    waitForSettle();
//
//            if (settledVelocity >= 0) {
//
//                samples.add(
//                        new double[]{
//                                power,
//                                settledVelocity
//                        }
//                );
//
//                telemetry.addData(
//                        "Ramp power",
//                        "%.3f",
//                        power
//                );
//
//                telemetry.addData(
//                        "Settled velocity",
//                        "%.1f",
//                        settledVelocity
//                );
//
//                telemetry.update();
//            }
//
//            power += RAMP_POWER_STEP;
//        }
//
//        return samples;
//    }
//
//    // ================================================================
//    // WAIT FOR SETTLE
//    // ================================================================
//
//    private double waitForSettle() {
//
//        ElapsedTime holdTimer =
//                new ElapsedTime();
//
//        resetFilter();
//
//        double lastFiltered =
//                updateFilter(
//                        getVelocity()
//                );
//
//        int settledStreak = 0;
//
//        while (
//                opModeIsActive()
//                        && holdTimer.milliseconds()
//                        < RAMP_HOLD_MS
//        ) {
//
//            sleep(RAMP_SETTLE_CHECK_MS);
//
//            /*
//             * getVelocity() calls:
//             *
//             *     shooter.update()
//             *
//             * followed immediately by:
//             *
//             *     shooter.power(currentPower)
//             *
//             * Therefore the current ramp power is maintained.
//             */
//            double filtered =
//                    updateFilter(
//                            getVelocity()
//                    );
//
//            double slope =
//                    Math.abs(
//                            filtered - lastFiltered
//                    );
//
//            lastFiltered = filtered;
//
//            if (
//                    slope < RAMP_SETTLE_VEL_SLOPE
//            ) {
//
//                settledStreak++;
//
//                if (
//                        settledStreak
//                                >= RAMP_SETTLE_STREAK
//                ) {
//
//                    return filtered;
//                }
//
//            } else {
//
//                settledStreak = 0;
//            }
//        }
//
//        if (!opModeIsActive()) {
//            return -1;
//        }
//
//        return lastFiltered;
//    }
//
//    // ================================================================
//    // LINEAR REGRESSION
//    // ================================================================
//
//    private double[] linearRegression(
//            List<double[]> samples
//    ) {
//
//        List<double[]> movingSamples =
//                new ArrayList<>();
//
//        for (double[] sample : samples) {
//
//            if (
//                    sample[1]
//                            >= MIN_MOVING_VELOCITY
//            ) {
//
//                movingSamples.add(sample);
//            }
//        }
//
//        int n =
//                movingSamples.size();
//
//        if (n < 2) {
//
//            return new double[]{
//                    0.0,
//                    0.0
//            };
//        }
//
//        double sumX = 0.0;
//        double sumY = 0.0;
//        double sumXY = 0.0;
//        double sumXX = 0.0;
//
//        for (double[] sample : movingSamples) {
//
//            double velocity = sample[1];
//            double power = sample[0];
//
//            sumX += velocity;
//            sumY += power;
//            sumXY += velocity * power;
//            sumXX += velocity * velocity;
//        }
//
//        double denom =
//                (n * sumXX)
//                        - (sumX * sumX);
//
//        if (
//                Math.abs(denom)
//                        < 1e-9
//        ) {
//
//            return new double[]{
//                    0.0,
//                    sumY / n
//            };
//        }
//
//        double slope =
//                (n * sumXY
//                        - sumX * sumY)
//                        / denom;
//
//        double intercept =
//                (sumY
//                        - slope * sumX)
//                        / n;
//
//        return new double[]{
//                slope,
//                intercept
//        };
//    }
//
//    // ================================================================
//    // NOISE FLOOR
//    // ================================================================
//
//    private double estimateNoiseFloor() {
//
//        setPower(0);
//
//        resetFilter();
//
//        double minV =
//                Double.MAX_VALUE;
//
//        double maxV =
//                -Double.MAX_VALUE;
//
//        ElapsedTime timer =
//                new ElapsedTime();
//
//        while (
//                opModeIsActive()
//                        && timer.milliseconds()
//                        < 300
//        ) {
//
//            double filtered =
//                    updateFilter(
//                            getVelocity()
//                    );
//
//            minV =
//                    Math.min(
//                            minV,
//                            filtered
//                    );
//
//            maxV =
//                    Math.max(
//                            maxV,
//                            filtered
//                    );
//
//            sleep(20);
//        }
//
//        double jitterTicksPerSec =
//                Math.max(
//                        0.0,
//                        maxV - minV
//                );
//
//        return Math.max(
//                0.01,
//                jitterTicksPerSec * 0.0005
//        );
//    }
//
//    // ================================================================
//    // PHASE 2: kP SEARCH
//    // ================================================================
//
//    private double findCriticalKp(
//            double kV,
//            double kS,
//            double targetVelocity
//    ) {
//
//        double lastStable = 0.0;
//
//        double kp = KP_START;
//
//        boolean foundOscillation = false;
//
//        while (
//                opModeIsActive()
//                        && kp <= KP_MAX
//        ) {
//
//            boolean oscillates =
//                    runStepTest(
//                            kV,
//                            kS,
//                            kp,
//                            targetVelocity
//                    );
//
//            telemetry.addData(
//                    "Testing kP",
//                    "%.6f",
//                    kp
//            );
//
//            telemetry.addData(
//                    "Oscillated?",
//                    oscillates
//            );
//
//            telemetry.update();
//
//            if (oscillates) {
//
//                foundOscillation = true;
//                break;
//            }
//
//            lastStable = kp;
//
//            kp *= KP_SEARCH_MULTIPLIER;
//        }
//
//        if (!foundOscillation) {
//
//            return lastStable > 0
//                    ? lastStable
//                    : kp;
//        }
//
//        double low =
//                lastStable;
//
//        double high =
//                kp;
//
//        for (
//                int i = 0;
//                i < KP_BINARY_SEARCH_ITERS
//                        && opModeIsActive();
//                i++
//        ) {
//
//            double mid =
//                    (low + high) / 2.0;
//
//            boolean oscillates =
//                    runStepTest(
//                            kV,
//                            kS,
//                            mid,
//                            targetVelocity
//                    );
//
//            telemetry.addData(
//                    "Binary search kP",
//                    "%.6f",
//                    mid
//            );
//
//            telemetry.addData(
//                    "Oscillated?",
//                    oscillates
//            );
//
//            telemetry.update();
//
//            if (oscillates) {
//
//                high = mid;
//
//            } else {
//
//                low = mid;
//            }
//        }
//
//        return low;
//    }
//
//    // ================================================================
//    // STEP TEST
//    // ================================================================
//
//    private boolean runStepTest(
//            double kV,
//            double kS,
//            double kP,
//            double targetVelocity
//    ) {
//
//        // Stop the flywheel before the next test.
//        setPower(0);
//
//        sleep(STEP_SETTLE_WAIT_MS);
//
//        ElapsedTime stepTimer =
//                new ElapsedTime();
//
//        double maxVelocitySeen = 0.0;
//
//        double rawVelocity =
//                getVelocity();
//
//        double prevError =
//                targetVelocity
//                        - rawVelocity;
//
//        int signChanges = 0;
//
//        boolean reachedTargetOnce = false;
//
//        while (
//                opModeIsActive()
//                        && stepTimer.milliseconds()
//                        < STEP_TEST_DURATION_MS
//        ) {
//
//            // Refresh current velocity.
//            rawVelocity =
//                    getVelocity();
//
//            double error =
//                    targetVelocity
//                            - rawVelocity;
//
//            // Control equation.
//            double power =
//                    (kV * targetVelocity)
//                            + (kP * error)
//                            + kS;
//
//            power =
//                    Math.max(
//                            0.0,
//                            Math.min(
//                                    1.0,
//                                    power
//                            )
//                    );
//
//            // Apply calculated power to BOTH shooter motors.
//            setPower(power);
//
//            maxVelocitySeen =
//                    Math.max(
//                            maxVelocitySeen,
//                            rawVelocity
//                    );
//
//            if (
//                    rawVelocity
//                            >= targetVelocity * 0.95
//            ) {
//
//                reachedTargetOnce = true;
//            }
//
//            if (reachedTargetOnce) {
//
//                if (
//                        prevError > NOISE_DEADBAND
//                                && error < -NOISE_DEADBAND
//                ) {
//
//                    signChanges++;
//
//                    prevError = error;
//
//                } else if (
//                        prevError < -NOISE_DEADBAND
//                                && error > NOISE_DEADBAND
//                ) {
//
//                    signChanges++;
//
//                    prevError = error;
//                }
//
//            } else {
//
//                prevError = error;
//            }
//        }
//
//        double overshootFraction =
//                targetVelocity > 1e-6
//                        ? Math.max(
//                        0.0,
//                        (
//                                maxVelocitySeen
//                                - targetVelocity
//                        )
//                        / targetVelocity
//                )
//                        : 0.0;
//
//        return
//                signChanges
//                        >= OSCILLATION_CROSSINGS_THRESHOLD
//                        ||
//                        overshootFraction
//                                >= OVERSHOOT_FRACTION_THRESHOLD;
//    }
//
//    // ================================================================
//    // FILTER
//    // ================================================================
//
//    private void resetFilter() {
//
//        filterWindow.clear();
//
//        filterSum = 0.0;
//    }
//
//    private double updateFilter(
//            double value
//    ) {
//
//        filterWindow.addLast(value);
//
//        filterSum += value;
//
//        if (
//                filterWindow.size()
//                        > FILTER_WINDOW
//        ) {
//
//            filterSum -=
//                    filterWindow.removeFirst();
//        }
//
//        return
//                filterSum
//                        / filterWindow.size();
//    }
//}