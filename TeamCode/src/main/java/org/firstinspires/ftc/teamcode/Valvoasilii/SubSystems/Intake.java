package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotor;
import dev.frozenmilk.dairy.cachinghardware.CachingServo;

public class Intake {
    Sensors sensors;
    CachingDcMotor transfer,intake;
    CachingServo arms;
    public double arm_flower, arm_open;
    public enum State {
        ForceReverse,
        Stop,
        Shoot,
        Intake,
        Flower
    }

    public State state;
    private void moveArms(double pos) {arms.setPosition(pos);}
    private void setPower(double powIntake,double powTransfer) { intake.setPower(powIntake); transfer.setPower(powTransfer); }

    public void update() {
        switch (state) {
            case ForceReverse:
                moveArms(arm_open);
                setPower(-0.5, -1);

                if(Globals.start_transfer)
                    state = State.Shoot;
                break;
            case Stop:
                moveArms(arm_open);
                setPower(0,0);

                if(Globals.start_transfer)
                    state = State.Shoot;
                break;
            case Shoot:
                moveArms(arm_open);
                setPower(1,1);

                if(!Globals.start_transfer)
                    state = State.Intake;
                break;
            case Intake:
                moveArms(arm_open);
                sensors.checkFullTransfer();

                if(Globals.balls[3]) setPower(0,0);
                else setPower(1,1);

                if(Globals.start_transfer)
                    state = State.Shoot;

                break;
            case Flower:
                moveArms(arm_flower);
                setPower(1,1);

                if(Globals.start_transfer)
                    state = State.Shoot;
                break;
        }
    }

    public Intake(HardwareMap map) {
        intake = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[0]));
        transfer = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[1]));
        arms = new CachingServo(map.get(Servo.class, Globals.servoArms));

        state = State.Stop;
    }
}
