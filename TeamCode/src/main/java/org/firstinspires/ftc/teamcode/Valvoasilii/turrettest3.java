package org.firstinspires.ftc.teamcode.Valvoasilii;

//import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp
//@Configurable
public class turrettest3 extends LinearOpMode {
    Servo servo_dreapta;
    public static String dreapta = "t_dreapta";
    public static double pos = 0.5;

    @Override
    public void runOpMode() throws InterruptedException {
        servo_dreapta = hardwareMap.get(Servo.class, dreapta);

        waitForStart();

        while (opModeIsActive()) {
            servo_dreapta.setPosition(pos);

            telemetry.addData("Poziție trimisă la servo", pos);
            telemetry.addData("Poziție fizică servo", servo_dreapta.getPosition());
            telemetry.update();

        }
    }
}