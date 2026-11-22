package org.firstinspires.ftc.teamcode.mechanisms

import com.pedropathing.ivy.Command
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.hardware.actuators.NextServo
import dev.nextftc.robot.Mechanism

class Stopper : Mechanism {
    private val servo = NextServo("stopper", direction = NextMotor.Direction.REVERSE)

    fun close() {
        servo.position = 1.0
    }

    fun open() {
        servo.position = 0.0
    }

    fun toggle(): Command = instant {
        servo.position = 1.0 - servo.position
    }
}
