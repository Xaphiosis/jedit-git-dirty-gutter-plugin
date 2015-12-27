/*
 * Copyright (C) 2015 Steven Soloff
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal

import spock.lang.Specification

class StringUtilsSpec extends Specification {
    def 'joinLinesWithExplicitFinalLine - when line count is zero it should return an empty string'() {
        expect:
        StringUtils.joinLinesWithExplicitFinalLine([]) == ''
    }

    def 'joinLinesWithExplicitFinalLine - when line count is one it should return a string with the line and no final newline'() {
        expect:
        StringUtils.joinLinesWithExplicitFinalLine([line1]) == "$line1"

        where:
        line1 << ['line1', '']
    }

    def 'joinLinesWithExplicitFinalLine - when line count is two it should return a string with a newline between each line and no final newline'() {
        expect:
        StringUtils.joinLinesWithExplicitFinalLine([line1, line2]) == "$line1\n$line2"

        where:
        line1 << ['line1', '',      'line1', '']
        line2 << ['line2', 'line2', '',      '']
    }

    def 'joinLinesWithImplicitFinalLine - when line count is zero it should return an empty string'() {
        expect:
        StringUtils.joinLinesWithImplicitFinalLine([]) == ''
    }

    def 'joinLinesWithImplicitFinalLine - when line count is one it should return a string with the line and a final newline'() {
        expect:
        StringUtils.joinLinesWithImplicitFinalLine([line1]) == "$line1\n"

        where:
        line1 << ['line1', '']
    }

    def 'joinLinesWithImplicitFinalLine - when line count is two it should return a string with a newline between each line and a final newline'() {
        expect:
        StringUtils.joinLinesWithImplicitFinalLine([line1, line2]) == "$line1\n$line2\n"

        where:
        line1 << ['line1', '',      'line1', '']
        line2 << ['line2', 'line2', '',      '']
    }

    def 'splitLinesWithExplicitFinalLine - when input is empty it should return an empty collection'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine('') == []
    }

    def 'splitLinesWithExplicitFinalLine - when input contains one line and no final newline it should return a collection with one line'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine('line1') == ['line1']
    }

    def 'splitLinesWithExplicitFinalLine - when input contains one line and a final newline it should return a collection with two lines'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine("$line1\n") == [line1, '']

        where:
        line1 << ['line1', '']
    }

    def 'splitLinesWithExplicitFinalLine - when input contains two lines and no final newline it should return a collection with two lines'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine("$line1\n$line2") == [line1, line2]

        where:
        line1 << ['line1', '',      'line1', '']
        line2 << ['line2', 'line2', '',      '']
    }

    def 'splitLinesWithExplicitFinalLine - when input contains two lines and a final newline it should return a collection with three lines'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine("$line1\n$line2\n") == [line1, line2, '']

        where:
        line1 << ['line1', '',      'line1', '']
        line2 << ['line2', 'line2', '',      '']
    }

    def 'splitLinesWithExplicitFinalLine - it should handle Mac line separators'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine('line1\rline2\r') == ['line1', 'line2', '']
    }

    def 'splitLinesWithExplicitFinalLine - it should handle Windows line separators'() {
        expect:
        StringUtils.splitLinesWithExplicitFinalLine('line1\r\nline2\r\n') == ['line1', 'line2', '']
    }

    def 'splitLinesWithImplicitFinalLine - when input is empty it should return an empty collection'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine('') == []
    }

    def 'splitLinesWithImplicitFinalLine - when input contains one line and no final newline it should return a collection with one line'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine('line1') == ['line1']
    }

    def 'splitLinesWithImplicitFinalLine - when input contains one line and a final newline it should return a collection with one line'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine("$line1\n") == [line1]

        where:
        line1 << ['line1', '']
    }

    def 'splitLinesWithImplicitFinalLine - when input contains two lines and no final newline it should return a collection with two lines'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine("$line1\n$line2") == [line1, line2]

        where:
        line1 << ['line1', ''     ]
        line2 << ['line2', 'line2']
    }

    def 'splitLinesWithImplicitFinalLine - when input contains two lines and a final newline it should return a collection with two lines'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine("$line1\n$line2\n") == [line1, line2]

        where:
        line1 << ['line1', '',      'line1', '']
        line2 << ['line2', 'line2', '',      '']
    }

    def 'splitLinesWithImplicitFinalLine - it should handle Mac line separators'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine('line1\rline2\r') == ['line1', 'line2']
    }

    def 'splitLinesWithImplicitFinalLine - it should handle Windows line separators'() {
        expect:
        StringUtils.splitLinesWithImplicitFinalLine('line1\r\nline2\r\n') == ['line1', 'line2']
    }
}
