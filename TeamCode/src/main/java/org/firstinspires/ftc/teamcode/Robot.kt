package org.firstinspires.ftc.teamcode

import dev.nextftc.robot.Mechanism
import dev.nextftc.robot.NextRobot
import org.firstinspires.ftc.teamcode.mechanisms.Drivetrain
import org.firstinspires.ftc.teamcode.mechanisms.Intake
import org.firstinspires.ftc.teamcode.mechanisms.Shooter

class Robot : NextRobot {
    val drivetrain = Drivetrain()
    val intake = Intake()
    val shooter = Shooter()

    override val mechanisms: Set<Mechanism> = linkedSetOf(drivetrain, intake, shooter)
}
