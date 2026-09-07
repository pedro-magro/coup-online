package com.example.coup.game;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class CourtDeck {
    private final List<Influence> deck = new ArrayList<>(15);


    public CourtDeck(){
        prepareDeck();
        shuffleDeck();
    }

    public int getRemainingCards(){
        return deck.size();
    }

    Influence draw(){
        if(deck.isEmpty()){
            throw new GameRuleException("No cards left in the deck");
        }
        return deck.removeLast();

    }

    private void prepareDeck(){

        for(CharacterType characterType : CharacterType.values()){
            for(int i = 0; i < 3; i++){
                deck.add(new Influence(characterType));
            }
        }
    }

    Influence tradeCard(Influence influence){
        Objects.requireNonNull(influence, "Influence is required");
        if(influence.isRevealed()){
            throw new GameRuleException("Lost influence cannot be traded");
        }
        deck.add(influence);
        shuffleDeck();
        return deck.removeLast();
    }

    private void shuffleDeck(){
        Collections.shuffle(deck, new SecureRandom());
    }
}
