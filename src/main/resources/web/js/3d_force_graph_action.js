var gui = new dat.GUI({ autoPlace: false });//new dat.GUI({name: 'My GUI', });
gui.width = 400;
//gui.domElement.id = "parent_canvas";
var vis = gui.addFolder('Visualization');

var defaultVisController = {
    // defaults from https://github.com/anvaka/ngraph.physics.simulator/blob/master/index.js
              timeStep: 20,
              gravity: -1.2,
              theta: 0.8,
              springLength: 30,
              springCoefficient: 0.0008,
              dragCoefficient: 0.02
};
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
dg =vis.add(visController,'dragCoefficient').name("drag coefficient");
gr = vis.add(visController,'gravity').name("Coulomb constant (K)");
sc = vis.add(visController,'springCoefficient').name("Spring coefficient (k)");
sl = vis.add(visController,'springLength').name("Spring length");
theta = vis.add(visController,'theta');
timeStep = vis.add(visController, 'timeStep').name("Time step");

//const settings = new Settings();
//const controllerOne = gui.add(settings, 'redDistance', 0, 100);
//controllerOne.onChange(updateLinkDistance);

var buttonSetDefault = { Reset:function(){
  gui.__folders.Visualization.__controllers[0].setValue(defaultVisController.dragCoefficient);
  gui.__folders.Visualization.__controllers[1].setValue(defaultVisController.gravity);
  gui.__folders.Visualization.__controllers[2].setValue(defaultVisController.springCoefficient);
  gui.__folders.Visualization.__controllers[3].setValue(defaultVisController.springLength);
  gui.__folders.Visualization.__controllers[4].setValue(defaultVisController.theta);
  gui.__folders.Visualization.__controllers[5].setValue(defaultVisController.timeStep);
}};

var localSave = {LocalSave:function() {
    localStorage.setItem('dragCoefficient', gui.__folders.Visualization.__controllers[0].getValue())
    localStorage.setItem('gravity', gui.__folders.Visualization.__controllers[1].getValue())
    localStorage.setItem('springCoefficient', gui.__folders.Visualization.__controllers[2].getValue())
    localStorage.setItem('springLength', gui.__folders.Visualization.__controllers[3].getValue())
    localStorage.setItem('theta', gui.__folders.Visualization.__controllers[4].getValue())
    localStorage.setItem('timeStep', gui.__folders.Visualization.__controllers[5].getValue())
}};

var loadFromLocalSave = {LoadFromLocalSave:function(){
  gui.__folders.Visualization.__controllers[0].setValue(localStorage.getItem('dragCoefficient'));
  gui.__folders.Visualization.__controllers[1].setValue(localStorage.getItem('gravity'));
  gui.__folders.Visualization.__controllers[2].setValue(localStorage.getItem('springCoefficient'));
  gui.__folders.Visualization.__controllers[3].setValue(localStorage.getItem('springLength'));
  gui.__folders.Visualization.__controllers[4].setValue(localStorage.getItem('theta'));
  gui.__folders.Visualization.__controllers[5].setValue(localStorage.getItem('timeStep'));
}};
cooldownTime = {cooldownTime : 15000};
gui.add(buttonSetDefault, 'Reset')
gui.add(localSave,'LocalSave').name("Local save")
gui.add(loadFromLocalSave, 'LoadFromLocalSave').name("Load from local save");
gui.add(cooldownTime, 'cooldownTime').name("Cooldown time")
$('#right_tools').append(gui.domElement);
let nodesWithTheSpecificPropertyDiscussedWithMaxwell = new Set();
Graph1 = {}
function threed_force_graph_action(data,ended) {
        const gData = {
          nodes: data.nodes.map(i => ({ id: i.data.id })),
          links: data.edges.map(i => ({ source: i.data.source, target:i.data.target} ))
        };
    ggdata = {}
    tick = 0;
    let selectedNodes = new Set();
    Graph1 = ForceGraph3D()
              (document.getElementById('canvas'))
              .forceEngine('ngraph')
//                .ngraphPhysics(visController)
                .graphData(gData).onEngineTick(function(){
                       ggData = Graph1.graphData();
                     //  ggData.nodes =
                      // ggData.nodes.map(i => ({id: i.id, position: {x: i.__threeObj.position.x, y:i.__threeObj.position.y, z: i.__threeObj.position.z}}));
                })
                .onEngineStop(function(){
                console.log(Graph1.graphData());
                console.log(ggData);
                ggData.nodes =  ggData.nodes.map(i => ({id: i.id, position: i.__threeObj.position}));
        Graph = ForceGraph3D()
          (document.getElementById('canvas'))
          .forceEngine('ngraph')
            .ngraphPhysics(visController)
            .graphData(ggData)
            .cooldownTime(cooldownTime.cooldownTime)
//                        .backgroundColor('#ffffff')
//                        nodeColor(node => highlightNodes.has(node) ? node === hoverNode ? 'rgb(255,0,0,1)' : 'rgba(255,160,0,0.8)' : 'rgba(0,255,255,0.6)')
            .nodeOpacity(1)
            .nodeColor(function(node){
                    if(selectedNodes.has(node)) return 'yellow';
//                    else if (nodesWithTheSpecificPropertyDiscussedWithMaxwell.has(node)) return 'green';
                    else return 'red';
                    })
            .linkColor('rgba(200,200,200,1)')
                    .linkWidth(2)
                    .linkOpacity(1)
                    .onNodeClick((node, event) => {
                    node.translateX(100);
//                    console.log(node);
//                              if (event.ctrlKey || event.shiftKey || event.altKey) { // multi-selection
//                                selectedNodes.has(node) ? selectedNodes.delete(node) : selectedNodes.add(node);
//                              } else { // single-selection
//                                const untoggle = selectedNodes.has(node) && selectedNodes.size === 1;
//                                selectedNodes.clear();
//                                !untoggle && selectedNodes.add(node);
//                              }
//
//                               // update color of selected nodes
                            }).onEngineTick(function(){
                                tick++;
                                if(tick % 30 == 0) {
                                  stats(Graph,selectedNodes);
                                }
                            })
//                     .onLinkClick((node, event) => {
//                            console.log(node);
//                      })
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
            stats(Graph);
        });

        $('#stopRendering').click(function(){
            Graph.pauseAnimation();
//            console.log('hereee');
//            Graph.renderer().setAnimationLoop(null)
        });

        $('#resumeAnimation').click(function(){
            Graph.resumeAnimation();
        });
        })
}

