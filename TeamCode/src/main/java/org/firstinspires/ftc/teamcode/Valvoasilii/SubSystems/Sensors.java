package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.hardware.digitalchickenlabs.OctoQuad;
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
    public OctoQuad octoquad;
    Globals globals;

    public void checkFullTransfer() {
        if ( !sensor_first.getState() ) Globals.balls[0] = true;
        if ( Globals.balls[0] && !sensor_second.getState() ) Globals.balls[1] = true;
        if ( Globals.balls[1] && !sensor_third.getState() ) Globals.balls[2] = true;
        if ( Globals.balls[2] && !sensor_fourth.getState() ) Globals.balls[3] = true;
    }

    public double readStopperAnalog() { return stopper_a.getVoltage(); }
    public boolean stopperOpen() { return (readStopperAnalog() > 0.0); }
    public void resetEncoders() {
        octoquad.resetAllPositions();
    }
    public Sensors(HardwareMap map) {
        sensor_first = map.get(DigitalChannel.class, "first");
        sensor_second = map.get(DigitalChannel.class, "second");
        sensor_third = map.get(DigitalChannel.class, "third");
        sensor_fourth = map.get(DigitalChannel.class, "fourth");

        stopper_a = map.get(AnalogInput.class, "stopper");
        limelight = map.get(Limelight3A.class, "limelight");

        octoquad = map.get(OctoQuad.class, "octoquad");
        resetEncoders();
    }

}
