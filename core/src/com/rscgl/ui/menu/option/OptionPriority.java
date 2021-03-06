package com.rscgl.ui.menu.option;

public enum OptionPriority {

    BANK1(0),
    BANK5(1),
    BANK_10(2),
    BANK_LASTX(3),
    BANK_X(4),
    BANK_ALL(5),

    REPORT_ABUSE(2833),
    PLAYER_FOLLOW(2820),
    PLAYER_TRADE(2810),
    PLAYER_DUEL(2806),
    PLAYER_ATTACK_SIMILAR(805),
    PLAYER_ATTACK_DIVERGENT(2805),
    PLAYER_USE_ITEM(810),
    PLAYER_CAST_SPELL(800),
    CHAT_ADD_FRIEND(2831),
    CHAT_ADD_IGNORE(2832),
    CHAT_MESSAGE(2830),
    CANCEL(4000),
    WALL_CAST_SPELL(300),
    WALL_USE_ITEM(310),
    WALL_COMMAND1(320),
    WALL_COMMAND2(2300),
    WALL_EXAMINE(3300),
    OBJECT_USE_ITEM(410),
    OBJECT_COMMAND1(420),
    OBJECT_COMMAND2(2400),
    OBJECT_EXAMINE(3400),
    OBJECT_CAST_SPELL(400),
    GROUND_ITEM_CAST_SPELL(200),
    GROUND_ITEM_TAKE_EQUIPABLES(205),
    GROUND_ITEM_TAKE_STACKABLES(210),
    GROUND_ITEM_TAKE_POTIONS(215),
    GROUND_ITEM_TAKE(218),
    GROUND_ITEM_TAKE_FOOD(220),
    GROUND_ITEM_EXAMINE(3200),
    GROUND_ITEM_USE_ITEM(210),
    NPC_CAST_SPELL(700),
    NPC_ATTACK1(715),
    NPC_ATTACK2(2715),
    NPC_TALK_TO(720),
    NPC_COMMAND(725),
    NPC_EXAMINE(3700),
    NPC_USE_ITEM(710),
    SELF_CAST_SPELL(1000),
    LANDSCAPE_CAST_SPELL(900),
    LANDSCAPE_WALK_HERE(920),
    ITEM_CAST_SPELL(600),
    ITEM_REMOVE_EQUIPPED(620),
    ITEM_EQUIP(630),
    ITEM_COMMAND(640),
    ITEM_USE(650),
    ITEM_DROP(660),
    ITEM_DROP_X(661),
    ITEM_EXAMINE(3600),
    ITEM_BRANDISH(621),
    ITEM_BRANDISH_INV(631),
    ITEM_USE_ITEM(610),
    TRADE_OFFER(1),
    TRADE_REMOVE(2),
    DUEL_STAKE(3),
    DUEL_REMOVE(4),
    PARTY_INVITE(2834),
    DEV_ADD_NPC(1337),
    DEV_REMOVE_NPC(1338),
    DEV_ADD_OBJECT(1339),
    DEV_REMOVE_OBJECT(1340),
    DEV_ROTATE_OBJECT(1341),
    DEV_ADD_WALLOBJECT(1342),
    MOD_SUMMON_PLAYER(2835),
    MOD_GOTO_PLAYER(2836),
    MOD_PUT_PLAYER_JAIL(2837),
    MOD_KICK_PLAYER(2838),
    MOD_CHECK_PLAYER(2839),
    MOD_TELEPORT(2840),
    PLAYER_DEATHMATCH(2841),
    TEAM_INVITE(2842),
    PLAYER_TEAM_DUEL(2843),
    DEADMAN_PROTECT_SKILL(2844),
    DEADMAN_UNPROTECT_SKILL(2845),
    MAKE_SNOWBALL(2846), INSERT_ORDER(0);
    private final int priority;

    OptionPriority(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }
}
