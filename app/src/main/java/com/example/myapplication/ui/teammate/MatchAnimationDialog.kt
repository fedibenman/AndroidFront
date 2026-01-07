package com.example.myapplication.ui.teammate

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.DTOs.Profile
import com.example.myapplication.ui.components.PixelatedButton
import com.example.myapplication.ui.components.PixelatedCard
import com.example.myapplication.ui.theme.PressStart
import com.example.myapplication.ui.theme.PrimaryGold

@Composable
fun MatchAnimationDialog(
    matchedUser: Profile,
    onDismiss: () -> Unit,
    onSendMessage: () -> Unit,
    onKeepSwiping: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "IT'S A MATCH!",
                    fontFamily = PressStart,
                    fontSize = 20.sp,
                    color = PrimaryGold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "You and ${matchedUser.name ?: "someone"} matched!",
                    fontFamily = PressStart,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Keep Swiping Button (Primary)
                PixelatedButton(
                    onClick = onKeepSwiping,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "KEEP SWIPING",
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Send Message Button (Secondary)
                PixelatedButton(
                    onClick = onSendMessage,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Gray
                ) {
                    Text(
                        text = "SEND MESSAGE",
                        fontFamily = PressStart,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
