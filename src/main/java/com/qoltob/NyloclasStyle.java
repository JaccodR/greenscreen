package com.qoltob;

import lombok.Getter;

public enum NyloclasStyle
{
    MELEE_SMALL(8342,8348),
    MELEE_BIG(8345,8351),
    RANGE_SMALL(8343, 8349),
    RANGE_BIG(8346, 8352),
    MAGE_SMALL(8344, 8350),
    MAGE_BIG(8347, 8353);

    @Getter
    private int id;

    @Getter
    private int aggro_id;

    NyloclasStyle(int id, int aggro_id)
    {
        this.id = id;
        this.aggro_id = aggro_id;
    }


}
