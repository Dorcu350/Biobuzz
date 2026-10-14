package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

public class Sensors {
    public AnalogInput first_sensor, second_sensor, third_sensor, fourth_sensor;
    AnalogInput stopper_a;
    public Limelight3A limelight;
    public OctoQuad octoquad;
    public static double voltageFirstSensor = 0.0, voltageSecondSensor = 0.0, voltageThirdSensor = 0.0, voltageFourthSensor = 0.0;
    private final OctoQuad.LocalizerDataBlock localizer = new OctoQuad.LocalizerDataBlock();
    private double x, y, heading;

    public void checkFullTransfer() {
        if (first_sensor.getVoltage() < voltageFirstSensor)
            Globals.firstBall = true;

        if (Globals.firstBall && second_sensor.getVoltage() < voltageSecondSensor)
            Globals.secondBall = true;

        if (Globals.secondBall && third_sensor.getVoltage() < voltageThirdSensor)
            Globals.thirdBall = true;

        if (Globals.thirdBall && fourth_sensor.getVoltage() < voltageFourthSensor)
            Globals.fourthBall = true;
    }

    public void resetTransfer() {
        Globals.firstBall = false; Globals.secondBall = false;
        Globals.thirdBall = false; Globals.fourthBall = false;
    }

    public double getTOF(double distance) {
        return 0;
    }

    // STOPPER -----------------------
    public double readStopperAnalog() { return stopper_a.getVoltage(); }
    public boolean stopperOpen() { return readStopperAnalog() > 0.0; }

    // OCTOQUAD -----------------------
    public void updatePose() {
        octoquad.readLocalizerData(localizer);
        if (localizer.isDataValid()) {
            x = DistanceUnit.INCH.fromMm(localizer.posX_mm);
            y = DistanceUnit.INCH.fromMm(localizer.posY_mm);
            heading = localizer.heading_rad;
        }
    }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getHeading() { return heading; }

    public Sensors(HardwareMap map) {
        first_sensor = map.get(AnalogInput.class, " ");
        second_sensor = map.get(AnalogInput.class, " ");
        third_sensor = map.get(AnalogInput.class, " ");
        fourth_sensor = map.get(AnalogInput.class, " ");

        stopper_a = map.get(AnalogInput.class, "stopper");
        limelight = map.get(Limelight3A.class, "limelight");

        octoquad = map.get(OctoQuad.class, "octoquad");
    }
}