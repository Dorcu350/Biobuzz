package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.pedropathing.follower.Follower;
import com.pedropathing.math.Pose;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Hood;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Intake;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Sensors;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Shooter;
import org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems.Turret;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.opencv.core.Mat;

import java.util.List;

public class TeleOpBlue extends LinearOpMode {
    public static Pose startingPose = new Pose(0,0, Math.toRadians(0));
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

        follower.update();
        Globals.alliance = Globals.Alliance.BLUE;
        Globals.faze = Globals.Faze.TeleOp;

        timer.startTime();
        loops.reset();
        telemetry.setMsTransmissionInterval(11);
        telemetry.addLine("Opinio de valvoasilio si in parametris trahit");
        telemetry.update();

        waitForStart();

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        while (opModeIsActive()) {

            for (LynxModule hub : allHubs) hub.clearBulkCache();

            follower.update();
            turret.update(0,0,0,0,0,0);
            hood.update();
            shooter.update(0,0,0,0,0);
            intake.update();
            follower.update();

        }

    }
}
