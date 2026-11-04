package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import android.health.connect.changelog.ChangeLogsRequest;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import javax.tools.DocumentationTool;

public class Sensors {
    DigitalChannel sensor_fourth,sensor_third,sensor_second,sensor_first;
    AnalogInput stopper_a;
    public Limelight3A limelight;
    GoBildaPinpointDriver pinpoint;



    // TRANSFER -----------------------

    public void checkFullTransfer() {
        if ( !sensor_first.getState() ) Globals.first_ball = true;
        if ( Globals.first_ball && !sensor_second.getState() ) Globals.second_ball = true;
        if ( Globals.second_ball && !sensor_third.getState() ) Globals.third_ball = true;
        if ( Globals.third_ball && !sensor_fourth.getState() ) Globals.fourth_ball = true;
    }

    public void resetTransfer() {
        Globals.first_ball = false; Globals.second_ball = false;
        Globals.third_ball = false; Globals.fourth_ball = false;
    }

    public double getTOF(double distance) {return 0;}


    //STOPPER -----------------------

    public double readStopperAnalog() { return stopper_a.getVoltage(); }
    public boolean stopperOpen() { return (readStopperAnalog() > 0.0); }


    public Sensors(HardwareMap map) {
        sensor_first = map.get(DigitalChannel.class, "first");
        sensor_second = map.get(DigitalChannel.class, "second");
        sensor_third = map.get(DigitalChannel.class, "third");
        sensor_fourth = map.get(DigitalChannel.class, "fourth");

        sensor_first.  setMode(DigitalChannel.Mode.INPUT);
        sensor_second. setMode(DigitalChannel.Mode.INPUT);
        sensor_third.  setMode(DigitalChannel.Mode.INPUT);
        sensor_fourth.  setMode(DigitalChannel.Mode.INPUT);


        stopper_a = map.get(AnalogInput.class, "stopper");
        limelight = map.get(Limelight3A.class, "limelight");

        pinpoint = map.get(GoBildaPinpointDriver.class, "pinpoint");
    }

}
