package com.example.data.repository

import com.example.data.local.dao.CharacterDao
import com.example.data.local.dao.SceneTemplateDao
import com.example.data.local.entities.CharacterEntity
import com.example.data.local.entities.SceneTemplateEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class StudioRepository(
    private val characterDao: CharacterDao,
    private val sceneTemplateDao: SceneTemplateDao
) {
    fun getCharactersForUser(userId: String): Flow<List<CharacterEntity>> =
        characterDao.getCharactersForUser(userId)

    suspend fun saveCharacter(character: CharacterEntity) {
        characterDao.insertCharacter(character)
    }

    suspend fun duplicateCharacter(character: CharacterEntity) {
        val copy = character.copy(
            id = "char_${UUID.randomUUID().toString().take(8)}",
            name = "${character.name} (Variant)",
            createdAt = System.currentTimeMillis()
        )
        characterDao.insertCharacter(copy)
    }

    suspend fun deleteCharacter(characterId: String) {
        characterDao.deleteCharacterById(characterId)
    }

    fun getTemplatesForUser(userId: String): Flow<List<SceneTemplateEntity>> =
        sceneTemplateDao.getTemplatesForUser(userId)

    suspend fun saveTemplate(template: SceneTemplateEntity) {
        sceneTemplateDao.insertTemplate(template)
    }

    suspend fun deleteTemplate(template: SceneTemplateEntity) {
        sceneTemplateDao.deleteTemplate(template)
    }
}
