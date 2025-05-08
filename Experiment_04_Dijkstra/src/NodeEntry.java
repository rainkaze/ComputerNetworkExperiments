public class NodeEntry implements Comparable<NodeEntry> {
    int nodeId;
    int distance;

    public NodeEntry(int nodeId, int distance) {
        this.nodeId = nodeId;
        this.distance = distance;
    }

    public int getNodeId() {
        return nodeId;
    }

    public int getDistance() {
        return distance;
    }

    @Override
    public int compareTo(NodeEntry other) {
        return Integer.compare(this.distance, other.distance);
    }
}