package com.darch.game;

/**
 * BehaviourInterface.java
 * Interface for game objects that behave (that is, their movements are non deterministic)
 * Currently all game objects react to the player, so this gets the player.
 *
 * Revision History: Created by Jon on 2015-12-01.
 *
 */
public interface BehaviourInterface {
    void GetPlayer(Player player);
}
