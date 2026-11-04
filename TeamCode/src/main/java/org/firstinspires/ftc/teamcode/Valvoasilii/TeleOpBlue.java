package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
<<<<<<< HEAD

import org.firstinspires.ftc.teamcode.pedroPathing.Constants; // calea către Constants-ul TĂU

@TeleOp(name = "TeleOp Blue", group = "Valvoasilii")
public class TeleOpBlue extends LinearOpMode {
=======
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

>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
    Follower follower;
    Pose startingPose = new Pose(0, 0, Math.toRadians(0));

    @Override
    public void runOpMode() throws InterruptedException {
<<<<<<< HEAD
        follower = Constants.create(hardwareMap);
        follower.setPose(startingPose);
=======
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
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1

        waitForStart();

        while (opModeIsActive()) {
<<<<<<< HEAD
            DrivePowers powers = ManualDrive.fieldCentric(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    0
            );

            follower.manual(powers);

            if (gamepad1.options) {
                follower.setPose(new Pose(follower.pose().x(), follower.pose().y(), 0));
            }

            follower.update();

//            telemetry.addData("x", follower.pose().x());
//            telemetry.addData("y", follower.pose().y());
//            telemetry.addData("heading", Math.toDegrees(follower.pose().heading()));
=======
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
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
            telemetry.update();
        }
    }
}