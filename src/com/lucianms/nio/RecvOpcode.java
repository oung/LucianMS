package com.lucianms.nio;

import com.lucianms.server.events.PacketEvent;
import com.lucianms.server.events.PlayerUpdateEvent;
import com.lucianms.server.events.PongEvent;
import com.lucianms.server.events.channel.*;
import com.lucianms.server.events.login.*;
import net.server.channel.handlers.*;
import net.server.handlers.login.PickCharHandler;
import net.server.handlers.login.ViewCharHandler;

public enum RecvOpcode {

    // @formatter:off
    LOGIN_PASSWORD          (0x0001, ReceivePacketState.LoginServer,   AccountLoginEvent.class),
    GUEST_LOGIN             (0x0002, ReceivePacketState.LoginServer),
    SERVERLIST_REREQUEST    (0x0004, ReceivePacketState.LoginServer,   WorldListEvent.class),
    CHARLIST_REQUEST        (0x0005, ReceivePacketState.LoginServer,   AccountChannelSelectEvent.class),
    SERVERSTATUS_REQUEST    (0x0006, ReceivePacketState.LoginServer,   WorldStatusCheckEvent.class),
    ACCEPT_TOS              (0x0007, ReceivePacketState.LoginServer,   AccountToSResultEvent.class),
    SET_GENDER              (0x0008, ReceivePacketState.LoginServer,   AccountGenderSetEvent.class),
    AFTER_LOGIN             (0x0009, ReceivePacketState.LoginServer,   AccountPostLoginEvent.class),
    REGISTER_PIN            (0x000A, ReceivePacketState.LoginServer,   AccountPINSetEvent.class),
    SERVERLIST_REQUEST      (0x000B, ReceivePacketState.LoginServer,   WorldListEvent.class),
    PLAYER_DC               (0x000C, ReceivePacketState.LoginServer),
    VIEW_ALL_CHAR           (0x000D, ReceivePacketState.LoginServer,   ViewCharHandler.class),
    PICK_ALL_CHAR           (0x000E, ReceivePacketState.LoginServer,   PickCharHandler.class),
    CHAR_SELECT             (0x0013, ReceivePacketState.LoginServer,   AccountPlayerSelectEvent.class),
    PLAYER_LOGGEDIN         (0x0014, ReceivePacketState.ChannelServer, PlayerLoginEvent.class),
    CHECK_CHAR_NAME         (0x0015, ReceivePacketState.LoginServer,   AccountPlayerCreateUsernameCheckEvent.class),
    CREATE_CHAR             (0x0016, ReceivePacketState.LoginServer,   AccountPlayerCreateEvent.class),
    DELETE_CHAR             (0x0017, ReceivePacketState.LoginServer,   AccountPlayerDeleteEvent.class),
    PONG                    (0x0018, ReceivePacketState.Both,          PongEvent.class),
    CLIENT_START_ERROR      (0x0019, ReceivePacketState.LoginServer),
    CLIENT_ERROR            (0x001A, ReceivePacketState.LoginServer),
    STRANGE_DATA            (0x001B, ReceivePacketState.Both       ),
    RELOG                   (0x001C, ReceivePacketState.LoginServer,   AccountRelogEvent.class),
    REGISTER_PIC            (0x001D, ReceivePacketState.LoginServer,   AccountRegisterPICEvent.class),
    CHAR_SELECT_WITH_PIC    (0x001E, ReceivePacketState.LoginServer,   AccountSelectPlayerPICEvent.class),
    VIEW_ALL_PIC_REGISTER   (0x001F, ReceivePacketState.LoginServer),
    VIEW_ALL_WITH_PIC       (0x0020, ReceivePacketState.LoginServer),
    CHANGE_MAP              (0x0026, ReceivePacketState.ChannelServer, ChangeMapEvent.class),
    CHANGE_CHANNEL          (0x0027, ReceivePacketState.ChannelServer, ChangeChannelEvent.class),
    ENTER_CASHSHOP          (0x0028, ReceivePacketState.ChannelServer, EnterCashShopEvent.class),
    MOVE_PLAYER             (0x0029, ReceivePacketState.ChannelServer, PlayerMoveEvent.class),
    CANCEL_CHAIR            (0x002A, ReceivePacketState.ChannelServer, PlayerChairRemoveEvent.class),
    USE_CHAIR               (0x002B, ReceivePacketState.ChannelServer, PlayerChairUseEvent.class),
    CLOSE_RANGE_ATTACK      (0x002C, ReceivePacketState.ChannelServer, PlayerDealDamageNearbyEvent.class),
    RANGED_ATTACK           (0x002D, ReceivePacketState.ChannelServer, PlayerDealDamageRangedEvent.class),
    MAGIC_ATTACK            (0x002E, ReceivePacketState.ChannelServer, PlayerDealDamageMagicEvent.class),
    TOUCH_MONSTER_ATTACK    (0x002F, ReceivePacketState.ChannelServer, PlayerDealDamageTouchEvent.class),
    TAKE_DAMAGE             (0x0030, ReceivePacketState.ChannelServer, PlayerTakeDamageEvent.class),
    GENERAL_CHAT            (0x0031, ReceivePacketState.ChannelServer, PlayerAllChatEvent.class),
    CLOSE_CHALKBOARD        (0x0032, ReceivePacketState.ChannelServer, PlayerChalkboardCloseEvent.class),
    FACE_EXPRESSION         (0x0033, ReceivePacketState.ChannelServer, PlayerFaceExpressionEvent.class),
    USE_ITEMEFFECT          (0x0034, ReceivePacketState.ChannelServer),
    USE_DEATHITEM           (0x0035, ReceivePacketState.ChannelServer),
    MONSTER_BOOK_COVER      (0x0039, ReceivePacketState.ChannelServer),
    NPC_TALK                (0x003A, ReceivePacketState.ChannelServer, NPCTalkEvent.class),
    REMOTE_STORE            (0x003B, ReceivePacketState.ChannelServer),
    NPC_TALK_MORE           (0x003C, ReceivePacketState.ChannelServer, NPCMoreTalkHandler.class),
    NPC_SHOP                (0x003D, ReceivePacketState.ChannelServer),
    STORAGE                 (0x003E, ReceivePacketState.ChannelServer, PlayerStorageOperationEvent.class),
    HIRED_MERCHANT_REQUEST  (0x003F, ReceivePacketState.ChannelServer, HiredMerchantEvent.class),
    FREDRICK_ACTION         (0x0040, ReceivePacketState.ChannelServer),
    DUEY_ACTION             (0x0041, ReceivePacketState.ChannelServer),
    ADMIN_SHOP              (0x0044, ReceivePacketState.ChannelServer),
    ITEM_SORT               (0x0045, ReceivePacketState.ChannelServer, PlayerInventorySortEvent.class),
    ITEM_SORT2              (0x0046, ReceivePacketState.ChannelServer),
    ITEM_MOVE               (0x0047, ReceivePacketState.ChannelServer, PlayerInventoryMoveEvent.class),
    USE_ITEM                (0x0048, ReceivePacketState.ChannelServer, PlayerItemUseEvent.class),
    CANCEL_ITEM_EFFECT      (0x0049, ReceivePacketState.ChannelServer, PlayerItemEffectCancelEvent.class),
    USE_SUMMON_BAG          (0x004B, ReceivePacketState.ChannelServer, PlayerSummoningBagUseEvent.class),
    PET_FOOD                (0x004C, ReceivePacketState.ChannelServer),
    USE_MOUNT_FOOD          (0x004D, ReceivePacketState.ChannelServer),
    SCRIPTED_ITEM           (0x004E, ReceivePacketState.ChannelServer),
    USE_CASH_ITEM           (0x004F, ReceivePacketState.ChannelServer, PlayerCashItemUseEvent.class),
    USE_CATCH_ITEM          (0x0051, ReceivePacketState.ChannelServer),
    USE_SKILL_BOOK          (0x0052, ReceivePacketState.ChannelServer),
    USE_TELEPORT_ROCK       (0x0054, ReceivePacketState.ChannelServer),
    USE_RETURN_SCROLL       (0x0055, ReceivePacketState.ChannelServer, PlayerItemUseEvent.class),
    USE_UPGRADE_SCROLL      (0x0056, ReceivePacketState.ChannelServer, PlayerScrollUseEvent.class),
    DISTRIBUTE_AP           (0x0057, ReceivePacketState.ChannelServer, DistributeAPEvent.class),
    AUTO_DISTRIBUTE_AP      (0x0058, ReceivePacketState.ChannelServer),
    HEAL_OVER_TIME          (0x0059, ReceivePacketState.ChannelServer, PlayerHealIdleEvent.class),
    DISTRIBUTE_SP           (0x005A, ReceivePacketState.ChannelServer, PlayerSkillPointUseEvent.class),
    SPECIAL_MOVE            (0x005B, ReceivePacketState.ChannelServer, PlayerSpecialMoveEvent.class),
    CANCEL_BUFF             (0x005C, ReceivePacketState.ChannelServer, PlayerBuffCancelEvent.class),
    SKILL_EFFECT            (0x005D, ReceivePacketState.ChannelServer, PlayerSkillEffectEvent.class),
    MESO_DROP               (0x005E, ReceivePacketState.ChannelServer, PlayerMoneyDropEvent.class),
    GIVE_FAME               (0x005F, ReceivePacketState.ChannelServer, PlayerFameGiveEvent.class),
    CHAR_INFO_REQUEST       (0x0061, ReceivePacketState.ChannelServer, ViewCharacterInfoEvent.class),
    SPAWN_PET               (0x0062, ReceivePacketState.ChannelServer),
    CANCEL_DEBUFF           (0x0063, ReceivePacketState.ChannelServer),
    CHANGE_MAP_SPECIAL      (0x0064, ReceivePacketState.ChannelServer),
    USE_INNER_PORTAL        (0x0065, ReceivePacketState.ChannelServer, PlayerFieldPortalUseEvent.class),
    TROCK_ADD_MAP           (0x0066, ReceivePacketState.ChannelServer),
    REPORT                  (0x006A, ReceivePacketState.ChannelServer),
    QUEST_ACTION            (0x006B, ReceivePacketState.ChannelServer, QuestOperastionEvent.class),
    SKILL_MACRO             (0x006E, ReceivePacketState.ChannelServer),
    USE_ITEM_REWARD         (0x0070, ReceivePacketState.ChannelServer, PlayerItemUseRewardEvent.class),
    MAKER_SKILL             (0x0071, ReceivePacketState.ChannelServer),
    USE_REMOTE              (0x0074, ReceivePacketState.ChannelServer, RemoteGachaponEvent.class),
    ADMIN_CHAT              (0x0076, ReceivePacketState.ChannelServer),
    PARTYCHAT               (0x0077, ReceivePacketState.ChannelServer, PlayerGroupChatEvent.class),
    WHISPER                 (0x0078, ReceivePacketState.ChannelServer, PlayerWhisperEvent.class),
    SPOUSE_CHAT             (0x0079, ReceivePacketState.ChannelServer, PlayerSpouseChatEvent.class),
    MESSENGER               (0x007A, ReceivePacketState.ChannelServer),
    PLAYER_INTERACTION      (0x007B, ReceivePacketState.ChannelServer, PlayerInteractionEvent.class),
    PARTY_OPERATION         (0x007C, ReceivePacketState.ChannelServer, PlayerPartyOperationEvent.class),
    DENY_PARTY_REQUEST      (0x007D, ReceivePacketState.ChannelServer, PlayerPartyInviteDenyEvent.class),
    GUILD_OPERATION         (0x007E, ReceivePacketState.ChannelServer, PlayerGuildOperationEvent.class),
    DENY_GUILD_REQUEST      (0x007F, ReceivePacketState.ChannelServer, PlayerGuildInviteDenyEvent.class),
    ADMIN_COMMAND           (0x0080, ReceivePacketState.ChannelServer, AdministratorCommandEvent.class),
    ADMIN_LOG               (0x0081, ReceivePacketState.ChannelServer),
    BUDDYLIST_MODIFY        (0x0082, ReceivePacketState.ChannelServer, PlayerFriendsListModifyEvent.class),
    NOTE_ACTION             (0x0083, ReceivePacketState.ChannelServer),
    USE_DOOR                (0x0085, ReceivePacketState.ChannelServer, PlayerMagicDoorUseEvent.class),
    CHANGE_KEYMAP           (0x0087, ReceivePacketState.ChannelServer, KeymapChangeEvent.class),
    RPS_ACTION              (0x0088, ReceivePacketState.ChannelServer, RockPaperScissorsEvent.class),
    RING_ACTION             (0x0089, ReceivePacketState.ChannelServer, PlayerRingActionEvent.class),
    WEDDING_ACTION          (0x008A, ReceivePacketState.ChannelServer),
    OPEN_FAMILY             (0x0092, ReceivePacketState.ChannelServer),
    ADD_FAMILY              (0x0093, ReceivePacketState.ChannelServer),
    ACCEPT_FAMILY           (0x0096, ReceivePacketState.ChannelServer),
    USE_FAMILY              (0x0097, ReceivePacketState.ChannelServer),
    ALLIANCE_OPERATION      (0x0098, ReceivePacketState.ChannelServer),
    BBS_OPERATION           (0x009B, ReceivePacketState.ChannelServer),
    ENTER_MTS               (0x009C, ReceivePacketState.ChannelServer, PlayerMTSEnterEvent.class),
    USE_SOLOMON_ITEM        (0x009D, ReceivePacketState.ChannelServer),
    USE_GACHA_EXP           (0x009E, ReceivePacketState.ChannelServer),
    CLICK_GUIDE             (0x00A2, ReceivePacketState.ChannelServer),
    ARAN_COMBO_COUNTER      (0x00A3, ReceivePacketState.ChannelServer, PlayerAranComboEvent.class),
    MOVE_PET                (0x00A7, ReceivePacketState.ChannelServer, PetMoveEvent.class),
    PET_CHAT                (0x00A8, ReceivePacketState.ChannelServer, PlayerPetChatEvent.class),
    PET_COMMAND             (0x00A9, ReceivePacketState.ChannelServer),
    PET_LOOT                (0x00AA, ReceivePacketState.ChannelServer),
    PET_AUTO_POT            (0x00AB, ReceivePacketState.ChannelServer),
    PET_EXCLUDE_ITEMS       (0x00AC, ReceivePacketState.ChannelServer),
    MOVE_SUMMON             (0x00AF, ReceivePacketState.ChannelServer, SummonMoveEvent.class),
    SUMMON_ATTACK           (0x00B0, ReceivePacketState.ChannelServer, SummonDealDamageEvent.class),
    DAMAGE_SUMMON           (0x00B1, ReceivePacketState.ChannelServer, PlayerSummonTakeDamageEvent.class),
    BEHOLDER                (0x00B2, ReceivePacketState.ChannelServer),
    MOVE_DRAGON             (0x00B5, ReceivePacketState.ChannelServer, DragonMoveEvent.class),
    MOVE_LIFE               (0x00BC, ReceivePacketState.ChannelServer, LifeMoveEvent.class),
    AUTO_AGGRO              (0x00BD, ReceivePacketState.ChannelServer),
    MOB_DAMAGE_MOB_FRIENDLY (0x00C0, ReceivePacketState.ChannelServer, MobDamageMobFriendlyEvent.class),
    MONSTER_BOMB            (0x00C1, ReceivePacketState.ChannelServer),
    MOB_DAMAGE_MOB          (0x00C2, ReceivePacketState.ChannelServer),
    NPC_ACTION              (0x00C5, ReceivePacketState.ChannelServer, NpcMoveEvent.class),
    ITEM_PICKUP             (0x00CA, ReceivePacketState.ChannelServer),
    DAMAGE_REACTOR          (0x00CD, ReceivePacketState.ChannelServer, PlayerReactorHitEvent.class),
    TOUCHING_REACTOR        (0x00CE, ReceivePacketState.ChannelServer, PlayerReactorTouchEvent.class),
    TEMP_SKILL              (0x00CF, ReceivePacketState.ChannelServer),
    MAPLETV                 (0xFFFE, ReceivePacketState.ChannelServer),
    SNOWBALL                (0x00D3, ReceivePacketState.ChannelServer),
    LEFT_KNOCKBACK          (0x00D4, ReceivePacketState.ChannelServer),
    COCONUT                 (0x00D5, ReceivePacketState.ChannelServer),
    MATCH_TABLE             (0x00D6, ReceivePacketState.ChannelServer),
    MONSTER_CARNIVAL        (0x00DA, ReceivePacketState.ChannelServer, MonsterCarnivalEvent.class),
    PARTY_SEARCH_REGISTER   (0x00DC, ReceivePacketState.ChannelServer),
    PARTY_SEARCH_START      (0x00DE, ReceivePacketState.ChannelServer),
    PLAYER_UPDATE           (0x00DF, ReceivePacketState.Both,          PlayerUpdateEvent.class),
    CHECK_CASH              (0x00E4, ReceivePacketState.ChannelServer),
    CASHSHOP_OPERATION      (0x00E5, ReceivePacketState.ChannelServer, PlayerCashShopOperationEvent.class),
    COUPON_CODE             (0x00E6, ReceivePacketState.ChannelServer),
    OPEN_ITEMUI             (0x00EB, ReceivePacketState.ChannelServer),
    CLOSE_ITEMUI            (0x00EC, ReceivePacketState.ChannelServer),
    USE_ITEMUI              (0x00ED, ReceivePacketState.ChannelServer),
    MTS_OPERATION           (0x00FD, ReceivePacketState.ChannelServer),
    USE_MAPLELIFE           (0x00FE, ReceivePacketState.ChannelServer),
    USE_HAMMER              (0x0104, ReceivePacketState.ChannelServer, PlayerHammerUseEvent.class);
    public final int value;
    public final ReceivePacketState packetState;
    public final Class<? extends PacketEvent> clazz;
    // @formatter:on

    RecvOpcode(int value, ReceivePacketState packetState) {
        this(value, packetState, null);
    }

    RecvOpcode(int value, ReceivePacketState packetState, Class<? extends PacketEvent> clazz) {
        this.value = value;
        this.packetState = packetState;
        this.clazz = clazz;
    }

    public int getValue() {
        return value;
    }
}
