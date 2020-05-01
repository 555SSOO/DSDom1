package rs.raf;

import org.apache.zookeeper.*;

import java.io.IOException;
import java.util.Random;

class Util {

    static final String VOTES_PATH = "/votes";
    static final String LEADER_PATH = "/leader";
    static int VOTE_THRESHOLD = 3;
    static int AMOUNT_OF_NODES = 4;
    static String HOST = "localhost:2181";

    // This variable can be set manually if you are sure the persistent /votes node is already set
    private static boolean persistentNodeSet = false;

    static int binaryRandom() {
        return (int) Math.round(Math.random());
    }

    static void initPersistentNode() throws IOException, KeeperException, InterruptedException {
        if(!persistentNodeSet) {
            new ZooKeeper("localhost:2181", 5000, event -> {})
                    .create("/votes", Integer.toHexString(new Random().nextInt()).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        }
    }

}
