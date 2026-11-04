package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants; // calea către Constants-ul TĂU

@TeleOp(name = "TeleOp Blue", group = "Valvoasilii")
public class TeleOpBlue extends LinearOpMode {
    Follower follower;
    Pose startingPose = new Pose(0, 0, Math.toRadians(0));

    @Override
    public void runOpMode() throws InterruptedException {
        follower = Constants.create(hardwareMap);
        follower.setPose(startingPose);

        waitForStart();

        while (opModeIsActive()) {
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
            telemetry.update();
        }
    }
}