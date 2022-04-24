var gui = new dat.GUI({ autoPlace: false });//new dat.GUI({name: 'My GUI', });
//gui.domElement.id = "parent_canvas";
var vis = gui.addFolder('Visualization');
//var dragCoefficientController = {dragCoeffiecient: 0.2};
var visController = {
    // defaults from https://github.com/anvaka/ngraph.physics.simulator/blob/master/index.js
              timeStep: 20,
              gravity: -1.2,
              theta: 0.8,
              springLength: 30,
              springCoefficient: 0.0008,
              dragCoefficient: 0.02
};
vis.add(visController,'dragCoefficient');
vis.add(visController,'gravity');
vis.add(visController,'springCoefficient');
vis.add(visController,'springLength');
$('#right_tools').append(gui.domElement);

function threed_force_graph_action(data,ended) {
        const gData = {
          nodes: data.nodes.map(i => ({ id: i.data.id })),
          links: data.edges.map(i => ({ source: i.data.source, target:i.data.target} ))
        };

        console.log(visController);
        const Graph = ForceGraph3D()
          (document.getElementById('canvas'))
          .forceEngine('ngraph')
            .ngraphPhysics(visController)
            .graphData(gData)
//                        .backgroundColor('#ffffff')
//                        nodeColor(node => highlightNodes.has(node) ? node === hoverNode ? 'rgb(255,0,0,1)' : 'rgba(255,160,0,0.8)' : 'rgba(0,255,255,0.6)')
            .nodeOpacity(1)
            .linkColor('rgba(200,200,200,1)')
                    .linkWidth(2)
                    .linkOpacity(1)
//                                .linkDirectionalParticles(link => highlightLinks.has(link) ? 4 : 0)
//                                .linkDirectionalParticleWidth(4)
//                                .onNodeHover(node => {
//                                   no state change
//                                  if ((!node && !highlightNodes.size) || (node && hoverNode === node)) return;
//
//                                  highlightNodes.clear();
//                                  highlightLinks.clear();
//                                  if (node) {
//                                    highlightNodes.add(node);
//                                    node.neighbors.forEach(neighbor => highlightNodes.add(neighbor));
//                                    node.links.forEach(link => highlightLinks.add(link));
//                                  }
//
//                                  hoverNode = node || null;
//
//                                  updateHighlight();
//                                })
//                                .onLinkHover(link => {
//                                  highlightNodes.clear();
//                                  highlightLinks.clear();
//
//                                  if (link) {
//                                    highlightLinks.add(link);
//                                    highlightNodes.add(link.source);
//                                    highlightNodes.add(link.target);
//                                  }
//
//                                  updateHighlight();
//                                });
        ended();
        $('#compute_stat_on_vis').click(function() {
//        });
//        Graph.onEngineStop(function(){
//        return;
        let { nodes, links } = Graph.graphData();
//        console.log(nodes);
//        console.log(links);
        var min_dist = 10000;
        var max_dist = 0;
        var sum = 0;
//        nodes.forEach(function(n1) {
//            n1.__threeObj.position.x = n1.__threeObj.position.x;
//            n1.__threeObj.position.y = n1.__threeObj.position.y;
//            n1.__threeObj.position.z = n1.__threeObj.position.z;
//        });

        nodes.forEach(function(n1) {
          nodes.forEach(function(n2) {
            if(n1.id > n2.id) {
              dist = Math.sqrt(
                Math.pow(n1.__threeObj.position.x-n2.__threeObj.position.x,2) +
                Math.pow(n1.__threeObj.position.y-n2.__threeObj.position.y,2) +
                Math.pow(n1.__threeObj.position.z-n2.__threeObj.position.z,2)
              );
              if(min_dist > dist)
                min_dist = dist;
              if(max_dist < dist)
                max_dist = dist;
              sum += dist;
            }
          });
        });

        avg = sum/nodes.length;

        min_dist_e = 1000;
        max_dist_e = 0;
        avg_dist_e = 0;
        sum_dist_e = 0;


        links.forEach(function(e) {
            var src = e.source;
            var tgt = e.target;
            var n1 = nodes[src];
            var n2 = nodes[tgt];
            dist = Math.sqrt(
                 Math.pow(n1.__threeObj.position.x-n2.__threeObj.position.x,2) +
                 Math.pow(n1.__threeObj.position.y-n2.__threeObj.position.y,2) +
                 Math.pow(n1.__threeObj.position.z-n2.__threeObj.position.z,2)
            );
            if(min_dist_e > dist)
                min_dist_e = dist;
              if(max_dist_e < dist)
                max_dist_e = dist;
              sum_dist_e += dist;
//                        str += (dist +"").substr(0,6) + ", ";

        });
        avg_dist_e = sum_dist_e/links.length;

//                    str += "\ncoordinates:\n";
        nodes.forEach(function(n) {
            sx = (n.x + "").substr(0,6);
            sy = (n.y + "").substr(0,6);
            sz = (n.z + "").substr(0,6);
//                        str += sx + ", " + sy + ", " + sz + "\n";
        });

        str = "\nminimum distance between all nodes:\n" + min_dist;
        str += "\nmaximum distance between all nodes:\n" + max_dist;
        str += "\naverage distance between all nodes:\n" + avg;

        str += "\nminimum distance between all edge nodes:\n" + min_dist_e;
        str += "\nmaximum distance between all edge nodes:\n" + max_dist_e;
        str += "\naverage distance between all edge nodes:\n" + avg_dist_e;

        $("#vis_inf").html(str)
        });
}