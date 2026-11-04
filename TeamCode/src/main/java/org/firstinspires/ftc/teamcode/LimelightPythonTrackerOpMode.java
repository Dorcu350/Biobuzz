package org.firstinspires.ftc.teamcode;

//import com.qualcomm.hardware.limelight.LLResult;
//import com.qualcomm.hardware.limelight.Limelight3A;
//import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Limelight Python Ball Tracker", group = "Vision")
public class LimelightPythonTrackerOpMode extends LinearOpMode {

    private Limelight3A limelight;

    @Override
    public void runOpMode() {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.setPollRateHz(100);
        limelight.start();

        // Switch la Pipeline-ul unde ai pus scriptul Python (ex. index 2)
        limelight.pipelineSwitch(2);

        telemetry.addData("Status", "Limelight Python Initializat");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result != null && result.isValid()) {
                double[] pyOutput = result.getPythonOutput();

                // Verificăm dacă vectorul conține date și dacă bila a fost detectată (status == 1.0)
                if (pyOutput != null && pyOutput.length >= 6 && pyOutput[5] == 1.0) {
                    double xRobot = pyOutput[0]; // metri stânga/dreapta față de centrul robotului
                    double yRobot = pyOutput[1]; // metri în fața centrului robotului
                    double distance = pyOutput[2]; // distanța directă în metri
                    double tx = pyOutput[3];
                    double ty = pyOutput[4];

                    telemetry.addData("Bila Detectata", "DA");
                    telemetry.addData("X Robot (stanga/dreapta)", "%.2f m", xRobot);
                    telemetry.addData("Y Robot (in fata)", "%.2f m", yRobot);
                    telemetry.addData("Distanta Totala", "%.2f m", distance);
                    telemetry.addData("Offset Unghi (tx)", "%.1f deg", tx);
                } else {
                    telemetry.addData("Bila Detectata", "NU (Sau sub threshold)");
                }
            } else {
                telemetry.addData("Status", "Niciun rezultat valid de la Limelight");
            }

            telemetry.update();
        }

        limelight.stop();
    }
}