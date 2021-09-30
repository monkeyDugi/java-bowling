package bowling.domain;

import qna.exception.NotFoundException;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Players {

    private List<Player> players;

    public Players(final List<String> names) {
        this.players = names.stream()
                .map(Player::new)
                .collect(Collectors.toList());
    }

    public Player currentPlayer() {
        int currentRound = round();

        return players.stream()
                .filter(player -> player.round() == currentRound)
                .findFirst()
                .orElseThrow(NotFoundException::new);
    }

    public void roll(final String scoreText) {
        int currentRound = round();

        Player currentPlayer = players.stream()
                .filter(player -> player.round() == currentRound)
                .findFirst()
                .orElseThrow(NotFoundException::new);

        currentPlayer.roll(scoreText);
    }

    private int round() {
        return players.stream()
                .map(Player::round)
                .min(Integer::compareTo)
                .get();
    }

    public boolean isFinish() {
        return players.stream().allMatch(Player::isFinish);
    }

    public List<Player> elements() {
        return Collections.unmodifiableList(this.players);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Players players1 = (Players) o;
        return Objects.equals(players, players1.players);
    }

    @Override
    public int hashCode() {
        return Objects.hash(players);
    }
}