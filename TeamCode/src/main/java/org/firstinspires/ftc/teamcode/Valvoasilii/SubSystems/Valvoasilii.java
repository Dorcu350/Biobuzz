package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

/** Direct field-centric mecanum drivetrain. */
public class Valvoasilii {
    private final DcMotorEx frontLeft;
    private final DcMotorEx frontRight;
    private final DcMotorEx backLeft;
    private final DcMotorEx backRight;

    public Valvoasilii(HardwareMap hardwareMap) {
        frontLeft = hardwareMap.get(
                DcMotorEx.class,
                Constants.drivetrainConfig.frontLeftName.get()
        );
        frontRight = hardwareMap.get(
                DcMotorEx.class,
                Constants.drivetrainConfig.frontRightName.get()
        );
        backLeft = hardwareMap.get(
                DcMotorEx.class,
                Constants.drivetrainConfig.backLeftName.get()
        );
        backRight = hardwareMap.get(
                DcMotorEx.class,
                Constants.drivetrainConfig.backRightName.get()
        );

        frontLeft.setDirection(Constants.drivetrainConfig.frontLeftDirection.get());
        frontRight.setDirection(Constants.drivetrainConfig.frontRightDirection.get());
        backLeft.setDirection(Constants.drivetrainConfig.backLeftDirection.get());
        backRight.setDirection(Constants.drivetrainConfig.backRightDirection.get());

        DcMotor.ZeroPowerBehavior zeroPowerBehavior =
                Constants.drivetrainConfig.manualBrakeMode.get()
                        ? DcMotor.ZeroPowerBehavior.BRAKE
                        : DcMotor.ZeroPowerBehavior.FLOAT;
        frontLeft.setZeroPowerBehavior(zeroPowerBehavior);
        frontRight.setZeroPowerBehavior(zeroPowerBehavior);
        backLeft.setZeroPowerBehavior(zeroPowerBehavior);
        backRight.setZeroPowerBehavior(zeroPowerBehavior);
    }

    public void driveFieldCentric(double leftStickY, double leftStickX, double rightStickX, double headingRad) {
        double forwardField = leftStickY;
        double strafeField = leftStickX;
        double turn = rightStickX;

        double angle = -headingRad;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double forward = forwardField * cos - strafeField * sin;
        double strafe = forwardField * sin + strafeField * cos;

        double fl = forward - strafe - turn;
        double fr = forward + strafe + turn;
        double bl = forward + strafe - turn;
        double br = forward - strafe + turn;

        double maxPower = Math.max(
                1.0,
                Math.max(
                        Math.max(Math.abs(fl), Math.abs(fr)),
                        Math.max(Math.abs(bl), Math.abs(br))
                )
        );

        frontLeft.setPower(fl / maxPower);
        frontRight.setPower(fr / maxPower);
        backLeft.setPower(bl / maxPower);
        backRight.setPower(br / maxPower);
    }

    public void stop() {
        frontLeft.setPower(0.0);
        frontRight.setPower(0.0);
        backLeft.setPower(0.0);
        backRight.setPower(0.0);
    }
}
