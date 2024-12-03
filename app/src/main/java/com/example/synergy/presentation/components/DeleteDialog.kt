package com.example.synergy.presentation.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun PreviewDeleteDialog() {
    DeleteDialog(
        isOpen = true,
        title = "Eliminar tarea",
        bodyText = "¿Estás seguro de que quieres eliminar esta tarea?",
        onDismissRequest = {},
        onConfirmButtonClick = {}
    )
}

@Composable
fun DeleteDialog(
    isOpen: Boolean,
    title: String,
    bodyText: String,
    onDismissRequest: () -> Unit,
    onConfirmButtonClick: () -> Unit
) {
    if (isOpen) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = title) },
            text = { Text(text = bodyText) },
            dismissButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = "Cancelar")
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmButtonClick) {
                    Text(text = "Eliminar")
                }
            }
        )
    }
}