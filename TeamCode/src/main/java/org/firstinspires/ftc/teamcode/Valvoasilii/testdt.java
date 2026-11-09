package org.firstinspires.ftc.teamcode.Valvoasilii;

//import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotorEx;

//@Config
@TeleOp(name = "Test DT", group = "Test")
public class testdt extends LinearOpMode {
    DcMotorEx fl, fr, bl, br;
    public static String motor_fl = "motor_fl", motor_bl = "motor_bl", motor_fr = "motor_fr", motor_br = "motor_br";

    @Override
    public void runOpMode() throws InterruptedException {
        bl = hardwareMap.get(DcMotorEx.class, motor_bl);
        fl = hardwareMap.get(DcMotorEx.class, motor_fl);
        fr = hardwareMap.get(DcMotorEx.class, motor_fr);
        br = hardwareMap.get(DcMotorEx.class, motor_br);

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.x) {
                bl.setPower(0.2);
                fl.setPower(0.2);
                fr.setPower(0.2);
                br.setPower(0.2);
            }
            if (gamepad1.triangle) {
                bl.setPower(0);
                fl.setPower(0);
                fr.setPower(0);
                br.setPower(0);
            }
            if (gamepad1.dpad_up) {
                fl.setPower(0.2);
            }
            if (gamepad1.dpad_left) {
                fr.setPower(0.2);
            }
            if (gamepad1.dpad_down) {
                br.setPower(0.2);
            }
            if (gamepad1.dpad_right) {
                bl.setPower(0.2);
            }

            // Telemetria standard va fi interceptată automat de Panels
            telemetry.addData("FL Power", fl.getPower());
            telemetry.update();
        }
    }
}