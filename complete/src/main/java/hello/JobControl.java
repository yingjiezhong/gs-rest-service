package hello;

import io.jmnarloch.spring.boot.rxjava.async.ObservableSseEmitter;
import io.reactivex.Observable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@RequestMapping("/job")
@RestController
public class JobControl {

    private ExecutorService executor = Executors.newCachedThreadPool();

    private final AtomicLong counter = new AtomicLong();
    private ConcurrentMap<String, Job> jobConcurrentMap = new ConcurrentHashMap<>();

    @GetMapping
    public Job getJob(@RequestParam(value="name", defaultValue="World") String name) {
        return new Job(counter.incrementAndGet(), name);
    }

    @GetMapping("/observable")
    public Observable<Job> getJobs() {

        return Observable.just(
                new Job(counter.incrementAndGet(), "observable_job_1"),
                new Job(counter.incrementAndGet(), "observable_job_2")
        );
    }

    @GetMapping("/osse")
    public ObservableSseEmitter<String> getString() {
        return new ObservableSseEmitter<String>(
                Observable.just(
                        "message 1", "message 2", "message 3"
                )
        );
    }

    @GetMapping("/rbe")
    public ResponseEntity<ResponseBodyEmitter> handleRbe() throws URISyntaxException {
        ResponseBodyEmitter emitter = new ResponseBodyEmitter();
        executor.execute(() -> {
            try {
                emitter.send(
                        "/rbe" + " @ " + new Date(), MediaType.TEXT_PLAIN);
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });

        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlAllowOrigin("*");
        ResponseEntity<ResponseBodyEmitter> entity = new ResponseEntity<>(emitter, headers, HttpStatus.OK);
        return entity;
    }

    @GetMapping("/sseInResponseEntity")
    public ResponseEntity<SseEmitter> handleSseInEntity() {
        SseEmitter emitter = new SseEmitter();
        executor.execute(() -> {
            try {
                emitter.send("/sseInResponseEntity" + " @ " + new Date());
                // we could send more events
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        HttpHeaders headers = new HttpHeaders();
        headers.setAccessControlAllowOrigin("*");
        ResponseEntity<SseEmitter> entity = new ResponseEntity<>(emitter, headers, HttpStatus.OK);
        return entity;
    }

    @GetMapping("/sse")
    public SseEmitter handleSse() {
        SseEmitter emitter = new SseEmitter();
        executor.execute(() -> {
            try {
                emitter.send("/sse" + " @ " + new Date());
                // we could send more events
                emitter.complete();
            } catch (Exception ex) {
                emitter.completeWithError(ex);
            }
        });
        return emitter;
    }

    @GetMapping("/running")
    public ResponseEntity<Job> getJobStatus(@RequestParam(value="id", defaultValue="0") String id) {
        Job job = jobConcurrentMap.get(id);
        if (job == null) {

        } else {
            job.progress();
        }
        return new ResponseEntity<>(job, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> postJob(@RequestBody Job job) {
        ResponseEntity<Job> entity;
        if (job.getName() != null) {
            job.progress();
            jobConcurrentMap.put(String.valueOf(job.getId()), job);
            HttpHeaders headers = new HttpHeaders();
            try {
                headers.setLocation(new URI("http://localhost:8080/job/running"));
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            entity = new ResponseEntity<>(job, headers, HttpStatus.ACCEPTED);
        } else {
            job.setStatus(-1);
            entity = new ResponseEntity<>(job, HttpStatus.BAD_REQUEST);
        }

        return entity;
    }
}
