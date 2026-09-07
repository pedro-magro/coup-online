package com.example.coup.game;

import java.util.Objects;

public class Influence {

    private final CharacterType characterType;
    private boolean revealed;

    public CharacterType getCharacterType() {
        return characterType;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public Influence(CharacterType characterType) {
        this.characterType = Objects.requireNonNull(characterType, "Character type is required");
        revealed = false;
    }

    void reveal() {
        if (revealed) {
            throw new GameRuleException("Influence is already revealed");
        }

        revealed = true;
    }
}
