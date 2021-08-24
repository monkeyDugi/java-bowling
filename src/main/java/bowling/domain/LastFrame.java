package bowling.domain;

import static bowling.domain.PitchResult.*;

import java.util.Optional;

import bowling.domain.exception.InvalidPinCountException;

public class LastFrame extends Frame {

	private static final int MAX = 10;

	private Pitch bonus;

	public LastFrame() {

	}

	public LastFrame(final Pitch first) {
		super(first);
	}

	public LastFrame(final Pitch first, final Pitch second) {
		super(first, second);
	}

	public LastFrame(final Pitch first, final Pitch second, final Pitch bonus) {
		super(first, second);
		this.bonus = bonus;
	}

	@Override
	public boolean isEnd() {
		if (first == null || second == null) {
			return false;
		}

		if (bonus != null) {
			return true;
		}

		if (first.getPitchResult().equals(STRIKE)
			|| PitchResult.findByPinCount(first.getPinCount(), second.getPinCount()).equals(SPARE)) {
			return false;
		}

		return true;
	}

	@Override
	public void pitch(final int pinCount) {
		if (first == null) {
			first = new Pitch(pinCount);
			return;
		}

		if (second == null) {
			addSecond(pinCount);
			return;
		}

		addBonus(pinCount);
	}

	@Override
	public String getResult() {
		return String.join(
			Optional.ofNullable(first)
				.map(e -> String.valueOf(e.getPinCount()))
				.orElse("")
				+ Optional.ofNullable(second)
				.map(e -> String.valueOf(e.getPinCount()))
				.orElse("")
				+ Optional.ofNullable(bonus)
				.map(e -> String.valueOf(e.getPinCount()))
				.orElse(""), DELIMITER);
	}

	// private String resultHelper(final Pitch first) {
	// 	if (first.getPitchResult().equals(STRIKE)) {
	// 		return STRIKE.getFlag();
	// 	}
	//
	// 	return String.valueOf(first.getPinCount());
	// }
	//
	// private String resultHelper(final Pitch first,) {
	// 	if (first.getPitchResult().equals(STRIKE)) {
	// 		return STRIKE.getFlag();
	// 	}
	//
	// 	return String.valueOf(first.getPinCount());
	// }

	private void addSecond(final int pinCount) {
		if (first.getPitchResult().equals(STRIKE)) {
			second = first.next(pinCount);
			return;
		}

		if (first.getPinCount() + pinCount > MAX) {
			throw new InvalidPinCountException();
		}

		second = first.next(pinCount);
	}

	private void addBonus(final int pinCount) {
		if (second.getPitchResult().equals(STRIKE)
			|| second.getPitchResult().equals(SPARE)) {
			bonus = second.next(pinCount);
			return;
		}

		if (second.getPinCount() + pinCount > MAX) {
			throw new InvalidPinCountException();
		}

		bonus = second.next(pinCount);
	}

	public Pitch getBonus() {
		return bonus;
	}
}
