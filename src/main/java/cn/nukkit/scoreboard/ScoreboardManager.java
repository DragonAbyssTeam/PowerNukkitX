package cn.nukkit.scoreboard;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.RemoveObjectivePacket;
import cn.nukkit.network.protocol.SetScorePacket;
import cn.nukkit.scoreboard.data.DisplaySlot;
import cn.nukkit.scoreboard.data.ScorerType;
import cn.nukkit.scoreboard.interfaces.AbstractScoreboardManager;
import cn.nukkit.scoreboard.interfaces.ScoreboardStorage;
import cn.nukkit.scoreboard.interfaces.Scorer;
import cn.nukkit.scoreboard.scorer.PlayerScorer;

import java.util.Map;
import java.util.stream.Collectors;

public class ScoreboardManager extends AbstractScoreboardManager {

    public ScoreboardManager(ScoreboardStorage storage) {
        super(storage);
    }

    @Override
    public void addScoreboard(Scoreboard scoreboard) {
        scoreboards.put(scoreboard.getObjectiveName(), scoreboard);
        storage.saveScoreboard(scoreboard);
    }

    @Override
    public void removeScoreboard(String name) {
        Scoreboard scoreboard = getScoreboard(name);
        for (Scorer scorer : scoreboard.getLines().keySet()) {
            if (scorer instanceof PlayerScorer playerScorer && playerScorer.getPlayer() != null) {
                SetScorePacket setScorePacket = new SetScorePacket();
                setScorePacket.action = SetScorePacket.Action.REMOVE;
                setScorePacket.infos = scoreboard.getLines().values().stream().map(line -> line.toRemovedScoreInfo()).collect(Collectors.toList());
                playerScorer.getPlayer().dataPacket(setScorePacket);
            }
        }
        RemoveObjectivePacket removeObjectivePacket = new RemoveObjectivePacket();
        removeObjectivePacket.objectiveName = name;
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            player.dataPacket(removeObjectivePacket);
        }
        scoreboards.remove(name);
        if (isScoreboardOnDisplay(name)) {
            removeDisplay(getDisplaySlot(name));
        }
        storage.removeScoreboard(name);
    }

    @Override
    public void setDisplay(DisplaySlot slot, String name) {
        display.put(slot, name);
        storage.saveDisplay(display);
        Server.getInstance().getOnlinePlayers().values().forEach(player -> {
            player.sendScoreboard(this.scoreboards.get(name), slot);
        });
        if (slot == DisplaySlot.BELOW_NAME)
            this.updateAllScoreTag();
    }

    @Override
    public void removeDisplay(DisplaySlot slot) {
        Server.getInstance().getOnlinePlayers().values().forEach(player -> {
            player.clearScoreboardSlot(slot);
        });
        display.put(slot, null);
        storage.saveDisplay(display);
        if (slot == DisplaySlot.BELOW_NAME)
            this.updateAllScoreTag();
    }

    @Override
    public void onPlayerJoin(Player player) {
        sendAllDisplayToAll();
        updateScoreTag(player);
    }

    @Override
    public void onPlayerQuit(Player player) {
        //in order to clear the old score information so that we can avoid "offline player"
        hideOfflinePlayerScore(player);
    }

    private void sendDisplayToAll(DisplaySlot slot) {
        Server.getInstance().getOnlinePlayers().values().forEach(player -> {
            sendDisplayTo(slot, player);
        });
    }

    private void sendDisplayTo(DisplaySlot slot, Player player) {
        if (display.get(slot) != null)
            player.sendScoreboard(this.scoreboards.get(this.display.get(slot)), slot);
    }

    private void sendAllDisplayToAll() {
        Server.getInstance().getOnlinePlayers().values().forEach(player -> {
            sendAllDisplayTo(player);
        });
    }

    private void sendAllDisplayTo(Player player) {
        for (DisplaySlot slot : DisplaySlot.values()) {
            sendDisplayTo(slot, player);
        }
    }

    private void hideOfflinePlayerScore(Player player) {
        PlayerScorer scorer = new PlayerScorer(player);

        SetScorePacket pk = new SetScorePacket();
        pk.action = SetScorePacket.Action.REMOVE;
        for (Scoreboard scoreboard : scoreboards.values()) {
            if (scoreboard.getLines().containsKey(scorer)) {
                pk.infos.add(new SetScorePacket.ScoreInfo(scoreboard.getLines().get(scorer).getScoreboardId(), scoreboard.getObjectiveName(), 0, ScorerType.PLAYER, -1));
            }
        }

        for (Player onlinePlayer : Server.getInstance().getOnlinePlayers().values()) {
            onlinePlayer.dataPacket(pk);
        }
    }
}
