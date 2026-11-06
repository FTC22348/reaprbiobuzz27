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

        val operator = CommandGamepad(gamepad2)
        operator.rightTrigger.isOver(0.1).whileTrue(robot.intake.run())
        operator.a.toggleOnTrue(robot.shooter.run())
    }

    override fun end() {
        robot.drivetrain.stop()
        robot.intake.stop()
        robot.shooter.stop()
    }
}
