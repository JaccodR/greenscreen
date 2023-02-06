package com.qoltob;

import lombok.Getter;

class Nylocas
{
    @Getter
    private Nylospawns spawn;

    @Getter
    private NyloStyle nylostyle;

    Nylocas(Nylospawns spawn, NyloStyle nylostyle)
    {
        this.spawn = spawn;
        this.nylostyle = nylostyle;
    }

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Nylocas))
        {
            return false;
        }
        Nylocas otherNPC = (Nylocas) other;
        return nylostyle.equals(otherNPC.getNylostyle()) && spawn.equals(otherNPC.getSpawn());
    }
}