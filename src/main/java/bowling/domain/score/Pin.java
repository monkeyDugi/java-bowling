package bowling.domain.score;

import static bowling.domain.state.finish.Spare.SPARE_DESC_STRING;

import bowling.exception.score.PinRangeException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class Pin {

    public static final int PIN_MIN_VALUE = 0;
    public static final int PIN_MAX_VALUE = 10;

    public static final String DESC_DELIMITER = "|";

    private static final Map<Integer, Pin> pins = new HashMap<>();

    private final int pin;

    static {
        IntStream.rangeClosed(PIN_MIN_VALUE, PIN_MAX_VALUE)
            .forEach(number -> pins.put(number, new Pin(number)));
    }

    private Pin(int pin) {
        this.pin = pin;
    }

    public static Pin of(int pin) {
        checkPinRange(pin);

        return pins.get(pin);
    }

    private static void checkPinRange(int pin) {
        if (!pins.containsKey(pin)) {
            throw new PinRangeException();
        }
    }

    public int sum(Pin pin) {
        return Math.addExact(this.pin, pin.pin);
    }

    public boolean isStrike() {
        return pin == PIN_MAX_VALUE;
    }

    public boolean isSpare(Pin pin) {
        return Math.addExact(this.pin, pin.pin) == PIN_MAX_VALUE;
    }

    public boolean isMiss(Pin pin) {
        return Math.addExact(this.pin, pin.pin) < PIN_MAX_VALUE;
    }

    public int value() {
        return pin;
    }

    public String valueToString() {
        return String.valueOf(pin);
    }

    public String valueToStringWithNextPin(Pin pin) {
        if (isSpare(pin)) {
            return this.pin + DESC_DELIMITER + SPARE_DESC_STRING;
        }
        return this.pin + DESC_DELIMITER + pin.pin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Pin pin1 = (Pin) o;
        return pin == pin1.pin;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pin);
    }

}