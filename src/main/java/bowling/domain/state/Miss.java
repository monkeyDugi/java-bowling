package bowling.domain.state;

import bowling.domain.Pin;
import bowling.domain.Score;

public class Miss extends Finished {
    private final Pin firstPin;
    private final Pin secondPin;

    public Miss(int firstPin, int secondPin) {
        this(new Pin(firstPin), new Pin(secondPin));
    }

    public Miss(Pin firstPin, Pin secondPin) {
        this.validate(firstPin, secondPin);

        this.firstPin = firstPin;
        this.secondPin = secondPin;
    }

    private void validate(Pin firstPin, Pin secondPin) {
        if (firstPin.getCount() + secondPin.getCount() >= 10) {
            throw new IllegalArgumentException("잘못된 투구입니다.");
        }
    }

    @Override
    public int getPitchCount() {
        return 2;
    }

    @Override
    public Score getScore() {
        return Score.ofMiss(firstPin.sum(secondPin));
    }

    @Override
    public int getTotalCount() {
        return firstPin.sum(secondPin);
    }

    @Override
    public String toString() {
        return firstPin.toString()
                + "|"
                + (firstPin.sum(secondPin) == 0 ? "-" : secondPin.toString());
    }
}