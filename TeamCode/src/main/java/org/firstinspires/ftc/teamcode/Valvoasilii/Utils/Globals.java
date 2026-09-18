package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

public class Globals {
    public static boolean[] balls = {false,false,false,false};
    public static String[] motorsIntake = {"intake","transfer"};
    public static String[] motorShooter = {"down","up"};
    public static String[] motorDriveTrain = {"frontLeft","frontRight","backLeft","backRight"};
    public static String[] servoTurret = {"servoLeft","servoRight","servoBack"};
    public static boolean[] transferBall = {false,false,false,false};
//    Globals.balls[0] sensor stopper
//    Globals.balls[1] sensor transfer
//    Globals.balls[2] sensor transfer
//    Globals.balls[3] sensor intake
    public static double shooterOffset = 0.0,turretOffset = 0.0;
    public static double targetVel,currentVel,error,distanceFromGoal;
    public static double nominalVoltage = 12.0;
    public static double deadBandTurret = 0.0 ,deadBandShooter = 0.0;
    public static double xGoalBlueRight = 0.0, xGoalBlueLeft = 0.0, yGoalBlueRight = 0.0, yGoalBlueLeft = 0.0;
    public static double xGoalRedRight = 0.0, xGoalRedLeft = 0.0, yGoalRedRight = 0.0, yGoalRedLeft = 0.0;
    public static boolean startTransfer = false,sotmActive = false;
    public static double xCenterBlue,yCenterBlue;
    public static double xCenterRed,yCenterRed;
    public static double virtualTargetX, virtualTargetY;
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
