package com.example.pathfindingapp.Graph;

public class Edge {
    public Node from;
    public Node to;
    private int weight;

    public Edge(Node from, Node to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }


    public int getWeight() {
        return weight;
    }
}
