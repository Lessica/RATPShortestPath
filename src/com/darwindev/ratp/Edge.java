package com.darwindev.ratp;

public class Edge implements Comparable<Edge> {
    private final int v;
    private final int w;
    private final double weight;
    private int betweenness;

    Edge(int either, int other, double power) {
        v = either;
        w = other;
        weight = power;
        betweenness = 0;
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

    public int getBetweenness() {
        return betweenness;
    }

    public void addBetweenness(int delta) {
        betweenness += delta;
    }

    public void cleanBetweenness() {
        betweenness = 0;
    }

    @Override
    public int compareTo(Edge o) {
        return (this.weight > o.weight()) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "(" + Integer.toString(v) + ", " + Integer.toString(w) + ", " + Double.toString(weight) + ", " + Integer.toString(betweenness) + ")";
    }
}
