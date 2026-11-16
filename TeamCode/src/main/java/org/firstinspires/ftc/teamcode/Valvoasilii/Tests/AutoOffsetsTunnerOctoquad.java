package org.firstinspires.ftc.teamcode.Valvoasilii.Tests;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.ArrayList;
import java.util.List;

@Configurable
@Autonomous(name = "Auto Offsets Tuner Octoquad", group = "Tests")
public class AutoOffsetsTunnerOctoquad extends OpMode {

    // Putere de rotație. Valoarea pozitivă comandă rotația în sens CCW în Pedro.
    public static double TURN_POWER = 0.22;

    // Rotațiile sunt numărate folosind heading-ul curent din Pedro.
    public static double TARGET_TURNS = 4.0;

    // Oprește calibrarea dacă nu ajunge la numărul de rotații în timpul acesta.
    public static double MAX_RUNTIME_SECONDS = 30.0;

    private static final int MIN_SAMPLES = 30;
    private static final double MIN_RADIUS_INCHES = 0.05;

    private final Timer timer = new Timer();
    private final List<Vector2D> poses = new ArrayList<>();

    private Follower follower;

    private enum State {
        READY,
        RUNNING,
        COMPLETE,
        FAILED
    }

    private State state = State.READY;

    private double originalXPodOffset;
    private double originalYPodOffset;
    private boolean savedOriginalOffsets = false;

    private double lastHeading;
    private boolean hasLastHeading = false;
    private double accumulatedHeading = 0.0;

    private OffsetResult result;
    private String errorMessage = "";

    private static class Circle {
        final double x;
        final double y;
        final double radius;

        Circle(double x, double y, double radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }
    }

    private static class OffsetResult {
        final double xPodOffset;
        final double yPodOffset;
        final double radius;
        final double rmsError;

        OffsetResult(
                double xPodOffset,
                double yPodOffset,
                double radius,
                double rmsError
        ) {
            this.xPodOffset = xPodOffset;
            this.yPodOffset = yPodOffset;
            this.radius = radius;
            this.rmsError = rmsError;
        }
    }

    @Override
    public void init() {
        // Păstrăm setările actuale ca să le putem restaura la oprirea OpMode-ului.
        originalXPodOffset = Constants.localizerConfig.xPodOffset.get();
        originalYPodOffset = Constants.localizerConfig.yPodOffset.get();
        savedOriginalOffsets = true;

        /*
         * Calibrarea pornește cu offseturi zero ca să poată calcula valorile finale,
         * nu doar o corecție față de valorile deja existente.
         */
        Constants.localizerConfig.xPodOffset.set(0.0);
        Constants.localizerConfig.yPodOffset.set(0.0);

        try {
            follower = Constants.create(hardwareMap);
            follower.setPose(new Pose(0, 0, 0));
            follower.update();
        } catch (RuntimeException e) {
            restoreOriginalOffsets();
            throw e;
        }
    }

    @Override
    public void init_loop() {
        if (follower != null) {
            follower.update();
        }

        telemetry.addLine("Auto Offset Tuner");
        telemetry.addLine("Apasă START pentru rotația automată.");
        telemetry.addLine("Offsets temporare = 0; valorile inițiale vor fi restaurate la STOP.");
        telemetry.addLine("Calibrează headingScalar înainte de test.");
        telemetry.addLine("Apasă B pentru oprire de urgență.");
        telemetry.update();
    }

    @Override
    public void start() {
        if (follower == null) {
            fail("Follower-ul nu a fost inițializat.");
            return;
        }

        if (!Double.isFinite(TURN_POWER)
                || TURN_POWER <= 0.0
                || TURN_POWER > 1.0) {
            fail("TURN_POWER trebuie să fie între 0 și 1.");
            return;
        }

        if (!Double.isFinite(TARGET_TURNS) || TARGET_TURNS < 1.0) {
            fail("TARGET_TURNS trebuie să fie cel puțin 1.");
            return;
        }

        if (!Double.isFinite(MAX_RUNTIME_SECONDS)
                || MAX_RUNTIME_SECONDS <= 0.0) {
            fail("MAX_RUNTIME_SECONDS trebuie să fie pozitiv.");
            return;
        }

        poses.clear();
        accumulatedHeading = 0.0;

        lastHeading = follower.pose().heading();
        hasLastHeading = true;

        timer.reset();
        state = State.RUNNING;

        follower.manual(0, 0, TURN_POWER);
    }

