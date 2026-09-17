package org.firstinspires.ftc.teamcode.Valvoasilii.SubSystems;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.teamcode.Valvoasilii.Utils.Globals;

import dev.frozenmilk.dairy.cachinghardware.CachingDcMotor;

public class Intake {
    CachingDcMotor transfer,intake;
    public static double[] reversePower = {-0.3, -0.2};
    public static double[] shootPower = {1,1};
    public static double[] intakePower = {1,1};
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
        for(boolean x : Globals.balls) if ( x == false ) return false;
        return true;
    }

    public void update() {
        switch (state) {
            case ForceReverse:
                setPower(reversePower[0], reversePower[1]);
                break;
            case ForceStop:
                break;
            case Stop:
                setPower(0,0);
                break;
            case Shoot:
                setPower(shootPower[0],shootPower[1]);
                break;
            case Intake:
                if (checkIfAllTrue()) setPower(0,0);
                else {
                    if (Globals.balls[3] == false) {
                        setPower(intakePower[0],0);
                    } else setPower(intakePower[0], intakePower[1]);
                }
                break;
        }
    }

    public Intake(HardwareMap map) {
        intake = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[0]));
        transfer = new CachingDcMotor(map.get(DcMotorEx.class, Globals.motorsIntake[1]));
    }
}
