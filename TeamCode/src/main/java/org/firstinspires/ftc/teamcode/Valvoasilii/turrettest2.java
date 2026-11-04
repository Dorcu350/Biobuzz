package org.firstinspires.ftc.teamcode.Valvoasilii;

//import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
//@Configurable
public class turrettest2 extends LinearOpMode {
    Servo servo_stanga;
    public static String stanga = "t_stanga";
    public static double pos = 0.5;

    @Override
    public void runOpMode() throws InterruptedException {
        servo_stanga = hardwareMap.get(Servo.class, stanga);

        waitForStart();

        while (opModeIsActive()) {
            servo_stanga.setPosition(pos);

            telemetry.addData("Poziție trimisă la servo stânga", pos);
            telemetry.addData("Poziție fizică servo stânga", servo_stanga.getPosition());
            telemetry.update();

            sleep(20);
        }
    }
}