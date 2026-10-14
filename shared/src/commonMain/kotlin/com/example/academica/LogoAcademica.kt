package com.example.academica

import academica.shared.generated.resources.Res
import academica.shared.generated.resources.logo_academica
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource

@Composable
fun LogoAcademica(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    showText: Boolean = true
) {
    val darkColor = Color(0xFF1F1B18)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.logo_academica),
            contentDescription = "Logo Académica",
            modifier = Modifier.size(size)
        )

        if (showText) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ACADÉMICA",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = darkColor,
                letterSpacing = 2.sp
            )
        }
    }
}
