/*
 * Copyright Squircle CE contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blacksquircle.ui.feature.themes.data.repository

import com.blacksquircle.ui.core.contract.FileType
import com.blacksquircle.ui.core.provider.coroutine.DispatcherProvider
import android.content.Context
import com.blacksquircle.ui.core.files.Directories
import com.blacksquircle.ui.core.settings.SettingsManager
import com.blacksquircle.ui.feature.themes.api.interactor.ThemeInteractor
import com.blacksquircle.ui.feature.themes.data.mapper.ThemeMapper
import com.blacksquircle.ui.feature.themes.data.model.AssetsTheme
import com.blacksquircle.ui.feature.themes.data.model.ExternalTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import java.io.File
import com.blacksquircle.ui.feature.themes.domain.model.ThemeModel
import com.blacksquircle.ui.feature.themes.domain.repository.ThemeRepository
import kotlinx.coroutines.withContext

internal class ThemeRepositoryImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val settingsManager: SettingsManager,
    private val themeInteractor: ThemeInteractor,
    private val context: Context,
    private val jsonParser: Json,
) : ThemeRepository {

    override suspend fun loadThemes(query: String): List<ThemeModel> {
        return withContext(dispatcherProvider.io()) {
            val builtIn = AssetsTheme.entries
                .filter { it.themeName.contains(query, ignoreCase = true) }
                .map(ThemeMapper::toModel)
            val imported = Directories.themesDir(context).listFiles { file ->
                file.isFile && file.extension.equals("json", ignoreCase = true)
            }.orEmpty().mapNotNull { file ->
                runCatching {
                    val external = jsonParser.decodeFromString<ExternalTheme>(file.readText())
                    val model = ThemeMapper.toModel(external, file.nameWithoutExtension, file.readText())
                    model.takeIf { it.name.contains(query, ignoreCase = true) }
                }.getOrNull()
            }
            builtIn + imported
        }
    }

    override suspend fun removeTheme(themeModel: ThemeModel) {
        withContext(dispatcherProvider.io()) {
            if (themeModel.isExternal) {
                File(Directories.themesDir(context), themeModel.uuid + FileType.JSON).delete()
            }
            if (settingsManager.editorTheme == themeModel.uuid) {
                settingsManager.remove(SettingsManager.KEY_EDITOR_THEME)
            }
        }
    }

    override suspend fun importTheme(json: String, fileName: String): ThemeModel {
        return withContext(dispatcherProvider.io()) {
            val external = jsonParser.decodeFromString<ExternalTheme>(json)

            val safeName = fileName.substringBeforeLast('.', fileName)
                .replace(Regex("[^A-Za-z0-9._-]"), "_")
                .ifBlank { "imported_theme" }
            val file = File(Directories.themesDir(context), safeName + FileType.JSON)
            file.writeText(json)
            ThemeMapper.toModel(external, safeName, json)
        }
    }

    override suspend fun selectTheme(themeModel: ThemeModel) {
        withContext(dispatcherProvider.io()) {
            themeInteractor.loadTheme(themeModel.uuid)
            settingsManager.editorTheme = themeModel.uuid
        }
    }
}