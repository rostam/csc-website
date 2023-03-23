// GraphTea Project: http://github.com/graphtheorysoftware/GraphTea
// Copyright (C) 2012 Graph Theory Software Foundation: http://GraphTheorySoftware.com
// Copyright (C) 2008 Mathematical Science Department of Sharif University of Technology
// Distributed under the terms of the GNU General Public License (GPL): http://www.gnu.org/licenses/
package graphtea.extensions.generators;

import graphtea.graph.graph.Edge;
import graphtea.graph.graph.GPoint;
import graphtea.graph.graph.GraphModel;
import graphtea.graph.graph.Vertex;
import graphtea.platform.lang.CommandAttitude;
import graphtea.platform.parameter.Parameter;
import graphtea.platform.parameter.Parametrizable;
import graphtea.plugins.graphgenerator.GraphGenerator;
import graphtea.plugins.graphgenerator.core.PositionGenerators;
import graphtea.plugins.graphgenerator.core.SimpleGeneratorInterface;
import graphtea.plugins.graphgenerator.core.extension.GraphGeneratorExtension;

import java.util.Arrays;
import java.util.Vector;

/**
 * @author azin azadi

 */
@CommandAttitude(name = "generate_pn", abbreviation = "_g_pn")
public class ChainGraph2 implements GraphGeneratorExtension, Parametrizable, SimpleGeneratorInterface {
    @Parameter(name = "Repeat unit (n)")
    public static Integer repeat_unit_n = 9;
    Vertex[] v;

    public String getName() {
        return "Chain Graph 2";
    }

    public String getDescription() {
        return "Chain Graph 2";
    }

    public Vertex[] getVertices() {
        Vertex[] ret = new Vertex[2* repeat_unit_n];
        for (int i = 0; i < 2* repeat_unit_n; i++)
            ret[i] = new Vertex();
        v = ret;
        return ret;
    }

    public Edge[] getEdges() {
        Vector<Edge> ret = new Vector<>(); //new Edge[2 * n - 2 + n/3 + 2];
        for (int i = 0; i < repeat_unit_n - 1; i++) {
            ret.add(new Edge(v[i], v[i + 1]));
        }

        for (int i = 0; i < repeat_unit_n - 1; i++) {
            ret.add(new Edge(v[i + repeat_unit_n], v[i + 1 + repeat_unit_n]));
        }

        for (int i = 0; i < repeat_unit_n; i = i + 2) {
            ret.add(new Edge(v[i], v[repeat_unit_n + i]));
        }

        Edge[] ee = new Edge[ret.size()];

        return ret.toArray(ee);
    }

    static <T> T[] concatWithArrayCopy(T[] array1, T[] array2) {
        T[] result = Arrays.copyOf(array1, array1.length + array2.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    public GPoint[] getVertexPositions() {
        GPoint[] p1 = PositionGenerators.line(5, 5, 10000, 10000, repeat_unit_n);
        GPoint[] p2 = PositionGenerators.line(1000, 5, 10000, 10000, repeat_unit_n);
        return concatWithArrayCopy(p1,p2);
    }

    public String checkParameters() {
        if (repeat_unit_n < 0) return "n must be positive";
        else
            return null;
    }

    public GraphModel generateGraph() {
        return GraphGenerator.getGraph(false, this);
    }


    /**
     * generates a Path Graph with given parameters
     */
    public static GraphModel generatePath(int n) {
        ChainGraph2.repeat_unit_n = n;
        return GraphGenerator.getGraph(false, new ChainGraph2());
    }

    @Override
    public String getCategory() {
        return "Examples";
    }

    public static void main(String[] args) {
        new ChainGraph2().generateGraph();
    }
}
