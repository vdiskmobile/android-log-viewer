/*
 * Copyright 2011 Mikhail Lopatkin
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
package org.bitbucket.mlopatkin.android.logviewer.search;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bitbucket.mlopatkin.android.logviewer.TextHighlighter;
import org.bitbucket.mlopatkin.utils.MyStringUtils;

class RegExpSearcher implements HighlightStrategy, SearchStrategy {
    private Pattern pattern;
    private String replacement;

    public RegExpSearcher(String regex) throws PatternSyntaxException {
        pattern = Pattern.compile("(" + regex + ")");
    }

    @Override
    public boolean isStringMatched(String s) {
        return pattern.matcher(s).find();
    }

    @Override
    public void setHighlights(String begin, String end) {
        replacement = MyStringUtils.escRegexChars(begin) + "$1" + MyStringUtils.escRegexChars(end);

    }

    @Override
    public String highlightOccurences(String text) {
        return pattern.matcher(text).replaceAll(replacement);
    }

    @Override
    public void highlightOccurences(String text, TextHighlighter highlighter) {
        // TODO Auto-generated method stub

    }
}