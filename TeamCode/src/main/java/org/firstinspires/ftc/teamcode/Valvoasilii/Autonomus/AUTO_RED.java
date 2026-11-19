package org.firstinspires.ftc.teamcode.Valvoasilii.Autonomus;

import androidx.core.location.GnssStatusCompat;

import com.pedropathing.api.Paths;
import com.pedropathing.api.PoseFactory;
import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.pedropathing.math.Vector;
import com.pedropathing.math.Velocity;
import com.pedropathing.paths.Path;
import com.pedropathing.utils.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Intake;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Sensors;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;


@Autonomous(name = "auto rosu test")
//@Configurable
public class AUTO_RED extends OpMode {
    Pose startingPose = new Pose(0,0,Math.toRadians(0));
    Timer pathTimer, opmodeTimer;
    int pathState;
    Turret turret;
    Shooter shooter;
    Intake intake;
    Sensors sensors;
    Follower follower;

    private static PoseFactory robot = PoseFactory.degrees();
    private final Pose startPose = robot.of(50.8742, 6.4882, 180);
    private final Pose intakeCornerPose = robot.of(10, 6.5, 180);
    private final Pose scoreCornerPose = robot.of(15, 6.5, 180);

    public Path intakeCorner() {
        return Paths.line(startPose, intakeCornerPose).constant(180);
    }

    public Path scoreFirst() {
        return Paths.line(intakeCornerPose, scoreCornerPose).reverseTangent();
    }

    public double getTvalue() {
        return (follower.remainingDistance() / follower.distanceToEndpoint());
    }

    public void autonomusPathUpdate() {
        switch (pathState) {
            case 0:
                Shooter.state = Shooter.State.Shoot;
                follower.follow(intakeCorner());

                setPathState(1);
                break;
            case 1:
                if (!Globals.first_ball)
                    Shooter.state = Shooter.State.Idle;

                if (Globals.fourth_ball) {
                    follower.follow(scoreFirst());
                    setPathState(2);
                }

                break;
            case 2:
                Shooter.state = Shooter.State.Shoot;

                if (!Globals.first_ball)
                    Shooter.state = Shooter.State.Idle;

                break;
        }
    }

    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.reset();
    }

    @Override
    public void start() {
        opmodeTimer.reset();
        setPathState(0);
    }

    @Override
    public void init() {
        opmodeTimer = new Timer();

        turret = new Turret(hardwareMap);
        shooter = new Shooter(hardwareMap);
        intake = new Intake(hardwareMap);
        sensors = new Sensors(hardwareMap);
        follower = Constants.create(hardwareMap);

        follower.setPose(startingPose);

        sensors.checkFullTransfer();

        Globals.alliance = Globals.Alliance.RED;
        Globals.faze = Globals.Faze.Auto;
        Globals.sotmActive = true;

        Shooter.state = Shooter.State.Idle;
        Turret.state = Turret.State.Normal;

        Pose pose = follower.pose();
        Velocity velo = follower.velocity();
        turret.update(pose.x(), pose.y(), velo.vx, velo.vy, velo.omega , pose.heading());
        intake.update();
        follower.update();
    }

    @Override
    public void loop() {
        follower.update();
        Pose pose = follower.pose();
        Velocity velo = follower.velocity();
        turret.update(pose.x(), pose.y(), velo.vx, velo.vy, velo.omega , pose.heading());
        shooter.update(pose.x(), pose.y(), velo.vx, velo.vy, pose.heading());
        intake.update();
        sensors.checkFullTransfer();

        autonomusPathUpdate();

        telemetry.addLine(" -- Robot Pose -- ");

        telemetry.addData("x: ", pose.x());
        telemetry.addData("y: ", pose.y());
        telemetry.addData("heading: ", pose.heading());

        telemetry.addLine(" -- Follower -- ");

        telemetry.addData("Distance to end point of path", follower.distanceToEndpoint());
        telemetry.addData("remaining distance ", follower.remainingDistance());
        telemetry.addData("t value: ", getTvalue());
        telemetry.addData("path state: ", pathState);

        telemetry.addLine(" -- turret && shooter -- ");

        telemetry.addData("relative angle: ", Turret.relative_angle);
        telemetry.addData("xGoal:", Turret.xGoal);
        telemetry.addData("yGoal:", Turret.yGoal);

        telemetry.addData("target vel: ", Shooter.targetVel);
        telemetry.addData("shooter vel: ", shooter.is_shooting());

        telemetry.update();
    }

    @Override
    public void init_loop() {
        telemetry.addLine("dumnezeu sa ierte victimele mele");
        telemetry.speak("dumnezeu sa ierte victimele mele");
        telemetry.update();
    }

    @Override
    public void stop() {
    }
}
