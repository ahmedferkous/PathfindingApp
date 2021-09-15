package com.example.pathfindingapp;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pathfindingapp.Graph.Node;

import java.util.ArrayList;

public class NodeAdapter extends RecyclerView.Adapter<NodeAdapter.ViewHolder> {
    private static final String TAG = "NodeAdapter";
    public static boolean START_NODE_EXISTS = false;
    public static boolean END_NODE_EXISTS = false;
    public static boolean SAVED = false;
    public static final String START_NODE = "startNode";
    public static final String END_NODE = "endNode";
    public static final String OBSTRUCTION_NODE = "obstructionNode";

    public ArrayList<Node> nodes = new ArrayList<>();
    private Context context;
    private String type;

    public NodeAdapter(Context context, String type) {
        this.context = context;
        this.type = type;
    }

    public void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
        notifyDataSetChanged();
    }

    public ArrayList<Node> getNodes() {
        return nodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.node_item, parent, false);
        return new ViewHolder(view);
    }

    public void edit() {
        ArrayList<Node> copiedNodes = new ArrayList<>();
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            Node newNode = new Node(n.getValue(), type);
            newNode.setObstruction(n.isObstruction());
            newNode.setStartNode(n.isStartNode());
            newNode.setEndNode(n.isEndNode());
            copiedNodes.add(newNode);
        }
        nodes = copiedNodes;
        notifyDataSetChanged();
    }

    public void showPath(ArrayList<Integer> values) {
        for (int i = 0; i < nodes.size(); i++) {
            Node n = nodes.get(i);
            if (values.contains(n.getValue())) {
                if (!(n.isStartNode()) && !(n.isEndNode())) {
                    n.setOnPath(true);

                }
            }
            nodes.set(i, n);
        }
        notifyDataSetChanged();
    }

    public void showProgress(Node n) {
        int pos = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (n.getValue() == nodes.get(i).getValue()) {
                nodes.set(i, n);
                pos = i;
            }
        }
        if (pos != -1) {
            notifyItemChanged(pos);
        }

    }

    public Node receiveNode(boolean receiveEndNode) {
        for (Node n : nodes) {
            if (receiveEndNode) {
                if (n.isEndNode()) {
                    return n;
                }
            } else {
                if (n.isStartNode()) {
                    return n;
                }
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Node boundNode = nodes.get(position);

        if (boundNode.isObstruction()) {
            setNodeType(holder, OBSTRUCTION_NODE);
        } else if (boundNode.isStartNode()) {
            setNodeType(holder, START_NODE);
        } else if (boundNode.isEndNode()) {
            setNodeType(holder, END_NODE);
        } else {
            setNodeType(holder, "");
        }

        if (!SAVED) {
            holder.astarParent.setVisibility(View.GONE);
            holder.dijkstraOrBellmanParent.setVisibility(View.GONE);

            holder.parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!boundNode.isStartNode() && !boundNode.isEndNode()) {
                        if (!boundNode.isObstruction()) {
                            boundNode.setObstruction(true);
                        } else {
                            boundNode.setObstruction(false);
                        }
                        nodes.set(position, boundNode);
                        notifyItemChanged(position);
                    }
                }
            });

            holder.parent.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!boundNode.isObstruction()) {
                        if (START_NODE_EXISTS && END_NODE_EXISTS) {
                            if (boundNode.isStartNode()) {
                                boundNode.setStartNode(false);
                                START_NODE_EXISTS = false;
                            } else if (boundNode.isEndNode()) {
                                boundNode.setEndNode(false);
                                END_NODE_EXISTS = false;
                            }
                        } else if (START_NODE_EXISTS) {
                            if (boundNode.isStartNode()) {
                                boundNode.setStartNode(false);
                                START_NODE_EXISTS = false;
                            } else {
                                boundNode.setEndNode(true);
                                END_NODE_EXISTS = true;
                            }
                        } else if (END_NODE_EXISTS) {
                            if (boundNode.isEndNode()) {
                                boundNode.setEndNode(false);
                                END_NODE_EXISTS = false;
                            } else {
                                boundNode.setStartNode(true);
                                START_NODE_EXISTS = true;
                            }
                        } else {
                            boundNode.setStartNode(true);
                            START_NODE_EXISTS = true;
                        }
                        Log.d(TAG, "onLongClick: " + START_NODE_EXISTS + " " + END_NODE_EXISTS);
                        nodes.set(position, boundNode);
                        notifyItemChanged(position);
                    }
                    return true;
                }
            });
        } else {
            switch (type) {
                case ConfigActivity.A_STAR:
                    if (!boundNode.isObstruction() && !boundNode.isStartNode() && !boundNode.isEndNode() && !(boundNode.g == Integer.MAX_VALUE)) {
                        holder.txtFCost.setText(String.valueOf(boundNode.f));
                        holder.txtGCost.setText(String.valueOf(boundNode.g));
                        holder.txtHCost.setText(String.valueOf(boundNode.h));

                        if (boundNode.isOnPath()) {
                            Log.d(TAG, "onBindViewHolder:PATH " + boundNode.x + " " + boundNode.y);
                            holder.astarParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.blue));
                        } else if (boundNode.getOpen() == 1) {
                            Log.d(TAG, "onBindViewHolder:OPEN " + boundNode.x + " " + boundNode.y);
                            holder.astarParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.green));
                        } else if (boundNode.getOpen() == 0) {
                            Log.d(TAG, "onBindViewHolder:CLOSED " + boundNode.x + " " + boundNode.y);
                            holder.astarParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.red));
                        }
                    } else {
                        holder.astarParent.setVisibility(View.GONE);
                    }
                    break;
                case ConfigActivity.BELLMAN_FORD:
                case ConfigActivity.BFS:
                case ConfigActivity.DIJKSTRA:
                    if (!boundNode.isObstruction() && !boundNode.isStartNode() && !boundNode.isEndNode() && !(boundNode.cost == Node.MAX_VALUE)) {
                        holder.txtCost.setText(String.valueOf(boundNode.cost));

                        if (boundNode.isOnPath()) {
                            holder.dijkstraOrBellmanParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.blue));
                        } else if (boundNode.getOpen() == 1) {
                            holder.dijkstraOrBellmanParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.green));
                        } else if (boundNode.getOpen() == 0) {
                            holder.dijkstraOrBellmanParent.setVisibility(View.VISIBLE);
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.red));
                        }

                    } else {
                        holder.dijkstraOrBellmanParent.setVisibility(View.GONE);
                    }
                    break;
                case ConfigActivity.DFS:
                    if (!boundNode.isObstruction() && !boundNode.isStartNode() && !boundNode.isEndNode()) {
                        if (boundNode.isOnPath()) {
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.blue));
                        } else if (boundNode.getOpen() == 1) {
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.green));
                        } else if (boundNode.getOpen() == 0) {
                            holder.parent.setBackgroundColor(context.getResources().getColor(R.color.red));
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void setNodeType(ViewHolder holder, String type) {
        Resources resources = context.getResources();

        switch (type) {
            case START_NODE:
                holder.parent.setBackgroundColor(resources.getColor(R.color.blue));
                holder.txtStartNode.setVisibility(View.VISIBLE);
                holder.txtEndNode.setVisibility(View.GONE);
                break;
            case END_NODE:
                holder.parent.setBackgroundColor(resources.getColor(R.color.blue));
                holder.txtStartNode.setVisibility(View.GONE);
                holder.txtEndNode.setVisibility(View.VISIBLE);
                break;
            case OBSTRUCTION_NODE:
                holder.parent.setBackgroundColor(resources.getColor(R.color.black));
                break;
            default:
                holder.txtStartNode.setVisibility(View.GONE);
                holder.txtEndNode.setVisibility(View.GONE);
                holder.parent.setBackgroundColor(resources.getColor(R.color.white));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return nodes.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView txtFCost, txtStartNode, txtEndNode, txtGCost, txtHCost, txtCost;
        private RelativeLayout parent, astarParent, dijkstraOrBellmanParent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtFCost = itemView.findViewById(R.id.txtFCost);
            txtStartNode = itemView.findViewById(R.id.txtStartNode);
            txtEndNode = itemView.findViewById(R.id.txtEndNode);
            parent = itemView.findViewById(R.id.parent);
            astarParent = itemView.findViewById(R.id.astarParent);
            dijkstraOrBellmanParent = itemView.findViewById(R.id.dijkstraOrBellmanParent);
            txtGCost = itemView.findViewById(R.id.txtGCost);
            txtHCost = itemView.findViewById(R.id.txtHCost);
            txtCost = itemView.findViewById(R.id.txtCost);
        }
    }
}
