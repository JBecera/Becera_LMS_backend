package edu.cit.becera.lrbms.mobile.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.cit.becera.lrbms.mobile.data.model.Member
import edu.cit.becera.lrbms.mobile.ui.dashboard.InfoCard

@Composable
fun ManageAccountsScreen(viewModel: ManageAccountsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val form = state.form

    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFFF8FAFC), Color(0xFFEFF2FF))))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Manage accounts", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
            if (form.id != null) TextButton(onClick = { viewModel.cancelEdit() }) { Text("Cancel edit") }
        }
        Text("Create librarian accounts and keep member records accurate.", fontSize = 13.sp, color = Color(0xFF64748B))
        state.message?.let { Text(it, fontSize = 13.sp, color = Color(0xFF4F46E5)) }

        Surface(shape = RoundedCornerShape(20.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (form.id != null) "Edit account" else "Add librarian account", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF0F172A))
                OutlinedTextField(form.firstName, { viewModel.updateForm { copy(firstName = it) } }, label = { Text("First name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5)))
                OutlinedTextField(form.lastName, { viewModel.updateForm { copy(lastName = it) } }, label = { Text("Last name") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5)))
                OutlinedTextField(form.email, { viewModel.updateForm { copy(email = it) } }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5)))
                OutlinedTextField(
                    form.password,
                    { viewModel.updateForm { copy(password = it) } },
                    label = { Text(if (form.id != null) "New password (leave blank to keep current)" else "Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF4F46E5))
                )
                Text("At least 8 characters, including a letter and a number.", fontSize = 11.sp, color = Color(0xFF94A3B8))
                Button(onClick = { viewModel.submit() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5))) {
                    Text(if (form.id != null) "Save changes" else "Create librarian", fontWeight = FontWeight.Bold)
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(Modifier.weight(1f), "Registered users", state.members.size.toString())
            StatTile(Modifier.weight(1f), "Librarian accounts", state.librarians.size.toString())
        }

        Text("Account directory", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF0F172A))
        if (state.isLoading) {
            CircularProgressIndicator(color = Color(0xFF4F46E5))
        } else if (state.members.isEmpty()) {
            InfoCard(title = "No accounts yet", subtitle = "Registered members and librarians will be listed here.")
        } else {
            state.members.forEach { member -> AccountRow(member, onEdit = { viewModel.edit(member) }, onDelete = { viewModel.delete(member.id!!) }) }
        }
    }
}

@Composable
private fun StatTile(modifier: Modifier = Modifier, label: String, value: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontSize = 12.sp, color = Color(0xFF64748B))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0F172A))
        }
    }
}

@Composable
private fun AccountRow(member: Member, onEdit: () -> Unit, onDelete: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${member.firstName} ${member.lastName}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF1E293B))
                    Text(member.email, fontSize = 12.sp, color = Color(0xFF64748B))
                }
                Text(member.role ?: "MEMBER", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Remove", color = Color(0xFFDC2626)) }
            }
        }
    }
}
