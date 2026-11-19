package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.arcrobotics.ftclib.hardware.motors.Motor;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;

import com.pedropathing.math.Pose;
import com.pedropathing.revhub.drivetrains.CachedMotor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

@Configurable
public class Shooter {
    Motor gay;
    final TelemetryManager telemetryM;
    CachingDcMotorEx shooter;
    CachingServo stopper;
    Sensors sensors;
    public static double kP = 0.0035, kV = 0.00034, kS = 0.063;
    public static double targetVel , error;
    public static double stopperOpen = 0.6 , stopperClose = 0.4;
    public double shooterWorldX , shooterWorldY;
    public boolean start = false;
    double xGoal, yGoal;
    VoltageSensor voltageSensorShooter;
    public enum State {
        Shoot,
        Idle,
    }
    public static State state;

    public void setPower(double power) { shooter.setPower(power); }

    public void update(double x, double y, double vx, double vy, double heading) {

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));
        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        Pose selectedGoal = Globals.getSelectedGoal(Globals.alliance,new Pose(x,y));
        xGoal = selectedGoal.x();
        yGoal = selectedGoal.y();

        // CALCULE SOTM ----------------------------------------------------

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

        targetVel += Globals.shooterOffset;

        double vel = shooter.getVelocity();
        error = targetVel - vel;

        Globals.currentVel = vel;
        Globals.targetVel = targetVel;
        Globals.error = error;

        // STATES ----------------------------------------------------

        switch (state) {

            case Shoot:

                start = true;
                Globals.pre_spin = false;

                if (noError(error))
                    stopper.setPosition(stopperOpen);

                if (sensors.stopperOpen())
                    Globals.start_transfer = true;

                sensors.resetTransfer();

                break;

            case Idle:

                start = true;
                Globals.start_transfer = false;
                Globals.pre_spin = true;
                stopper.setPosition(stopperClose);

                break;
        }

        // APLICARE PUTERE ----------------------------------------------------

        if (start) {
            double voltageScaling = Globals.nominalVoltage / voltageSensorShooter.getVoltage();

            double feedForward = (kV * targetVel) + kS;
            double proportional = kP * (error);

            double pow = (feedForward + proportional) * voltageScaling;
            pow = Math.max(-1.0, Math.min(1.0, pow));

            setPower(pow);

        } else setPower(0);

    }

    public double calculateShooterRPM(double distance) {
        return targetVel;
    }
    public boolean is_shooting() {
        return Shooter.state == State.Shoot;
    }
    public boolean noError(double error) {
        return error <= 40;
    }

    public Shooter(HardwareMap map) {
        shooter = new CachingDcMotorEx(map.get(DcMotorEx.class, Globals.motor_shooter));
        stopper = new CachingServo(map.get(Servo.class, Globals.servo_stopper));

        shooter.setDirection(DcMotorSimple.Direction.FORWARD);
        shooter.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        shooter.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        shooter.setDirection(DcMotorSimple.Direction.REVERSE);

        sensors = new Sensors(map);
        voltageSensorShooter = map.voltageSensor.iterator().next();

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        state = State.Idle;
        sensors.resetTransfer();
    }

}
