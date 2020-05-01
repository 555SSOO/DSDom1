package rs.raf;

import org.apache.zookeeper.KeeperException;

class VoterUtil {

    // If the /leader node has been changed
    static void leaderChanged(ZooNode zooNode) throws KeeperException, InterruptedException {
        // Check if the leader data is "Start", if it is not, we assume the voting has been finished and get results
        if (!voteIfLeaderStarted(zooNode)) {
            // If the leader has the same value as the /vote node then we won
            if (String.valueOf(zooNode.getMyVote()).equals(LeaderUtil.getLeaderData(zooNode))) {
                System.out.println(zooNode.getId() + ": My vote (" + zooNode.getMyVote() + ") won!");
            }
            // Otherwise, we assume that we won, but this can also be checked by comparing the winning vote with other voting options
            else {
                System.out.println(zooNode.getId() + ": My vote (" + zooNode.getMyVote() + ") lost...");
            }
        }
    }

    // If the leader has "Start" data, that means we should vote
    static boolean voteIfLeaderStarted(ZooNode zooNode) throws KeeperException, InterruptedException {
        boolean leaderStarted = "Start".equals(LeaderUtil.getLeaderData(zooNode));
        if (leaderStarted) {
            // We set our vote to be either 0 or 1
            zooNode.setMyVote(Util.binaryRandom());
            zooNode.getZk().setData(zooNode.getCurrentNode(), Integer.toHexString(zooNode.getMyVote()).getBytes(), 0);
        }
        return leaderStarted;
    }

}
