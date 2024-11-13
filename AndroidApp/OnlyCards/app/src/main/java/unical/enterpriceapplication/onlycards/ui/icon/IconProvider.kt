package unical.enterpriceapplication.onlycards.ui.icon

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import unical.enterpriceapplication.onlycards.R

val fontAwesome = FontFamily( Font(R.font.fa_solid_900) )
val fontAwesomeBrand = FontFamily( Font(R.font.fa_brands_400) )
val fontAwesomeRegular = FontFamily( Font(R.font.fa_regular_400) )

@Composable
fun FontAwesomeIcon(unicode: String, fontSize: TextUnit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(5.dp)) {
        BasicText(
            text = unicode,
            style = TextStyle(
                fontFamily = fontAwesome,
                fontSize = fontSize,
                color = if(isSystemInDarkTheme()) MaterialTheme.colors.onSurface else MaterialTheme.colors.onPrimary
            )
        )
    }
}

@Composable
fun FontAwesomeIconColor(unicode: String, fontSize: TextUnit, modifier: Modifier = Modifier, color: Color) {
    Box(modifier = modifier.padding(5.dp)) {
        BasicText(
            text = unicode,
            style = TextStyle(
                fontFamily = fontAwesome,
                fontSize = fontSize,
                color = color
            )
        )
    }
}

@Composable
fun FontAwesomeBrandIcon(unicode: String, fontSize: TextUnit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(5.dp)) {
        BasicText(
            text = unicode,
            style = TextStyle(
                fontFamily = fontAwesomeBrand,
                fontSize = fontSize,
                color = if(isSystemInDarkTheme()) MaterialTheme.colors.onSurface else MaterialTheme.colors.onPrimary
            )
        )
    }
}

@Composable
fun FontAwesomeRegularIcon(unicode: String, fontSize: TextUnit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(5.dp)) {
        BasicText(
            text = unicode,
            style = TextStyle(
                fontFamily = fontAwesomeRegular,
                fontSize = fontSize,
                color = if(isSystemInDarkTheme()) MaterialTheme.colors.onSurface else MaterialTheme.colors.onPrimary
            )
        )
    }
}
