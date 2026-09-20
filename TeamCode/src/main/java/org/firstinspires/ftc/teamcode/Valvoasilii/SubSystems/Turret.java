package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import static java.lang.Math.abs;

import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Turret {
    final TelemetryManager telemetryM;
    CachingServo servoLeft,servoRight,servoBack;
    Sensors sensors;
    public static double target_angle, relative_angle, target_position;
    public static double shooterWorldX , shooterWorldY , surface_speed, heading_comp;
    public static double MIN_ANGLE,  MAX_ANGLE, MIN_POS, MAX_POS, offset;
    public static double xGoal, yGoal;
    public enum State {
        FailSafe,
        Normal
    }
    public static State state;
    public void moveTo(double pos) { servoLeft.setPosition(pos); servoRight.setPosition(pos); servoBack.setPosition(pos);}

    public void update(double x, double y, double vx, double vy, double vh,double heading) {

        shooterWorldX = x + (Globals.shooterOffset * Math.cos(heading));

        shooterWorldY = y + (Globals.shooterOffset * Math.sin(heading));

        if(Globals.sotmActive) {
            double deadzone = Globals.deadBandTurret; // Adjust this if 0.5 noise persists

            double cleanVx = (Math.abs(vx) < deadzone) ? 0 : vx;

            double cleanVy = (Math.abs(vy) < deadzone) ? 0 : vy;

            double cleanVh = (Math.abs(vh) < deadzone) ? 0 : vh;


            double surfaceSpeedInches = (76.2 * Math.PI * (Globals.targetVel / 60.0)) / 25.4;

            double v_ball = surfaceSpeedInches * surface_speed;


            double timeToGoal = sensors.getTOF(Globals.virtualDistanceFromGoal);

            double ghostX, ghostY;

            if (Globals.alliance == Globals.Alliance.BLUE) {
                xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
                ghostX = xGoal - (cleanVx * timeToGoal);

                ghostY = Globals.yGoalBlue - (cleanVy * timeToGoal);

            } else {
                xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
                ghostX = xGoal - (cleanVx * timeToGoal);

                ghostY = Globals.yGoalRed - (cleanVy * timeToGoal);

            }

            target_angle = Math.atan2(ghostY - shooterWorldY, ghostX - shooterWorldX) + Math.PI + cleanVh * heading_comp;

        } else {
            if (Globals.alliance == Globals.Alliance.BLUE) {

                xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalBlueLeft : Globals.xGoalBlueRight;
                yGoal = Globals.yGoalBlue;

            } else {

                xGoal = (x <= Globals.xMiddleField) ? Globals.xGoalRedLeft : Globals.xGoalRedRight;
                yGoal = Globals.yGoalRed;

            }

            target_angle = Math.atan2(yGoal - shooterWorldY, xGoal - shooterWorldX) + Math.PI;

        }


        target_angle = AngleUnit.normalizeRadians(target_angle);

        relative_angle = Math.toDegrees(AngleUnit.normalizeRadians(target_angle - heading)) + offset;


            relative_angle = Math.max(MIN_ANGLE, Math.min(MAX_ANGLE, relative_angle));


            target_position = Range.scale(relative_angle, MIN_ANGLE, MAX_ANGLE, MIN_POS, MAX_POS);



        switch (state) {
            case FailSafe:

                moveTo(0.5); // MIJLOC SPRE SPATE

                break;
            case Normal:

                moveTo(target_position);

                break;
        }
    }

    public Turret(HardwareMap map) {
        servoLeft = new CachingServo(map.get(Servo.class, Globals.servoTurret[0]));
        servoRight = new CachingServo(map.get(Servo.class, Globals.servoTurret[1]));
        servoBack = new CachingServo(map.get(Servo.class, Globals.servoTurret[2]));

        sensors = new Sensors(map);

        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        state = State.Normal;
    }
}
