package org.firstinspires.ftc.teamcode.opmodes.teleop

import dev.nextftc.robot.opmode.NextOpMode
import dev.nextftc.robot.opmode.NextTeleop
import dev.nextftc.robot.triggers.CommandGamepad
import org.firstinspires.ftc.teamcode.Robot

@NextTeleop(name = "Basic TeleOp")
class BasicTeleOp(private val robot: Robot) : NextOpMode(robot) {
    init {
        // Initialize the motors and close the stopper during INIT.
        robot.drivetrain.stop()
        robot.intake.stop()
        robot.shooter.initialize(hardwareMap)
        robot.stopper.close()
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
        operator.b.onTrue(robot.stopper.toggle())
        operator.a.onTrue(robot.shooter.toggle())
        operator.x.onTrue(robot.shooter.speedUp())
        operator.y.onTrue(robot.shooter.speedDown())
    }

    override fun periodic() {
        robot.shooter.update()
        telemetry.addData("Shooter", if (robot.shooter.isRunning) "ON" else "OFF")
        telemetry.addData("Selected shooter RPM", robot.shooter.selectedRpm)
        telemetry.addData("Measured shooter RPM", "%.0f", robot.shooter.measuredRpm)
        telemetry.addData("Shooter power", "%.0f%%", robot.shooter.commandedPower * 100.0)
        telemetry.update()
    }

    override fun end() {
        robot.drivetrain.stop()
        robot.intake.stop()
        robot.shooter.stop()
    }
}
