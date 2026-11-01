package com.example.firebasecloudnotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CloudNotesScreen()
            }
        }
    }
}

@Composable
fun CloudNotesScreen() {
    val db = remember { Firebase.firestore }
    var note by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf(listOf<String>()) }
    var message by remember { mutableStateOf("") }

    DisposableEffect(Unit) {
        val registration = db.collection("notes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    message = "Unable to load notes"
                } else {
                    notes = snapshot?.documents?.mapNotNull { it.getString("text") } ?: emptyList()
                }
            }
        onDispose { registration.remove() }
    }

    Column(Modifier.padding(16.dp)) {
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Enter note") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(onClick = {
            if (note.isNotBlank()) {
                db.collection("notes")
                    .add(mapOf("text" to note))
                    .addOnSuccessListener {
                        note = ""
                        message = "Note saved"
                    }
                    .addOnFailureListener { message = "Save failed" }
            }
        }) {
            Text("Save to Cloud")
        }
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(message)
        }
        Spacer(Modifier.height(16.dp))
        LazyColumn {
            items(notes) { item ->
                Text(item, Modifier.padding(8.dp))
            }
        }
    }
}
