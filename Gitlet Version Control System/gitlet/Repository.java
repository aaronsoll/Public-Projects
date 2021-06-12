package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  @author Aaron Soll
 */
public class Repository implements Serializable {

    public static final File CWD = new File(System.getProperty("user.dir"));
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File COMMITS_DIR = join(GITLET_DIR, "Commits");
    public static final File BLOBS_DIR = join(GITLET_DIR, "Blobs");
    public static final File REPO_OBJ = join(GITLET_DIR, "Repo");
    private Hashtable<String, String> branches;
    private String currentBranch;
    private Hashtable<String, String> stagingArea;
    private ArrayList<String> deletionArea;


    public Repository(Commit initialCommit) {
        this.branches = new Hashtable<>();
        branches.put("master", initialCommit.commitID());
        branches.put("HEAD", initialCommit.commitID());
        this.currentBranch = "master";
        this.stagingArea = new Hashtable<>();
        this.deletionArea = new ArrayList<>();
    }


    /** STATIC METHODS */

    public static boolean checkIfExists() {
        return GITLET_DIR.exists();
    }

    public static boolean fileExists(String fileName) {
        File checking = join(CWD, fileName);
        return checking.exists();
    }

    public static void createNewRepo() {
        GITLET_DIR.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        Commit initialCommit = new Commit("initial commit", true, null, null);
        Repository repo = new Repository(initialCommit);
        initialCommit.save();
        repo.save();
    }

    /** NONSTATIC METHODS */


    public void addFile(String fileName) {
        File fileToAdd = join(CWD, fileName);
        Blob newBlob = new Blob(readContentsAsString(fileToAdd));
        Commit currentCommit = readCurrentCommit();

        if (stagingArea.containsKey(fileName)
                && currentCommit.fileID(fileName).equals(newBlob.blobID())) {
            /* checks if the version of the file in the CWD is the same as the version in the
            most recent commit*/
            stagingArea.remove(fileName);
        } else if (deletionArea.contains(fileName)) {
            deletionArea.remove(fileName);
        } else if (!stagingArea.containsKey(fileName)
                && newBlob.blobID().equals(currentCommit.fileID(fileName))) {
            /* nothing happens if the file isn't in the staging area yet but its contents are
            already tracked in the most recent commit */
            newBlob = newBlob; //not necessary, just appeasing style check
        } else {
            /* the file is updated in the staging area otherwise */
            this.stagingArea.put(fileName, newBlob.blobID());
        }

        newBlob.save();
        this.save();
    }

    public void newCommit(String m) {
        /*creating clone: */
        Commit lastCommit = readCurrentCommit();
        Commit newCommit = lastCommit.child(m);

        /* making the new commit the head commit and the head of the current branch: */
        branches.put("HEAD", newCommit.commitID());
        branches.put(currentBranch, newCommit.commitID());

        /* adding each file from staging area to the new commit */
        Set<String> additionFiles = stagingArea.keySet();
        for (String fileName : additionFiles) {
            newCommit.filesMapPut(fileName, stagingArea.get(fileName));
        }
        stagingArea.clear();

        /* removing each file that is in the deletion area from the new commit */
        for (String fileName : deletionArea) {
            if (newCommit.tracks(fileName)) {
                newCommit.filesMapRemove(fileName);
            }
        }
        deletionArea.clear();

        this.save();
        newCommit.save();
    }

    public void remove(String fileName) {
        /* prints error message if file is neither in staging area nor is it tracked */
        Commit currentCommit = readCurrentCommit();
        if (!this.stagingArea.containsKey(fileName) && !currentCommit.tracks(fileName)) {
            exitWithError("No reason to remove this file.");
        }

        /* removes file from staging area */
        this.stagingArea.remove(fileName);

        /* adds file to deletion area and deletes it from CWD */
        File toBeRemoved = join(CWD, fileName);
        if (currentCommit.tracks(fileName)) {
            this.deletionArea.add(fileName);
            restrictedDelete(toBeRemoved);
        }

        currentCommit.save();
        this.save();
    }

    public void printLog() {
        printLogRecursiveHelper(branches.get("HEAD"));
    }

    private void printLogRecursiveHelper(String commitID) {
        Commit commitToPrint = readThisCommit(commitID);
        System.out.println("===");
        System.out.println(commitToPrint.toString());

        if (!commitToPrint.isInitial()) {
            printLogRecursiveHelper(commitToPrint.parentHash());
        }
    }

