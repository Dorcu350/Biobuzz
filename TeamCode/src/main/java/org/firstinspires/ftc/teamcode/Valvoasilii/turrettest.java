package org.firstinspires.ftc.teamcode.Valvoasilii;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.bylazar.panels.Panels;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

@Configurable
@TeleOp(name = "Turret Test", group = "Test")
public class turrettest extends LinearOpMode {
    Servo servo_mij, stopper, hood;
    public static String mijloc = "t_mijloc";
    public static double pos = 0.5;
    public static double pos_hood = 0.5;
    public static double pos_stopper = 0.5;
    TelemetryManager telemetryM;
    AnalogInput stopper_a;
    AnalogInput first, third, fourth;
    DigitalChannel second;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();



        servo_mij = hardwareMap.get(Servo.class, mijloc);
        hood = hardwareMap.get(Servo.class, "hood");
        stopper = hardwareMap.get(Servo.class, "stopper");
        stopper_a = hardwareMap.get(AnalogInput.class, "stopper_a");
        first = hardwareMap.get(AnalogInput.class, "first");
        second = hardwareMap.get(DigitalChannel.class, "second");
        second.setMode(DigitalChannel.Mode.INPUT);
        third = hardwareMap.get(AnalogInput.class, "third");
        fourth = hardwareMap.get(AnalogInput.class, "fourth");

        telemetry.addData("Status", "Initialized & Ready");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            servo_mij.setPosition(pos);
            hood.setPosition(pos_hood);
            stopper.setPosition(pos_stopper);

            telemetry.addData("analog voltage", stopper_a.getVoltage());
            telemetry.addData("sensor first", first.getVoltage());
            telemetry.addData("sensor second mode", second.getMode());
            telemetry.addData("sensor second state", second.getState());
            telemetry.addData("sensor third", third.getVoltage());
            telemetry.addData("sensor fourth", fourth.getVoltage());

            telemetry.addData("Poziție trimisă la servo mijloc", pos);
            telemetry.addData("Poziție fizică servo mijloc", servo_mij.getPosition());
            telemetry.update();
        }
    }
}