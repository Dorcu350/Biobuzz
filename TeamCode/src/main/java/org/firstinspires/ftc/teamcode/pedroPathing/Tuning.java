package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.algorithm.Foresight;
import com.pedropathing.revhub.drivetrains.Mecanum;
<<<<<<< HEAD
import com.pedropathing.revhub.localizers.PinpointLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.pedroPathing.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.PinpointTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.Tests;

@TeleOp
public class Tuning {

    @Tuner
    public static Procedure mecanum() {
=======
import com.pedropathing.revhub.localizers.OctoQuadLocalizer;
import com.pedropathing.tuning.autotune.Procedure;
import com.pedropathing.tuning.autotune.Tuner;

import org.firstinspires.ftc.teamcode.pedroPathing.procedures.ForesightTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.MecanumTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.OctoQuadTuner;
import org.firstinspires.ftc.teamcode.pedroPathing.procedures.Tests;

public class Tuning {
    @Tuner
    public static Procedure mecanumTuner() {
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
        return new MecanumTuner();
    }

    @Tuner
<<<<<<< HEAD
    public static Procedure pinpoint() {
        return new PinpointTuner();
=======
    public static Procedure octoquadTuner() {
        return new OctoQuadTuner();
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
    }

    @Tuner
    public static Procedure foresightTuner() {
<<<<<<< HEAD
        return new ForesightTuner((hardwareMap) -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig), (hardwareMap) -> new Mecanum(hardwareMap, Constants.drivetrainConfig));
    }

    // Foresight Tuner: copiază metoda exact din Tuning.java din Quickstart

    @Tuner
    public static Procedure tests() {
        return new Tests(
                hardwareMap -> new Mecanum(hardwareMap, Constants.drivetrainConfig),
                hardwareMap -> new PinpointLocalizer(hardwareMap, Constants.localizerConfig),
                () -> new Foresight(Constants.foresightConfig));
    }
}
=======
        return new ForesightTuner(
                hardwareMap -> new OctoQuadLocalizer(hardwareMap, Constants.localizerConfig),
                hardwareMap -> new Mecanum(hardwareMap, Constants.driveConfig)
        );
    }

    @Tuner
    public static Procedure tests() {
        return new Tests(
                hardwareMap -> new Mecanum(hardwareMap, Constants.driveConfig),
                hardwareMap -> new OctoQuadLocalizer(hardwareMap, Constants.localizerConfig),
                () -> new Foresight(Constants.foresightConfig)
        );
    }
}
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
