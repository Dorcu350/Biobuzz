package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;
import org.opencv.core.Mat;

import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Hood {
    CachingServo hood;
    public static double min = 0.0 , max = 1.0;
    public static double hoodAngle;
    public enum State {
        ShootingNormal,
        ShootingSOTM
    }
    public static State state;
    private double clamp(double value) { return Math.max(min, Math.min(max,value)); }

    public void update() {

        if (Globals.sotmActive)
            hoodAngle = calculateHoodAngle(Globals.virtualDistanceFromGoal);
        else
            hoodAngle = calculateHoodAngle(Globals.distanceFromGoal);

        hood.setPosition( clamp(hoodAngle) );

    }

    public double calculateHoodAngle(double distance) {
        return distance;
    }

    public Hood(HardwareMap map) {
        hood = new CachingServo(map.get(Servo.class, Globals.servoHood));

        state = State.ShootingSOTM;
    }
}
