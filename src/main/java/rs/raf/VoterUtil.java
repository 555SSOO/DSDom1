package rs.raf;

import org.apache.zookeeper.KeeperException;

class VoterUtil {

    static void leaderChanged(ZooNode zooNode) throws KeeperException, InterruptedException {
        if (!voteIfLeaderStarted(zooNode)) {
            if (String.valueOf(zooNode.getMyVote()).equals(LeaderUtil.getLeaderData(zooNode))) {
                System.out.println(zooNode.getId() + ": My vote (" + zooNode.getMyVote() + ") won!");
            } else {
                System.out.println(zooNode.getId() + ": My vote (" + zooNode.getMyVote() + ") lost...");
            }
        }
    }

    static boolean voteIfLeaderStarted(ZooNode zooNode) throws KeeperException, InterruptedException {
        boolean leaderStarted = "Start".equals(LeaderUtil.getLeaderData(zooNode));
        if (leaderStarted) {
            zooNode.setMyVote(Util.binaryRandom());
            zooNode.getZk().setData(zooNode.getCurrentNode(), Integer.toHexString(zooNode.getMyVote()).getBytes(), 0);
        }
        return leaderStarted;
    }

}
