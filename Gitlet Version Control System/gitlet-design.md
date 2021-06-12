# Gitlet Design Document

**Name**: Aaron Soll

---

## Classes and Data Structures

### Repository

#### Instance Variables

1. (Hashtable) branches: this is a mapping of each (String) branchName 
   --> (String) commitID. branches is initialized with the repository 
   with Master and HEAD entries
   
2. (String) currentBranch: this is the branchName of the branch 
   that the user is currently in 

3. (Hashtable) stagingArea: this is a mapping of each (String) fileName 
    --> (String) blobID
   
4. (List) deletionArea: this is a list of each (String) fileName to be
    deleted from the next commit
   
5. (File) CWD: this is the CWD file

6. (File) GITLET_DIR: this is the .gitlet folder as a File Object in Java

7. (File) COMMITS_DIR: this is the Commits folder within .gitlet

8. (File) BLOBS_DIR: this is the Blobs folder within .gitlet

#### Primary Methods

- static boolean checkIfExists(): returns if CWD/.gitlet exists

- static boolean fileExists(String fileName): returns if fileName exists in 
  the CWD
  
- static void createNewRepo(): makes all approprate gitlet directories, then 
  initializes the first commit, initializes the repository, and writes 
  it to .gitlet/Repo

- void addFile(String fileName): adds file to staging area after performing 
  correct checks to current commit and CWD
  
- void newCommit(String m): creates a clone of the most recent commit, updating 
  relevant information, makes this new commit the head commit, then adds files 
  from the staging area and removes the files listed in the deletion area
  
- void remove(String fileName): removes file from staging area, adds it to deletions 
  area, and deletes it from CWD
  
- void printLog(): calls printLogRecursiveHelper, starting at the HEAD commit

- void printLogRecursiveHelper(String commitID) prints the toString() of the input 
  commit and calls itself on the current commit's parent
  
- void printGlobalLog(): prints the toString() of all commits in .gitlet/Commits

- void find(String message): searches through .gitlet/Commits and prints the commitID 
  of each commit whose message is the input message
  
- void status(): prints branches, staged files, and removed files as specified in spec

- void checkoutFile(String, FileName): 

- void checkoutFileFromCommit(String fileName, String commitID):

- void checkoutBranch(String branchName): 

#### Helper Methods

- Commit readCurrentCommit(): returns the current commit object

- Commit readThisCommit(String commitID): returns the commit object with the given ID

- Commit readHEADCommit(): returns the HEAD commit object

- boolean stagingAreaIsEmpty(): returns if the staging area is empty

- void save(): saves the repository to REPO_OBJ

### Commit

#### Instance Variables

1. (String) dateTime: this is implemented by using 
   Java.util.Date and java.util.Formatter, and possibly toString()
   
2. (String) commitID: this is simply the sha1 hash of dateTime, message, 
   and parentHash strings concatenated together (since they are invariant)
   
3. (String) parentHash: this is the hashID of the parent commit

4. (String) secondParentHash: this is the hashID of the secondary parent
    commit if this commit is the result of a merge; the secondary
    - this variable is null for unmerged commits
   - the first parent branch is where the merge occurred, the second is
     that of the merged in branch
   
5. (Hashtable) filesMap: this is a mapping of each (String) file name --> 
   (String) blobID for the current commit
   
6. (String) message: the message corresponding with each commit
   

#### Methods

- String toString(): prints the relevant commit information as outlined in
   the log section of the spec, including a === at the beginning and a new
   line at the end
   - if secondParentHash is not null, this means that there must be an extra
    line (see log section of spec)
     
- String fileID(String FileName): returns the blobID of the version of 
  fileName saved in this commit; if no such file is tracked by this commit, 
  returns null
  
- Commit child(String message): returns a child of this with the input message, 
  copying over the filesMap of this

- void save(): writes the commit to .gitlet/Commits 

### Blob

#### Instance Variables

1. (String) contents: the actual contents of the file that references
   this blob
   
2. (String) blobID: the sha1 hash of contents

#### Methods

-

- void save(): writes the blob to .gitlet/Blobs

---

## Algorithms

### init

- checks if .gitlet already exists, and if so, prints error message

- creates new Repository using Repository constructor and creates 
  initial commit using the Commit constructor
  
### add

- checks if file exists in CWD; if not, prints error message

- creates a blob for the new file
  
- checks if fileName is in staging area already AND checks if blob
hash exists under the name fileName in the most recent commit
    - if the former is true but not the latter, the staging area file is
    updated
      
    - if the latter is true but not the former, nothing happens
      
    - if both are true, the file is *removed* from the staging area
    
