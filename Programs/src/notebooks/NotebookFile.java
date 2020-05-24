package notebooks;

import java.util.function.Consumer;

class NotebookFile {
    private static NotebookFile[] directory = new NotebookFile[2800000]; /// FIXME

    public static void register(int id, int notebookNumber) {
        if (NotebookFile.directory[id] != null) return;

        final NotebookFile nb = new NotebookFile(notebookNumber); 
        NotebookFile.directory[id] = nb;
    }

    public static void forAll(Consumer<NotebookFile> f) {
        for (NotebookFile nb : directory) {
            if (nb != null) {
                f.accept(nb);
            }
        }
    }
    
    public static NotebookFile getById(int id) {
        assert (0 <= id && id < NotebookFile.directory.length) : "Illegal repo id " + id;
        assert (NotebookFile.directory[id] != null) : "Tried to lookup non-existing NotebookFile (id=" + id + ")";

        return NotebookFile.directory[id];
    }

    private final int id;
    /// Connections between snippets where inter/intra denotes different/same notebooks
    private int intraConnections = 0;
    private int intraEmptyConnections = 0;
    private int interConnections = 0;
    private int interEmptyConnections = 0;
    /// Connections between snippets where inter/intra denotes different/same repo
    private int intraRepoConnections = 0;
    private int intraRepoEmptyConnections = 0;
    private int interRepoConnections = 0;
    private int interRepoEmptyConnections = 0;

    public Repository getRepo() {
        return Repository.getByNotebookNumber(this.id);
    }
    
    public NotebookFile(int id) {
        this.id = id;
    }

    public void addIntraConnections(boolean empty) {
        addIntraConnections(1, empty);
    }

    public void addInterConnections(boolean empty) {
        addInterConnections(1, empty);
    }

    public void addIntraConnections(int value, boolean empty) {
        if (empty) {
            intraEmptyConnections += value;
        } else {
            intraConnections += value;
        }
    }

    public void addInterConnections(int value, boolean empty) {
        if (empty) {
            interEmptyConnections += value;
        } else {
            interConnections += value;
        }
    }

    public int intraConnections() {
        return intraConnections;
    }

    public int intraEmptyConnections() {
        return intraEmptyConnections;
    }

    public int interConnections() {
        return interConnections;
    }

    public int interEmptyConnections() {
        return interEmptyConnections;
    }

    public void addIntraRepoConnections(boolean empty) {
        addIntraRepoConnections(1, empty);
    }

    public void addInterRepoConnections(boolean empty) {
        addInterRepoConnections(1, empty);
    }

    public void addIntraRepoConnections(int value, boolean empty) {
        if (empty) {
            intraRepoEmptyConnections += value;
        } else {
            intraRepoConnections += value;
        }
    }

    public void addInterRepoConnections(int value, boolean empty) {
        if (empty) {
            interRepoEmptyConnections += value;
        } else {
            interRepoConnections += value;
        }
    }

    public int intraRepoConnections() {
        return intraRepoConnections;
    }

    public int intraRepoEmptyConnections() {
        return intraRepoEmptyConnections;
    }

    public int interRepoConnections() {
        return interRepoConnections;
    }

    public int interRepoEmptyConnections() {
        return interRepoEmptyConnections;
    }

    public String fileName() {
        return new StringBuilder()
            .append("nb_")
            .append(this.id)
            .append(".ipynb")
            .toString();
    }
}
