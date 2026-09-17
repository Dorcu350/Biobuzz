package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import static java.lang.Math.abs;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Turret {
    CachingServo servoLeft,servoRight,servoBack;
    public static double targetAngle,relativeAngle,targetPos;
    public static double shooterWorldX,shooterWorldY;
    public static double minAngle,maxAngle,minPos,maxPos;
    public void moveTo(double pos) { servoLeft.setPosition(pos); servoRight.setPosition(pos); servoBack.setPosition(pos);}

    public void update(double x, double y, double vx, double vy, double heading) {
        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));
        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        double cleanVx = (abs(vx) > Globals.deadBandTurret) ? vx : 0.0;
        double cleanVy = (abs(vy) > Globals.deadBandTurret) ? vy : 0.0;

        double xGoal,yGoal,temporaryDistance;
        if (Globals.alliance == Globals.Alliance.BLUE) {
            temporaryDistance = Math.hypot(Globals.xCenterBlue - shooterWorldX,Globals.yCenterBlue - shooterWorldY);
            xGoal = (temporaryDistance <= 0) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
            yGoal = (temporaryDistance <= 0) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;
        } else {
            temporaryDistance = Math.hypot(Globals.xCenterRed - shooterWorldX,Globals.yCenterRed - shooterWorldY);
            xGoal = (temporaryDistance <= 0) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
            yGoal = (temporaryDistance <= 0) ? Globals.yGoalRedLeft : Globals.yGoalRedRight;
        }

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));
        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        targetAngle = AngleUnit.normalizeRadians(Math.atan2(yGoal - shooterWorldY, xGoal - shooterWorldX) + Math.PI);

        relativeAngle = Math.toDegrees(targetAngle - heading) + Globals.turretOffset;
        relativeAngle = Math.max(minAngle,Math.min(maxAngle,relativeAngle));

        targetPos = Range.scale(relativeAngle,minAngle,maxAngle,minPos,maxPos);
    }

    public Turret(HardwareMap map) {
        servoLeft = new CachingServo(map.get(Servo.class, Globals.servoTurret[0]));
        servoRight = new CachingServo(map.get(Servo.class, Globals.servoTurret[1]));
        servoBack = new CachingServo(map.get(Servo.class, Globals.servoTurret[2]));
    }
}

