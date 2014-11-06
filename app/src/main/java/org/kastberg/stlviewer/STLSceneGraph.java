package org.kastberg.stlviewer;

import java.util.LinkedList;
import java.util.List;

class STLSceneGraph {
    private static final String TAG = "STLSceneGraph";
    public STLCamera camera = null;
    public final List<STLNode> children = new LinkedList<STLNode>();
    public final List<Integer> shaders = new LinkedList<Integer>();


    public void render() {
        for (STLNode n : children) {
            render(n);
        }
    }

    void render(STLNode node) {
        node.pre(this);
        for (STLNode n : node.children) {
            render(n);
            n.in(this);
        }
        node.post(this);
    }
}
