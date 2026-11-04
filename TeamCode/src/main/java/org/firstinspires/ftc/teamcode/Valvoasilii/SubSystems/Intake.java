package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

<<<<<<< HEAD
import dev.frozenmilk.dairy.cachinghardware.CachingDcMotor;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Intake {
    Sensors sensors;
    CachingDcMotor transfer,intake;
    CachingServo flower,dropdown;
    public double flower_down,flower_up;
    public double intake_down,intake_up;
    public enum State {
        ForceReverse,
        Stop,
        Shoot,
        Intake,
        Flower
    }

    public State state;
    private void moveFlower(double pos) {flower.setPosition(pos);}
    private void moveIntake(double pos) {dropdown.setPosition(pos);}
    private void setPower(double powIntake,double powTransfer) { intake.setPower(powIntake); transfer.setPower(powTransfer); }

    public void update() {
        switch (state) {
            case ForceReverse:

                moveFlower(flower_up);
                moveIntake(intake_down);
                setPower(-0.5, -0.5);

                break;
            case Stop:

                moveFlower(flower_up);
                moveIntake( Globals.force_drop_intake ? intake_down : intake_up );
                setPower(0,0);

                break;
            case Shoot:

                moveFlower(flower_up);
                moveIntake(intake_down);
                setPower(1,1);

                if(!Globals.start_transfer)
                    state = State.Intake;

                break;
            case Intake:

                moveFlower(flower_up);
                sensors.checkFullTransfer();
                if (Globals.first_ball) setPower(1,0);
                else if (Globals.fourth_ball) {
                    setPower(0, 0);
                    moveIntake( Globals.force_drop_intake ? intake_down : intake_up );
                } else setPower(1,1);

                break;
            case Flower:


                setPower(1,1);

=======
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
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
                break;
        }

        if(Globals.start_transfer)
            state = State.Shoot;
    }

<<<<<<< HEAD
    public Intake(HardwareMap map) {
        intake = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motor_intake));
        transfer = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motor_transfer));
        flower = new CachingServo(map.get(Servo.class, Globals.servoFlower));
        dropdown = new CachingServo(map.get(Servo.class, Globals.servoDropdown));

        transfer.setDirection(DcMotorSimple.Direction.REVERSE);

        state = State.Stop;
=======
    public Intake(HardwareMap hardwareMap) {
        intake   = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, Globals.motor_intake));
        transfer = new CachingDcMotorEx(hardwareMap.get(DcMotorEx.class, Globals.motor_transfer));
        flower   = new CachingServo(hardwareMap.get(Servo.class, "")); // TODO: numele din config
        dropdown = new CachingServo(hardwareMap.get(Servo.class, "")); // TODO: numele din config

        state = State.Intake;
        flower.setPosition(flower_up);
        dropdown.setPosition(dropdown_down);
>>>>>>> e5373d65ab5ddba0c81d89c88aa3dfda32a939a1
    }
}