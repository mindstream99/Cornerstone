/*
 * Copyright to the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paxxis.chime.indexing;

import java.io.IOException;
import java.io.Reader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

/**
 * A Lucene analyzer that behaves as a StandardAnalyzer for all fields except
 * those that end with the ENDING characters, in which case it behaves as a
 * WhitespaceAnalyzer.  This is used in Chime searches so that the standard unique
 * Chime ids are treated as searchable untokenized text, instead of being broken
 * down (which is what the StandardAnalyzer) does.
 * 
 * @author Robert Englander
 */
public class ChimeAnalyzer extends StandardAnalyzer {

    private final static String ENDING = "id";

    private WhitespaceTokenizer whitespace = null;

    @Override
    public TokenStream tokenStream(String fieldName, Reader reader) {
        if (fieldName.toLowerCase().endsWith(ENDING)) {
            if (whitespace == null) {
                whitespace = new WhitespaceTokenizer(reader);
            } else {
                try {
                    whitespace.reset(reader);
                } catch (IOException e) {
                    whitespace = new WhitespaceTokenizer(reader);
                }
            }

            return whitespace;
        } else {
            return super.tokenStream(fieldName, reader);
        }
    }

    @Override
    public TokenStream reusableTokenStream(String fieldName, Reader reader) throws IOException {
        if (fieldName.toLowerCase().endsWith(ENDING)) {
            return (Tokenizer)tokenStream(fieldName, reader);
        } else {
            return super.reusableTokenStream(fieldName, reader);
        }
    }

}