    @Override
    public void loop() {
        if (follower == null) {
            telemetry.addLine("Follower-ul nu a fost inițializat.");
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

            Pose pose = follower.pose();
            poses.add(pose.toVector2D());
            updateAccumulatedHeading(pose.heading());

            double measuredTurns =
                    Math.abs(accumulatedHeading) / (2.0 * Math.PI);

            if (measuredTurns >= TARGET_TURNS) {
                finishCalibration();
            } else if (timer.seconds() >= MAX_RUNTIME_SECONDS) {
                fail(
                        "Timeout înainte să ajungă la numărul de rotații. "
                                + "Rotații estimate: "
                                + String.format("%.2f", measuredTurns)
                );
            }
        }

        showTelemetry();
    }

    private void updateAccumulatedHeading(double currentHeading) {
        if (!hasLastHeading) {
            lastHeading = currentHeading;
            hasLastHeading = true;
            return;
        }

        double delta = wrapRadians(currentHeading - lastHeading);
        accumulatedHeading += delta;
        lastHeading = currentHeading;
    }

    private void finishCalibration() {
        follower.stop();
        state = State.COMPLETE;

        try {
            if (poses.size() < MIN_SAMPLES) {
                throw new IllegalArgumentException(
                        "Prea puține măsurători: " + poses.size()
                );
            }

            Vector2D[] points = poses.toArray(new Vector2D[0]);

            Circle circle = algebraicFit(points);
            circle = gaussNewton(points, circle);

            if (!Double.isFinite(circle.x)
                    || !Double.isFinite(circle.y)
                    || !Double.isFinite(circle.radius)
                    || circle.radius < MIN_RADIUS_INCHES) {
                throw new IllegalArgumentException(
                        "Cercul estimat nu este valid."
                );
            }

            /*
             * În Pedro, podul X este podul înainte, iar podul Y este podul
             * de strafing. Codul inițial numește coordonatele rezultate
             * forwardY și strafeX:
             *
             *   xPodOffset = forwardY = -circle.y
             *   yPodOffset = strafeX  = -circle.x
             */
            double forwardY = -circle.y;
            double strafeX = -circle.x;

            double rmsError = calculateRmsError(points, circle);

            result = new OffsetResult(
                    forwardY,
                    strafeX,
                    circle.radius,
                    rmsError
            );

        } catch (RuntimeException e) {
            state = State.FAILED;
            errorMessage = "Fit-ul cercului a eșuat: " + e.getMessage();
        }
    }

    private void fail(String message) {
        if (follower != null) {
            follower.stop();
        }

        state = State.FAILED;
        errorMessage = message;
    }

    private void showTelemetry() {
        telemetry.addLine("Auto Offset Tuner");

        if (state == State.RUNNING) {
            double measuredTurns =
                    Math.abs(accumulatedHeading) / (2.0 * Math.PI);

            telemetry.addData(
                    "Rotatii estimate",
                    "%.2f / %.2f",
                    measuredTurns,
                    TARGET_TURNS
            );
            telemetry.addData("Masuratori", poses.size());
            telemetry.addData("Timp", "%.1f s", timer.seconds());
            telemetry.addLine("Apasă B pentru oprire.");
        } else if (state == State.COMPLETE && result != null) {
            telemetry.addLine("Calibrarea offseturilor s-a terminat.");
            telemetry.addLine("Valorile sunt în inch.");

            telemetry.addData(
                    "xPodOffset (forwardY)",
                    "%.5f",
                    result.xPodOffset
            );
            telemetry.addData(
                    "yPodOffset (strafeX)",
                    "%.5f",
                    result.yPodOffset
            );

            telemetry.addData("Raza fit-ului", "%.3f in", result.radius);
            telemetry.addData("Eroare RMS", "%.3f in", result.rmsError);
            telemetry.addData("Masuratori", poses.size());

            telemetry.addLine(
                    "Copiază valorile în Constants.localizerConfig."
            );
        } else if (state == State.FAILED) {
            telemetry.addLine("Calibrarea a eșuat.");
            telemetry.addLine(errorMessage);
        } else {
            telemetry.addLine("Așteaptă START.");
        }

        telemetry.update();
    }

    private static double calculateRmsError(
            Vector2D[] points,
            Circle circle
    ) {
        double sumSquaredError = 0.0;

        for (Vector2D point : points) {
            double distance = Math.hypot(
                    point.x() - circle.x,
                    point.y() - circle.y
            );

            double error = distance - circle.radius;
            sumSquaredError += error * error;
        }

        return Math.sqrt(sumSquaredError / points.length);
    }

