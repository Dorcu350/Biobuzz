package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.drivetrain.DrivePowers;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.ManualDrive;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Velocity;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;

//import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Hood;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Intake;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Sensors;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
//import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@TeleOp(name = "TeleOp Red")
public class TeleOpRed extends LinearOpMode {
    Shooter shooter;
    Follower follower;
    Turret turret;
    Intake intake;
//    Hood hood;
    Sensors sensors;
    ElapsedTime timer = new ElapsedTime();
    Pose startingPose = new Pose(7.25, 6.12, Math.toRadians(0));
    double orbitx = 0,orbity = 0,orbitheading = 0;

    @Override
    public void runOpMode() throws InterruptedException {
        follower = Constants.create(hardwareMap);
        shooter = new Shooter(hardwareMap);
        turret = new Turret(hardwareMap);
        intake = new Intake(hardwareMap);
//        hood = new Hood(hardwareMap);
        sensors = new Sensors(hardwareMap);

        Globals.alliance = Globals.Alliance.RED;

        follower.setPose(startingPose);
        follower.update();

        telemetry.setMsTransmissionInterval(11);
        telemetry.addLine("Ce i in mana nu i minciuna");
        telemetry.update();

        timer.startTime();

        waitForStart();

        while (opModeIsActive()) {
            DrivePowers powers = ManualDrive.fieldCentric(
                    gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    follower.pose().heading()
            );

            follower.manual(powers);

            if (gamepad1.options) {
                follower.setPose(new Pose(follower.pose().x(), follower.pose().y(), 0));
            }

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

            if (gamepad1.dpad_up) {
                orbitx = follower.pose().x();
                orbity = follower.pose().y();
                orbitheading = follower.pose().heading();
            }

            follower.update();

            Pose pose = follower.pose();
            Velocity velo = follower.velocity();
            shooter.update(pose.x(), pose.y(), velo.vx, velo.vy, pose.heading());
            turret.update(pose.x(), pose.y(), velo.vx, velo.vy, velo.omega , pose.heading());
            intake.update();
//            hood.update();

            Pose selectedGoal = Globals.getSelectedGoal(Globals.alliance,new Pose(pose.x(), pose.y()));

            telemetry.update();

            telemetry.addData("first senzor v ", sensors.first_sensor.getVoltage());
            telemetry.addData("second senzor v ", sensors.second_sensor.getVoltage());
            telemetry.addData("third senzor v ", sensors.third_sensor.getVoltage());
            telemetry.addData("fourth senzor v ", sensors.fourth_sensor.getVoltage());

            telemetry.addData("stopper v", sensors.readStopperAnalog());

            telemetry.addData("vel" , Globals.currentVel);
            telemetry.addData("error, ",Globals.error);

            telemetry.addData("first ball", Globals.first_ball);
            telemetry.addData("2 ball", Globals.second_ball);
            telemetry.addData("third ball", Globals.third_ball);
            telemetry.addData("last ball", Globals.fourth_ball);

            telemetry.addData("current goal", Globals.currentGoal);

            telemetry.addData("looking at x: ", selectedGoal.x());
            telemetry.addData("looking at y: ", selectedGoal.y());
            telemetry.addData("distance to seleceted goal: ", Globals.distanceFromSelectedGoal);

            telemetry.addData("x", pose.x());
            telemetry.addData("y", pose.y());
            telemetry.addData("heading", Math.toDegrees(pose.heading()));

            telemetry.addData("differential x", (orbitx - pose.x()));
            telemetry.addData("differntial y", (orbity - pose.y()));
            telemetry.addData("differential heading", (orbitheading - pose.heading()));
        }
    }
}