- Repo (and new blob if relevant) are saved
    
### commit

- current head commit is read and cloned using the Commit.child method, 
  which automatically populates the Commit with accurate dateTime, commitID, 
  parentHash, and message
  
- head is reassigned based on new commitID
    - the currentBranch is also found in branches and reassigned to
    new commitID
  
- each element in stagingArea is mapped to its corresponding blobID in 
the filesMap of this new commit
  
- each element in deletionArea is removed from filesMap

- stagingArea and deletionArea are cleared 

- Repo and newCommit are saved

### rm

- checks if file is in stagingArea and then removes it if necessary; 
  then checks if file is tracked in the current commit and adds it 
  to deletionArea if necessary
    - if neither are true, error message is printed
    
### log

- prints the toString() of the current commit

- recursively prints the toString() of parent commits until the original 
  commit is reached
  
### global-log

- prints the toString() of each commit in .gitlet/Commits using 
  Utils.plainFilenamesIn(File dir)
  
### find

- prints the commitID of each commit in .gitlet/Commits using 
  Utils.plainFilenamesIn(File dir)

### status

- prints appropriate headers and extra lines before while doing each of the 
  following tasks
  
- prints each branchName from branches, printing an asterisk before if branchName 
  is the same as currentBranch
  
- prints each fileName from stagingArea

- prints each element from deletionArea

- **Modifications Not Staged For Commit and Untracked Files should both be done 
  at the very end if there is time**

### checkout

- if args[1] == "--": 
    - checks if fileName exists in the head commit's filesMap; if not, prints 
      error message
    - if fileName does exist in the head commit's filesMap, reads that blob from 
      .gitlet/Blobs and writes its contents to CWD/filename
      
- if args[2] == "--":
  - if args[1] is not in .gitlet/Commits 
    (can be checked using Utils.plainFilenamesIn(File dir)) or args[3] does not exist 
    in this commit's filesMap, prints appropriate error message
  - otherwise reads the blob referenced from the commit titled args[1] titled args[3] 
    and writes it to CWD/args[3] 
    
- otherwise:
    - prints error message if args[1] is not in branches
    - prints error message if args[1] is the same as currentBranch
    - prints error message if there is a file in CWD that is not in branches["HEAD"]
    - if no error message is prant, reads each file in the commit branches[args1] and 
      writes their contents to files of the same name in CWD       
    - then checks all files names in the commit with title branches["HEAD"] and deletes 
      files whose names are not in the commit with title branches[args1]
    - then changes currentBranch and maps branches["HEAD"] to branches[args1]
    - then clears stagingArea and deletionArea (**Double check if deletionArea needs to be 
      cleared as well**)
      

### branch

- prints an error message if args[1] is already in branches
  
- adds an element to branches with key args[1] and value branches["HEAD"]

### rm-branch

- prints an error message if args[1] is not a key in branches

- prints an error message if currentBranch == args[1]

- otherwise, removes args[1] from branches 

### reset

- prints an error message if args[1] is not in .gitlet/Commits

- prints an error message if there is a file name in CWD that is not tracked in 
  branches[currentBranch]'s filesMap

- runs the same function used in section two of checkout on each file in the 
  args[1] commit
  
- runs the same function used in section two to remove files that are not in \
  present in args[1]
  
- sets branches["HEAD"] to args[1]

### merge
  
- checks for errors and prints messages first
  
- finds split point using BFS
    - first makes a list of all the ancestors of the given branch
    - then does a BFS of the current branch's ancestors, checking each time if 
      they are in the list of the given branch's ancestors (the first one to 
      succeed is the split point)
      
- creates a new commit with the appropriate parent hash IDs and the appropriate toString()

- checks each of the seven conditions to see what to do in the new commit for each file in 
  each of the two parent commits
  
- updates the current branch


## Persistence

Within .gitlet, there are 2 folders and a file:

####Repo

- can be found in .gitlet/Repo

- a file that is read and rewritten everytime gitlet.Main is called 
  using readObject and writeObject (contains all info that is stored 
  in the Repository object)
  
####Blobs

- can be found in .gitlet/Blobs

- a folder that contains all blobs that might be referenced by Gitlet

- each blob is named blobID, and the contents are written to each
blob using writeContents(contents)
  
####Commits

- can be found in .gitlet/Commits

- a folder that contains all commits for the repository

- each commit is named commitID, and the contents are written to each
commit using writeObject and readObject


