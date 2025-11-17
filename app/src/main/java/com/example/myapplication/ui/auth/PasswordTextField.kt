package com.example.myapplication.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.PressStart


/**
 * Reusable password text field with eye icon to toggle visibility.
 */
@Composable
fun PasswordTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean,
    Placeholder:String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    val passwordVisible: MutableState<Boolean> = remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(60.dp)
    ) {
        Image(
            painter = painterResource(
                id = if (isError) R.drawable.input_error else R.drawable.input
            ),
            contentDescription = "Password Input Background",
            modifier = Modifier
                .matchParentSize(),
            contentScale = ContentScale.FillBounds
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = Color.Black,
                fontFamily = PressStart,
                fontWeight = FontWeight.Normal
            ),
            visualTransformation = if (passwordVisible.value)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = Placeholder,
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontFamily = PressStart,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
                innerTextField()
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 24.dp, end = 56.dp) // leave space for eye icon
        )

        Image(
            painter = painterResource(
                id = if (passwordVisible.value) R.drawable.eye_cross else R.drawable.eye
            ),
            contentDescription = if (passwordVisible.value) "Hide password" else "Show password",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp).size(30.dp)
                .clickable { passwordVisible.value = !passwordVisible.value }
        )
    }
}




@Preview(showBackground = true)
@Composable
fun PasswordTextFieldPreview() {
    MyApplicationTheme {
        PasswordTextField(
            value = "Password",
            onValueChange = {},
            isError = false,
            Placeholder = "Enter Password"
        )
    }
}
