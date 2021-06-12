package gitlet;

import static gitlet.Utils.*;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Date;
import java.io.File;
import java.io.Serializable;

/** Represents a gitlet commit object.
 *  @author Aaron Soll
 */
public class Commit implements Serializable, Cloneable {
    /** List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     * edit
     * another edit
     * another edit
     * final edit hopefully
     * anotha one
     */

    /** The message of this Commit. */
    private String message;
    private String dateTime;
    private String commitID;
    private String parentHash;
    private String secondParentHash;
    private Hashtable<String, String> filesMap;

    public String message() {
        return message;
    }
    public String commitID() {
        return commitID;
    }
    public String parentHash() {
        return parentHash;
    }
    public String secondParentHash() {
        return secondParentHash;
    }
    public Hashtable<String, String> filesMap() {
        return filesMap;
    }
    public String filesMapGet(String key) {
        return filesMap.get(key);
    }
    public void filesMapPut(String key, String value) {
        filesMap.put(key, value);
    }
    public void filesMapRemove(String key) {
        filesMap.remove(key);
    }

    public Commit(String m, Boolean initialCommit, String ph, String sph) {
        this.message = m;
        this.parentHash = ph;
        this.secondParentHash = sph;

        Date date;
        if (initialCommit) {
            date = new Date(0);
        } else {
            date = new Date();
        }
        this.dateTime = formatDate(date);
        this.commitID = Utils.sha1(dateTime + message + parentHash);

        filesMap = new Hashtable<>();
    }

    @Override
    public String toString() {
        String rv = "commit " + commitID + "\n";
        if (secondParentHash != null) {
            rv += "Merge: " + parentHash.substring(0, 7) + " "
                    + secondParentHash.substring(0, 7) + "\n";
        }
        rv += "Date: " + dateTime + "\n";
        rv += message + "\n";
        return rv;
    }

    public boolean isInitial() {
        return (parentHash == null);
    }


    /** adds all the ancestors of commit to ancestors */
    public HashSet<String> addAllAncestors(HashSet<String> ancestors) {
        ancestors.add(this.commitID());
        if (this.parentHash != null) {
            Commit parent = Repository.readThisCommit(this.parentHash);
            ancestors = parent.addAllAncestors(ancestors);
        }
        if (this.secondParentHash != null) {
            Commit secondParent = Repository.readThisCommit(this.secondParentHash);
            ancestors = secondParent.addAllAncestors(ancestors);
        }

        return ancestors;
    }

    /** uses BFS to find see the latest ancestor of b that is in aAncestors*/
    public Commit bfs(HashSet<String> aAncestors) {
        LinkedList<String> toCheck = new LinkedList<>();
        toCheck.addLast(this.commitID);
        String nextCommitString;
        Commit nextCommit;
        while (!toCheck.isEmpty()) {
            nextCommitString = toCheck.removeFirst();
            nextCommit = Repository.readThisCommit(nextCommitString);
            if (aAncestors.contains(nextCommitString)) {
                return nextCommit;
            }
            if (nextCommit.parentHash != null) {
                toCheck.addLast(nextCommit.parentHash);
            }
            if (nextCommit.secondParentHash != null) {
                toCheck.addLast(nextCommit.secondParentHash);
            }
        }
        return null;
    }

