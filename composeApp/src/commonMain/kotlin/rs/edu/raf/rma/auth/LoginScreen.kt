package rs.edu.raf.rma.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel

private val BgDark = Color(0xFF14181C)
private val BgCard = Color(0xFF22272E)
private val AccentGreen = Color(0xFF00E054)
private val TextPrimary = Color(0xFFFFFFFF)
private val TextMuted = Color(0xFF8899AA)
private val FieldBorder = Color(0xFF3A4550)
private val ErrorRed = Color(0xFFFF6B6B)

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .imePadding(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // logo / title
            Text(
                text = "SHOWTIME",
                color = AccentGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 4.sp,
            )
            Text(
                text = "Your movie companion",
                color = TextMuted,
                fontSize = 13.sp,
            )

            Spacer(Modifier.height(40.dp))

            // card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard, RoundedCornerShape(16.dp))
                    .padding(24.dp),
            ) {
                // login / register tabs
                TabRow(
                    selectedTabIndex = if (state.mode == LoginMode.Login) 0 else 1,
                    containerColor = BgCard,
                    contentColor = AccentGreen,
                    indicator = {},
                    divider = {},
                ) {
                    Tab(
                        selected = state.mode == LoginMode.Login,
                        onClick = { if (state.mode != LoginMode.Login) viewModel.setEvent(LoginEvent.SwitchMode) },
                        text = {
                            Text(
                                "Login",
                                color = if (state.mode == LoginMode.Login) AccentGreen else TextMuted,
                                fontWeight = if (state.mode == LoginMode.Login) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                    Tab(
                        selected = state.mode == LoginMode.Register,
                        onClick = { if (state.mode != LoginMode.Register) viewModel.setEvent(LoginEvent.SwitchMode) },
                        text = {
                            Text(
                                "Register",
                                color = if (state.mode == LoginMode.Register) AccentGreen else TextMuted,
                                fontWeight = if (state.mode == LoginMode.Register) FontWeight.Bold else FontWeight.Normal,
                            )
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))

                // username
                AuthField(
                    label = "Username",
                    value = state.username,
                    onValueChange = { viewModel.setEvent(LoginEvent.SetUsername(it)) },
                    imeAction = if (state.mode == LoginMode.Register) ImeAction.Next else ImeAction.Next,
                )

                Spacer(Modifier.height(12.dp))

                // full name (register only)
                AnimatedVisibility(visible = state.mode == LoginMode.Register) {
                    Column {
                        AuthField(
                            label = "Full Name",
                            value = state.fullName,
                            onValueChange = { viewModel.setEvent(LoginEvent.SetFullName(it)) },
                            imeAction = ImeAction.Next,
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                // password
                AuthField(
                    label = "Password",
                    value = state.password,
                    onValueChange = { viewModel.setEvent(LoginEvent.SetPassword(it)) },
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done,
                    isPassword = true,
                    onDone = { viewModel.setEvent(LoginEvent.Submit) },
                )

                // error
                AnimatedVisibility(visible = state.error != null) {
                    Column {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = state.error ?: "",
                            color = ErrorRed,
                            fontSize = 13.sp,
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // submit button
                Button(
                    onClick = { viewModel.setEvent(LoginEvent.Submit) },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = AccentGreen.copy(alpha = 0.4f),
                    ),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(
                            text = if (state.mode == LoginMode.Login) "Sign in" else "Create account",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // switch mode hint
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = if (state.mode == LoginMode.Login) "Don't have an account?" else "Already have an account?",
                        color = TextMuted,
                        fontSize = 13.sp,
                    )
                    TextButton(onClick = { viewModel.setEvent(LoginEvent.SwitchMode) }) {
                        Text(
                            text = if (state.mode == LoginMode.Login) "Register" else "Sign in",
                            color = AccentGreen,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    isPassword: Boolean = false,
    onDone: (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = TextMuted) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = imeAction,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onDone?.invoke() },
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = AccentGreen,
            unfocusedBorderColor = FieldBorder,
            cursorColor = AccentGreen,
            focusedLabelColor = AccentGreen,
        ),
        shape = RoundedCornerShape(8.dp),
    )
}
