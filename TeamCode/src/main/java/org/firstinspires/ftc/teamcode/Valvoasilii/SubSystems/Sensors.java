package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import android.health.connect.changelog.ChangeLogsRequest;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

public class Sensors {
    public AnalogInput first_sensor, third_sensor, fourth_sensor, second_sensor;
    AnalogInput stopper_a;
    OctoQuad octoQuad;


    // TRANSFER -----------------------

    public void checkFullTransfer() {
        if ( first_sensor.getVoltage() >= 2.5 ) Globals.first_ball = true;
        if ( Globals.first_ball && second_sensor.getVoltage() >= 2.5 ) Globals.second_ball = true;
        if ( Globals.second_ball && third_sensor.getVoltage() >= 2.5 ) Globals.third_ball = true;
        if ( Globals.third_ball && fourth_sensor.getVoltage() >= 2.5 ) Globals.fourth_ball = true;
    }

    public void resetTransfer() {
        Globals.first_ball = false; Globals.second_ball = false;
        Globals.third_ball = false; Globals.fourth_ball = false;
    }

    public double getTOF(double distance) {return 0;}


    //STOPPER -----------------------

    public double readStopperAnalog() { return stopper_a.getVoltage(); }
    public boolean stopperOpen() { return (readStopperAnalog() > 0.9); }


    public Sensors(HardwareMap map) {
        first_sensor = map.get(AnalogInput.class, "first");
        second_sensor = map.get(AnalogInput.class, "second");
        third_sensor = map.get(AnalogInput.class, "third");
        fourth_sensor = map.get(AnalogInput.class, "fourth");
        stopper_a = map.get(AnalogInput.class, "stopper_a");

        octoQuad = map.get(OctoQuad.class, "octo");
    }
}