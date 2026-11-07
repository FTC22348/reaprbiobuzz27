package org.firstinspires.ftc.teamcode.opmodes.teleop

import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.robot.triggers.CommandGamepad
import org.firstinspires.ftc.teamcode.Robot

@NextTeleop(name = "Basic TeleOp")
class BasicTeleOp(private val robot: Robot) : NextOpMode(robot) {
    init {
        // Resolve the motors and start every mechanism at zero power during INIT.
        robot.drivetrain.stop()
        robot.intake.stop()
        robot.shooter.stop()
    }

    override fun start() {
        robot.drivetrain.startDrive(gamepad1)

        robot.intake.run {
            // Reverse takes priority if both triggers are held.
            when {
                gamepad2.left_trigger > 0.1 -> -1.0
                gamepad2.right_trigger > 0.1 -> 1.0
                else -> 0.0
            }
        }.schedule()

        val operator = CommandGamepad(gamepad2)
        operator.a.toggleOnTrue(robot.shooter.run())
    }

    override fun end() {
        robot.drivetrain.stop()
        robot.intake.stop()
        robot.shooter.stop()
    }
}
