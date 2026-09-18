//package org.firstinspires.ftc.teamcode.Valvoasilii.Utils;
//
//import com.qualcomm.robotcore.util.ElapsedTime;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//
//public class Math {
//    // Rezultate finale folosite de Turret si Shooter
//    public double shooterWorldX, shooterWorldY;
//    public double xGoal, yGoal;
//    public double virtualTargetX, virtualTargetY;
//    public double distance;
//    public double targetAngle, relativeAngle;
//    public double timeOfFlight;
//    public double omega = 0.0;
//
//    // Setări SOTM (FtcDashboard)
//    public static double exitVelocity = 300.0;
//    public static double launchAngleDeg = 45.0;
//    public static double latency = 0.06;
//    public static int sotmIterations = 3;
//
//    private double lastHeading = Double.NaN;
//    private final ElapsedTime timer = new ElapsedTime();
//
//    public void update(double x, double y, double vx, double vy, double heading, boolean sotmActive) {
//        double dt = getDeltaTime();
//        updateOmega(heading, dt);
//
//        double cleanVx = (java.lang.Math.abs(vx) > Globals.deadBandTurret) ? vx : 0.0;
//        double cleanVy = (java.lang.Math.abs(vy) > Globals.deadBandTurret) ? vy : 0.0;
//
//        selectGoal(x, y);
//
//        if (sotmActive) {
//            calculateSOTM(x, y, cleanVx, cleanVy, heading);
//        } else {
//            calculateStaticShooterPos(x, y, heading);
//            virtualTargetX = xGoal;
//            virtualTargetY = yGoal;
//        }
//
//        calculateAnglesAndDistance(heading);
//        syncGlobals(sotmActive);
//    }
//
//    // Calcul iterativ pentru Shoot On The Move
//    public void calculateSOTM(double x, double y, double vx, double vy, double heading) {
//        double hPred = AngleUnit.normalizeRadians(heading + omega * latency);
//
//        shooterWorldX = x + (vx * latency) + (Globals.shooterOffset * java.lang.Math.cos(hPred));
//        shooterWorldY = y + (vy * latency) + (Globals.shooterOffset * java.lang.Math.sin(hPred));
//
//        double vsx = vx - (omega * Globals.shooterOffset * java.lang.Math.sin(hPred));
//        double vsy = vy + (omega * Globals.shooterOffset * java.lang.Math.cos(hPred));
//
//        double horizVel = exitVelocity * java.lang.Math.cos(java.lang.Math.toRadians(launchAngleDeg));
//
//        double gx = xGoal;
//        double gy = yGoal;
//
//        for (int i = 0; i < sotmIterations; i++) {
//            double dist = java.lang.Math.hypot(gx - shooterWorldX, gy - shooterWorldY);
//            timeOfFlight = (horizVel > 1e-6) ? (dist / horizVel) : 0;
//            gx = xGoal - (vsx * timeOfFlight);
//            gy = yGoal - (vsy * timeOfFlight);
//        }
//
//        virtualTargetX = gx;
//        virtualTargetY = gy;
//    }
//
//    private void selectGoal(double x, double y) {
//        if (Globals.alliance == Globals.Alliance.BLUE) {
//            xGoal = (x <= Globals.xCenterBlue) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
//            yGoal = (y <= Globals.yCenterBlue) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;
//        } else {
//            xGoal = (x <= Globals.xCenterRed) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
//            yGoal = (y <= Globals.yCenterRed) ? Globals.yGoalRedLeft : Globals.yGoalRedRight;
//        }
//    }
//
//    private void calculateStaticShooterPos(double x, double y, double heading) {
//        shooterWorldX = x + (Globals.shooterOffset * java.lang.Math.cos(heading));
//        shooterWorldY = y + (Globals.shooterOffset * java.lang.Math.sin(heading));
//    }
//
//    private void calculateAnglesAndDistance(double heading) {
//        distance = java.lang.Math.hypot(virtualTargetX - shooterWorldX, virtualTargetY - shooterWorldY);
//        targetAngle = AngleUnit.normalizeRadians(java.lang.Math.atan2(virtualTargetY - shooterWorldY, virtualTargetX - shooterWorldX) + java.lang.Math.PI);
//        relativeAngle = java.lang.Math.toDegrees(AngleUnit.normalizeRadians(targetAngle - heading)) + Globals.turretOffset;
//    }
//
//    private void updateOmega(double heading, double dt) {
//        if (!Double.isNaN(lastHeading)) {
//            double deltaHeading = AngleUnit.normalizeRadians(heading - lastHeading);
//            omega += 0.35 * (deltaHeading / dt - omega);
//        }
//        lastHeading = heading;
//    }
//
//    private double getDeltaTime() {
//        double dt = timer.seconds();
//        timer.reset();
//        return (dt <= 1e-4 || dt > 0.25) ? 0.02 : dt;
//    }
//
//    private void syncGlobals(boolean sotmActive) {
//        Globals.virtualTargetX = virtualTargetX;
//        Globals.virtualTargetY = virtualTargetY;
//        Globals.distanceFromGoal = distance;
//        Globals.sotmActive = sotmActive;
//    }
//}