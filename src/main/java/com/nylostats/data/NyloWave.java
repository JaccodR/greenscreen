package com.nylostats.data;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NyloWave
{
    @Getter
    private int durationWaves; //when 0 nylos left in the room
    private int durationBoss; //from boss spawn to boss death
    private int[] splits;
    private int[] bossRota;
    private int damageDealt;
    private int damageTaken;
    private int idleTicks;

    public NyloWave(int durationWaves, int durationBoss, int[] splits,int[] bossRota, int damageDealt, int damageTaken, int idleTicks)
    {
        this.durationWaves = durationWaves;
        this.durationBoss = durationBoss;
        this.splits = splits;
        this.bossRota = bossRota;
        this.damageDealt = damageDealt;
        this.damageTaken = damageTaken;
        this.idleTicks = idleTicks;
    }
}