function threed_force_graph_action_d33d(data,ended) {
        const gData = {
          nodes: data.nodes.map(i => ({ id: i.data.id })),
          links: data.edges.map(i => ({ source: i.data.source, target:i.data.target} ))
        };

    tick = 0;
    let selectedNodes = new Set();
        const Graph = ForceGraph3D()
          (document.getElementById('canvas'))
            .graphData(gData)
            .cooldownTime(cooldownTime.cooldownTime)
//                        .backgroundColor('#ffffff')
//                        nodeColor(node => highlightNodes.has(node) ? node === hoverNode ? 'rgb(255,0,0,1)' : 'rgba(255,160,0,0.8)' : 'rgba(0,255,255,0.6)')
            .nodeOpacity(1)
            .nodeColor(node => selectedNodes.has(node) ? 'red' : 'yellow')
            .linkColor('rgba(200,200,200,1)')
                    .linkWidth(2)
                    .linkOpacity(1)
                    .onNodeClick((node, event) => {
                              if (event.ctrlKey || event.shiftKey || event.altKey) { // multi-selection
                                selectedNodes.has(node) ? selectedNodes.delete(node) : selectedNodes.add(node);
                              } else { // single-selection
                                const untoggle = selectedNodes.has(node) && selectedNodes.size === 1;
                                selectedNodes.clear();
                                !untoggle && selectedNodes.add(node);
                              }

                              Graph.nodeColor(Graph.nodeColor()); // update color of selected nodes
                            }).onEngineTick(function(){
                                tick++;
                                if(tick % 30 == 0) {
                                  stats(Graph,selectedNodes);
                                }
                            });
//                     .onLinkClick((node, event) => {
//
//                      });
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
            stats_d33d(Graph);
        });
}

function stats(Graph,selectedNodes) {
//        console.log(Graph.graphData());
//        console.log(Graph.ngraphPhysics());
        let { nodes, links } = Graph.graphData();
        var min_dist = 10000;
        var max_dist = 0;
        var sum = 0;
        var cnt_nonlink = 0;


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
//                        str += (dist +"").substr(0,6) + ", ";//        nodes.forEach(function(n1) {
//            n1.__threeObj.position.x = n1.__threeObj.position.x;
//            n1.__threeObj.position.y = n1.__threeObj.position.y;
//            n1.__threeObj.position.z = n1.__threeObj.position.z;
//        });


        });
        avg_dist_e = sum_dist_e/links.length;

        nodesWithTheSpecificPropertyDiscussedWithMaxwell.clear();
        nodes.forEach(function(n1) {
              nodes.forEach(function(n2) {
                    if(n1.id > n2.id) {
                        islink = false;
                        links.forEach(function(e) {
                                var src = e.source;
                                 var tgt = e.target;
                                 if( (src == n1.id && tgt == n2.id) ||
                                     (src == n2.id && tgt == n1.id)) {
                                     islink = true;return;
                                 }

                        });

                    if(!islink) {
                      cnt_nonlink++;
                      dist = Math.sqrt(
                        Math.pow(n1.__threeObj.position.x-n2.__threeObj.position.x,2) +
                        Math.pow(n1.__threeObj.position.y-n2.__threeObj.position.y,2) +
                        Math.pow(n1.__threeObj.position.z-n2.__threeObj.position.z,2)
                      );

                      if(dist < 1.6 * max_dist_e) {
                        nodesWithTheSpecificPropertyDiscussedWithMaxwell.add(n1);
                        nodesWithTheSpecificPropertyDiscussedWithMaxwell.add(n2);
                      } else {
                      }

                      if(min_dist > dist)
                        min_dist = dist;
                      if(max_dist < dist)
                        max_dist = dist;
                      sum += dist;
                    }
                    }
                  });
                });
                console.log(cnt_nonlink);

                avg = sum/cnt_nonlink;

        if(nodesWithTheSpecificPropertyDiscussedWithMaxwell.size > 0) Graph.nodeColor(Graph.nodeColor()); //Graph.refresh();

