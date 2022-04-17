package server;

import Jama.Matrix;
import graphtea.extensions.io.LoadMtx;
import graphtea.extensions.io.MM;
import graphtea.extensions.reports.coloring.ColumnIntersectionGraph;
import graphtea.extensions.reports.coloring.SpMat;
import graphtea.graph.graph.GraphModel;
import graphtea.plugins.graphgenerator.core.extension.GraphGeneratorExtension;
import graphtea.plugins.main.saveload.core.GraphIOException;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ejml.simple.SimpleMatrix;
import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class Test {
    public static void main(String[] args) throws IOException, JSONException {
        File f = new File("/home/rostam/kara/csc-website/src/main/resources/mats/nos3.mtx");
        GraphModel g = new LoadMtx().read(f);
        GraphModel cig = ColumnIntersectionGraph.from(g);
//        JSONObject s = (JSONObject) new GreedyColoring().calculate(cig);
//        System.out.println(s.toString());

        Matrix m = MM.loadMatrixFromSPARSE(f);
        for (int i = 0; i < m.getColumnDimension(); i++) {
            for (int j = 0; j < m.getColumnDimension(); j++) {
                if (m.get(i, j) != 0) m.set(i, j, 1);
            }
        }

        SimpleMatrix sm = new SimpleMatrix(m.getArray());
        SimpleMatrix col = sm.extractVector(false, 0);
//        JSONArray jsonArray = (JSONArray) s.get("colors");
//        SimpleMatrix[] misses = new SimpleMatrix[(int) s.get("num_of_colors")];
//        for(int i =0 ;i < misses.length;i++) {
//            misses[i] = new SimpleMatrix(sm.numRows(),1);
//            misses[i].zero();
//        }
//
//        for(int i=0;i < jsonArray.length();i++) {
//            int color = (int) jsonArray.get(i);
//            for (int j = 1; j < sm.numCols(); j++) {
//                col = col.plus(sm.extractVector(false, i));
//            }
//        }
//        col.print();
    }
}
