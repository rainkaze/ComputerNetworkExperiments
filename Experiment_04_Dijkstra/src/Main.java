import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet; // For sorted output of nodes

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Graph customGraph = new Graph();
        System.out.print("输入节点数量 (节点将从0到N-1编号): ");
        int numNodes = scanner.nextInt();
        for(int i=0; i<numNodes; i++) {
            customGraph.addNode(i);
        }

        System.out.print("输入边的数量: ");
        int numEdges = scanner.nextInt();
        System.out.println("输入每条边的信息 (格式: 源节点 目标节点 代价)，每行一条:");
        for (int i = 0; i < numEdges; i++) {
            int u = scanner.nextInt();
            int v = scanner.nextInt();
            int cost = scanner.nextInt();
            customGraph.addEdge(u, v, cost);
        }
        System.out.print("输入起始计算节点: ");
        int customStartNode = scanner.nextInt();
        runAndDisplayDijkstra(customGraph, customStartNode);

        scanner.close();
    }

    public static void runAndDisplayDijkstra(Graph graph, int startNode) {
        if (!graph.getAllNodes().contains(startNode)) {
            System.out.println("起始节点 " + startNode + " 不在图中。");
            return;
        }

        DijkstraAlgorithm.Result result = DijkstraAlgorithm.findShortestPaths(graph, startNode);
        Map<Integer, Integer> distances = result.distances;

        System.out.println("从节点 " + startNode + " 到图中各节点的最短路径信息:");
        for (int node : new TreeSet<>(graph.getAllNodes())) {
            System.out.print("  -> 节点 " + node + ": ");
            if (!distances.containsKey(node) || distances.get(node) == Integer.MAX_VALUE) {
                System.out.println("不可达");
            } else {
                System.out.print("距离 = " + distances.get(node));
                List<Integer> path = DijkstraAlgorithm.reconstructPath(node, result);
                System.out.println(", 路径 = " + path);
            }
        }
    }
}