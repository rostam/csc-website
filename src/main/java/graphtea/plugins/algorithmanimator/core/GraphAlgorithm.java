package graphtea.plugins.algorithmanimator.core;

import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.Vertex;
import graphtea.library.algorithms.Algorithm;
import graphtea.library.event.VertexRequest;
import graphtea.platform.core.BlackBoard;
import graphtea.plugins.main.GraphData;

/**
 * author: azin azadi
 */
public class GraphAlgorithm extends Algorithm {

    protected BlackBoard blackBoard;
    public GraphData graphData;

    public GraphAlgorithm(BlackBoard blackBoard){
        this.blackBoard = blackBoard;
        this.graphData = new GraphData(blackBoard);
    }
    
    public Vertex requestVertex(GraphModel g,String msg){
        VertexRequest<Vertex, Edge> vr = new VertexRequest<>(g, msg);
        dispatchEvent(vr);
        return vr.getVertex();
    }
    
    public String getMatrixHTML(GraphModel g){
        return graphData.saveLoad.Graph2MatrixHTML(g);
    }

}
