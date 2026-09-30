package ru.alexey.flowapp.core.designsystem.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.Laptop
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.SelfImprovement
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.ui.graphics.vector.ImageVector
import ru.alexey.flowapp.core.model.FlowIconKey

/** Maps domain icon keys to Material vectors */
object FlowIcons {
    val Check: ImageVector = Icons.Filled.Check

    /** Unknown keys fall back to the default icon */
    fun byKey(key: String?): ImageVector =
        when (key) {
            FlowIconKey.WORKOUT -> Icons.Outlined.FitnessCenter
            FlowIconKey.READ -> Icons.AutoMirrored.Outlined.MenuBook
            FlowIconKey.CODE -> Icons.Outlined.Laptop
            FlowIconKey.MEDITATE -> Icons.Outlined.SelfImprovement
            FlowIconKey.HEART -> Icons.Outlined.Favorite
            FlowIconKey.STAR -> Icons.Outlined.Star
            FlowIconKey.WATER -> Icons.Outlined.LocalDrink
            FlowIconKey.WALK -> Icons.AutoMirrored.Filled.DirectionsWalk
            FlowIconKey.WORK -> Icons.Outlined.Work
            FlowIconKey.LEARNING -> Icons.Outlined.School
            FlowIconKey.PERSONAL -> Icons.Outlined.Person
            FlowIconKey.HEALTH -> Icons.Outlined.WorkspacePremium
            else -> Icons.Outlined.Bolt
        }
}