package ch.so.agi.ilivalidator.service;

import org.jobrunr.jobs.annotations.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class IlivalidatorJobService {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Job(name = "The sample job without variable")
    public synchronized boolean validate(String inputFilename) {
        log.info("validating: " + inputFilename);
        
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        log.info("after sleep");

        return true;
    }
}
