package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
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
    public static double pos_test = 0.5;
    public static double target_angle, relative_angle, target_position;
    public static double shooterWorldX , shooterWorldY , heading_comp;
    public static double MIN_ANGLE,  MAX_ANGLE, MIN_POS, MAX_POS, offset;
    public static double xGoal, yGoal;
    public enum State {
        FailSafe,
        Normal
    }
    public static State state;
    public void moveTo(double pos) { servoLeft.setPosition(pos); servoRight.setPosition(pos); servoCenter.setPosition(pos);}

    public void test() {
        moveTo(pos_test);
    }

    public void update(double x, double y, double vx, double vy, double vh,double heading) {

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));

        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        // SOTM BIAS ----------------------------------------------------

        if(Globals.sotmActive) {
            double deadzone = Globals.deadBandTurret;

            double cleanVx = (Math.abs(vx) < deadzone) ? 0 : vx;

            double cleanVy = (Math.abs(vy) < deadzone) ? 0 : vy;

            double cleanVh = (Math.abs(vh) < deadzone) ? 0 : vh;


            double timeToGoal = sensors.getTOF(Globals.virtualDistanceFromGoal);

            double ghostX, ghostY;

            // GOAL SELECTION ----------------------------------------------------

            if (Globals.alliance == Globals.Alliance.BLUE) {
                yGoal = (y <= Globals.xMiddleField) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;
                ghostY = yGoal - (cleanVy * timeToGoal);

                ghostX = Globals.xGoalBlue - (cleanVx * timeToGoal);

            } else {
                yGoal = (y <= Globals.xMiddleField) ? Globals.yGoalRedLeft : Globals.yGoalRedRight;
                ghostY = yGoal - (cleanVy * timeToGoal);

                ghostX = Globals.xGoalRed- (cleanVx * timeToGoal);

            }

            target_angle = Math.atan2(ghostY - shooterWorldY, ghostX - shooterWorldX) + Math.PI + cleanVh * heading_comp;

        } else {

            // GOAL SELECTION ----------------------------------------------------

            if (Globals.alliance == Globals.Alliance.BLUE) {

                yGoal = (y <= Globals.xMiddleField) ? Globals.yGoalBlueLeft : Globals.yGoalBlueRight;
                xGoal = Globals.xGoalBlue;

            } else {

                yGoal = (y <= Globals.xMiddleField) ? Globals.yGoalRedLeft : Globals.yGoalRedRight;
                xGoal = Globals.xGoalRed;

            }

            target_angle = Math.atan2(yGoal - shooterWorldY, xGoal - shooterWorldX) + Math.PI;

        }

        // CALCUL TARGET ----------------------------------------------------

        target_angle = AngleUnit.normalizeRadians(target_angle);

        relative_angle = Math.toDegrees(AngleUnit.normalizeRadians(target_angle - heading)) + offset;


        relative_angle = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, relative_angle));


        target_position = Range.scale(relative_angle, MIN_ANGLE, MAX_ANGLE, MIN_POS, MAX_POS);

        // STATES ----------------------------------------------------

        switch (state) {
            case FailSafe:

                moveTo(0.5); // MIJLOC SPRE SPATE

                break;
            case Normal:

//                moveTo(target_position);
                moveTo(pos_test);


                break;
        }
    }

    public Turret(HardwareMap map) {
        servoLeft   = new CachingServo(map.get(Servo.class, Globals.servo_left));
        servoRight  = new CachingServo(map.get(Servo.class, Globals.servo_right));
        servoCenter   = new CachingServo(map.get(Servo.class, Globals.servo_center));

        servoLeft.    setDirection(Servo.Direction.REVERSE);
        servoRight.   setDirection(Servo.Direction.REVERSE);

        sensors     = new Sensors(map);
        telemetryM  = PanelsTelemetry.INSTANCE.getTelemetry();

        state       = State.Normal;
    }
}
