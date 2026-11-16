package org.firstinspires.ftc.teamcode.Valvoasilii.Tests;

import com.bylazar.field.FieldManager;
import com.bylazar.field.FieldPresets;
import com.bylazar.field.PanelsField;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Locale;

@TeleOp(name = "Pedro Localizer Field", group = "Tests")
public class PedroLocalizerFieldTeleOp extends OpMode {

    // Poziția inițială presupusă pe teren. Schimbă valorile dacă robotul pornește
    // din alt loc. Valorile sunt în inch, în coordonatele Pedro.
    public static double START_X_IN = 72.0;
    public static double START_Y_IN = 72.0;
    public static double START_HEADING_DEGREES = 0.0;

    // Robotul se rotește doar cât timp ții apăsat A.
    // Semnul pozitiv este rotație CCW în convenția Pedro.
    public static double AUTO_TURN_POWER = 0.15;

    // Dimensiunile aproximative ale conturului desenat în Panels.
    public static double ROBOT_LENGTH_IN = 18.0;
    public static double ROBOT_WIDTH_IN = 18.0;

    private static final int MAX_TRAIL_POINTS = 500;
    private static final double MIN_TRAIL_STEP_IN = 0.12;

    private final Deque<FieldPoint> trail = new ArrayDeque<>();

    private Follower follower;
    private FieldManager field;
    private TelemetryManager panelsTelemetry;
    private Pose startPose;

    private static final class FieldPoint {
        final double x;
        final double y;

        FieldPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    @Override
    public void init() {
        follower = Constants.create(hardwareMap);
        field = PanelsField.INSTANCE.getField();
        panelsTelemetry = PanelsTelemetry.INSTANCE.getTelemetry();

        // Acest preset aliniază coordonatele desenului cu cele folosite de Pedro.
        field.setOffsets(FieldPresets.INSTANCE.getPEDRO_PATHING());

        startPose = new Pose(
                START_X_IN,
                START_Y_IN,
                Math.toRadians(START_HEADING_DEGREES)
        );
        follower.setPose(startPose);
        follower.update();

        panelsTelemetry.addLine("Pedro Localizer Field gata.");
        panelsTelemetry.addLine("Robotul trebuie pus la coordonatele START_X_IN / START_Y_IN.");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void start() {
        trail.clear();
        startPose = new Pose(
                START_X_IN,
                START_Y_IN,
                Math.toRadians(START_HEADING_DEGREES)
        );
        follower.setPose(startPose);
        follower.update();
    }

    @Override
    public void loop() {
        boolean autoSpin = gamepad1.a;

        if (autoSpin) {
            // În modul de rotație ignorăm comenzile de translație ale stickurilor.
            follower.manual(0.0, 0.0, AUTO_TURN_POWER);
        } else {
            DrivePowers powers = ManualDrive.fieldCentric(
                    gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );
            follower.manual(powers);
        }

        follower.update();

        Pose pose = follower.pose();
        updatePanelsField(pose);
        updateTelemetry(pose, autoSpin);
    }

    private void updatePanelsField(Pose pose) {
        if (!field.getShouldUpdateCanvas()) {
            return;
        }

        addTrailPoint(pose.x(), pose.y());

        Iterator<FieldPoint> points = trail.iterator();
        if (trail.size() >= 2) {
            FieldPoint previous = points.next();
            field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), "green", 0.45);

            while (points.hasNext()) {
                FieldPoint current = points.next();
                field.moveCursor(previous.x, previous.y);
                field.line(current.x, current.y);
                previous = current;
            }
        }

        drawStartMarker();
        drawRobot(pose);
        field.update();
    }

    private void addTrailPoint(double x, double y) {
        FieldPoint last = trail.peekLast();
        if (last == null
                || Math.hypot(x - last.x, y - last.y) >= MIN_TRAIL_STEP_IN) {
            trail.addLast(new FieldPoint(x, y));
            while (trail.size() > MAX_TRAIL_POINTS) {
                trail.removeFirst();
            }
        }
    }

    private void drawStartMarker() {
        field.setStyle("yellow", "yellow", 0.35);
        field.moveCursor(startPose.x(), startPose.y());
        field.circle(2.0);
    }

    private void drawRobot(Pose pose) {
        double heading = pose.heading();
        double halfLength = ROBOT_LENGTH_IN / 2.0;
        double halfWidth = ROBOT_WIDTH_IN / 2.0;

        // Conturul pătratului este rotit după heading-ul curent din Pedro.
        double[][] localCorners = {
                { halfLength, halfWidth },
                { halfLength, -halfWidth },
                { -halfLength, -halfWidth },
                { -halfLength, halfWidth }
        };

        double[] x = new double[4];
        double[] y = new double[4];
        for (int i = 0; i < localCorners.length; i++) {
            double forward = localCorners[i][0];
            double lateral = localCorners[i][1];
            x[i] = pose.x()
                    + forward * Math.cos(heading)
                    - lateral * Math.sin(heading);
            y[i] = pose.y()
                    + forward * Math.sin(heading)
                    + lateral * Math.cos(heading);
        }

        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), "blue", 0.7);
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            field.moveCursor(x[i], y[i]);
            field.line(x[next], y[next]);
        }

        // Linia roșie indică partea din față a robotului.
        field.setStyle(PanelsField.INSTANCE.getTRANSPARENT(), "red", 1.0);
        field.moveCursor(pose.x(), pose.y());
        field.line(
                pose.x() + halfLength * Math.cos(heading),
                pose.y() + halfLength * Math.sin(heading)
        );

        field.setStyle("blue", "blue", 0.35);
        field.moveCursor(pose.x(), pose.y());
        field.circle(1.5);
    }

    private void updateTelemetry(Pose pose, boolean autoSpin) {
        double driftFromStart = Math.hypot(
                pose.x() - startPose.x(),
                pose.y() - startPose.y()
        );

        panelsTelemetry.addData("Mode", autoSpin ? "AUTO SPIN (hold A)" : "MANUAL");
        panelsTelemetry.addData("X (in)", String.format(Locale.US, "%.2f", pose.x()));
        panelsTelemetry.addData("Y (in)", String.format(Locale.US, "%.2f", pose.y()));
        panelsTelemetry.addData(
                "Heading (deg)",
                String.format(Locale.US, "%.1f", Math.toDegrees(pose.heading()))
        );
        panelsTelemetry.addData(
                "Drift from start (in)",
                String.format(Locale.US, "%.2f", driftFromStart)
        );
        panelsTelemetry.addLine("Hold A to spin in place; release A to stop spinning.");
        panelsTelemetry.update(telemetry);
    }

    @Override
    public void stop() {
        if (follower != null) {
            follower.stop();
        }
    }
}
