package rs.raf;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static rs.raf.Util.VOTE_THRESHOLD;

class LeaderUtil {

    private static boolean leaderExists(ZooNode zooNode) throws InterruptedException {
        while (true) {
            try {
                byte[] data = zooNode.getZk().getData("/leader", true, null);
                zooNode.setLeader(data != null && new String(data).equals(zooNode.getId()));
                return true;
            } catch (KeeperException.NoNodeException e) {
                return false;
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    static void tryForLeader(ZooNode zooNode) throws InterruptedException {
        while (!leaderExists(zooNode)) {
            try {
                zooNode.getZk().create("/leader", zooNode.getId().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zooNode.setLeader(true);
                break;
            } catch (KeeperException.NodeExistsException e) {
                zooNode.setLeader(false);
                break;
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    static String getLeaderData(ZooNode zooNode) throws KeeperException, InterruptedException {
        byte[] data = zooNode.getZk().getData("/leader", true, null);
        return new String(data);
    }

    static void voteChanged(ZooNode zooNode) throws InterruptedException, KeeperException {
        List<String> children = zooNode.getZk().getChildren("/votes", true);
        for (String child : children) {
            try {
                byte[] data = zooNode.getZk().getData("/votes/" + child, true, null);
                zooNode.getVotes().putIfAbsent(child, Integer.decode(new String(data)));
                if (zooNode.getVotes().size() >= VOTE_THRESHOLD) {
                    finishVotingAndPrintResult(zooNode);
                    break;
                }
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void finishVotingAndPrintResult(ZooNode zooNode) {
        zooNode.getVotes().values()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .ifPresent(winningVote -> {
                    try {
                        System.out.println("FINAL RESULT: The vote " + winningVote.getKey() + " has won with " + winningVote.getValue() + " votes!");
                        zooNode.getZk().setData("/leader", winningVote.getKey().toString().getBytes(), 1);
                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

}
