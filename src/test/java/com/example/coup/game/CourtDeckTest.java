package com.example.coup.game;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;


public class CourtDeckTest {

    @Test
    void courtDeckStartsWithFifteenCards(){
        CourtDeck courtDeck = new CourtDeck();

        assertThat(courtDeck.getRemainingCards()).isEqualTo(15);
    }

    @Test
    void drawACardSubtractsOneFromTheDeck(){
        CourtDeck deck = new CourtDeck();

        int sub = deck.getRemainingCards();
        deck.draw();
        assertThat(deck.getRemainingCards()).isEqualTo(sub - 1);
    }

    @Test
    void hasThreeCardsForEveryCharacter(){
        CourtDeck deck = new CourtDeck();
        Map<CharacterType, Integer> checksheet = new HashMap<>();
        while(deck.getRemainingCards() > 0){
            Influence card = deck.draw();
            checksheet.put(card.getCharacterType(), checksheet.getOrDefault(card.getCharacterType(), 0) + 1);
        }
        assertThat(checksheet.size()).isEqualTo(5);
        checksheet.forEach((cardtype, quantity) -> {
            assertThat(quantity).isEqualTo(3);
        });
    }

    @Test
    void drawingMoreThanTheRemainingCardsThrowsException(){
        CourtDeck deck = new CourtDeck();

        for(int i = 0; i <15; i++){
            deck.draw();
        }

        assertThatThrownBy(() -> deck.draw())
                .isInstanceOf(GameRuleException.class)
                .hasMessage("No cards left in the deck");
    }

    @Test
    void tradeCardKeepsDeckSizeStable(){
        CourtDeck deck = new CourtDeck();
        Influence card = deck.draw();

        int qtd = deck.getRemainingCards();
        Influence replacement = deck.tradeCard(card);

        assertThat(replacement).isNotNull();
        assertThat(deck.getRemainingCards()).isEqualTo(qtd);
    }

    @Test
    void tradeNullCardThrowsException(){
        CourtDeck deck = new CourtDeck();
        int qtdBeforeTrade = deck.getRemainingCards();

        assertThatThrownBy(() -> deck.tradeCard(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Influence is required");

        assertThat(deck.getRemainingCards()).isEqualTo(qtdBeforeTrade);
    }

    @Test
    void tradeLostInfluenceThrowsException(){
        CourtDeck deck = new CourtDeck();
        Influence lostInfluence = deck.draw();
        lostInfluence.reveal();

        int qtdBefore = deck.getRemainingCards();

        assertThatThrownBy(() -> deck.tradeCard(lostInfluence))
                .isInstanceOf(GameRuleException.class)
                .hasMessage("Lost influence cannot be traded");

        assertThat(deck.getRemainingCards()).isEqualTo(qtdBefore);

    }
}
