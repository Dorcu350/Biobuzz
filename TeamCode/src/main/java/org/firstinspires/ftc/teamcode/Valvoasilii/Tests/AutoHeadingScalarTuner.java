package org.firstinspires.ftc.teamcode.Valvoasilii.Tests;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.pedropathing.utils.Angle;
import com.pedropathing.utils.Timer;
import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Configurable
@Autonomous(name = "Auto Heading Scalar Tuner", group = "Tests")
public class AutoHeadingScalarTuner extends OpMode {

    /*
     * Înlocuiește aceste două valori cu coordonatele PODURILOR din Onshape, în mm:
     *
     * X_POD_OFFSET_MM: podul forward/X față de centru, lateral.
     *   + = la stânga robotului, - = la dreapta.
     *
     * Y_POD_OFFSET_MM: podul strafe/Y față de centru, înainte/înapoi.
     *   + = înainte, - = în spate.
     *
     * 0.0 este intenționat: OpMode-ul refuză să pornească până completezi cotele.
     */
    public static double X_POD_OFFSET_MM = 0.0;
    public static double Y_POD_OFFSET_MM = 0.0;

    // Rotație lentă; se oprește după ture numărate din encoder-ele podurilor.
    public static double TURN_POWER = 0.20;
    public static double TARGET_TURNS = 10.0;
    public static double MAX_RUNTIME_SECONDS = 90.0;

    private static final double MM_PER_INCH = 25.4;
    private static final double MIN_REFERENCE_OFFSET_MM = 10.0;
    private static final double MAX_POD_DISAGREEMENT = 0.20;
    private static final int MAX_BAD_FRAMES = 8;
    private static final int MIN_VALID_SAMPLES = 100;

    private enum State {
        READY,
        RUNNING,
        COMPLETE,
        FAILED
    }

    private final Timer timer = new Timer();

    private State state = State.READY;
    private Follower follower;
    private OctoQuad octo;

    private int xPort;
    private int yPort;
    private double referenceXOffsetMm;
    private double referenceYOffsetMm;
    private double millimetersPerTick;

    private int previousXPosition;
    private int previousYPosition;
    private double previousHeading;
    private boolean haveBaseline = false;

    private double totalXDistanceMm = 0.0;
    private double totalYDistanceMm = 0.0;
    private double totalRawImuHeading = 0.0;
    private int validSamples = 0;
    private int consecutiveBadFrames = 0;

    private double calculatedHeadingScalar = Double.NaN;
    private double xPodTurns = Double.NaN;
    private double yPodTurns = Double.NaN;
    private double podDisagreement = Double.NaN;
    private String errorMessage = "";

    private double oldXPodOffset;
    private double oldYPodOffset;
    private double oldHeadingScalar;
    private DistanceUnit oldOffsetUnits;
    private DistanceUnit oldGlobalDistanceUnit;
    private boolean oldConfigSaved = false;

    @Override
    public void init() {
        saveOriginalConfig();

        referenceXOffsetMm = X_POD_OFFSET_MM;
        referenceYOffsetMm = Y_POD_OFFSET_MM;

        try {
            configureForMeasurement();

            follower = Constants.create(hardwareMap);
            if (!(follower.localizer instanceof OctoQuadLocalizer)) {
                throw new IllegalStateException("Constants.create() nu a creat un OctoQuadLocalizer.");
            }

            OctoQuadLocalizer localizer = (OctoQuadLocalizer) follower.localizer;
            octo = localizer.octoQuad;

            xPort = Constants.localizerConfig.xPodPort.get();
            yPort = Constants.localizerConfig.yPodPort.get();

            double ticksPerUnit = Constants.localizerConfig.ticksPerUnit.get();
            DistanceUnit resolutionUnit = Constants.localizerConfig.encoderResolutionUnit.get();
            if (!Double.isFinite(ticksPerUnit) || ticksPerUnit <= 0.0) {
                throw new IllegalStateException("ticksPerUnit trebuie să fie pozitiv.");
            }
            millimetersPerTick = resolutionUnit.toMm(1.0) / ticksPerUnit;

            follower.setPose(new Pose(0, 0, 0));
            follower.update();
        } catch (RuntimeException e) {
            restoreOriginalConfig();
            throw e;
        }
    }

