package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.math.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingServo;

@Configurable
public class Turret {
    final TelemetryManager telemetryM;
    CachingServo servoLeft,servoRight,servoCenter;
    Sensors sensors;
    public static double pos_test = 0.56;
    public static double target_angle, relative_angle, target_position;
    public static double shooterWorldX, shooterWorldY;
    public static double MIN_ANGLE = -160,  MAX_ANGLE = 160, MIN_POS = 0.186, MAX_POS = 0.933, heading_comp;
    //max pos 0.92
    //min pos 0.182
    public static double xGoal, yGoal;
    public enum State {
        FailSafe,
        Normal
    }
    public static State state;
    public void moveTo(double pos) { servoLeft.setPosition(pos); servoRight.setPosition(pos); servoCenter.setPosition(pos);}

    public void update(double x, double y, double vx, double vy, double vh,double heading) {

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));

        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        Pose selectedGoal = Globals.getSelectedGoal(Globals.alliance,new Pose(x,y));
        xGoal = selectedGoal.x();
        yGoal = selectedGoal.y();

        // SOTM BIAS ----------------------------------------------------

        if(Globals.sotmActive) {
            double deadzone = Globals.deadBandTurret;

            double cleanVx = (Math.abs(vx) < deadzone) ? 0 : vx;

            double cleanVy = (Math.abs(vy) < deadzone) ? 0 : vy;

            double cleanVh = (Math.abs(vh) < deadzone) ? 0 : vh;

            double ghostX, ghostY;

            double timeToGoal = sensors.getTOF(Globals.virtualDistanceFromGoal);

            ghostX = shooterWorldX + (cleanVx * timeToGoal);
            ghostY = shooterWorldY + (cleanVy * timeToGoal);

//            double rotationalVx = -cleanVh * Globals.shooterOffset * Math.sin(heading);
//            double rotationalVy =  cleanVh * Globals.shooterOffset * Math.cos(heading);
//
//            ghostX = shooterWorldX + (cleanVx + rotationalVx) * timeToGoal;
//            ghostY = shooterWorldY + (cleanVy + rotationalVy) * timeToGoal;
//
//            target_angle = Math.atan2(ghostY - shooterWorldY, ghostX - shooterWorldX) + Math.PI;
            target_angle = Math.atan2(ghostY - shooterWorldY, ghostX - shooterWorldX) + Math.PI + cleanVh * heading_comp;

        } else
            target_angle = Math.atan2(yGoal - shooterWorldY, xGoal - shooterWorldX) + Math.PI;

        // CALCUL TARGET ----------------------------------------------------

        target_angle = AngleUnit.normalizeRadians(target_angle);

        relative_angle = Math.toDegrees(AngleUnit.normalizeRadians(target_angle - heading)) + Globals.offset_turret;

        relative_angle = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, relative_angle));

        target_position = Range.scale(relative_angle, MIN_ANGLE, MAX_ANGLE, MIN_POS, MAX_POS);

        // STATES ----------------------------------------------------

        switch (state) {
            case FailSafe:

                moveTo(0.5); // MIJLOC SPRE SPATE

                break;
            case Normal:

                moveTo(target_position);
//                moveTo(pos_test);


                break;
        }
    }

    public Turret(HardwareMap map) {
        servoLeft   = new CachingServo(map.get(Servo.class, Globals.servo_left));
        servoRight  = new CachingServo(map.get(Servo.class, Globals.servo_right));
        servoCenter   = new CachingServo(map.get(Servo.class, Globals.servo_center));

        servoCenter.    setDirection(Servo.Direction.REVERSE);

        sensors     = new Sensors(map);
        telemetryM  = PanelsTelemetry.INSTANCE.getTelemetry();

        state       = State.Normal;
    }
}
