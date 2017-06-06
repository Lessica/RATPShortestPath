package com.darwindev.ratp;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by Zheng on 31/05/2017.
 *
 */
public class BFSShortestPath {

    private int sourceNode;
    private boolean[] marked;
    private Edge[] edgeTo;
    private int[] distance;

    public BFSShortestPath() {
        sourceNode = -1;
        marked = null;
        edgeTo = null;
        distance = null;
    }

    public BFSShortestPath(BFSShortestPath aBFSShortestPath) {
        sourceNode = aBFSShortestPath.sourceNode;
        marked = aBFSShortestPath.marked;
        edgeTo = aBFSShortestPath.edgeTo;
        distance = aBFSShortestPath.distance;
    }

    public ArrayList<ArrayList<Integer>> bfsAll(EdgeWeightedGraph G) {
        int v = G.getNodeCount() + 1;
        marked = new boolean[v];
        edgeTo = new Edge[v];
        distance = new int[v];
        for (int i = 0; i < v; i++) {
            edgeTo[i] = null; // UNDEFINED
            distance[i] = Integer.MAX_VALUE; // +INFINITY
        }
        ArrayList<ArrayList<Integer>> result = new ArrayList<>();
        for (int i = 0; i < v - 1; i++) {
            if (!marked[i]) {
                result.add(parse(G, i));
            }
        }
        return result;
    }

    private ArrayList<Integer> parse(EdgeWeightedGraph G, int s) {
        sourceNode = s;
        // Use an array list to record visit orders.
        ArrayList<Integer> visitOrder = new ArrayList<>();
        // Perform search algorithm without recursive calls.
        // BFS uses Queue data structure.
        Queue<Integer> queue = new LinkedList<>();
        Queue<Integer> distanceQueue = new LinkedList<>();
        // Put root node into queue
        // Put distance into queue (correspond to node)
        queue.add(s);
        marked[s] = true;
        edgeTo[s] = null;
        distanceQueue.add(0);
        distance[s] = 0;
        while (!queue.isEmpty()) {
            int node = queue.remove();
            int nodeDistance = distanceQueue.remove();
            visitOrder.add(node);
            // In case of choice, the vertex with the smallest identifier will be chosen.
            for (Edge childEdge : G.getNode(node)) {
                int thisNode = childEdge.other(node);
                if (!marked[thisNode]) {
                    queue.add(thisNode);
                    // Mark child node
                    marked[thisNode] = true;
                    edgeTo[thisNode] = childEdge;
                    // Update distance
                    distanceQueue.add(nodeDistance + 1);
                    distance[thisNode] = nodeDistance + 1;
                }
            }
        }
        return visitOrder;
    }

    // The function to do BFS traversal.
    public ArrayList<Integer> bfs(EdgeWeightedGraph G, int s) {
        int v = G.getNodeCount() + 1;
        marked = new boolean[v];
        edgeTo = new Edge[v];
        distance = new int[v];
        for (int i = 0; i < v; i++) {
            edgeTo[i] = null; // UNDEFINED
            distance[i] = Integer.MAX_VALUE; // +INFINITY
        }
        return parse(G, s);
    }

    public boolean hasPathTo(int v) {
        return marked[v];
    }

    public int distTo(int v) {
        return distance[v];
    }

    public Edge edgeTo(int v) { return edgeTo[v]; }

    public ArrayList<Integer> pathTo(int w) {
        ArrayList<Integer> shortestPath = new ArrayList<>();
        int thisNode = w;
        while (thisNode > -1 && thisNode != sourceNode) {
            shortestPath.add(thisNode);
            thisNode = edgeTo[thisNode].other(thisNode);
            if (thisNode == sourceNode) {
                shortestPath.add(sourceNode);
                break;
            }
        }
        Collections.reverse(shortestPath);
        return shortestPath;
    }

    public static boolean isConnectedGraph(EdgeWeightedGraph G) {
        Integer total = new BFSShortestPath().bfs(G, 0).size();
        return total == G.getNodeCount();
    }

    public static void main(String[] args) throws IOException {

        FileReader reader = new FileReader("data-output/stop-detail.json");
        Type type = new TypeToken<HashMap<Integer, Map>>(){}.getType();
        Gson gson = new Gson();
        HashMap<Integer, Map> stopMap = gson.fromJson(reader, type);

        EdgeWeightedGraph graph = new EdgeWeightedGraph("data-output/edge.csv");

        BFSShortestPath BFSShortestPath = new BFSShortestPath();
        BFSShortestPath.bfs(graph, 77);
        ArrayList<Integer> suggestedPath = BFSShortestPath.pathTo(67);
        System.out.println(suggestedPath);
        for (Integer stopId : suggestedPath) {
            Map stopDetail = stopMap.get(stopId);
            System.out.println(stopDetail.get("name"));
        }

    }

}
