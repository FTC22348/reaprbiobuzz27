package org.firstinspires.ftc.teamcode.opmodes.teleop

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Servo

// Use the SDK directly so testing one servo does not initialize the drivetrain.
@TeleOp(name = "Test Servo", group = "Tests")
class TestServoTeleOp : OpMode() {
    private lateinit var test: Servo

    override fun init() {
        test = hardwareMap.get(Servo::class.java, "test")
        test.direction = Servo.Direction.FORWARD
        telemetry.addLine("Gamepad 1: A = position 0, B = position 1.")
    }

    override fun loop() {
        if (gamepad1.a) {
            test.position = 0.0
        } else if (gamepad1.b) {
            test.position = 1.0
        }

        telemetry.addData("Test servo target", "%.2f", test.position)
    }
}