    private void saveOriginalConfig() {
        oldXPodOffset = Constants.localizerConfig.xPodOffset.get();
        oldYPodOffset = Constants.localizerConfig.yPodOffset.get();
        oldHeadingScalar = Constants.localizerConfig.headingScalar.get();
        oldOffsetUnits = Constants.localizerConfig.offsetUnits.get();
        oldGlobalDistanceUnit = Constants.localizerConfig.globalDistanceUnit.get();
        oldConfigSaved = true;
    }

    private void configureForMeasurement() {
        Constants.localizerConfig.offsetUnits.set(DistanceUnit.INCH);
        Constants.localizerConfig.globalDistanceUnit.set(DistanceUnit.INCH);

        // Convertim măsurătorile CAD din mm în unitatea offseturilor din Constants.
        Constants.localizerConfig.xPodOffset.set(referenceXOffsetMm / MM_PER_INCH);
        Constants.localizerConfig.yPodOffset.set(referenceYOffsetMm / MM_PER_INCH);

        // Scalarul este fixat la 1 ca să comparăm yaw-ul necorectat cu unghiul podurilor.
        Constants.localizerConfig.headingScalar.set(1.0);
    }

    @Override
    public void init_loop() {
        if (follower != null) {
            follower.update();
        }

        telemetry.addLine("Auto Heading Scalar Tuner");
        telemetry.addLine("Completează offseturile CAD în mm înainte de START.");
        telemetry.addData("X pod offset CAD", "%.1f mm", referenceXOffsetMm);
        telemetry.addData("Y pod offset CAD", "%.1f mm", referenceYOffsetMm);
        telemetry.addLine("Hub-ul rotit 45° în plan nu cere un factor cos(45°) pentru yaw.");
        telemetry.addLine("Apasă B în timpul testului pentru oprire de urgență.");
        telemetry.update();
    }

    @Override
    public void start() {
        String validationError = validateSettings();
        if (!validationError.isEmpty()) {
            fail(validationError);
            return;
        }

        OctoQuad.EncoderDataBlock initial = octo.readAllEncoderData();
        if (!initial.isDataValid()) {
            fail("OctoQuad a returnat un cadru invalid la start. Încearcă din nou.");
            return;
        }

        previousXPosition = initial.positions[xPort];
        previousYPosition = initial.positions[yPort];
        previousHeading = follower.pose().heading();
        haveBaseline = true;

        totalXDistanceMm = 0.0;
        totalYDistanceMm = 0.0;
        totalRawImuHeading = 0.0;
        validSamples = 0;
        consecutiveBadFrames = 0;

        timer.reset();
        state = State.RUNNING;
        follower.manual(0, 0, TURN_POWER);
    }

    private String validateSettings() {
        if (octo == null || follower == null) {
            return "OctoQuad/Follower nu a fost inițializat.";
        }
        if (Math.abs(referenceXOffsetMm) < MIN_REFERENCE_OFFSET_MM
                && Math.abs(referenceYOffsetMm) < MIN_REFERENCE_OFFSET_MM) {
            return "Pune cel puțin un offset CAD nenul (minimum 10 mm).";
        }
        if (!Double.isFinite(TURN_POWER) || TURN_POWER <= 0.0 || TURN_POWER > 1.0) {
            return "TURN_POWER trebuie să fie între 0 și 1.";
        }
        if (!Double.isFinite(TARGET_TURNS) || TARGET_TURNS < 1.0) {
            return "TARGET_TURNS trebuie să fie cel puțin 1.";
        }
        if (!Double.isFinite(MAX_RUNTIME_SECONDS) || MAX_RUNTIME_SECONDS <= 0.0) {
            return "MAX_RUNTIME_SECONDS trebuie să fie pozitiv.";
        }
        if (xPort == yPort || xPort < 0 || xPort >= 8 || yPort < 0 || yPort >= 8) {
            return "Porturile X/Y din Constants nu sunt valide.";
        }
        return "";
    }

