package org.firstinspires.ftc.teamcode.mechanisms

import com.pedropathing.ivy.Command
import com.pedropathing.ivy.commands.Commands
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import dev.nextftc.robot.Mechanism

class Shooter : Mechanism {
    companion object {
        // goBILDA 5203-2402-0001: 6000 RPM, 1:1 gearbox, 28 encoder counts/revolution.
        private const val TICKS_PER_REVOLUTION = 28.0
        private const val MIN_RPM = 4000
        private const val MAX_RPM = 6000
        private const val RPM_STEP = 1000
    }

    private lateinit var motor: DcMotorEx

    fun initialize(hardwareMap: HardwareMap) {
        motor = hardwareMap.get(DcMotorEx::class.java, "shoota").apply {
            power = 0.0
            direction = DcMotorSimple.Direction.FORWARD
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            mode = DcMotor.RunMode.RUN_USING_ENCODER
            velocity = 0.0
        }
        isRunning = false
    }

    var selectedRpm = MAX_RPM
        private set

    var isRunning = false
        private set

    val measuredRpm: Double
        get() = motor.velocity * 60.0 / TICKS_PER_REVOLUTION

    // Selection changes must not require this mechanism and interrupt the running command.
    fun speedUp(): Command = Commands.instant {
        selectedRpm = (selectedRpm + RPM_STEP).coerceAtMost(MAX_RPM)
    }

    fun speedDown(): Command = Commands.instant {
        selectedRpm = (selectedRpm - RPM_STEP).coerceAtLeast(MIN_RPM)
    }

    fun run(): Command = infinite {
        motor.velocity = selectedRpm * TICKS_PER_REVOLUTION / 60.0
    }
        .setStart { isRunning = true }
        .setEnd { stop() }

    fun stop() {
        // Use velocity directly: the SDK power cache can otherwise skip a zero-power write.
        if (::motor.isInitialized) {
            motor.velocity = 0.0
        }
        isRunning = false
    }
}
