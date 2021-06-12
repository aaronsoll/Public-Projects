package gitlet;

import java.io.File;
import static gitlet.Utils.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author Aaron Soll
 */


// GRAND TODO LIST: allow for commitID shortening, plan/do merge,

public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        File cwd = new File(System.getProperty("user.dir"));
        File gitletDir = join(cwd, ".gitlet");
        File repoObj = join(gitletDir, "Repo");
        Repository repo = null;
        if (Repository.checkIfExists()) {
            repo = readObject(repoObj, Repository.class);
        }
        assertNonemptyArgs(args);
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                assertUninitializedDirectory();
                assertNumArgs(1, args);
                Repository.createNewRepo();
                break;
            case "add":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                String fileName = args[1];
                if (!Repository.fileExists(fileName)) {
                    exitWithError("File does not exist.");
                }
                repo.addFile(fileName);
                break;
            case "commit":
                assertCommitConditions(args, repo);
                repo.newCommit(args[1]);
                break;
            case "rm":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                repo.remove(args[1]);
                break;
            case "log":
                assertInitializedDirectory();
                assertNumArgs(1, args);
                repo.printLog();
                break;
            case "global-log":
                assertInitializedDirectory();
                assertNumArgs(1, args);
                repo.printGlobalLog();
                break;
            case "find":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                repo.find(args[1]);
                break;
            case "status":
                assertInitializedDirectory();
                assertNumArgs(1, args);
                repo.status();
                break;
            case "checkout":
                assertInitializedDirectory();
                if (args.length == 3 && "--".equals(args[1])) {
                    assertNumArgs(3, args);
                    repo.checkoutFileFromHead(args[2]);
                } else if (args.length == 4 && "--".equals(args[2])) {
                    assertNumArgs(4, args);
                    repo.checkoutFileFromCommit(args[1], args[3]);
                } else {
                    assertNumArgs(2, args);
                    repo.checkoutBranch(args[1]);
                }
                break;
            case "branch":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                repo.addBranch(args[1]);
                break;
            case "rm-branch":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                repo.removeBranch(args[1]);
                break;
            case "reset":
                assertInitializedDirectory();
                assertNumArgs(2, args);
                repo.reset(args[1]);
                break;
            case "merge":
                assertMergeConditions(args, repo);
                repo.merge(args[1]);
                break;
            default:
                exitWithError("No command with that name exists.");

        }
    }

    private static void assertCommitConditions(String[] args, Repository repo) {
        assertInitializedDirectory();
        assertNumArgs(2, args);
        assertStagingAreaIsNotEmpty(repo);
        assertCommitMessage(args[1]);
    }
    private static void assertMergeConditions(String[] args, Repository repo) {
        assertInitializedDirectory();
        assertNumArgs(2, args);
        repo.assertNothingStaged();
        repo.assertBranchExists(args[1]);
        repo.assertDifferentBranches(args[1]);
        repo.assertNoUntrackedFiles();
    }

    private static void assertCommitMessage(String s) {
        if (s.length() == 0) {
            exitWithError("Please enter a commit message");
        }
    }

    public static void assertStagingAreaIsNotEmpty(Repository repo) {
        if (repo.stagingAreaIsEmpty() && repo.deletionAreaIsEmpty()) {
            exitWithError("No changes added to the commit.");
        }
    }

    private static void assertNumArgs(int expected, String[] args) {
        if (expected != args.length) {
            exitWithError("Incorrect operands.");
        }
    }

    private static void assertInitializedDirectory() {
        if (!Repository.checkIfExists()) {
            exitWithError("Not in an initialized Gitlet directory.");
        }
    }

    private static void assertUninitializedDirectory() {
        if (Repository.checkIfExists()) {
            exitWithError("A Gitlet version-control system already exists in the current "
                    + "directory.");
        }
    }

    private static void assertNonemptyArgs(String[] args) {
        if (args.length == 0) {
            exitWithError("Please enter a command.");
        }
    }
}
