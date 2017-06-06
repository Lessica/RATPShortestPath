package com.darwindev.ratp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class EdgeWeightedGraph {


    private int capacity = 0;   // capacity of graph
    private int nodeCount = 0; // number of vertex
    private int edgeCount = 0; // number of edge

    // Array of lists for Adjacency List Representation
    private LinkedList<Edge>[] adj;

    // Constructor
    public EdgeWeightedGraph(int v) {
        capacity = v;
        adj = new LinkedList[capacity];
    }

    // Constructor: build graph from file
    public EdgeWeightedGraph(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath),
                StandardCharsets.UTF_8);
        HashSet<Integer> set = new HashSet<>();
        Integer lineIndex = 0;
        for (String line : lines) {
            lineIndex++;
            if (lineIndex == 1) continue;
//            String[] nodesId = line.trim().split("\\s+");
            String[] nodesId = CSVUtils.parseLine(line).toArray(new String[0]);
            if (nodesId.length >= 2) {
                set.add(Integer.parseInt(nodesId[0]));
                set.add(Integer.parseInt(nodesId[1]));
            }
        }
        ArrayList<Integer> nodeIds = new ArrayList<Integer>(set);
        int size = Collections.max(nodeIds) + 1;
        capacity = size;
        adj = new LinkedList[size];
        for (int i = 0; i < size; ++i)
            addVertex(i);
        lineIndex = 0;
        for (String line : lines) {
            lineIndex++;
            if (lineIndex == 1) continue;
//            String[] nodesId = line.split("\\s+");
            String[] nodesId = CSVUtils.parseLine(line).toArray(new String[0]);
            if (nodesId.length == 2) {
                addEdge(new Edge(Integer.parseInt(nodesId[0]), Integer.parseInt(nodesId[1]), 1));
            } else if (nodesId.length == 3) {
                addEdge(new Edge(Integer.parseInt(nodesId[0]), Integer.parseInt(nodesId[1]), Double.parseDouble(nodesId[2])));
            }
        }
    }

    public void addVertex(int v) {
        adj[v] = new LinkedList<>();
        nodeCount = nodeCount + 1;
    }

    // Function to add an edge into the graph
    public void addEdge(Edge e) {
        adj[e.either()].add(e);  // Add w to v's list.
        adj[e.other(e.either())].add(e); // Add v to w's list.
        edgeCount = edgeCount + 1;
    }

    public LinkedList<Edge> getNode(int v) {
        return adj[v];
    }

    public Edge getEdge(int v, int w) {
        LinkedList<Edge> source = getNode(v);
        for (Edge edge : source) {
            if (edge.other(v) == w) {
                return edge;
            }
        }
        return null;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getCapacity() {
        return capacity;
    }

    public HashSet<Edge> edges() {
        HashSet<Edge> edges = new HashSet<>();
        for (LinkedList<Edge> anAdj : adj) {
            edges.addAll(anAdj);
        }
        return edges;
    }

    public void removeEdge(Edge e) {
        for (int i = 0; i < capacity; i++) {
            if (adj[i] != null) {
                for (Edge n : adj[i]) {
                    if (n == e) {
                        adj[i].remove(e);
                        break;
                    }
                }
            }
        }
    }

    // Print the whole graph
    public void print() {
        for (int i = 0; i < capacity; i++) {
            System.out.print(i + ": ");
            if (adj[i] != null) {
                for (Edge n : adj[i]) {
                    System.out.print(n + ", ");
                }
            }
            System.out.println();
        }
    }

}
