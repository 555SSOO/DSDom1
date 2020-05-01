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

    // Function to check if the leader already exists as a node in ZooKeeper
    private static boolean leaderExists(ZooNode zooNode) throws InterruptedException {
        while (true) {
            try {
                // If there is a leader node, get it's id
                byte[] data = zooNode.getZk().getData("/leader", true, null);
                // We are the leader if that is our id
                zooNode.setLeader(data != null && new String(data).equals(zooNode.getId()));
                return true;
            } catch (KeeperException.NoNodeException e) {
                // If there is no leader node
                return false;
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    // Try to become a leader
    static void tryForLeader(ZooNode zooNode) throws InterruptedException {
        // We keep trying until a node assumes the role of leader
        while (!leaderExists(zooNode)) {
            try {
                // We try to become the leader
                zooNode.getZk().create("/leader", zooNode.getId().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
                zooNode.setLeader(true);
                break;
            } catch (KeeperException.NodeExistsException e) {
                // If there is already a leader, we are a voter node
                zooNode.setLeader(false);
                break;
            } catch (KeeperException e) {
                e.printStackTrace();
            }
        }
    }

    // If a voting node has voted, or has just been created, we check existing data and set up watches
    static void voteChanged(ZooNode zooNode) throws InterruptedException, KeeperException {
        // Get all current children nodes of /votes
        List<String> children = zooNode.getZk().getChildren("/votes", true);
        for (String child : children) {
            try {
                // Get all current data and set watches
                byte[] data = zooNode.getZk().getData("/votes/" + child, true, null);
                // If the node has not already voted, put its vote in the voting map
                zooNode.getVotes().putIfAbsent(child, Integer.decode(new String(data)));
                // If we get enough votes, finish voting
                if (zooNode.getVotes().size() >= VOTE_THRESHOLD) {
                    finishVotingAndPrintResult(zooNode);
                    break;
                }
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // If we get enough votes, we get the majority of votes and print out the result
    private static void finishVotingAndPrintResult(ZooNode zooNode) {
        zooNode.getVotes().values()
                .stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .max(Comparator.comparing(Map.Entry::getValue))
                .ifPresent(winningVote -> {
                    try {
                        // Print out the voting result
                        System.out.println("FINAL RESULT: The vote " + winningVote.getKey() + " has won with " + winningVote.getValue() + " votes!");
                        // Set the leader node data as the winning vote, so other nodes can see who won
                        zooNode.getZk().setData("/leader", winningVote.getKey().toString().getBytes(), 1);
                    } catch (KeeperException | InterruptedException e) {
                        e.printStackTrace();
                    }
                });
    }

    // Get the data written in the /leader node
    static String getLeaderData(ZooNode zooNode) throws KeeperException, InterruptedException {
        byte[] data = zooNode.getZk().getData("/leader", true, null);
        return new String(data);
    }

}
