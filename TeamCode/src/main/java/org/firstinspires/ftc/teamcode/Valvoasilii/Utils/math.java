package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;

import static java.lang.Math.abs;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class math {
    public static double getTimeOfFlight(double dist) {return dist;}
    public static double clamp(double x,double min,double max) {return Math.max(min,Math.min(x,max));}
    public static double getCleanVel(double vel,double deadBand) {return ((abs(vel) <= deadBand) ? 0.0 : vel);}
    public static double getVelShooter(double dist) {return dist;}
    public static double getHoodAngle(double dist) {return dist;}
    public static double normalizeRadians(double x) {return AngleUnit.normalizeRadians(x);}
    public static double normalizeDegrees(double x) { return AngleUnit.normalizeDegrees(x);}
    public static double grade(double x) {return Math.toDegrees(x);}
    public static double normalizeDegreesToRadians(double x) { return AngleUnit.normalizeRadians(Math.toRadians(x));}

//    public Math(){}
}
