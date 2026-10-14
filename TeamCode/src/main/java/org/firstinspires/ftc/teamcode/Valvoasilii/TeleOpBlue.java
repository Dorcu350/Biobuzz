package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Hood;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Intake;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Sensors;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.List;

@TeleOp(name = "TeleOp Blue")
public class TeleOpBlue extends LinearOpMode {
    public static double testRotation = 1.0;

    Follower follower;
    Turret turret;
    Shooter shooter;
    Hood hood;
    Intake intake;
    Sensors sensors;
    ElapsedTime timer;
    ElapsedTime loops = new ElapsedTime();

    @Override
    public void runOpMode() throws InterruptedException {
        sensors = new Sensors(hardwareMap);
        intake = new Intake(hardwareMap);
        turret = new Turret(hardwareMap);
        shooter = new Shooter(hardwareMap);
        hood = new Hood(hardwareMap);
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        follower = Constants.create(hardwareMap);
        sensors.updatePose();
        follower.setPose(new Pose(sensors.getX(), sensors.getY(), sensors.getHeading()));
        follower.update();

        Globals.alliance = Globals.Alliance.BLUE;
        Globals.faze = Globals.Faze.TeleOp;

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        timer.startTime();
        loops.reset();
        telemetry.setMsTransmissionInterval(11);
        telemetry.addLine("Opinio de valvoasilio si in parametris trahit");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            for (LynxModule hub : allHubs) hub.clearBulkCache();

            double forward = gamepad1.left_stick_y;
            double strafe = gamepad1.left_stick_x;

            DrivePowers powers = ManualDrive.fieldCentric(
                    -forward,
                    -strafe,
                    -gamepad1.right_stick_x * testRotation,
                    follower.pose().heading(),
                    Math.toRadians(0)
            );
            follower.manual(powers);

            intake.update();
            turret.update(0,0,0,0,0,0);
            hood.update();
            shooter.update(0,0,0,0,0);
            follower.update();

            Pose pose = follower.pose();
            telemetry.addData("x", pose.x());
            telemetry.addData("y", pose.y());
            telemetry.addData("heading", Math.toDegrees(pose.heading()));
            telemetry.update();
        }
    }
}