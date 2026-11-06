package org.firstinspires.ftc.teamcode.opmodes.teleop

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorSimple

// Use the SDK directly so testing one motor does not initialize the drivetrain.
@TeleOp(name = "Test Motor", group = "Tests")
class TestMotorTeleOp : OpMode() {
    private lateinit var test: DcMotor

    override fun init() {
        test = hardwareMap.get(DcMotor::class.java, "test")
        test.power = 0.0
        test.direction = DcMotorSimple.Direction.FORWARD
        test.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        test.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        telemetry.addLine("Gamepad 1: left stick up/down runs the test motor.")
    }

    override fun loop() {
        val power = -gamepad1.left_stick_y.toDouble()
        test.power = power
        telemetry.addData("Test motor power", "%.2f", power)
    }

    override fun stop() {
        if (::test.isInitialized) {
            test.power = 0.0
        }
    }
}
