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
            Util.initPersistentNode();
        } catch (KeeperException.NodeExistsException e){
            System.out.println("The /votes node already exists, you can set the value Util.persistentNodeSet to true in order to avoid this check.");
        }

        List<ZooNode> zooNodes = new ArrayList<>();

        for (int i = 0; i < AMOUNT_OF_NODES; i++) {
            zooNodes.add(new ZooNode(HOST));
        }
        zooNodes.forEach(zooNode -> {
            try {
                zooNode.startZk();
                LeaderUtil.tryForLeader(zooNode);
                zooNode.init();

            } catch (IOException | InterruptedException | KeeperException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(120000);
    }

}