//                    str += "\ncoordinates:\n";
        nodes.forEach(function(n) {
            sx = (n.x + "").substr(0,6);
            sy = (n.y + "").substr(0,6);
            sz = (n.z + "").substr(0,6);
//                        str += sx + ", " + sy + ", " + sz + "\n";
        });

        min_dist_selected_nodes = 1000;
        max_dist_selected_nodes = 0;
        avg_dist_selected_nodes = 0;
        sum_dist_selected_nodes = 0;

        selectedNodes.forEach(function(n1) {
                  selectedNodes.forEach(function(n2) {
                    if(n1.id > n2.id) {
                      dist = Math.sqrt(
                        Math.pow(n1.__threeObj.position.x-n2.__threeObj.position.x,2) +
                        Math.pow(n1.__threeObj.position.y-n2.__threeObj.position.y,2) +
                        Math.pow(n1.__threeObj.position.z-n2.__threeObj.position.z,2)
                      );
                      if(min_dist_selected_nodes > dist)
                        min_dist_selected_nodes = dist;
                      if(max_dist_selected_nodes < dist)
                        max_dist_selected_nodes = dist;
                      sum_dist_selected_nodes += dist;
                    }
                  });
                });

//        avg_dist_selected_nodes = sum_dist_selected_nodes/selectedNodes.size;

        str = "T...T:\nMinimum: " + min_dist;
//        str += "\nMaximum: " + max_dist;
//        str += "\nAverage: " + avg;

        str += "\n\nT-T:\nMinimum: " + min_dist_e;
        str += "\nMaximum: " + max_dist_e;
        str += "\nAverage: " + avg_dist_e;

//        str += "\n\nDistance between selected nodes:\nMinimum: " + min_dist_selected_nodes;
//        str += "\nMaximum: " + max_dist_selected_nodes;
//        str += "\nAverage: " + avg_dist_selected_nodes;


        $("#vis_inf").html(str)
}

function stats_d33d(Graph,selectedNodes) {
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
                Math.pow(n1.x-n2.x,2) +
                Math.pow(n1.y-n2.y,2) +
                Math.pow(n1.z-n2.z,2)
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
                 Math.pow(n1.x-n2.x,2) +
                 Math.pow(n1.y-n2.y,2) +
                 Math.pow(n1.z-n2.z,2)
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

        min_dist_selected_nodes = 1000;
        max_dist_selected_nodes = 0;
        avg_dist_selected_nodes = 0;
        sum_dist_selected_nodes = 0;

        selectedNodes.forEach(function(n1) {
                  selectedNodes.forEach(function(n2) {
                    if(n1.id > n2.id) {
                      dist = Math.sqrt(
                        Math.pow(n1.x-n2.x,2) +
                        Math.pow(n1.y-n2.y,2) +
                        Math.pow(n1.z-n2.z,2)
                      );
                      if(min_dist_selected_nodes > dist)
                        min_dist_selected_nodes = dist;
                      if(max_dist_selected_nodes < dist)
                        max_dist_selected_nodes = dist;
                      sum_dist_selected_nodes += dist;
                    }
                  });
                });

        avg_dist_selected_nodes = sum_dist_selected_nodes/selectedNodes.size;

        str = "Distance between all nodes:\nMinimum: " + min_dist;
        str += "\nMaximum: " + max_dist;
        str += "\nAverage: " + avg;

        str += "\n\nDistance between all edge nodes:\nMinimum: " + min_dist_e;
        str += "\nMaximum: " + max_dist_e;
        str += "\nAverage: " + avg_dist_e;

        str += "\n\nDistance between selected nodes:\nMinimum: " + min_dist_selected_nodes;
        str += "\nMaximum: " + max_dist_selected_nodes;
        str += "\nAverage: " + avg_dist_selected_nodes;


        $("#vis_inf").html(str)
}