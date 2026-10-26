package com.jobautoapply.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jobautoapply.app.data.model.UserProfile
import com.jobautoapply.app.data.repository.AppPreferences
import com.jobautoapply.app.ui.theme.DarkBackground
import com.jobautoapply.app.ui.theme.DarkCardBorder
import com.jobautoapply.app.ui.theme.DarkSurface
import com.jobautoapply.app.ui.theme.EmeraldSuccess
import com.jobautoapply.app.ui.theme.PrimaryBlue
import com.jobautoapply.app.ui.theme.TextMuted
import com.jobautoapply.app.ui.theme.TextPrimary
import com.jobautoapply.app.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    appPreferences: AppPreferences
) {
    val currentProfile by appPreferences.profile.collectAsState()

    var fullName by remember(currentProfile) { mutableStateOf(currentProfile.fullName) }
    var phoneNumber by remember(currentProfile) { mutableStateOf(currentProfile.phoneNumber) }
    var totalExp by remember(currentProfile) { mutableStateOf(currentProfile.totalExperienceYears.toString()) }
    var currentCtc by remember(currentProfile) { mutableStateOf(currentProfile.currentCtcLakhs.toString()) }
    var expectedCtc by remember(currentProfile) { mutableStateOf(currentProfile.expectedCtcLakhs.toString()) }
    var noticePeriod by remember(currentProfile) { mutableStateOf(currentProfile.noticePeriodDays.toString()) }
    var isAuthorized by remember(currentProfile) { mutableStateOf(currentProfile.isAuthorizedToWork) }
    var requiresVisa by remember(currentProfile) { mutableStateOf(currentProfile.requiresVisaSponsorship) }
    var savedMessage by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Candidate Profile Answers",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Used by the bot to auto-answer screening questions in LinkedIn Easy Apply.",
            fontSize = 12.sp,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Contact Info Section
        SectionBox(title = "CONTACT DETAILS") {
            ProfileTextField(
                label = "Full Name",
                value = fullName,
                onValueChange = { fullName = it; savedMessage = false }
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileTextField(
                label = "Phone Number (required for Easy Apply)",
                value = phoneNumber,
                onValueChange = { phoneNumber = it; savedMessage = false }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Experience & Compensation Section
        SectionBox(title = "EXPERIENCE & COMPENSATION") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileTextField(
                    modifier = Modifier.weight(1f),
                    label = "Total Exp (Years)",
                    value = totalExp,
                    onValueChange = { totalExp = it; savedMessage = false }
                )
                ProfileTextField(
                    modifier = Modifier.weight(1f),
                    label = "Notice Period (Days)",
                    value = noticePeriod,
                    onValueChange = { noticePeriod = it; savedMessage = false }
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ProfileTextField(
                    modifier = Modifier.weight(1f),
                    label = "Current CTC (LPA)",
                    value = currentCtc,
                    onValueChange = { currentCtc = it; savedMessage = false }
                )
                ProfileTextField(
                    modifier = Modifier.weight(1f),
                    label = "Expected CTC (LPA)",
                    value = expectedCtc,
                    onValueChange = { expectedCtc = it; savedMessage = false }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Screening Questions Section
        SectionBox(title = "SCREENING DEFAULTS") {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Legally Authorized to Work", fontSize = 13.sp, color = TextPrimary)
                    Text("Auto-select 'Yes' on work authorization", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = isAuthorized,
                    onCheckedChange = { isAuthorized = it; savedMessage = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Require Visa Sponsorship", fontSize = 13.sp, color = TextPrimary)
                    Text("Auto-select 'No' to visa sponsorship", fontSize = 11.sp, color = TextMuted)
                }
                Switch(
                    checked = requiresVisa,
                    onCheckedChange = { requiresVisa = it; savedMessage = false },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldSuccess)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (savedMessage) {
            Text(
                text = "✓ Profile answers saved successfully!",
                fontSize = 13.sp,
                color = EmeraldSuccess,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                val updated = currentProfile.copy(
                    fullName = fullName,
                    phoneNumber = phoneNumber,
                    totalExperienceYears = totalExp.toFloatOrNull() ?: 3.0f,
                    noticePeriodDays = noticePeriod.toIntOrNull() ?: 30,
                    currentCtcLakhs = currentCtc.toFloatOrNull() ?: 12.0f,
                    expectedCtcLakhs = expectedCtc.toFloatOrNull() ?: 18.0f,
                    isAuthorizedToWork = isAuthorized,
                    requiresVisaSponsorship = requiresVisa
                )
                appPreferences.saveProfile(updated)
                savedMessage = true
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
        ) {
            Text("Save Profile Answers")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SectionBox(
    title: String,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(14.dp))
            .border(1.dp, DarkCardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun ProfileTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 12.sp) },
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = DarkCardBorder,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary
        )
    )
}