    public boolean condition1(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && a.tracks(fileName) && b.tracks(fileName)
                && this.trackSameVersionOf(fileName, a)) {
            return true;
        }
        return false;
    }

    public boolean condition2(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && a.tracks(fileName) && b.tracks(fileName)
                && this.trackSameVersionOf(fileName, b)) {
            return true;
        }
        return false;
    }

    public boolean condition3a(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && this.trackDifferentVersionsOf(fileName, a)
                && a.trackSameVersionOf(fileName, b)) {
            return true;
        }
        return false;
    }

    public boolean condition3b(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && this.trackDifferentVersionsOf(fileName, a)
                && this.trackDifferentVersionsOf(fileName, b)
                && a.trackDifferentVersionsOf(fileName, b)) {
            return true;
        }
        return false;
    }

    public boolean condition4(Commit a, Commit b, String fileName) {
        if (!this.tracks(fileName) && !b.tracks(fileName)) {
            return true;
        }
        return false;
    }

    public boolean condition5(Commit a, Commit b, String fileName) {
        if (!this.tracks(fileName) && !a.tracks(fileName)) {
            return true;
        }
        return false;
    }

    public boolean condition6(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && a.tracks(fileName) && !b.tracks(fileName)
                && this.trackSameVersionOf(fileName, a)) {
            return true;
        }
        return false;
    }

    public boolean condition7(Commit a, Commit b, String fileName) {
        if (this.tracks(fileName) && !a.tracks(fileName) && b.tracks(fileName)
                && this.trackSameVersionOf(fileName, b)) {
            return true;
        }
        return false;
    }

    /** writes a file that is the result of a merge conflict*/
    public String mergeConflict(String fileName, Commit otherCommit) {
        File location = join(Repository.BLOBS_DIR, this.filesMap.get(fileName));
        String currentContents = readObject(location, Blob.class).contents();

        String otherContents = "";
        if (otherCommit.filesMap().containsKey(fileName)) {
            location = join(Repository.BLOBS_DIR, otherCommit.filesMap().get(fileName));
            otherContents = readObject(location, Blob.class).contents();
        }

        String newContents = "<<<<<<< HEAD\n" + currentContents + "=======\n"
                + otherContents + ">>>>>>>\n";
        Blob mergeConflictBlob = new Blob(newContents);
        mergeConflictBlob.save();

        /* writes new contents to the CWD */
        File fileToWrite = join(Repository.CWD, fileName);
        writeContents(fileToWrite, mergeConflictBlob.contents());

        return mergeConflictBlob.blobID();
    }

    /** returns the blobID of the version of fileName saved in this commit; if
     * no such file is tracked by this commit, returns null
     */
    public String fileID(String fileName) {
        if (!filesMap.containsKey(fileName)) {
            return null;
        }
        return filesMap.get(fileName);
    }

    public Commit child(String mes) {
        Commit newCommit = new Commit(mes, false, this.commitID, null);
        newCommit.filesMap = (Hashtable<String, String>) this.filesMap.clone();
        return newCommit;
    }

    /** creates a new commit that is the result of merging this with otherCommit */
    public Commit mergeChild(Commit otherCommit, String currentBranch, String otherBranch) {
        String mes = "Merged " + otherBranch + " into " + currentBranch + ".";
        Commit newCommit = new Commit(mes, false, this.commitID, otherCommit.commitID);
        newCommit.filesMap = (Hashtable<String, String>) this.filesMap.clone();
        return newCommit;
    }

    public boolean tracks(String fileName) {
        return this.filesMap.containsKey(fileName);
    }

    public boolean trackSameVersionOf(String fileName, Commit otherCommit) {
        if (this.filesMap.get(fileName) == null && otherCommit.filesMap.get(fileName) == null) {
            return true;
        } else if (this.filesMap.get(fileName) == null || otherCommit.filesMap.get(fileName)
                == null) {
            return false;
        } else if (this.filesMap.get(fileName).equals(otherCommit.filesMap.get(fileName))) {
            return true;
        }
        return false;
    }

    public boolean trackDifferentVersionsOf(String fileName, Commit otherCommit) {
        if (this.filesMap.get(fileName) == null && otherCommit.filesMap.get(fileName) != null) {
            return true;
        } else if (this.filesMap.get(fileName) != null && otherCommit.filesMap.get(fileName)
                == null) {
            return true;
        } else {
            return !this.trackSameVersionOf(fileName, otherCommit);
        }
    }

    public String formatDate(Date date) {
        return String.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
    }

    public void save() {
        File saveLocation = join(Repository.COMMITS_DIR, this.commitID);
        Utils.writeObject(saveLocation, this);
    }

}
