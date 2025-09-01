package org.robiul.kmprecipeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.robiul.kmprecipeapp.domain.models.Ingredient

@Composable
fun IngredientEditor(
    ingredient: Ingredient,
    onChange: (Ingredient) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OutlinedTextField(
            value = ingredient.name,
            onValueChange = { newName -> onChange(ingredient.copy(name = newName)) },
            label = { Text("Name") },
            modifier = Modifier.weight(2f) // more space for name
        )

        OutlinedTextField(
            value = ingredient.measure,
            onValueChange = { newMeasure -> onChange(ingredient.copy(measure = newMeasure)) },
            label = { Text("Measure") },
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
        )

        IconButton(onClick = onRemove, modifier = Modifier.padding(start = 8.dp)) {
            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove ingredient")
        }
    }

}
