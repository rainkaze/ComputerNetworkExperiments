import java.util.*;

public class DijkstraAlgorithm {

    public static class Result {
        public final Map<Integer, Integer> distances;
        public final Map<Integer, Integer> previousNodes;
        public final int startNode; // 记录起始节点，方便路径重建

        public Result(Map<Integer, Integer> distances, Map<Integer, Integer> previousNodes, int startNode) {
            this.distances = distances;
            this.previousNodes = previousNodes;
            this.startNode = startNode;
        }
    }

    public static Result findShortestPaths(Graph graph, int startNode) {
        Map<Integer, Integer> distances = new HashMap<>();
        Map<Integer, Integer> previousNodes = new HashMap<>();
        PriorityQueue<NodeEntry> pq = new PriorityQueue<>();

        if (!graph.getAllNodes().contains(startNode)) {
            System.err.println("错误: 起始节点 " + startNode + " 不存在于图中。");
            // 返回空结果或抛出异常
            return new Result(new HashMap<>(), new HashMap<>(), startNode);
        }

        for (int node : graph.getAllNodes()) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(startNode, 0);
        pq.add(new NodeEntry(startNode, 0));

        while (!pq.isEmpty()) {
            NodeEntry currentEntry = pq.poll();
            int u = currentEntry.getNodeId();
            int currentDist = currentEntry.getDistance();

            // 如果这是一个过时的条目（已找到更短的路径），则跳过
            if (currentDist > distances.get(u)) {
                continue;
            }

            for (Edge edge : graph.getNeighbors(u)) {
                int v = edge.getTargetNode();
                int weightUV = edge.getWeight();

                if (distances.get(u) == Integer.MAX_VALUE) continue; // u 本身不可达

                int newDist = distances.get(u) + weightUV;
                if (newDist < distances.get(v)) {
                    distances.put(v, newDist);
                    previousNodes.put(v, u); // v 的前驱是 u
                    pq.add(new NodeEntry(v, newDist));
                }
            }
        }
        return new Result(distances, previousNodes, startNode);
    }

    public static List<Integer> reconstructPath(int targetNode, Result dijkstraResult) {
        LinkedList<Integer> path = new LinkedList<>();
        Map<Integer, Integer> distances = dijkstraResult.distances;
        Map<Integer, Integer> previousNodes = dijkstraResult.previousNodes;
        int startNode = dijkstraResult.startNode;

        if (!distances.containsKey(targetNode) || distances.get(targetNode) == Integer.MAX_VALUE) {
            return Collections.emptyList(); // 目标节点不可达或不存在
        }

        Integer currentNode = targetNode;
        // 从目标节点回溯到起始节点
        while (currentNode != null) {
            path.addFirst(currentNode);
            if (currentNode.equals(startNode)) {
                break; // 到达起始节点
            }
            currentNode = previousNodes.get(currentNode);
            if (currentNode == null && !path.peekFirst().equals(startNode)) {
                // 路径中断但尚未到达起始节点（理论上如果距离不是MAX_VALUE，不应发生）
                return Collections.emptyList(); // 表示路径不完整
            }
        }

        // 如果path的第一个元素不是startNode（比如targetNode就是startNode且没有前驱），
        // 并且targetNode确实是startNode，确保路径是[startNode]。
        // 或者如果路径找到了但第一个不是startNode（异常情况），返回空。
        if (path.isEmpty() && targetNode == startNode) {
            path.addFirst(startNode);
        } else if (path.isEmpty() || !path.peekFirst().equals(startNode)) {
            // 如果路径为空（非startNode的目标不可达）或者路径的起点不正确
            if (targetNode != startNode) return Collections.emptyList();
        }

        return path;
    }
}