    public void printGlobalLog() {
        List<String> commitIDs = plainFilenamesIn(COMMITS_DIR);
        Commit commitToPrint;
        for (String commitID : commitIDs) {
            commitToPrint = readThisCommit(commitID);
            System.out.println("===");
            System.out.println(commitToPrint.toString());
        }
    }

    public void find(String message) {
        List<String> commitIDs = plainFilenamesIn(COMMITS_DIR);
        Commit commitToCheck;
        boolean prant = false;
        for (String commitID : commitIDs) {
            commitToCheck = readThisCommit(commitID);
            if (commitToCheck.message().equals(message)) {
                System.out.println(commitID);
                prant = true;
            }
        }
        if (!prant) {
            System.out.println("Found no commit with that message.");
        }
    }

    public void status() {
        System.out.println("=== Branches ===");
        Set<String> keySetB = branches.keySet();
        PriorityQueue<String> orderedBranches = new PriorityQueue<>();
        orderedBranches.addAll(keySetB);
        for (String branch : orderedBranches) {
            if (branch.equals(currentBranch)) {
                System.out.print("*");
            }
            if (!branch.equals("HEAD")) {
                System.out.println(branch);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        Set<String> keySetS = stagingArea.keySet();
        PriorityQueue<String> orderedStagingArea = new PriorityQueue<>();
        orderedStagingArea.addAll(keySetS);
        for (String file : orderedStagingArea) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        deletionArea.sort(String::compareTo);
        for (String file : deletionArea) {
            System.out.println(file);
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        System.out.println();

        System.out.println("=== Untracked Files ===");
        System.out.println();
    }

    public void checkoutFileFromHead(String fileName) {
        checkoutFileFromCommit(this.branches.get("HEAD"), fileName);
    }

    public void checkoutFileFromCommit(String commitID, String fileName) {
        List<String> commitIDs = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String checkingThisID : commitIDs) {
            if (startsWith(checkingThisID, commitID)) {
                commitID = checkingThisID;
                found = true;
            }
        }
        if (!found) {
            exitWithError("No commit with that id exists.");
        }

        Commit appropriateCommit = readThisCommit(commitID);
        if (!appropriateCommit.tracks(fileName)) {
            exitWithError("File does not exist in that commit.");
        }

        String desiredBlobID = appropriateCommit.filesMapGet(fileName);
        Blob desiredBlob = readThisBlob(desiredBlobID);

        File fileToWrite = join(CWD, fileName);
        writeContents(fileToWrite, desiredBlob.contents());
    }

    public void checkoutBranch(String branchName) {
        /* checks and errors if there is a file in CWD untracked in the HEAD */
        List<String> filesInCWD = plainFilenamesIn(CWD);
        Commit headCommit = readHeadCommit();
        if (filesInCWD != null) {
            for (String file : filesInCWD) {
                if (!headCommit.tracks(file)) {
                    exitWithError("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }

        /* checks if branch is valid  */
        if (!branches.containsKey(branchName)) {
            exitWithError("No such branch exists.");
        } else if (branchName.equals(currentBranch)) {
            exitWithError("No need to checkout the current branch.");
        }

        /* checks out each file in the given branch */
        Commit headOfGivenBranch = readThisCommit(branches.get(branchName));
        Set<String> fileNamesInGivenBranch = headOfGivenBranch.filesMap().keySet();
        for (String fileName : fileNamesInGivenBranch) {
            checkoutFileFromCommit(headOfGivenBranch.commitID(), fileName);
        }

        /* iterates through each file in the HEAD commit, deleting them from CWD
        if untracked not tracked by given commit */
        Set<String> fileNamesInHEAD = headCommit.filesMap().keySet();
        File fileToDelete;
        for (String fileName : fileNamesInHEAD) {
            if (!headOfGivenBranch.tracks(fileName)) {
                fileToDelete = join(CWD, fileName);
                restrictedDelete(fileToDelete);
            }
        }

        /* clears staging area and updates HEAD */
        stagingArea.clear();
        deletionArea.clear();
        branches.put("HEAD", branches.get(branchName));
        currentBranch = branchName;
        this.save();
    }

    public void addBranch(String branchName) {
        if (branches.containsKey(branchName)) {
            exitWithError("A branch with that name already exists.");
        }
        String currentHead = branches.get("HEAD");
        branches.put(branchName, currentHead);
        this.save();
    }

    public void removeBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            exitWithError("A branch with that name does not exist.");
        } else if (branchName.equals(currentBranch)) {
            exitWithError("Cannot remove the current branch.");
        }

        branches.remove(branchName);
        this.save();
    }

    public void reset(String commitID) {
        /* checks if the commit exists */
        List<String> commits = plainFilenamesIn(COMMITS_DIR);
        boolean found = false;
        for (String commit : commits) {
            if (startsWith(commit, commitID)) {
                commitID = commit;
                found = true;
                continue;
            }
        }
        if (!found) {
            exitWithError("No commit with that id exists.");
        }

        assertNoUntrackedFiles();

        /* checks out files from given commit */
        Commit resetCommit = readThisCommit(commitID);
        Set<String> trackedFiles = resetCommit.filesMap().keySet();
        for (String file : trackedFiles) {
            checkoutFileFromCommit(commitID, file);
        }

        /* deletes files  in CWD untracked by given commit */
        List<String> filesInCWD = plainFilenamesIn(CWD);
        filesInCWD = plainFilenamesIn(CWD);
        File fileToDelete;
        if (filesInCWD != null) {
            for (String file : filesInCWD) {
                if (!resetCommit.tracks(file)) {
                    fileToDelete = join(CWD, file);
                    restrictedDelete(fileToDelete);
                }
            }
        }

        /* clears staging area and updates HEAD and current branch */
        stagingArea.clear();
        deletionArea.clear();
        branches.put(currentBranch, commitID);
        branches.put("HEAD", commitID);
        this.save();
    }

    public void merge(String otherBranchName) {
        Commit thisCommit = readCurrentCommit();
        Commit otherCommit = readThisCommit(branches.get(otherBranchName));
        Commit splitPoint = findSplitPoint(thisCommit, otherCommit);
        assertNoSpecialCases(splitPoint, thisCommit, otherCommit, otherBranchName);

        Set<String> fileNames = addAllFilesFrom(thisCommit, otherCommit, splitPoint);
        Commit mergeCommit = thisCommit.mergeChild(otherCommit, currentBranch, otherBranchName);

        boolean mergeConflict = false;
        for (String file : fileNames) {
            if (splitPoint.condition1(thisCommit, otherCommit, file)) {
                checkoutFileFromCommit(otherCommit.commitID(), file);
                addFile(file);
            } else if (splitPoint.condition2(thisCommit, otherCommit, file)) {
                /* nothing happens since the file should stay as is */
                file = file;
            } else if (splitPoint.condition3a(thisCommit, otherCommit, file)) {
                /* nothing happens since the file should stay as is */
                file = file;
            } else if (splitPoint.condition3b(thisCommit, otherCommit, file)) {
                mergeConflict = true;
                String newBlobID = thisCommit.mergeConflict(file, otherCommit);
                this.stagingArea.put(file, newBlobID);
            } else if (splitPoint.condition4(thisCommit, otherCommit, file)) {
                /* nothing happens since the file should stay as is */
                file = file;
            } else if (splitPoint.condition5(thisCommit, otherCommit, file)) {
                checkoutFileFromCommit(otherCommit.commitID(), file);
                addFile(file);
            } else if (splitPoint.condition6(thisCommit, otherCommit, file)) {
                remove(file);
            } else if (splitPoint.condition7(thisCommit, otherCommit, file)) {
                /* nothing happens since the file should remain removed */
                file = file;
            }
        }

        Main.assertStagingAreaIsNotEmpty(this);
        if (mergeConflict) {
            System.out.println("Encountered a merge conflict.");
        }

        /* making the new commit the head commit and the head of the current branch: */
        branches.put("HEAD", mergeCommit.commitID());
        branches.put(currentBranch, mergeCommit.commitID());

        /* adding each file from staging area to the new commit */
        Set<String> additionFiles = stagingArea.keySet();
        for (String fileName : additionFiles) {
            mergeCommit.filesMapPut(fileName, stagingArea.get(fileName));
        }
        stagingArea.clear();

        /* removing each file that is in the deletion area from the new commit */
        for (String fileName : deletionArea) {
            if (mergeCommit.tracks(fileName)) {
                mergeCommit.filesMapRemove(fileName);
            }
        }
        deletionArea.clear();

        this.save();
        mergeCommit.save();
    }







    /** HELPER FUNCTIONS */


    /** reads the commit with the given ID */
    public static Commit readThisCommit(String commitID) {
        File thisCommitFile = join(COMMITS_DIR, commitID);
        Commit thisCommit = readObject(thisCommitFile, Commit.class);
        return thisCommit;
    }

    /** reads the blob with the given ID */
    public Blob readThisBlob(String blobID) {
        File thisBlobFile = join(BLOBS_DIR, blobID);
        Blob thisBlob = readObject(thisBlobFile, Blob.class);
        return thisBlob;
    }

    /** reads the most recent commit from the current branch */
    public Commit readCurrentCommit() {
        File currentCommitFile = join(COMMITS_DIR, branches.get(currentBranch));
        Commit currentCommit = readObject(currentCommitFile, Commit.class);
        return currentCommit;
    }

    /** reads the HEAD commit */
    public Commit readHeadCommit() {
        File headCommitFile = join(COMMITS_DIR, branches.get("HEAD"));
        Commit headCommit = readObject(headCommitFile, Commit.class);
        return headCommit;
    }

    /** finds and returns the latest common ancestor of the two input commits recursively*/
    private Commit findSplitPoint(Commit a, Commit b) {
        HashSet<String> aAncestors = new HashSet<>();
        aAncestors = a.addAllAncestors(aAncestors);
        return b.bfs(aAncestors);
    }

    /** adds all filenames to masterSet from the three given commits */
    private static Set<String> addAllFilesFrom(Commit a, Commit b, Commit c) {
        Set<String> masterSet = new HashSet<>();
        Set<String> aSet = a.filesMap().keySet();
        Set<String> bSet = b.filesMap().keySet();
        Set<String> cSet = c.filesMap().keySet();

        if (!aSet.isEmpty()) {
            for (String file : aSet) {
                masterSet.add(file);
            }
        }
        if (!bSet.isEmpty()) {
            for (String file : bSet) {
                masterSet.add(file);
            }
        }
        if (!cSet.isEmpty()) {
            for (String file : cSet) {
                masterSet.add(file);
            }
        }

        return masterSet;
    }

    public boolean stagingAreaIsEmpty() {
        return stagingArea.size() == 0;
    }

    public boolean deletionAreaIsEmpty() {
        return deletionArea.size() == 0;
    }

    public void assertNothingStaged() {
        if (!stagingAreaIsEmpty() || !deletionAreaIsEmpty()) {
            exitWithError("You have uncommitted changes.");
        }
    }

    public void assertBranchExists(String branch) {
        if (!branches.containsKey(branch)) {
            exitWithError("A branch with that name does not exist.");
        }
    }

    public void assertDifferentBranches(String branch) {
        if (currentBranch.equals(branch)) {
            exitWithError("Cannot merge a branch with itself.");
        }
    }

    public void assertNoUntrackedFiles() {
        List<String> filesInCWD = plainFilenamesIn(CWD);
        Commit currentCommit = readCurrentCommit();
        if (filesInCWD != null) {
            for (String file : filesInCWD) {
                if (!currentCommit.tracks(file) && !stagingArea.containsKey(file)) {
                    exitWithError("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                }
            }
        }
    }

    private void assertNoSpecialCases(Commit splitPoint, Commit thisCommit,
                                      Commit otherCommit, String branchName) {
        if (splitPoint.commitID().equals(thisCommit.commitID())) {
            checkoutBranch(branchName);
            exitWithError("Current branch fast-forwarded.");
        } else if (splitPoint.commitID().equals(otherCommit.commitID())) {
            exitWithError("Given branch is an ancestor of the current branch");
        }
    }

    public boolean startsWith(String full, String partial) {
        return full.substring(0, partial.length()).equals(partial);
    }

    public boolean commitExists(String commitID) {
        List<String> commits = plainFilenamesIn(COMMITS_DIR);
        for (String commit : commits) {
            if (startsWith(commit, commitID)) {
                return true;
            }
        }
        return false;
    }

    public void save() {
        Utils.writeObject(REPO_OBJ, this);
    }

}
