package com.example.todolist.feature.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.todolist.R
import com.example.todolist.core.model.Gender
import com.example.todolist.core.model.User
import com.example.todolist.ui.common.rememberBounceOverscrollEffect
import com.example.todolist.ui.common.bounceOverscroll

@Composable
fun OnboardingScreen(
    onUserCreated: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    // Thêm state để điều khiển màn hình welcome vs form
    var showWelcome by remember { mutableStateOf(true) }

    if (showWelcome) {
        WelcomeScreen(
            onLetsStart = { showWelcome = false },
            modifier = modifier
        )
    } else {
        ProfileFormScreen(
            onUserCreated = onUserCreated,
            onBack = { showWelcome = true },
            modifier = modifier
        )
    }
}

@Composable
private fun WelcomeScreen(
    onLetsStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    // Gradient colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    // Scroll state with bounce
    val scrollState = rememberScrollState()
    val bounceState = rememberBounceOverscrollEffect()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.15f),
                        secondaryColor.copy(alpha = 0.1f),
                        tertiaryColor.copy(alpha = 0.12f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .bounceOverscroll(bounceState)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Top spacer for better layout
            Spacer(modifier = Modifier.height(48.dp))
            // Animated App Icon với multiple circles
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    initialScale = 0.2f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    // Outer circle
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.15f),
                                        secondaryColor.copy(alpha = 0.05f)
                                    )
                                )
                            )
                    )
                    // Middle circle
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.25f),
                                        secondaryColor.copy(alpha = 0.1f)
                                    )
                                )
                            )
                    )
                    // Inner circle with icon
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        primaryColor.copy(alpha = 0.4f),
                                        secondaryColor.copy(alpha = 0.2f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TaskAlt,
                            contentDescription = stringResource(R.string.cd_todolist),
                            modifier = Modifier.size(70.dp),
                            tint = primaryColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated App Name
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -50 },
                    animationSpec = tween(800, delayMillis = 300)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 300))
            ) {
                Text(
                    text = stringResource(R.string.todolist),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 56.sp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor,
                                secondaryColor,
                                tertiaryColor
                            )
                        )
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Animated Tagline
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 50 },
                    animationSpec = tween(800, delayMillis = 500)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 500))
            ) {
                Text(
                    text = stringResource(R.string.tagline),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(80.dp))

            // Animated Features
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(animationSpec = tween(1000, delayMillis = 700))
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    FeatureItem("📝", stringResource(R.string.feature_create_tasks))
                    FeatureItem("🎯", stringResource(R.string.feature_set_goals))
                    FeatureItem("📊", stringResource(R.string.feature_analyze))
                    FeatureItem("🔔", stringResource(R.string.feature_reminders))
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated Button
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(1000, delayMillis = 1000)
                ) + fadeIn(animationSpec = tween(1000, delayMillis = 1000))
            ) {
                Button(
                    onClick = onLetsStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp,
                        pressedElevation = 12.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.lets_start),
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun FeatureItem(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = emoji, fontSize = 24.sp)
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileFormScreen(
    onUserCreated: (User) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.OTHER) }
    var expanded by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.1f),
                        secondaryColor.copy(alpha = 0.05f),
                        tertiaryColor.copy(alpha = 0.08f)
                    ),
                    start = androidx.compose.ui.geometry.Offset(animatedOffset, animatedOffset),
                    end = androidx.compose.ui.geometry.Offset(
                        animatedOffset + 1000f,
                        animatedOffset + 1000f
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated Icon với background gradient
            AnimatedVisibility(
                visible = isVisible,
                enter = scaleIn(
                    initialScale = 0.3f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ) + fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    secondaryColor.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "User Profile",
                        modifier = Modifier.size(80.dp),
                        tint = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Animated Title
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = tween(600, delayMillis = 200)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.create_your_profile),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp
                        ),
                        color = primaryColor,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Animated Subtitle
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = tween(600, delayMillis = 400)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 400))
            ) {
                Text(
                    text = stringResource(R.string.tell_us_about_yourself),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Animated Form Card
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(800, delayMillis = 600)
                ) + fadeIn(animationSpec = tween(800, delayMillis = 600))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        // Full Name Input
                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                showError = false
                            },
                            label = { Text(stringResource(R.string.full_name)) },
                            placeholder = { Text(stringResource(R.string.enter_full_name)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showError && fullName.isBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // Age Input
                        OutlinedTextField(
                            value = age,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                    age = it
                                    showError = false
                                }
                            },
                            label = { Text(stringResource(R.string.age)) },
                            placeholder = { Text(stringResource(R.string.enter_age)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            isError = showError && (age.isBlank() || age.toIntOrNull() == null || age.toIntOrNull()!! <= 0),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor
                            )
                        )

                        // Gender Dropdown - Simple clickable approach
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = when (gender) {
                                    Gender.MALE -> stringResource(R.string.gender_male)
                                    Gender.FEMALE -> stringResource(R.string.gender_female)
                                    Gender.OTHER -> stringResource(R.string.gender_other)
                                },
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text(stringResource(R.string.gender)) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = primaryColor
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledBorderColor = primaryColor.copy(alpha = 0.5f),
                                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            // Invisible clickable overlay
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { expanded = !expanded }
                            )
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.9f)
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.gender_male)) },
                                    onClick = {
                                        gender = Gender.MALE
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.gender_female)) },
                                    onClick = {
                                        gender = Gender.FEMALE
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.gender_other)) },
                                    onClick = {
                                        gender = Gender.OTHER
                                        expanded = false
                                    }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = showError,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                ),
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "⚠️ " + stringResource(R.string.validation_error),
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Back Button
                            OutlinedButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = primaryColor
                                )
                            ) {
                                Text(
                                    text = "← " + stringResource(R.string.button_back),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            // Get Started Button
                            Button(
                                onClick = {
                                    val ageValue = age.toIntOrNull()
                                    if (fullName.isNotBlank() && ageValue != null && ageValue > 0) {
                                        val newUser = User(
                                            id = 0,
                                            fullName = fullName.trim(),
                                            age = ageValue,
                                            gender = gender,
                                            avatarUrl = null
                                        )
                                        onUserCreated(newUser)
                                    } else {
                                        showError = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = primaryColor
                                ),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 4.dp,
                                    pressedElevation = 8.dp
                                )
                            ) {
                                Text(
                                    text = stringResource(R.string.button_start),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
