package com.tarvo.kedlin.yardgoat.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tarvo.kedlin.yardgoat.ui.theme.GameFonts
import com.tarvo.kedlin.yardgoat.ui.theme.Palette

@Composable
fun drawableId(name: String): Int {
    val ctx = LocalContext.current
    return remember(name) { ctx.resources.getIdentifier(name, "drawable", ctx.packageName) }
}

@Composable
fun Art(name: String, modifier: Modifier = Modifier, alpha: Float = 1f) {
    val id = drawableId(name)
    if (id != 0) {
        Image(painterResource(id), null, modifier.alpha(alpha), contentScale = ContentScale.Fit)
    }
}

@Composable
fun Backdrop(name: String, dim: Float = 0.55f, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Palette.Ink)) {
        val id = drawableId(name)
        if (id != 0) {
            Image(
                painterResource(id),
                null,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        Palette.Ink.copy(alpha = dim + 0.2f),
                        Palette.Ink.copy(alpha = dim),
                        Palette.Ink.copy(alpha = dim + 0.3f)
                    )
                )
            )
        )
        Box(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing)) { content() }
    }
}

@Composable
fun PlatePanel(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    plate: String = "btn_plate_steel",
    textColor: Color = Palette.Cream,
    enabled: Boolean = true,
    fontSize: Int = 18
) {
    val id = drawableId(plate)
    val alpha = if (enabled) 1f else 0.45f
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (id != 0) {
            Image(
                painterResource(id),
                null,
                Modifier.fillMaxSize().scale(1.1f),
                contentScale = ContentScale.Crop,
                alpha = alpha
            )
        } else {
            Box(Modifier.fillMaxSize().background(Palette.Steel.copy(alpha = alpha)))
        }
        Text(
            text,
            color = textColor.copy(alpha = alpha),
            fontFamily = GameFonts.primary,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            letterSpacing = 1.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun IconPlate(icon: String, label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Panel.copy(alpha = 0.9f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Art(icon, Modifier.size(30.dp))
            Text(
                label,
                color = Palette.Cream,
                fontFamily = GameFonts.primary,
                fontSize = 11.sp,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}

@Composable
fun StarRow(stars: Int, size: Int = 14, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0 until 3) {
            Art(
                "ic_star",
                Modifier.size(size.dp).padding(end = 2.dp),
                alpha = if (i < stars) 1f else 0.22f
            )
        }
    }
}

@Composable
fun StarBadge(stars: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Palette.Ink.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Art("ic_star", Modifier.size(16.dp))
        Text(
            "  $stars / $total",
            color = Palette.Gold,
            fontFamily = GameFonts.hud,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun Divider(width: Int = 60, modifier: Modifier = Modifier) {
    Box(
        modifier
            .width(width.dp)
            .background(Palette.Amber)
    )
}
