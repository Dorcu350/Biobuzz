package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotor;

public class Intake {
    CachingDcMotor transfer,intake;
    public enum State {
        ForceReverse,
        ForceStop,
        Stop,
        Shoot,
        Intake
    }

    public State state;

    private void setPower(double powIntake,double powTransfer) { intake.setPower(powIntake); transfer.setPower(powTransfer); }
    private boolean checkIfAllTrue() {
        return Globals.balls[0] && Globals.balls[1] && Globals.balls[2] && Globals.balls[3];
    }

    public void update() {
        switch (state) {
            case ForceReverse:
                setPower(-0.5, -1);
                break;
            case ForceStop:
                setPower(0,0);
                break;
            case Stop:
                setPower(0,0);
                break;
            case Shoot:
                setPower(1,1);
                break;
            case Intake:
                if (checkIfAllTrue()) setPower(0,0);
                else {
                    if (Globals.balls[3] == false) {
                        setPower(1,0);
                    } else setPower(1, 1);
                }
                break;
        }
    }

    public Intake(HardwareMap map) {
        intake = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[0]));
        transfer = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[1]));
    }
}
