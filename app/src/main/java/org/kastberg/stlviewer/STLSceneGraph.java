package org.kastberg.stlviewer;

import java.util.LinkedList;
import java.util.List;

class STLSceneGraph {
    private static final String TAG = "STLSceneGraph";
    public final List<STLNode> children = new LinkedList<STLNode>();

    public void render() {
        for (STLNode n : children) {
            render(n);
        }
    }

    void render(STLNode node) {
        node.pre();
        for (STLNode n : node.children) {
            render(n);
            n.in();
        }
        node.post();
    }
}
