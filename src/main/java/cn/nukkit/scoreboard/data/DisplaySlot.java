package cn.nukkit.scoreboard.data;

import lombok.Getter;

public enum DisplaySlot {

    SIDEBAR("sidebar"),
    LIST("list"),
    BELOW_NAME("belowname");

    @Getter
    private String slotName;

    DisplaySlot(String slotName) {
        this.slotName = slotName;
    }
}
