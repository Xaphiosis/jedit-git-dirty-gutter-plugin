/*
 * Copyright (C) 2016 Steven Soloff
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
package io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.test

import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.model.IBuffer
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.StringUtils
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.ProcessRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.GitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunner
import io.github.ssoloff.jedit.plugins.git_dirty_gutter.internal.util.process.git.IGitRunnerFactory
import java.nio.file.Path
import java.nio.file.Paths
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

/**
 * Superclass for all integration tests that interact with an external Git
 * program.
 */
class GitIntegrationSpecification extends Specification {
    protected static final COMMAND_ADD = 'add'
    protected static final COMMAND_COMMIT = 'commit'
    protected static final COMMAND_CONFIG = 'config'
    protected static final COMMAND_INIT = 'init'

    @Rule
    protected TemporaryFolder temporaryFolder = new TemporaryFolder()

    protected Path repoPath = null

    protected void addAndCommitFile(Path filePath) {
        runGit(COMMAND_ADD, filePath)
        runGit(COMMAND_COMMIT, '-m', 'test commit')
    }

    protected static Path getGitProgramPath() {
        Paths.get('git')
    }

    private initRepo() {
        repoPath = newTemporaryFolder()

        runGit(COMMAND_INIT)

        // configure required user properties
        runGit(COMMAND_CONFIG, 'user.name', 'TestUser')
        runGit(COMMAND_CONFIG, 'user.email', 'TestEmail')

        // create an initial commit so HEAD is present
        def filePath = repoPath.resolve('README')
        touchFile(filePath)
        addAndCommitFile(filePath)
    }

    protected static IBuffer newBufferForFile(Path filePath) {
        new IBuffer() {
            Path getFilePath() {
                filePath
            }

            List<String> getLines() {
                StringUtils.splitLinesWithExplicitFinalLine(new String(filePath.readBytes()))
            }
        }
    }

    protected IGitRunner newGitRunner() {
        newGitRunnerForRepo(repoPath)
    }

    protected static IGitRunnerFactory newGitRunnerFactory() {
        { workingDirPath -> new GitRunner(new ProcessRunner(), workingDirPath, gitProgramPath) }
    }

    protected static IGitRunner newGitRunnerForRepo(Path repoPath) {
        def gitRunnerFactory = newGitRunnerFactory()
        gitRunnerFactory.createGitRunner(repoPath)
    }

    protected Path newTemporaryFile() {
        temporaryFolder.newFile().toPath()
    }

    protected Path newTemporaryFolder() {
        temporaryFolder.newFolder().toPath()
    }

    protected void runGit(Object... args) {
        def gitRunner = newGitRunner()
        gitRunner.run(new StringWriter(), args.each { it.toString() } as String[] )
    }

    protected static void touchFile(Path filePath, String fileContent='') {
        def parentDir = filePath.parent.toFile()
        if (!parentDir.exists()) {
            assert parentDir.mkdirs()
        }

        filePath.setText(fileContent)
    }

    def setup() {
        initRepo()
    }
}
