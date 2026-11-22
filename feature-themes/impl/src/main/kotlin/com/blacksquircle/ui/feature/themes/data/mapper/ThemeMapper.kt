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

package com.blacksquircle.ui.feature.themes.data.mapper

import android.graphics.Color
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import com.blacksquircle.ui.feature.themes.api.model.ColorScheme
import com.blacksquircle.ui.feature.themes.api.model.ThemeType
import com.blacksquircle.ui.feature.themes.data.model.AssetsTheme
import com.blacksquircle.ui.feature.themes.data.model.EditorTheme
import com.blacksquircle.ui.feature.themes.data.model.ExternalTheme
import com.blacksquircle.ui.feature.themes.domain.model.ThemeModel

internal object ThemeMapper {

    fun toModel(assetsTheme: AssetsTheme): ThemeModel {
        return ThemeModel(
            uuid = assetsTheme.themeId,
            name = assetsTheme.themeName,
            author = "Squircle CE",
            colors = when (assetsTheme) {
                AssetsTheme.THEME_DARCULA -> EditorTheme.DARCULA
                AssetsTheme.THEME_ECLIPSE -> EditorTheme.ECLIPSE
                AssetsTheme.THEME_MONOKAI -> EditorTheme.MONOKAI
                AssetsTheme.THEME_OBSIDIAN -> EditorTheme.OBSIDIAN
                AssetsTheme.THEME_INTELLIJ_LIGHT -> EditorTheme.INTELLIJ_LIGHT
                AssetsTheme.THEME_LADIES_NIGHT -> EditorTheme.LADIES_NIGHT
                AssetsTheme.THEME_TOMORROW_NIGHT -> EditorTheme.TOMORROW_NIGHT
                AssetsTheme.THEME_SOLARIZED_LIGHT -> EditorTheme.SOLARIZED_LIGHT
                AssetsTheme.THEME_VISUAL_STUDIO -> EditorTheme.VISUAL_STUDIO
            },
            isExternal = false,
        )
    }


    fun toModel(externalTheme: ExternalTheme, uuid: String, rawJson: String? = null): ThemeModel {
        val previewColors = rawJson?.let(::parsePreviewColors)
            ?: externalTheme.colors?.let { colors ->
                com.blacksquircle.ui.feature.themes.domain.model.ColorModel(
                    textColor = Color.parseColor(colors.colorTextAndIconPrimary),
                    backgroundColor = Color.parseColor(colors.colorBackgroundPrimary),
                    numberColor = Color.parseColor("#61AFEF"),
                    operatorColor = Color.parseColor("#56B6C2"),
                    keywordColor = Color.parseColor("#C678DD"),
                    variableColor = Color.parseColor("#ABB2BF"),
                    functionColor = Color.parseColor("#61AFEF"),
                    stringColor = Color.parseColor("#98C379"),
                    commentColor = Color.parseColor("#5C6370"),
                )
            } ?: com.blacksquircle.ui.feature.themes.data.model.EditorTheme.DARCULA
        return ThemeModel(
            uuid = uuid,
            name = externalTheme.name?.takeIf { it.isNotBlank() } ?: uuid,
            author = externalTheme.author?.takeIf { it.isNotBlank() } ?: "Imported",
            colors = previewColors,
            isExternal = true,
        )
    }

    private fun parsePreviewColors(rawJson: String): com.blacksquircle.ui.feature.themes.domain.model.ColorModel? = runCatching {
        val root = Json.parseToJsonElement(rawJson).jsonObject
        val colors = root["colors"]?.jsonObject ?: JsonObject(emptyMap())
        val tokens = root["tokenColors"]?.jsonArray ?: JsonArray(emptyList())
        fun color(value: String?, fallback: String): Int =
            runCatching { Color.parseColor(value ?: fallback) }.getOrDefault(Color.parseColor(fallback))
        fun editor(key: String, fallback: String) = colors[key]?.jsonPrimitive?.content ?: fallback
        fun token(scope: String, fallback: String): String {
            for (entry in tokens) {
                val obj = entry.jsonObject
                val scopes = obj["scope"]?.let { element ->
                    when (element) {
                        is JsonPrimitive -> element.content.split(",")
                        is JsonArray -> element.mapNotNull { it.jsonPrimitive.content }
                        else -> emptyList()
                    }
                }.orEmpty()
                if (scopes.any { it.trim().equals(scope, true) || it.trim().startsWith(scope, true) }) {
                    return obj["settings"]?.jsonObject?.get("foreground")?.jsonPrimitive?.content ?: fallback
                }
            }
            return fallback
        }
        com.blacksquircle.ui.feature.themes.domain.model.ColorModel(
            textColor = color(editor("editor.foreground", "#ABB2BF"), "#ABB2BF"),
            backgroundColor = color(editor("editor.background", "#1E1E1E"), "#1E1E1E"),
            numberColor = color(token("constant.numeric", "#D19A66"), "#D19A66"),
            operatorColor = color(token("keyword.operator", "#56B6C2"), "#56B6C2"),
            keywordColor = color(token("keyword", "#C678DD"), "#C678DD"),
            variableColor = color(token("variable", "#ABB2BF"), "#ABB2BF"),
            functionColor = color(token("entity.name.function", "#61AFEF"), "#61AFEF"),
            stringColor = color(token("string", "#98C379"), "#98C379"),
            commentColor = color(token("comment", "#5C6370"), "#5C6370"),
        )
    }.getOrNull()

    fun toColorScheme(externalTheme: ExternalTheme): ColorScheme {
        val colors = externalTheme.colors ?: com.blacksquircle.ui.feature.themes.data.model.ExternalColors()
        return ColorScheme(
            type = ThemeType.of(externalTheme.type.orEmpty()),
            colorPrimary = Color.parseColor(colors.colorPrimary),
            colorOutline = Color.parseColor(colors.colorOutline),
            colorBackgroundPrimary = Color.parseColor(colors.colorBackgroundPrimary),
            colorBackgroundSecondary = Color.parseColor(colors.colorBackgroundSecondary),
            colorBackgroundTertiary = Color.parseColor(colors.colorBackgroundTertiary),
            colorTextAndIconPrimary = Color.parseColor(colors.colorTextAndIconPrimary),
            colorTextAndIconPrimaryInverse = Color.parseColor(colors.colorTextAndIconPrimaryInverse),
            colorTextAndIconSecondary = Color.parseColor(colors.colorTextAndIconSecondary),
            colorTextAndIconDisabled = Color.parseColor(colors.colorTextAndIconDisabled),
            colorTextAndIconAdditional = Color.parseColor(colors.colorTextAndIconAdditional),
            colorTextAndIconSuccess = Color.parseColor(colors.colorTextAndIconSuccess),
            colorTextAndIconError = Color.parseColor(colors.colorTextAndIconError),
        )
    }
}
