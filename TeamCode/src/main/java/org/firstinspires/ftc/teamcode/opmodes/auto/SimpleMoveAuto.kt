package org.firstinspires.ftc.teamcode.opmodes.auto

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.util.ElapsedTime
import org.firstinspires.ftc.teamcode.mechanisms.Shooter
import org.firstinspires.ftc.teamcode.mechanisms.Stopper
import kotlin.math.abs

@Autonomous(name = "Simple Move Auto", group = "Autonomous")
class SimpleMoveAuto : LinearOpMode() {
    companion object {
        private const val DRIVE_POWER = 0.30
        private const val DEFAULT_DRIVE_SECONDS = 1.5
        private const val SHOOTER_RPM = 4000
        private const val RPM_TOLERANCE = 500.0
        private const val READY_SECONDS = 0.25
        private const val SPINUP_TIMEOUT_SECONDS = 5.0
        private const val STOPPER_SETTLE_SECONDS = 0.5
        private const val FEED_SECONDS = 4.0
    }

    private enum class Direction(val forward: Double, val right: Double) {
        FORWARD(1.0, 0.0),
        BACKWARD(-1.0, 0.0),
        LEFT(0.0, -1.0),
        RIGHT(0.0, 1.0)
    }

    override fun runOpMode() {
        val frontLeft = hardwareMap.get(DcMotor::class.java, "frontLeft")
        val frontRight = hardwareMap.get(DcMotor::class.java, "frontRight")
        val backLeft = hardwareMap.get(DcMotor::class.java, "backLeft")
        val backRight = hardwareMap.get(DcMotor::class.java, "backRight")
        val motors = listOf(frontLeft, frontRight, backLeft, backRight)
        val intake = hardwareMap.get(DcMotor::class.java, "intake")
        val shooter = Shooter()
        val stopper = Stopper()

        var direction = Direction.BACKWARD
        var driveSeconds = DEFAULT_DRIVE_SECONDS
        var previousA = false
        var previousB = false

        try {
            motors.forEach {
                it.power = 0.0
                it.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
                it.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            }
            frontLeft.direction = DcMotorSimple.Direction.REVERSE
            backLeft.direction = DcMotorSimple.Direction.REVERSE
            frontRight.direction = DcMotorSimple.Direction.FORWARD
            backRight.direction = DcMotorSimple.Direction.FORWARD
            intake.power = 0.0
            intake.direction = DcMotorSimple.Direction.FORWARD
            intake.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
            intake.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            shooter.initialize(hardwareMap)
            stopper.close()

            while (opModeInInit()) {
                direction = when {
                    gamepad1.dpad_up -> Direction.FORWARD
                    gamepad1.dpad_down -> Direction.BACKWARD
                    gamepad1.dpad_left -> Direction.LEFT
                    gamepad1.dpad_right -> Direction.RIGHT
                    else -> direction
                }
                if (gamepad1.a && !previousA) {
                    driveSeconds = (driveSeconds - 0.25).coerceAtLeast(0.25)
                }
                if (gamepad1.b && !previousB) {
                    driveSeconds = (driveSeconds + 0.25).coerceAtMost(3.0)
                }
                previousA = gamepad1.a
                previousB = gamepad1.b

                telemetry.addLine("Gamepad 1 during INIT: D-pad chooses direction.")
                telemetry.addLine("A: shorter move | B: longer move (0.25 seconds per press)")
                telemetry.addData("Direction (relative to robot)", direction)
                telemetry.addData("Drive time (seconds)", "%.2f", driveSeconds)
                telemetry.addData("Drive power", "%.0f%%", DRIVE_POWER * 100.0)
                telemetry.addLine("Then: shooter 4000 RPM, stopper open, intake feeds for 4 seconds.")
                telemetry.addLine("Choose a clear direction away from the starting wall.")
                telemetry.update()
                idle()
            }

            waitForStart()
            if (isStopRequested) return

            val forward = direction.forward * DRIVE_POWER
            val right = direction.right * DRIVE_POWER
            val timer = ElapsedTime()
            while (opModeIsActive() && timer.seconds() < driveSeconds) {
                frontLeft.power = forward + right
                frontRight.power = forward - right
                backLeft.power = forward - right
                backRight.power = forward + right
                idle()
            }

            motors.forEach { it.power = 0.0 }
            if (!opModeIsActive()) return

            shooter.spinAtRpm(SHOOTER_RPM)
            timer.reset()
            var readySince: Double? = null
            var shooterReady = false
            while (opModeIsActive() && timer.seconds() < SPINUP_TIMEOUT_SECONDS) {
                shooter.update()
                val rpm = shooter.measuredRpm
                val now = timer.seconds()
                if (abs(rpm - SHOOTER_RPM) <= RPM_TOLERANCE) {
                    if (readySince == null) readySince = now
                    if (now - readySince >= READY_SECONDS) {
                        shooterReady = true
                        break
                    }
                } else {
                    readySince = null
                }
                showShooterStatus("Spinning up", rpm)
                idle()
            }
            if (!opModeIsActive()) return
            if (!shooterReady) {
                telemetry.addLine("Shooter did not reach 4000 RPM. Feeding cancelled.")
                telemetry.update()
                return
            }

            stopper.open()
            timer.reset()
            while (opModeIsActive() && timer.seconds() < STOPPER_SETTLE_SECONDS) {
                shooter.update()
                idle()
            }
            if (!opModeIsActive()) return

            intake.power = 0.7
            timer.reset()
            while (opModeIsActive() && timer.seconds() < FEED_SECONDS) {
                shooter.update()
                showShooterStatus("Feeding preloads", shooter.measuredRpm)
                idle()
            }
        } finally {
            motors.forEach { it.power = 0.0 }
            intake.power = 0.0
            shooter.stop()
            stopper.close()
        }

        telemetry.addLine("Auto complete. Motors stopped; stopper closed.")
        telemetry.update()
    }

    private fun showShooterStatus(stage: String, measuredRpm: Double) {
        telemetry.addData("Stage", stage)
        telemetry.addData("Shooter target RPM", SHOOTER_RPM)
        telemetry.addData("Shooter measured RPM", "%.0f", measuredRpm)
        telemetry.update()
    }
}
