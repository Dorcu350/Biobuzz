package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Shooter {
    CachingDcMotorEx shooterDown,shooterUp;
    CachingServo stopper;
    Turret turret;
    Sensors sensors;
    public static double kP,kV,kS;
    public static double targetVel,error;
    public static double stopperOpen,stopperClose;
    public static double shooterWorldX,shooterWorldY;
    public static double look_ahaed_time;
    public static boolean start = false;
    VoltageSensor voltageSensorShooter;
    public enum State {
        Stopped,
        Idle,
        ShootingNormal,
        ShootingSOTM
    }
    public static State state;

    public void setPower(double power) { shooterDown.setPower(power); shooterUp.setPower(power); }

    public void update(double x, double y, double heading) {
        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));
        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        double xGoal,yGoal,trueReligionX,trueReligionY;
        if (Globals.alliance == Globals.Alliance.BLUE) {
            xGoal = (x <= Globals.xCenterBlue) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
            yGoal = (y <= Globals.yCenterBlue) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;
        } else {
            xGoal = (x <= Globals.xCenterRed) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
            yGoal = (y <= Globals.yCenterRed) ? Globals.yGoalRedLeft : Globals.yGoalRedRight;
        }
        trueReligionX = (Globals.sotmActive) ? Globals.virtualTargetX : xGoal;
        trueReligionY = (Globals.sotmActive) ? Globals.virtualTargetY : yGoal;

        double vel = shooterUp.getVelocity();
        double distance = Math.hypot(trueReligionX - shooterWorldX,trueReligionY - shooterWorldY);
        targetVel = calculateShooterRPM(distance);
        error = targetVel - vel;
        Globals.currentVel = vel;
        Globals.distanceFromGoal = distance;
        Globals.targetVel = targetVel;
        Globals.error = error;

        if (start) {
            double voltageScaling = Globals.nominalVoltage / voltageSensorShooter.getVoltage();

            double feedForward = (kV * targetVel) + kS;
            double proportional = kP * (error);

            double pow = (feedForward + proportional) * voltageScaling;
            pow = Math.max(-1.0, Math.min(1.0, pow));

            setPower(pow);
        }

        switch (state) {
            case Stopped:
                start = false;
                Globals.startTransfer = false;
                break;
            case Idle:
                start = true;
                Globals.startTransfer = false;
                stopper.setPosition(stopperClose);
                break;
            case ShootingSOTM:
                start = true;
                if (!isTurretInSotm()) {
                    state = State.ShootingNormal;
                }
                if (noError(error)){
                    stopper.setPosition(stopperOpen);
                    resetTransfer();
                }
                if (sensors.stopperOpen()) Globals.startTransfer = true;
                break;
            case ShootingNormal:
                start = true;
                Globals.sotmActive = false;
                if (noError(error)){
                    stopper.setPosition(stopperOpen);
                    resetTransfer();
                }
                if (sensors.stopperOpen()) Globals.startTransfer = true;
                break;
        }

    }

    private void resetTransfer() {
        Globals.balls[0] = false; Globals.balls[1] = false;
        Globals.balls[2] = false; Globals.balls[3] = false;
    }

    private boolean isTurretInSotm() {
        return Turret.state == Turret.State.TurretSotm;
    }

    public boolean noError(double error) {
        return (abs(error) < 40);
    }

    public double calculateShooterRPM(double distance) {
        return distance;
    }

    public Shooter(HardwareMap map) {
        shooterUp = new CachingDcMotorEx(map.get(DcMotorEx.class, Globals.motorShooter[0]));
        shooterDown = new CachingDcMotorEx(map.get(DcMotorEx.class, Globals.motorShooter[1]));

        shooterUp.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterDown.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterUp.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterDown.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterDown.setDirection(DcMotorSimple.Direction.REVERSE);

        voltageSensorShooter = map.voltageSensor.iterator().next();
    }
}
