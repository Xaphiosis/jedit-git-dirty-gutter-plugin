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

import common.io.ProcessExecutor
import git.GitCommand
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import spock.lang.Specification

class GetRepoRelativePathGitCommandIntegSpec extends Specification {
    private def repoPath = initRepo()

    private static void addAndCommitFile(Path filePath) {
        executeGitCommand(filePath.parent, 'add', filePath)
        executeGitCommand(filePath.parent, 'commit', '-m', 'test commit')
    }

    private static def createCommandUnderTest(Path filePath) {
        assert filePath.isAbsolute()
        new GetRepoRelativePathGitCommand(filePath, Paths.get('git'))
    }

    private static void createNewFile(Path filePath) {
        def parentPath = filePath.parent
        if (Files.notExists(parentPath)) {
            assert parentPath.toFile().mkdirs()
        }

        filePath.setText('test')
    }

    private static Path createTempDirectory() {
        Files.createTempDirectory(this.class.simpleName)
    }

    private static Path createTempFile() {
        Files.createTempFile(this.class.simpleName, null)
    }

    private static void deleteDirectory(Path dirPath) {
        if ((dirPath != null) && !dirPath.deleteDir()) {
            System.err.println "failed to delete directory $dirPath"
        }
    }

    private static void deleteFile(Path filePath) {
        if ((filePath != null) && !Files.deleteIfExists(filePath)) {
            System.err.println "failed to delete file $filePath"
        }
    }

    private static void executeGitCommand(Path workingDirPath, Object... args) {
        def cmdline = ['git']
        args.each { cmdline << it.toString() }
        def executor = new ProcessExecutor(cmdline as String[])
        executor.setDirectory(workingDirPath.toString())

        def command = new GitCommand(executor)
        def exitCode = command.execute()
        if (exitCode != 0) {
            System.err.println "Git command: '${command.executor.cmd}'"
            System.err.println "      error: ${command.error}"
        }
        assert exitCode == 0
    }

    private static Path initRepo() {
        def repoPath = createTempDirectory()
        executeGitCommand(repoPath, 'init')

        // create an initial commit so HEAD is present
        def filePath = repoPath.resolve('README')
        createNewFile(filePath)
        addAndCommitFile(filePath)

        repoPath
    }

    def cleanup() {
        deleteDirectory(repoPath)
    }

    def 'when file exists on HEAD it should succeed and set repo-relative path'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        createNewFile(filePath)
        addAndCommitFile(filePath)
        def command = createCommandUnderTest(filePath)

        when:
        def exitCode = command.execute()

        then:
        exitCode == 0
        command.getRepoRelativePath() == repoPath.relativize(filePath)
    }

    def 'when file is inside repo but does not exist on HEAD it should succeed and set repo-relative path to null'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        createNewFile(filePath)
        // do not commit so it does not exist on HEAD
        def command = createCommandUnderTest(filePath)

        when:
        def exitCode = command.execute()

        then:
        exitCode == 0
        command.getRepoRelativePath() == null
    }

    def 'when file is outside repo it should fail and set repo-relative path to null'() {
        setup:
        def filePath = createTempFile()
        def command = createCommandUnderTest(filePath)

        when:
        def exitCode = command.execute()

        then:
        exitCode != 0
        command.getRepoRelativePath() == null

        cleanup:
        deleteFile(filePath)
    }
}
