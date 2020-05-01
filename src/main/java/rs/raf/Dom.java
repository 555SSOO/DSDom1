package rs.raf;

import org.apache.zookeeper.KeeperException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static rs.raf.Util.AMOUNT_OF_NODES;
import static rs.raf.Util.HOST;

public class Dom {

    public static void main(String[] args) throws Exception {

        try {
            // Initialize persistent /votes node
            Util.initPersistentNode();
        } catch (KeeperException.NodeExistsException e){
            System.out.println("The /votes node already exists, you can set the value Util.persistentNodeSet to true in order to avoid this check.");
        }

        // Initialize all nodes
        List<ZooNode> zooNodes = new ArrayList<>();
        for (int i = 0; i < AMOUNT_OF_NODES; i++) {
            zooNodes.add(new ZooNode(HOST));
        }
        // Boot up each node
        zooNodes.forEach(zooNode -> {
            try {
                // Start ZooKeeper instance
                zooNode.startZk();
                // Try to become a leader
                LeaderUtil.tryForLeader(zooNode);
                // Do initial checks and set up watches
                zooNode.init();

            } catch (IOException | InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        });
        // Keep the process running
        Thread.sleep(120000);
    }

}
