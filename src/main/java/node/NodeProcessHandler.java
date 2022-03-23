package node;

import lombok.Setter;

import java.io.IOException;

import static java.lang.String.format;
import static java.lang.Thread.sleep;
import static util.NodeUtils.addNodeLog;

public class NodeProcessHandler {

    private static final Long SLEEP_TIME = 6000L;
    private static final Long CLONE_DISCOVERY_TIME = 10000L;
    private static final Long SYNC_SLEEP_TIME = 20000L;

    private final Node node;

    public NodeProcessHandler(Node node) {
        this.node = node;

        logBlocks();
        logClones();
        findClones();
        synchronizeBlocks();
    }

    private void findClones() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(CLONE_DISCOVERY_TIME);
                    node.getClonesFromClones();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void synchronizeBlocks() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SYNC_SLEEP_TIME);
                    node.getBlocksFromClones();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void logBlocks() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SLEEP_TIME);
                    addNodeLog(node, format("blocks: %s", node.getBlocks()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void logClones() {
        Thread thread = new Thread(() -> {
            while (true) {
                try {
                    sleep(SLEEP_TIME);
                    addNodeLog(node, format("clones: %s", node.getClones()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
}