    @Override
    public void loop() {
        if (follower == null || octo == null) {
            telemetry.addLine("OctoQuad/Follower nu a fost inițializat.");
            telemetry.update();
            return;
        }

        if (gamepad1.bWasPressed()) {
            follower.stop();
            requestOpModeStop();
            return;
        }

        if (state == State.RUNNING) {
            follower.manual(0, 0, TURN_POWER);
            follower.update();

            OctoQuad.EncoderDataBlock data = octo.readAllEncoderData();
            if (data.isDataValid()) {
                recordSample(data, follower.pose().heading());
                consecutiveBadFrames = 0;
            } else {
                consecutiveBadFrames++;
                if (consecutiveBadFrames > MAX_BAD_FRAMES) {
                    fail("Prea multe cadre OctoQuad invalide consecutiv.");
                }
            }

            if (state == State.RUNNING) {
                double measuredTurns = Math.abs(encoderHeadingRadians()) / (2.0 * Math.PI);

                if (measuredTurns >= TARGET_TURNS) {
                    finishCalibration();
                } else if (timer.seconds() >= MAX_RUNTIME_SECONDS) {
                    fail("Timeout înainte să ajungă la turele cerute. Verifică podurile și offseturile CAD.");
                }
            }
        }

        showTelemetry();
    }

    private void recordSample(OctoQuad.EncoderDataBlock data, double currentHeading) {
        if (!haveBaseline) {
            previousXPosition = data.positions[xPort];
            previousYPosition = data.positions[yPort];
            previousHeading = currentHeading;
            haveBaseline = true;
            return;
        }

        long deltaXTicks = (long) data.positions[xPort] - (long) previousXPosition;
        long deltaYTicks = (long) data.positions[yPort] - (long) previousYPosition;

        totalXDistanceMm += deltaXTicks * millimetersPerTick;
        totalYDistanceMm += deltaYTicks * millimetersPerTick;

        totalRawImuHeading += Angle.normalizeSigned(currentHeading - previousHeading);

        previousXPosition = data.positions[xPort];
        previousYPosition = data.positions[yPort];
        previousHeading = currentHeading;
        validSamples++;
    }

    /**
     * The forward X pod measures -xPodOffset * dHeading during an in-place turn.
     * The lateral Y pod measures yPodOffset * dHeading.
     * Both signed measurements are combined with least squares.
     */
    private double encoderHeadingRadians() {
        double numerator = 0.0;
        double denominator = 0.0;

        if (Math.abs(referenceXOffsetMm) >= MIN_REFERENCE_OFFSET_MM) {
            numerator += -referenceXOffsetMm * totalXDistanceMm;
            denominator += referenceXOffsetMm * referenceXOffsetMm;
        }

        if (Math.abs(referenceYOffsetMm) >= MIN_REFERENCE_OFFSET_MM) {
            numerator += referenceYOffsetMm * totalYDistanceMm;
            denominator += referenceYOffsetMm * referenceYOffsetMm;
        }

        if (denominator <= 0.0) {
            return 0.0;
        }

        return numerator / denominator;
    }

