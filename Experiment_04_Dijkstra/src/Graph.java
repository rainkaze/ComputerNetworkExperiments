import java.util.*;

public class Graph {
    private final Map<Integer, List<Edge>> adjList;
    private final Set<Integer> nodes;

    public Graph() {
        this.adjList = new HashMap<>();
        this.nodes = new HashSet<>();
    }

    public void addNode(int nodeId) {
        nodes.add(nodeId);
        adjList.putIfAbsent(nodeId, new ArrayList<>());
    }

    /**
     * 添加一条从 source 到 destination 的有向边，权重为 weight。
     * 如果需要无向图，则需要为 (destination, source) 也添加一条边。
     */
    public void addEdge(int source, int destination, int weight) {
        addNode(source); // 确保源节点存在
        addNode(destination); // 确保目标节点存在
        adjList.get(source).add(new Edge(destination, weight));
    }

    public List<Edge> getNeighbors(int nodeId) {
        return adjList.getOrDefault(nodeId, Collections.emptyList());
    }

    public Set<Integer> getAllNodes() {
        return Collections.unmodifiableSet(nodes);
    }
}