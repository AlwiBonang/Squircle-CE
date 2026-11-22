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

package com.blacksquircle.ui.feature.explorer.ui.explorer.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.blacksquircle.ui.feature.explorer.ui.explorer.model.FileNode

@Composable
internal fun FileExplorer(
    contentPadding: PaddingValues,
    fileNodes: List<FileNode>,
    selectedNodes: List<FileNode>,
    modifier: Modifier = Modifier,
    onFileClicked: (FileNode) -> Unit = {},
    onFileSelected: (FileNode) -> Unit = {},
) {
    val hapticFeedback = LocalHapticFeedback.current
    // Cache selection keys to avoid repeated linear scans during row recomposition.
    val selectedKeys = remember(selectedNodes) { selectedNodes.mapTo(HashSet()) { it.key } }

    LazyColumn(
        contentPadding = contentPadding,
        modifier = modifier.fillMaxSize()
    ) {
        items(
            items = fileNodes,
            key = { it.file.fileUri },
            contentType = { "file-row" },
        ) { fileNode ->
            val isSelected = fileNode.key in selectedKeys
            FileItem(
                fileNode = fileNode,
                isSelected = isSelected,
                onClick = { onFileClicked(fileNode) },
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFileSelected(fileNode)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}