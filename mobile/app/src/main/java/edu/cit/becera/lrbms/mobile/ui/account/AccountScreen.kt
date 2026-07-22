package edu.cit.becera.lrbms.mobile.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AccountScreen(onSignedOut: () -> Unit, viewModel: AccountViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    var tab by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("My Account", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        state.memberId?.let {
            Text("Member ID: $it" + (state.dateRegistered?.let { d -> " · Registered $d" } ?: ""), fontSize = 12.sp, color = Color(0xFF64748B))
        }

        TabRow(selectedTabIndex = tab, containerColor = Color.Transparent, contentColor = Color(0xFF4F46E5)) {
            Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Profile") })
            Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Security") })
        }

        Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            if (tab == 0) {
                ProfileTab(state, viewModel)
            } else {
                SecurityTab(state, viewModel)
            }

            Button(
                onClick = { viewModel.signOut(onSignedOut) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9), contentColor = Color(0xFF0F172A))
            ) {
                Text("Sign out", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProfileTab(state: AccountUiState, viewModel: AccountViewModel) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.profileMessage?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

            LabeledField("First name", state.firstName) { viewModel.updateField { copy(firstName = it) } }
            LabeledField("Last name", state.lastName) { viewModel.updateField { copy(lastName = it) } }
            LabeledField("Email", state.email, keyboardType = KeyboardType.Email) { viewModel.updateField { copy(email = it) } }
            LabeledField("Phone number", state.phoneNumber, keyboardType = KeyboardType.Phone) { viewModel.updateField { copy(phoneNumber = it) } }
            LabeledField("Address", state.address) { viewModel.updateField { copy(address = it) } }

            Button(
                onClick = { viewModel.saveProfile() },
                enabled = !state.profileSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text(if (state.profileSaving) "Saving…" else "Save changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SecurityTab(state: AccountUiState, viewModel: AccountViewModel) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Changing your password requires your current password, so no one can hijack your account from a shared or unlocked device.",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
            )
            state.passwordMessage?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

            PasswordField("Current password", state.currentPassword, state.currentPasswordError) {
                viewModel.updateField { copy(currentPassword = it, currentPasswordError = null) }
            }
            PasswordField("New password", state.newPassword, state.newPasswordError, hint = "At least 8 characters, including a letter and a number.") {
                viewModel.updateField { copy(newPassword = it, newPasswordError = null) }
            }
            PasswordField("Confirm new password", state.confirmPassword, state.confirmPasswordError) {
                viewModel.updateField { copy(confirmPassword = it, confirmPasswordError = null) }
            }

            Button(
                onClick = { viewModel.changePassword() },
                enabled = !state.passwordSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))
            ) {
                Text(if (state.passwordSaving) "Updating…" else "Update password", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, value: String, keyboardType: KeyboardType = KeyboardType.Text, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
    )
}

@Composable
private fun PasswordField(label: String, value: String, error: String?, hint: String? = null, onValueChange: (String) -> Unit) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = error != null,
            shape = RoundedCornerShape(12.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
        )
        when {
            error != null -> Text(error, fontSize = 11.sp, color = Color(0xFFDC2626))
            hint != null -> Text(hint, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}