//package org.firstinspires.ftc.teamcode.Geko.SubSystems;
//
//import com.qualcomm.robotcore.hardware.HardwareMap;
//import com.qualcomm.robotcore.hardware.Servo;
//import com.qualcomm.robotcore.util.ElapsedTime;
//import com.qualcomm.robotcore.util.Range;
//
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.teamcode.Geko.Utils.Globals;
//
//import dev.frozenmilk.dairy.cachinghardware.CachingServo;
//
//public class Turret {
//    CachingServo servoLeft, servoRight, servoBack;
//
//    public static double minAngle = -110, maxAngle = 110, minPos = 0.05, maxPos = 0.95;
//    public static double deadBand = 0.35;
//
//    public static double servoFreeRate = 60.0 / 0.115;
//    public static double servoSpeedPct = 0.85;
//    public static double gearRatio = 3.0;
//
//    public static double xGoalBlueLeft, yGoalBlueLeft, xGoalBlueRight, yGoalBlueRight;
//    public static double xGoalRedLeft, yGoalRedLeft, xGoalRedRight, yGoalRedRight;
//
//    public static boolean sotmEnabled = true;
//    public static double exitVelocity = 300;
//    public static double launchAngleDeg = 45;
//    public static double latency = 0.06;
//    public static int sotmIterations = 3;
//
//    public static double targetAngle, relativeAngle, commandedAngle, targetPos;
//    public static double shooterWorldX, shooterWorldY, shotDistance, timeOfFlight, omega;
//    public static boolean saturated;
//
//    private double lastHeading = Double.NaN, lastPos = Double.NaN;
//    private final ElapsedTime timer = new ElapsedTime();
//
//    public void moveTo(double pos) {
//        pos = Range.clip(pos, 0, 1);
//        servoLeft.setPosition(pos);
//        servoRight.setPosition(pos);
//        servoBack.setPosition(pos);
//        lastPos = pos;
//    }
//
//    public void update(double x, double y, double vx, double vy, double heading) {
//        double dt = timer.seconds(); timer.reset();
//        if (dt <= 1e-4 || dt > 0.25) dt = 0.02;
//
//        if (!Double.isNaN(lastHeading))
//            omega += 0.35 * (AngleUnit.normalizeRadians(heading - lastHeading) / dt - omega);
//        lastHeading = heading;
//
//        double lat = sotmEnabled ? latency : 0;
//        double h = AngleUnit.normalizeRadians(heading + omega * lat);
//        shooterWorldX = x + vx * lat + Globals.shooterOffset * Math.cos(h);
//        shooterWorldY = y + vy * lat + Globals.shooterOffset * Math.sin(h);
//
//        double vsx = sotmEnabled ? vx - omega * Globals.shooterOffset * Math.sin(h) : 0;
//        double vsy = sotmEnabled ? vy + omega * Globals.shooterOffset * Math.cos(h) : 0;
//
//        boolean blue = Globals.alliance == Globals.Alliance.BLUE;
//        double xl = blue ? xGoalBlueLeft : xGoalRedLeft,   yl = blue ? yGoalBlueLeft : yGoalRedLeft;
//        double xr = blue ? xGoalBlueRight : xGoalRedRight, yr = blue ? yGoalBlueRight : yGoalRedRight;
//        boolean left = Math.hypot(xl - shooterWorldX, yl - shooterWorldY)
//                <= Math.hypot(xr - shooterWorldX, yr - shooterWorldY);
//        double xGoal = left ? xl : xr, yGoal = left ? yl : yr;
//
//        double gx = xGoal, gy = yGoal;
//        double horiz = exitVelocity * Math.cos(Math.toRadians(launchAngleDeg));
//        for (int i = 0; i < sotmIterations; i++) {
//            double d = Math.hypot(gx - shooterWorldX, gy - shooterWorldY);
//            timeOfFlight = horiz > 1e-6 ? d / horiz : 0;
//            gx = xGoal - vsx * timeOfFlight;
//            gy = yGoal - vsy * timeOfFlight;
//        }
//        shotDistance = Math.hypot(gx - shooterWorldX, gy - shooterWorldY);
//
//        targetAngle = AngleUnit.normalizeRadians(Math.atan2(gy - shooterWorldY, gx - shooterWorldX) + Math.PI);
//
//        relativeAngle = Math.toDegrees(AngleUnit.normalizeRadians(targetAngle - h)) + Globals.turretOffset;
//        while (relativeAngle > 180) relativeAngle -= 360;
//        while (relativeAngle < -180) relativeAngle += 360;
//
//        double clamped = Range.clip(relativeAngle, minAngle, maxAngle);
//        saturated = Math.abs(clamped - relativeAngle) > 0.5;
//
//        if (Double.isNaN(commandedAngle)) commandedAngle = clamped;
//        double maxStep = servoFreeRate * servoSpeedPct / gearRatio * dt;
//        commandedAngle += Range.clip(clamped - commandedAngle, -maxStep, maxStep);
//
//        double span = maxAngle - minAngle;
//        if (Math.abs(span) < 1e-6) return;
//        targetPos = Range.clip(Range.scale(commandedAngle, minAngle, maxAngle, minPos, maxPos), 0, 1);
//        if (Double.isNaN(lastPos) || Math.abs(targetPos - lastPos) >= deadBand * Math.abs((maxPos - minPos) / span))
//            moveTo(targetPos);
//    }
//
//    public boolean onTarget() { return !saturated && Math.abs(relativeAngle - commandedAngle) <= 1.5; }
//
//    public Turret(HardwareMap map) {
//        servoLeft  = new CachingServo(map.get(Servo.class, Globals.servoTurret[0]));
//        servoRight = new CachingServo(map.get(Servo.class, Globals.servoTurret[1]));
//        servoBack  = new CachingServo(map.get(Servo.class, Globals.servoTurret[2]));
//        commandedAngle = Double.NaN; omega = 0; timer.reset();
//    }
//}