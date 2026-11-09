package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.bylazar.configurables.annotations.Configurable;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotorEx;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

@Configurable
public class Intake {
    Sensors sensors;
    CachingDcMotorEx transfer, intake;
    CachingServo flower, dropdown;
    public static double flower_up, flower_down;
    public static double dropdown_up = 0.5, dropdown_down = 0.4;

    public enum State {
        Intake,
        Transfer,
        Stopped,
        Reverse
    }
    public static State state;

    public void update() {
        sensors.checkFullTransfer();
        switch (state) {
            case Intake:
                flower.setPosition( (Globals.force_flower) ? flower_down : flower_up );
                if (Globals.fourth_ball) {
                    intake.setPower(0);
                    dropdown.setPosition(dropdown_up);
                } else {
                    intake.setPower(1);
                    dropdown.setPosition(dropdown_down);
                }

                if (Globals.second_ball) transfer.setPower(0);
                else transfer.setPower(1);

                break;
            case Stopped:
                intake.setPower(0);
                transfer.setPower(0);
                flower.setPosition( (Globals.force_flower) ? flower_down : flower_up );

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

//        if(Globals.start_transfer)
//            state = State.Transfer;
    }
    public boolean is_intaking() {
        return intake.getPower() != 0;
    }
    public Intake(HardwareMap hardwareMap) {
        intake   = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, "intake"));
        transfer = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, "transfer"));
        flower   = new CachingServo(hardwareMap.get(Servo.class, "flower")); // TODO: numele din config
        dropdown = new CachingServo(hardwareMap.get(Servo.class, "dropdown")); // TODO: numele din config

        sensors = new Sensors(hardwareMap);

        transfer.setCachingTolerance(0.05);
        transfer.setDirection(DcMotorSimple.Direction.REVERSE);
//        intake.setDirection(DcMotorSimple.Direction.REVERSE);


        state = State.Intake;
        sensors.resetTransfer();
        flower.setPosition(flower_up);
        dropdown.setPosition(dropdown_down);
    }
}