public class Edge {
    int targetNode;
    int weight;

    public Edge(int targetNode, int weight) {
        this.targetNode = targetNode;
        this.weight = weight;
    }

    public int getTargetNode() {
        return targetNode;
    }

    public int getWeight() {
        return weight;
    }
}