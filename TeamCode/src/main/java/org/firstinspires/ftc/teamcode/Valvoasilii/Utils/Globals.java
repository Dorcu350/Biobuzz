package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

import com.bylazar.configurables.annotations.Configurable;

@Configurable
public class Globals {

    //HARDWAREMAP -----------------
    public static boolean first_ball = false,second_ball = false,third_ball = false,fourth_ball = false;
    public static String motor_intake = "intake", motor_transfer = "transfer";
    public static String servoFlower = "arms",servoDropdown = "dropdown", servoHood = "hood", servoStopper = "stopper";

    public static String shooter_up = "shooter_up" , shooter_down = "shooter_down";
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
    public static double xMiddleField;

    //MISC -----------------
//<<<<<<< HEAD
//    public static boolean start_transfer = false, sotmActive = false, pre_spin;
    public static boolean drop_flower = false,force_drop_intake = false;
//=======
    public static boolean start_transfer = false, sotmActive = false, pre_spin , force_flower = false, force_dropdown = false;
//>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1

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
}
