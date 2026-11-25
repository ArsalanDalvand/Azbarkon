package com.arsalandalvand.azbarkon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TagViewModel(private val tagDao: TagDao) : ViewModel() {

    // کل تگ‌ها از دیتابیس (به صورت زنده)
    private val _allTags = tagDao.getAllTags()

    // لایه‌ای که الان کاربر در آن قرار دارد (null یعنی ریشه اصلی)
    private val _currentParentId = MutableStateFlow<Long?>(null)

    // ترکیب داده‌ها برای UI
    val uiState: StateFlow<TagManagerUiState> = combine(_allTags, _currentParentId) { tags, parentId ->
        // تگ‌های لایه فعلی
        val currentLevelTags = tags.filter { it.parentId == parentId }

        // پیدا کردن مسیر (Breadcrumb) برای نمایش در هدر: مثلا "ریشه > شعر > حافظ"
        val breadcrumbs = generateBreadcrumbs(tags, parentId)

        TagManagerUiState(
            currentTags = currentLevelTags,
            breadcrumbs = breadcrumbs,
            currentParentId = parentId,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TagManagerUiState(isLoading = true))

    // --- Actions ---

    fun navigateTo(tagId: Long) {
        _currentParentId.value = tagId
    }

    fun navigateUp() {
        // پیدا کردن پدرِ پدر فعلی برای بازگشت به عقب
        viewModelScope.launch {
            val allTagsList = _allTags.stateIn(viewModelScope).value // دسترسی سریع به اسنپ‌شات
            val currentId = _currentParentId.value
            if (currentId != null) {
                val currentTag = allTagsList.find { it.id == currentId }
                _currentParentId.value = currentTag?.parentId
            }
        }
    }

    fun navigateToRoot() {
        _currentParentId.value = null
    }

    fun addTag(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            tagDao.insertTag(Tag(title = title, parentId = _currentParentId.value))
        }
    }

    fun updateTag(tag: Tag, newTitle: String) {
        viewModelScope.launch {
            // چون در TagDao تابع update نداریم، اینجا فرضی اضافه کردم.
            // اگر ندارید باید delete و insert کنید یا تابع @Update اضافه کنید.
            // پیشنهاد می‌شود تابع @Update در TagDao اضافه شود.
            tagDao.insertTag(tag.copy(title = newTitle)) // در Room اگر @Insert(onConflict = REPLACE) باشد آپدیت میکند، اگر نه باید کوئری آپدیت بزنید
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch {
            tagDao.deleteTag(tag)
        }
    }

    // --- Helpers ---
    private fun generateBreadcrumbs(allTags: List<Tag>, currentId: Long?): List<Tag> {
        val path = mutableListOf<Tag>()
        var tempId = currentId
        while (tempId != null) {
            val found = allTags.find { it.id == tempId }
            if (found != null) {
                path.add(0, found) // افزودن به ابتدای لیست
                tempId = found.parentId
            } else {
                break
            }
        }
        return path
    }
}

data class TagManagerUiState(
    val currentTags: List<Tag> = emptyList(),
    val breadcrumbs: List<Tag> = emptyList(),
    val currentParentId: Long? = null,
    val isLoading: Boolean = false
)