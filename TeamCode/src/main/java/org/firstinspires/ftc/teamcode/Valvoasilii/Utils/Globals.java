package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
public class Globals {

    // HARDWARE MAP -----------------
    public static boolean first_ball = false,second_ball = false,third_ball = false,fourth_ball = false;
    public static String motor_intake = "intake", motor_transfer = "transfer";
    public static String servo_flower = "flower",servo_dropdown = "dropdown", servo_hood = "hood", servo_stopper = "stopper";
    public static String motor_shooter = "shooter";
    public static String servo_left = "t_stanga", servo_right = "t_dreapta", servo_center = "t_mijloc";
    // SHOOTER + TURRET VARIABLES ----------------
    public static double shooterOffset = 0.0;
    public static double targetVel ,currentVel ,error;
    public static double distanceFromGoal, virtualDistanceFromGoal;
    public static double nominalVoltage = 12.0;
    public static double deadBandTurret = 0.0 , deadBandShooter = 0.0;
    public static double offset_turret = 0.0, offset_shooter = 0.0;

    //MISC -----------------
    public static boolean start_transfer = false, sotmActive = false, pre_spin;
    public static boolean force_dropdown = false,force_flower = false;

    public enum Alliance {
        BLUE,
        RED
    }
    public static Alliance alliance;
    public enum Faze {
        Auto,
        TeleOp
    }
    public static Faze faze;


    //Pedro Offical goals

    public final static Pose center = new Pose(70.75, 70.75);
    public static double xOffset = 11.5, yOffset = 18;
    public static Pose leftBlueGoal = center.plus(new Pose(xOffset-3, yOffset));
    public static Pose leftRedGoal = center.plus(new Pose(-xOffset+1.5, yOffset));
    public static Pose rightBlueGoal = center.plus(new Pose(xOffset, -yOffset));
    public static Pose rightRedGoal = center.plus(new Pose(-xOffset-2, -yOffset));

    public static Pose getSelectedGoal(Alliance alliance,Pose currentPose) {
        if (alliance == Alliance.BLUE) {
            if (currentPose.y() > center.y())
                return leftBlueGoal;
            else
                return rightBlueGoal;
        } else {
            if (currentPose.y() > center.y())
                return leftRedGoal;
            else
                return rightRedGoal;
        }
    }
}
