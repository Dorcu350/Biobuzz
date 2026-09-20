package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

public class Globals {

    //HARDWAREMAP -----------------
    public static boolean[] balls = {false,false,false,false};
    public static String[] motorsIntake = {"intake","transfer"};
    public static String servoArms = "arms", servoHood = "hood", servoStopper = "stopper";
    public static String[] motorShooter = {"down","up"};
    public static String[] motorDriveTrain = {"frontLeft","frontRight","backLeft","backRight"};
    public static String[] servoTurret = {"servoLeft","servoRight","servoBack"};
    public static boolean[] transferBall = {false,false,false,false};

    //SHOOTER + TURRET -----------------
    public static double shooterOffset = 0.0;
    public static double targetVel ,currentVel ,error;
    public static double distanceFromGoal, virtualDistanceFromGoal;
    public static double nominalVoltage = 12.0;
    public static double deadBandTurret = 0.0 , deadBandShooter = 0.0;

    //GOALS -----------------
    public static double xGoalBlueRight = 0.0, xGoalBlueLeft = 0.0, yGoalBlue = 0.0;
    public static double xGoalRedRight = 0.0, xGoalRedLeft = 0.0, yGoalRed = 0.0;
    public static double xMiddleField;

    //MISC -----------------
    public static boolean start_transfer = false, sotmActive = false, pre_spin;

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
