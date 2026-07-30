package com.linhtetko.j2k_auto_android_sample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhtetko.j2k_auto_android_sample.data.model.browse.recipe.Recipe
import com.linhtetko.j2k_auto_android_sample.data.repository.RecipeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RecipeViewModel(private val repository: RecipeRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Recipe>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Recipe>>> = _uiState

    init {
        fetchRecipes()
    }

    fun fetchRecipes() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = repository.getRecipes()
                _uiState.value = UiState.Success(response.recipes)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