    private void finishCalibration() {
        follower.stop();

        if (validSamples < MIN_VALID_SAMPLES) {
            fail("Prea puține măsurători valide pentru o estimare stabilă.");
            return;
        }

        double encoderHeading = encoderHeadingRadians();
        if (!Double.isFinite(encoderHeading)
                || Math.abs(encoderHeading) < Math.PI * 2.0) {
            fail("Unghiul de referință calculat din poduri este prea mic.");
            return;
        }

        if (!Double.isFinite(totalRawImuHeading)
                || Math.abs(totalRawImuHeading) < 1.0) {
            fail("IMU-ul nu a raportat suficientă rotație.");
            return;
        }

        boolean useX = Math.abs(referenceXOffsetMm) >= MIN_REFERENCE_OFFSET_MM;
        boolean useY = Math.abs(referenceYOffsetMm) >= MIN_REFERENCE_OFFSET_MM;

        if (useX && useY) {
            xPodTurns = -totalXDistanceMm / referenceXOffsetMm;
            yPodTurns = totalYDistanceMm / referenceYOffsetMm;

            double averageMagnitude =
                    (Math.abs(xPodTurns) + Math.abs(yPodTurns)) / 2.0;
            podDisagreement = Math.abs(
                    Math.abs(xPodTurns) - Math.abs(yPodTurns)
            ) / Math.max(averageMagnitude, 1e-9);

            if (xPodTurns * yPodTurns <= 0.0) {
                fail("Podurile indică sensuri diferite. Verifică offseturile și direcțiile encoderelor.");
                return;
            }

            if (podDisagreement > MAX_POD_DISAGREEMENT) {
                fail("Estimările podurilor diferă cu peste 20%. Verifică valorile CAD, direcțiile și aderența.");
                return;
            }
        }

        calculatedHeadingScalar = Math.abs(encoderHeading / totalRawImuHeading);
        if (!Double.isFinite(calculatedHeadingScalar)
                || calculatedHeadingScalar < 0.1
                || calculatedHeadingScalar > 10.0) {
            fail("headingScalar calculat este neverosimil; nu îl copia.");
            return;
        }

        state = State.COMPLETE;
    }

    private void fail(String message) {
        if (follower != null) {
            follower.stop();
        }
        state = State.FAILED;
        errorMessage = message;
    }

    private void showTelemetry() {
        double encoderTurns = Math.abs(encoderHeadingRadians()) / (2.0 * Math.PI);
        double imuTurns = Math.abs(totalRawImuHeading) / (2.0 * Math.PI);

        telemetry.addLine("Auto Heading Scalar Tuner");
        telemetry.addData("CAD X pod", "%.1f mm", referenceXOffsetMm);
        telemetry.addData("CAD Y pod", "%.1f mm", referenceYOffsetMm);

        if (state == State.RUNNING) {
            telemetry.addData("Ture din encoder-ele podurilor", "%.2f / %.2f", encoderTurns, TARGET_TURNS);
            telemetry.addData("Ture IMU necorectate", "%.2f", imuTurns);
            telemetry.addData("Samples valide", validSamples);
            telemetry.addData("Timp", "%.1f / %.1f s", timer.seconds(), MAX_RUNTIME_SECONDS);
            telemetry.addLine("Apasă B pentru oprire de urgență.");
        } else if (state == State.COMPLETE) {
            telemetry.addLine("Calibrare terminată. Valoarea este în memorie; copiaz-o în Constants.");
            telemetry.addData("headingScalar", "%.12f", calculatedHeadingScalar);
            telemetry.addData("Linie de copiat", "c.headingScalar.set(%.12f);", calculatedHeadingScalar);
            telemetry.addData("Unghi real din poduri", "%.4f rad", encoderHeadingRadians());
            telemetry.addData("Heading brut IMU", "%.4f rad", totalRawImuHeading);
            if (Double.isFinite(podDisagreement)) {
                telemetry.addData("Diferență între poduri", "%.1f%%", podDisagreement * 100.0);
            }
        } else if (state == State.FAILED) {
            telemetry.addLine("Calibrarea a fost oprită.");
            telemetry.addLine(errorMessage);
        } else {
            telemetry.addLine("Așteaptă START.");
        }

        telemetry.update();
    }

    @Override
    public void stop() {
        if (follower != null) {
            follower.stop();
        }
        restoreOriginalConfig();
    }

    private void restoreOriginalConfig() {
        if (!oldConfigSaved) {
            return;
        }

        Constants.localizerConfig.xPodOffset.set(oldXPodOffset);
        Constants.localizerConfig.yPodOffset.set(oldYPodOffset);
        Constants.localizerConfig.headingScalar.set(oldHeadingScalar);
        Constants.localizerConfig.offsetUnits.set(oldOffsetUnits);
        Constants.localizerConfig.globalDistanceUnit.set(oldGlobalDistanceUnit);
        oldConfigSaved = false;
    }
}
