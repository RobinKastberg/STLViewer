package org.kastberg.stlviewer;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by kastberg on 10/31/2014.
 */
public class STLSceneGraph extends STLNode{

    public void render() {
        for(STLNode n : children) {
            render(n);
        }
    }
    public void render(STLNode node) {
        node.pre();
        for(STLNode n : node.children) {
            render(n);
            n.in();
        }
        node.post();
    }
}
