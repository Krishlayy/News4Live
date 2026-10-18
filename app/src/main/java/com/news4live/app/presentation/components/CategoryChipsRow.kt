package com.news4live.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.news4live.app.domain.model.Category
import com.news4live.app.presentation.theme.EditorialRed

@Composable
fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    // "होम" (All) is represented by null or 0L
    val allItems = listOf(Category(id = 0L, name = "होम", slug = "home", postCount = 0)) + categories

    val selectedIndex = allItems.indexOfFirst {
        if (selectedCategoryId == null || selectedCategoryId == 0L) it.id == 0L else it.id == selectedCategoryId
    }.coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 12.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = EditorialRed,
        indicator = { tabPositions ->
            if (selectedIndex < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                    height = 3.dp,
                    color = EditorialRed
                )
            }
        },
        divider = {},
        modifier = modifier.fillMaxWidth()
    ) {
        allItems.forEachIndexed { index, category ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = {
                    onCategorySelected(if (category.id == 0L) null else category.id)
                },
                text = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (isSelected) EditorialRed else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    )
                }
            )
        }
    }
}
