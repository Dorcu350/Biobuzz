//package org.firstinspires.ftc.teamcode.Valvoasilii;
//
//import com.acmerobotics.dashboard.config.Config;
//import com.bylazar.configurables.annotations.Configurable;
//import com.bylazar.telemetry.PanelsTelemetry;
//import com.bylazar.telemetry.TelemetryManager;
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//import com.qualcomm.robotcore.hardware.DcMotorEx;
//
//import org.firstinspires.ftc.robotcore.external.Telemetry;
//
//@Config
//@Configurable
//@TeleOp(name = "Test DT", group = "Test")
//public class testdt extends LinearOpMode {
//    DcMotorEx fl, fr, bl, br;
//    public Telemetry telemetryM;
//
//    public static String motor_fl = "motor_fl", motor_bl = "motor_bl", motor_fr = "motor_fr", motor_br = "motor_br";
//
//    @Override
//    public void runOpMode() throws InterruptedException {
//        // Obținere instanță de telemetrie Panels
//        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();
//
//        bl = hardwareMap.get(DcMotorEx.class, motor_bl);
//        fl = hardwareMap.get(DcMotorEx.class, motor_fl);
//        fr = hardwareMap.get(DcMotorEx.class, motor_fr);
//        br = hardwareMap.get(DcMotorEx.class, motor_br);
//
//        waitForStart();
//
//        while (opModeIsActive()) {
//            if (gamepad1.x) {
//                bl.setPower(0.2);
//                fl.setPower(0.2);
//                fr.setPower(0.2);
//                br.setPower(0.2);
//            }
//            if (gamepad1.triangle) {
//                bl.setPower(0);
//                fl.setPower(0);
//                fr.setPower(0);
//                br.setPower(0);
//            }
//            if (gamepad1.dpad_up) {
//                fl.setPower(0.2);
//            }
//            if (gamepad1.dpad_left) {
//                fr.setPower(0.2);
//            }
//            if (gamepad1.dpad_down) {
//                br.setPower(0.2);
//            }
//            if (gamepad1.dpad_right) {
//                bl.setPower(0.2);
//            }
//
//            // Trimitere date către Panels și Driver Station
//            if (telemetryM != null) {
//                telemetryM.addData("FL Power", fl.getPower());
//                telemetryM.addData("FR Power", fr.getPower());
//                telemetryM.addData("BL Power", bl.getPower());
//                telemetryM.addData("BR Power", br.getPower());
//                telemetryM.update();
//            }
//        }
//    }
//}