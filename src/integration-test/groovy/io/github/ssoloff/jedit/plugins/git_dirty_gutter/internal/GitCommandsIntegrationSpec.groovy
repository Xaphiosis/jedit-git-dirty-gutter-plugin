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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import spock.lang.Specification

class GitCommandsIntegrationSpec extends Specification {
    private def repoPath = createTempDirectory()
    private def gitCommands = new GitCommands(createGitRunner())

    private void addAndCommitFile(Path filePath) {
        runGit('add', filePath)
        runGit('commit', '-m', 'test commit')
    }

    private IGitRunner createGitRunner() {
        new GitRunner(new ProcessRunner(), Paths.get('git'), repoPath)
    }

    private static void createNewFile(Path filePath) {
        def parentPath = filePath.parent
        if (Files.notExists(parentPath)) {
            assert parentPath.toFile().mkdirs()
        }

        filePath.setText('test')
    }

    private Path createTempDirectory() {
        Files.createTempDirectory(this.class.simpleName)
    }

    private Path createTempFile() {
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

    private void initRepo() {
        runGit('init')

        // configure required user properties
        runGit('config', 'user.name', 'TestUser')
        runGit('config', 'user.email', 'TestEmail')

        // create an initial commit so HEAD is present
        def filePath = repoPath.resolve('README')
        createNewFile(filePath)
        addAndCommitFile(filePath)
    }

    private void runGit(Object... args) {
        def gitRunner = createGitRunner()
        gitRunner.run(new StringWriter(), args.each { it.toString() } as String[] )
    }

    def setup() {
        initRepo()
    }

    def cleanup() {
        deleteDirectory(repoPath)
    }

    def 'getRepoRelativePath - when file exists on HEAD it should return repo-relative path'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        createNewFile(filePath)
        addAndCommitFile(filePath)

        when:
        def repoRelativePath = gitCommands.getRepoRelativePath(filePath)

        then:
        repoRelativePath == repoPath.relativize(filePath)
    }

    def 'getRepoRelativePath - when file is inside repo but does not exist on HEAD it should return null'() {
        setup:
        def filePath = repoPath.resolve('subdir1').resolve('file')
        createNewFile(filePath)
        // do not commit so it does not exist on HEAD

        when:
        def repoRelativePath = gitCommands.getRepoRelativePath(filePath)

        then:
        repoRelativePath == null
    }

    def 'getRepoRelativePath - when file is outside repo it should throw an exception'() {
        setup:
        def filePath = createTempFile()

        when:
        def repoRelativePath = gitCommands.getRepoRelativePath(filePath)

        then:
        thrown(GitException)

        cleanup:
        deleteFile(filePath)
    }
}
