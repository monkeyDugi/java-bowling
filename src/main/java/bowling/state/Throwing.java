package bowling.state;

import bowling.Pins;

public interface Throwing {

    Throwing bowl(Pins fallenPins);

    String symbol();
}
