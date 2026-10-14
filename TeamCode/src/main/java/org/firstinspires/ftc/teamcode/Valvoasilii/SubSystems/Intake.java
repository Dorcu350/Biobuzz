package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Intake {
    CachingDcMotorEx transfer, intake;
    CachingServo flower, dropdown;
    public static double flower_up, flower_down;
    public static double dropdown_up, dropdown_down;

    public enum State {
        Intake,
        Transfer,
        Flower,
        Reverse,
        ForceReverse
    }
    public static State state;

    public void update() {
        switch (state) {
            case Intake:
                flower.setPosition(flower_up);
                if (Globals.fourthBall){
                    intake.setPower(0);
                    dropdown.setPosition(dropdown_up);
                } else {
                    intake.setPower(1);
                    dropdown.setPosition(dropdown_down);
                }

                if (Globals.firstBall) transfer.setPower(0);
                else transfer.setPower(1);

                break;
            case Flower:
                flower.setPosition(flower_down);
                if (Globals.fourthBall){
                    intake.setPower(0);
                    dropdown.setPosition(dropdown_up);
                } else {
                    intake.setPower(1);
                    dropdown.setPosition(dropdown_down);
                }

                if (Globals.firstBall) transfer.setPower(0);
                else transfer.setPower(1);

                break;
            case Transfer:
                intake.setPower(1);
                transfer.setPower(1);
                flower.setPosition( (Globals.force_flower) ? flower_down : flower_up );
                dropdown.setPosition(dropdown_down);

                break;
            case Reverse:
                intake.setPower(-0.4);
                transfer.setPower(-0.4);
                flower.setPosition(flower_up);
                dropdown.setPosition(dropdown_down);
                break;
        }
    }

    public Intake(HardwareMap hardwareMap) {
        intake   = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, Globals.motor_intake));
        transfer = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, Globals.motor_transfer));
        flower   = new CachingServo(hardwareMap.get(Servo.class, "")); // TODO: numele din config
        dropdown = new CachingServo(hardwareMap.get(Servo.class, "")); // TODO: numele din config

        state = State.Intake;
        flower.setPosition(flower_up);
        dropdown.setPosition(dropdown_down);
    }
}