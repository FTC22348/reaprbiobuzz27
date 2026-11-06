package org.firstinspires.ftc.teamcode.mechanisms

import com.pedropathing.ivy.Command
import dev.nextftc.hardware.actuators.NextMotor
import dev.nextftc.robot.Mechanism

class Intake : Mechanism {
    private val motor = NextMotor("intake")

    init {
        motor.direction = NextMotor.Direction.FORWARD
        motor.zeroPowerBehavior = NextMotor.ZeroPowerBehavior.BRAKE
    }

    fun run(): Command = infinite { motor.throttle = 1.0 }
        .setEnd { motor.throttle = 0.0 }

    fun stop() {
        motor.throttle = 0.0
        motor.update()
    }
}
