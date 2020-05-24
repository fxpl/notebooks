package notebooks;

import java.util.HashMap;

public class Repository {
    private static HashMap<Integer, Repository> urlRepo = new HashMap<Integer, Repository>();
    private static HashMap<Integer, Repository> notebookRepo = new HashMap<Integer, Repository>();

    public static void register(int nb, String url) {
        Repository r = getByURL(url);

        if (r == null) {
            r = new Repository(); /// Todo: should we save url?
            Repository.urlRepo.put(url.hashCode(), r); 
        }
        
        Repository.notebookRepo.put(nb, r);
    }
    
    public static Repository getByNotebookNumber(int nb) {
        return Repository.notebookRepo.get(nb);
    }
    
    public static Repository getByURL(String url) {
        return Repository.urlRepo.get(url.hashCode());
    }
    
    private Repository() {} 
    
    private int intraConnections = 0;
    private int intraEmptyConnections = 0;
    private int interConnections = 0;
    private int interEmptyConnections = 0;

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
    
    public void addIntraConnections(boolean empty) {
        addIntraConnections(1, empty);
    }

    public void addInterConnections(boolean empty) {
        addInterConnections(1, empty);
    }

    public void addIntraConnections(int value, boolean empty) {
        if (empty) {
            ++intraEmptyConnections;
        } else {
            ++intraConnections;
        }
    }

    public void addInterConnections(int value, boolean empty) {
        if (empty) {
            interEmptyConnections += value;
        } else {
            interConnections += value;
        }
    }
}
