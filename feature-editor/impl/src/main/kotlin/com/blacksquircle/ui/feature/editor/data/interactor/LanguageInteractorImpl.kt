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

package com.blacksquircle.ui.feature.editor.data.interactor

import android.content.Context
import com.blacksquircle.ui.core.provider.coroutine.DispatcherProvider
import com.blacksquircle.ui.feature.editor.data.mapper.GrammarMapper
import com.blacksquircle.ui.feature.editor.data.model.GrammarData
import com.blacksquircle.ui.feature.editor.domain.interactor.LanguageInteractor
import com.blacksquircle.ui.feature.editor.domain.model.GrammarModel
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

internal class LanguageInteractorImpl(
    private val dispatcherProvider: DispatcherProvider,
    private val jsonParser: Json,
    private val context: Context,
) : LanguageInteractor {

    private var grammars = emptyList<GrammarModel>()
    private var grammarsByScope = emptyMap<String, GrammarModel>()
    private var grammarsByName = emptyMap<String, GrammarModel>()
    private val registeredGrammars = mutableSetOf<String>()

    init {
        FileProviderRegistry.getInstance().addFileProvider(
            AssetsFileResolver(context.assets)
        )
    }

    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun loadGrammars(): List<GrammarModel> {
        return withContext(dispatcherProvider.io()) {
            if (grammars.isNotEmpty()) {
                return@withContext grammars
            }
            context.assets.open(ASSET_FILE).use { languagesFile ->
                grammars = jsonParser.decodeFromStream<List<GrammarData>>(languagesFile)
                    .map(GrammarMapper::toModel)
            }
            grammarsByScope = grammars.associateBy(GrammarModel::scopeName)
            grammarsByName = grammars.associateBy(GrammarModel::name)

            // Metadata is cached; expensive TextMate grammar files are loaded only
            // when a document actually needs that language.
            grammars
        }
    }

    override suspend fun registerGrammar(language: String) {
        withContext(dispatcherProvider.io()) {
            val grammar = grammarsByScope[language] ?: return@withContext
            if (language in registeredGrammars ||
                GrammarRegistry.getInstance().findGrammar(language) != null
            ) {
                registeredGrammars.add(language)
                return@withContext
            }

            GrammarRegistry.getInstance().loadGrammar(GrammarMapper.toDefinition(grammar))
            registeredGrammars.add(language)

            for ((_, embeddedName) in grammar.embeddedLanguages) {
                val embeddedGrammar = grammarsByName[embeddedName] ?: continue
                if (embeddedGrammar.scopeName !in registeredGrammars) {
                    registerGrammar(embeddedGrammar.scopeName)
                }
            }
        }
    }

    companion object {
        private const val ASSET_FILE = "languages.json"
    }
}