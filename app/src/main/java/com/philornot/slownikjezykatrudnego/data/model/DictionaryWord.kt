package com.philornot.slownikjezykatrudnego.data.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

/**
 * Data model representing a dictionary vocabulary entry.
 *
 * @property id Unique identifier of the word.
 * @property word The main word term (e.g., "Imponderabilia").
 * @property phonetic Optional phonetic transcription / pronunciation.
 * @property shortDefinition Concise summary definition used in quizzes.
 * @property fullDefinition Comprehensive and detailed encyclopedic definition.
 * @property etymology Historical linguistic origin of the term.
 * @property examples Contextual example sentences demonstrating correct usage.
 * @property category Thematic category name (e.g., "Filozofia i Pojęcia").
 * @property sjpUrl Direct URL link to the official PWN Polish dictionary entry.
 * @property distractors Plausible incorrect definitions used for multiple-choice quizzes.
 */
@OptIn(InternalSerializationApi::class)
@Serializable
data class DictionaryWord(
    val id: String,
    val word: String,
    val phonetic: String? = null,
    val shortDefinition: String,
    val fullDefinition: String,
    val etymology: String? = null,
    val examples: List<String> = emptyList(),
    val category: String,
    val sjpUrl: String,
    val distractors: List<String> = emptyList()
)
