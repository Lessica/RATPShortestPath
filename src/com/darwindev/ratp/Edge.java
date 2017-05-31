package com.darwindev.ratp;

public class Edge implements Comparable<Edge> {
    private final int v;
    private final int w;
    private final double weight;

    Edge(int either, int other, double power) {
        v = either;
        w = other;
        weight = power;
    }

    public int either() {
        return v;
    }

    public int other(int vertex) {
        if      (vertex == v) return w;
        else if (vertex == w) return v;
        throw new IllegalArgumentException("Illegal endpoint");
    }

    public double weight() {
        return weight;
    }

    @Override
    public int compareTo(Edge o) {
        return (this.weight > o.weight()) ? 1 : -1;
    }
}
