package gitlet;

import static gitlet.Utils.*;
import java.io.File;
import java.io.Serializable;

public class Blob implements Serializable {

    private String contents;
    private String blobID;

    public String contents() {
        return contents;
    }
    public String blobID() {
        return blobID;
    }
    public Blob(String c) {
        this.contents = c;
        this.blobID = sha1(contents);
    }

    public void save() {
        File saveLocation = join(Repository.BLOBS_DIR, this.blobID);
        writeObject(saveLocation, this);
    }
}
