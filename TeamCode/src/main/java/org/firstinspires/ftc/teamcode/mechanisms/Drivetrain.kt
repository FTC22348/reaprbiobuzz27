package org.firstinspires.ftc.teamcode.mechanisms

import com.qualcomm.robotcore.hardware.Gamepad
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism
import dev.nextftc.robot.drive.mecanumDrive
import dev.nextftc.robot.drive.scalar

class Drivetrain : Mechanism {
    private val frontLeft = NextMotor("frontLeft")
    private val frontRight = NextMotor("frontRight")
    private val backLeft = NextMotor("backLeft")
    private val backRight = NextMotor("backRight")
    private val motors = arrayOf(frontLeft, frontRight, backLeft, backRight)

    init {
        // Adjust these directions if your motor mounting requires it.
        frontLeft.direction = NextMotor.Direction.REVERSE
        backLeft.direction = NextMotor.Direction.REVERSE
        frontRight.direction = NextMotor.Direction.FORWARD
        backRight.direction = NextMotor.Direction.FORWARD

        for (motor in motors) {
            motor.zeroPowerBehavior = NextMotor.ZeroPowerBehavior.BRAKE
        }
    }

    fun startDrive(gamepad: Gamepad) {
        scalar = 1.0
        mecanumDrive(frontLeft, frontRight, backLeft, backRight, gamepad).schedule()
    }

    fun stop() {
        for (motor in motors) {
            motor.throttle = 0.0
            motor.update()
        }
    }
}
