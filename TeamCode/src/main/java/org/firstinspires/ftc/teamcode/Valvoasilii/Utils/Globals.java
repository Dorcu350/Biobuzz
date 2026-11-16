package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

import com.bylazar.configurables.annotations.Configurable;
import com.pedropathing.math.Pose;

@Configurable
public class Globals {

    //HARDWAREMAP -----------------
    public static boolean first_ball = false,second_ball = false,third_ball = false,fourth_ball = false;
    public static String motor_intake = "intake", motor_transfer = "transfer";
    public static String servoFlower = "flower",servoDropdown = "dropdown", servoHood = "hood", servoStopper = "stopper";
    public static String motor_shooter = "shooter";
    public static String servo_left = "t_stanga", servo_right = "t_dreapta", servo_center = "t_mijloc";
    //SHOOTER + TURRET -----------------
    public static double shooterOffset = 0.0;
    public static double targetVel ,currentVel ,error;
    public static double distanceFromGoal, virtualDistanceFromGoal;
    public static double nominalVoltage = 12.0;
    public static double deadBandTurret = 0.0 , deadBandShooter = 0.0;

    //GOALS -----------------
    public static double yGoalBlueRight = 84.0, yGoalBlueLeft = 57.5, xGoalBlue = 84.0;
    public static double yGoalRedRight = 84.0, yGoalRedLeft = 57.5, xGoalRed = 58.0;
    public static double xMiddleField = 70;
    public static String[] Goals = {"left red", "right red", "left blue", "right blue"};
    public static String currentGoal;
    public static double distanceFromSelectedGoal,selectedX,selectedY;

    //MISC -----------------
    public static boolean drop_flower = false,force_drop_intake = false;
    public static boolean start_transfer = false, sotmActive = false, pre_spin , force_flower = false, force_dropdown = false;
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
