package com.arsalandalvand.azbarkon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NoteViewModel(
    private val noteDao: NoteDao,
    private val tagDao: TagDao
) : ViewModel() {

    // ورودی‌های کاربر
    private val _searchQuery = MutableStateFlow("")
    // تغییر: عمومی کردن این متغیر برای دسترسی در UI
    private val _selectedFilterTagIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedFilterTagIds = _selectedFilterTagIds.asStateFlow()

    // داده‌های خام از دیتابیس
    private val _allNotes = noteDao.getNotesWithTags()
    val allTags = tagDao.getAllTags().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ترکیب داده‌ها برای خروجی نهایی (فیلتر + جستجو)
    val filteredNotes = combine(
        _allNotes,
        _searchQuery,
        _selectedFilterTagIds,
        allTags
    ) { notesWithTags, query, filterIds, tagsList ->

        // مرحله ۱: پیدا کردن تمام IDهای مجاز براساس فیلتر درختی
        val effectiveFilterIds = if (filterIds.isEmpty()) {
            null
        } else {
            val expandedIds = mutableSetOf<Long>()
            filterIds.forEach { parentId ->
                expandedIds.add(parentId)
                expandedIds.addAll(getAllDescendantIds(parentId, tagsList))
            }
            expandedIds
        }

        // مرحله ۲: اعمال فیلتر و جستجو
        notesWithTags.filter { item ->
            val note = item.note
            val noteTagIds = item.tags.map { it.id }.toSet()

            val matchesSearch = query.isBlank() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.description.contains(query, ignoreCase = true)

            val matchesTags = effectiveFilterIds == null ||
                    noteTagIds.any { it in effectiveFilterIds }

            matchesSearch && matchesTags
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun getAllDescendantIds(parentId: Long, allTags: List<Tag>): Set<Long> {
        val children = allTags.filter { it.parentId == parentId }
        val result = children.map { it.id }.toMutableSet()
        children.forEach { child ->
            result.addAll(getAllDescendantIds(child.id, allTags))
        }
        return result
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterTagToggle(tagId: Long) {
        val current = _selectedFilterTagIds.value.toMutableSet()
        if (current.contains(tagId)) current.remove(tagId) else current.add(tagId)
        _selectedFilterTagIds.value = current
    }

    fun clearFilters() {
        _selectedFilterTagIds.value = emptySet()
    }

    // عملیات دیتابیس (اصلاح شده)
    fun saveNote(note: Note, tagIds: List<Long>) {
        viewModelScope.launch {
            val noteId = if (note.id == 0) {
                noteDao.insertNoteItem(note).toInt() // نام تابع اصلاح شد
            } else {
                noteDao.updateNoteItem(note) // نام تابع اصلاح شد
                note.id
            }

            noteDao.clearNoteTags(noteId)
            tagIds.forEach { tagId ->
                noteDao.insertNoteTagCrossRef(NoteTagCrossRef(noteId, tagId))
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteDao.deleteNoteItem(note) // نام تابع اصلاح شد
        }
    }

    fun addNewTag(title: String, parentId: Long?) {
        viewModelScope.launch {
            tagDao.insertTag(Tag(title = title, parentId = parentId))
        }
    }
}