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

package com.blacksquircle.ui.feature.themes.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ExternalTheme(
    @SerialName("name")
    val name: String? = null,
    val author: String? = null,
    @SerialName("type")
    val type: String? = "dark",
    @SerialName("colors")
    val colors: ExternalColors? = null,
)

@Serializable
internal data class ExternalColors(
    @SerialName("global.colorPrimary")
    val colorPrimary: String = "#FF8000",
    @SerialName("global.colorOutline")
    val colorOutline: String = "#393B40",
    @SerialName("global.colorBackgroundPrimary")
    val colorBackgroundPrimary: String = "#1E1F22",
    @SerialName("global.colorBackgroundSecondary")
    val colorBackgroundSecondary: String = "#2B2D30",
    @SerialName("global.colorBackgroundTertiary")
    val colorBackgroundTertiary: String = "#3E3F43",
    @SerialName("global.colorTextAndIconPrimary")
    val colorTextAndIconPrimary: String = "#FFFFFF",
    @SerialName("global.colorTextAndIconPrimaryInverse")
    val colorTextAndIconPrimaryInverse: String = "#000000",
    @SerialName("global.colorTextAndIconSecondary")
    val colorTextAndIconSecondary: String = "#BCBCBC",
    @SerialName("global.colorTextAndIconDisabled")
    val colorTextAndIconDisabled: String = "#6E6E6E",
    @SerialName("global.colorTextAndIconAdditional")
    val colorTextAndIconAdditional: String = "#FFBB33",
    @SerialName("global.colorTextAndIconSuccess")
    val colorTextAndIconSuccess: String = "#71D98C",
    @SerialName("global.colorTextAndIconError")
    val colorTextAndIconError: String = "#E45356",
)