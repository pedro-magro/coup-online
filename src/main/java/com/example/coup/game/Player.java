package com.example.coup.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Player {

    private final String id;
    private final String name;
    private int coins =2;
    private final List<Influence> aliveInfluences = new ArrayList<>(2);
    private final List<Influence> deadInfluences = new ArrayList<>(2);


    public Player(String id, String name){
        this.id = requireText(id, "Player id is required");
        this.name = requireText(name, "Player name is required");
    }

    public String getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public int getCoins(){
        return coins;
    }

    public int getRemainingInfluences(){
        return aliveInfluences.size();
    }

    public boolean isAlive(){
        return !aliveInfluences.isEmpty();
    }

    void receiveCoins(int amount){
        if(amount<=0){
            throw  new IllegalArgumentException("Amount must be greater than 0");
        }
        coins+= amount;
    }

    void payCoins(int amount){
        if(amount > coins){
            throw new GameRuleException("Player does not have enough coins");
        }

        if(amount <= 0){
            throw   new IllegalArgumentException("Amount must be positive");
        }

        coins-= amount;
    }

    void dealInitialInfluence(Influence influence){
        if(influence == null){
            throw new NullPointerException("Influence can not be null");
        }
        else if(influence.isRevealed()){
            throw new GameRuleException("Influence is already revealed");
        }
        else if(!deadInfluences.isEmpty()){
            throw new GameRuleException("Cannot deal an initial influence after the game has started");
        }
        else if(aliveInfluences.size() >= 2){
            throw new GameRuleException("Player can not hold more than two cards");
        }
        else{
            this.aliveInfluences.add(influence);
        }

    }

    void loseInfluence(int index){
        if(!isAlive()){
            throw new GameRuleException("Player is not alive");
        }
        else if(index >= aliveInfluences.size() || index < 0 ){
            throw new GameRuleException("Invalid influence index");
        }

        Influence deadCard = aliveInfluences.remove(index);
        deadCard.reveal();
        deadInfluences.add(deadCard);

    }

    void replaceInfluence(Influence influenceToReplace, Influence replacement){
        Objects.requireNonNull(influenceToReplace, "Influence to replace is required");
        Objects.requireNonNull(replacement, "Replacement influence is required");
        if(influenceToReplace.isRevealed()){
            throw new GameRuleException("Cannot trade a lost influence");
        }
        if(replacement.isRevealed()){
            throw new GameRuleException("Replacement influence cannot be lost");
        }
        int index = aliveInfluences.indexOf(influenceToReplace);
        if(index < 0){
            throw new GameRuleException("Influence is not active");
        }
        aliveInfluences.set(index, replacement);
    }

    public List<Influence> getAliveInfluences(){
        return List.copyOf(aliveInfluences);
    }

    public List<Influence> getDeadInfluences(){
        return List.copyOf(deadInfluences);
    }

    private static String requireText(String value, String message){
        if(Objects.requireNonNull(value, message).isBlank()){
            throw new IllegalArgumentException(message);
        }

        return value;
    }






}
