package com.linhtetko.j2k_auto_android_sample.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.linhtetko.j2k_auto_android_sample.data.model.feeds.Comment
import com.linhtetko.j2k_auto_android_sample.data.repository.CommentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CommentViewModel(private val repository: CommentRepository) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<Comment>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Comment>>> = _uiState

    init {
        fetchComments()
    }

    fun fetchComments() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val response = repository.getComments()
                _uiState.value = UiState.Success(response.comments)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
