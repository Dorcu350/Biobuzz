package org.firstinspires.ftc.teamcode.Valvoasilii;

//import com.acmerobotics.dashboard.FtcDashboard;
//import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.bylazar.panels.Panels;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

@Configurable
@TeleOp(name = "Turret Test", group = "Test")
public class turrettest extends LinearOpMode {
    Servo servo_mij, stopper;
    AnalogInput first;
    DcMotorEx motor_up,motor_down;
    DcMotorEx intake,transfer;
    public static String mijloc = "t_mijloc",stanga = "t_stanga", dreapta = "t_dreapta";
    public static double pos = 0.5; // min = 0.76 // max = 0.99 hood
    public static double stopper_open = 0.325 , stopper_close = 0.225;
    public static double kV = 0.0,kS = 0.0,kP = 0.0;
    public static double targetVel = 0;
    public static double stopper_voltage = 1,voltage_first = 0;
    TelemetryManager telemetryM;
    AnalogInput stopper_a;

    @Override
    public void runOpMode() throws InterruptedException {
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        motor_up = hardwareMap.get(DcMotorEx.class, "shooter_up");
        motor_down = hardwareMap.get(DcMotorEx.class, "shooter_down");
        servo_mij = hardwareMap.get(Servo.class, "t_mijloc");

        motor_down.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor_up.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        motor_down.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor_up.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        motor_down    .setDirection(DcMotorSimple.Direction.REVERSE);
        intake = hardwareMap.get(DcMotorEx.class, "intake");
        transfer = hardwareMap.get(DcMotorEx.class, "transfer");
        intake.setDirection(DcMotorSimple.Direction.REVERSE);

        first = hardwareMap.get(AnalogInput.class , "first");
        stopper = hardwareMap.get(Servo.class, "stopper");
        stopper_a = hardwareMap.get(AnalogInput.class, "stopper_a");

        telemetry.addData("Status", "Initialized & Ready");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
//            hood.setPosition(pos_hood);
//            dropdown.setPosition(pos_dropdown);

            servo_mij.setPosition(pos);

            if (gamepad1.right_bumper) {
                stopper.setPosition(stopper_open);
                if (stopper_a.getVoltage() > stopper_voltage) {
                    intake.setPower(1);
                    transfer.setPower(1);
                } else {
                    intake.setPower(0);
                    transfer.setPower(0);
                }
            } else {
                stopper.setPosition(stopper_close);
                if (first.getVoltage() > voltage_first) {
                    transfer.setPower(0);
                } else {
                    transfer.setPower(1);
                }
                intake.setPower(1);
            }

            double vel = motor_up.getVelocity();
            double error = targetVel - vel;

            double feedForward = (kV * targetVel) + kS;
            double proportional = kP * (error);

            double pow = (feedForward + proportional);
            pow = Math.max(-1.0, Math.min(1.0, pow));

            motor_down.setPower(pow);
            motor_up.setPower(pow);

            telemetry.update();
        }
    }
}