    private static Circle algebraicFit(Vector2D[] points) {
        int n = points.length;

        if (n < 3) {
            throw new IllegalArgumentException(
                    "Sunt necesare cel puțin 3 puncte."
            );
        }

        double[][] a = new double[3][3];
        double[] b = new double[3];

        double meanX = 0.0;
        double meanY = 0.0;

        for (Vector2D point : points) {
            meanX += point.x();
            meanY += point.y();
        }

        meanX /= n;
        meanY /= n;

        for (Vector2D point : points) {
            double x = point.x() - meanX;
            double y = point.y() - meanY;
            double z = -(x * x + y * y);

            double[] row = {x, y, 1.0};

            for (int i = 0; i < 3; i++) {
                b[i] += row[i] * z;

                for (int j = 0; j < 3; j++) {
                    a[i][j] += row[i] * row[j];
                }
            }
        }

        double[] solution = solve3x3(a, b);

        double centerX = meanX - solution[0] / 2.0;
        double centerY = meanY - solution[1] / 2.0;

        double radiusSquared =
                (solution[0] * solution[0]
                        + solution[1] * solution[1]) / 4.0
                        - solution[2];

        if (radiusSquared <= 0.0 || !Double.isFinite(radiusSquared)) {
            throw new IllegalArgumentException(
                    "Raza fit-ului nu este validă."
            );
        }

        return new Circle(
                centerX,
                centerY,
                Math.sqrt(radiusSquared)
        );
    }

    private static Circle gaussNewton(
            Vector2D[] points,
            Circle initial
    ) {
        double centerX = initial.x;
        double centerY = initial.y;
        double radius = initial.radius;

        for (int iteration = 0; iteration < 100; iteration++) {
            double[][] jtj = new double[3][3];
            double[] jtf = new double[3];

            for (Vector2D point : points) {
                double dx = point.x() - centerX;
                double dy = point.y() - centerY;
                double distance = Math.hypot(dx, dy);

                if (distance < 1e-10) {
                    continue;
                }

                double residual = distance - radius;

                double[] j = {
                        -dx / distance,
                        -dy / distance,
                        -1.0
                };

                for (int i = 0; i < 3; i++) {
                    jtf[i] += j[i] * residual;

                    for (int k = 0; k < 3; k++) {
                        jtj[i][k] += j[i] * j[k];
                    }
                }
            }

            double gradientNorm = Math.sqrt(
                    jtf[0] * jtf[0]
                            + jtf[1] * jtf[1]
                            + jtf[2] * jtf[2]
            );

            if (!Double.isFinite(gradientNorm)) {
                throw new IllegalArgumentException(
                        "Optimizarea fit-ului a devenit instabilă."
                );
            }

            if (gradientNorm < 1e-10) {
                break;
            }

            double[] delta = solve3x3(jtj, jtf);

            centerX -= delta[0];
            centerY -= delta[1];
            radius -= delta[2];

            if (radius <= 0.0
                    || !Double.isFinite(radius)
                    || !Double.isFinite(centerX)
                    || !Double.isFinite(centerY)) {
                throw new IllegalArgumentException(
                        "Optimizarea fit-ului a devenit instabilă."
                );
            }

            double change = Math.sqrt(
                    delta[0] * delta[0]
                            + delta[1] * delta[1]
                            + delta[2] * delta[2]
            );

            if (change < 1e-9) {
                break;
            }
        }

        return new Circle(centerX, centerY, radius);
    }

    private static double[] solve3x3(
            double[][] matrix,
            double[] values
    ) {
        double[][] a = new double[3][4];

        for (int i = 0; i < 3; i++) {
            System.arraycopy(matrix[i], 0, a[i], 0, 3);
            a[i][3] = values[i];
        }

        for (int column = 0; column < 3; column++) {
            int pivot = column;

            for (int row = column + 1; row < 3; row++) {
                if (Math.abs(a[row][column])
                        > Math.abs(a[pivot][column])) {
                    pivot = row;
                }
            }

            if (Math.abs(a[pivot][column]) < 1e-12) {
                throw new IllegalArgumentException(
                        "Punctele nu definesc un cerc stabil."
                );
            }

            double[] temp = a[column];
            a[column] = a[pivot];
            a[pivot] = temp;

            double divisor = a[column][column];

            for (int j = column; j < 4; j++) {
                a[column][j] /= divisor;
            }

            for (int row = 0; row < 3; row++) {
                if (row == column) {
                    continue;
                }

                double factor = a[row][column];

                for (int j = column; j < 4; j++) {
                    a[row][j] -= factor * a[column][j];
                }
            }
        }

        return new double[]{
                a[0][3],
                a[1][3],
                a[2][3]
        };
    }

    private static double wrapRadians(double angle) {
        while (angle <= -Math.PI) {
            angle += 2.0 * Math.PI;
        }

        while (angle > Math.PI) {
            angle -= 2.0 * Math.PI;
        }

        return angle;
    }

    private void restoreOriginalOffsets() {
        if (!savedOriginalOffsets) {
            return;
        }

        Constants.localizerConfig.xPodOffset.set(originalXPodOffset);
        Constants.localizerConfig.yPodOffset.set(originalYPodOffset);
        savedOriginalOffsets = false;
    }

    @Override
    public void stop() {
        if (follower != null) {
            follower.stop();
        }

        restoreOriginalOffsets();
    }
}