package com.example.coup.game;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class InfluenceTest {

    @Test
    void newInfluenceStartsHidden(){
        Influence influence = new Influence(CharacterType.DUKE);

        assertThat(influence.isRevealed()).isFalse();
    }

    @Test
    void influenceCanBeRevealed(){
        Influence influence = new Influence(CharacterType.CONTESSA);

        influence.reveal();

        assertThat(influence.isRevealed()).isTrue();
    }

    @Test
    void revealInfluenceTwiceThrowsException(){
        Influence influence = new Influence(CharacterType.CAPTAIN);

        influence.reveal();
        assertThatThrownBy(()-> influence.reveal())
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Influence is already revealed");

    }

    @Test
    void nullNewInfluenceThrowsException(){
        assertThatThrownBy(()-> new Influence(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Character type is required");
    }
}
