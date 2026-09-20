package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import static java.lang.Math.abs;
import static java.lang.Math.pow;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Shooter {
    final TelemetryManager telemetryM;
    CachingDcMotorEx shooterDown,shooterUp;
    CachingServo stopper;
    Sensors sensors;
    public static double kP , kV , kS;
    public static double targetVel , error;
    public static double stopperOpen , stopperClose;
    public static double shooterWorldX , shooterWorldY;
    public static boolean start = false;
    double xGoal, yGoal;
    VoltageSensor voltageSensorShooter;
    public enum State {
        Shoot,
        Idle,
    }
    public static State state;

    public void setPower(double power) { shooterDown.setPower(power); shooterUp.setPower(power); }


    public void update(double x, double y, double vx, double vy, double heading) {

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));
        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        if (Globals.alliance == Globals.Alliance.BLUE) {
            xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
            yGoal = Globals.yGoalBlue;
        }
        else {
            xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
            yGoal = Globals.yGoalRed;
        }


        if(Globals.sotmActive) {
            double cleanVx = (Math.abs(vx) < Globals.deadBandShooter) ? 0 : vx;
            double cleanVy = (Math.abs(vy) < Globals.deadBandTurret) ? 0 : vy;

            double staticDistance = Math.hypot(xGoal - shooterWorldX, yGoal - shooterWorldY);

            double timeToGoal = sensors.getTOF(staticDistance);

            double predRobotX = shooterWorldX + (cleanVx * timeToGoal);
            double predRobotY = shooterWorldY + (cleanVy * timeToGoal);

            Globals.virtualDistanceFromGoal = Math.hypot(xGoal - predRobotX, yGoal - predRobotY);
            targetVel = calculateShooterRPM(Globals.virtualDistanceFromGoal);
        } else {
            Globals.distanceFromGoal = Math.hypot(xGoal - shooterWorldX,  yGoal - shooterWorldY);

            targetVel = calculateShooterRPM(Globals.distanceFromGoal);
        }

        double vel = shooterUp.getVelocity();
        error = targetVel - vel;

        Globals.currentVel = vel;
        Globals.targetVel = targetVel;
        Globals.error = error;

        switch (state) {

            case Shoot:

                start = true;
                Globals.pre_spin = false;
                Globals.start_transfer = true;

                if (noError(error)){
                    stopper.setPosition(stopperOpen);
                    sensors.resetTransfer();
                }
                if (sensors.stopperOpen()) Globals.start_transfer = true;

                break;

            case Idle:

                start = true;
                Globals.start_transfer = false;
                Globals.pre_spin = true;
                stopper.setPosition(stopperClose);

                break;
        }

        if (start) {
            double voltageScaling = Globals.nominalVoltage / voltageSensorShooter.getVoltage();

            double feedForward = (kV * targetVel) + kS;
            double proportional = kP * (error);

            double pow = (feedForward + proportional) * voltageScaling;
            pow = Math.max(-1.0, Math.min(1.0, pow));

            setPower(pow);

        } else setPower(0);
    }
    public boolean noError(double error) {
        return (abs(error) <= 80);
    }

    public double calculateShooterRPM(double distance) {
        return distance;
    }

    public Shooter(HardwareMap map) {
        shooterUp = new CachingDcMotorEx(map.get(DcMotorEx.class, Globals.motorShooter[0]));
        shooterDown = new CachingDcMotorEx(map.get(DcMotorEx.class, Globals.motorShooter[1]));

        stopper = new CachingServo(map.get(Servo.class, Globals.servoStopper));

        shooterUp.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterDown.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooterUp.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterDown.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooterDown.setDirection(DcMotorSimple.Direction.REVERSE);

        sensors = new Sensors(map);

        voltageSensorShooter = map.voltageSensor.iterator().next();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        state = State.Idle;
        sensors.resetTransfer();
    }

    //            yGoal = (y <= Globals.yCenterBlue) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;

}
