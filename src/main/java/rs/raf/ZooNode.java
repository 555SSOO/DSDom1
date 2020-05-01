package rs.raf;

import lombok.Getter;
import lombok.Setter;
import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static rs.raf.Util.LEADER_PATH;
import static rs.raf.Util.VOTES_PATH;

@Getter
@Setter
public class ZooNode implements Watcher {

    private ZooKeeper zk;
    private boolean isLeader = false;
    private String id = Integer.toHexString(new Random().nextInt());
    private int myVote;
    private String currentNode;
    private String host;
    private Map<String, Integer> votes = new HashMap<>();

    ZooNode(String hostPort) {
        this.host = hostPort;
    }

    void startZk() throws IOException {
        zk = new ZooKeeper(host, 2000, this);
    }

    public void process(WatchedEvent event) {
        try {
            if (event.getType() == Event.EventType.NodeDataChanged) {
                if (LEADER_PATH.equals(event.getPath())) {
                    VoterUtil.leaderChanged(this);
                } else if (VOTES_PATH.equals(event.getPath().substring(0, VOTES_PATH.length()))) {
                    LeaderUtil.voteChanged(this);
                }
            } else if (event.getType() == Event.EventType.NodeChildrenChanged) {
                if (VOTES_PATH.equals(event.getPath())) {
                    LeaderUtil.voteChanged(this);
                }
            }
        } catch (InterruptedException | KeeperException e) {
            e.printStackTrace();
        }
    }

    void init() throws KeeperException, InterruptedException {
        if (isLeader) {
            zk.setData("/leader", "Start".getBytes(), 0);
            LeaderUtil.voteChanged(this);
        } else {
            currentNode = zk.create("/votes/vote-", id.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            VoterUtil.voteIfLeaderStarted(this);
        }
    }

}
