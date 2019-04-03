package hello;

public class Job {

    private final long id;
    private final String name;
    private int status;

    public Job(long id, String name) {
        this.id = id;
        this.name = name;
        this.status = 0;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void progress() {
        this.status += 10;
    }
}
