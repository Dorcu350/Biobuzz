package org.firstinspires.ftc.teamcode.Valvoasilii.Tests;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector2D;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.ArrayList;
import java.util.List;

@Configurable
@Autonomous(name = "Auto Offsets Tuner", group = "Tests")
public class AutoOffsetsTuner extends OpMode {

    public static double POWER = 0.5;
    public static double RUNTIME = 5.0;

    private final Timer timer = new Timer();

    private Follower follower;

    private boolean done = false;
    private final List<Vector2D> poses = new ArrayList<>();

    private Vector2D offsets;

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

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);

        follower.setPose(new Pose(0, 0, 0));
        follower.update();
    }

    @Override
    public void start() {
        if (follower == null) return;

        timer.reset();
        follower.manual(0, 0, POWER);
    }

    @Override
    public void init_loop() {
        telemetry.addLine(
                "This will turn continuously in place for " + RUNTIME + " seconds."
        );
        telemetry.addLine("Make sure you have enough room.");
        telemetry.update();

        if (follower != null) {
            follower.update();
        }
    }

    @Override
    public void loop() {
        if (follower == null) {
            telemetry.addLine("Follower is not initialized!");
            telemetry.update();
            return;
        }

        follower.update();

        if (gamepad1.bWasPressed()) {
            follower.stop();
            requestOpModeStop();
            return;
        }

        if (!done) {
            // Record the robot's current position.
            poses.add(follower.pose().toVector2D());

            if (timer.seconds() >= RUNTIME) {
                done = true;

                follower.stop();

                try {
                    offsets = fitCircle(
                            poses.toArray(new Vector2D[0])
                    );
                } catch (IllegalArgumentException e) {
                    telemetry.addData("Circle fitting error", e.getMessage());
                    telemetry.update();
                    return;
                }

            } else {
                // Keep rotating.
                follower.manual(0, 0, POWER);

                telemetry.addData("Elapsed", "%.2f", timer.seconds());
                telemetry.addData("Samples", poses.size());
                telemetry.update();
                return;
            }
        }

        telemetry.addLine("Circle fitting complete!");
        telemetry.addLine(
                "Apply these values to your localizer offsets."
        );

        telemetry.addData("strafeX", "%.5f", offsets.x());
        telemetry.addData("forwardY", "%.5f", offsets.y());

        telemetry.addData("Elapsed time", "%.4f", timer.seconds());
        telemetry.addData("Samples", poses.size());

        telemetry.update();
    }

    /**
     * Fits a circle to the recorded robot positions.
     * Returns the negative circle center as the localizer offset.
     */
    private Vector2D fitCircle(Vector2D[] points) {
        if (points == null || points.length < 3) {
            throw new IllegalArgumentException(
                    "At least 3 points are required."
            );
        }

        Circle circle = algebraicFit(points);
        circle = gaussNewton(points, circle);

        return Vector2D.cartesian(
                -circle.x,
                -circle.y
        );
    }

    /**
     * Algebraic least-squares circle fitting.
     *
     * Circle equation:
     * x² + y² + Dx + Ey + F = 0
     */
    private static Circle algebraicFit(Vector2D[] points) {
        int n = points.length;

        double[][] A = new double[3][3];
        double[] B = new double[3];

        double meanX = 0;
        double meanY = 0;

        for (Vector2D p : points) {
            meanX += p.x();
            meanY += p.y();
        }

        meanX /= n;
        meanY /= n;

        for (Vector2D p : points) {
            double x = p.x() - meanX;
            double y = p.y() - meanY;
            double z = -(x * x + y * y);

            double[] row = {x, y, 1};

            for (int i = 0; i < 3; i++) {
                B[i] += row[i] * z;

                for (int j = 0; j < 3; j++) {
                    A[i][j] += row[i] * row[j];
                }
            }
        }

        double[] solution = solve3x3(A, B);

        double cx = meanX - solution[0] / 2.0;
        double cy = meanY - solution[1] / 2.0;

        double radiusSquared =
                (solution[0] * solution[0]
                        + solution[1] * solution[1]) / 4.0
                        - solution[2];

        if (radiusSquared <= 0 || !Double.isFinite(radiusSquared)) {
            throw new IllegalArgumentException(
                    "Invalid circle radius. Check the recorded positions."
            );
        }

        return new Circle(
                cx,
                cy,
                Math.sqrt(radiusSquared)
        );
    }

    /**
     * Solves a 3x3 linear system using Gaussian elimination
     * with partial pivoting.
     */
    private static double[] solve3x3(double[][] matrix, double[] values) {
        double[][] a = new double[3][4];

        for (int i = 0; i < 3; i++) {
            System.arraycopy(matrix[i], 0, a[i], 0, 3);
            a[i][3] = values[i];
        }

        for (int col = 0; col < 3; col++) {
            int pivot = col;

            for (int row = col + 1; row < 3; row++) {
                if (Math.abs(a[row][col]) >
                        Math.abs(a[pivot][col])) {
                    pivot = row;
                }
            }

            if (Math.abs(a[pivot][col]) < 1e-12) {
                throw new IllegalArgumentException(
                        "Recorded points do not define a stable circle."
                );
            }

            double[] temp = a[col];
            a[col] = a[pivot];
            a[pivot] = temp;

            double divisor = a[col][col];

            for (int j = col; j < 4; j++) {
                a[col][j] /= divisor;
            }

            for (int row = 0; row < 3; row++) {
                if (row == col) continue;

                double factor = a[row][col];

                for (int j = col; j < 4; j++) {
                    a[row][j] -= factor * a[col][j];
                }
            }
        }

        return new double[]{
                a[0][3],
                a[1][3],
                a[2][3]
        };
    }

    /**
     * Refines the circle using Gauss-Newton optimization.
     */
    private static Circle gaussNewton(
            Vector2D[] points,
            Circle initial
    ) {
        double a = initial.x;
        double b = initial.y;
        double r = initial.radius;

        int n = points.length;

        for (int iter = 0; iter < 100; iter++) {
            double[][] JTJ = new double[3][3];
            double[] JTf = new double[3];

            for (Vector2D p : points) {
                double dx = p.x() - a;
                double dy = p.y() - b;

                double dist = Math.hypot(dx, dy);

                if (dist < 1e-10) continue;

                double residual = dist - r;

                double[] j = {
                        -dx / dist,
                        -dy / dist,
                        -1.0
                };

                for (int i = 0; i < 3; i++) {
                    JTf[i] += j[i] * residual;

                    for (int k = 0; k < 3; k++) {
                        JTJ[i][k] += j[i] * j[k];
                    }
                }
            }

            double gradientNorm = Math.sqrt(
                    JTf[0] * JTf[0]
                            + JTf[1] * JTf[1]
                            + JTf[2] * JTf[2]
            );

            if (gradientNorm < 1e-10) break;

            double[] delta;

            try {
                delta = solve3x3(JTJ, JTf);
            } catch (IllegalArgumentException e) {
                break;
            }

            a -= delta[0];
            b -= delta[1];
            r -= delta[2];

            if (r <= 0 || !Double.isFinite(r)
                    || !Double.isFinite(a)
                    || !Double.isFinite(b)) {
                throw new IllegalArgumentException(
                        "Circle refinement became unstable."
                );
            }

            double change = Math.sqrt(
                    delta[0] * delta[0]
                            + delta[1] * delta[1]
                            + delta[2] * delta[2]
            );

            if (change < 1e-9) break;
        }

        return new Circle(a, b, r);
    }
}