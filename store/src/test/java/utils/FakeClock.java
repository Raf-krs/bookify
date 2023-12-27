package utils;

import com.demo.shared.clock.Clock;

import java.time.Duration;
import java.time.LocalDateTime;

public class FakeClock implements Clock {
    private LocalDateTime time;

    public FakeClock(LocalDateTime time) {
        this.time = time;
    }

    public FakeClock() {
        this(LocalDateTime.now());
    }

    @Override
    public LocalDateTime now() {
        return time;
    }

    public void tick(Duration duration) {
        time = time.plus(duration);
    }
}