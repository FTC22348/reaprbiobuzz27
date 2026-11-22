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
        // Positive shooting power produces negative encoder counts on this robot.
        private const val ENCODER_SIGN = -1.0
        // Starting values for smooth speed holding; output is motor power, error is RPM.
        private const val NOMINAL_FREE_RPM = 6000.0
        private const val SPEED_KP = 0.00015
        private const val SPEED_KI = 0.00015
        private const val MAX_INTEGRAL_POWER = 0.4
        private const val MIN_RPM = 3000
        private const val MAX_RPM = 6000
        private const val RPM_STEP = 1000
    }

    private lateinit var motor: DcMotorEx
    private var outputPower = 0.0
    private var integralPower = 0.0
    private var lastUpdateNanos = 0L
    private var controlRpm = MIN_RPM

    val commandedPower: Double
        get() = outputPower

    fun initialize(hardwareMap: HardwareMap) {
        motor = hardwareMap.get(DcMotorEx::class.java, "shoota").apply {
            power = 0.0
            direction = DcMotorSimple.Direction.FORWARD
            zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT
            // Use software feedback so the inverted encoder does not drive the hub PID away from its target.
            mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            power = 0.0
        }
        outputPower = 0.0
        isRunning = false
        resetController()
    }

    var selectedRpm = MIN_RPM
        private set

    var isRunning = false
        private set

    val measuredRpm: Double
        get() = ENCODER_SIGN * motor.velocity * 60.0 / TICKS_PER_REVOLUTION

    // Selection changes must not require this mechanism and interrupt the running command.
    fun speedUp(): Command = Commands.instant {
        selectedRpm = (selectedRpm + RPM_STEP).coerceAtMost(MAX_RPM)
    }

    fun speedDown(): Command = Commands.instant {
        selectedRpm = (selectedRpm - RPM_STEP).coerceAtLeast(MIN_RPM)
    }

    fun spinAtRpm(rpm: Int) {
        require(rpm in MIN_RPM..MAX_RPM) { "Shooter RPM must be between $MIN_RPM and $MAX_RPM" }
        selectedRpm = rpm
        if (!isRunning) resetController()
        isRunning = true
        update()
    }

    fun toggle(): Command = Commands.instant {
        if (isRunning) stop() else spinAtRpm(selectedRpm)
    }

    fun update() {
        if (!isRunning) return
        if (controlRpm != selectedRpm) resetController()

        val now = System.nanoTime()
        val dt = if (lastUpdateNanos == 0L) 0.0 else
            ((now - lastUpdateNanos) / 1e9).coerceIn(0.0, 0.1)
        lastUpdateNanos = now

        val error = selectedRpm - measuredRpm
        val basePower = selectedRpm / NOMINAL_FREE_RPM + SPEED_KP * error
        val candidateIntegral = (integralPower + SPEED_KI * error * dt)
            .coerceIn(-MAX_INTEGRAL_POWER, MAX_INTEGRAL_POWER)
        val candidatePower = basePower + candidateIntegral
        // Avoid accumulating more correction when the motor is already saturated.
        if (candidatePower in 0.0..1.0 ||
            (candidatePower > 1.0 && error < 0.0) ||
            (candidatePower < 0.0 && error > 0.0)
        ) {
            integralPower = candidateIntegral
        }
        outputPower = (basePower + integralPower).coerceIn(0.0, 1.0)
        motor.power = outputPower
    }

    fun run(): Command = infinite { spinAtRpm(selectedRpm) }
        .setStart { isRunning = true }
        .setEnd { stop() }

    fun stop() {
        outputPower = 0.0
        isRunning = false
        resetController()
        if (::motor.isInitialized) {
            motor.power = 0.0
        }
    }

    private fun resetController() {
        integralPower = 0.0
        lastUpdateNanos = 0L
        controlRpm = selectedRpm
    }
}
