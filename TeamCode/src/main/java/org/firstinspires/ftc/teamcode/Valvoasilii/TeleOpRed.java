package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.pedropathing.follower.Follower;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Intake;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Valvoasilii;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Sensors;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.List;

@TeleOp(name = "TeleOp Red")
public class TeleOpRed extends LinearOpMode {
    Shooter shooter;
    Follower follower;
    Turret turret;
    Intake intake;
    Sensors sensors;
    ElapsedTime timer = new ElapsedTime();
    ElapsedTime loops = new ElapsedTime();
    Pose startingPose = new Pose(7.25, 6.12, Math.toRadians(0));
//    Pose[] resetPose = {
//            new Pose(7.25, 6.12, Math.toRadians(0)),
//            new Pose(0,0,Math.toRadians(0)),
//            new Pose(0,0,Math.toRadians(0)),
//            new Pose(0,0,Math.toRadians(0))
//    };

    @Override
    public void runOpMode() throws InterruptedException {
        follower = Constants.create(hardwareMap);
        shooter = new Shooter(hardwareMap);
        turret = new Turret(hardwareMap);
        intake = new Intake(hardwareMap);
        sensors = new Sensors(hardwareMap);

        Globals.alliance = Globals.Alliance.RED;
        Globals.faze = Globals.Faze.TeleOp;

        follower.setPose(startingPose);
        follower.localizer.update();

        telemetry.setMsTransmissionInterval(11);
        telemetry.addLine("Ce i in mana nu i minciuna");
        telemetry.update();

        timer.startTime();

        waitForStart();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        loops.startTime();

        while (opModeIsActive()) {
            for (LynxModule hub : allHubs) hub.clearBulkCache();

            follower.localizer.update();

            if(gamepad1.right_trigger >= 0.25) {
                Intake.state = Intake.State.Transfer;
                Shooter.state = Shooter.State.Shoot;
            } else {
                if(Intake.state == Intake.State.Transfer)
                    Intake.state = Intake.State.Intake;
                Shooter.state = Shooter.State.Idle;
            }

            if(gamepad1.left_bumper && !shooter.is_shooting() && timer.milliseconds() > 250) {
                if(intake.is_intaking())
                    Intake.state = Intake.State.Stopped;
                else
                    Intake.state = Intake.State.Intake;

                timer.reset();
            }

            if(gamepad1.dpad_left && timer.milliseconds() > 250) {
                if(intake.is_intaking())
                    Intake.state = Intake.State.Stopped;
                else
                    Intake.state = Intake.State.Reverse;

                timer.reset();
            }

            // Failsafe buttons

            if (gamepad2.right_bumper && timer.milliseconds() > 250) {
                Globals.offset_turret += 0.5;
                timer.reset();
            }

            if (gamepad2.left_bumper && timer.milliseconds() > 250) {
                Globals.offset_turret -= 0.5;
                timer.reset();
            }

            if (gamepad2.x) Globals.offset_shooter += 20;

            if (gamepad2.circle) Globals.offset_shooter -= 20;

            if (gamepad2.right_trigger >= 0.25 || gamepad1.left_bumper) {
                follower.setPose(new Pose(startingPose.x(), startingPose.y(), startingPose.heading()));
                Globals.offset_shooter = 0.0;
                Globals.offset_turret = 0.0;
            }

//            if (gamepad2.options) {
//                Pose current = follower.pose();
//
//                int closestIndex = 0;
//                double closesetDistance = 144.5;
//
//                for (int i = 0; i < resetPose.length; i++) {
//                    double distance = Math.hypot(resetPose[i].x() - current.x(), resetPose[i].y() - current.y());
//
//                    if (distance < closesetDistance) {
//                        closesetDistance = distance;
//                        closestIndex = i;
//                    }
//                }
//
//                Globals.offset_shooter = 0.0;
//                Globals.offset_turret = 0.0;
//                follower.setPose(resetPose[closestIndex]);
//            }


            Pose pose = follower.pose();
            Velocity velo = follower.velocity();
            shooter.update(pose.x(), pose.y(), velo.vx, velo.vy, pose.heading());
            turret.update(pose.x(), pose.y(), velo.vx, velo.vy, velo.omega , pose.heading());
            intake.update();

            Pose selectedGoal = Globals.getSelectedGoal(Globals.alliance,new Pose(pose.x(), pose.y()));

            telemetry.addLine(" -- sensors voltage -- ");

            telemetry.addData("first senspr v: ", sensors.first_sensor.getVoltage());
            telemetry.addData("second sensor v: ", sensors.second_sensor.getVoltage());
            telemetry.addData("third sensor v: ", sensors.third_sensor.getVoltage());
            telemetry.addData("fourth sensor v: ", sensors.fourth_sensor.getVoltage());
            telemetry.addData("stopper v: ", sensors.readStopperAnalog());

            telemetry.addLine(" -- shooter values -- ");

            telemetry.addData("vel" , Globals.currentVel);
            telemetry.addData("error, ",Globals.error);

            telemetry.addLine(" -- balls in robot -- ");

            telemetry.addData("first ball", Globals.first_ball);
            telemetry.addData("second ball", Globals.second_ball);
            telemetry.addData("third ball", Globals.third_ball);
            telemetry.addData("fourth ball", Globals.fourth_ball);

            telemetry.addLine(" -- looking at goal -- ");

            telemetry.addData("looking at x: ", selectedGoal.x());
            telemetry.addData("looking at y: ", selectedGoal.y());

            telemetry.addLine(" -- robot pose -- ");

            telemetry.addData("x", pose.x());
            telemetry.addData("y", pose.y());
            telemetry.addData("heading", Math.toDegrees(pose.heading()));

            telemetry.addLine(" -- offsets -- ");

            telemetry.addData("offset turret deg: ", Globals.offset_turret);
            telemetry.addData("offset shooter vel: ", Globals.offset_shooter);

            telemetry.addLine(" -- loop time -- ");

            telemetry.addData("miliseconds: ", loops.milliseconds());

            telemetry.update();
            loops.reset();
        }
    }
}
